package ua.renamer.app.api.session;

import ua.renamer.app.api.enums.TextCaseOptions;
import ua.renamer.app.api.model.TransformationMode;

/**
 * Parameters for the {@link TransformationMode#CHANGE_CASE} transformation mode.
 * Specifies the target case style and whether the first letter should be capitalized.
 *
 * @param caseOption            the target case style to apply; must not be null
 * @param capitalizeFirstLetter {@code true} if the first character should always be uppercased after transformation
 */
public record ChangeCaseParams(TextCaseOptions caseOption, boolean capitalizeFirstLetter)
        implements ModeParameters {

    @Override
    public TransformationMode mode() {
        return TransformationMode.CHANGE_CASE;
    }

    @Override
    public ValidationResult validate() {
        if (caseOption == null) {
            return ValidationResult.fieldError("caseOption", "must not be null");
        }
        return ValidationResult.valid();
    }

    /**
     * Returns a copy of this record with the given case option.
     *
     * @param caseOption the replacement case option; must not be null for valid configuration
     * @return a new {@link ChangeCaseParams} with the updated field
     */
    public ChangeCaseParams withCaseOption(TextCaseOptions caseOption) {
        return new ChangeCaseParams(caseOption, this.capitalizeFirstLetter);
    }

    /**
     * Returns a copy of this record with the given capitalize-first-letter flag.
     *
     * @param capitalizeFirstLetter {@code true} to capitalize the first letter after transformation
     * @return a new {@link ChangeCaseParams} with the updated field
     */
    public ChangeCaseParams withCapitalizeFirstLetter(boolean capitalizeFirstLetter) {
        return new ChangeCaseParams(this.caseOption, capitalizeFirstLetter);
    }
}
