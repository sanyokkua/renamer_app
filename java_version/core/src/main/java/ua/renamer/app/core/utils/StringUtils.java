package ua.renamer.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.enums.TextCaseOptions;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class providing various string manipulation methods.
 * This class contains methods to convert strings to different cases
 * and to perform basic string checks.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtils {

    private static final String SPACE_SYMBOL = " ";

    /**
     * Checks if a given string is empty or null.
     *
     * @param input the string to check
     * @return true if the string is null or empty, false otherwise
     */
    public static boolean isEmpty(final String input) {
        return Objects.isNull(input) || input.isBlank();
    }

    /**
     * Capitalizes the first letter of the given word and converts the rest of the letters to lowercase.
     *
     * @param word the word to capitalize
     * @return the capitalized word, or the original word if it is empty or null
     */
    public static String capitalize(final String word) {
        if (isEmpty(word)) {
            return word;
        }

        var firstLetter = word.substring(0, 1).toUpperCase();
        var restOfLetters = word.substring(1).toLowerCase();

        return firstLetter + restOfLetters;
    }

    /**
     * Replaces certain delimiters in the input string with spaces and then divides the string
     * into a list of words.
     * <p>
     * The delimiters that are replaced with spaces are:
     * - Underscores '_'
     * - Hyphens '-'
     * - Periods '.'
     *
     * @param inputString the string to process
     * @return a list of words from the processed string
     */
    static List<String> getSeparateWordsFromInputString(String inputString) {
        if (isEmpty(inputString)) {
            return List.of();
        }
        var stringWithReplacedDividers = inputString.replace("_", SPACE_SYMBOL)
                                                    .replace("-", SPACE_SYMBOL)
                                                    .replace(".", SPACE_SYMBOL);

        return divideStringToListOfWords(stringWithReplacedDividers);
    }

    /**
     * Divides a string into a list of words separated by whitespace, numbers, case change.
     *
     * @param text the string to divide
     * @return a list of words, or an empty list if the input string is empty or null
     */
    private static List<String> divideStringToListOfWords(String text) {
        if (isEmpty(text)) {
            return List.of();
        }

        // Split by spaces first
        String[] splitResult = text.split("\\s+");

        // Further split each part by camelCase, PascalCase, and alphanumeric boundaries
        Pattern camelCasePattern = Pattern.compile("(?<=[a-z])(?=[A-Z])");
        Pattern alphanumericPattern = Pattern.compile("(?<=\\d)(?=\\D)|(?<=\\D)(?=\\d)");

        return Stream.of(splitResult)
                     .flatMap(part -> Stream.of(camelCasePattern.split(part)))
                     .flatMap(part -> Stream.of(alphanumericPattern.split(part)))
                     .filter(word -> !word.isBlank())
                     .collect(Collectors.toList());
    }

    /**
     * Converts a string to camelCase.
     * Words are separated by whitespace, underscores, hyphens, or periods, and the first word is in lowercase.
     *
     * @param inputString the string to convert
     * @return the camelCase version of the string, or the original string if it is empty or null
     */
    public static String toCamelCase(final String inputString) {
        if (isEmpty(inputString)) {
            return inputString;
        }

        var separateWords = getSeparateWordsFromInputString(inputString);

        if (separateWords.isEmpty()) {
            return inputString;
        }

        var firstWordInLowerCase = separateWords.getFirst().toLowerCase();

        return firstWordInLowerCase + separateWords.stream()
                                                   .skip(1)
                                                   .map(StringUtils::capitalize)
                                                   .collect(Collectors.joining());
    }

    /**
     * Converts a string to PascalCase.
     * Words are separated by whitespace, underscores, hyphens, or periods, and each word starts with an uppercase letter.
     *
     * @param inputString the string to convert
     * @return the PascalCase version of the string, or the original string if it is empty or null
     */
    public static String toPascalCase(final String inputString) {
        if (isEmpty(inputString)) {
            return inputString;
        }

        var separateWords = getSeparateWordsFromInputString(inputString);

        return separateWords.stream().map(StringUtils::capitalize).collect(Collectors.joining());
    }

    /**
     * Converts a string to snake_case.
     * Words are separated by underscores, and the entire string is in lowercase.
     *
     * @param inputString the string to convert
     * @return the snake_case version of the string, or the original string if it is empty or null
     */
    public static String toSnakeCase(final String inputString) {
        if (isEmpty(inputString)) {
            return inputString;
        }

        var separateWords = getSeparateWordsFromInputString(inputString);

        return String.join("_", separateWords).toLowerCase();
    }

    /**
     * Converts a string to SCREAMING_SNAKE_CASE.
     * Words are separated by underscores, and the entire string is in uppercase.
     *
     * @param inputString the string to convert
     * @return the SCREAMING_SNAKE_CASE version of the string, or the original string if it is empty or null
     */
    public static String toScreamingSnakeCase(final String inputString) {
        if (isEmpty(inputString)) {
            return inputString;
        }

        var separateWords = getSeparateWordsFromInputString(inputString);

        return String.join("_", separateWords).toUpperCase();
    }

    /**
     * Converts a string to kebab-case.
     * Words are separated by hyphens, and the entire string is in lowercase.
     *
     * @param inputString the string to convert
     * @return the kebab-case version of the string, or the original string if it is empty or null
     */
    public static String toKebabCase(final String inputString) {
        if (isEmpty(inputString)) {
            return inputString;
        }

        var separateWords = getSeparateWordsFromInputString(inputString);

        return String.join("-", separateWords).toLowerCase();
    }

    /**
     * Converts a string to uppercase.
     *
     * @param inputString the string to convert
     * @return the uppercase version of the string, or the original string if it is empty or null
     */
    public static String toUppercase(final String inputString) {
        if (isEmpty(inputString)) {
            return inputString;
        }

        return inputString.toUpperCase();
    }

    /**
     * Converts a string to lowercase.
     *
     * @param inputString the string to convert
     * @return the lowercase version of the string, or the original string if it is empty or null
     */
    public static String toLowercase(final String inputString) {
        if (isEmpty(inputString)) {
            return inputString;
        }

        return inputString.toLowerCase();
    }

    /**
     * Converts a string to Title Case.
     * Words are separated by spaces, and each word starts with an uppercase letter followed by lowercase letters.
     *
     * @param inputString the string to convert
     * @return the Title Case version of the string, or the original string if it is empty or null
     */
    public static String toTitleCase(final String inputString) {
        if (isEmpty(inputString)) {
            return inputString;
        }

        var separateWords = getSeparateWordsFromInputString(inputString);

        return separateWords.stream().map(StringUtils::capitalize).collect(Collectors.joining(SPACE_SYMBOL));
    }

    /**
     * Converts a string to a specified case based on the provided TextCaseOptions.
     *
     * @param inputString     the string to convert
     * @param textCaseOptions the case option to apply
     * @return the string converted to the specified case, or the original string if it is empty or null
     */
    public static String toProvidedCase(final String inputString, TextCaseOptions textCaseOptions) {
        if (isEmpty(inputString)) {
            return inputString;
        }

        return switch (textCaseOptions) {
            case CAMEL_CASE -> toCamelCase(inputString);
            case PASCAL_CASE -> toPascalCase(inputString);
            case SNAKE_CASE -> toSnakeCase(inputString);
            case SNAKE_CASE_SCREAMING -> toScreamingSnakeCase(inputString);
            case KEBAB_CASE -> toKebabCase(inputString);
            case UPPERCASE -> toUppercase(inputString);
            case LOWERCASE -> toLowercase(inputString);
            case TITLE_CASE -> toTitleCase(inputString);
        };
    }

}
