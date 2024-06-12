package ua.renamer.app.ui.controller;

import com.google.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.enums.AppModes;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.core.service.command.FileInformationCommand;
import ua.renamer.app.core.service.command.ListProcessingCommand;
import ua.renamer.app.core.service.command.impl.MapFileInformationToRenameModel;
import ua.renamer.app.core.service.command.impl.MapFileToFileInformation;
import ua.renamer.app.core.service.command.impl.RenameCommand;
import ua.renamer.app.core.service.mapper.impl.FileInformationToHtmlMapper;
import ua.renamer.app.ui.controller.mode.ModeControllerApi;
import ua.renamer.app.ui.converter.AppModesConverter;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.enums.ViewNames;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;
import ua.renamer.app.ui.service.ViewLoaderApi;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Slf4j
public class ApplicationMainViewController implements Initializable {

    private final FileInformationToHtmlMapper fileInformationToHtmlMapper;
    private final MapFileToFileInformation mapFileToFileInformation;
    private final MapFileInformationToRenameModel mapFileInformationToRenameModel;
    private final RenameCommand renameCommand;
    private final ViewLoaderApi viewLoaderApi;
    private final AppModesConverter appModesConverter;
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
    private WebView fileInfoWebView;
    @FXML
    private ProgressBar appProgressBar;

    @Inject
    public ApplicationMainViewController(FileInformationToHtmlMapper fileInformationToHtmlMapper,
                                         MapFileToFileInformation mapFileToFileInformation,
                                         MapFileInformationToRenameModel mapFileInformationToRenameModel,
                                         RenameCommand renameCommand, ViewLoaderApi viewLoaderApi,
                                         AppModesConverter appModesConverter,
                                         LanguageTextRetrieverApi languageTextRetriever) {
        this.fileInformationToHtmlMapper = fileInformationToHtmlMapper;
        this.mapFileToFileInformation = mapFileToFileInformation;
        this.mapFileInformationToRenameModel = mapFileInformationToRenameModel;
        this.renameCommand = renameCommand;
        this.viewLoaderApi = viewLoaderApi;
        this.appModesConverter = appModesConverter;
        this.languageTextRetriever = languageTextRetriever;

        appModeToViewMap = new EnumMap<>(AppModes.class);
        appModeToControllerMap = new EnumMap<>(AppModes.class);
        loadedAppFilesList = FXCollections.observableArrayList();
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

        var viewLoaderIsMissed = Stream.of(modeAddCustomTextLoader,
                                           modeChangeCaseLoader,
                                           modeUseDatetimeLoader,
                                           modeUseImageDimensionsLoader,
                                           modeUseParentFolderNameLoader,
                                           modeRemoveCustomTextLoader,
                                           modeReplaceCustomTextLoader,
                                           modeAddSequenceLoader,
                                           modeTruncateFileNameLoader,
                                           modeChangeExtensionLoader).anyMatch(Optional::isEmpty);
        if (viewLoaderIsMissed) {
            throw new IllegalStateException("At least one of ViewLoaders is missing");
        }

        try {
            // @formatter:off
            appModeToViewMap.put(AppModes.ADD_CUSTOM_TEXT, modeAddCustomTextLoader.get().load());
            appModeToViewMap.put(AppModes.CHANGE_CASE, modeChangeCaseLoader.get().load());
            appModeToViewMap.put(AppModes.USE_DATETIME, modeUseDatetimeLoader.get().load());
            appModeToViewMap.put(AppModes.USE_IMAGE_DIMENSIONS, modeUseImageDimensionsLoader.get().load());
            appModeToViewMap.put(AppModes.USE_PARENT_FOLDER_NAME, modeUseParentFolderNameLoader.get().load());
            appModeToViewMap.put(AppModes.REMOVE_CUSTOM_TEXT, modeRemoveCustomTextLoader.get().load());
            appModeToViewMap.put(AppModes.REPLACE_CUSTOM_TEXT, modeReplaceCustomTextLoader.get().load());
            appModeToViewMap.put(AppModes.ADD_SEQUENCE, modeAddSequenceLoader.get().load());
            appModeToViewMap.put(AppModes.TRUNCATE_FILE_NAME, modeTruncateFileNameLoader.get().load());
            appModeToViewMap.put(AppModes.CHANGE_EXTENSION, modeChangeExtensionLoader.get().load());

            appModeToControllerMap.put(AppModes.ADD_CUSTOM_TEXT, modeAddCustomTextLoader.get().getController());
            appModeToControllerMap.put(AppModes.CHANGE_CASE, modeChangeCaseLoader.get().getController());
            appModeToControllerMap.put(AppModes.USE_DATETIME, modeUseDatetimeLoader.get().getController());
            appModeToControllerMap.put(AppModes.USE_IMAGE_DIMENSIONS, modeUseImageDimensionsLoader.get().getController());
            appModeToControllerMap.put(AppModes.USE_PARENT_FOLDER_NAME, modeUseParentFolderNameLoader.get().getController());
            appModeToControllerMap.put(AppModes.REMOVE_CUSTOM_TEXT, modeRemoveCustomTextLoader.get().getController());
            appModeToControllerMap.put(AppModes.REPLACE_CUSTOM_TEXT, modeReplaceCustomTextLoader.get().getController());
            appModeToControllerMap.put(AppModes.ADD_SEQUENCE, modeAddSequenceLoader.get().getController());
            appModeToControllerMap.put(AppModes.TRUNCATE_FILE_NAME, modeTruncateFileNameLoader.get().getController());
            appModeToControllerMap.put(AppModes.CHANGE_EXTENSION, modeChangeExtensionLoader.get().getController());
            // @formatter:on
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

    private void configureModeCommandChangedListener() {
        log.info("Configuring modeCommandChangedListener");
        appModeToControllerMap.forEach((key, value) -> value.commandProperty()
                                                            .addListener((observable, oldValue, newValue) -> this.handleCommandInTheModeViewUpdated(
                                                                    key,
                                                                    newValue)));
    }

    public String formatFileType(RenameModel fileInformation) {
        return fileInformation.getFileInformation().isFile()
                ? languageTextRetriever.getString(TextKeys.TYPE_FILE)
                : languageTextRetriever.getString(TextKeys.TYPE_FOLDER);
    }

    public String formatOriginalFileName(RenameModel fileInformation) {
        return fileInformation.getOldName();
    }

    private void configureModeChoiceBox() {
        log.info("Configuring modeChoiceBox");
        appModeChoiceBox.getItems().addAll(AppModes.values());
        appModeChoiceBox.setValue(AppModes.ADD_CUSTOM_TEXT);
        appModeChoiceBox.setConverter(appModesConverter);
        appModeChoiceBox.setOnAction((event -> this.handleModeChanged()));
    }

    private void configureFilesTableView() {
        log.info("Configuring filesTableView");
        filesTableView.setOnDragOver(this::handleDragOverEvent);
        filesTableView.setOnDragDropped(this::handleFilesDroppedEvent);
        filesTableView.setItems(loadedAppFilesList);
        filesTableView.getSelectionModel()
                      .selectedItemProperty()
                      .addListener((obs, oldSelection, newSelection) -> handleFileInTableSelectedEvent(newSelection));

        filesTableView.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double availableWidth = newWidth.doubleValue() - itemTypeColumn.getWidth();
            double columnWidth = availableWidth / 2;
            originalNameColumn.setPrefWidth(columnWidth);
            newNameColumn.setPrefWidth(columnWidth);
        });
    }

