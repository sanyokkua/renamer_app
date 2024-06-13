package ua.renamer.app.ui.controller;

import com.google.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.enums.AppModes;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.core.service.command.FileInformationCommand;
import ua.renamer.app.ui.controller.mode.ModeControllerApi;
import ua.renamer.app.ui.converter.AppModesConverter;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.enums.ViewNames;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;
import ua.renamer.app.ui.service.ViewLoaderApi;
import ua.renamer.app.ui.service.impl.AppCoreFunctionalityHelper;

import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import static ua.renamer.app.core.enums.AppModes.*;

@Slf4j
public class ApplicationMainViewController implements Initializable {

    private final AppCoreFunctionalityHelper appCoreFunctionalityHelper;
    private final AppModesConverter appModesConverter;
    private final ViewLoaderApi viewLoaderApi;
    private final LanguageTextRetrieverApi languageTextRetriever;

    private final Map<AppModes, Parent> appModeToViewMap;
    private final Map<AppModes, ModeControllerApi> appModeToControllerMap;
    private final ObservableList<RenameModel> loadedAppFilesList;

    @FXML
    private ChoiceBox<AppModes> appModeChoiceBox;
    @FXML
    private StackPane appModeContainer;
    @FXML
    private CheckBox autoPreviewCheckBox;
    @FXML
    private Button previewBtn;
    @FXML
    private Button renameBtn;
    @FXML
    private Button clearBtn;
    @FXML
    private TableView<RenameModel> filesTableView;
    @FXML
    private TableColumn<RenameModel, String> originalNameColumn;
    @FXML
    private TableColumn<RenameModel, String> itemTypeColumn;
    @FXML
    private TableColumn<RenameModel, String> newNameColumn;
    @FXML
    private TableColumn<RenameModel, String> statusColumn;
    @FXML
    private WebView fileInfoWebView;
    @FXML
    private ProgressBar appProgressBar;

    @Inject
    public ApplicationMainViewController(AppCoreFunctionalityHelper appCoreFunctionalityHelper,
                                         ViewLoaderApi viewLoaderApi, AppModesConverter appModesConverter,
                                         LanguageTextRetrieverApi languageTextRetriever) {
        this.appCoreFunctionalityHelper = appCoreFunctionalityHelper;
        this.viewLoaderApi = viewLoaderApi;
        this.appModesConverter = appModesConverter;
        this.languageTextRetriever = languageTextRetriever;

        appModeToViewMap = new EnumMap<>(AppModes.class);
        appModeToControllerMap = new EnumMap<>(AppModes.class);
        loadedAppFilesList = FXCollections.observableArrayList();
    }

