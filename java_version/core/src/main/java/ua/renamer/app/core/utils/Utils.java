package ua.renamer.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Utility class providing various utility methods.
 * This class contains methods for handling common tasks such as finding the minimum value in a list of LocalDateTime objects.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    /**
     * Finds the minimum LocalDateTime value from an array of LocalDateTime objects.
     * If the input array is null or empty, the method returns null. Null values in the array are ignored.
     *
     * @param values an array of LocalDateTime objects to search
     * @return the minimum LocalDateTime value, or null if the input array is null, empty, or contains only null values
     */
    public static LocalDateTime findMinOrNull(LocalDateTime... values) {
        if (values == null || values.length == 0) {
            return null;
        }

        return Stream.of(values)
                     .filter(Objects::nonNull)
                     .min(LocalDateTime::compareTo)
                     .orElse(null);
    }

}
