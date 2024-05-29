package ua.renamer.app.core.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import ua.renamer.app.core.enums.TextCaseOptions;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class StringUtilsTest {

    static Stream<Arguments> capitalizeArguments() {
        return Stream.of(
                arguments("", ""),
                arguments("  ", "  "),
                arguments("hello", "Hello"),
                arguments("java", "Java"),
                arguments("JUnit", "Junit"),
                arguments("parameterized", "Parameterized"),
                arguments("pARAMETERIZED", "Parameterized")
                        );
    }

    static Stream<Arguments> toCamelCaseArguments() {
        return Stream.of(
                arguments(null, null),
                arguments("", ""),
                arguments("    ", "    "),
                arguments("THIS IS CAMEL CASE", "thisIsCamelCase"),
                arguments("THIS_IS_CAMEL_CASE", "thisIsCamelCase"),
                arguments("this-IS-camel-CASE", "thisIsCamelCase"),
                arguments("THIS.IS.CAMEL.CASE", "thisIsCamelCase"),
                arguments("this_IS   .caMel-CASE", "thisIsCamelCase"),
                arguments("  .-_", "  .-_")
                        );
    }

    static Stream<Arguments> toPascalCaseArguments() {
        return Stream.of(
                arguments(null, null),
                arguments("", ""),
                arguments("    ", "    "),
                arguments("THIS IS PASCAL CASE", "ThisIsPascalCase"),
                arguments("THIS_IS_PASCAL_CASE", "ThisIsPascalCase"),
                arguments("this-IS-pascal-casE", "ThisIsPascalCase"),
                arguments("this.is.pascal.case", "ThisIsPascalCase"),
                arguments("this_IS   .pascal-CASE", "ThisIsPascalCase")
                        );
    }

    static Stream<Arguments> toSnakeCaseArguments() {
        return Stream.of(
                arguments(null, null),
                arguments("", ""),
                arguments("    ", "    "),
                arguments("THIS IS SNAKE CASE", "this_is_snake_case"),
                arguments("THIS_IS_SNAKE_CASE", "this_is_snake_case"),
                arguments("this-IS-snake-casE", "this_is_snake_case"),
                arguments("this.is.snake.case", "this_is_snake_case"),
                arguments("this_IS   .snake-CASE", "this_is_snake_case")
                        );
    }

    static Stream<Arguments> toScreamingSnakeCaseArguments() {
        return Stream.of(
                arguments(null, null),
                arguments("", ""),
                arguments("    ", "    "),
                arguments("THIS IS SCREAMING SNAKE CASE", "THIS_IS_SCREAMING_SNAKE_CASE"),
                arguments("THIS_IS_SCREAMING_SNAKE_CASE", "THIS_IS_SCREAMING_SNAKE_CASE"),
                arguments("this-IS-sCreaming-snake-casE", "THIS_IS_SCREAMING_SNAKE_CASE"),
                arguments("this.is.screaming.snake.case", "THIS_IS_SCREAMING_SNAKE_CASE"),
                arguments("this_IS screaming  .snake-CASE", "THIS_IS_SCREAMING_SNAKE_CASE")
                        );
    }

    static Stream<Arguments> toKebabCaseArguments() {
        return Stream.of(
                arguments(null, null),
                arguments("", ""),
                arguments("    ", "    "),
                arguments("THIS IS KEBAB CASE", "this-is-kebab-case"),
                arguments("THIS_IS_KEBAB_CASE", "this-is-kebab-case"),
                arguments("this-IS-kebab-casE", "this-is-kebab-case"),
                arguments("this.is.kebab.case", "this-is-kebab-case"),
                arguments("this_IS   .kebab-CASE", "this-is-kebab-case")
                        );
    }

    static Stream<Arguments> toUppercaseArguments() {
        return Stream.of(
                arguments(null, null),
                arguments("", ""),
                arguments("    ", "    "),
                arguments("THIS IS UPPER CASE", "THIS IS UPPER CASE"),
                arguments("THIS_IS_UPPER_CASE", "THIS_IS_UPPER_CASE"),
                arguments("this-IS-upper-casE", "THIS-IS-UPPER-CASE"),
                arguments("this.is.upper.case", "THIS.IS.UPPER.CASE"),
                arguments("this_IS   .upper-CASE", "THIS_IS   .UPPER-CASE")
                        );
    }

    static Stream<Arguments> toLowercaseArguments() {
        return Stream.of(
                arguments(null, null),
                arguments("", ""),
                arguments("    ", "    "),
                arguments("THIS IS LOWER CASE", "this is lower case"),
                arguments("THIS_IS_LOWER_CASE", "this_is_lower_case"),
                arguments("this-IS-lower-casE", "this-is-lower-case"),
                arguments("this.is.lower.case", "this.is.lower.case"),
                arguments("this_IS   .lower-CASE", "this_is   .lower-case")
                        );
    }

    static Stream<Arguments> toTitleCaseArguments() {
        return Stream.of(
                arguments(null, null),
                arguments("", ""),
                arguments("    ", "    "),
                arguments("THIS IS TITLE CASE", "This Is Title Case"),
                arguments("THIS_IS_TITLE_CASE", "This Is Title Case"),
                arguments("this-IS-title-casE", "This Is Title Case"),
                arguments("this.is.title.case", "This Is Title Case"),
                arguments("this_IS   .title-CASE", "This Is Title Case")
                        );
    }

    static Stream<Arguments> testToProvidedCaseArguments() {
        return Stream.of(
                arguments(null, null, null),
                arguments("", TextCaseOptions.CAMEL_CASE, ""),
                arguments(" This text.should_Be-converted  ", TextCaseOptions.CAMEL_CASE, "thisTextShouldBeConverted"),
                arguments(" This text.should_Be-converted  ", TextCaseOptions.PASCAL_CASE, "ThisTextShouldBeConverted"),
                arguments(" This text.should_Be-converted  ",
                          TextCaseOptions.SNAKE_CASE,
                          "this_text_should_be_converted"
                         ),
                arguments(" This text.should_Be-converted  ",
                          TextCaseOptions.SNAKE_CASE_SCREAMING,
                          "THIS_TEXT_SHOULD_BE_CONVERTED"
                         ),
                arguments(" This text.should_Be-converted  ",
                          TextCaseOptions.KEBAB_CASE,
                          "this-text-should-be-converted"
                         ),
                arguments(" This text.should_Be-converted  ",
                          TextCaseOptions.UPPERCASE,
                          " THIS TEXT.SHOULD_BE-CONVERTED  "
                         ),
                arguments(" This text.should_Be-converted  ",
                          TextCaseOptions.LOWERCASE,
                          " this text.should_be-converted  "
                         ),
                arguments(" This text.should_Be-converted  ",
                          TextCaseOptions.TITLE_CASE,
                          "This Text Should Be Converted"
                         )
                        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\n", "\t"})
    void testIsEmptyWithNullAndWhitespaceReturnsTrue(String value) {
        assertTrue(StringUtils.isEmpty(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "test"})
    void testIsEmptyWithNonEmptyStringsReturnsFalse(String value) {
        assertFalse(StringUtils.isEmpty(value));
    }

    @ParameterizedTest
    @MethodSource("capitalizeArguments")
    void testCapitalize(String actualValue, String expectedValue) {
        var result = StringUtils.capitalize(actualValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("toCamelCaseArguments")
    void testToCamelCase(String actualValue, String expectedValue) {
        var result = StringUtils.toCamelCase(actualValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("toPascalCaseArguments")
    void testToPascalCase(String actualValue, String expectedValue) {
        var result = StringUtils.toPascalCase(actualValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("toSnakeCaseArguments")
    void testToSnakeCase(String actualValue, String expectedValue) {
        var result = StringUtils.toSnakeCase(actualValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("toScreamingSnakeCaseArguments")
    void testToScreamingSnakeCase(String actualValue, String expectedValue) {
        var result = StringUtils.toScreamingSnakeCase(actualValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("toKebabCaseArguments")
    void testToKebabCase(String actualValue, String expectedValue) {
        var result = StringUtils.toKebabCase(actualValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("toUppercaseArguments")
    void testToUppercase(String actualValue, String expectedValue) {
        var result = StringUtils.toUppercase(actualValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("toLowercaseArguments")
    void testToLowercase(String actualValue, String expectedValue) {
        var result = StringUtils.toLowercase(actualValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("toTitleCaseArguments")
    void testToTitleCase(String actualValue, String expectedValue) {
        var result = StringUtils.toTitleCase(actualValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("testToProvidedCaseArguments")
    void testToProvidedCase(String actualValue, TextCaseOptions options, String expectedValue) {
        var result = StringUtils.toProvidedCase(actualValue, options);

        assertEquals(expectedValue, result);
    }

}