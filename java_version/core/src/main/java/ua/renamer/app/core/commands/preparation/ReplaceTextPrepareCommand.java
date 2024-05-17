package ua.renamer.app.core.commands.preparation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.AppFileCommand;
import ua.renamer.app.core.enums.ItemPositionExtended;
import ua.renamer.app.core.model.AppFile;


@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplaceTextPrepareCommand extends AppFileCommand {
    @Builder.Default
    private ItemPositionExtended position = ItemPositionExtended.BEGIN;
    @Builder.Default
    private String textToReplace = "";
    @Builder.Default
    private String newValueextends = "";

    @Override
    public AppFile processItem(AppFile item) {
        return null;
    }
}
