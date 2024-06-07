package ua.renamer.app.core.commands.preparation;

import lombok.*;
import ua.renamer.app.core.abstracts.FileInformationCommand;
import ua.renamer.app.core.enums.ImageDimensionOptions;
import ua.renamer.app.core.enums.ItemPositionWithReplacement;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;

import java.util.Objects;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageDimensionsPrepareInformationCommand extends FileInformationCommand {

    @NonNull
    @Builder.Default
    private final ItemPositionWithReplacement position = ItemPositionWithReplacement.BEGIN;
    @NonNull
    @Builder.Default
    private final ImageDimensionOptions leftSide = ImageDimensionOptions.DO_NOT_USE;
    @NonNull
    @Builder.Default
    private final ImageDimensionOptions rightSide = ImageDimensionOptions.DO_NOT_USE;
    @NonNull
    @Builder.Default
    private final String dimensionSeparator = "x";
    @NonNull
    @Builder.Default
    private final String nameSeparator = "";

    @Override
    public FileInformation processItem(FileInformation item) {
        if (ImageDimensionOptions.DO_NOT_USE.equals(leftSide) && ImageDimensionOptions.DO_NOT_USE.equals(rightSide)) {
            return item;
        }

        var width = item.getMetadata().flatMap(FileInformationMetadata::getImgVidWidth).orElse(null);
        var height = item.getMetadata().flatMap(FileInformationMetadata::getImgVidHeight).orElse(null);

        if (Objects.isNull(width) && Objects.isNull(height)) {
            return item;
        }

        var leftSideValue = getDimensionValue(leftSide, width, height);
        var rightSideValue = getDimensionValue(rightSide, width, height);
        var dimensionsText = createDimensionsText(leftSideValue, rightSideValue);

        var newName = createNewName(item.getFileName(), dimensionsText);
        item.setNewName(newName);

        return item;
    }

    private String getDimensionValue(ImageDimensionOptions side, Integer width, Integer height) {
        return switch (side) {
            case WIDTH -> Objects.nonNull(width) ? width.toString() : "";
            case HEIGHT -> Objects.nonNull(height) ? height.toString() : "";
            default -> "";
        };
    }

    private String createDimensionsText(String leftSideValue, String rightSideValue) {
        if (leftSideValue.isEmpty()) {
            return rightSideValue;
        }
        if (rightSideValue.isEmpty()) {
            return leftSideValue;
        }
        return leftSideValue + dimensionSeparator + rightSideValue;
    }

    private String createNewName(String originalName, String dimensionsText) {
        return switch (position) {
            case REPLACE -> dimensionsText;
            case BEGIN -> dimensionsText + nameSeparator + originalName;
            case END -> originalName + nameSeparator + dimensionsText;
        };
    }

}
