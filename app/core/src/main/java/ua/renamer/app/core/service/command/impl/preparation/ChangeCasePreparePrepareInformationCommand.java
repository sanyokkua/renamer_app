package ua.renamer.app.core.service.command.impl.preparation;

import lombok.*;
import ua.renamer.app.core.enums.TextCaseOptions;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.FileInformationCommand;
import ua.renamer.app.core.util.StringUtils;

/**
 * Command for changing the case of the file names within {@link FileInformation} objects.
 * This class extends {@link FileInformationCommand} and provides functionality to
 * change the case of the file names and optionally capitalize them.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChangeCasePreparePrepareInformationCommand extends FileInformationCommand {

    /**
     * The case option to apply to the file names.
     */
    @NonNull
    @Builder.Default
    private final TextCaseOptions textCase = TextCaseOptions.CAMEL_CASE;
    /**
     * Flag indicating whether to capitalize the first letter of the file names.
     */
    @Builder.Default
    private final boolean capitalize = false;

    /**
     * Processes a {@link FileInformation} item by changing the case of its file name
     * based on the specified {@link TextCaseOptions} and optionally capitalizing it.
     *
     * @param item the {@link FileInformation} item to be processed.
     *
     * @return the processed {@link FileInformation} item with the new file name.
     */
    @Override
    public FileInformation processItem(FileInformation item) {
        if (item.getFileName().isBlank()) {
            return item;
        }

        var currentName = item.getFileName();
        var newName = StringUtils.toProvidedCase(currentName, textCase);

        if (capitalize) {
            var firstLetter = newName.substring(0, 1).toUpperCase();
            var restOfLetters = newName.substring(1);
            newName = firstLetter + restOfLetters;
        }

        item.setNewName(newName);
        return item;
    }

}
