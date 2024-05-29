package ua.renamer.app.core.abstracts;

public interface Validator<T> {

    boolean isValid(T value);

}
