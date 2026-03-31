package ua.renamer.app.metadata.extractor.strategy.format;

import org.jspecify.annotations.Nullable;

/**
 * Generic result wrapper for metadata extraction operations.
 *
 * @param <T> Type of the extracted value
 */
public record ExtractionResult<T>(@Nullable T value, @Nullable String errorMessage) {
    public static <T> ExtractionResult<T> success(T value) {
        return new ExtractionResult<>(value, null);
    }

    public static <T> ExtractionResult<T> failure(String errorMessage) {
        return new ExtractionResult<>(null, errorMessage);
    }

    public boolean hasError() {
        return errorMessage != null;
    }
}
