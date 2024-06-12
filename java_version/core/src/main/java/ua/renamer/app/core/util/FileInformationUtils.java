package ua.renamer.app.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.FileInformation;

import java.util.Objects;
import java.util.function.Function;

/**
 * Utility class for working with FileInformation objects.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileInformationUtils {

    /**
     * Gets the full file name by combining the file name and extension from the provided FileInformation object.
     * If no extension is present, an empty string is used.
     *
     * @param fileInfo the FileInformation object containing file name and extension information
     *
     * @return the full file name as a String (e.g., "fileName.ext")
     */
    public static String getFileFullName(FileInformation fileInfo) {
        log.debug("getFileFullName({})", fileInfo);
        String formatted = getFileNewFullName(fileInfo.getFileName(), fileInfo.getFileExtension());
        log.debug("getFileFullName({}) formatted name:", formatted);
        return formatted;
    }

    /**
     * Helper method to get the full file name using the new name and extension from the FileInformation object.
     * Similar logic to getFileFullName but uses new name and extension.
     *
     * @param name the new name of the file
     * @param ext  the new extension of the file (including the leading ".")
     *
     * @return the full file name as a String (e.g., "fileName.ext")
     */
    private static String getFileNewFullName(String name, String ext) {
        log.debug("getFileNewFullName({}, {})", name, ext);
        var fixedExt = "";
        if (ext != null && !ext.isBlank() && ext.startsWith(".")) {
            fixedExt = ext;
        } else if (Objects.isNull(ext) || ext.isBlank()) {
            fixedExt = "";
        } else {
            fixedExt = ".%s".formatted(ext);
        }

        String formatted = "%s%s".formatted(name, fixedExt);
        log.debug("getFileNewFullName({}) formatted name:", formatted);
        return formatted;
    }

    /**
     * Gets the full file name by combining the new name and extension from the provided FileInformation object.
     * If no extension is present, an empty string is used.
     *
     * @param fileInfo the FileInformation object containing new name and extension information
     *
     * @return the full file name as a String (e.g., "fileName.ext")
     */
    public static String getFileNewFullName(FileInformation fileInfo) {
        log.debug("getFileNewFullName({})", fileInfo);
        String formatted = getFileNewFullName(fileInfo.getNewName(), fileInfo.getNewExtension());
        log.debug("getFileNewFullName({}) formatted name:", formatted);
        return formatted;
    }

    /**
     * Checks if the file name or extension has changed between the original and new information in the FileInformation object.
     *
     * @param fileInfo the FileInformation object containing file name and extension information
     *
     * @return true if the file name or extension has changed, false otherwise
     */
    public static boolean isFileHasChangedName(FileInformation fileInfo) {
        log.debug("isFileRenamed({})", fileInfo);
        boolean renamed = !(fileInfo.getFileName().equals(fileInfo.getNewName()) && fileInfo.getFileExtension()
                                                                                            .equals(fileInfo.getNewExtension()));
        log.debug("isFileRenamed() is renamed={}", renamed);
        return renamed;
    }

    /**
     * Gets the absolute path of the file without the file name.
     *
     * @param fileInfo      the FileInformation object containing file path information
     * @param nameExtractor a function that extracts the file name from the FileInformation object
     *
     * @return the absolute path of the file without the file name (e.g., "/path/to/folder/")
     *
     * @throws IllegalArgumentException if the file name is not found in the absolute path
     */
    public static String getFileAbsolutePathWithoutName(FileInformation fileInfo,
                                                        Function<FileInformation, String> nameExtractor) {
        log.debug("getFileAbsolutePathWithoutName({})", fileInfo);
        var fileName = nameExtractor.apply(fileInfo);
        var fileAbsolutePath = fileInfo.getFileAbsolutePath();

        if (!fileAbsolutePath.endsWith(fileName)) {
            var absoluteErr = "Check if the file name (%s) and absolute path (%s) is correct".formatted(fileName,
                                                                                                        fileAbsolutePath);
            var nameErr = "File Name (%s) is not found in the path (%s)".formatted(fileName, fileAbsolutePath);
            var err = "%s. %s".formatted(absoluteErr, nameErr);
            throw new IllegalArgumentException(err);
        }

        return fileAbsolutePath.substring(0, fileAbsolutePath.length() - fileName.length());
    }
}
