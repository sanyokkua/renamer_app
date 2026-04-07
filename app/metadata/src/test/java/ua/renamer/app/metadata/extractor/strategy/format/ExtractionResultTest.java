package ua.renamer.app.metadata.extractor.strategy.format;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ExtractionResult}.
 *
 * <p>Covers the success/failure factory methods and the {@code hasError} logic.
 */
class ExtractionResultTest {

    // ============================================================================
    // success() factory
    // ============================================================================

    @Test
    void success_withValue_storesValue() {
        ExtractionResult<Integer> result = ExtractionResult.success(42);

        assertThat(result.value()).isEqualTo(42);
    }

    @Test
    void success_withValue_hasNullErrorMessage() {
        ExtractionResult<String> result = ExtractionResult.success("hello");

        assertThat(result.errorMessage()).isNull();
    }

    @Test
    void success_withValue_hasErrorReturnsFalse() {
        ExtractionResult<Integer> result = ExtractionResult.success(100);

        assertThat(result.hasError()).isFalse();
    }

    @Test
    void success_withNullValue_hasErrorReturnsFalse() {
        ExtractionResult<Integer> result = ExtractionResult.success(null);

        assertThat(result.hasError()).isFalse();
        assertThat(result.value()).isNull();
    }

    @Test
    void success_withStringValue_storesCorrectly() {
        ExtractionResult<String> result = ExtractionResult.success("test-value");

        assertThat(result.value()).isEqualTo("test-value");
        assertThat(result.hasError()).isFalse();
    }

    // ============================================================================
    // failure() factory
    // ============================================================================

    @Test
    void failure_withMessage_storesErrorMessage() {
        ExtractionResult<Integer> result = ExtractionResult.failure("Something went wrong");

        assertThat(result.errorMessage()).isEqualTo("Something went wrong");
    }

    @Test
    void failure_withMessage_hasNullValue() {
        ExtractionResult<Integer> result = ExtractionResult.failure("Error occurred");

        assertThat(result.value()).isNull();
    }

    @Test
    void failure_withMessage_hasErrorReturnsTrue() {
        ExtractionResult<String> result = ExtractionResult.failure("extraction failed");

        assertThat(result.hasError()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "detailed error message", "Error: file not found"})
    void failure_withVariousMessages_hasErrorAlwaysTrue(String message) {
        ExtractionResult<Integer> result = ExtractionResult.failure(message);

        assertThat(result.hasError()).isTrue();
    }

    // ============================================================================
    // hasError() boundary condition: null errorMessage == no error
    // ============================================================================

    @Test
    void hasError_whenErrorMessageIsNull_returnsFalse() {
        // Direct construction with null errorMessage
        ExtractionResult<String> result = new ExtractionResult<>(null, null);

        assertThat(result.hasError()).isFalse();
    }

    @Test
    void hasError_whenErrorMessageIsNonNull_returnsTrue() {
        ExtractionResult<String> result = new ExtractionResult<>(null, "some error");

        assertThat(result.hasError()).isTrue();
    }

    // ============================================================================
    // Record equality
    // ============================================================================

    @Test
    void success_twoResultsWithSameValue_areEqual() {
        ExtractionResult<Integer> r1 = ExtractionResult.success(7);
        ExtractionResult<Integer> r2 = ExtractionResult.success(7);

        assertThat(r1).isEqualTo(r2);
    }

    @Test
    void failure_twoResultsWithSameMessage_areEqual() {
        ExtractionResult<Integer> r1 = ExtractionResult.failure("err");
        ExtractionResult<Integer> r2 = ExtractionResult.failure("err");

        assertThat(r1).isEqualTo(r2);
    }

    @Test
    void success_andFailure_withSameValueNull_areNotEqual() {
        ExtractionResult<Integer> success = ExtractionResult.success(null);
        ExtractionResult<Integer> failure = ExtractionResult.failure("error");

        assertThat(success).isNotEqualTo(failure);
    }
}
