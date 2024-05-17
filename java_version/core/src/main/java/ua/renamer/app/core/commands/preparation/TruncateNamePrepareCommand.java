package ua.renamer.app.core.commands.preparation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.AppFileCommand;
import ua.renamer.app.core.enums.TruncateOptions;
import ua.renamer.app.core.model.AppFile;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruncateNamePrepareCommand extends AppFileCommand {
    @Builder.Default
    private int numberOfSymbols = 0;
    @Builder.Default
    private TruncateOptions truncateOptions = TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN;


    @Override
    public AppFile processItem(AppFile item) {
        return null;
    }
}
