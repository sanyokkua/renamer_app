package ua.renamer.app.core.commands.preparation;

import lombok.*;
import ua.renamer.app.core.abstracts.FileInformationCommand;
import ua.renamer.app.core.enums.TextCaseOptions;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.utils.StringUtils;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChangeCasePreparePrepareInformationCommand extends FileInformationCommand {

    @NonNull
    @Builder.Default
    private final TextCaseOptions textCase = TextCaseOptions.CAMEL_CASE;
    @NonNull
    @Builder.Default
    private final boolean capitalize = false;

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
