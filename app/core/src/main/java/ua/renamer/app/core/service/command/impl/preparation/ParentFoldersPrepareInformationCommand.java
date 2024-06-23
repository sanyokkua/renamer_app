package ua.renamer.app.core.service.command.impl.preparation;

import lombok.*;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.FileInformationCommand;
import ua.renamer.app.core.service.file.impl.FilesOperations;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Command for adding parent folder names to the file names within {@link FileInformation} objects.
 * This class extends {@link FileInformationCommand} and provides functionality to prepend or append
 * parent folder names to the file names based on the specified options.
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ParentFoldersPrepareInformationCommand extends FileInformationCommand {

    /**
     * The file operations service used to retrieve parent folders.
     */
    private final FilesOperations filesOperations;

    /**
     * The position where the parent folders should be added in the file name.
     */
    @NonNull
    @Builder.Default
    private final ItemPosition position = ItemPosition.BEGIN;
    /**
     * The number of parent folders to include in the file name.
     */
    @Builder.Default
    private final int numberOfParents = 1;
    /**
     * The separator between the parent folders and the file name.
     */
    @NonNull
    @Builder.Default
    private final String separator = "";

    /**
     * Processes a {@link FileInformation} item by adding its parent folder names to the file name
     * based on the specified options and positions.
     *
     * @param item the {@link FileInformation} item to be processed.
     *
     * @return the processed {@link FileInformation} item with the new file name.
     */
    @Override
    public FileInformation processItem(FileInformation item) {
        if (this.numberOfParents <= 0) {
            return item;
        }

        var filePathStr = item.getFileAbsolutePath();
        var parents = filesOperations.getParentFolders(filePathStr);
        Collections.reverse(parents);

        List<String> parentsList = new LinkedList<>();
        int numOfParents = 0;

        while (numOfParents < this.numberOfParents) {
            if (parents.size() > numOfParents) {
                parentsList.addFirst(parents.get(numOfParents++));
            } else {
                break;
            }
        }

        if (parentsList.isEmpty()) {
            return item;
        }

        var parentsStr = String.join(this.separator, parentsList);
        var fileName = item.getFileName();

        String nextName = switch (position) {
            case BEGIN -> parentsStr + separator + fileName;
            case END -> fileName + this.separator + parentsStr;
        };

        item.setNewName(nextName);
        return item;
    }

}
