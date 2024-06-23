package ua.renamer.app.core.service.validator;

/**
 * FunctionalInterface for validating objects of type T.
 *
 * @param <T> The type of objects to validate.
 */
@FunctionalInterface
public interface Validator<T> {

    /**
     * Validates an object of type T.
     *
     * @param value The value to validate.
     *
     * @return {@code true} if the value is valid, {@code false} otherwise.
     */
    boolean isValid(T value);

}
