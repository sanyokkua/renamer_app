package ua.renamer.app.utils.text;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.enums.TextCaseOptions;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class providing various case conversion methods for strings.
 * All methods are static, pure functions without side effects.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CaseUtils {

    private static final String SPACE_SYMBOL = " ";

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
    public static List<String> getSeparateWordsFromInputString(String inputString) {
        if (TextUtils.isEmpty(inputString)) {
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
    public static List<String> divideStringToListOfWords(String text) {
        if (TextUtils.isEmpty(text)) {
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
                .toList();
    }

    /**
     * Converts a string to camelCase.
     * Words are separated by whitespace, underscores, hyphens, or periods, and the first word is in lowercase.
     *
     * @param inputString the string to convert
     * @return the camelCase version of the string, or the original string if it is empty or null
     */
    public static String toCamelCase(final String inputString) {
        if (TextUtils.isEmpty(inputString)) {
            return inputString;
        }

        var separateWords = getSeparateWordsFromInputString(inputString);

        if (separateWords.isEmpty()) {
            return inputString;
        }

        var firstWordInLowerCase = separateWords.getFirst().toLowerCase(Locale.ROOT);

        return firstWordInLowerCase + separateWords.stream()
                .skip(1)
                .map(TextUtils::capitalize)
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
        if (TextUtils.isEmpty(inputString)) {
            return inputString;
        }

        var separateWords = getSeparateWordsFromInputString(inputString);

        return separateWords.stream().map(TextUtils::capitalize).collect(Collectors.joining());
    }

    /**
     * Converts a string to snake_case.
     * Words are separated by underscores, and the entire string is in lowercase.
     *
     * @param inputString the string to convert
     * @return the snake_case version of the string, or the original string if it is empty or null
     */
    public static String toSnakeCase(final String inputString) {
        if (TextUtils.isEmpty(inputString)) {
            return inputString;
        }

        var separateWords = getSeparateWordsFromInputString(inputString);

        return String.join("_", separateWords).toLowerCase(Locale.ROOT);
    }

    /**
     * Converts a string to SCREAMING_SNAKE_CASE.
     * Words are separated by underscores, and the entire string is in uppercase.
     *
     * @param inputString the string to convert
     * @return the SCREAMING_SNAKE_CASE version of the string, or the original string if it is empty or null
     */
    public static String toScreamingSnakeCase(final String inputString) {
        if (TextUtils.isEmpty(inputString)) {
            return inputString;
        }

        var separateWords = getSeparateWordsFromInputString(inputString);

        return String.join("_", separateWords).toUpperCase(Locale.ROOT);
    }

    /**
     * Converts a string to kebab-case.
     * Words are separated by hyphens, and the entire string is in lowercase.
     *
     * @param inputString the string to convert
     * @return the kebab-case version of the string, or the original string if it is empty or null
     */
    public static String toKebabCase(final String inputString) {
        if (TextUtils.isEmpty(inputString)) {
            return inputString;
        }

        var separateWords = getSeparateWordsFromInputString(inputString);

        return String.join("-", separateWords).toLowerCase(Locale.ROOT);
    }

    /**
     * Converts a string to uppercase.
     *
     * @param inputString the string to convert
     * @return the uppercase version of the string, or the original string if it is empty or null
     */
    public static String toUppercase(final String inputString) {
        if (TextUtils.isEmpty(inputString)) {
            return inputString;
        }

        return inputString.toUpperCase(Locale.ROOT);
    }

    /**
     * Converts a string to lowercase.
     *
     * @param inputString the string to convert
     * @return the lowercase version of the string, or the original string if it is empty or null
     */
    public static String toLowercase(final String inputString) {
        if (TextUtils.isEmpty(inputString)) {
            return inputString;
        }

        return inputString.toLowerCase(Locale.ROOT);
    }

    /**
     * Converts a string to Title Case.
     * Words are separated by spaces, and each word starts with an uppercase letter followed by lowercase letters.
     *
     * @param inputString the string to convert
     * @return the Title Case version of the string, or the original string if it is empty or null
     */
    public static String toTitleCase(final String inputString) {
        if (TextUtils.isEmpty(inputString)) {
            return inputString;
        }

        var separateWords = getSeparateWordsFromInputString(inputString);

        return separateWords.stream().map(TextUtils::capitalize).collect(Collectors.joining(SPACE_SYMBOL));
    }

    /**
     * Dispatches a string to the appropriate case-conversion method based on the provided {@link TextCaseOptions}.
     *
     * @param input      the string to convert; returned unchanged if empty or null
     * @param caseOption the target case format; must not be null — returns {@code input} unchanged and logs a warning if null
     * @return the converted string, or the original {@code input} if {@code input} is empty, null, or {@code caseOption} is null
     */
    public static String toProvidedCase(String input, TextCaseOptions caseOption) {
        if (caseOption == null) {
            log.warn("Case option is null, returning original input");
            return input;
        }
        return switch (caseOption) {
            case CAMEL_CASE -> toCamelCase(input);
            case PASCAL_CASE -> toPascalCase(input);
            case SNAKE_CASE -> toSnakeCase(input);
            case SNAKE_CASE_SCREAMING -> toScreamingSnakeCase(input);
            case KEBAB_CASE -> toKebabCase(input);
            case UPPERCASE -> toUppercase(input);
            case LOWERCASE -> toLowercase(input);
            case TITLE_CASE -> toTitleCase(input);
        };
    }

}
