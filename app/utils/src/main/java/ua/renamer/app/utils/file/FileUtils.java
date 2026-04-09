package ua.renamer.app.utils.file;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Utility class providing file path manipulation methods.
 * All methods are static, pure functions without side effects.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {

    /**
     * Extracts the base name (filename without extension) from a path.
     * Handles hidden files (files starting with '.') correctly.
     *
     * @param path the file path
     * @return the base name of the file
     */
    public static String getFileBaseName(Path path) {
        Path fileNamePath = path.getFileName();
        if (fileNamePath == null) {
            return "";
        }
        String fileName = fileNamePath.toString();

        // Handle hidden files (files starting with .)
        if (fileName.startsWith(".")) {
            int lastDotIndex = fileName.lastIndexOf('.');
            // If it's just a dot or only one dot at the start, the whole name is the base name
            if (lastDotIndex == 0) {
                return fileName;
            }
            // If there are multiple dots, return everything up to the last dot
            return fileName.substring(0, lastDotIndex);
        }

        return FilenameUtils.getBaseName(fileName);
    }

    /**
     * Returns the absolute path of the given path as a string.
     *
     * @param path the file path
     * @return the absolute path string
     */
    public static String getFileAbsolutePath(Path path) {
        return path.toAbsolutePath().toString();
    }

    /**
     * Extracts the file extension from a path.
     * Handles hidden files (files starting with '.') correctly.
     * Returns empty string if no extension is present.
     *
     * @param path the file path
     * @return the file extension (without the leading dot), or empty string
     */
    public static String getFileExtension(Path path) {
        Path fileNamePath = path.getFileName();
        if (fileNamePath == null) {
            return "";
        }
        String fileName = fileNamePath.toString();

        // Handle hidden files (files starting with .)
        if (fileName.startsWith(".")) {
            int lastDotIndex = fileName.lastIndexOf('.');
            // If there's only one dot at the start, there's no extension
            if (lastDotIndex == 0) {
                return "";
            }
            // Return the part after the last dot
            return fileName.substring(lastDotIndex + 1);
        }

        return FilenameUtils.getExtension(fileName);
    }

    /**
     * Retrieves the parent folders of a file or directory given its path string.
     * Normalizes backslashes to forward slashes and removes redundant/trailing slashes.
     * Excludes the root element and the last path component (filename or directory).
     *
     * @param filePath The path of the file or directory.
     * @return A list containing the names of the parent folders, or empty list if none.
     */
    public static List<String> getParentFolders(String filePath) {
        if (Objects.isNull(filePath) || filePath.trim().isEmpty()) {
            return List.of();
        }
        // Normalize the file path by replacing backslashes with forward slashes
        // and removing redundant slashes and trailing slash
        String normalizedPath = filePath.replace("\\", "/").replaceAll("//+", "/").replaceAll("/$", "");
        String[] splitPathItems = normalizedPath.split("/");
        if (splitPathItems.length == 0 || splitPathItems.length == 1) {
            // If path is empty or file in the root
            return List.of();
        }
        // Exclude the root element (drive letter for Windows or empty string for Unix)
        // and exclude last item (filename or directory)
        return Arrays.asList(splitPathItems).subList(1, splitPathItems.length - 1);
    }

}
