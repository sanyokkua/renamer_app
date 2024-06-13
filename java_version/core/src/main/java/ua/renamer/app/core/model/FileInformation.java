package ua.renamer.app.core.model;

import lombok.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * Represents information about a file, including its path, type, size, and metadata.
 */
@Builder
@AllArgsConstructor
public class FileInformation {

    /**
     * The original File object.
     */
    @NonNull
    @Getter
    private final File originalFile;

    // Non-Null VALUES

    /**
     * The absolute path of the file from the root, including name and extension.
     */
    @NonNull
    @Getter
    private final String fileAbsolutePath; // From the root, including name and extension
    /**
     * Indicates if the path is a file (true) or a directory (false).
     */
    @Getter
    private final boolean isFile; // True if a file, false if a directory
    /**
     * The name of the file without the extension.
     */
    @NonNull
    @Getter
    private final String fileName; // Name without extension

    /**
     * The file extension, like .jpg, or an empty string if no extension.
     */
    @NonNull
    @Getter
    @Builder.Default
    private final String fileExtension = ""; // extension, like .jpg, or empty string
    @NonNull
    @Getter
    @Builder.Default
    private final Set<String> detectedExtension = Set.of(); // extensions, like [.jpg, .png], or empty string
    @NonNull
    @Getter
    @Builder.Default
    private final String detectedMimeType = ""; // example: "text/json", or empty string
    /**
     * The size of the file in bytes.
     */
    @Getter
    private final long fileSize; // file size in Bytes

    // Possible Null VALUES

    /**
     * The file system creation date and time timestamp, which may be null.
     */
    private final LocalDateTime fsCreationDate; // file creation datetime timestamp
    /**
     * The file system modification date and time timestamp, which may be null.
     */
    private final LocalDateTime fsModificationDate; // file modification datetime timestamp
    /**
     * Additional metadata about the file, which may be null.
     */
    private final FileInformationMetadata metadata;

    // Values that can be changed after obj creation

    /**
     * The new name for the file, which can be changed after object creation.
     */
    @Getter
    @Setter
    private String newName; // new file name
    /**
     * The new file extension, like .jpg, or an empty string, which can be changed after object creation.
     */
    @Getter
    @Setter
    private String newExtension; // extension, like .jpg, or empty string

    /**
     * Gets the file system creation date as an Optional.
     *
     * @return an Optional containing the creation date if available, otherwise empty.
     */
    public Optional<LocalDateTime> getFsCreationDate() {
        return Optional.ofNullable(fsCreationDate);
    }

    /**
     * Gets the file system modification date as an Optional.
     *
     * @return an Optional containing the modification date if available, otherwise empty.
     */
    public Optional<LocalDateTime> getFsModificationDate() {
        return Optional.ofNullable(fsModificationDate);
    }

    /**
     * Gets the file metadata as an Optional.
     *
     * @return an Optional containing the metadata if available, otherwise empty.
     */
    public Optional<FileInformationMetadata> getMetadata() {
        return Optional.ofNullable(metadata);
    }

}
