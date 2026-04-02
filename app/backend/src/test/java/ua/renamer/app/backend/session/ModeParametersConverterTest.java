package ua.renamer.app.backend.session;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.*;
import ua.renamer.app.api.model.config.*;
import ua.renamer.app.api.session.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ModeParametersConverter}.
 * All objects are built using real builders — no mocks.
 */
class ModeParametersConverterTest {

    // =========================================================================
    // AddText
    // =========================================================================

    @Nested
    class AddTextTests {

        @Test
        void givenAddTextParams_whenConvert_thenAddTextConfigFieldsMatch() {
            // Arrange
            var params = new AddTextParams("_suffix", ItemPosition.END);

            // Act
            var config = (AddTextConfig) ModeParametersConverter.toConfig(params);

            // Assert
            assertThat(config.getTextToAdd()).isEqualTo("_suffix");
            assertThat(config.getPosition()).isEqualTo(ItemPosition.END);
        }
    }

    // =========================================================================
    // RemoveText
    // =========================================================================

    @Nested
    class RemoveTextTests {

        @Test
        void givenRemoveTextParams_whenConvert_thenRemoveTextConfigFieldsMatch() {
            // Arrange
            var params = new RemoveTextParams("old_", ItemPosition.BEGIN);

            // Act
            var config = (RemoveTextConfig) ModeParametersConverter.toConfig(params);

            // Assert
            assertThat(config.getTextToRemove()).isEqualTo("old_");
            assertThat(config.getPosition()).isEqualTo(ItemPosition.BEGIN);
        }
    }

    // =========================================================================
    // ReplaceText
    // =========================================================================

    @Nested
    class ReplaceTextTests {

        @Test
        void givenReplaceTextParams_whenConvert_thenReplaceTextConfigFieldsMatch() {
            // Arrange
            var params = new ReplaceTextParams("foo", "bar", ItemPositionExtended.EVERYWHERE);

            // Act
            var config = (ReplaceTextConfig) ModeParametersConverter.toConfig(params);

            // Assert
            assertThat(config.getTextToReplace()).isEqualTo("foo");
            assertThat(config.getReplacementText()).isEqualTo("bar");
            assertThat(config.getPosition()).isEqualTo(ItemPositionExtended.EVERYWHERE);
        }
    }

    // =========================================================================
    // ChangeCase
    // =========================================================================

    @Nested
    class ChangeCaseTests {

        @Test
        void givenChangeCaseParams_whenConvert_thenCaseChangeConfigFieldsMatch() {
            // Arrange
            var params = new ChangeCaseParams(TextCaseOptions.UPPERCASE, true);

            // Act
            var config = (CaseChangeConfig) ModeParametersConverter.toConfig(params);

            // Assert
            assertThat(config.getCaseOption()).isEqualTo(TextCaseOptions.UPPERCASE);
            assertThat(config.isCapitalizeFirstLetter()).isTrue();
        }
    }

    // =========================================================================
    // Sequence
    // =========================================================================

    @Nested
    class SequenceTests {

        @Test
        void givenSequenceParams_whenConvert_thenSequenceConfigFieldsMatch() {
            // Arrange
            var params = new SequenceParams(1, 2, 3, SortSource.FILE_NAME);

            // Act
            var config = (SequenceConfig) ModeParametersConverter.toConfig(params);

            // Assert
            assertThat(config.getStartNumber()).isEqualTo(1);
            assertThat(config.getStepValue()).isEqualTo(2);
            assertThat(config.getPadding()).isEqualTo(3);
            assertThat(config.getSortSource()).isEqualTo(SortSource.FILE_NAME);
        }

        @Test
        void givenSequenceParamsWithNegativePadding_whenConvert_thenIllegalArgumentExceptionFromConfig() {
            // Arrange — padding = -1 passes through converter into SequenceConfig.build() validation
            var params = new SequenceParams(1, 1, -1, SortSource.FILE_NAME);

            // Act & Assert
            assertThatThrownBy(() -> ModeParametersConverter.toConfig(params))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("padding");
        }
    }

    // =========================================================================
    // Truncate
    // =========================================================================

    @Nested
    class TruncateTests {

        @Test
        void givenTruncateParams_whenConvert_thenTruncateConfigFieldsMatch() {
            // Arrange
            var params = new TruncateParams(10, TruncateOptions.REMOVE_SYMBOLS_FROM_END);

            // Act
            var config = (TruncateConfig) ModeParametersConverter.toConfig(params);

            // Assert
            assertThat(config.getNumberOfSymbols()).isEqualTo(10);
            assertThat(config.getTruncateOption()).isEqualTo(TruncateOptions.REMOVE_SYMBOLS_FROM_END);
        }
    }

    // =========================================================================
    // ExtensionChange
    // =========================================================================

    @Nested
    class ExtensionChangeTests {

        @Test
        void givenExtensionChangeParams_whenConvert_thenExtensionChangeConfigFieldsMatch() {
            // Arrange
            var params = new ExtensionChangeParams("mp4");

            // Act
            var config = (ExtensionChangeConfig) ModeParametersConverter.toConfig(params);

            // Assert
            assertThat(config.getNewExtension()).isEqualTo("mp4");
        }
    }

