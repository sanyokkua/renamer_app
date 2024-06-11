package ua.renamer.app.core.service.file.impl;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.core.service.file.BasicFileAttributesExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides various operations for handling file attributes and information.
 */
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class FilesOperations {

    private final BasicFileAttributesExtractor basicFileAttributesExtractor;

    /**
     * Gets the absolute path of a file.
     *
     * @param file The file whose absolute path is to be retrieved.
     *
     * @return The absolute path of the file.
     */
    public String getFileAbsolutePath(File file) {
        validateFileInstance(file);

        return file.getAbsolutePath();
    }

    /**
     * Validates that the file instance is not null and exists.
     *
     * @param file The file to be validated.
     *
     * @throws IllegalArgumentException If the file does not exist.
     */
    public void validateFileInstance(File file) {
        Objects.requireNonNull(file);
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }
    }

    /**
     * Gets the file name without its extension.
     *
     * @param file The file whose name is to be retrieved.
     *
     * @return The name of the file without its extension.
     */
    public String getFileNameWithoutExtension(File file) {
        validateFileInstance(file);

        if (file.isDirectory()) {
            return file.getName();
        }

        final var fileNameAndExtension = file.getName();
        return removeFileExtension(fileNameAndExtension);
    }

    /**
     * Removes the extension from a file name.
     *
     * @param fileName The name of the file.
     *
     * @return The name of the file without its extension.
     */
    private String removeFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return fileName;
        }

        return fileName.substring(0, dotIndex);
    }

    /**
     * Gets the file extension.
     *
     * @param file The file whose extension is to be retrieved.
     *
     * @return The extension of the file.
     */
    public String getFileExtension(File file) {
        validateFileInstance(file);

        if (file.isDirectory()) {
            return "";
        }

        var fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return "";
        }

        var extension = fileName.substring(dotIndex);
        return extension.toLowerCase();
    }

    /**
     * Checks if the given file is a regular file.
     *
     * @param file The file to be checked.
     *
     * @return true if the file is a regular file, false if it is a directory.
     */
    public boolean isFile(File file) {
        validateFileInstance(file);

        return file.isFile();
    }

    /**
     * Gets the creation time of the file.
     *
     * @param file The file whose creation time is to be retrieved.
     *
     * @return An Optional containing the creation time if available, otherwise empty.
     */
    public Optional<LocalDateTime> getFileCreationTime(File file) {
        validateFileInstance(file);

        var attr = getFileAttributes(file);
        if (attr.isEmpty()) {
            return Optional.empty();
        }

        var time = attr.get().creationTime();
        return Optional.of(LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault()));
    }

    /**
     * Gets the file attributes.
     *
     * @param file The file whose attributes are to be retrieved.
     *
     * @return An Optional containing the file attributes if available, otherwise empty.
     */
    private Optional<BasicFileAttributes> getFileAttributes(File file) {
        validateFileInstance(file);
        try {
            var path = file.toPath();
            var fileAttributes = basicFileAttributesExtractor.getAttributes(path, BasicFileAttributes.class);
            return Optional.ofNullable(fileAttributes);
        } catch (IOException ex) {
            return Optional.empty();
        }
    }

    /**
     * Gets the modification time of the file.
     *
     * @param file The file whose modification time is to be retrieved.
     *
     * @return An Optional containing the modification time if available, otherwise empty.
     */
    public Optional<LocalDateTime> getFileModificationTime(File file) {
        validateFileInstance(file);
        var attr = getFileAttributes(file);
        if (attr.isEmpty()) {
            return Optional.empty();
        }

        var time = attr.get().lastModifiedTime();
        return Optional.of(LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault()));
    }

    /**
     * Gets the size of the file.
     *
     * @param file The file whose size is to be retrieved.
     *
     * @return The size of the file in bytes.
     */
    public long getFileSize(File file) {
        validateFileInstance(file);

        return file.length();
    }

    /**
     * Retrieves the parent folders of a file or directory given its path.
     *
     * @param filePath The path of the file or directory.
     *
     * @return A list containing the names of the parent folders.
     */
    public List<String> getParentFolders(String filePath) {
        if (Objects.isNull(filePath) || filePath.trim().isEmpty()) {
            return List.of();
        }

        // Normalize the file path by replacing backslashes with forward slashes
        // and removing redundant slashes and trailing slash
        filePath = filePath.replace("\\", "/").replaceAll("//+", "/").replaceAll("/$", "");

        String[] splitPathItems = filePath.split("/");

        if (splitPathItems.length == 0 || splitPathItems.length == 1) {
            // If path is empty or file in the root
            return List.of();
        }

        // Exclude the root element (drive letter for Windows or empty string for Unix)
        // and exclude last item (filename or directory)
        return Arrays.asList(splitPathItems).subList(1, splitPathItems.length - 1);
    }

}
