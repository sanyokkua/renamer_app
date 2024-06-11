package ua.renamer.app.core.service.command.impl.preparation;

import lombok.*;
import ua.renamer.app.core.enums.ItemPositionExtended;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.FileInformationCommand;

/**
 * Command for replacing specified text in the file names within {@link FileInformation} objects.
 * This class extends {@link FileInformationCommand} and provides functionality to replace text
 * at the beginning, end, or anywhere in the file names based on the specified options.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReplaceTextPrepareInformationCommand extends FileInformationCommand {

    /**
     * The position where the text should be replaced in the file name.
     */
    @NonNull
    @Builder.Default
    private final ItemPositionExtended position = ItemPositionExtended.BEGIN;
    /**
     * The text to be replaced in the file name.
     */
    @NonNull
    @Builder.Default
    private final String textToReplace = "";
    /**
     * The new value to add in place of the replaced text.
     */
    @NonNull
    @Builder.Default
    private final String newValueToAdd = "";

    /**
     * Processes a {@link FileInformation} item by replacing the specified text in the file name
     * based on the specified options and positions.
     *
     * @param item the {@link FileInformation} item to be processed.
     *
     * @return the processed {@link FileInformation} item with the new file name.
     */
    @Override
    public FileInformation processItem(FileInformation item) {
        if (textToReplace.isEmpty()) {
            return item;
        }

        final var fileName = item.getFileName();
        var nextName = fileName;

        if (ItemPositionExtended.BEGIN.equals(position)) {
            nextName = fileName.replaceFirst(textToReplace, newValueToAdd);
        } else if (ItemPositionExtended.END.equals(position)) {
            StringBuilder reversedName = new StringBuilder(fileName).reverse();
            StringBuilder reversedValueToReplace = new StringBuilder(textToReplace).reverse();

            StringBuilder reversedNewValue = new StringBuilder(newValueToAdd).reverse();
            String reversedResult = reversedName.toString()
                                                .replaceFirst(reversedValueToReplace.toString(), reversedNewValue.toString());
            nextName = reversedResult.isEmpty()
                    ? reversedResult
                    : new StringBuilder(reversedResult).reverse().toString();
        } else {
            nextName = nextName.replaceAll(textToReplace, newValueToAdd);
        }

        item.setNewName(nextName);
        return item;
    }

}
