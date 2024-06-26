package ua.renamer.app.core.service.command.impl.preparation;

import lombok.*;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.FileInformationCommand;
import ua.renamer.app.core.util.StringUtils;

/**
 * Command for removing specified text from the file names within {@link FileInformation} objects.
 * This class extends {@link FileInformationCommand} and provides functionality to remove text from
 * the beginning or end of the file names based on the specified options.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RemoveTextPrepareInformationCommand extends FileInformationCommand {

    /**
     * The position where the text should be removed from the file name.
     */
    @NonNull
    @Builder.Default
    private final ItemPosition position = ItemPosition.BEGIN;
    /**
     * The text to be removed from the file name.
     */
    @NonNull
    @Builder.Default
    private final String text = "";

    /**
     * Processes a {@link FileInformation} item by removing the specified text from the file name
     * based on the specified options and positions.
     *
     * @param item the {@link FileInformation} item to be processed.
     *
     * @return the processed {@link FileInformation} item with the new file name.
     */
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
