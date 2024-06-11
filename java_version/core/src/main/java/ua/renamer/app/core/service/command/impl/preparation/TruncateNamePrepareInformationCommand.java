package ua.renamer.app.core.service.command.impl.preparation;

import lombok.*;
import ua.renamer.app.core.enums.TruncateOptions;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.FileInformationCommand;

/**
 * Command for truncating file names within {@link FileInformation} objects.
 * This class extends {@link FileInformationCommand} and provides functionality to truncate
 * file names based on specified options such as the number of symbols and truncate options.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TruncateNamePrepareInformationCommand extends FileInformationCommand {

    /**
     * The number of symbols to truncate in the file name.
     */
    @Builder.Default
    private final int numberOfSymbols = 0;
    /**
     * The truncate options to apply for truncating the file name.
     */
    @NonNull
    @Builder.Default
    private final TruncateOptions truncateOptions = TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN;

    /**
     * Processes a {@link FileInformation} item by truncating the file name based on the specified options.
     *
     * @param item the {@link FileInformation} item to be processed.
     *
     * @return the processed {@link FileInformation} item with the new file name.
     */
    @Override
    public FileInformation processItem(FileInformation item) {
        if (item.getFileName().isEmpty()) {
            return item;
        }

        if (TruncateOptions.TRUNCATE_EMPTY_SYMBOLS.equals(truncateOptions)) {
            item.setNewName(item.getNewName().trim());
            return item;
        }

        if (numberOfSymbols >= item.getNewName().length()) {
            item.setNewName("");
            return item;
        }

        if (numberOfSymbols < 1) {
            return item;
        }

        if (TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN.equals(truncateOptions)) {
            item.setNewName(item.getNewName().substring(numberOfSymbols));
        } else if (TruncateOptions.REMOVE_SYMBOLS_FROM_END.equals(truncateOptions)) {
            item.setNewName(item.getNewName().substring(0, item.getNewName().length() - this.numberOfSymbols));
        }

        return item;
    }

}
