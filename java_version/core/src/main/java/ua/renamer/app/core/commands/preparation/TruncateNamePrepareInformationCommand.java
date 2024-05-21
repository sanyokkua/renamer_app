package ua.renamer.app.core.commands.preparation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.FileInformationCommand;
import ua.renamer.app.core.enums.TruncateOptions;
import ua.renamer.app.core.model.FileInformation;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruncateNamePrepareInformationCommand extends FileInformationCommand {

    @Builder.Default
    private int numberOfSymbols = 0;
    @Builder.Default
    private TruncateOptions truncateOptions = TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN;

    @Override
    public FileInformation processItem(FileInformation item) {
        return null;
    }

}
