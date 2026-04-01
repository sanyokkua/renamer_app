package ua.renamer.app.api.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {

    // --- valid() factory ---

    @Test
    void valid_whenCalled_thenOkIsTrue() {
        ValidationResult result = ValidationResult.valid();

        assertTrue(result.ok());
    }

    @Test
    void valid_whenCalled_thenFieldIsNull() {
        ValidationResult result = ValidationResult.valid();

        assertNull(result.field());
    }

    @Test
    void valid_whenCalled_thenMessageIsNull() {
        ValidationResult result = ValidationResult.valid();

        assertNull(result.message());
    }

    @Test
    void valid_whenCalled_thenIsErrorReturnsFalse() {
        ValidationResult result = ValidationResult.valid();

        assertFalse(result.isError());
    }

    // --- fieldError() factory ---

    @Test
    void fieldError_whenCalled_thenOkIsFalse() {
        ValidationResult result = ValidationResult.fieldError("email", "must not be blank");

        assertFalse(result.ok());
    }

    @Test
    void fieldError_whenCalled_thenFieldMatches() {
        ValidationResult result = ValidationResult.fieldError("text", "empty");

        assertEquals("text", result.field());
    }

    @Test
    void fieldError_whenCalled_thenMessageMatches() {
        ValidationResult result = ValidationResult.fieldError("count", "must be positive");

        assertEquals("must be positive", result.message());
    }

    @Test
    void fieldError_whenCalled_thenIsErrorReturnsTrue() {
        ValidationResult result = ValidationResult.fieldError("name", "required");

        assertTrue(result.isError());
    }

    // --- isError() is the inverse of ok ---

    @Test
    void isError_whenOkIsTrue_thenReturnsFalse() {
        ValidationResult result = new ValidationResult(true, null, null);

        assertFalse(result.isError());
    }

    @Test
    void isError_whenOkIsFalse_thenReturnsTrue() {
        ValidationResult result = new ValidationResult(false, "f", "m");

        assertTrue(result.isError());
    }

    // --- record equality and identity ---

    @Test
    void valid_whenCalledTwice_thenResultsAreEqual() {
        ValidationResult a = ValidationResult.valid();
        ValidationResult b = ValidationResult.valid();

        assertEquals(a, b);
    }

    @Test
    void fieldError_whenSameArguments_thenResultsAreEqual() {
        ValidationResult a = ValidationResult.fieldError("field", "msg");
        ValidationResult b = ValidationResult.fieldError("field", "msg");

        assertEquals(a, b);
    }

    @Test
    void fieldError_whenDifferentFields_thenResultsAreNotEqual() {
        ValidationResult a = ValidationResult.fieldError("fieldA", "msg");
        ValidationResult b = ValidationResult.fieldError("fieldB", "msg");

        assertNotEquals(a, b);
    }

    // --- boundary: empty strings are accepted by the record ---

    @Test
    void fieldError_withEmptyField_thenFieldIsEmpty() {
        ValidationResult result = ValidationResult.fieldError("", "some error");

        assertEquals("", result.field());
    }

    @Test
    void fieldError_withEmptyMessage_thenMessageIsEmpty() {
        ValidationResult result = ValidationResult.fieldError("someField", "");

        assertEquals("", result.message());
    }
}
