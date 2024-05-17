package ua.renamer.app.core.commands.preparation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.AppFileCommand;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.model.AppFile;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentFoldersPrepareCommand extends AppFileCommand {
    @Builder.Default
    private ItemPosition position = ItemPosition.BEGIN;
    @Builder.Default
    private int numberOfParents = 1;
    @Builder.Default
    private String separator = "";

    @Override
    public AppFile processItem(AppFile item) {
        return null;
    }
}
