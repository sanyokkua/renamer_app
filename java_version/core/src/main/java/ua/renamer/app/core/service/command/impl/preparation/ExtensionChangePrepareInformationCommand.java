package ua.renamer.app.core.service.command.impl.preparation;

import lombok.*;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.FileInformationCommand;

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
