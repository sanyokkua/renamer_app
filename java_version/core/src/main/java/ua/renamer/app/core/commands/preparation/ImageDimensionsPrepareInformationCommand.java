package ua.renamer.app.core.commands.preparation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.FileInformationCommand;
import ua.renamer.app.core.enums.ImageDimensionOptions;
import ua.renamer.app.core.enums.ItemPositionWithReplacement;
import ua.renamer.app.core.model.FileInformation;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDimensionsPrepareInformationCommand extends FileInformationCommand {

    @Builder.Default
    private ItemPositionWithReplacement position = ItemPositionWithReplacement.BEGIN;
    @Builder.Default
    private ImageDimensionOptions leftSide = ImageDimensionOptions.DO_NOT_USE;
    @Builder.Default
    private ImageDimensionOptions rightSide = ImageDimensionOptions.DO_NOT_USE;
    @Builder.Default
    private String dimensionSeparator = "x";
    @Builder.Default
    private String nameSeparator = "";

    @Override
    public FileInformation processItem(FileInformation item) {
        return null;
    }

}
