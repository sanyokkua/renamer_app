package ua.renamer.app.ui.widgets.controllers;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import ua.renamer.app.core.abstracts.AppFileCommand;
import ua.renamer.app.core.abstracts.ListProcessingCommand;
import ua.renamer.app.core.commands.MapFileToAppFileCommand;
import ua.renamer.app.core.enums.AppModes;
import ua.renamer.app.core.model.AppFile;
import ua.renamer.app.ui.ViewUtils;
import ua.renamer.app.ui.abstracts.ControllerApi;
import ua.renamer.app.ui.constants.ViewNames;
import ua.renamer.app.ui.converters.AppModesConverter;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

import static ua.renamer.app.core.utils.AppFileUtils.generateHTML;

public class ApplicationMainViewController implements Initializable {
    private final Map<AppModes, Parent> appModeToViewMap;
    private final Map<AppModes, ControllerApi> appModeToControllerMap;
    private final ObservableList<AppFile> loadedAppFilesList;

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
    private TableView<AppFile> filesTableView;
    @FXML
    private TableColumn<AppFile, String> originalNameColumn;
    @FXML
    private TableColumn<AppFile, String> itemTypeColumn;
    @FXML
    private TableColumn<AppFile, String> newNameColumn;
    @FXML
    private WebView fileInfoWebView;
    @FXML
    private ProgressBar appProgressBar;

    public ApplicationMainViewController() {
        appModeToViewMap = new EnumMap<>(AppModes.class);
        appModeToControllerMap = new EnumMap<>(AppModes.class);
        loadedAppFilesList = FXCollections.observableArrayList();
    }

    private static <I, O> void executeListProcessingCommand(ListProcessingCommand<I, O> command, List<I> items, ProgressBar progressBar, Consumer<List<O>> callback) {
        var optCallback = Optional.ofNullable(callback);

        var runCommandTask = new Task<>() {
            @Override
            protected Void call() {
                var result = command.execute(items, this::updateProgress);
                optCallback.ifPresent(callback -> callback.accept(result));
                updateProgress(0, 0);
                return null;
            }
        };
        progressBar.progressProperty().bind(runCommandTask.progressProperty());

        Thread thread = new Thread(runCommandTask);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
    }

    private void initializeModeViews() {
        var modeAddCustomTextLoader = ViewUtils.createLoader(ViewNames.MODE_ADD_CUSTOM_TEXT);
        var modeChangeCaseLoader = ViewUtils.createLoader(ViewNames.MODE_CHANGE_CASE);
        var modeUseDatetimeLoader = ViewUtils.createLoader(ViewNames.MODE_USE_DATETIME);
        var modeUseImageDimensionsLoader = ViewUtils.createLoader(ViewNames.MODE_USE_IMAGE_DIMENSIONS);
        var modeUseParentFolderNameLoader = ViewUtils.createLoader(ViewNames.MODE_USE_PARENT_FOLDER_NAME);
        var modeRemoveCustomTextLoader = ViewUtils.createLoader(ViewNames.MODE_REMOVE_CUSTOM_TEXT);
        var modeReplaceCustomTextLoader = ViewUtils.createLoader(ViewNames.MODE_REPLACE_CUSTOM_TEXT);
        var modeAddSequenceLoader = ViewUtils.createLoader(ViewNames.MODE_ADD_SEQUENCE);
        var modeTruncateFileNameLoader = ViewUtils.createLoader(ViewNames.MODE_TRUNCATE_FILE_NAME);
        var modeChangeExtensionLoader = ViewUtils.createLoader(ViewNames.MODE_CHANGE_EXTENSION);

        try {
            appModeToViewMap.put(AppModes.ADD_CUSTOM_TEXT, modeAddCustomTextLoader.load());
            appModeToViewMap.put(AppModes.CHANGE_CASE, modeChangeCaseLoader.load());
            appModeToViewMap.put(AppModes.USE_DATETIME, modeUseDatetimeLoader.load());
            appModeToViewMap.put(AppModes.USE_IMAGE_DIMENSIONS, modeUseImageDimensionsLoader.load());
            appModeToViewMap.put(AppModes.USE_PARENT_FOLDER_NAME, modeUseParentFolderNameLoader.load());
            appModeToViewMap.put(AppModes.REMOVE_CUSTOM_TEXT, modeRemoveCustomTextLoader.load());
            appModeToViewMap.put(AppModes.REPLACE_CUSTOM_TEXT, modeReplaceCustomTextLoader.load());
            appModeToViewMap.put(AppModes.ADD_SEQUENCE, modeAddSequenceLoader.load());
            appModeToViewMap.put(AppModes.TRUNCATE_FILE_NAME, modeTruncateFileNameLoader.load());
            appModeToViewMap.put(AppModes.CHANGE_EXTENSION, modeChangeExtensionLoader.load());

            appModeToControllerMap.put(AppModes.ADD_CUSTOM_TEXT, modeAddCustomTextLoader.getController());
            appModeToControllerMap.put(AppModes.CHANGE_CASE, modeChangeCaseLoader.getController());
            appModeToControllerMap.put(AppModes.USE_DATETIME, modeUseDatetimeLoader.getController());
            appModeToControllerMap.put(AppModes.USE_IMAGE_DIMENSIONS, modeUseImageDimensionsLoader.getController());
            appModeToControllerMap.put(AppModes.USE_PARENT_FOLDER_NAME, modeUseParentFolderNameLoader.getController());
            appModeToControllerMap.put(AppModes.REMOVE_CUSTOM_TEXT, modeRemoveCustomTextLoader.getController());
            appModeToControllerMap.put(AppModes.REPLACE_CUSTOM_TEXT, modeReplaceCustomTextLoader.getController());
            appModeToControllerMap.put(AppModes.ADD_SEQUENCE, modeAddSequenceLoader.getController());
            appModeToControllerMap.put(AppModes.TRUNCATE_FILE_NAME, modeTruncateFileNameLoader.getController());
            appModeToControllerMap.put(AppModes.CHANGE_EXTENSION, modeChangeExtensionLoader.getController());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load view", e);
        }
    }

