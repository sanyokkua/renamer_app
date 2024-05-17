package ua.renamer.app.core.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppFile {
    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private String absolutePath;
    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private String type;
    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private String fileName;
    @NonNull
    @Builder.Default
    @Setter(AccessLevel.PRIVATE)
    private String fileExtension = "";
    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private long fileSize;
    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private long fsCreationDate;
    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private long fsModificationDate;
    @NonNull
    @Setter(AccessLevel.PRIVATE)
    private Metadata metadata;

    @NonNull
    @Builder.Default
    private String nextName = "";
    @NonNull
    @Builder.Default
    private String fileExtensionNew = "";
}
