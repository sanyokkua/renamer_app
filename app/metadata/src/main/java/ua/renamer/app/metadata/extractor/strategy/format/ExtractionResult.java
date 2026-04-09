package ua.renamer.app.metadata.extractor.strategy.format;

import org.jspecify.annotations.Nullable;

/**
 * Generic result wrapper for metadata extraction operations.
 *
 * @param <T> Type of the extracted value
 */
public record ExtractionResult<T>(@Nullable T value, @Nullable String errorMessage) {
    /**
     * @param value the successfully extracted value
     * @param <T>   the value type
     * @return a successful result wrapping the given value
     */
    public static <T> ExtractionResult<T> success(T value) {
        return new ExtractionResult<>(value, null);
    }

    /**
     * @param errorMessage description of why extraction failed
     * @param <T>          the value type
     * @return a failed result with no value and the given error message
     */
    public static <T> ExtractionResult<T> failure(String errorMessage) {
        return new ExtractionResult<>(null, errorMessage);
    }

    /**
     * @return {@code true} if extraction failed and an error message is present
     */
    public boolean hasError() {
        return errorMessage != null;
    }
}
