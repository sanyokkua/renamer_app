package ua.renamer.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {

    public static String getFileExtension(File file) {
        if (Objects.isNull(file) || !file.exists() || file.isDirectory()) {
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

}
