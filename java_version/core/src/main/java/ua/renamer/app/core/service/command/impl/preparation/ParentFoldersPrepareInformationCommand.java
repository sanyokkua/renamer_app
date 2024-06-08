package ua.renamer.app.core.service.command.impl.preparation;

import lombok.*;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.FileInformationCommand;
import ua.renamer.app.core.service.file.impl.FilesOperations;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ParentFoldersPrepareInformationCommand extends FileInformationCommand {

    private final FilesOperations filesOperations;

    @NonNull
    @Builder.Default
    private final ItemPosition position = ItemPosition.BEGIN;
    @Builder.Default
    private final int numberOfParents = 1;
    @NonNull
    @Builder.Default
    private final String separator = "";

    @Override
    public FileInformation processItem(FileInformation item) {
        if (this.numberOfParents == 0) {
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
