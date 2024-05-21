package ua.renamer.app.core.commands.preparation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.FileInformationCommand;
import ua.renamer.app.core.model.FileInformation;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtensionChangePrepareInformationCommand extends FileInformationCommand {

    @Builder.Default
    private String newExtension = "";

    @Override
    public FileInformation processItem(FileInformation item) {
        return null;
    }

}
