package ua.renamer.app.ui.controller;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ua.renamer.app.api.model.FolderDropOptions;

/**
 * Builds and shows the modal dialog displayed when one or more folders are
 * dropped onto the file table.
 *
 * <p>This is a static utility class — no instantiation or Guice injection needed.
 * Must be called on the JavaFX Application Thread.
 */
public final class FolderDropDialogController {

    private FolderDropDialogController() {}

    /**
     * Shows the folder drop dialog and blocks until the user dismisses it.
     *
     * @param folderCount number of folders dropped (used to customise the header)
     * @return the user's choice; never null; returns {@link FolderDropOptions#cancel()}
     *         if the user closes the dialog without choosing
     */
    public static FolderDropOptions show(int folderCount) {
        var btnCancel   = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        var btnAsItem   = new ButtonType("Use as item", ButtonBar.ButtonData.LEFT);
        var btnContents = new ButtonType("Use folder contents", ButtonBar.ButtonData.OK_DONE);

        var dialog = new Dialog<FolderDropOptions>();
        dialog.setTitle("Folder dropped");
        dialog.setHeaderText(folderCount == 1
                ? "A folder was dropped. How should it be handled?"
                : folderCount + " folders were dropped. How should they be handled?");
        dialog.getDialogPane().getButtonTypes().addAll(btnCancel, btnAsItem, btnContents);

        var cbRecursive      = new CheckBox("Include files from subfolders (recursive)");
        var cbIncludeFolders = new CheckBox("Include subfolders as items");

        var content = new VBox(10,
                new Label("Options for \"Use folder contents\":"),
                cbRecursive,
                cbIncludeFolders);
        content.setPadding(new Insets(10, 0, 0, 0));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == btnAsItem)   return FolderDropOptions.useAsItem();
            if (buttonType == btnContents) return new FolderDropOptions(
                    FolderDropOptions.Action.USE_CONTENTS,
                    cbRecursive.isSelected(),
                    cbIncludeFolders.isSelected());
            return FolderDropOptions.cancel();
        });

        dialog.getDialogPane().getStylesheets().addAll(
                FolderDropDialogController.class.getResource("/styles/base.css").toExternalForm(),
                FolderDropDialogController.class.getResource("/styles/components.css").toExternalForm()
        );

        return dialog.showAndWait().orElse(FolderDropOptions.cancel());
    }
}
