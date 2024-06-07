package ua.renamer.app.core.commands.preparation;

import lombok.*;
import ua.renamer.app.core.abstracts.FileInformationCommand;
import ua.renamer.app.core.enums.ItemPositionExtended;
import ua.renamer.app.core.model.FileInformation;

import java.util.Objects;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReplaceTextPrepareInformationCommand extends FileInformationCommand {

    @NonNull
    @Builder.Default
    private final ItemPositionExtended position = ItemPositionExtended.BEGIN;
    @NonNull
    @Builder.Default
    private final String textToReplace = "";
    @NonNull
    @Builder.Default
    private final String newValueToAdd = "";

    @Override
    public FileInformation processItem(FileInformation item) {
        if (Objects.isNull(textToReplace) || textToReplace.isEmpty()) {
            return item;
        }

        final var fileName = item.getFileName();
        var nextName = fileName;

        if (ItemPositionExtended.BEGIN.equals(position)) {
            nextName = fileName.replaceFirst(textToReplace, newValueToAdd);
        } else if (ItemPositionExtended.END.equals(position)) {
            StringBuilder reversedName = new StringBuilder(fileName).reverse();
            StringBuilder reversedValueToReplace = new StringBuilder(textToReplace).reverse();

            StringBuilder reversedNewValue = new StringBuilder(newValueToAdd).reverse();
            String reversedResult = reversedName.toString()
                                                .replaceFirst(reversedValueToReplace.toString(),
                                                              reversedNewValue.toString()
                                                             );
            nextName = reversedResult.isEmpty() ?
                    reversedResult :
                    new StringBuilder(reversedResult).reverse().toString();
        } else {
            nextName = nextName.replaceAll(textToReplace, newValueToAdd);
        }

        item.setNewName(nextName);
        return item;
    }

}
