package ua.renamer.app.api.session;

import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.TransformationMode;

/**
 * Parameters for the {@link TransformationMode#USE_PARENT_FOLDER_NAME} mode.
 * Adds one or more parent folder names to the filename.
 *
 * @param numberOfParentFolders number of parent folders to include; must be &gt;= 1
 * @param position              where to insert the folder name(s) (BEGIN or END)
 * @param separator             string placed between folder name and filename
 */
public record ParentFolderParams(
        int numberOfParentFolders,
        ItemPosition position,
        String separator
) implements ModeParameters {

    @Override
    public TransformationMode mode() {
        return TransformationMode.USE_PARENT_FOLDER_NAME;
    }

    @Override
    public ValidationResult validate() {
        if (position == null) {
            return ValidationResult.fieldError("position", "must not be null");
        }
        if (numberOfParentFolders < 1) {
            return ValidationResult.fieldError("numberOfParentFolders", "must be >= 1, got: " + numberOfParentFolders);
        }
        return ValidationResult.valid();
    }

    /**
     * Returns a copy of this record with a new {@code numberOfParentFolders} value.
     *
     * @param numberOfParentFolders the new value; must be &gt;= 1 (validated on {@link #validate()})
     * @return a new {@link ParentFolderParams} instance
     */
    public ParentFolderParams withNumberOfParentFolders(int numberOfParentFolders) {
        return new ParentFolderParams(numberOfParentFolders, this.position, this.separator);
    }

    /**
     * Returns a copy of this record with a new {@code position} value.
     *
     * @param position the new position; must not be null (validated on {@link #validate()})
     * @return a new {@link ParentFolderParams} instance
     */
    public ParentFolderParams withPosition(ItemPosition position) {
        return new ParentFolderParams(this.numberOfParentFolders, position, this.separator);
    }

    /**
     * Returns a copy of this record with a new {@code separator} value.
     *
     * @param separator the new separator string
     * @return a new {@link ParentFolderParams} instance
     */
    public ParentFolderParams withSeparator(String separator) {
        return new ParentFolderParams(this.numberOfParentFolders, this.position, separator);
    }
}
