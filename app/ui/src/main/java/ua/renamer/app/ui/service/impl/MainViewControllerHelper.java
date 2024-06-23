package ua.renamer.app.ui.service.impl;

import com.google.inject.Inject;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.enums.AppModes;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.ui.controller.mode.ModeControllerApi;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;
import ua.renamer.app.ui.widget.table.TableCustomCellValueFactory;
import ua.renamer.app.ui.widget.table.TableCustomContextMenu;

import java.util.function.Function;

import static ua.renamer.app.ui.config.InjectQualifiers.*;

@Slf4j
public class MainViewControllerHelper {

    private final Parent modeAddCustomTextParent;
    private final Parent modeChangeCaseParent;
    private final Parent modeUseDatetimeParent;
    private final Parent modeUseImageDimensionsParent;
    private final Parent modeUseParentFolderNameParent;
    private final Parent modeRemoveCustomTextParent;
    private final Parent modeReplaceCustomTextParent;
    private final Parent modeAddSequenceParent;
    private final Parent modeTruncateFileNameParent;
    private final Parent modeChangeExtensionParent;
    private final ModeControllerApi modeAddCustomTextController;
    private final ModeControllerApi modeChangeCaseController;
    private final ModeControllerApi modeUseDatetimeController;
    private final ModeControllerApi modeUseImageDimensionsController;
    private final ModeControllerApi modeUseParentFolderNameController;
    private final ModeControllerApi modeRemoveCustomTextController;
    private final ModeControllerApi modeReplaceCustomTextController;
    private final ModeControllerApi modeAddSequenceController;
    private final ModeControllerApi modeTruncateFileNameController;
    private final ModeControllerApi modeChangeExtensionController;
    private final LanguageTextRetrieverApi languageTextRetriever;

    @Inject
    public MainViewControllerHelper(@AddCustomTextParent Parent modeAddCustomTextParent,
                                    @ChangeCaseParent Parent modeChangeCaseParent,
                                    @UseDatetimeParent Parent modeUseDatetimeParent,
                                    @UseImageDimensionsParent Parent modeUseImageDimensionsParent,
                                    @UseParentFolderNameParent Parent modeUseParentFolderNameParent,
                                    @RemoveCustomTextParent Parent modeRemoveCustomTextParent,
                                    @ReplaceCustomTextParent Parent modeReplaceCustomTextParent,
                                    @AddSequenceParent Parent modeAddSequenceParent,
                                    @TruncateFileNameParent Parent modeTruncateFileNameParent,
                                    @ChangeExtensionParent Parent modeChangeExtensionParent,
                                    @AddCustomTextController ModeControllerApi modeAddCustomTextController,
                                    @ChangeCaseController ModeControllerApi modeChangeCaseController,
                                    @UseDatetimeController ModeControllerApi modeUseDatetimeController,
                                    @UseImageDimensionsController ModeControllerApi modeUseImageDimensionsController,
                                    @UseParentFolderNameController ModeControllerApi modeUseParentFolderNameController,
                                    @RemoveCustomTextController ModeControllerApi modeRemoveCustomTextController,
                                    @ReplaceCustomTextController ModeControllerApi modeReplaceCustomTextController,
                                    @AddSequenceController ModeControllerApi modeAddSequenceController,
                                    @TruncateFileNameController ModeControllerApi modeTruncateFileNameController,
                                    @ChangeExtensionController ModeControllerApi modeChangeExtensionController,
                                    LanguageTextRetrieverApi languageTextRetriever) {
        this.modeAddCustomTextParent = modeAddCustomTextParent;
        this.modeChangeCaseParent = modeChangeCaseParent;
        this.modeUseDatetimeParent = modeUseDatetimeParent;
        this.modeUseImageDimensionsParent = modeUseImageDimensionsParent;
        this.modeUseParentFolderNameParent = modeUseParentFolderNameParent;
        this.modeRemoveCustomTextParent = modeRemoveCustomTextParent;
        this.modeReplaceCustomTextParent = modeReplaceCustomTextParent;
        this.modeAddSequenceParent = modeAddSequenceParent;
        this.modeTruncateFileNameParent = modeTruncateFileNameParent;
        this.modeChangeExtensionParent = modeChangeExtensionParent;
        this.modeAddCustomTextController = modeAddCustomTextController;
        this.modeChangeCaseController = modeChangeCaseController;
        this.modeUseDatetimeController = modeUseDatetimeController;
        this.modeUseImageDimensionsController = modeUseImageDimensionsController;
        this.modeUseParentFolderNameController = modeUseParentFolderNameController;
        this.modeRemoveCustomTextController = modeRemoveCustomTextController;
        this.modeReplaceCustomTextController = modeReplaceCustomTextController;
        this.modeAddSequenceController = modeAddSequenceController;
        this.modeTruncateFileNameController = modeTruncateFileNameController;
        this.modeChangeExtensionController = modeChangeExtensionController;
        this.languageTextRetriever = languageTextRetriever;
    }

