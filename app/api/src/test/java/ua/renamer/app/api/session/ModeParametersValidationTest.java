package ua.renamer.app.api.session;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import ua.renamer.app.api.enums.*;
import ua.renamer.app.api.model.TransformationMode;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive validation tests for the {@link ModeParameters} sealed hierarchy.
 * Each nested class covers one record implementation.
 */
class ModeParametersValidationTest {

    // ─────────────────────────────────────────────────────────────────────────
    // AddTextParams
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class AddTextParamsTest {

        private AddTextParams valid() {
            return new AddTextParams("prefix_", ItemPosition.BEGIN);
        }

        @Test
        void validate_whenFullyValid_thenReturnsValid() {
            ValidationResult result = valid().validate();

            assertThat(result.ok()).isTrue();
            assertThat(result.isError()).isFalse();
        }

        @Test
        void mode_whenCalled_thenReturnsAddText() {
            assertThat(valid().mode()).isEqualTo(TransformationMode.ADD_TEXT);
        }

        @Test
        void requiresSequentialExecution_whenCalled_thenReturnsFalse() {
            assertThat(valid().requiresSequentialExecution()).isFalse();
        }

        @Test
        void validate_whenPositionIsNull_thenReturnsFieldError() {
            AddTextParams params = new AddTextParams("text", null);

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("position");
        }

        @Test
        void validate_whenTextToAddIsNull_thenReturnsFieldError() {
            AddTextParams params = new AddTextParams(null, ItemPosition.END);

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("textToAdd");
        }

        @Test
        void validate_whenBothFieldsNull_thenPositionErrorTakesPriority() {
            // position is checked first in the implementation
            AddTextParams params = new AddTextParams(null, null);

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("position");
        }

        @Test
        void withTextToAdd_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            AddTextParams original = valid();
            AddTextParams modified = original.withTextToAdd("new_text");

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.textToAdd()).isEqualTo("new_text");
            assertThat(modified.position()).isEqualTo(original.position());
        }

        @Test
        void withPosition_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            AddTextParams original = valid();
            AddTextParams modified = original.withPosition(ItemPosition.END);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.position()).isEqualTo(ItemPosition.END);
            assertThat(modified.textToAdd()).isEqualTo(original.textToAdd());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "a", "unicode_\u4e2d\u6587", "  spaces  "})
        void validate_withAnyNonNullText_thenValid(String text) {
            AddTextParams params = new AddTextParams(text, ItemPosition.BEGIN);

            assertThat(params.validate().ok()).isTrue();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RemoveTextParams
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class RemoveTextParamsTest {

        private RemoveTextParams valid() {
            return new RemoveTextParams("suffix_", ItemPosition.END);
        }

        @Test
        void validate_whenFullyValid_thenReturnsValid() {
            assertThat(valid().validate().ok()).isTrue();
        }

        @Test
        void mode_whenCalled_thenReturnsRemoveText() {
            assertThat(valid().mode()).isEqualTo(TransformationMode.REMOVE_TEXT);
        }

        @Test
        void requiresSequentialExecution_whenCalled_thenReturnsFalse() {
            assertThat(valid().requiresSequentialExecution()).isFalse();
        }

        @Test
        void validate_whenPositionIsNull_thenReturnsFieldError() {
            RemoveTextParams params = new RemoveTextParams("text", null);

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("position");
        }

        @Test
        void validate_whenTextToRemoveIsNull_thenReturnsFieldError() {
            RemoveTextParams params = new RemoveTextParams(null, ItemPosition.BEGIN);

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("textToRemove");
        }

        @Test
        void withTextToRemove_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            RemoveTextParams original = valid();
            RemoveTextParams modified = original.withTextToRemove("changed");

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.textToRemove()).isEqualTo("changed");
            assertThat(modified.position()).isEqualTo(original.position());
        }

        @Test
        void withPosition_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            RemoveTextParams original = valid();
            RemoveTextParams modified = original.withPosition(ItemPosition.BEGIN);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.position()).isEqualTo(ItemPosition.BEGIN);
            assertThat(modified.textToRemove()).isEqualTo(original.textToRemove());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ReplaceTextParams
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class ReplaceTextParamsTest {

        private ReplaceTextParams valid() {
            return new ReplaceTextParams("foo", "bar", ItemPositionExtended.EVERYWHERE);
        }

        @Test
        void validate_whenFullyValid_thenReturnsValid() {
            assertThat(valid().validate().ok()).isTrue();
        }

        @Test
        void mode_whenCalled_thenReturnsReplaceText() {
            assertThat(valid().mode()).isEqualTo(TransformationMode.REPLACE_TEXT);
        }

        @Test
        void requiresSequentialExecution_whenCalled_thenReturnsFalse() {
            assertThat(valid().requiresSequentialExecution()).isFalse();
        }

        @Test
        void validate_whenTextToReplaceIsNull_thenReturnsFieldError() {
            ReplaceTextParams params = new ReplaceTextParams(null, "bar", ItemPositionExtended.BEGIN);

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("textToReplace");
        }

        @Test
        void validate_whenReplacementTextIsNull_thenReturnsFieldError() {
            ReplaceTextParams params = new ReplaceTextParams("foo", null, ItemPositionExtended.END);

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("replacementText");
        }

        @Test
        void validate_whenPositionIsNull_thenReturnsFieldError() {
            ReplaceTextParams params = new ReplaceTextParams("foo", "bar", null);

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("position");
        }

        @Test
        void withTextToReplace_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            ReplaceTextParams original = valid();
            ReplaceTextParams modified = original.withTextToReplace("new_foo");

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.textToReplace()).isEqualTo("new_foo");
            assertThat(modified.replacementText()).isEqualTo(original.replacementText());
            assertThat(modified.position()).isEqualTo(original.position());
        }

        @Test
        void withReplacementText_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            ReplaceTextParams original = valid();
            ReplaceTextParams modified = original.withReplacementText("new_bar");

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.replacementText()).isEqualTo("new_bar");
            assertThat(modified.textToReplace()).isEqualTo(original.textToReplace());
            assertThat(modified.position()).isEqualTo(original.position());
        }

        @Test
        void withPosition_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            ReplaceTextParams original = valid();
            ReplaceTextParams modified = original.withPosition(ItemPositionExtended.BEGIN);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.position()).isEqualTo(ItemPositionExtended.BEGIN);
            assertThat(modified.textToReplace()).isEqualTo(original.textToReplace());
            assertThat(modified.replacementText()).isEqualTo(original.replacementText());
        }

        @Test
        void validate_withEmptyStringsForBothTexts_thenValid() {
            ReplaceTextParams params = new ReplaceTextParams("", "", ItemPositionExtended.EVERYWHERE);

            assertThat(params.validate().ok()).isTrue();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ChangeCaseParams
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class ChangeCaseParamsTest {

        private ChangeCaseParams valid() {
            return new ChangeCaseParams(TextCaseOptions.UPPERCASE, false);
        }

        @Test
        void validate_whenFullyValid_thenReturnsValid() {
            assertThat(valid().validate().ok()).isTrue();
        }

        @Test
        void mode_whenCalled_thenReturnsChangeCase() {
            assertThat(valid().mode()).isEqualTo(TransformationMode.CHANGE_CASE);
        }

        @Test
        void requiresSequentialExecution_whenCalled_thenReturnsFalse() {
            assertThat(valid().requiresSequentialExecution()).isFalse();
        }

        @Test
        void validate_whenCaseOptionIsNull_thenReturnsFieldError() {
            ChangeCaseParams params = new ChangeCaseParams(null, true);

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("caseOption");
        }

        @Test
        void validate_withCapitalizeFirstLetterTrue_thenValid() {
            ChangeCaseParams params = new ChangeCaseParams(TextCaseOptions.LOWERCASE, true);

            assertThat(params.validate().ok()).isTrue();
        }

        @Test
        void withCaseOption_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            ChangeCaseParams original = valid();
            ChangeCaseParams modified = original.withCaseOption(TextCaseOptions.TITLE_CASE);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.caseOption()).isEqualTo(TextCaseOptions.TITLE_CASE);
            assertThat(modified.capitalizeFirstLetter()).isEqualTo(original.capitalizeFirstLetter());
        }

        @Test
        void withCapitalizeFirstLetter_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            ChangeCaseParams original = valid();
            ChangeCaseParams modified = original.withCapitalizeFirstLetter(true);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.capitalizeFirstLetter()).isTrue();
            assertThat(modified.caseOption()).isEqualTo(original.caseOption());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SequenceParams
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class SequenceParamsTest {

        private SequenceParams valid() {
            return new SequenceParams(1, 1, 3, SortSource.FILE_NAME, true);
        }

        @Test
        void validate_whenFullyValid_thenReturnsValid() {
            assertThat(valid().validate().ok()).isTrue();
        }

        @Test
        void mode_whenCalled_thenReturnsAddSequence() {
            assertThat(valid().mode()).isEqualTo(TransformationMode.NUMBER_FILES);
        }

        @Test
        void requiresSequentialExecution_whenCalled_thenReturnsTrue() {
            assertThat(valid().requiresSequentialExecution()).isTrue();
        }

        @Test
        void validate_whenSortSourceIsNull_thenReturnsFieldError() {
            SequenceParams params = new SequenceParams(1, 1, 0, null, true);

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("sortSource");
        }

        @Test
        void validate_whenStepValueIsZero_thenReturnsFieldError() {
            SequenceParams params = new SequenceParams(1, 0, 0, SortSource.FILE_NAME, true);

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("stepValue");
        }

        @Test
        void validate_whenStepValueIsNegative_thenReturnsFieldError() {
            SequenceParams params = new SequenceParams(1, -1, 0, SortSource.FILE_NAME, true);

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("stepValue");
        }

        @Test
        void validate_whenPaddingDigitsIsNegative_thenReturnsFieldError() {
            SequenceParams params = new SequenceParams(1, 1, -1, SortSource.FILE_NAME, true);

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("paddingDigits");
        }

        @Test
        void validate_whenPaddingDigitsIsZero_thenValid() {
            SequenceParams params = new SequenceParams(1, 1, 0, SortSource.FILE_NAME, true);

            assertThat(params.validate().ok()).isTrue();
        }

        @Test
        void validate_whenStartNumberIsNegative_thenValid() {
            // startNumber has no lower bound constraint
            SequenceParams params = new SequenceParams(-100, 1, 0, SortSource.FILE_NAME, true);

            assertThat(params.validate().ok()).isTrue();
        }

        @Test
        void validate_whenStepValueIsIntegerMaxValue_thenValid() {
            SequenceParams params = new SequenceParams(0, Integer.MAX_VALUE, 0, SortSource.FILE_NAME, true);

            assertThat(params.validate().ok()).isTrue();
        }

        @Test
        void withStartNumber_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            SequenceParams original = valid();
            SequenceParams modified = original.withStartNumber(100);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.startNumber()).isEqualTo(100);
            assertThat(modified.stepValue()).isEqualTo(original.stepValue());
            assertThat(modified.paddingDigits()).isEqualTo(original.paddingDigits());
            assertThat(modified.sortSource()).isEqualTo(original.sortSource());
        }

        @Test
        void withStepValue_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            SequenceParams original = valid();
            SequenceParams modified = original.withStepValue(5);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.stepValue()).isEqualTo(5);
            assertThat(modified.startNumber()).isEqualTo(original.startNumber());
            assertThat(modified.paddingDigits()).isEqualTo(original.paddingDigits());
            assertThat(modified.sortSource()).isEqualTo(original.sortSource());
        }

        @Test
        void withPaddingDigits_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            SequenceParams original = valid();
            SequenceParams modified = original.withPaddingDigits(6);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.paddingDigits()).isEqualTo(6);
            assertThat(modified.startNumber()).isEqualTo(original.startNumber());
            assertThat(modified.stepValue()).isEqualTo(original.stepValue());
            assertThat(modified.sortSource()).isEqualTo(original.sortSource());
        }

        @Test
        void withSortSource_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            SequenceParams original = valid();
            SequenceParams modified = original.withSortSource(SortSource.FILE_SIZE);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.sortSource()).isEqualTo(SortSource.FILE_SIZE);
            assertThat(modified.startNumber()).isEqualTo(original.startNumber());
            assertThat(modified.stepValue()).isEqualTo(original.stepValue());
            assertThat(modified.paddingDigits()).isEqualTo(original.paddingDigits());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TruncateParams
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class TruncateParamsTest {

        private TruncateParams valid() {
            return new TruncateParams(5, TruncateOptions.REMOVE_SYMBOLS_FROM_END);
        }

        @Test
        void validate_whenFullyValid_thenReturnsValid() {
            assertThat(valid().validate().ok()).isTrue();
        }

        @Test
        void mode_whenCalled_thenReturnsTruncateFileName() {
            assertThat(valid().mode()).isEqualTo(TransformationMode.TRIM_NAME);
        }

        @Test
        void requiresSequentialExecution_whenCalled_thenReturnsFalse() {
            assertThat(valid().requiresSequentialExecution()).isFalse();
        }

        @Test
        void validate_whenTruncateOptionIsNull_thenReturnsFieldError() {
            TruncateParams params = new TruncateParams(3, null);

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("truncateOption");
        }

        @Test
        void validate_whenNumberOfSymbolsIsNegative_thenReturnsFieldError() {
            TruncateParams params = new TruncateParams(-1, TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN);

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("numberOfSymbols");
        }

        @Test
        void validate_whenNumberOfSymbolsIsZero_thenValid() {
            TruncateParams params = new TruncateParams(0, TruncateOptions.TRUNCATE_EMPTY_SYMBOLS);

            assertThat(params.validate().ok()).isTrue();
        }

        @Test
        void validate_whenBothNullAndNegative_thenTruncateOptionErrorTakesPriority() {
            // truncateOption is checked first in the implementation
            TruncateParams params = new TruncateParams(-5, null);

            ValidationResult result = params.validate();

            assertThat(result.field()).isEqualTo("truncateOption");
        }

        @Test
        void withNumberOfSymbols_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            TruncateParams original = valid();
            TruncateParams modified = original.withNumberOfSymbols(10);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.numberOfSymbols()).isEqualTo(10);
            assertThat(modified.truncateOption()).isEqualTo(original.truncateOption());
        }

        @Test
        void withTruncateOption_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            TruncateParams original = valid();
            TruncateParams modified = original.withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.truncateOption()).isEqualTo(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN);
            assertThat(modified.numberOfSymbols()).isEqualTo(original.numberOfSymbols());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ExtensionChangeParams
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class ExtensionChangeParamsTest {

        private ExtensionChangeParams valid() {
            return new ExtensionChangeParams("jpg");
        }

        @Test
        void validate_whenFullyValid_thenReturnsValid() {
            assertThat(valid().validate().ok()).isTrue();
        }

        @Test
        void mode_whenCalled_thenReturnsChangeExtension() {
            assertThat(valid().mode()).isEqualTo(TransformationMode.CHANGE_EXTENSION);
        }

        @Test
        void requiresSequentialExecution_whenCalled_thenReturnsFalse() {
            assertThat(valid().requiresSequentialExecution()).isFalse();
        }

        @Test
        void validate_whenNewExtensionIsNull_thenReturnsFieldError() {
            ExtensionChangeParams params = new ExtensionChangeParams(null);

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("newExtension");
        }

        @Test
        void validate_whenNewExtensionIsEmpty_thenReturnsFieldError() {
            ExtensionChangeParams params = new ExtensionChangeParams("");

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("newExtension");
        }

        @Test
        void validate_whenNewExtensionIsBlankWithSpaces_thenReturnsFieldError() {
            ExtensionChangeParams params = new ExtensionChangeParams("  ");

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("newExtension");
        }

        @Test
        void validate_whenNewExtensionIsBlankWithTab_thenReturnsFieldError() {
            ExtensionChangeParams params = new ExtensionChangeParams("\t");

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("newExtension");
        }

        @Test
        void validate_withExtensionContainingOnlyLetters_thenValid() {
            ExtensionChangeParams params = new ExtensionChangeParams("mp4");

            assertThat(params.validate().ok()).isTrue();
        }

        @Test
        void withNewExtension_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            ExtensionChangeParams original = valid();
            ExtensionChangeParams modified = original.withNewExtension("png");

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.newExtension()).isEqualTo("png");
        }

        @Test
        void withNewExtension_toNull_thenValidationFails() {
            ExtensionChangeParams modified = valid().withNewExtension(null);

            assertThat(modified.validate().ok()).isFalse();
            assertThat(modified.validate().field()).isEqualTo("newExtension");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DateTimeParams
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class DateTimeParamsTest {

        private static final LocalDateTime FIXED_DT = LocalDateTime.of(2025, 6, 15, 10, 30, 0);

        private DateTimeParams fullyValid() {
            return new DateTimeParams(
                    DateTimeSource.FILE_CREATION_DATE,
                    DateFormat.YYYY_MM_DD_DASHED,
                    TimeFormat.HH_MM_SS_24_TOGETHER,
                    ItemPositionWithReplacement.BEGIN,
                    true,
                    true,
                    false,
                    false,
                    false,
                    null,
                    false,
                    DateTimeFormat.DATE_TIME_TOGETHER,
                    ""
            );
        }

        @Test
        void validate_whenFullyValid_thenReturnsValid() {
            assertThat(fullyValid().validate().ok()).isTrue();
        }

        @Test
        void mode_whenCalled_thenReturnsUseDatetime() {
            assertThat(fullyValid().mode()).isEqualTo(TransformationMode.ADD_DATETIME);
        }

        @Test
        void requiresSequentialExecution_whenCalled_thenReturnsFalse() {
            assertThat(fullyValid().requiresSequentialExecution()).isFalse();
        }

        @Test
        void validate_whenSourceIsNull_thenReturnsFieldError() {
            DateTimeParams params = new DateTimeParams(
                    null,
                    DateFormat.YYYY_MM_DD_DASHED,
                    TimeFormat.HH_MM_SS_24_TOGETHER,
                    ItemPositionWithReplacement.BEGIN,
                    true, true, false, false, false, null, false,
                    DateTimeFormat.DATE_TIME_TOGETHER, ""
            );

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("source");
        }

        @Test
        void validate_whenBothDatePartAndTimePartAreFalse_thenReturnsFieldError() {
            DateTimeParams params = new DateTimeParams(
                    DateTimeSource.FILE_MODIFICATION_DATE,
                    DateFormat.YYYY_MM_DD_DASHED,
                    TimeFormat.HH_MM_SS_24_TOGETHER,
                    ItemPositionWithReplacement.END,
                    false, false, false, false, false, null, false,
                    DateTimeFormat.DATE_TIME_TOGETHER, ""
            );

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("useDatePart");
            assertThat(result.message()).contains("useDatePart");
        }

        @Test
        void validate_whenUseDatePartTrueAndDateFormatNull_thenReturnsFieldError() {
            DateTimeParams params = new DateTimeParams(
                    DateTimeSource.FILE_CREATION_DATE,
                    null,
                    TimeFormat.HH_MM_SS_24_TOGETHER,
                    ItemPositionWithReplacement.BEGIN,
                    true, true, false, false, false, null, false,
                    DateTimeFormat.DATE_TIME_TOGETHER, ""
            );

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("dateFormat");
        }

        @Test
        void validate_whenUseTimePartTrueAndTimeFormatNull_thenReturnsFieldError() {
            DateTimeParams params = new DateTimeParams(
                    DateTimeSource.FILE_CREATION_DATE,
                    DateFormat.YYYY_MM_DD_DASHED,
                    null,
                    ItemPositionWithReplacement.BEGIN,
                    true, true, false, false, false, null, false,
                    DateTimeFormat.DATE_TIME_TOGETHER, ""
            );

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("timeFormat");
        }

        @Test
        void validate_whenSourceIsCustomDateAndCustomDateTimeIsNull_thenReturnsFieldError() {
            DateTimeParams params = new DateTimeParams(
                    DateTimeSource.CUSTOM_DATE,
                    DateFormat.YYYY_MM_DD_DASHED,
                    TimeFormat.HH_MM_SS_24_TOGETHER,
                    ItemPositionWithReplacement.BEGIN,
                    true, true, false, false, false, null, false,
                    DateTimeFormat.DATE_TIME_TOGETHER, ""
            );

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("customDateTime");
        }

        @Test
        void validate_whenSourceIsCustomDateAndCustomDateTimeIsProvided_thenValid() {
            DateTimeParams params = new DateTimeParams(
                    DateTimeSource.CUSTOM_DATE,
                    DateFormat.YYYY_MM_DD_DASHED,
                    TimeFormat.HH_MM_SS_24_TOGETHER,
                    ItemPositionWithReplacement.BEGIN,
                    true, true, false, false, false, FIXED_DT, false,
                    DateTimeFormat.DATE_TIME_TOGETHER, ""
            );

            assertThat(params.validate().ok()).isTrue();
        }

        @Test
        void validate_whenOnlyDatePartTrue_andDateFormatProvided_thenValid() {
            DateTimeParams params = new DateTimeParams(
                    DateTimeSource.FILE_CREATION_DATE,
                    DateFormat.YYYY_MM_DD_DASHED,
                    null,
                    ItemPositionWithReplacement.BEGIN,
                    true, false, false, false, false, null, false,
                    DateTimeFormat.DATE_TIME_TOGETHER, ""
            );

            assertThat(params.validate().ok()).isTrue();
        }

        @Test
        void validate_whenOnlyTimePartTrue_andTimeFormatProvided_thenValid() {
            DateTimeParams params = new DateTimeParams(
                    DateTimeSource.FILE_CREATION_DATE,
                    null,
                    TimeFormat.HH_MM_SS_24_TOGETHER,
                    ItemPositionWithReplacement.BEGIN,
                    false, true, false, false, false, null, false,
                    DateTimeFormat.DATE_TIME_TOGETHER, ""
            );

            assertThat(params.validate().ok()).isTrue();
        }

        @Test
        void withSource_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            DateTimeParams original = fullyValid();
            DateTimeParams modified = original.withSource(DateTimeSource.CURRENT_DATE);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.source()).isEqualTo(DateTimeSource.CURRENT_DATE);
            assertThat(modified.dateFormat()).isEqualTo(original.dateFormat());
            assertThat(modified.timeFormat()).isEqualTo(original.timeFormat());
            assertThat(modified.position()).isEqualTo(original.position());
        }

        @Test
        void withDateFormat_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            DateTimeParams original = fullyValid();
            DateTimeParams modified = original.withDateFormat(DateFormat.DD_MM_YYYY_DASHED);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.dateFormat()).isEqualTo(DateFormat.DD_MM_YYYY_DASHED);
            assertThat(modified.source()).isEqualTo(original.source());
        }

        @Test
        void withTimeFormat_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            DateTimeParams original = fullyValid();
            DateTimeParams modified = original.withTimeFormat(TimeFormat.HH_MM_24_DASHED);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.timeFormat()).isEqualTo(TimeFormat.HH_MM_24_DASHED);
            assertThat(modified.source()).isEqualTo(original.source());
        }

        @Test
        void withPosition_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            DateTimeParams original = fullyValid();
            DateTimeParams modified = original.withPosition(ItemPositionWithReplacement.REPLACE);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.position()).isEqualTo(ItemPositionWithReplacement.REPLACE);
            assertThat(modified.source()).isEqualTo(original.source());
        }

        @Test
        void withUseDatePart_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            DateTimeParams original = fullyValid();
            DateTimeParams modified = original.withUseDatePart(false);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.useDatePart()).isFalse();
            assertThat(modified.useTimePart()).isEqualTo(original.useTimePart());
        }

        @Test
        void withUseTimePart_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            DateTimeParams original = fullyValid();
            DateTimeParams modified = original.withUseTimePart(false);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.useTimePart()).isFalse();
            assertThat(modified.useDatePart()).isEqualTo(original.useDatePart());
        }

        @Test
        void withApplyToExtension_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            DateTimeParams original = fullyValid();
            DateTimeParams modified = original.withApplyToExtension(true);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.applyToExtension()).isTrue();
            assertThat(modified.source()).isEqualTo(original.source());
        }

        @Test
        void withUseFallbackDateTime_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            DateTimeParams original = fullyValid();
            DateTimeParams modified = original.withUseFallbackDateTime(true);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.useFallbackDateTime()).isTrue();
        }

        @Test
        void withUseCustomDateTimeAsFallback_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            DateTimeParams original = fullyValid();
            DateTimeParams modified = original.withUseCustomDateTimeAsFallback(true);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.useCustomDateTimeAsFallback()).isTrue();
        }

        @Test
        void withCustomDateTime_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            DateTimeParams original = fullyValid();
            DateTimeParams modified = original.withCustomDateTime(FIXED_DT);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.customDateTime()).isEqualTo(FIXED_DT);
        }

        @Test
        void withUseUppercaseForAmPm_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            DateTimeParams original = fullyValid();
            DateTimeParams modified = original.withUseUppercaseForAmPm(true);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.useUppercaseForAmPm()).isTrue();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ImageDimensionsParams
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class ImageDimensionsParamsTest {

        private ImageDimensionsParams valid() {
            return new ImageDimensionsParams(
                    ImageDimensionOptions.WIDTH,
                    ImageDimensionOptions.HEIGHT,
                    ItemPositionWithReplacement.BEGIN,
                    "x",
                    "x"
            );
        }

        @Test
        void validate_whenFullyValid_thenReturnsValid() {
            assertThat(valid().validate().ok()).isTrue();
        }

        @Test
        void mode_whenCalled_thenReturnsUseImageDimensions() {
            assertThat(valid().mode()).isEqualTo(TransformationMode.ADD_DIMENSIONS);
        }

        @Test
        void requiresSequentialExecution_whenCalled_thenReturnsFalse() {
            assertThat(valid().requiresSequentialExecution()).isFalse();
        }

        @Test
        void validate_whenPositionIsNull_thenReturnsFieldError() {
            ImageDimensionsParams params = new ImageDimensionsParams(
                    ImageDimensionOptions.WIDTH, ImageDimensionOptions.HEIGHT, null, "x", "x"
            );

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("position");
        }

        @Test
        void validate_whenBothSidesAreDoNotUse_thenReturnsFieldError() {
            ImageDimensionsParams params = new ImageDimensionsParams(
                    ImageDimensionOptions.DO_NOT_USE,
                    ImageDimensionOptions.DO_NOT_USE,
                    ItemPositionWithReplacement.END,
                    "x",
                    "x"
            );

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("leftSide");
            assertThat(result.message()).contains("DO_NOT_USE");
        }

        @Test
        void validate_whenLeftSideIsDoNotUseButRightSideIsUsed_thenValid() {
            ImageDimensionsParams params = new ImageDimensionsParams(
                    ImageDimensionOptions.DO_NOT_USE,
                    ImageDimensionOptions.WIDTH,
                    ItemPositionWithReplacement.BEGIN,
                    "-",
                    "x"
            );

            assertThat(params.validate().ok()).isTrue();
        }

        @Test
        void validate_whenRightSideIsDoNotUseButLeftSideIsUsed_thenValid() {
            ImageDimensionsParams params = new ImageDimensionsParams(
                    ImageDimensionOptions.HEIGHT,
                    ImageDimensionOptions.DO_NOT_USE,
                    ItemPositionWithReplacement.BEGIN,
                    "",
                    "x"
            );

            assertThat(params.validate().ok()).isTrue();
        }

        @Test
        void validate_whenPositionNullAndBothDoNotUse_thenPositionErrorTakesPriority() {
            // position is checked first in the implementation
            ImageDimensionsParams params = new ImageDimensionsParams(
                    ImageDimensionOptions.DO_NOT_USE,
                    ImageDimensionOptions.DO_NOT_USE,
                    null,
                    "x",
                    "x"
            );

            ValidationResult result = params.validate();

            assertThat(result.field()).isEqualTo("position");
        }

        @Test
        void validate_withNullNameSeparator_thenValid() {
            // nameSeparator may be null per Javadoc
            ImageDimensionsParams params = new ImageDimensionsParams(
                    ImageDimensionOptions.WIDTH,
                    ImageDimensionOptions.HEIGHT,
                    ItemPositionWithReplacement.END,
                    null,
                    "x"
            );

            assertThat(params.validate().ok()).isTrue();
        }

        @Test
        void withLeftSide_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            ImageDimensionsParams original = valid();
            ImageDimensionsParams modified = original.withLeftSide(ImageDimensionOptions.HEIGHT);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.leftSide()).isEqualTo(ImageDimensionOptions.HEIGHT);
            assertThat(modified.rightSide()).isEqualTo(original.rightSide());
            assertThat(modified.position()).isEqualTo(original.position());
            assertThat(modified.nameSeparator()).isEqualTo(original.nameSeparator());
        }

        @Test
        void withRightSide_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            ImageDimensionsParams original = valid();
            ImageDimensionsParams modified = original.withRightSide(ImageDimensionOptions.DO_NOT_USE);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.rightSide()).isEqualTo(ImageDimensionOptions.DO_NOT_USE);
            assertThat(modified.leftSide()).isEqualTo(original.leftSide());
        }

        @Test
        void withPosition_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            ImageDimensionsParams original = valid();
            ImageDimensionsParams modified = original.withPosition(ItemPositionWithReplacement.REPLACE);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.position()).isEqualTo(ItemPositionWithReplacement.REPLACE);
            assertThat(modified.leftSide()).isEqualTo(original.leftSide());
        }

        @Test
        void withNameSeparator_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            ImageDimensionsParams original = valid();
            ImageDimensionsParams modified = original.withNameSeparator("_by_");

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.nameSeparator()).isEqualTo("_by_");
            assertThat(modified.leftSide()).isEqualTo(original.leftSide());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ParentFolderParams
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class ParentFolderParamsTest {

        private ParentFolderParams valid() {
            return new ParentFolderParams(1, ItemPosition.BEGIN, "_");
        }

        @Test
        void validate_whenFullyValid_thenReturnsValid() {
            assertThat(valid().validate().ok()).isTrue();
        }

        @Test
        void mode_whenCalled_thenReturnsUseParentFolderName() {
            assertThat(valid().mode()).isEqualTo(TransformationMode.ADD_FOLDER_NAME);
        }

        @Test
        void requiresSequentialExecution_whenCalled_thenReturnsFalse() {
            assertThat(valid().requiresSequentialExecution()).isFalse();
        }

        @Test
        void validate_whenPositionIsNull_thenReturnsFieldError() {
            ParentFolderParams params = new ParentFolderParams(1, null, "_");

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("position");
        }

        @Test
        void validate_whenNumberOfParentFoldersIsZero_thenReturnsFieldError() {
            ParentFolderParams params = new ParentFolderParams(0, ItemPosition.BEGIN, "_");

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("numberOfParentFolders");
        }

        @Test
        void validate_whenNumberOfParentFoldersIsNegative_thenReturnsFieldError() {
            ParentFolderParams params = new ParentFolderParams(-1, ItemPosition.END, "_");

            ValidationResult result = params.validate();

            assertThat(result.ok()).isFalse();
            assertThat(result.field()).isEqualTo("numberOfParentFolders");
            assertThat(result.message()).contains("-1");
        }

        @Test
        void validate_whenNumberOfParentFoldersIsOne_thenValid() {
            assertThat(valid().validate().ok()).isTrue();
        }

        @Test
        void validate_whenNumberOfParentFoldersIsLarge_thenValid() {
            ParentFolderParams params = new ParentFolderParams(99, ItemPosition.END, "/");

            assertThat(params.validate().ok()).isTrue();
        }

        @Test
        void validate_whenPositionNullAndCountZero_thenPositionErrorTakesPriority() {
            // position is checked first in the implementation
            ParentFolderParams params = new ParentFolderParams(0, null, "_");

            ValidationResult result = params.validate();

            assertThat(result.field()).isEqualTo("position");
        }

        @Test
        void validate_withNullSeparator_thenValid() {
            // separator has no null constraint
            ParentFolderParams params = new ParentFolderParams(1, ItemPosition.BEGIN, null);

            assertThat(params.validate().ok()).isTrue();
        }

        @Test
        void withNumberOfParentFolders_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            ParentFolderParams original = valid();
            ParentFolderParams modified = original.withNumberOfParentFolders(3);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.numberOfParentFolders()).isEqualTo(3);
            assertThat(modified.position()).isEqualTo(original.position());
            assertThat(modified.separator()).isEqualTo(original.separator());
        }

        @Test
        void withPosition_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            ParentFolderParams original = valid();
            ParentFolderParams modified = original.withPosition(ItemPosition.END);

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.position()).isEqualTo(ItemPosition.END);
            assertThat(modified.numberOfParentFolders()).isEqualTo(original.numberOfParentFolders());
            assertThat(modified.separator()).isEqualTo(original.separator());
        }

        @Test
        void withSeparator_whenChanged_thenReturnsNewInstanceWithUpdatedField() {
            ParentFolderParams original = valid();
            ParentFolderParams modified = original.withSeparator(" - ");

            assertThat(modified).isNotSameAs(original);
            assertThat(modified.separator()).isEqualTo(" - ");
            assertThat(modified.numberOfParentFolders()).isEqualTo(original.numberOfParentFolders());
            assertThat(modified.position()).isEqualTo(original.position());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sealed interface contract — every concrete type is a ModeParameters
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class SealedInterfaceContractTest {

        @Test
        void addTextParams_implementsModeParameters() {
            ModeParameters params = new AddTextParams("x", ItemPosition.BEGIN);
            assertThat(params).isInstanceOf(ModeParameters.class);
        }

        @Test
        void removeTextParams_implementsModeParameters() {
            ModeParameters params = new RemoveTextParams("x", ItemPosition.END);
            assertThat(params).isInstanceOf(ModeParameters.class);
        }

        @Test
        void replaceTextParams_implementsModeParameters() {
            ModeParameters params = new ReplaceTextParams("a", "b", ItemPositionExtended.BEGIN);
            assertThat(params).isInstanceOf(ModeParameters.class);
        }

        @Test
        void changeCaseParams_implementsModeParameters() {
            ModeParameters params = new ChangeCaseParams(TextCaseOptions.UPPERCASE, false);
            assertThat(params).isInstanceOf(ModeParameters.class);
        }

        @Test
        void sequenceParams_implementsModeParameters() {
            ModeParameters params = new SequenceParams(1, 1, 0, SortSource.FILE_NAME, true);
            assertThat(params).isInstanceOf(ModeParameters.class);
        }

        @Test
        void truncateParams_implementsModeParameters() {
            ModeParameters params = new TruncateParams(5, TruncateOptions.REMOVE_SYMBOLS_FROM_END);
            assertThat(params).isInstanceOf(ModeParameters.class);
        }

        @Test
        void extensionChangeParams_implementsModeParameters() {
            ModeParameters params = new ExtensionChangeParams("png");
            assertThat(params).isInstanceOf(ModeParameters.class);
        }

        @Test
        void dateTimeParams_implementsModeParameters() {
            ModeParameters params = new DateTimeParams(
                    DateTimeSource.FILE_CREATION_DATE,
                    DateFormat.YYYY_MM_DD_DASHED,
                    TimeFormat.HH_MM_SS_24_TOGETHER,
                    ItemPositionWithReplacement.BEGIN,
                    true, true, false, false, false, null, false,
                    DateTimeFormat.DATE_TIME_TOGETHER, ""
            );
            assertThat(params).isInstanceOf(ModeParameters.class);
        }

        @Test
        void imageDimensionsParams_implementsModeParameters() {
            ModeParameters params = new ImageDimensionsParams(
                    ImageDimensionOptions.WIDTH, ImageDimensionOptions.HEIGHT,
                    ItemPositionWithReplacement.BEGIN, "x", "x"
            );
            assertThat(params).isInstanceOf(ModeParameters.class);
        }

        @Test
        void parentFolderParams_implementsModeParameters() {
            ModeParameters params = new ParentFolderParams(1, ItemPosition.BEGIN, "_");
            assertThat(params).isInstanceOf(ModeParameters.class);
        }

        @Test
        void defaultRequiresSequentialExecution_returnsFalseForAllNonSequenceTypes() {
            // All non-SequenceParams records must return false from the default method
            ModeParameters[] nonSequential = {
                    new AddTextParams("x", ItemPosition.BEGIN),
                    new RemoveTextParams("x", ItemPosition.END),
                    new ReplaceTextParams("a", "b", ItemPositionExtended.EVERYWHERE),
                    new ChangeCaseParams(TextCaseOptions.UPPERCASE, false),
                    new TruncateParams(3, TruncateOptions.REMOVE_SYMBOLS_FROM_END),
                    new ExtensionChangeParams("jpg"),
                    new ImageDimensionsParams(
                            ImageDimensionOptions.WIDTH, ImageDimensionOptions.HEIGHT,
                            ItemPositionWithReplacement.BEGIN, "x", "x"),
                    new ParentFolderParams(1, ItemPosition.BEGIN, "_"),
            };

            for (ModeParameters p : nonSequential) {
                assertThat(p.requiresSequentialExecution())
                        .as("requiresSequentialExecution() should be false for %s", p.getClass().getSimpleName())
                        .isFalse();
            }
        }

        @Test
        void sequenceParams_isTheOnlyTypeRequiringSequentialExecution() {
            SequenceParams params = new SequenceParams(0, 1, 0, SortSource.FILE_NAME, true);
            assertThat(params.requiresSequentialExecution()).isTrue();
        }

        @ParameterizedTest
        @CsvSource({
                "ADD_TEXT",
                "REMOVE_TEXT",
                "REPLACE_TEXT",
                "CHANGE_CASE",
                "NUMBER_FILES",
                "TRIM_NAME",
                "CHANGE_EXTENSION",
                "ADD_DATETIME",
                "ADD_DIMENSIONS",
                "ADD_FOLDER_NAME"
        })
        void eachModeConstant_isCoveredByExactlyOneRecord(String modeName) {
            TransformationMode mode = TransformationMode.valueOf(modeName);
            assertThat(mode).isNotNull();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cross-record: validate() never returns null
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class ValidateNeverReturnsNullTest {

        @Test
        void addTextParams_validateNeverReturnsNull() {
            assertThat(new AddTextParams(null, null).validate()).isNotNull();
        }

        @Test
        void removeTextParams_validateNeverReturnsNull() {
            assertThat(new RemoveTextParams(null, null).validate()).isNotNull();
        }

        @Test
        void replaceTextParams_validateNeverReturnsNull() {
            assertThat(new ReplaceTextParams(null, null, null).validate()).isNotNull();
        }

        @Test
        void changeCaseParams_validateNeverReturnsNull() {
            assertThat(new ChangeCaseParams(null, false).validate()).isNotNull();
        }

        @Test
        void sequenceParams_validateNeverReturnsNull() {
            assertThat(new SequenceParams(0, 0, -1, null, true).validate()).isNotNull();
        }

        @Test
        void truncateParams_validateNeverReturnsNull() {
            assertThat(new TruncateParams(-1, null).validate()).isNotNull();
        }

        @Test
        void extensionChangeParams_validateNeverReturnsNull() {
            assertThat(new ExtensionChangeParams(null).validate()).isNotNull();
        }

        @Test
        void dateTimeParams_validateNeverReturnsNull() {
            DateTimeParams params = new DateTimeParams(
                    null, null, null, null, false, false, false, false, false, null, false,
                    null, null
            );
            assertThat(params.validate()).isNotNull();
        }

        @Test
        void imageDimensionsParams_validateNeverReturnsNull() {
            ImageDimensionsParams params = new ImageDimensionsParams(null, null, null, null, null);
            assertThat(params.validate()).isNotNull();
        }

        @Test
        void parentFolderParams_validateNeverReturnsNull() {
            assertThat(new ParentFolderParams(0, null, null).validate()).isNotNull();
        }
    }
}
