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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Acceptance tests for TASK-1.6: {@code useUppercaseForAmPm} in {@link DateTimeTransformer}.
 *
 * <p>All tests use {@code CUSTOM_DATE} as the datetime source with a fixed
 * {@link LocalDateTime} of {@code 2024-06-15 14:30:00} so the formatted output
 * is deterministic regardless of locale or system clock.
 * Hour 14 in 12-hour format is {@code 02}, which produces a PM designator.
 *
 * <p>Covers four scenarios:
 * <ol>
 *   <li>AM/PM format + {@code useUppercaseForAmPm=true}  → designator is uppercase.</li>
 *   <li>AM/PM format + {@code useUppercaseForAmPm=false} → designator is lowercase.</li>
 *   <li>24-hour format + {@code useUppercaseForAmPm=true} → flag is silently ignored, no error.</li>
 *   <li>AM/PM format built without calling {@code withUseUppercaseForAmPm} → default {@code true} → uppercase.</li>
 * </ol>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DateTimeTransformerAmPmTest {

    /**
     * Fixed reference datetime: 14:30:00 on 2024-06-15. In 12-hour format: 02:30:00 PM.
     */
    private static final LocalDateTime FIXED_DATETIME = LocalDateTime.of(2024, 6, 15, 14, 30, 0);

    private DateTimeTransformer transformer;

    @BeforeAll
    void setUp() {
        DateTimeUtils dateTimeUtils = new TestDateTimeUtils();
        transformer = new DateTimeTransformer(dateTimeUtils);
    }

    // -----------------------------------------------------------------------
    // Shared helpers
    // -----------------------------------------------------------------------

    /**
     * Builds a minimal {@link FileModel} backed by a fake path.
     * The datetime source used in all tests is {@code CUSTOM_DATE}, so the
     * file's own creation/modification dates are irrelevant — they are set to
     * {@code null} to make that clear.
     */
    private FileModel buildFileModel() {
        return FileModel.builder()
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
    }

    /**
     * Builds a {@link DateTimeConfig} for an AM/PM {@link TimeFormat} with the
     * given uppercase flag, using {@code REPLACE} position so that
     * {@code result.getNewName()} equals the formatted datetime string directly.
     *
     * @param timeFormat   an AM/PM 12-hour format
     * @param useUppercase value passed to {@code withUseUppercaseForAmPm}
     * @return a fully configured {@link DateTimeConfig}
     */
    private DateTimeConfig amPmConfig(TimeFormat timeFormat, boolean useUppercase) {
        return DateTimeConfig.builder()
                .withSource(DateTimeSource.CUSTOM_DATE)
                .withCustomDateTime(FIXED_DATETIME)
                .withDateFormat(DateFormat.DO_NOT_USE_DATE)
                .withTimeFormat(timeFormat)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.REPLACE)
                .withSeparator("")
                .withUseFallbackDateTime(false)
                .withUseUppercaseForAmPm(useUppercase)
                .build();
    }

    /**
     * Builds a {@link DateTimeConfig} for a 24-hour {@link TimeFormat} with
     * {@code useUppercaseForAmPm=true}.
     *
     * @param timeFormat a 24-hour format (no AM/PM designator)
     * @return a fully configured {@link DateTimeConfig}
     */
    private DateTimeConfig nonAmPmConfig(TimeFormat timeFormat) {
        return DateTimeConfig.builder()
                .withSource(DateTimeSource.CUSTOM_DATE)
                .withCustomDateTime(FIXED_DATETIME)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(timeFormat)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.REPLACE)
                .withSeparator("")
                .withUseFallbackDateTime(false)
                .withUseUppercaseForAmPm(true)
                .build();
    }

    // -----------------------------------------------------------------------
    // Test 1 — AM/PM format + useUppercaseForAmPm=true → uppercase designator
    // -----------------------------------------------------------------------

    /**
     * With {@code HH_MM_SS_AM_PM_TOGETHER} and {@code useUppercaseForAmPm=true},
     * the transformer must uppercase the entire formatted string so the AM/PM
     * designator becomes {@code "PM"} (hour 14 = 2 PM).
     */
    @Test
    void givenAmPmFormatAndUppercaseTrue_whenTransform_thenResultContainsUppercaseAMPM() {
        // Arrange
        FileModel input = buildFileModel();
        DateTimeConfig config = amPmConfig(TimeFormat.HH_MM_SS_AM_PM_TOGETHER, true);

        // Act
        PreparedFileModel result = transformer.transform(input, config);

        // Assert
        assertThat(result.isHasError())
                .as("no error expected for a valid CUSTOM_DATE config")
                .isFalse();
        assertThat(result.getNewName())
                .as("formatted string must contain uppercase AM or PM designator")
                .matches(name -> name.contains("AM") || name.contains("PM"));
        assertThat(result.getNewName())
                .as("formatted string must NOT contain lowercase am or pm designator")
                .matches(name -> !name.contains("am") && !name.contains("pm"));
    }

    // -----------------------------------------------------------------------
    // Test 2 — AM/PM format + useUppercaseForAmPm=false → lowercase designator
    // -----------------------------------------------------------------------

    /**
     * With {@code HH_MM_SS_AM_PM_TOGETHER} and {@code useUppercaseForAmPm=false},
     * the transformer must lowercase the entire formatted string so the AM/PM
     * designator becomes {@code "pm"} (hour 14 = 2 PM).
     */
    @Test
    void givenAmPmFormatAndUppercaseFalse_whenTransform_thenResultContainsLowercaseAmPm() {
        // Arrange
        FileModel input = buildFileModel();
        DateTimeConfig config = amPmConfig(TimeFormat.HH_MM_SS_AM_PM_TOGETHER, false);

        // Act
        PreparedFileModel result = transformer.transform(input, config);

        // Assert
        assertThat(result.isHasError())
                .as("no error expected for a valid CUSTOM_DATE config")
                .isFalse();
        assertThat(result.getNewName())
                .as("formatted string must contain lowercase am or pm designator")
                .matches(name -> name.contains("am") || name.contains("pm"));
        assertThat(result.getNewName())
                .as("formatted string must NOT contain uppercase AM or PM")
                .matches(name -> !name.contains("AM") && !name.contains("PM"));
    }

    // -----------------------------------------------------------------------
    // Test 3 — 24-hour format + useUppercaseForAmPm=true → flag has no effect
    // -----------------------------------------------------------------------

    /**
     * When the configured {@link TimeFormat} is a 24-hour format (no AM/PM),
     * setting {@code useUppercaseForAmPm=true} must be silently ignored.
     * The transformer must return a successful result without crashing.
     */
    @Test
    void givenNonAmPmFormat_whenTransform_thenUppercaseFlagHasNoEffect() {
        // Arrange
        FileModel input = buildFileModel();
        DateTimeConfig config = nonAmPmConfig(TimeFormat.HH_MM_SS_24_TOGETHER);

        // Act
        PreparedFileModel result = transformer.transform(input, config);

        // Assert
        assertThat(result.isHasError())
                .as("useUppercaseForAmPm flag must not cause an error on 24-hour formats")
                .isFalse();
        assertThat(result.getNewName())
                .as("new name must not be null or blank")
                .isNotBlank();
    }

    // -----------------------------------------------------------------------
    // Test 4 — default config (withUseUppercaseForAmPm not called) → uppercase
    // -----------------------------------------------------------------------

    /**
     * When {@code DateTimeConfig} is built WITHOUT calling {@code withUseUppercaseForAmPm},
     * the field must default to {@code true}, so the AM/PM designator is uppercase.
     * This confirms the {@code @Builder.Default boolean useUppercaseForAmPm = true}
     * contract in {@link ua.renamer.app.api.model.config.DateTimeConfig}.
     */
    @Test
    void givenAmPmFormatDefaultConfig_whenTransform_thenResultIsUppercase() {
        // Arrange
        FileModel input = buildFileModel();
        // Intentionally omit withUseUppercaseForAmPm — rely entirely on the default
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.CUSTOM_DATE)
                .withCustomDateTime(FIXED_DATETIME)
                .withDateFormat(DateFormat.DO_NOT_USE_DATE)
                .withTimeFormat(TimeFormat.HH_MM_SS_AM_PM_TOGETHER)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.REPLACE)
                .withSeparator("")
                .build();

        // Act
        PreparedFileModel result = transformer.transform(input, config);

        // Assert
        assertThat(result.isHasError())
                .as("no error expected when using default config")
                .isFalse();
        assertThat(result.getNewName())
                .as("default useUppercaseForAmPm=true must produce uppercase AM or PM")
                .matches(name -> name.contains("AM") || name.contains("PM"));
        assertThat(result.getNewName())
                .as("default config must NOT produce lowercase am or pm")
                .matches(name -> !name.contains("am") && !name.contains("pm"));
    }
}
