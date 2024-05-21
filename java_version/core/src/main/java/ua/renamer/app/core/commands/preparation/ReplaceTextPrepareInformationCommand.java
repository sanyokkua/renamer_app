package ua.renamer.app.core.commands.preparation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.FileInformationCommand;
import ua.renamer.app.core.enums.ItemPositionExtended;
import ua.renamer.app.core.model.FileInformation;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplaceTextPrepareInformationCommand extends FileInformationCommand {

    @Builder.Default
    private ItemPositionExtended position = ItemPositionExtended.BEGIN;
    @Builder.Default
    private String textToReplace = "";
    @Builder.Default
    private String newValueToAdd = "";

    @Override
    public FileInformation processItem(FileInformation item) {
        return null;
    }

}