    public Parent getViewForAppMode(AppModes appModes) {
        return switch (appModes) {
            case AppModes.ADD_CUSTOM_TEXT -> this.modeAddCustomTextParent;
            case AppModes.CHANGE_CASE -> this.modeChangeCaseParent;
            case AppModes.USE_DATETIME -> this.modeUseDatetimeParent;
            case AppModes.USE_IMAGE_DIMENSIONS -> this.modeUseImageDimensionsParent;
            case AppModes.USE_PARENT_FOLDER_NAME -> this.modeUseParentFolderNameParent;
            case AppModes.REMOVE_CUSTOM_TEXT -> this.modeRemoveCustomTextParent;
            case AppModes.REPLACE_CUSTOM_TEXT -> this.modeReplaceCustomTextParent;
            case AppModes.ADD_SEQUENCE -> this.modeAddSequenceParent;
            case AppModes.TRUNCATE_FILE_NAME -> this.modeTruncateFileNameParent;
            case AppModes.CHANGE_EXTENSION -> this.modeChangeExtensionParent;
        };
    }

    public ModeControllerApi getControllerForAppMode(AppModes appModes) {
        return switch (appModes) {
            case AppModes.ADD_CUSTOM_TEXT -> this.modeAddCustomTextController;
            case AppModes.CHANGE_CASE -> this.modeChangeCaseController;
            case AppModes.USE_DATETIME -> this.modeUseDatetimeController;
            case AppModes.USE_IMAGE_DIMENSIONS -> this.modeUseImageDimensionsController;
            case AppModes.USE_PARENT_FOLDER_NAME -> this.modeUseParentFolderNameController;
            case AppModes.REMOVE_CUSTOM_TEXT -> this.modeRemoveCustomTextController;
            case AppModes.REPLACE_CUSTOM_TEXT -> this.modeReplaceCustomTextController;
            case AppModes.ADD_SEQUENCE -> this.modeAddSequenceController;
            case AppModes.TRUNCATE_FILE_NAME -> this.modeTruncateFileNameController;
            case AppModes.CHANGE_EXTENSION -> this.modeChangeExtensionController;
        };
    }

    public boolean showConfirmationDialog(TextKeys content, TextKeys title) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(languageTextRetriever.getString(title));
        alert.setHeaderText(null);
        alert.setContentText(languageTextRetriever.getString(content));

        var confirmButton = new ButtonType(languageTextRetriever.getString(TextKeys.DIALOG_CONFIRM_BTN_OK));
        var cancelButton = new ButtonType(languageTextRetriever.getString(TextKeys.DIALOG_CONFIRM_BTN_CANCEL));

        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        alert.showAndWait();

        return alert.getResult() == confirmButton;
    }

    public Tooltip createTooltip(TextKeys textKey) {
        return new Tooltip(languageTextRetriever.getString(textKey));
    }

    public TableCustomCellValueFactory createCellValueFactory(Function<RenameModel, String> extractor) {
        return new TableCustomCellValueFactory(extractor);
    }

    public TableCustomContextMenu createTableContextMenu(TableView<RenameModel> filesTableView) {
        return new TableCustomContextMenu(filesTableView);
    }
}
