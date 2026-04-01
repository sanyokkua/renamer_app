package ua.renamer.app.api.session;

import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.TransformationMode;

/**
 * Parameters for the {@link TransformationMode#ADD_TEXT} transformation mode.
 * Specifies the text to append or prepend to a filename.
 *
 * @param textToAdd the text to insert; must not be null
 * @param position  the position (BEGIN or END) where the text is inserted; must not be null
 */
public record AddTextParams(String textToAdd, ItemPosition position) implements ModeParameters {

    @Override
    public TransformationMode mode() {
        return TransformationMode.ADD_TEXT;
    }

    @Override
    public ValidationResult validate() {
        if (position == null) {
            return ValidationResult.fieldError("position", "must not be null");
        }
        if (textToAdd == null) {
            return ValidationResult.fieldError("textToAdd", "must not be null");
        }
        return ValidationResult.valid();
    }

    /**
     * Returns a copy of this record with the given text to add.
     *
     * @param textToAdd the replacement text; may be null (will fail validation)
     * @return a new {@link AddTextParams} with the updated field
     */
    public AddTextParams withTextToAdd(String textToAdd) {
        return new AddTextParams(textToAdd, this.position);
    }

    /**
     * Returns a copy of this record with the given position.
     *
     * @param position the replacement position; must not be null for valid configuration
     * @return a new {@link AddTextParams} with the updated field
     */
    public AddTextParams withPosition(ItemPosition position) {
        return new AddTextParams(this.textToAdd, position);
    }
}
