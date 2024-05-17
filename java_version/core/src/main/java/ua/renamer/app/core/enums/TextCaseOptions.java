package ua.renamer.app.core.enums;

import ua.renamer.app.core.abstracts.EnumWithExample;

/**
 * An enumeration representing different options for text casing.
 * Each enum constant provides an example string of the casing.
 */
public enum TextCaseOptions implements EnumWithExample {
    CAMEL_CASE("camelCaseText"),
    PASCAL_CASE("PascalCaseText"),
    SNAKE_CASE("snake_case_text"),
    SNAKE_CASE_SCREAMING("SNAKE_CASE_TEXT"),
    KEBAB_CASE("kebab-case-text"),
    UPPERCASE("UPPERCASE TEXT"),
    LOWERCASE("lowercase text"),
    TITLE_CASE("Title Case Text");

    private final String example;

    TextCaseOptions(String example) {
        this.example = example;
    }

    /**
     * Gets an example string of the text case option.
     *
     * @return the example string.
     */
    @Override
    public String getExampleString() {
        return this.example;
    }
}
