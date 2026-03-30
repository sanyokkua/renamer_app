package ua.renamer.app.utils.text;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class CaseUtilsTest {

    static Stream<Arguments> toCamelCaseArguments() {
        return Stream.of(
                arguments(null, null),
                arguments("", ""),
                arguments("    ", "    "),
                arguments("THIS IS CAMEL CASE", "thisIsCamelCase"),
                arguments("THIS_IS_CAMEL_CASE", "thisIsCamelCase"),
                arguments("this-IS-camel-CASE", "thisIsCamelCase"),
                arguments("THIS.IS.CAMEL.CASE", "thisIsCamelCase"),
                arguments("this_IS   .camel-CASE", "thisIsCamelCase"),
                arguments("  .-_", "  .-_"),
                arguments("ThisShouldBeCamelCase", "thisShouldBeCamelCase"),
                arguments("thisShouldBeCamelCase", "thisShouldBeCamelCase"),
                arguments("This Should Be Camel Case", "thisShouldBeCamelCase")
        );
    }

    static Stream<Arguments> toPascalCaseArguments() {
        return Stream.of(
                arguments(null, null),
                arguments("", ""),
                arguments("    ", "    "),
                arguments("THIS IS PASCAL CASE", "ThisIsPascalCase"),
                arguments("THIS_IS_PASCAL_CASE", "ThisIsPascalCase"),
                arguments("this-IS-pascal-case", "ThisIsPascalCase"),
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
                arguments("this-IS-snake-case", "this_is_snake_case"),
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
                arguments("this-IS-Screaming-snake-case", "THIS_IS_SCREAMING_SNAKE_CASE"),
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
                arguments("this-IS-kebab-case", "this-is-kebab-case"),
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
                arguments("this-IS-title-case", "This Is Title Case"),
                arguments("this.is.title.case", "This Is Title Case"),
                arguments("this_IS   .title-CASE", "This Is Title Case")
        );
    }

    static Stream<Arguments> getSeparateWordsFromInputStringArguments() {
        return Stream.of(
                arguments(null, 0),
                arguments("", 0),
                arguments("    ", 0),
                arguments("THIS IS TITLE CASE", 4),
                arguments("this_is-a_file name", 5),
                arguments("THIS_IS_A_FILE_NAME", 5),
                arguments("this-is-a-file-name", 5),
                arguments("THIS_IS-A_FILE NAME", 5),
                arguments("FileNameOriginal", 3),
                arguments("fileNameOriginal", 3),
                arguments("File_name_original", 3),
                arguments("        File_name_original", 3),
                arguments("        File_name_original         ", 3),
                arguments("        File_     name     _  original         ", 3),
                arguments("        _file_     name     _  original         ", 3),
                arguments("        _file_     ___name   _____  _  original         ", 3),
                arguments("Resume_JohnDoe", 3),
                arguments("download", 1),
                arguments("IMG_20230606_123456", 3),
                arguments("Shopping_List_2024", 3),
                arguments("12345qwerty", 2),
                arguments("12345qwerty12345", 3)
        );
    }

    static Stream<Arguments> divideStringToListOfWordsArguments() {
        return Stream.of(
                arguments(null, 0),
                arguments("", 0),
                arguments("    ", 0),
                arguments("hello world", 2),
                arguments("helloWorld", 2),
                arguments("HelloWorld", 2),
                arguments("hello123world", 3),
                arguments("123hello", 2),
                arguments("ALLCAPS", 1),
                arguments("single", 1)
        );
    }

    @ParameterizedTest
    @MethodSource("toCamelCaseArguments")
    void testToCamelCase(String actualValue, String expectedValue) {
        var result = CaseUtils.toCamelCase(actualValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("toPascalCaseArguments")
    void testToPascalCase(String actualValue, String expectedValue) {
        var result = CaseUtils.toPascalCase(actualValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("toSnakeCaseArguments")
    void testToSnakeCase(String actualValue, String expectedValue) {
        var result = CaseUtils.toSnakeCase(actualValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("toScreamingSnakeCaseArguments")
    void testToScreamingSnakeCase(String actualValue, String expectedValue) {
        var result = CaseUtils.toScreamingSnakeCase(actualValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("toKebabCaseArguments")
    void testToKebabCase(String actualValue, String expectedValue) {
        var result = CaseUtils.toKebabCase(actualValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("toUppercaseArguments")
    void testToUppercase(String actualValue, String expectedValue) {
        var result = CaseUtils.toUppercase(actualValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("toLowercaseArguments")
    void testToLowercase(String actualValue, String expectedValue) {
        var result = CaseUtils.toLowercase(actualValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("toTitleCaseArguments")
    void testToTitleCase(String actualValue, String expectedValue) {
        var result = CaseUtils.toTitleCase(actualValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("getSeparateWordsFromInputStringArguments")
    void testGetSeparateWordsFromInputString(String input, int amountOfWords) {
        var result = CaseUtils.getSeparateWordsFromInputString(input);

        assertNotNull(result);
        assertEquals(amountOfWords, result.size());
    }

    @ParameterizedTest
    @MethodSource("divideStringToListOfWordsArguments")
    void testDivideStringToListOfWords(String input, int amountOfWords) {
        var result = CaseUtils.divideStringToListOfWords(input);

        assertNotNull(result);
        assertEquals(amountOfWords, result.size());
    }

}
