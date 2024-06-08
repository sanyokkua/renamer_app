package ua.renamer.app.core.model;

import lombok.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;

@Builder
@AllArgsConstructor
public class FileInformation {

    @NonNull
    @Getter
    private final File originalFile;
    // Non-Null VALUES
    @NonNull
    @Getter
    private final String fileAbsolutePath; // From the root, including name and extension
    @Getter
    private final boolean isFile; // True if a file, false if a directory
    @NonNull
    @Getter
    private final String fileName; // Name without extension
    @NonNull
    @Getter
    @Builder.Default
    private final String fileExtension = ""; // extension, like .jpg, or empty string
    @Getter
    private final long fileSize; // file size in Bytes

    // Possible Null VALUES
    private final LocalDateTime fsCreationDate; // file creation datetime timestamp
    private final LocalDateTime fsModificationDate; // file modification datetime timestamp
    private final FileInformationMetadata metadata;

    // Values that can be changed after obj creation
    @Getter
    @Setter
    private String newName; // new file name
    @Getter
    @Setter
    private String newExtension; // extension, like .jpg, or empty string

    public Optional<LocalDateTime> getFsCreationDate() {
        return Optional.ofNullable(fsCreationDate);
    }

    public Optional<LocalDateTime> getFsModificationDate() {
        return Optional.ofNullable(fsModificationDate);
    }

    public Optional<FileInformationMetadata> getMetadata() {
        return Optional.ofNullable(metadata);
    }

}