    private void configureFilesTableViewColumns() {
        log.info("Configuring filesTableViewColumns");
        originalNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(formatOriginalFileName(cellData.getValue())));
        itemTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(formatFileType(cellData.getValue())));
        newNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(formatNextFileName(cellData.getValue())));
    }

    private void handleFilesDroppedEvent(DragEvent event) {
        log.debug("handleFilesDroppedEvent");

        var dragboard = event.getDragboard();
        var success = false;
        if (dragboard.hasFiles()) {
            executeListProcessingCommand(mapFileToFileInformation, dragboard.getFiles(), appProgressBar, result -> {
                loadedAppFilesList.addAll(mapFileInformationToRenameModel.execute(result, null));
                configureControlWidgetsState();
            });
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();

        configureControlWidgetsState();
    }

    private static <I, O> void executeListProcessingCommand(ListProcessingCommand<I, O> command, List<I> items,
                                                            ProgressBar progressBar, Consumer<List<O>> callback) {
        log.debug("Executing list processing command: {}", command);

        var optCallback = Optional.ofNullable(callback);

        var runCommandTask = new Task<>() {
            @Override
            protected Void call() {
                log.debug("Background task is started");

                var result = command.execute(items, this::updateProgress);
                optCallback.ifPresent(callback -> callback.accept(result));
                updateProgress(0, 0);

                log.debug("Background task is finished");
                return null;
            }
        };
        progressBar.progressProperty().bind(runCommandTask.progressProperty());

        Thread thread = new Thread(runCommandTask);
        thread.setDaemon(true);
        thread.start();
    }

    public String formatNextFileName(RenameModel fileInformation) {
        return fileInformation.getNewName();
    }

    private void configureAutoPreviewCheckbox() {
        log.info("Configuring autoPreviewCheckbox");
        BooleanProperty selectedProperty = autoPreviewCheckBox.selectedProperty();
        selectedProperty.addListener((observable, oldValue, newValue) -> this.handleAutoPreviewChanged(newValue));
    }

    private void configurePreviewBtn() {
        log.info("Configuring previewBtn");
        previewBtn.setOnAction(event -> this.handlePreviewBtnClicked());
    }

    private void configureRenameBtn() {
        log.info("Configuring renameBtn");
        renameBtn.setOnAction(event -> this.handleRenameBtnClicked());
    }

    private void configureClearBtn() {
        log.info("Configuring clearBtn");
        clearBtn.setOnAction(event -> this.handleClearBtnClicked());
    }

    private void configureProgressBar() {
        log.info("Configuring progressBar");
        appProgressBar.setProgress(0);
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

    private void handleDragOverEvent(DragEvent event) {
        log.debug("handleDragOverEvent");

        if (event.getGestureSource() != filesTableView && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    private void handleRenameBtnClicked() {
        log.debug("handleRenameBtnClicked");
        var confirmed = showConfirmationDialog(languageTextRetriever.getString(TextKeys.DIALOG_CONFIRM_CONTENT),
                                               languageTextRetriever.getString(TextKeys.DIALOG_CONFIRM_HEADER));
        if (confirmed) {
            log.debug("handleRenameBtnClicked. Confirmed");
            renameFiles();
        }
        this.configureControlWidgetsState();
    }

    private void handleFileInTableSelectedEvent(RenameModel newSelection) {
        log.debug("handleFileInTableSelectedEvent: {}", newSelection);

        if (newSelection != null) {
            var result = fileInformationToHtmlMapper.map(newSelection);
            setTextToTheFileDetailsView(result);
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

    private void handleAutoPreviewChanged(boolean isChecked) {
        log.debug("handleAutoPreviewChanged: {}", isChecked);
        this.configureControlWidgetsState();
    }

    private void handlePreviewBtnClicked() {
        log.debug("handlePreviewBtnClicked");
        AppModes appMode = appModeChoiceBox.getValue();
        var controller = appModeToControllerMap.get(appMode);

        log.debug("handlePreviewBtnClicked. AppMode: {}", appMode.name());

        if (controller != null) {
            var cmd = controller.getCommand();
            log.debug("handlePreviewBtnClicked. Command: {}", cmd);
            updatePreview(cmd);
        }

        this.configureControlWidgetsState();
    }

    private boolean showConfirmationDialog(String message, String title) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        var confirmButton = new ButtonType(languageTextRetriever.getString(TextKeys.DIALOG_CONFIRM_BTN_OK));
        var cancelButton = new ButtonType(languageTextRetriever.getString(TextKeys.DIALOG_CONFIRM_BTN_CANCEL));

        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        alert.showAndWait();

        return alert.getResult() == confirmButton;
    }

    private void handleClearBtnClicked() {
        log.debug("handleClearBtnClicked");
        loadedAppFilesList.clear();
        setTextToTheFileDetailsView("");
        this.configureControlWidgetsState();
    }

    private void handleCommandInTheModeViewUpdated(AppModes modeThatHasUpdates, FileInformationCommand command) {
        log.debug("handleCommandInTheModeViewUpdated Command: {}", command);
        log.debug("handleCommandInTheModeViewUpdated Mode: {}", modeThatHasUpdates.name());

        if (autoPreviewCheckBox.isSelected()) {
            updatePreview(command);
        }
    }

    private void updatePreview(FileInformationCommand command) {
        log.debug("updatePreview");
        executeListProcessingCommand(command,
                                     loadedAppFilesList.stream().map(RenameModel::getFileInformation).toList(),
                                     appProgressBar,
                                     result -> {
                                         loadedAppFilesList.clear();
                                         loadedAppFilesList.addAll(mapFileInformationToRenameModel.execute(result,
                                                                                                           null));
                                         filesTableView.setItems(loadedAppFilesList);
                                     });
    }

    private void setTextToTheFileDetailsView(String text) {
        WebEngine engine = this.fileInfoWebView.getEngine();
        fileInfoWebView.setFontScale(0.7);
        engine.loadContent(text);
    }

    private void renameFiles() {
        log.debug("renameFiles");
        // TODO: implement
    }

}
