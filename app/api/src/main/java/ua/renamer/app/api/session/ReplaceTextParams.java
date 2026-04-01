package ua.renamer.app.api.session;

import ua.renamer.app.api.enums.ItemPositionExtended;
import ua.renamer.app.api.model.TransformationMode;

/**
 * Parameters for the {@link TransformationMode#REPLACE_TEXT} transformation mode.
 * Specifies the text to replace and the replacement text, along with the scope of replacement.
 *
 * @param textToReplace   the text that should be matched and replaced; must not be null
 * @param replacementText the text to substitute in place of the matched text; must not be null
 * @param position        the scope of replacement (BEGIN, END, or EVERYWHERE); must not be null
 */
public record ReplaceTextParams(String textToReplace, String replacementText,
                                ItemPositionExtended position) implements ModeParameters {

    @Override
    public TransformationMode mode() {
        return TransformationMode.REPLACE_TEXT;
    }

    @Override
    public ValidationResult validate() {
        if (textToReplace == null) {
            return ValidationResult.fieldError("textToReplace", "must not be null");
        }
        if (replacementText == null) {
            return ValidationResult.fieldError("replacementText", "must not be null");
        }
        if (position == null) {
            return ValidationResult.fieldError("position", "must not be null");
        }
        return ValidationResult.valid();
    }

    /**
     * Returns a copy of this record with the given text to replace.
     *
     * @param textToReplace the replacement source text; may be null (will fail validation)
     * @return a new {@link ReplaceTextParams} with the updated field
     */
    public ReplaceTextParams withTextToReplace(String textToReplace) {
        return new ReplaceTextParams(textToReplace, this.replacementText, this.position);
    }

    /**
     * Returns a copy of this record with the given replacement text.
     *
     * @param replacementText the new replacement text; may be null (will fail validation)
     * @return a new {@link ReplaceTextParams} with the updated field
     */
    public ReplaceTextParams withReplacementText(String replacementText) {
        return new ReplaceTextParams(this.textToReplace, replacementText, this.position);
    }

    /**
     * Returns a copy of this record with the given position.
     *
     * @param position the replacement scope; must not be null for valid configuration
     * @return a new {@link ReplaceTextParams} with the updated field
     */
    public ReplaceTextParams withPosition(ItemPositionExtended position) {
        return new ReplaceTextParams(this.textToReplace, this.replacementText, position);
    }
}
