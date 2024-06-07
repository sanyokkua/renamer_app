package ua.renamer.app.core.commands.preparation;

import lombok.*;
import ua.renamer.app.core.abstracts.FileInformationCommand;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.model.FileInformation;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddTextPrepareInformationCommand extends FileInformationCommand {

    @NonNull
    @Builder.Default
    private final ItemPosition position = ItemPosition.BEGIN;
    @NonNull
    @Builder.Default
    private final String text = "";

    @Override
    public FileInformation processItem(FileInformation item) {
        if (ItemPosition.BEGIN.equals(position)) {
            item.setNewName(text + item.getFileName());
        } else {
            item.setNewName(item.getFileName() + text);
        }

        return item;
    }

}
