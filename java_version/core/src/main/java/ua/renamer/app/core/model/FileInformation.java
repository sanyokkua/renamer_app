package ua.renamer.app.core.model;

import lombok.*;
import ua.renamer.app.core.lang.LanguageManager;
import ua.renamer.app.core.lang.TextKeys;

import java.io.File;
import java.util.Objects;
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
    private final Long fsCreationDate; // file creation datetime timestamp
    private final Long fsModificationDate; // file modification datetime timestamp
    private final FileInformationMetadata metadata;

    // Values that can be changed after obj creation
    @Getter
    @Setter
    private String newName; // new file name
    @Getter
    @Setter
    private String newExtension; // extension, like .jpg, or empty string

    public Optional<Long> getFsCreationDate() {
        return Optional.ofNullable(fsCreationDate);
    }

    public Optional<Long> getFsModificationDate() {
        return Optional.ofNullable(fsModificationDate);
    }

    public Optional<FileInformationMetadata> getMetadata() {
        return Optional.ofNullable(metadata);
    }

    public String formatOriginalFileName() {
        return fileName + fileExtension;
    }

    public String formatFileType() {
        return isFile ? LanguageManager.getString(TextKeys.TYPE_FILE) : LanguageManager.getString(TextKeys.TYPE_FOLDER);
    }

    public String formatNextFileName() {
        var name = Objects.nonNull(newName) ? newName : "";
        var ext = Objects.nonNull(newExtension) ? newExtension : "";
        return name + ext;
    }

}