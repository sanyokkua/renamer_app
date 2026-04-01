package ua.renamer.app.api.session;

import ua.renamer.app.api.enums.TruncateOptions;
import ua.renamer.app.api.model.TransformationMode;

/**
 * Parameters for the {@link TransformationMode#TRUNCATE_FILE_NAME} transformation mode.
 * Specifies how many characters to remove and from which end of the filename.
 *
 * @param numberOfSymbols the number of characters to remove; must be zero or greater
 * @param truncateOption  the direction or style of truncation; must not be null
 */
public record TruncateParams(int numberOfSymbols, TruncateOptions truncateOption)
        implements ModeParameters {

    @Override
    public TransformationMode mode() {
        return TransformationMode.TRUNCATE_FILE_NAME;
    }

    @Override
    public ValidationResult validate() {
        if (truncateOption == null) {
            return ValidationResult.fieldError("truncateOption", "must not be null");
        }
        if (numberOfSymbols < 0) {
            return ValidationResult.fieldError("numberOfSymbols", "must be zero or greater");
        }
        return ValidationResult.valid();
    }

    /**
     * Returns a copy of this record with the given number of symbols.
     *
     * @param numberOfSymbols the new character count; must be zero or greater for valid configuration
     * @return a new {@link TruncateParams} with the updated field
     */
    public TruncateParams withNumberOfSymbols(int numberOfSymbols) {
        return new TruncateParams(numberOfSymbols, this.truncateOption);
    }

    /**
     * Returns a copy of this record with the given truncate option.
     *
     * @param truncateOption the new truncation direction; must not be null for valid configuration
     * @return a new {@link TruncateParams} with the updated field
     */
    public TruncateParams withTruncateOption(TruncateOptions truncateOption) {
        return new TruncateParams(this.numberOfSymbols, truncateOption);
    }
}
