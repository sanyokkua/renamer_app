package ua.renamer.app.core.service.command.impl.preparation;

import lombok.*;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.FileInformationCommand;
import ua.renamer.app.core.util.StringUtils;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RemoveTextPrepareInformationCommand extends FileInformationCommand {

    @NonNull
    @Builder.Default
    private final ItemPosition position = ItemPosition.BEGIN;
    @NonNull
    @Builder.Default
    private final String text = "";

    @Override
    public FileInformation processItem(FileInformation item) {
        if (StringUtils.isEmpty(text)) {
            return item;
        }

        var fileName = item.getFileName();
        var newName = fileName;

        if (ItemPosition.BEGIN.equals(position) && fileName.startsWith(this.text)) {
            newName = fileName.substring(this.text.length());
        } else if (ItemPosition.END.equals(position) && fileName.endsWith(this.text)) {
            newName = fileName.substring(0, fileName.length() - this.text.length());
        }

        item.setNewName(newName);

        return item;
    }

}