    private void configureModeChoiceBox() {
        appModeChoiceBox.getItems().addAll(AppModes.values());
        appModeChoiceBox.setValue(AppModes.ADD_CUSTOM_TEXT);
        appModeChoiceBox.setConverter(new AppModesConverter());
    }

    private void configureFilesTableView() {
        filesTableView.setOnDragOver(event -> {
            if (event.getGestureSource() != filesTableView && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        filesTableView.setOnDragDropped(event -> {
            var dragboard = event.getDragboard();
            var success = false;
            if (dragboard.hasFiles()) {
                MapFileToAppFileCommand command = new MapFileToAppFileCommand();
                executeListProcessingCommand(command, dragboard.getFiles(), appProgressBar,
                        result -> {
                            loadedAppFilesList.addAll(result);
                            configureControlWidgetsState();
                        });
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();

            configureControlWidgetsState();
        });

        filesTableView.setItems(loadedAppFilesList);

        filesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                var result = generateHTML(newSelection);
                WebEngine engine = this.fileInfoWebView.getEngine();
                fileInfoWebView.setFontScale(0.7);
                engine.loadContent(result);
            }
        });
    }

    private void configureFilesTableViewColumns() {
        originalNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFileName()));
        itemTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        newNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNextName()));
    }

    private void configureAutoPreviewCheckbox() {
        BooleanProperty selectedProperty = autoPreviewCheckBox.selectedProperty();
        selectedProperty.addListener((observable, oldValue, newValue) -> this.handleAutoPreviewChanged(newValue));
    }

    private void configurePreviewBtn() {
        previewBtn.setOnAction(event -> this.handlePreviewBtnClicked());
    }

    private void configureRenameBtn() {
        renameBtn.setOnAction(event -> this.handleRenameBtnClicked());
    }

    private void configureClearBtn() {
        clearBtn.setOnAction(event -> this.handleClearBtnClicked());
    }

    private void configureProgressBar() {
        appProgressBar.setProgress(0);
    }

    private void configureModeCommandChangedListener() {
        appModeToControllerMap.forEach((key, value) ->
                value.commandProperty().addListener((observable, oldValue, newValue) ->
                        this.handleCommandInTheModeViewUpdated(key, newValue)));
    }

    private void configureControlWidgetsState() {
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
        AppModes appMode = appModeChoiceBox.getValue();

        appModeContainer.getChildren().clear();
        var view = appModeToViewMap.get(appMode);
        appModeContainer.getChildren().add(view);

        System.out.println("handleModeChanged: " + appMode.name());
    }

    private void handleAutoPreviewChanged(boolean isChecked) {
        System.out.println("handleAutoPreviewChanged.isChecked: " + isChecked);
        this.configureControlWidgetsState();
    }

    private void handlePreviewBtnClicked() {
        System.out.println("handlePreviewBtnClicked");
        AppModes appMode = appModeChoiceBox.getValue();
        var controller = appModeToControllerMap.get(appMode);

        System.out.println("handlePreviewBtnClicked. AppMode: " + appMode.name());

        if (controller != null) {
            var cmd = controller.getCommand();
            System.out.println("cmd: " + cmd);
            updatePreview(cmd);
        }

        this.configureControlWidgetsState();
    }

    private void handleRenameBtnClicked() {
        System.out.println("handleRenameBtnClicked");
        this.configureControlWidgetsState();
    }

    private void handleClearBtnClicked() {
        System.out.println("handleClearBtnClicked");
        loadedAppFilesList.clear();
        this.configureControlWidgetsState();
    }

    private void handleCommandInTheModeViewUpdated(AppModes modeThatHasUpdates, AppFileCommand command) {
        System.out.println("handleCommandInTheModeViewUpdated: " + modeThatHasUpdates.name());
        System.out.println("Command: " + command);

        if (autoPreviewCheckBox.isSelected()) {
            updatePreview(command);
        }
    }

    private void updatePreview(AppFileCommand command) {
        executeListProcessingCommand(command, loadedAppFilesList, appProgressBar,
                result -> {
                    loadedAppFilesList.clear();
                    loadedAppFilesList.addAll(result);
                    filesTableView.setItems(loadedAppFilesList);
                });
    }
}
