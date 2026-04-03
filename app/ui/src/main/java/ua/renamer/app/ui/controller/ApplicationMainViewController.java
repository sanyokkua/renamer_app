package ua.renamer.app.ui.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
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
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.*;
import ua.renamer.app.core.enums.AppModes;
import ua.renamer.app.ui.controller.mode.ModeControllerV2Api;
import ua.renamer.app.ui.converter.AppModesConverter;
import ua.renamer.app.ui.enums.TableStyles;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;
import ua.renamer.app.ui.state.FxStateMirror;
import ua.renamer.app.ui.view.ModeViewRegistry;
import ua.renamer.app.ui.widget.table.TableCustomContextMenu;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ApplicationMainViewController implements Initializable {

    private final AppModesConverter appModesConverter;
    private final SessionApi sessionApi;
    private final FxStateMirror fxStateMirror;
    private final ModeViewRegistry modeViewRegistry;
    private final LanguageTextRetrieverApi languageTextRetriever;

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
    private TableView<RenamePreview> filesTableView;
    @FXML
    private TableColumn<RenamePreview, String> originalNameColumn;
    @FXML
    private TableColumn<RenamePreview, String> itemTypeColumn;
    @FXML
    private TableColumn<RenamePreview, String> newNameColumn;
    @FXML
    private TableColumn<RenamePreview, String> statusColumn;
    @FXML
    private WebView fileInfoWebView;
    @FXML
    private ProgressBar appProgressBar;

    private boolean areFilesRenamed = false;
    private ModeApi<?> currentModeApi;
    private Map<String, RenameCandidate> candidatesByFileId = new HashMap<>();
    private Map<String, RenameSessionResult> renameResultsByFileId = new HashMap<>();

    @SuppressWarnings("unchecked")
    private static <P extends ModeParameters> void callBind(ModeControllerV2Api<?> ctrl, ModeApi<?> api) {
        ((ModeControllerV2Api<P>) ctrl).bind((ModeApi<P>) api);
    }

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
        configureFxStateMirrorListeners();
        configureControlWidgetsState();
        handleModeChanged();
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
        filesTableView.setItems(fxStateMirror.preview());
        filesTableView.setContextMenu(new TableCustomContextMenu(filesTableView));
        filesTableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(RenamePreview preview, boolean empty) {
                super.updateItem(preview, empty);
                if (preview == null || empty) {
                    setStyle("");
                } else if (preview.hasError()) {
                    setStyle(TableStyles.HAS_ERROR.getStyle());
                } else if (preview.newName() != null && !preview.newName().equals(preview.originalName())) {
                    setStyle(TableStyles.READY_FOR_RENAMING.getStyle());
                } else {
                    setStyle(TableStyles.BLANK.getStyle());
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
        originalNameColumn.setCellValueFactory(
                cd -> new SimpleStringProperty(cd.getValue().originalName()));
        itemTypeColumn.setCellValueFactory(cd -> {
            var candidate = candidatesByFileId.get(cd.getValue().fileId());
            if (candidate == null) return new SimpleStringProperty("");
            boolean isFile = !java.nio.file.Files.isDirectory(candidate.path());
            var key = isFile ? TextKeys.TYPE_FILE : TextKeys.TYPE_FOLDER;
            return new SimpleStringProperty(languageTextRetriever.getString(key));
        });
        newNameColumn.setCellValueFactory(cd -> {
            var p = cd.getValue();
            return new SimpleStringProperty(p.newName() != null ? p.newName() : "");
        });
        statusColumn.setCellValueFactory(cd -> {
            var p = cd.getValue();
            if (p.hasError()) {
                return new SimpleStringProperty(
                        languageTextRetriever.getString(TextKeys.NOT_RENAMED_BECAUSE_OF_ERROR));
            }
            var result = renameResultsByFileId.get(p.fileId());
            if (result != null) {
                return switch (result.status()) {
                    case SUCCESS -> new SimpleStringProperty(
                            languageTextRetriever.getString(TextKeys.RENAMED_WITHOUT_ERRORS));
                    case SKIPPED -> new SimpleStringProperty(
                            languageTextRetriever.getString(TextKeys.NOT_RENAMED_BECAUSE_NOT_NEEDED));
                    default -> new SimpleStringProperty(
                            languageTextRetriever.getString(TextKeys.NOT_RENAMED_BECAUSE_OF_ERROR));
                };
            }
            if (p.newName() != null && !p.newName().equals(p.originalName())) {
                return new SimpleStringProperty(
                        languageTextRetriever.getString(TextKeys.NO_ACTIONS_HAPPEN));
            }
            return new SimpleStringProperty(
                    languageTextRetriever.getString(TextKeys.NOT_RENAMED_BECAUSE_NOT_NEEDED));
        });
    }

    private void configureAutoPreviewCheckbox() {
        log.info("Configuring autoPreviewCheckbox");
        autoPreviewCheckBox.setDisable(true);
        autoPreviewCheckBox.setTooltip(
                new Tooltip(languageTextRetriever.getString(TextKeys.CHECK_BOX_AUTO_PREVIEW)));
    }

    private void configurePreviewBtn() {
        log.info("Configuring previewBtn");
        previewBtn.setOnAction(event -> handleBtnClickedPreview());
        previewBtn.setTooltip(new Tooltip(languageTextRetriever.getString(TextKeys.BTN_PREVIEW)));
    }

    private void configureRenameBtn() {
        log.info("Configuring renameBtn");
        renameBtn.setOnAction(event -> handleBtnClickedRename());
        renameBtn.setTooltip(new Tooltip(languageTextRetriever.getString(TextKeys.BTN_RENAME)));
    }

    private void configureClearBtn() {
        log.info("Configuring clearBtn");
        clearBtn.setOnAction(event -> handleBtnClickedClear());
        clearBtn.setTooltip(new Tooltip(languageTextRetriever.getString(TextKeys.BTN_CLEAR)));
    }

    private void configureReloadBtn() {
        log.info("Configuring reloadBtn");
        reloadBtn.setOnAction(event -> handleBtnClickedReload());
        reloadBtn.setTooltip(new Tooltip(languageTextRetriever.getString(TextKeys.BTN_RELOAD)));
    }

    private void configureProgressBar() {
        log.info("Configuring progressBar");
        appProgressBar.setProgress(0);
    }

    private void configureFxStateMirrorListeners() {
        log.info("Configuring FxStateMirror listeners (V2 path)");
        fxStateMirror.files().addListener((ListChangeListener<RenameCandidate>) change -> {
            candidatesByFileId = fxStateMirror.files().stream()
                    .collect(Collectors.toMap(RenameCandidate::fileId, c -> c));
            configureControlWidgetsState();
        });
        fxStateMirror.renameResults().addListener((ListChangeListener<RenameSessionResult>) change -> {
            if (!fxStateMirror.renameResults().isEmpty()) {
                renameResultsByFileId = fxStateMirror.renameResults().stream()
                        .collect(Collectors.toMap(RenameSessionResult::fileId, r -> r));
                areFilesRenamed = true;
                configureControlWidgetsState();
                filesTableView.refresh();
            }
        });
    }

    private void configureControlWidgetsState() {
        log.info("Configuring controlWidgetsState");
        reloadBtn.setVisible(areFilesRenamed);

        if (fxStateMirror.files().isEmpty()) {
            previewBtn.setDisable(true);
            renameBtn.setDisable(true);
            clearBtn.setDisable(true);
        } else {
            previewBtn.setDisable(false);
            renameBtn.setDisable(false);
            clearBtn.setDisable(false);
            renameBtn.setDisable(reloadBtn.isVisible());
        }
    }

    @FXML
    private void handleModeChanged() {
        log.debug("handleModeChanged");
        var mode = appModeChoiceBox.getValue();
        var transformationMode = toTransformationMode(mode);

        modeViewRegistry.getView(transformationMode).ifPresent(view -> {
            StackPane.setMargin(view, new Insets(10, 10, 10, 10));
            appModeContainer.getChildren().clear();
            appModeContainer.getChildren().add(view);
        });

        log.debug("handleModeChanged: {}", mode.name());

        modeViewRegistry.getController(transformationMode).ifPresent(v2ctrl ->
                sessionApi.selectMode(v2ctrl.supportedMode())
                        .thenAcceptAsync(modeApi -> {
                            currentModeApi = modeApi;
                            callBind(v2ctrl, modeApi);
                        }, Platform::runLater)
        );
    }

    private TransformationMode toTransformationMode(AppModes mode) {
        return switch (mode) {
            case ADD_CUSTOM_TEXT -> TransformationMode.ADD_TEXT;
            case CHANGE_CASE -> TransformationMode.CHANGE_CASE;
            case USE_DATETIME -> TransformationMode.USE_DATETIME;
            case USE_IMAGE_DIMENSIONS -> TransformationMode.USE_IMAGE_DIMENSIONS;
            case USE_PARENT_FOLDER_NAME -> TransformationMode.USE_PARENT_FOLDER_NAME;
            case REMOVE_CUSTOM_TEXT -> TransformationMode.REMOVE_TEXT;
            case REPLACE_CUSTOM_TEXT -> TransformationMode.REPLACE_TEXT;
            case ADD_SEQUENCE -> TransformationMode.ADD_SEQUENCE;
            case TRUNCATE_FILE_NAME -> TransformationMode.TRUNCATE_FILE_NAME;
            case CHANGE_EXTENSION -> TransformationMode.CHANGE_EXTENSION;
        };
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
            var paths = dragboard.getFiles().stream()
                    .map(java.io.File::toPath)
                    .toList();
            appProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            sessionApi.addFiles(paths).thenRunAsync(() -> {
                appProgressBar.setProgress(0);
                configureControlWidgetsState();
            }, Platform::runLater);
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

    private void handleFileInTableSelectedEvent(RenamePreview preview) {
        log.debug("handleFileInTableSelectedEvent: {}", preview);
        if (preview == null) {
            setTextToTheFileDetailsView("");
            return;
        }
        var candidate = candidatesByFileId.get(preview.fileId());
        setTextToTheFileDetailsView(buildPreviewHtml(preview, candidate));
    }

    private String buildPreviewHtml(RenamePreview preview, RenameCandidate candidate) {
        var sb = new StringBuilder("<html><body style='font-family:sans-serif;font-size:12px;padding:4px'>");
        sb.append("<b>").append(languageTextRetriever.getString(TextKeys.FILE_NAME))
                .append(":</b> ").append(preview.originalName()).append("<br>");
        if (preview.newName() != null && !preview.newName().equals(preview.originalName())) {
            sb.append("<b>New name:</b> ").append(preview.newName()).append("<br>");
        }
        if (candidate != null) {
            sb.append("<b>").append(languageTextRetriever.getString(TextKeys.ABSOLUTE_PATH))
                    .append(":</b> ").append(candidate.path().getParent()).append("<br>");
            boolean isFile = !java.nio.file.Files.isDirectory(candidate.path());
            var typeKey = isFile ? TextKeys.IS_FILE : TextKeys.IS_FOLDER;
            sb.append("<b>").append(languageTextRetriever.getString(TextKeys.FILE_TYPE))
                    .append(":</b> ").append(languageTextRetriever.getString(typeKey)).append("<br>");
        }
        if (preview.hasError()) {
            sb.append("<span style='color:red'><b>Error:</b> ")
                    .append(preview.errorMessage()).append("</span><br>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private void handleCheckboxAutoPreviewChanged(boolean isChecked) {
        log.debug("handleAutoPreviewChanged: {}", isChecked);
        configureControlWidgetsState();
    }

    private void handleBtnClickedPreview() {
        log.debug("handlePreviewBtnClicked");
        configureControlWidgetsState();
    }

    private void handleBtnClickedRename() {
        log.debug("handleRenameBtnClicked");
        if (!showConfirmationDialog()) {
            return;
        }
        log.debug("handleRenameBtnClicked. Confirmed");
        var handle = sessionApi.execute();
        handle.addProgressListener((workDone, totalWork, message) ->
                Platform.runLater(() -> {
                    if (totalWork > 0) {
                        appProgressBar.setProgress(workDone / totalWork);
                    } else {
                        appProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                    }
                })
        );
        handle.result().thenRunAsync(() -> {
            appProgressBar.setProgress(0);
            areFilesRenamed = true;
            configureControlWidgetsState();
        }, Platform::runLater);
    }

    private boolean showConfirmationDialog() {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(languageTextRetriever.getString(TextKeys.DIALOG_CONFIRM_HEADER));
        alert.setHeaderText(null);
        alert.setContentText(languageTextRetriever.getString(TextKeys.DIALOG_CONFIRM_CONTENT));
        var confirmButton = new ButtonType(
                languageTextRetriever.getString(TextKeys.DIALOG_CONFIRM_BTN_OK));
        var cancelButton = new ButtonType(
                languageTextRetriever.getString(TextKeys.DIALOG_CONFIRM_BTN_CANCEL));
        alert.getButtonTypes().setAll(confirmButton, cancelButton);
        alert.showAndWait();
        return alert.getResult() == confirmButton;
    }

    private void handleBtnClickedClear() {
        log.debug("handleClearBtnClicked");
        sessionApi.clearFiles().thenRunAsync(() -> {
            areFilesRenamed = false;
            setTextToTheFileDetailsView("");
            configureControlWidgetsState();
        }, Platform::runLater);
    }

    private void handleBtnClickedReload() {
        log.debug("handleBtnClickedReload");
        areFilesRenamed = false;
        renameResultsByFileId = new HashMap<>();
        filesTableView.refresh();

        var files = List.copyOf(fxStateMirror.files());
        var resultsByFileId = fxStateMirror.renameResults().stream()
                .collect(Collectors.toMap(RenameSessionResult::fileId, r -> r));

        var reloadPaths = files.stream()
                .map(candidate -> {
                    var result = resultsByFileId.get(candidate.fileId());
                    if (result != null && result.status() == RenameStatus.SUCCESS) {
                        return candidate.path().getParent().resolve(result.finalName());
                    }
                    return candidate.path();
                })
                .toList();

        sessionApi.clearFiles()
                .thenCompose(ignored -> sessionApi.addFiles(reloadPaths))
                .thenRunAsync(() -> configureControlWidgetsState(), Platform::runLater);
    }

    private void setTextToTheFileDetailsView(String text) {
        var engine = fileInfoWebView.getEngine();
        fileInfoWebView.setFontScale(0.7);
        engine.loadContent(text);
    }

}
