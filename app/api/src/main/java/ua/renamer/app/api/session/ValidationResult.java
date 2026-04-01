package ua.renamer.app.api.session;

/**
 * Result of a configuration validation check.
 *
 * @param ok      {@code true} if validation passed, {@code false} if a field error was found
 * @param field   the name of the field that failed validation; {@code null} when {@code ok} is {@code true}
 * @param message a human-readable description of the validation error; {@code null} when {@code ok} is {@code true}
 */
public record ValidationResult(boolean ok, String field, String message) {

    /**
     * Creates a successful validation result.
     *
     * @return a {@link ValidationResult} with {@code ok} set to {@code true}
     */
    public static ValidationResult valid() {
        return new ValidationResult(true, null, null);
    }

    /**
     * Creates a field-level validation error.
     *
     * @param field   the name of the field that failed validation; must not be null
     * @param message a human-readable error description; must not be null
     * @return a {@link ValidationResult} with {@code ok} set to {@code false}
     */
    public static ValidationResult fieldError(String field, String message) {
        return new ValidationResult(false, field, message);
    }

    /**
     * Returns {@code true} if this result represents a validation error.
     *
     * @return {@code true} if validation failed, {@code false} if it passed
     */
    public boolean isError() {
        return !ok;
    }
}
