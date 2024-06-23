package ua.renamer.app.core.service.command.impl.preparation;

import lombok.*;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.FileInformationCommand;

/**
 * Command for changing the file extension within {@link FileInformation} objects.
 * This class extends {@link FileInformationCommand} and provides functionality to
 * change the extension of the file names.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExtensionChangePrepareInformationCommand extends FileInformationCommand {

    /**
     * The new extension to be applied to the file names.
     */
    @NonNull
    @Builder.Default
    private final String newExtension = "";

    /**
     * Processes a {@link FileInformation} item by changing its file extension to the specified new extension.
     *
     * @param item the {@link FileInformation} item to be processed.
     *
     * @return the processed {@link FileInformation} item with the new extension.
     */
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
