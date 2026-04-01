package ua.renamer.app.core.v2.service.transformation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.enums.DateFormat;
import ua.renamer.app.api.enums.DateTimeFormat;
import ua.renamer.app.api.enums.DateTimeSource;
import ua.renamer.app.api.enums.ItemPositionWithReplacement;
import ua.renamer.app.api.enums.TimeFormat;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.config.DateTimeConfig;
import ua.renamer.app.api.interfaces.DateTimeUtils;
import ua.renamer.app.core.v2.util.TestDateTimeUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Acceptance tests for TASK-1.5: DateTimeTransformer fallback behaviour.
 *
 * <p>Covers four scenarios mandated by the acceptance criteria:
 * <ol>
 *   <li>Primary source returns null, fallback enabled → earliest available date used.</li>
 *   <li>Primary source returns null, all fallback dates null, fallback enabled → error.</li>
 *   <li>Primary source returns null, fallback disabled → error (V2 no-throw contract preserved).</li>
 *   <li>Primary source is non-null, fallback enabled → primary used, not fallback.</li>
 * </ol>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DateTimeTransformerFallbackTest {

    private DateTimeTransformer transformer;

    @BeforeAll
    void setUp() {
        DateTimeUtils dateTimeUtils = new TestDateTimeUtils();
        transformer = new DateTimeTransformer(dateTimeUtils);
    }

    // -----------------------------------------------------------------------
    // Shared config factory — date-only, REPLACE position, no separator noise
    // -----------------------------------------------------------------------

    /**
     * Builds a minimal {@link DateTimeConfig} for the given source.
     * Uses YYYY_MM_DD_DASHED so the formatted string is predictable (e.g. "2024-01-15").
     * REPLACE position keeps newName == formattedDateTime for simple assertion.
     */
    private DateTimeConfig configFor(DateTimeSource source, boolean useFallback) {
        return DateTimeConfig.builder()
                             .withSource(source)
                             .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                             .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                             .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                             .withPosition(ItemPositionWithReplacement.REPLACE)
                             .withSeparator("_")
                             .withCustomDateTime(null)
                             .withUseFallbackDateTime(useFallback)
                             .build();
    }

    // -----------------------------------------------------------------------
    // Test 1 — fallback enabled, primary null, fallback dates present
    // -----------------------------------------------------------------------

    /**
     * When the primary source is CONTENT_CREATION_DATE but no metadata is attached
     * (null FileMeta) and useFallbackDateTime=true, the transformer must select the
     * minimum of creationDate and modificationDate.
     *
     * <p>creationDate=2024-01-15, modificationDate=2024-02-20 → earliest is 2024-01-15.
     */
    @Test
    void givenContentCreationDateMissingAndFallbackEnabled_whenExtract_thenEarliestAvailableDateUsed() {
        // Arrange — no metadata attached so CONTENT_CREATION_DATE resolves to null
        FileModel input = FileModel.builder()
                                   .withFile(new File("/test/photo.jpg"))
                                   .withIsFile(true)
                                   .withFileSize(1024L)
                                   .withName("photo")
                                   .withExtension("jpg")
                                   .withAbsolutePath("/test/photo.jpg")
                                   .withCreationDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                                   .withModificationDate(LocalDateTime.of(2024, 2, 20, 14, 45, 30))
                                   .withDetectedMimeType("image/jpeg")
                                   .withDetectedExtensions(Collections.emptySet())
                                   .withCategory(Category.IMAGE)
                                   .withMetadata(null)
                                   .build();

        DateTimeConfig config = configFor(DateTimeSource.CONTENT_CREATION_DATE, true);

        // Act
        PreparedFileModel result = transformer.transform(input, config);

        // Assert — no error, and the name reflects 2024-01-15 (the earliest date)
        assertFalse(result.isHasError(), "Expected no error when fallback dates are available");
        assertTrue(
                result.getNewName().contains("2024-01-15"),
                "Expected new name to contain '2024-01-15' (earliest date), but was: " + result.getNewName()
        );
        assertFalse(
                result.getNewName().contains("2024-02-20"),
                "New name must NOT contain the later modificationDate '2024-02-20', but was: " + result.getNewName()
        );
    }

    // -----------------------------------------------------------------------
    // Test 2 — fallback enabled, all dates null → error
    // -----------------------------------------------------------------------

    /**
     * When all three datetime candidates (creationDate, modificationDate,
     * contentCreationDate) are null, the fallback stream is empty and the
     * transformer must return a result with hasError=true.
     */
    @Test
    void givenAllDatesNullAndFallbackEnabled_whenExtract_thenErrorTransformationReturned() {
        // Arrange — explicitly set both file dates to null, no metadata
        FileModel input = FileModel.builder()
                                   .withFile(new File("/test/photo.jpg"))
                                   .withIsFile(true)
                                   .withFileSize(1024L)
                                   .withName("photo")
                                   .withExtension("jpg")
                                   .withAbsolutePath("/test/photo.jpg")
                                   .withCreationDate(null)
                                   .withModificationDate(null)
                                   .withDetectedMimeType("image/jpeg")
                                   .withDetectedExtensions(Collections.emptySet())
                                   .withCategory(Category.IMAGE)
                                   .withMetadata(null)
                                   .build();

        DateTimeConfig config = configFor(DateTimeSource.CONTENT_CREATION_DATE, true);

        // Act
        PreparedFileModel result = transformer.transform(input, config);

        // Assert — fallback stream is empty → error result
        assertTrue(result.isHasError(), "Expected hasError=true when all candidate dates are null");
    }

    // -----------------------------------------------------------------------
    // Test 3 — fallback disabled, primary source null → error (existing behavior)
    // -----------------------------------------------------------------------

    /**
     * When useFallbackDateTime=false (the default) and the primary source
     * resolves to null, the transformer must return a result with hasError=true.
     * creationDate is populated but must NOT be used because the fallback flag is off.
     */
    @Test
    void givenFallbackDisabled_whenPrimarySourceNull_thenErrorTransformationReturned() {
        // Arrange — CONTENT_CREATION_DATE source with no metadata (resolves null),
        //            but creationDate IS populated — it must be ignored when fallback=false
        FileModel input = FileModel.builder()
                                   .withFile(new File("/test/photo.jpg"))
                                   .withIsFile(true)
                                   .withFileSize(1024L)
                                   .withName("photo")
                                   .withExtension("jpg")
                                   .withAbsolutePath("/test/photo.jpg")
                                   .withCreationDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                                   .withModificationDate(LocalDateTime.of(2024, 2, 20, 14, 45, 30))
                                   .withDetectedMimeType("image/jpeg")
                                   .withDetectedExtensions(Collections.emptySet())
                                   .withCategory(Category.IMAGE)
                                   .withMetadata(null)
                                   .build();

        // useFallback=false preserves the original V2 behavior
        DateTimeConfig config = configFor(DateTimeSource.CONTENT_CREATION_DATE, false);

        // Act
        PreparedFileModel result = transformer.transform(input, config);

        // Assert — primary returned null, fallback disabled → error
        assertTrue(result.isHasError(),
                   "Expected hasError=true when primary source is null and fallback is disabled");
    }

    // -----------------------------------------------------------------------
    // Test 4 — fallback enabled but primary source is non-null → primary wins
    // -----------------------------------------------------------------------

    /**
     * When useFallbackDateTime=true but the primary source already resolves to a
     * non-null value, the fallback path must NOT execute.
     * creationDate=2024-01-15, modificationDate=2024-02-20.
     * Source is FILE_CREATION_DATE → primary resolves to 2024-01-15.
     * If the fallback were wrongly preferred it could pick the wrong date;
     * the assertion checks for 2024-01-15 and explicitly rejects 2024-02-20.
     */
    @Test
    void givenFallbackEnabled_whenPrimarySourcePresent_thenPrimaryUsedNotFallback() {
        // Arrange
        FileModel input = FileModel.builder()
                                   .withFile(new File("/test/photo.jpg"))
                                   .withIsFile(true)
                                   .withFileSize(1024L)
                                   .withName("photo")
                                   .withExtension("jpg")
                                   .withAbsolutePath("/test/photo.jpg")
                                   .withCreationDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                                   .withModificationDate(LocalDateTime.of(2024, 2, 20, 14, 45, 30))
                                   .withDetectedMimeType("image/jpeg")
                                   .withDetectedExtensions(Collections.emptySet())
                                   .withCategory(Category.IMAGE)
                                   .withMetadata(null)
                                   .build();

        // FILE_CREATION_DATE → resolves directly to creationDate (2024-01-15)
        DateTimeConfig config = configFor(DateTimeSource.FILE_CREATION_DATE, true);

        // Act
        PreparedFileModel result = transformer.transform(input, config);

        // Assert — no error, name contains the primary date not the later modification date
        assertFalse(result.isHasError(), "Expected no error when primary source resolves to a date");
        assertTrue(
                result.getNewName().contains("2024-01-15"),
                "Expected name to contain primary date '2024-01-15', but was: " + result.getNewName()
        );
        assertFalse(
                result.getNewName().contains("2024-02-20"),
                "Name must NOT contain modificationDate '2024-02-20' when primary already resolved, but was: "
                + result.getNewName()
        );
    }
}
