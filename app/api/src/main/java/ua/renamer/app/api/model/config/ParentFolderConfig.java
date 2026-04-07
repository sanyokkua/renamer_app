package ua.renamer.app.api.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.api.enums.ItemPosition;

import java.util.Objects;

/**
 * Configuration for adding parent folder name(s) to filenames.
 */
@Value
@Builder(setterPrefix = "with")
public class ParentFolderConfig implements TransformationConfig {
    /**
     * Number of parent folders to include (e.g., 1 = immediate parent, 2 = parent + grandparent).
     */
    int numberOfParentFolders;

    /**
     * Position where to add parent folder names (BEGIN or END).
     */
    ItemPosition position;

    /**
     * Separator between folder names and filename.
     */
    String separator;

    // Partial Lombok builder — Lombok adds with* methods; we override build() for validation

    /**
     * Partial Lombok builder that overrides {@code build()} to add validation.
     */
    public static class ParentFolderConfigBuilder {
        /**
         * Builds the {@link ParentFolderConfig}, validating that required fields are non-null and
         * numberOfParentFolders is at least 1.
         *
         * @return a new {@link ParentFolderConfig} instance
         * @throws NullPointerException     if position is null
         * @throws IllegalArgumentException if numberOfParentFolders is less than 1
         */
        public ParentFolderConfig build() {
            Objects.requireNonNull(position, "position must not be null");
            if (numberOfParentFolders < 1) {
                throw new IllegalArgumentException(
                        "numberOfParentFolders must be >= 1, got: " + numberOfParentFolders);
            }
            return new ParentFolderConfig(numberOfParentFolders, position, separator);
        }
    }
}
