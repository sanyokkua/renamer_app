package ua.renamer.app.core.commands.preparation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.FileInformationCommand;
import ua.renamer.app.core.enums.SortSource;
import ua.renamer.app.core.model.FileInformation;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SequencePrepareInformationCommand extends FileInformationCommand {

    @Builder.Default
    private SortSource sortSource = SortSource.FILE_NAME;
    @Builder.Default
    private int startNumber = 0;
    @Builder.Default
    private int stepValue = 1;
    @Builder.Default
    private int padding = 0;

    @Override
    public FileInformation processItem(FileInformation item) {
        return null;
    }

}
