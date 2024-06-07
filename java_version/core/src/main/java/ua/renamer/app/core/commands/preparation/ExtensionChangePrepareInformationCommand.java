package ua.renamer.app.core.commands.preparation;

import lombok.*;
import ua.renamer.app.core.abstracts.FileInformationCommand;
import ua.renamer.app.core.model.FileInformation;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExtensionChangePrepareInformationCommand extends FileInformationCommand {

    @NonNull
    @Builder.Default
    private final String newExtension = "";

    @Override
    public FileInformation processItem(FileInformation item) {
        if (item.isFile()) {
            var newExt = newExtension.strip();
            if (!newExt.isEmpty() && !newExt.startsWith(".")) {
                newExt = "." + newExt;
            }

            item.setNewExtension(newExt);
        }

        return item;
    }

}
