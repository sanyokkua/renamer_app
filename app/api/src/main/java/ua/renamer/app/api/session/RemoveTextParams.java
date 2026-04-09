package ua.renamer.app.api.session;

import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.TransformationMode;

/**
 * Parameters for the {@link TransformationMode#REMOVE_TEXT} transformation mode.
 * Specifies the text to remove from the beginning or end of a filename.
 *
 * @param textToRemove the text to remove; must not be null
 * @param position     the position (BEGIN or END) from which text is removed; must not be null
 */
public record RemoveTextParams(String textToRemove, ItemPosition position) implements ModeParameters {

    @Override
    public TransformationMode mode() {
        return TransformationMode.REMOVE_TEXT;
    }

    @Override
    public ValidationResult validate() {
        if (position == null) {
            return ValidationResult.fieldError("position", "must not be null");
        }
        if (textToRemove == null) {
            return ValidationResult.fieldError("textToRemove", "must not be null");
        }
        return ValidationResult.valid();
    }

    /**
     * Returns a copy of this record with the given text to remove.
     *
     * @param textToRemove the replacement text; may be null (will fail validation)
     * @return a new {@link RemoveTextParams} with the updated field
     */
    public RemoveTextParams withTextToRemove(String textToRemove) {
        return new RemoveTextParams(textToRemove, this.position);
    }

    /**
     * Returns a copy of this record with the given position.
     *
     * @param position the replacement position; must not be null for valid configuration
     * @return a new {@link RemoveTextParams} with the updated field
     */
    public RemoveTextParams withPosition(ItemPosition position) {
        return new RemoveTextParams(this.textToRemove, position);
    }
}
