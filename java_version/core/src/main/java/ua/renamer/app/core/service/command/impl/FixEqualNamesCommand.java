package ua.renamer.app.core.service.command.impl;

import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.FileInformationCommand;

import java.util.*;

/**
 * Command to fix files with equal names by appending a suffix.
 */
@Slf4j
public class FixEqualNamesCommand extends FileInformationCommand {

    /**
     * Processes the input list of FileInformation objects to ensure no files have the same name.
     *
     * @param input List of FileInformation objects to be processed.
     *
     * @return List of processed FileInformation objects with unique names.
     */
    @Override
    protected List<FileInformation> preprocessInput(List<FileInformation> input) {
        var nameGroups = new HashMap<String, List<FileInformation>>();
        var uniqueNames = new HashSet<String>();

        for (FileInformation file : input) {
            String key = getFileNewFullName(file);
            nameGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(file);
            uniqueNames.add(getFileNewFullName(file));
        }
        log.debug("nameGroups: {}", nameGroups);
        log.debug("uniqueNames: {}", uniqueNames);

        for (List<FileInformation> filesWithSameName : nameGroups.values()) {
            int numFiles = filesWithSameName.size();
            if (numFiles < 2) {
                continue;
            }

            int digits = String.valueOf(numFiles).length();
            for (FileInformation file : filesWithSameName) {
                if (isFileRenamed(file)) {
                    int counter = 1;
                    String newName;
                    String newFullName;

                    do {
                        var suffix = " (" + String.format("%0" + digits + "d", counter++) + ")";
                        newName = file.getNewName() + suffix;
                        newFullName = getFileNewFullName(newName, file.getNewExtension());
                        log.debug("New name: {}", newFullName);
                    } while (uniqueNames.contains(newFullName));

                    file.setNewName(newName);
                    uniqueNames.add(newFullName);
                }
            }
        }
        log.debug("nameGroups: {}", nameGroups);
        log.debug("uniqueNames: {}", uniqueNames);
        return input;
    }

    /**
     * Constructs the full name of the file including its extension.
     *
     * @param fileInfo The FileInformation object.
     *
     * @return The full name of the file.
     */
    private static String getFileNewFullName(FileInformation fileInfo) {
        log.debug("getFileNewFullName({})", fileInfo);
        String formatted = getFileNewFullName(fileInfo.getNewName(), fileInfo.getNewExtension());
        log.debug("getFileNewFullName({}) formatted name:", formatted);
        return formatted;
    }

    /**
     * Checks if the file has been renamed.
     *
     * @param fileInfo The FileInformation object.
     *
     * @return true if the file has been renamed, false otherwise.
     */
    private static boolean isFileRenamed(FileInformation fileInfo) {
        log.debug("isFileRenamed({})", fileInfo);
        boolean renamed = !(fileInfo.getFileName().equals(fileInfo.getNewName()) && fileInfo.getFileExtension()
                                                                                            .equals(fileInfo.getNewExtension()));
        log.debug("isFileRenamed() is renamed={}", renamed);
        return renamed;
    }

    /**
     * Constructs the full name of the file from its name and extension.
     *
     * @param name The name of the file.
     * @param ext  The extension of the file.
     *
     * @return The full name of the file.
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

}
