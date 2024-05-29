package ua.renamer.app.core.utils;

import ua.renamer.app.core.abstracts.Validator;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class NameValidator implements Validator<String> {

    private static final List<String> WINDOWS_RESTRICTED_CHARS = List.of("\\", "*", "?", "<", ">", "|");
    private static final List<String> WINDOWS_RESERVED_NAMES = Arrays.asList("CON",
                                                                             "PRN",
                                                                             "AUX",
                                                                             "NUL",
                                                                             "COM1",
                                                                             "COM2",
                                                                             "COM3",
                                                                             "COM4",
                                                                             "COM5",
                                                                             "COM6",
                                                                             "COM7",
                                                                             "COM8",
                                                                             "COM9",
                                                                             "LPT1",
                                                                             "LPT2",
                                                                             "LPT3",
                                                                             "LPT4",
                                                                             "LPT5",
                                                                             "LPT6",
                                                                             "LPT7",
                                                                             "LPT8",
                                                                             "LPT9"
                                                                            );
    private static final List<String> COMMON_RESTRICTED_SYMBOLS = Arrays.asList(":", "/");

    @Override
    public boolean isValid(String fileName) {
        if (Objects.isNull(fileName) || fileName.isEmpty()) {
            return false;
        }

        var isContainingRestrictedSymbols = COMMON_RESTRICTED_SYMBOLS.stream().anyMatch(fileName::contains);
        if (isContainingRestrictedSymbols) {
            return false;
        }

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            var isContainingRestrictedWinChars = WINDOWS_RESTRICTED_CHARS.stream().anyMatch(fileName::contains);
            if (isContainingRestrictedWinChars) {
                return false;
            }

            var isContainingRestrictedName = WINDOWS_RESERVED_NAMES.stream().anyMatch(fileName::equalsIgnoreCase);
            if (isContainingRestrictedName) {
                return false;
            }
        }

        try {
            Path path = FileSystems.getDefault().getPath(fileName); // Can be slow
            path.toRealPath();  // This will throw an exception if the file name is invalid
            return true;
        } catch (InvalidPathException | IOException e) {
            return false;
        }
    }

}
