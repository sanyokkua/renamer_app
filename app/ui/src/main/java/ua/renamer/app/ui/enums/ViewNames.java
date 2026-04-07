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
     * The view for the Add Text mode.
     */
    MODE_ADD_TEXT("ModeAddText.fxml"),

    /**
     * The view for the mode where the case of text is changed.
     */
    MODE_CHANGE_CASE("ModeChangeCase.fxml"),

    /**
     * The view for the Add Date & Time mode.
     */
    MODE_ADD_DATETIME("ModeAddDatetime.fxml"),

    /**
     * The view for the Add Dimensions mode.
     */
    MODE_ADD_DIMENSIONS("ModeAddDimensions.fxml"),

    /**
     * The view for the Add Folder Name mode.
     */
    MODE_ADD_FOLDER_NAME("ModeAddFolderName.fxml"),

    /**
     * The view for the Remove Text mode.
     */
    MODE_REMOVE_TEXT("ModeRemoveText.fxml"),

    /**
     * The view for the Replace Text mode.
     */
    MODE_REPLACE_TEXT("ModeReplaceText.fxml"),

    /**
     * The view for the Number Files mode.
     */
    MODE_NUMBER_FILES("ModeNumberFiles.fxml"),

    /**
     * The view for the Trim Name mode.
     */
    MODE_TRIM_NAME("ModeTrimName.fxml"),

    /**
     * The view for the mode where the file extension is changed.
     */
    MODE_CHANGE_EXTENSION("ModeChangeExtension.fxml"),

    /**
     * The settings dialog view.
     */
    SETTINGS_DIALOG("SettingsDialog.fxml");

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
