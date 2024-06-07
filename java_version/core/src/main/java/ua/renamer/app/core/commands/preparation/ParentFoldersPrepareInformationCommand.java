package ua.renamer.app.core.commands.preparation;

import lombok.*;
import ua.renamer.app.core.abstracts.FileInformationCommand;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.model.FileInformation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static ua.renamer.app.core.utils.FileUtils.getParentFolders;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ParentFoldersPrepareInformationCommand extends FileInformationCommand {

    @NonNull
    @Builder.Default
    private final ItemPosition position = ItemPosition.BEGIN;
    @NonNull
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
        var parents = getParentFolders(filePathStr);
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
