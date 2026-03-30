package ua.renamer.app.utils.text;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Utility class providing basic text manipulation methods.
 * All methods are static, pure functions without side effects.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TextUtils {

    /**
     * Checks if a given string is empty or null.
     *
     * @param input the string to check
     *
     * @return true if the string is null or empty, false otherwise
     */
    public static boolean isEmpty(final String input) {
        return Objects.isNull(input) || input.isBlank();
    }

    /**
     * Capitalizes the first letter of the given word and converts the rest of the letters to lowercase.
     *
     * @param word the word to capitalize
     *
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
     * Combines a file name and extension into a full file name string.
     * Handles dot prefix logic: if ext already starts with ".", it's used as-is;
     * if ext is null/blank, no extension is appended; otherwise a "." is prepended.
     *
     * @param name the file name
     * @param ext  the file extension
     *
     * @return the full file name (e.g., "fileName.ext")
     */
    public static String getFileNewFullName(String name, String ext) {
        var fixedExt = "";
        if (ext != null && !ext.isBlank() && ext.startsWith(".")) {
            fixedExt = ext;
        } else if (Objects.isNull(ext) || ext.isBlank()) {
            fixedExt = "";
        } else {
            fixedExt = ".%s".formatted(ext);
        }

        return "%s%s".formatted(name, fixedExt);
    }

}
