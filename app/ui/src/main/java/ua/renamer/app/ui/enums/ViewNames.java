package ua.renamer.app.ui.enums;

import lombok.Getter;

/**
 * Enum representing the different view names for the application's UI components.
 * Each enum constant corresponds to a specific FXML file used in the application.
 * The {@link lombok.Getter} annotation generates a getter for the `viewName` field.
 */
@Getter
public enum ViewNames {
    /**
     * The main view of the application.
     */
    APP_MAIN_VIEW("ApplicationMainView.fxml"),

    /**
     * The view for the mode where custom text is added.
     */
    MODE_ADD_CUSTOM_TEXT("ModeAddCustomText.fxml"),

    /**
     * The view for the mode where the case of text is changed.
     */
    MODE_CHANGE_CASE("ModeChangeCase.fxml"),

    /**
     * The view for the mode where the current date and time are used.
     */
    MODE_USE_DATETIME("ModeUseDatetime.fxml"),

    /**
     * The view for the mode where image dimensions are used.
     */
    MODE_USE_IMAGE_DIMENSIONS("ModeUseImageDimensions.fxml"),

    /**
     * The view for the mode where the parent folder's name is used.
     */
    MODE_USE_PARENT_FOLDER_NAME("ModeUseParentFolderName.fxml"),

    /**
     * The view for the mode where custom text is removed.
     */
    MODE_REMOVE_CUSTOM_TEXT("ModeRemoveCustomText.fxml"),

    /**
     * The view for the mode where custom text is replaced.
     */
    MODE_REPLACE_CUSTOM_TEXT("ModeReplaceCustomText.fxml"),

    /**
     * The view for the mode where a sequence is added.
     */
    MODE_ADD_SEQUENCE("ModeAddSequence.fxml"),

    /**
     * The view for the mode where the file name is truncated.
     */
    MODE_TRUNCATE_FILE_NAME("ModeTruncateFileName.fxml"),

    /**
     * The view for the mode where the file extension is changed.
     */
    MODE_CHANGE_EXTENSION("ModeChangeExtension.fxml");

    /**
     * The name of the FXML file associated with the view.
     */
    private final String viewName;

    /**
     * Constructor to initialize the enum constant with the corresponding FXML file name.
     *
     * @param viewName the name of the FXML file.
     */
    ViewNames(String viewName) {
        this.viewName = viewName;
    }
}
