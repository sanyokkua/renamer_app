package ua.renamer.app.core.service;

import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;

import java.util.List;

/**
 * Service for transforming file models into prepared file models with new names.
 * Each transformation mode implements this interface.
 *
 * @param <C> Configuration type for this transformer
 */
@FunctionalInterface
public interface FileTransformationService<C> {
    /**
     * Transform a single file model to prepared file model.
     * Errors are captured in PreparedFileModel.errorMessage rather than thrown.
     *
     * @param input  File model to transform
     * @param config Transformation configuration
     * @return Prepared file model with new name or error information
     */
    PreparedFileModel transform(FileModel input, C config);

    /**
     * Transform a batch of files. Default implementation processes individually.
     * Override for modes requiring batch processing (e.g., sequence).
     *
     * @param inputs List of file models to transform
     * @param config Transformation configuration
     * @return List of prepared file models
     */
    default List<PreparedFileModel> transformBatch(List<FileModel> inputs, C config) {
        return inputs.stream()
                .map(input -> transform(input, config))
                .toList();
    }

    /**
     * Indicates if this transformer requires sequential execution.
     * True only for SEQUENCE mode.
     *
     * @return true if sequential execution is required, false for parallel
     */
    default boolean requiresSequentialExecution() {
        return false;
    }
}
