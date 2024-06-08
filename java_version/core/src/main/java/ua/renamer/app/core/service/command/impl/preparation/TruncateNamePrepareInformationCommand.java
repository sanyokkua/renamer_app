package ua.renamer.app.core.service.command.impl.preparation;

import lombok.*;
import ua.renamer.app.core.enums.TruncateOptions;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.FileInformationCommand;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TruncateNamePrepareInformationCommand extends FileInformationCommand {

    @Builder.Default
    private final int numberOfSymbols = 0;
    @NonNull
    @Builder.Default
    private final TruncateOptions truncateOptions = TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN;

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

        if (TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN.equals(truncateOptions)) {
            item.setNewName(item.getNewName().substring(numberOfSymbols));
        } else if (TruncateOptions.REMOVE_SYMBOLS_FROM_END.equals(truncateOptions)) {
            item.setNewName(item.getNewName().substring(0, item.getNewName().length() - this.numberOfSymbols));
        }

        return item;
    }

}
