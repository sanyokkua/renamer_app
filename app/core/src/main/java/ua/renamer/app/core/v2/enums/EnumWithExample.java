package ua.renamer.app.core.v2.enums;

/**
 * An interface representing an enum with an example string.
 * Implementing enums should provide a method to get an example string.
 *
 * <p>This is a V2-specific copy of the original interface from ua.renamer.app.core.enums package.
 * Created to support the V2 architecture redesign with independent type definitions.</p>
 */
public interface EnumWithExample {

    /**
     * Gets an example string representing the enum constant.
     *
     * @return the example string.
     */
    String getExampleString();

}
