package ua.renamer.app.api.session;

import ua.renamer.app.api.enums.SortSource;
import ua.renamer.app.api.model.TransformationMode;

/**
 * Parameters for the {@link TransformationMode#ADD_SEQUENCE} transformation mode.
 * Specifies how a numeric sequence is generated and appended to filenames.
 * This mode requires sequential (non-parallel) execution across all files;
 * see {@link #requiresSequentialExecution()}.
 *
 * @param startNumber   the first number in the sequence; may be any integer
 * @param stepValue     the increment between successive sequence numbers; must be greater than zero
 * @param paddingDigits the minimum number of digits in the formatted sequence number; must be zero or greater
 * @param sortSource    the criterion used to order files before assigning sequence numbers; must not be null
 */
public record SequenceParams(int startNumber, int stepValue, int paddingDigits, SortSource sortSource)
        implements ModeParameters {

    @Override
    public TransformationMode mode() {
        return TransformationMode.ADD_SEQUENCE;
    }

    @Override
    public ValidationResult validate() {
        if (sortSource == null) {
            return ValidationResult.fieldError("sortSource", "must not be null");
        }
        if (stepValue <= 0) {
            return ValidationResult.fieldError("stepValue", "must be greater than zero");
        }
        if (paddingDigits < 0) {
            return ValidationResult.fieldError("paddingDigits", "must be zero or greater");
        }
        return ValidationResult.valid();
    }

    @Override
    public boolean requiresSequentialExecution() {
        return true;
    }

    /**
     * Returns a copy of this record with the given start number.
     *
     * @param startNumber the new starting sequence number
     * @return a new {@link SequenceParams} with the updated field
     */
    public SequenceParams withStartNumber(int startNumber) {
        return new SequenceParams(startNumber, this.stepValue, this.paddingDigits, this.sortSource);
    }

    /**
     * Returns a copy of this record with the given step value.
     *
     * @param stepValue the new step increment; must be greater than zero for valid configuration
     * @return a new {@link SequenceParams} with the updated field
     */
    public SequenceParams withStepValue(int stepValue) {
        return new SequenceParams(this.startNumber, stepValue, this.paddingDigits, this.sortSource);
    }

    /**
     * Returns a copy of this record with the given padding digits count.
     *
     * @param paddingDigits the new minimum digit count; must be zero or greater for valid configuration
     * @return a new {@link SequenceParams} with the updated field
     */
    public SequenceParams withPaddingDigits(int paddingDigits) {
        return new SequenceParams(this.startNumber, this.stepValue, paddingDigits, this.sortSource);
    }

    /**
     * Returns a copy of this record with the given sort source.
     *
     * @param sortSource the new sort criterion; must not be null for valid configuration
     * @return a new {@link SequenceParams} with the updated field
     */
    public SequenceParams withSortSource(SortSource sortSource) {
        return new SequenceParams(this.startNumber, this.stepValue, this.paddingDigits, sortSource);
    }
}
