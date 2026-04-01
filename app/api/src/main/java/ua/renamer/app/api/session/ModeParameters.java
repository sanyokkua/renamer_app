package ua.renamer.app.api.session;

import ua.renamer.app.api.model.TransformationMode;

/**
 * Sealed parameter hierarchy for all transformation modes.
 * Each record captures the UI-facing configuration for one mode.
 * Implementations are immutable records; use the {@code withX} wither
 * methods they expose to produce modified copies.
 */
public sealed interface ModeParameters
        permits AddTextParams, RemoveTextParams, ReplaceTextParams,
                ChangeCaseParams, SequenceParams, TruncateParams,
                ExtensionChangeParams, DateTimeParams,
                ImageDimensionsParams, ParentFolderParams {

    /**
     * Returns the transformation mode this parameter object belongs to.
     *
     * @return the {@link TransformationMode} constant; never null
     */
    TransformationMode mode();

    /**
     * Validates all fields.
     *
     * @return {@link ValidationResult#valid()} when all fields are valid,
     *         or a {@link ValidationResult#fieldError(String, String)} describing the first invalid field
     */
    ValidationResult validate();

    /**
     * Returns {@code true} when transformation must run sequentially across all files.
     * Only {@link SequenceParams} returns {@code true}; all other implementations
     * inherit this default which returns {@code false}.
     *
     * @return {@code true} if sequential execution is required, {@code false} otherwise
     */
    default boolean requiresSequentialExecution() {
        return false;
    }
}
