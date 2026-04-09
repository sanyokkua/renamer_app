package ua.renamer.app.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for performing calculations related to file sizes.
 * All methods are static, pure functions without side effects.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SizeUtils {

    /**
     * Converts bytes to megabytes.
     *
     * @param bytes The size in bytes to be converted.
     * @return The size in megabytes.
     */
    public static long toMegabytes(long bytes) {
        return toKilobytes(bytes) / 1024L;
    }

    /**
     * Converts bytes to kilobytes.
     *
     * @param bytes The size in bytes to be converted.
     * @return The size in kilobytes.
     */
    public static long toKilobytes(long bytes) {
        return bytes / 1024L;
    }

}
