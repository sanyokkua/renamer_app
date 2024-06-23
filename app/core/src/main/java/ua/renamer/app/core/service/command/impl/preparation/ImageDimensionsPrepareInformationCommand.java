package ua.renamer.app.core.service.command.impl.preparation;

import lombok.*;
import ua.renamer.app.core.enums.ImageDimensionOptions;
import ua.renamer.app.core.enums.ItemPositionWithReplacement;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.command.FileInformationCommand;

import java.util.Objects;

/**
 * Command for adding image dimensions to the file names within {@link FileInformation} objects.
 * This class extends {@link FileInformationCommand} and provides functionality to add image
 * dimensions (width and height) to the file names based on various options.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageDimensionsPrepareInformationCommand extends FileInformationCommand {

    /**
     * The position where the dimensions should be added in the file name.
     */
    @NonNull
    @Builder.Default
    private final ItemPositionWithReplacement position = ItemPositionWithReplacement.BEGIN;
    /**
     * The option for what to display on the left side of the dimensions.
     */
    @NonNull
    @Builder.Default
    private final ImageDimensionOptions leftSide = ImageDimensionOptions.DO_NOT_USE;
    /**
     * The option for what to display on the right side of the dimensions.
     */
    @NonNull
    @Builder.Default
    private final ImageDimensionOptions rightSide = ImageDimensionOptions.DO_NOT_USE;
    /**
     * The separator between the width and height dimensions.
     */
    @NonNull
    @Builder.Default
    private final String dimensionSeparator = "x";
    /**
     * The separator between the dimensions and the original file name.
     */
    @NonNull
    @Builder.Default
    private final String nameSeparator = "";

    /**
     * Processes a {@link FileInformation} item by adding its image dimensions to the file name
     * based on the specified options and positions.
     *
     * @param item the {@link FileInformation} item to be processed.
     *
     * @return the processed {@link FileInformation} item with the new file name.
     */
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

    /**
     * Retrieves the dimension value based on the specified side option.
     *
     * @param side   the {@link ImageDimensionOptions} specifying which dimension to use.
     * @param width  the width of the image.
     * @param height the height of the image.
     *
     * @return the dimension value as a string.
     */
    private String getDimensionValue(ImageDimensionOptions side, Integer width, Integer height) {
        return switch (side) {
            case WIDTH -> Objects.nonNull(width) ? width.toString() : "";
            case HEIGHT -> Objects.nonNull(height) ? height.toString() : "";
            default -> "";
        };
    }

    /**
     * Creates the dimensions text based on the specified left and right side values.
     *
     * @param leftSideValue  the value for the left side of the dimensions.
     * @param rightSideValue the value for the right side of the dimensions.
     *
     * @return the combined dimensions text.
     */
    private String createDimensionsText(String leftSideValue, String rightSideValue) {
        if (leftSideValue.isEmpty()) {
            return rightSideValue;
        }
        if (rightSideValue.isEmpty()) {
            return leftSideValue;
        }
        return leftSideValue + dimensionSeparator + rightSideValue;
    }

    /**
     * Creates the new file name by adding the dimensions text to the original file name based on the specified position.
     *
     * @param originalName   the original file name.
     * @param dimensionsText the dimensions text to be added.
     *
     * @return the new file name with dimensions.
     */
    private String createNewName(String originalName, String dimensionsText) {
        if (dimensionsText.isBlank()) {
            return originalName;
        }
        return switch (position) {
            case REPLACE -> dimensionsText;
            case BEGIN -> dimensionsText + nameSeparator + originalName;
            case END -> originalName + nameSeparator + dimensionsText;
        };
    }

}
