package ua.renamer.app.backend.session;

import ua.renamer.app.api.model.config.AddTextConfig;
import ua.renamer.app.api.model.config.CaseChangeConfig;
import ua.renamer.app.api.model.config.DateTimeConfig;
import ua.renamer.app.api.model.config.ExtensionChangeConfig;
import ua.renamer.app.api.model.config.ImageDimensionsConfig;
import ua.renamer.app.api.model.config.ParentFolderConfig;
import ua.renamer.app.api.model.config.RemoveTextConfig;
import ua.renamer.app.api.model.config.ReplaceTextConfig;
import ua.renamer.app.api.model.config.SequenceConfig;
import ua.renamer.app.api.model.config.TransformationConfig;
import ua.renamer.app.api.model.config.TruncateConfig;
import ua.renamer.app.api.session.AddTextParams;
import ua.renamer.app.api.session.ChangeCaseParams;
import ua.renamer.app.api.session.DateTimeParams;
import ua.renamer.app.api.session.ExtensionChangeParams;
import ua.renamer.app.api.session.ImageDimensionsParams;
import ua.renamer.app.api.session.ModeParameters;
import ua.renamer.app.api.session.ParentFolderParams;
import ua.renamer.app.api.session.RemoveTextParams;
import ua.renamer.app.api.session.ReplaceTextParams;
import ua.renamer.app.api.session.SequenceParams;
import ua.renamer.app.api.session.TruncateParams;

/**
 * Stateless utility that converts a {@link ModeParameters} record to the
 * corresponding {@link TransformationConfig} consumed by the V2 pipeline.
 *
 * <p>The sealed {@code switch} is exhaustive — adding a new {@link ModeParameters}
 * permit without updating this converter is a compile error.
 */
public final class ModeParametersConverter {

    private ModeParametersConverter() {
    }

    /**
     * Converts the given {@link ModeParameters} record to the matching
     * {@link TransformationConfig} for the V2 rename pipeline.
     *
     * @param params the UI-layer parameters; must not be {@code null}
     * @return the corresponding V2 config instance
     * @throws NullPointerException     if {@code params} is {@code null}
     * @throws IllegalArgumentException if a config field fails builder validation
     *                                  (e.g. negative padding in {@link SequenceConfig})
     */
    public static TransformationConfig toConfig(ModeParameters params) {
        return switch (params) {
            case AddTextParams p -> AddTextConfig.builder()
                    .withTextToAdd(p.textToAdd())
                    .withPosition(p.position())
                    .build();
            case RemoveTextParams p -> RemoveTextConfig.builder()
                    .withTextToRemove(p.textToRemove())
                    .withPosition(p.position())
                    .build();
            case ReplaceTextParams p -> ReplaceTextConfig.builder()
                    .withTextToReplace(p.textToReplace())
                    .withReplacementText(p.replacementText())
                    .withPosition(p.position())
                    .build();
            case ChangeCaseParams p -> CaseChangeConfig.builder()
                    .withCaseOption(p.caseOption())
                    .withCapitalizeFirstLetter(p.capitalizeFirstLetter())
                    .build();
            case SequenceParams p -> SequenceConfig.builder()
                    .withStartNumber(p.startNumber())
                    .withStepValue(p.stepValue())
                    .withPadding(p.paddingDigits())
                    .withSortSource(p.sortSource())
                    .build();
            case TruncateParams p -> TruncateConfig.builder()
                    .withNumberOfSymbols(p.numberOfSymbols())
                    .withTruncateOption(p.truncateOption())
                    .build();
            case ExtensionChangeParams p -> ExtensionChangeConfig.builder()
                    .withNewExtension(p.newExtension())
                    .build();
            case DateTimeParams p -> DateTimeConfig.builder()
                    .withSource(p.source())
                    .withDateFormat(p.dateFormat())
                    .withTimeFormat(p.timeFormat())
                    .withPosition(p.position())
                    .withCustomDateTime(p.customDateTime())
                    .withUseFallbackDateTime(p.useFallbackDateTime())
                    .withUseCustomDateTimeAsFallback(p.useCustomDateTimeAsFallback())
                    .withUseUppercaseForAmPm(p.useUppercaseForAmPm())
                    .build();
            case ImageDimensionsParams p -> ImageDimensionsConfig.builder()
                    .withLeftSide(p.leftSide())
                    .withRightSide(p.rightSide())
                    .withPosition(p.position())
                    .withNameSeparator(p.nameSeparator())
                    .build();
            case ParentFolderParams p -> ParentFolderConfig.builder()
                    .withNumberOfParentFolders(p.numberOfParentFolders())
                    .withPosition(p.position())
                    .withSeparator(p.separator())
                    .build();
        };
    }
}
