package ua.renamer.app.ui.controller;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ua.renamer.app.api.model.FolderDropOptions;
import ua.renamer.app.ui.enums.TextKeys;

import java.util.function.Function;

/**
 * Builds and shows the modal dialog displayed when one or more folders are
 * dropped onto the file table.
 *
 * <p>This is a static utility class — no instantiation or Guice injection needed.
 * Must be called on the JavaFX Application Thread.
 */
public final class FolderDropDialogController {

    private FolderDropDialogController() {
    }

    /**
     * Shows the folder drop dialog and blocks until the user dismisses it.
     *
     * @param folderCount number of folders dropped (used to customise the header)
     * @param resolver    function that converts a {@link TextKeys} constant to a localized string;
     *                    must not be null
     * @return the user's choice; never null; returns {@link FolderDropOptions#cancel()}
     * if the user closes the dialog without choosing
     */
    public static FolderDropOptions show(int folderCount, Function<TextKeys, String> resolver) {
        var btnCancel = new ButtonType(resolver.apply(TextKeys.DIALOG_FOLDER_BTN_CANCEL), ButtonBar.ButtonData.CANCEL_CLOSE);
        var btnAsItem = new ButtonType(resolver.apply(TextKeys.DIALOG_FOLDER_BTN_AS_ITEM), ButtonBar.ButtonData.OTHER);
        var btnContents = new ButtonType(resolver.apply(TextKeys.DIALOG_FOLDER_BTN_CONTENTS), ButtonBar.ButtonData.OK_DONE);

        var dialog = new Dialog<FolderDropOptions>();
        dialog.setTitle(resolver.apply(TextKeys.DIALOG_FOLDER_TITLE));
        dialog.setHeaderText(folderCount == 1
                ? resolver.apply(TextKeys.DIALOG_FOLDER_HEADER_SINGLE)
                : resolver.apply(TextKeys.DIALOG_FOLDER_HEADER_MULTIPLE).replace("{0}", String.valueOf(folderCount)));
        dialog.getDialogPane().getButtonTypes().addAll(btnCancel, btnAsItem, btnContents);

        var cbRecursive = new CheckBox(resolver.apply(TextKeys.DIALOG_FOLDER_CB_RECURSIVE));
        var cbIncludeFolders = new CheckBox(resolver.apply(TextKeys.DIALOG_FOLDER_CB_INCLUDE_FOLDERS));

        var content = new VBox(10,
                new Label(resolver.apply(TextKeys.DIALOG_FOLDER_OPTIONS_LABEL)),
                cbRecursive,
                cbIncludeFolders);
        content.setPadding(new Insets(10, 16, 8, 16));

        dialog.setOnShowing(e -> {
            // Disable platform-specific button reordering; buttons render in insertion order:
            // Cancel | Use as item | Use folder contents
            var bar = dialog.getDialogPane().lookup(".button-bar");
            if (bar instanceof ButtonBar buttonBar) {
                buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_NONE);
            }
            // Visual hierarchy: primary (accent fill) > secondary (outline) > ghost (muted)
            var nodeContents = dialog.getDialogPane().lookupButton(btnContents);
            var nodeAsItem = dialog.getDialogPane().lookupButton(btnAsItem);
            var nodeCancel = dialog.getDialogPane().lookupButton(btnCancel);
            if (nodeContents != null) nodeContents.getStyleClass().add("btn-primary");
            if (nodeAsItem != null) nodeAsItem.getStyleClass().add("btn-secondary");
            if (nodeCancel != null) nodeCancel.getStyleClass().add("btn-ghost");
        });

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == btnAsItem) return FolderDropOptions.useAsItem();
            if (buttonType == btnContents) return new FolderDropOptions(
                    FolderDropOptions.Action.USE_CONTENTS,
                    cbRecursive.isSelected(),
                    cbIncludeFolders.isSelected());
            return FolderDropOptions.cancel();
        });

        dialog.getDialogPane().getStylesheets().addAll(
                FolderDropDialogController.class.getResource("/styles/base.css").toExternalForm(),
                FolderDropDialogController.class.getResource("/styles/buttons.css").toExternalForm(),
                FolderDropDialogController.class.getResource("/styles/components.css").toExternalForm()
        );

        return dialog.showAndWait().orElse(FolderDropOptions.cancel());
    }
}
