package ua.renamer.app.core.commands.preparation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.AppFileCommand;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.model.AppFile;

import java.util.Objects;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddTextPrepareCommand extends AppFileCommand {

    @Builder.Default
    private ItemPosition position = ItemPosition.BEGIN;
    @Builder.Default
    private String text = "";

    @Override
    public AppFile processItem(AppFile item) {
        if (Objects.requireNonNull(position) == ItemPosition.BEGIN) {
            item.setNextName(text + item.getFileName());
        } else if (position == ItemPosition.END) {
            item.setNextName(item.getFileName() + text);
        }

        return item;
    }
}
