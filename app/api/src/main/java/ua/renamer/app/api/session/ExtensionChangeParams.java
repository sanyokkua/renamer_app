package ua.renamer.app.api.session;

import ua.renamer.app.api.model.TransformationMode;

/**
 * Parameters for the {@link TransformationMode#CHANGE_EXTENSION} transformation mode.
 * Specifies the new file extension to apply.
 *
 * @param newExtension the extension to assign (without a leading dot); must not be null or blank
 */
public record ExtensionChangeParams(String newExtension) implements ModeParameters {

    @Override
    public TransformationMode mode() {
        return TransformationMode.CHANGE_EXTENSION;
    }

    @Override
    public ValidationResult validate() {
        if (newExtension == null) {
            return ValidationResult.fieldError("newExtension", "must not be null");
        }
        if (newExtension.isBlank()) {
            return ValidationResult.fieldError("newExtension", "must not be blank");
        }
        return ValidationResult.valid();
    }

    /**
     * Returns a copy of this record with the given new extension.
     *
     * @param newExtension the replacement extension; must not be null or blank for valid configuration
     * @return a new {@link ExtensionChangeParams} with the updated field
     */
    public ExtensionChangeParams withNewExtension(String newExtension) {
        return new ExtensionChangeParams(newExtension);
    }
}
