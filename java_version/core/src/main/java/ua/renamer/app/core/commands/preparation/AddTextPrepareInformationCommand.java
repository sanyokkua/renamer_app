package ua.renamer.app.core.commands.preparation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.FileInformationCommand;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.model.FileInformation;

import java.util.Objects;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddTextPrepareInformationCommand extends FileInformationCommand {

    @Builder.Default
    private ItemPosition position = ItemPosition.BEGIN;
    @Builder.Default
    private String text = "";

    @Override
    public FileInformation processItem(FileInformation item) {
        if (Objects.requireNonNull(position) == ItemPosition.BEGIN) {
            item.setNewName(text + item.getFileName());
        } else if (position == ItemPosition.END) {
            item.setNewName(item.getFileName() + text);
        }

        return item;
    }

}
