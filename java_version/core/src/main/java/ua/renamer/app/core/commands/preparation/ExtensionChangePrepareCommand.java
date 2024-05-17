package ua.renamer.app.core.commands.preparation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.AppFileCommand;
import ua.renamer.app.core.model.AppFile;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtensionChangePrepareCommand extends AppFileCommand {
    @Builder.Default
    private String newExtension = "";

    @Override
    public AppFile processItem(AppFile item) {
        return null;
    }
}
