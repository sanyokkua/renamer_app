package ua.renamer.app.core.commands.preparation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.FileInformationCommand;
import ua.renamer.app.core.enums.TextCaseOptions;
import ua.renamer.app.core.model.FileInformation;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeCasePreparePrepareInformationCommand extends FileInformationCommand {

    @Builder.Default
    private TextCaseOptions textCase = TextCaseOptions.CAMEL_CASE;
    @Builder.Default
    private boolean capitalize = false;

    @Override
    public FileInformation processItem(FileInformation item) {
        return null;
    }

}