    // =========================================================================
    // DateTime
    // =========================================================================

    @Nested
    class DateTimeTests {

        @Test
        void givenDateTimeParams_whenConvert_thenAllFieldsTransferred() {
            // Arrange
            LocalDateTime custom = LocalDateTime.of(2026, 1, 15, 10, 30);
            var params = new DateTimeParams(
                    DateTimeSource.FILE_CREATION_DATE,
                    DateFormat.YYYY_MM_DD_TOGETHER,
                    TimeFormat.HH_MM_SS_24_TOGETHER,
                    ItemPositionWithReplacement.BEGIN,
                    true,    // useDatePart
                    false,   // useTimePart
                    false,   // applyToExtension
                    true,    // useFallbackDateTime
                    true,    // useCustomDateTimeAsFallback
                    custom,  // customDateTime
                    false    // useUppercaseForAmPm
            );

            // Act
            var config = (DateTimeConfig) ModeParametersConverter.toConfig(params);

            // Assert — 9 mapped fields
            assertThat(config.getSource()).isEqualTo(DateTimeSource.FILE_CREATION_DATE);
            assertThat(config.getDateFormat()).isEqualTo(DateFormat.YYYY_MM_DD_TOGETHER);
            assertThat(config.getTimeFormat()).isEqualTo(TimeFormat.HH_MM_SS_24_TOGETHER);
            assertThat(config.getPosition()).isEqualTo(ItemPositionWithReplacement.BEGIN);
            assertThat(config.getCustomDateTime().orElse(null)).isEqualTo(custom);
            assertThat(config.isUseFallbackDateTime()).isTrue();
            assertThat(config.isUseCustomDateTimeAsFallback()).isTrue();
            assertThat(config.isUseUppercaseForAmPm()).isFalse();
            assertThat(config.isApplyToExtension()).isFalse();
            // Fields not in DateTimeConfig — just verify config builds without error
            assertThat(config.getDateTimeFormat()).isNull();
            assertThat(config.getSeparator()).isNull();
        }

        @Test
        void givenDateTimeParamsWithApplyToExtensionTrue_whenConvert_thenApplyToExtensionMappedTrue() {
            // Arrange
            var params = new DateTimeParams(
                    DateTimeSource.FILE_CREATION_DATE,
                    DateFormat.YYYY_MM_DD_TOGETHER,
                    TimeFormat.HH_MM_SS_24_TOGETHER,
                    ItemPositionWithReplacement.END,
                    true,   // useDatePart
                    false,  // useTimePart
                    true,   // applyToExtension ← true this time
                    false,  // useFallbackDateTime
                    false,  // useCustomDateTimeAsFallback
                    null,   // customDateTime
                    true    // useUppercaseForAmPm
            );

            // Act
            var config = (DateTimeConfig) ModeParametersConverter.toConfig(params);

            // Assert
            assertThat(config.isApplyToExtension()).isTrue();
        }
    }

    // =========================================================================
    // ImageDimensions
    // =========================================================================

    @Nested
    class ImageDimensionsTests {

        @Test
        void givenImageDimensionsParams_whenConvert_thenImageDimensionsConfigFieldsMatch() {
            // Arrange
            var params = new ImageDimensionsParams(
                    ImageDimensionOptions.WIDTH,
                    ImageDimensionOptions.HEIGHT,
                    ItemPositionWithReplacement.END,
                    "_"
            );

            // Act
            var config = (ImageDimensionsConfig) ModeParametersConverter.toConfig(params);

            // Assert
            assertThat(config.getLeftSide()).isEqualTo(ImageDimensionOptions.WIDTH);
            assertThat(config.getRightSide()).isEqualTo(ImageDimensionOptions.HEIGHT);
            assertThat(config.getPosition()).isEqualTo(ItemPositionWithReplacement.END);
            assertThat(config.getNameSeparator()).isEqualTo("_");
            // separator ("x" between WxH) is not present in Params — must be null
            assertThat(config.getSeparator()).isNull();
        }
    }

    // =========================================================================
    // ParentFolder
    // =========================================================================

    @Nested
    class ParentFolderTests {

        @Test
        void givenParentFolderParams_whenConvert_thenParentFolderConfigFieldsMatch() {
            // Arrange
            var params = new ParentFolderParams(2, ItemPosition.BEGIN, "-");

            // Act
            var config = (ParentFolderConfig) ModeParametersConverter.toConfig(params);

            // Assert
            assertThat(config.getNumberOfParentFolders()).isEqualTo(2);
            assertThat(config.getPosition()).isEqualTo(ItemPosition.BEGIN);
            assertThat(config.getSeparator()).isEqualTo("-");
        }
    }

    // =========================================================================
    // Null safety
    // =========================================================================

    @Nested
    class NullSafetyTests {

        @Test
        void givenNullParams_whenConvert_thenNullPointerException() {
            assertThatThrownBy(() -> ModeParametersConverter.toConfig(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
