package ua.renamer.app.core.model;

import lombok.*;
import ua.renamer.app.core.enums.RenameResult;

/**
 * This class represents a model for storing information related to a file renaming operation.
 */
@Getter
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenameModel {

    /**
     * The FileInformation object containing details about the file being renamed. (Non-null)
     */
    @NonNull
    private final FileInformation fileInformation;
    /**
     * A flag indicating whether the file needs to be renamed. (Non-null)
     */
    private final boolean isNeedRename;
    /**
     * The original name of the file before renaming. (Non-null)
     */
    @NonNull
    private final String oldName;
    /**
     * The new name for the file after renaming. (Non-null)
     */
    @NonNull
    private final String newName;
    /**
     * The absolute path of the directory containing the file, excluding the file name. (Non-null)
     */
    @NonNull
    private final String absolutePathWithoutName;
    /**
     * A flag indicating whether an error occurred during renaming. (Defaults to false)
     */
    @Setter
    @Builder.Default
    private boolean hasRenamingError = false;
    /**
     * An optional error message associated with the renaming process. (Defaults to an empty string)
     */
    @Setter
    @NonNull
    @Builder.Default
    private String renamingErrorMessage = "";
    /**
     * A flag indicating whether the file was successfully renamed. (Defaults to false)
     */
    @Setter
    @Builder.Default
    private boolean isRenamed = false;
    /**
     * Message that will show reasons of the renaming result. (Defaults to an empty string)
     */
    @Setter
    @Builder.Default
    private RenameResult renameResult = RenameResult.NO_ACTIONS_HAPPEN;

}
