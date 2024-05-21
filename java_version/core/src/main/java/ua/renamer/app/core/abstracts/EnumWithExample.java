package ua.renamer.app.core.abstracts;

/**
 * An interface representing an enum with an example string.
 * Implementing enums should provide a method to get an example string.
 */
public interface EnumWithExample {

    /**
     * Gets an example string representing the enum constant.
     *
     * @return the example string.
     */
    String getExampleString();

}
