package ua.renamer.app.api.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.api.enums.ItemPosition;

/**
 * Configuration for adding parent folder name(s) to filenames.
 */
@Value
@Builder(setterPrefix = "with")
public class ParentFolderConfig implements TransformationConfig {
    /** Number of parent folders to include (e.g., 1 = immediate parent, 2 = parent + grandparent). */
    int numberOfParentFolders;

    /** Position where to add parent folder names (BEGIN or END). */
    ItemPosition position;

    /** Separator between folder names and filename. */
    String separator;
}
