package ua.renamer.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {

    public static void validateFileInstance(File file) {
        Objects.requireNonNull(file);
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }
    }

    public static String getFileAbsolutePath(File file) {
        validateFileInstance(file);

        return file.getAbsolutePath();
    }

    public static String getFileNameWithoutExtension(File file) {
        validateFileInstance(file);

        if (file.isDirectory()) {
            return file.getName();
        }

        final var fileNameAndExtension = file.getName();
        return removeFileExtension(fileNameAndExtension);
    }

    public static String getFileExtension(File file) {
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

    public static boolean isFile(File file) {
        validateFileInstance(file);

        return file.isFile();
    }

    private static Optional<BasicFileAttributes> getFileAttributes(File file) {
        validateFileInstance(file);
        try {
            var path = file.toPath();
            BasicFileAttributes fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
            return Optional.ofNullable(fileAttributes);
        } catch (IOException ex) {
            return Optional.empty();
        }
    }

    public static Optional<LocalDateTime> getFileCreationTime(File file) {
        validateFileInstance(file);

        var attr = getFileAttributes(file);
        if (attr.isEmpty()) {
            return Optional.empty();
        }

        var time = attr.get().creationTime();
        return Optional.of(LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault()));
    }

    public static Optional<LocalDateTime> getFileModificationTime(File file) {
        validateFileInstance(file);
        var attr = getFileAttributes(file);
        if (attr.isEmpty()) {
            return Optional.empty();
        }

        var time = attr.get().lastModifiedTime();
        return Optional.of(LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault()));
    }

    public static long getFileSize(File file) {
        validateFileInstance(file);

        return file.length();
    }

    /**
     * Retrieves the parent folders of a file or directory given its path.
     *
     * @param filePath The path of the file or directory.
     * @return A list containing the names of the parent folders.
     */
    public static List<String> getParentFolders(String filePath) {
        if (Objects.isNull(filePath) || filePath.trim().isEmpty()) {
            return List.of();
        }

        // Normalize the file path by replacing backslashes with forward slashes
        // and removing redundant slashes and trailing slash
        filePath = filePath.replace("\\", "/")
                           .replaceAll("//+", "/")
                           .replaceAll("/$", "");

        String[] splitPathItems = filePath.split("/");

        // Exclude the root element (drive letter for Windows or empty string for Unix)
        // and exclude last item (filename or directory)
        return Arrays.asList(splitPathItems).subList(1, splitPathItems.length - 1);
    }

    private static String removeFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return fileName;
        }

        return fileName.substring(0, dotIndex);
    }

}