    private static Callback<TableColumn<RenameModel, String>, TableCell<RenameModel, String>> createFactoryWithTooltip() {
        return col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    Tooltip tooltip = new Tooltip(item);
                    setTooltip(tooltip);
                    RenameModel model = getTableView().getItems().get(getIndex());
                    if (model.isNeedRename()) {
                        setTextFill(Color.valueOf("#ffffff"));
                    } else if (model.isHasRenamingError()) {
                        setTextFill(Color.valueOf("#ffffff"));
                    } else if (model.isRenamed()) {
                        setTextFill(Color.valueOf("#000000"));
                    } else {
                        setTextFill(Color.BLACK);
                    }
                }

            }
        };
    }

    private void initializeModeViews() {
        log.info("Initializing modeViews");
        var modeAddCustomTextLoader = viewLoaderApi.createLoader(ViewNames.MODE_ADD_CUSTOM_TEXT);
        var modeChangeCaseLoader = viewLoaderApi.createLoader(ViewNames.MODE_CHANGE_CASE);
        var modeUseDatetimeLoader = viewLoaderApi.createLoader(ViewNames.MODE_USE_DATETIME);
        var modeUseImageDimensionsLoader = viewLoaderApi.createLoader(ViewNames.MODE_USE_IMAGE_DIMENSIONS);
        var modeUseParentFolderNameLoader = viewLoaderApi.createLoader(ViewNames.MODE_USE_PARENT_FOLDER_NAME);
        var modeRemoveCustomTextLoader = viewLoaderApi.createLoader(ViewNames.MODE_REMOVE_CUSTOM_TEXT);
        var modeReplaceCustomTextLoader = viewLoaderApi.createLoader(ViewNames.MODE_REPLACE_CUSTOM_TEXT);
        var modeAddSequenceLoader = viewLoaderApi.createLoader(ViewNames.MODE_ADD_SEQUENCE);
        var modeTruncateFileNameLoader = viewLoaderApi.createLoader(ViewNames.MODE_TRUNCATE_FILE_NAME);
        var modeChangeExtensionLoader = viewLoaderApi.createLoader(ViewNames.MODE_CHANGE_EXTENSION);

        if (modeAddCustomTextLoader.isEmpty()) {
            throw new IllegalStateException("Failed to create loader: modeAddCustomTextLoader");
        }
        if (modeChangeCaseLoader.isEmpty()) {
            throw new IllegalStateException("Failed to create loader: modeChangeCaseLoader");
        }
        if (modeUseDatetimeLoader.isEmpty()) {
            throw new IllegalStateException("Failed to create loader: modeUseDatetimeLoader");
        }
        if (modeUseImageDimensionsLoader.isEmpty()) {
            throw new IllegalStateException("Failed to create loader: modeUseImageDimensionsLoader");
        }
        if (modeUseParentFolderNameLoader.isEmpty()) {
            throw new IllegalStateException("Failed to create loader: modeUseParentFolderNameLoader");
        }
        if (modeRemoveCustomTextLoader.isEmpty()) {
            throw new IllegalStateException("Failed to create loader: modeRemoveCustomTextLoader");
        }
        if (modeReplaceCustomTextLoader.isEmpty()) {
            throw new IllegalStateException("Failed to create loader: modeReplaceCustomTextLoader");
        }
        if (modeAddSequenceLoader.isEmpty()) {
            throw new IllegalStateException("Failed to create loader: modeAddSequenceLoader");
        }
        if (modeTruncateFileNameLoader.isEmpty()) {
            throw new IllegalStateException("Failed to create loader: modeTruncateFileNameLoader");
        }
        if (modeChangeExtensionLoader.isEmpty()) {
            throw new IllegalStateException("Failed to create loader: modeChangeExtensionLoader");
        }

        try {
            FXMLLoader addCustomTextLoader = modeAddCustomTextLoader.get();
            FXMLLoader changeCaseLoader = modeChangeCaseLoader.get();
            FXMLLoader useDatetimeLoader = modeUseDatetimeLoader.get();
            FXMLLoader useImageDimensionsLoader = modeUseImageDimensionsLoader.get();
            FXMLLoader useParentFolderNameLoader = modeUseParentFolderNameLoader.get();
            FXMLLoader removeCustomTextLoader = modeRemoveCustomTextLoader.get();
            FXMLLoader replaceCustomTextLoader = modeReplaceCustomTextLoader.get();
            FXMLLoader addSequenceLoader = modeAddSequenceLoader.get();
            FXMLLoader truncateFileNameLoader = modeTruncateFileNameLoader.get();
            FXMLLoader changeExtensionLoader = modeChangeExtensionLoader.get();

            appModeToViewMap.put(ADD_CUSTOM_TEXT, addCustomTextLoader.load());
            appModeToViewMap.put(CHANGE_CASE, changeCaseLoader.load());
            appModeToViewMap.put(USE_DATETIME, useDatetimeLoader.load());
            appModeToViewMap.put(USE_IMAGE_DIMENSIONS, useImageDimensionsLoader.load());
            appModeToViewMap.put(USE_PARENT_FOLDER_NAME, useParentFolderNameLoader.load());
            appModeToViewMap.put(REMOVE_CUSTOM_TEXT, removeCustomTextLoader.load());
            appModeToViewMap.put(REPLACE_CUSTOM_TEXT, replaceCustomTextLoader.load());
            appModeToViewMap.put(ADD_SEQUENCE, addSequenceLoader.load());
            appModeToViewMap.put(TRUNCATE_FILE_NAME, truncateFileNameLoader.load());
            appModeToViewMap.put(CHANGE_EXTENSION, changeExtensionLoader.load());

            appModeToControllerMap.put(ADD_CUSTOM_TEXT, addCustomTextLoader.getController());
            appModeToControllerMap.put(CHANGE_CASE, changeCaseLoader.getController());
            appModeToControllerMap.put(USE_DATETIME, useDatetimeLoader.getController());
            appModeToControllerMap.put(USE_IMAGE_DIMENSIONS, useImageDimensionsLoader.getController());
            appModeToControllerMap.put(USE_PARENT_FOLDER_NAME, useParentFolderNameLoader.getController());
            appModeToControllerMap.put(REMOVE_CUSTOM_TEXT, removeCustomTextLoader.getController());
            appModeToControllerMap.put(REPLACE_CUSTOM_TEXT, replaceCustomTextLoader.getController());
            appModeToControllerMap.put(ADD_SEQUENCE, addSequenceLoader.getController());
            appModeToControllerMap.put(TRUNCATE_FILE_NAME, truncateFileNameLoader.getController());
            appModeToControllerMap.put(CHANGE_EXTENSION, changeExtensionLoader.getController());

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load view", e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing ApplicationMainViewController");
        initializeModeViews();
        configureModeChoiceBox();
        configureFilesTableView();
        configureFilesTableViewColumns();
        configureAutoPreviewCheckbox();
        configurePreviewBtn();
        configureRenameBtn();
        configureClearBtn();
        configureProgressBar();
        configureModeCommandChangedListener();
        configureControlWidgetsState();
        handleModeChanged(); // Select default view
    }

    private void configureFilesTableView() {
        log.info("Configuring filesTableView");
        filesTableView.setOnDragOver(this::handleDragOverEvent);
        filesTableView.setOnDragDropped(this::handleFilesDroppedEvent);
        filesTableView.setItems(loadedAppFilesList);
        filesTableView.getSelectionModel()
                      .selectedItemProperty()
                      .addListener((obs, oldSelection, newSelection) -> handleFileInTableSelectedEvent(newSelection));
        filesTableView.setRowFactory(tableView -> new TableRow<>() {
            @Override
            protected void updateItem(RenameModel item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    if (item.isNeedRename()) {
                        setStyle("-fx-background-color: #005780;");
                    } else if (item.isHasRenamingError()) {
                        setStyle("-fx-background-color: #ef0b0b;");
                    } else if (item.isRenamed()) {
                        setStyle("-fx-background-color: #67ff67;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        filesTableView.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double availableWidth = newWidth.doubleValue() - itemTypeColumn.getWidth();
            double columnWidth = availableWidth / 3;
            originalNameColumn.setPrefWidth(columnWidth);
            newNameColumn.setPrefWidth(columnWidth);
            statusColumn.setPrefWidth(columnWidth);
        });
        filesTableView.setContextMenu(createColumnVisibilityMenu(filesTableView));
    }

    private void configureModeChoiceBox() {
        log.info("Configuring modeChoiceBox");
        appModeChoiceBox.getItems().addAll(values());
        appModeChoiceBox.setValue(ADD_CUSTOM_TEXT);
        appModeChoiceBox.setConverter(appModesConverter);
        appModeChoiceBox.setOnAction((event -> this.handleModeChanged()));
    }

    private void handleFilesDroppedEvent(DragEvent event) {
        log.debug("handleFilesDroppedEvent");

        var dragboard = event.getDragboard();
        var success = false;
        if (dragboard.hasFiles()) {
            appCoreFunctionalityHelper.mapFileToRenameModel(dragboard.getFiles(), appProgressBar, result -> {
                loadedAppFilesList.addAll(result);
                configureControlWidgetsState();
            });
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();

        configureControlWidgetsState();
    }

    private ContextMenu createColumnVisibilityMenu(TableView<RenameModel> filesTableView) {
        ContextMenu contextMenu = new ContextMenu();
        for (TableColumn<RenameModel, ?> column : filesTableView.getColumns()) {
            CheckBox checkBox = new CheckBox(column.getText());
            checkBox.setSelected(true);
            checkBox.setOnAction(event -> {
                if (checkBox.isSelected()) {
                    if (!filesTableView.getColumns().contains(column)) {
                        filesTableView.getColumns().add(column);
                    }
                } else {
                    filesTableView.getColumns().remove(column);
                }
            });
            CustomMenuItem customMenuItem = new CustomMenuItem(checkBox);
            customMenuItem.setHideOnClick(false);
            contextMenu.getItems().add(customMenuItem);
        }
        return contextMenu;
    }

    private void configureFilesTableViewColumns() {
        log.info("Configuring filesTableViewColumns");
        originalNameColumn.setCellFactory(createFactoryWithTooltip());
        itemTypeColumn.setCellFactory(createFactoryWithTooltip());
        newNameColumn.setCellFactory(createFactoryWithTooltip());
        statusColumn.setCellFactory(createFactoryWithTooltip());
        originalNameColumn.setCellValueFactory(appCoreFunctionalityHelper::extractOriginalNameFromRenameModel);
        itemTypeColumn.setCellValueFactory(appCoreFunctionalityHelper::extractFileTypeFromRenameModel);
        newNameColumn.setCellValueFactory(appCoreFunctionalityHelper::extractNewFileNameFromRenameModel);
        statusColumn.setCellValueFactory(appCoreFunctionalityHelper::extractStatusFromRenameModel);
    }

    private void configureAutoPreviewCheckbox() {
        log.info("Configuring autoPreviewCheckbox");
        BooleanProperty selectedProperty = autoPreviewCheckBox.selectedProperty();
        selectedProperty.addListener((observable, oldValue, newValue) -> this.handleAutoPreviewChanged(newValue));
        autoPreviewCheckBox.setTooltip(new Tooltip(languageTextRetriever.getString(TextKeys.CHECK_BOX_AUTO_PREVIEW)));
    }

    private void handleAutoPreviewChanged(boolean isChecked) {
        log.debug("handleAutoPreviewChanged: {}", isChecked);
        if (isChecked) {
            this.handlePreviewBtnClicked();
        }
        this.configureControlWidgetsState();
    }

    private void handlePreviewBtnClicked() {
        log.debug("handlePreviewBtnClicked");
        AppModes appMode = appModeChoiceBox.getValue();
        var controller = appModeToControllerMap.get(appMode);

        log.debug("handlePreviewBtnClicked. AppMode: {}", appMode.name());

        if (Objects.nonNull(controller)) {
            var cmd = controller.getCommand();
            log.debug("handlePreviewBtnClicked. Command: {}", cmd);
            updatePreview(cmd);
        }

        this.configureControlWidgetsState();
    }

    private void configureProgressBar() {
        log.info("Configuring progressBar");
        appProgressBar.setProgress(0);
    }

    private void configureModeCommandChangedListener() {
        log.info("Configuring modeCommandChangedListener");
        // @formatter:off
        appModeToControllerMap.forEach((key, value)
                                               -> value.commandProperty().addListener((observable, oldValue, newValue)
                                                    -> this.handleCommandInTheModeViewUpdated(key, newValue)));
        // @formatter:on
    }

    private void configureControlWidgetsState() {
        log.info("Configuring controlWidgetsState");

        if (loadedAppFilesList.isEmpty()) {
            autoPreviewCheckBox.setDisable(true);
            previewBtn.setDisable(true);
            renameBtn.setDisable(true);
            clearBtn.setDisable(true);
        } else {
            autoPreviewCheckBox.setDisable(false);
            previewBtn.setDisable(false);
            renameBtn.setDisable(false);
            clearBtn.setDisable(false);
        }

        if (!loadedAppFilesList.isEmpty()) {
            previewBtn.setDisable(autoPreviewCheckBox.isSelected());
        }

    }

    @FXML
    private void handleModeChanged() {
        log.debug("handleModeChanged");
        AppModes appMode = appModeChoiceBox.getValue();

        appModeContainer.getChildren().clear();
        var view = appModeToViewMap.get(appMode);
        StackPane.setMargin(view, new Insets(10, 10, 10, 10));
        appModeContainer.getChildren().add(view);

        log.debug("handleModeChanged: {}", appMode.name());
    }

    private void handleDragOverEvent(DragEvent event) {
        log.debug("handleDragOverEvent");

        if (event.getGestureSource() != filesTableView && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    private void updatePreview(FileInformationCommand command) {
        log.debug("updatePreview");
        if (Objects.nonNull(command)) {
            appCoreFunctionalityHelper.processCommand(loadedAppFilesList, command, appProgressBar, result -> {
                loadedAppFilesList.clear();
                loadedAppFilesList.addAll(result);
                filesTableView.setItems(loadedAppFilesList);
            });
        }
    }

    private void handleFileInTableSelectedEvent(RenameModel newSelection) {
        log.debug("handleFileInTableSelectedEvent: {}", newSelection);
        var result = appCoreFunctionalityHelper.mapRenameModelToHtmlString(newSelection);
        setTextToTheFileDetailsView(result);
    }

    private void configurePreviewBtn() {
        log.info("Configuring previewBtn");
        previewBtn.setOnAction(event -> this.handlePreviewBtnClicked());
        previewBtn.setTooltip(new Tooltip(languageTextRetriever.getString(TextKeys.BTN_PREVIEW)));
    }

    private void configureRenameBtn() {
        log.info("Configuring renameBtn");
        renameBtn.setOnAction(event -> this.handleRenameBtnClicked());
        renameBtn.setTooltip(new Tooltip(languageTextRetriever.getString(TextKeys.BTN_RENAME)));
    }

    private void handleRenameBtnClicked() {
        log.debug("handleRenameBtnClicked");
        if (showConfirmationDialog(TextKeys.DIALOG_CONFIRM_CONTENT, TextKeys.DIALOG_CONFIRM_HEADER)) {
            log.debug("handleRenameBtnClicked. Confirmed");
            renameFiles();
        }
        this.configureControlWidgetsState();
    }

    private void handleClearBtnClicked() {
        log.debug("handleClearBtnClicked");
        loadedAppFilesList.clear();
        setTextToTheFileDetailsView("");
        this.configureControlWidgetsState();
    }

    private boolean showConfirmationDialog(TextKeys content, TextKeys title) {
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

    private void setTextToTheFileDetailsView(String text) {
        WebEngine engine = this.fileInfoWebView.getEngine();
        fileInfoWebView.setFontScale(0.7);
        engine.loadContent(text);
    }

    private void renameFiles() {
        log.debug("renameFiles");
        appCoreFunctionalityHelper.renameFiles(loadedAppFilesList, appProgressBar, result -> {
            loadedAppFilesList.clear();
            loadedAppFilesList.addAll(result);
            filesTableView.setItems(loadedAppFilesList);
        });
    }

    private void configureClearBtn() {
        log.info("Configuring clearBtn");
        clearBtn.setOnAction(event -> this.handleClearBtnClicked());
        clearBtn.setTooltip(new Tooltip(languageTextRetriever.getString(TextKeys.BTN_CLEAR)));
    }

    private void handleCommandInTheModeViewUpdated(AppModes modeThatHasUpdates, FileInformationCommand command) {
        log.debug("handleCommandInTheModeViewUpdated Command: {}, Mode: {}", command, modeThatHasUpdates.name());
        if (autoPreviewCheckBox.isSelected()) {
            updatePreview(command);
        }
    }

}
