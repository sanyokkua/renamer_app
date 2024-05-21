package ua.renamer.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileInformationUtils {

    public static FileInformation createFileInformationFromFile(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }

        try {
            final var filePath = file.toPath();
            final var fileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
            final var fileNameAndExtension = file.getName();

            final var fileAbsolutePath = file.getAbsolutePath();
            final var isFile = file.isFile();
            final var fileNameWithoutExtension = removeFileExtension(fileNameAndExtension);
            final var fileExtension = getFileExtension(fileNameAndExtension);
            final var fsCreationDate = getTimeValue(fileAttributes.creationTime());
            final var fsModificationDate = getTimeValue(fileAttributes.lastModifiedTime());
            final var fileSize = file.length();
            final var metadata = extractFileMetadata(file);

            return FileInformation.builder()
                                  .originalFile(file)
                                  .fileAbsolutePath(fileAbsolutePath)
                                  .isFile(isFile)
                                  .fileName(fileNameWithoutExtension)
                                  .newName(fileNameWithoutExtension) // Initial value is current file name
                                  .fileExtension(fileExtension)
                                  .newExtension(fileExtension) // Initial value is current file extension
                                  .fileSize(fileSize)
                                  .fsCreationDate(fsCreationDate)
                                  .fsModificationDate(fsModificationDate)
                                  .metadata(metadata)
                                  .build();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to read file information", ex);
        }
    }

    private static FileInformationMetadata extractFileMetadata(File file) {
        if (!file.exists() || file.isDirectory()) {
            return null;
        }
        // TODO: Implement logic to extract metadata from the file
        return FileInformationMetadata.builder().build();
    }

    private static Long getTimeValue(FileTime fileTime) {
        var result = fileTime.toMillis();
        if (result < 0 || result == 0) {
            return null;
        }
        return result;
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return "";
        }
        return fileName.substring(dotIndex);
    }

    private static String removeFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

}
