package ua.renamer.app.core.service.transformation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.enums.DateFormat;
import ua.renamer.app.api.enums.DateTimeFormat;
import ua.renamer.app.api.enums.DateTimeSource;
import ua.renamer.app.api.enums.ItemPositionWithReplacement;
import ua.renamer.app.api.enums.TimeFormat;
import ua.renamer.app.api.interfaces.DateTimeUtils;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.config.DateTimeConfig;
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

    /**
     * Builds a minimal {@link DateTimeConfig} with both {@code useFallbackDateTime=true}
     * and {@code useCustomDateTimeAsFallback=true}, for testing the custom-date fallback path.
     */
    private DateTimeConfig configForWithCustomFallback(DateTimeSource source, LocalDateTime customDt) {
        return DateTimeConfig.builder()
                .withSource(source)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.REPLACE)
                .withSeparator("_")
                .withCustomDateTime(customDt)
                .withUseFallbackDateTime(true)
                .withUseCustomDateTimeAsFallback(true)
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

    // -----------------------------------------------------------------------
    // Test 5 — all natural dates null, custom fallback enabled → custom date used
    // -----------------------------------------------------------------------

    /**
     * When all natural dates are null and {@code useCustomDateTimeAsFallback=true}
     * with a non-null {@code customDateTime}, the transformer must use the custom date.
     */
    @Test
    void givenAllNaturalDatesNullAndCustomFallbackEnabled_whenExtract_thenCustomDateUsed() {
        // Arrange — no creation, no modification, no metadata
        FileModel input = FileModel.builder()
                .withFile(new File("/test/scan.pdf"))
                .withIsFile(true)
                .withFileSize(2048L)
                .withName("scan")
                .withExtension("pdf")
                .withAbsolutePath("/test/scan.pdf")
                .withCreationDate(null)
                .withModificationDate(null)
                .withDetectedMimeType("application/pdf")
                .withDetectedExtensions(Collections.emptySet())
                .withCategory(Category.GENERIC)
                .withMetadata(null)
                .build();

        DateTimeConfig config = configForWithCustomFallback(
                DateTimeSource.CONTENT_CREATION_DATE,
                LocalDateTime.of(2023, 6, 1, 0, 0, 0)
        );

        // Act
        PreparedFileModel result = transformer.transform(input, config);

        // Assert — no error, custom date used
        assertFalse(result.isHasError(), "Expected no error when custom fallback is enabled and customDateTime is set");
        assertTrue(
                result.getNewName().contains("2023-06-01"),
                "Expected new name to contain '2023-06-01' (custom fallback), but was: " + result.getNewName()
        );
    }

    // -----------------------------------------------------------------------
    // Test 6 — custom fallback enabled but customDateTime null → min natural dates
    // -----------------------------------------------------------------------

    /**
     * When {@code useCustomDateTimeAsFallback=true} but {@code customDateTime} is null,
     * the transformer must fall back to the minimum of the natural dates.
     */
    @Test
    void givenCustomFallbackEnabledButCustomDateNull_whenExtract_thenFallsBackToMinNaturalDates() {
        // Arrange — natural dates present; customDateTime is null
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

        // customDt=null — custom fallback path must be skipped
        DateTimeConfig config = configForWithCustomFallback(DateTimeSource.CONTENT_CREATION_DATE, null);

        // Act
        PreparedFileModel result = transformer.transform(input, config);

        // Assert — no error, earliest natural date used
        assertFalse(result.isHasError(), "Expected no error when natural dates are available");
        assertTrue(
                result.getNewName().contains("2024-01-15"),
                "Expected '2024-01-15' (min natural date), but was: " + result.getNewName()
        );
        assertFalse(
                result.getNewName().contains("2023"),
                "Name must NOT contain '2023' (custom date was null), but was: " + result.getNewName()
        );
    }

    // -----------------------------------------------------------------------
    // Test 7 — primary source present, custom fallback enabled → primary wins
    // -----------------------------------------------------------------------

    /**
     * When the primary source resolves to a non-null value, the fallback block
     * (including the custom-date path) must not execute.
     */
    @Test
    void givenCustomFallbackEnabledAndPrimarySourcePresent_thenPrimaryUsedNotCustom() {
        // Arrange — FILE_CREATION_DATE resolves directly to creationDate
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

        DateTimeConfig config = configForWithCustomFallback(
                DateTimeSource.FILE_CREATION_DATE,
                LocalDateTime.of(2023, 6, 1, 0, 0, 0)
        );

        // Act
        PreparedFileModel result = transformer.transform(input, config);

        // Assert — primary date used, custom date ignored
        assertFalse(result.isHasError(), "Expected no error when primary source resolves to a date");
        assertTrue(
                result.getNewName().contains("2024-01-15"),
                "Expected primary date '2024-01-15', but was: " + result.getNewName()
        );
        assertFalse(
                result.getNewName().contains("2023-06-01"),
                "Name must NOT contain custom date '2023-06-01' when primary resolved, but was: " + result.getNewName()
        );
    }

    // -----------------------------------------------------------------------
    // Test 8 — custom fallback disabled (flag=false), natural dates present → min natural date
    // -----------------------------------------------------------------------

    /**
     * When {@code useCustomDateTimeAsFallback} is not set (defaults to {@code false}) but
     * {@code useFallbackDateTime=true}, the standard natural-dates minimum must be used
     * regardless of a non-null {@code customDateTime}.
     */
    @Test
    void givenCustomFallbackDisabledAndFallbackEnabled_thenMinNaturalDateUsed() {
        // Arrange — natural dates present
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

        // useCustomDateTimeAsFallback NOT set (defaults to false); customDateTime supplied but must be ignored
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.CONTENT_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.REPLACE)
                .withSeparator("_")
                .withCustomDateTime(LocalDateTime.of(2023, 6, 1, 0, 0, 0))
                .withUseFallbackDateTime(true)
                .build();

        // Act
        PreparedFileModel result = transformer.transform(input, config);

        // Assert — no error, min natural date used; custom date ignored
        assertFalse(result.isHasError(), "Expected no error when natural dates are available");
        assertTrue(
                result.getNewName().contains("2024-01-15"),
                "Expected min natural date '2024-01-15', but was: " + result.getNewName()
        );
        assertFalse(
                result.getNewName().contains("2023"),
                "Name must NOT contain '2023' when useCustomDateTimeAsFallback=false, but was: " + result.getNewName()
        );
    }

    // -----------------------------------------------------------------------
    // Test 9 — useFallbackDateTime=false AND useCustomDateTimeAsFallback=true → error
    // -----------------------------------------------------------------------

    /**
     * The outer gate {@code useFallbackDateTime} must be respected even when
     * {@code useCustomDateTimeAsFallback=true}. When the primary source resolves to
     * null and {@code useFallbackDateTime=false}, the transformer must return an error
     * regardless of the custom-fallback flag or a non-null {@code customDateTime}.
     *
     * <p>Verifies that {@code useCustomDateTimeAsFallback} cannot bypass the outer gate.
     */
    @Test
    void givenFallbackGateDisabledAndCustomFallbackEnabled_whenPrimaryNull_thenErrorReturned() {
        // Arrange — CONTENT_CREATION_DATE with no metadata (primary resolves to null).
        //            customDateTime is set and useCustomDateTimeAsFallback=true, but the
        //            outer gate useFallbackDateTime=false must prevent any fallback path.
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

        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.CONTENT_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.REPLACE)
                .withSeparator("_")
                .withCustomDateTime(LocalDateTime.of(2023, 6, 1, 0, 0, 0))
                .withUseFallbackDateTime(false)          // outer gate OFF
                .withUseCustomDateTimeAsFallback(true)   // inner flag ON — must be ignored
                .build();

        // Act
        PreparedFileModel result = transformer.transform(input, config);

        // Assert — outer gate is off → no fallback of any kind → error
        assertTrue(result.isHasError(),
                "Expected hasError=true: useFallbackDateTime=false must block custom fallback path");
        assertFalse(
                result.getNewName().contains("2023"),
                "Name must NOT contain custom date '2023' when useFallbackDateTime=false, but was: "
                        + result.getNewName()
        );
    }

    // -----------------------------------------------------------------------
    // Test 10 — source=CUSTOM_DATE (primary resolves directly), custom fallback irrelevant
    // -----------------------------------------------------------------------

    /**
     * When source is {@link DateTimeSource#CUSTOM_DATE}, the primary resolution
     * directly returns {@code customDateTime}, so the fallback block must never execute.
     *
     * <p>Both fallback flags are set to {@code true} to verify they cannot interfere
     * with the primary path that has already resolved a non-null datetime.
     */
    @Test
    void givenSourceIsCustomDate_whenCustomDateTimeIsSet_thenPrimaryUsedAndFallbackDoesNotInterfere() {
        // Arrange — CUSTOM_DATE source with a specific customDateTime.
        //            creationDate and modificationDate differ from customDateTime
        //            so we can confirm the correct value was used.
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

        // Both fallback flags true — they must have zero effect because primary resolves
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.CUSTOM_DATE)
                .withCustomDateTime(LocalDateTime.of(2022, 3, 10, 0, 0, 0))
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.REPLACE)
                .withSeparator("_")
                .withUseFallbackDateTime(true)
                .withUseCustomDateTimeAsFallback(true)
                .build();

        // Act
        PreparedFileModel result = transformer.transform(input, config);

        // Assert — primary resolved; no error; name contains custom date, not file dates
        assertFalse(result.isHasError(), "Expected no error when CUSTOM_DATE source provides a non-null datetime");
        assertTrue(
                result.getNewName().contains("2022-03-10"),
                "Expected custom date '2022-03-10' from primary CUSTOM_DATE source, but was: " + result.getNewName()
        );
        assertFalse(
                result.getNewName().contains("2024"),
                "Name must NOT contain file dates '2024-...' when CUSTOM_DATE primary resolves, but was: "
                        + result.getNewName()
        );
    }
}
