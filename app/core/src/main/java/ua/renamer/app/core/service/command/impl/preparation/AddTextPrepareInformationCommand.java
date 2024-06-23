package ua.renamer.app.core.service.command.impl.preparation;

import lombok.*;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.FileInformationCommand;

/**
 * Command for adding text to the file names within {@link FileInformation} objects.
 * This class extends {@link FileInformationCommand} and provides functionality to
 * add text either at the beginning or at the end of the file names.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddTextPrepareInformationCommand extends FileInformationCommand {

    /**
     * The position where the text should be added, either at the beginning or the end of the file name.
     */
    @NonNull
    @Builder.Default
    private final ItemPosition position = ItemPosition.BEGIN;
    /**
     * The text to be added to the file names.
     */
    @NonNull
    @Builder.Default
    private final String text = "";

    /**
     * Processes a {@link FileInformation} item by adding the specified text
     * to its file name based on the specified position.
     *
     * @param item the {@link FileInformation} item to be processed.
     *
     * @return the processed {@link FileInformation} item with the new file name.
     */
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
