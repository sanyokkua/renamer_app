package ua.renamer.app.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for performing calculations related to file sizes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CalculationUtils {

    /**
     * Converts bytes to kilobytes.
     *
     * @param bytes The size in bytes to be converted.
     * @return The size in kilobytes.
     */
    public static long toKilobytes(long bytes) {
        return bytes / 1024L;
    }

    /**
     * Converts bytes to megabytes.
     *
     * @param bytes The size in bytes to be converted.
     * @return The size in megabytes.
     */
    public static long toMegabytes(long bytes) {
        return toKilobytes(bytes) / 1024L; // Use long literal to avoid integer division truncation
    }

}
