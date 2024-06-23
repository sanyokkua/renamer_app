package ua.renamer.app.ui.controller;

import com.google.inject.Inject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.enums.AppModes;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.core.service.command.FileInformationCommand;
import ua.renamer.app.ui.converter.AppModesConverter;
import ua.renamer.app.ui.enums.TableStyles;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.impl.CoreFunctionalityHelper;
import ua.renamer.app.ui.service.impl.MainViewControllerHelper;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ApplicationMainViewController implements Initializable {

    private final CoreFunctionalityHelper coreHelper;
    private final MainViewControllerHelper mainControllerHelper;
    private final AppModesConverter appModesConverter;
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
    private Button reloadBtn;
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
    private boolean areFilesRenamed = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing ApplicationMainViewController");
        configureModeChoiceBox();
        configureFilesTableView();
        configureFilesTableViewColumns();
        configureAutoPreviewCheckbox();
        configurePreviewBtn();
        configureRenameBtn();
        configureClearBtn();
        configureReloadBtn();
        configureProgressBar();
        configureModeCommandChangedListener();
        configureControlWidgetsState();
        handleModeChanged(); // Select default view
    }

    private void configureModeChoiceBox() {
        log.info("Configuring modeChoiceBox");
        appModeChoiceBox.getItems().addAll(AppModes.values());
        appModeChoiceBox.setValue(AppModes.ADD_CUSTOM_TEXT);
        appModeChoiceBox.setConverter(appModesConverter);
        appModeChoiceBox.setOnAction((event -> handleModeChanged()));
    }

    private void configureFilesTableView() {
        log.info("Configuring filesTableView");
        filesTableView.setOnDragOver(this::handleFilesTableViewDragOverEvent);
        filesTableView.setOnDragDropped(this::handleFilesTableViewFilesDroppedEvent);
        filesTableView.setItems(loadedAppFilesList);
        filesTableView.setContextMenu(mainControllerHelper.createTableContextMenu(filesTableView));
        filesTableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(RenameModel renameModel, boolean empty) {
                super.updateItem(renameModel, empty);
                if (renameModel == null || empty) {
                    setStyle("");
                } else {
                    if (renameModel.isRenamed()) {
                        setStyle(TableStyles.RENAMED.getStyle());
                    } else if (renameModel.isNeedRename() && !renameModel.isHasRenamingError()) {
                        setStyle(TableStyles.READY_FOR_RENAMING.getStyle());
                    } else if (renameModel.isHasRenamingError()) {
                        setStyle(TableStyles.HAS_ERROR.getStyle());
                    } else {
                        setStyle(TableStyles.BLANK.getStyle());
                    }
                }
            }
        });

        var tableWidthProperty = filesTableView.widthProperty();
        var tableSelectionModel = filesTableView.getSelectionModel();
        var tableSelectedItemProperty = tableSelectionModel.selectedItemProperty();

        tableWidthProperty.addListener((obs, oldVal, newVal) -> handleFilesTableViewResize(newVal));
        tableSelectedItemProperty.addListener((obs, oldVal, newVal) -> handleFileInTableSelectedEvent(newVal));
    }

    private void configureFilesTableViewColumns() {
        log.info("Configuring filesTableViewColumns");
        originalNameColumn.setCellValueFactory(mainControllerHelper.createCellValueFactory(coreHelper::getOldName));
        itemTypeColumn.setCellValueFactory(mainControllerHelper.createCellValueFactory(coreHelper::getFileType));
        newNameColumn.setCellValueFactory(mainControllerHelper.createCellValueFactory(coreHelper::getNewName));
        statusColumn.setCellValueFactory(mainControllerHelper.createCellValueFactory(coreHelper::getFileStatus));
    }

    private void configureAutoPreviewCheckbox() {
        log.info("Configuring autoPreviewCheckbox");
        var selectedProperty = autoPreviewCheckBox.selectedProperty();
        selectedProperty.addListener((obs, oldVal, newVal) -> handleCheckboxAutoPreviewChanged(newVal));
        autoPreviewCheckBox.setTooltip(mainControllerHelper.createTooltip(TextKeys.CHECK_BOX_AUTO_PREVIEW));
    }

    private void configurePreviewBtn() {
        log.info("Configuring previewBtn");
        previewBtn.setOnAction(event -> handleBtnClickedPreview());
        previewBtn.setTooltip(mainControllerHelper.createTooltip(TextKeys.BTN_PREVIEW));
    }

    private void configureRenameBtn() {
        log.info("Configuring renameBtn");
        renameBtn.setOnAction(event -> handleBtnClickedRename());
        renameBtn.setTooltip(mainControllerHelper.createTooltip(TextKeys.BTN_RENAME));
    }

    private void configureClearBtn() {
        log.info("Configuring clearBtn");
        clearBtn.setOnAction(event -> handleBtnClickedClear());
        clearBtn.setTooltip(mainControllerHelper.createTooltip(TextKeys.BTN_CLEAR));
    }

    private void configureReloadBtn() {
        log.info("Configuring reloadBtn");
        reloadBtn.setOnAction(event -> handleBtnClickedReload());
        reloadBtn.setTooltip(mainControllerHelper.createTooltip(TextKeys.BTN_RELOAD));
    }

    private void configureProgressBar() {
        log.info("Configuring progressBar");
        appProgressBar.setProgress(0);
    }

    private void configureModeCommandChangedListener() {
        log.info("Configuring modeCommandChangedListener");
        Stream.of(AppModes.values()).forEach(item -> {
            var controller = mainControllerHelper.getControllerForAppMode(item);
            var property = controller.commandProperty();
            property.addListener((obs, oldVal, newVal) -> handleModeControllerCommandChanged(item, newVal));
        });
    }

    private void configureControlWidgetsState() {
        log.info("Configuring controlWidgetsState");
        reloadBtn.setVisible(areFilesRenamed);

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
            renameBtn.setDisable(reloadBtn.isVisible());
        }

        if (!loadedAppFilesList.isEmpty()) {
            previewBtn.setDisable(autoPreviewCheckBox.isSelected());
        }
    }

    @FXML
    private void handleModeChanged() {
        log.debug("handleModeChanged");

        var mode = appModeChoiceBox.getValue();
        var view = mainControllerHelper.getViewForAppMode(mode);
        StackPane.setMargin(view, new Insets(10, 10, 10, 10));

        appModeContainer.getChildren().clear();
        appModeContainer.getChildren().add(view);

        log.debug("handleModeChanged: {}", mode.name());

        var controllerForAppMode = mainControllerHelper.getControllerForAppMode(mode);
        var command = controllerForAppMode.getCommand();

        if (!areFilesRenamed) {
            coreHelper.resetModels(loadedAppFilesList, command, appProgressBar, resultList -> {
                loadedAppFilesList.clear();
                loadedAppFilesList.addAll(resultList);
                filesTableView.setItems(loadedAppFilesList);
            });
        }
    }

    private void handleFilesTableViewDragOverEvent(DragEvent event) {
        log.debug("handleDragOverEvent");

        if (event.getGestureSource() != filesTableView && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    private void handleFilesTableViewFilesDroppedEvent(DragEvent event) {
        log.debug("handleFilesDroppedEvent");

        var dragboard = event.getDragboard();
        var success = false;
        if (dragboard.hasFiles()) {
            coreHelper.mapFileToRenameModel(dragboard.getFiles(), appProgressBar, result -> {
                loadedAppFilesList.addAll(result);
                configureControlWidgetsState();
            });
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();

        configureControlWidgetsState();
    }

    private void handleFilesTableViewResize(Number newWidth) {
        double availableWidth = newWidth.doubleValue() - itemTypeColumn.getWidth();
        double columnWidth = availableWidth / 3;
        originalNameColumn.setPrefWidth(columnWidth);
        newNameColumn.setPrefWidth(columnWidth);
        statusColumn.setPrefWidth(columnWidth);
    }

    private void handleFileInTableSelectedEvent(RenameModel currentModel) {
        log.debug("handleFileInTableSelectedEvent: {}", currentModel);
        var result = coreHelper.mapRenameModelToHtmlString(currentModel);
        setTextToTheFileDetailsView(result);
    }

    private void handleCheckboxAutoPreviewChanged(boolean isChecked) {
        log.debug("handleAutoPreviewChanged: {}", isChecked);
        if (isChecked) {
            handleBtnClickedPreview();
        }
        configureControlWidgetsState();
    }

    private void handleBtnClickedPreview() {
        log.debug("handlePreviewBtnClicked");
        var mode = appModeChoiceBox.getValue();
        var controller = mainControllerHelper.getControllerForAppMode(mode);

        log.debug("handlePreviewBtnClicked. AppMode: {}", mode.name());

        if (Objects.nonNull(controller)) {
            var cmd = controller.getCommand();
            log.debug("handlePreviewBtnClicked. Command: {}", cmd);
            updatePreview(cmd);
        }

        configureControlWidgetsState();
    }

    private void updatePreview(FileInformationCommand command) {
        log.debug("updatePreview");
        if (Objects.nonNull(command) && !areFilesRenamed) {
            coreHelper.prepareFiles(loadedAppFilesList, command, appProgressBar, result -> {
                loadedAppFilesList.clear();
                loadedAppFilesList.addAll(result);
                filesTableView.setItems(loadedAppFilesList);
            });
        }
    }

    private void handleBtnClickedRename() {
        log.debug("handleRenameBtnClicked");
        if (mainControllerHelper.showConfirmationDialog(TextKeys.DIALOG_CONFIRM_CONTENT,
                                                        TextKeys.DIALOG_CONFIRM_HEADER)) {
            log.debug("handleRenameBtnClicked. Confirmed");
            coreHelper.renameFiles(loadedAppFilesList, appProgressBar, result -> {
                loadedAppFilesList.clear();
                loadedAppFilesList.addAll(result);
                filesTableView.setItems(loadedAppFilesList);
                areFilesRenamed = true;
                configureControlWidgetsState();
            });
        }
    }

    private void handleBtnClickedClear() {
        log.debug("handleClearBtnClicked");
        loadedAppFilesList.clear();
        areFilesRenamed = false;
        setTextToTheFileDetailsView("");
        configureControlWidgetsState();
    }

    private void handleBtnClickedReload() {
        log.debug("handleBtnClickedReload");
        coreHelper.reloadFiles(loadedAppFilesList, appProgressBar, result -> {
            loadedAppFilesList.clear();
            loadedAppFilesList.addAll(result);
            filesTableView.setItems(loadedAppFilesList);
            areFilesRenamed = false;
            configureControlWidgetsState();
        });
    }

    private void handleModeControllerCommandChanged(AppModes modeThatHasUpdates, FileInformationCommand command) {
        log.debug("handleCommandInTheModeViewUpdated Command: {}, Mode: {}", command, modeThatHasUpdates.name());
        if (autoPreviewCheckBox.isSelected()) {
            updatePreview(command);
        }
    }

    private void setTextToTheFileDetailsView(String text) {
        var engine = fileInfoWebView.getEngine();
        fileInfoWebView.setFontScale(0.7);
        engine.loadContent(text);
    }

}