package ua.renamer.app.ui.controller;

import com.google.inject.Inject;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.FolderDropOptions;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.ModeParameters;
import ua.renamer.app.api.session.RenameCandidate;
import ua.renamer.app.api.session.RenamePreview;
import ua.renamer.app.api.session.RenameSessionResult;
import ua.renamer.app.api.session.SessionApi;
import ua.renamer.app.backend.service.FolderExpansionService;
import ua.renamer.app.ui.controller.mode.ModeControllerV2Api;
import ua.renamer.app.ui.converter.AppModesConverter;
import ua.renamer.app.ui.enums.TableStyles;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.AppResourceRegistryApi;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;
import ua.renamer.app.ui.state.FxStateMirror;
import ua.renamer.app.ui.view.ModeViewRegistry;
import ua.renamer.app.ui.widget.table.TableCustomContextMenu;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    private final FolderExpansionService folderExpansionService;
    private final SettingsDialogController settingsDialogController;
    private final AppResourceRegistryApi appResources;

    @FXML
    private Menu modeMenu;
    @FXML
    private MenuItem settingsMenuItem;
    @FXML
    private StackPane appModeContainer;
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
    private VBox fileInfoPanel;
    @FXML
    private ProgressBar appProgressBar;
    @FXML
    private Label progressLabel;
    @FXML
    private Label fileCountLabel;
    @FXML
    private Label mainPreviewLabel;

    private boolean areFilesRenamed = false;
    private ModeApi<?> currentModeApi;
    private Map<String, RenameCandidate> candidatesByFileId = new HashMap<>();
    private Map<String, RenameSessionResult> renameResultsByFileId = new HashMap<>();
    private RenamePreview lastFileInfoPreview;
    private RenameCandidate lastFileInfoCandidate;

    @SuppressWarnings("unchecked")
    private static <P extends ModeParameters> void callBind(ModeControllerV2Api<?> ctrl, ModeApi<?> api) {
        ((ModeControllerV2Api<P>) ctrl).bind((ModeApi<P>) api);
    }

    private static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        if (bytes < 1024L * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        }
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing ApplicationMainViewController");
        configureModeMenu();
        configureFilesTableView();
        configureFilesTableViewColumns();
        configureRenameBtn();
        configureClearBtn();
        configureReloadBtn();
        configureProgressBar();
        configureFxStateMirrorListeners();
        configureControlWidgetsState();
        handleModeChanged(TransformationMode.ADD_TEXT);
        updateFileCountLabel();
        clearFileInfoPanel();
        fileInfoPanel.widthProperty().addListener((obs, oldW, newW) -> {
            if (newW.doubleValue() > 0 && lastFileInfoPreview != null) {
                updateFileInfoPanel(lastFileInfoPreview, lastFileInfoCandidate);
            }
        });
        settingsMenuItem.setAccelerator(
                new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));
    }

    private void configureModeMenu() {
        log.info("Configuring modeMenu");
        var group = new ToggleGroup();
        for (var mode : TransformationMode.values()) {
            var item = new RadioMenuItem(appModesConverter.toString(mode));
            item.setToggleGroup(group);
            item.setOnAction(e -> handleModeChanged(mode));
            modeMenu.getItems().add(item);
        }
        ((RadioMenuItem) modeMenu.getItems().getFirst()).setSelected(true);
    }

    private void configureFilesTableView() {
        log.info("Configuring filesTableView");
        filesTableView.setOnDragOver(this::handleFilesTableViewDragOverEvent);
        filesTableView.setOnDragDropped(this::handleFilesTableViewFilesDroppedEvent);
        var sortedList = new javafx.collections.transformation.SortedList<>(fxStateMirror.preview());
        sortedList.comparatorProperty().bind(filesTableView.comparatorProperty());
        filesTableView.setItems(sortedList);
        filesTableView.setContextMenu(new TableCustomContextMenu(filesTableView));
        filesTableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(RenamePreview preview, boolean empty) {
                super.updateItem(preview, empty);
                getStyleClass().removeAll("row-ready", "row-error", "row-renamed");
                if (preview == null || empty) {
                    return;
                }
                if (preview.hasError()) {
                    getStyleClass().add(TableStyles.HAS_ERROR.getStyleClass());
                    return;
                }
                var result = renameResultsByFileId.get(preview.fileId());
                if (result != null && result.status() == RenameStatus.SUCCESS) {
                    getStyleClass().add(TableStyles.RENAMED.getStyleClass());
                    return;
                }
                if (preview.newName() != null && !preview.newName().equals(preview.originalName())) {
                    getStyleClass().add(TableStyles.READY_FOR_RENAMING.getStyleClass());
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
            if (candidate == null) {
                return new SimpleStringProperty("");
            }
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

        originalNameColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    setTooltip(new Tooltip(item));
                }
            }
        });
        newNameColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    setTooltip(new Tooltip(item));
                }
            }
        });
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("-fx-alignment: CENTER;");
                if (empty) {
                    setGraphic(null);
                    return;
                }
                var preview = getTableRow() != null ? getTableRow().getItem() : null;
                if (preview == null) {
                    setGraphic(null);
                    return;
                }
                String badgeClass;
                String badgeText;
                if (preview.hasError()) {
                    badgeClass = "badge-error";
                    badgeText = "✕ Error";
                } else {
                    var result = renameResultsByFileId.get(preview.fileId());
                    if (result != null) {
                        badgeClass = switch (result.status()) {
                            case SUCCESS -> "badge-success";
                            case SKIPPED -> "badge-warning";
                            default -> "badge-error";
                        };
                        badgeText = switch (result.status()) {
                            case SUCCESS -> "✓ Renamed";
                            case SKIPPED -> "⚠ Skipped";
                            default -> "✕ Error";
                        };
                    } else if (preview.newName() != null && !preview.newName().equals(preview.originalName())) {
                        badgeClass = "badge-pending";
                        badgeText = "● Pending";
                    } else {
                        setGraphic(null);
                        return;
                    }
                }
                var badge = new javafx.scene.control.Label(badgeText);
                badge.getStyleClass().add(badgeClass);
                setGraphic(badge);
            }
        });
        itemTypeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("-fx-alignment: CENTER;");
                if (empty) {
                    setGraphic(null);
                    return;
                }
                var preview = getTableRow() != null ? getTableRow().getItem() : null;
                if (preview == null) {
                    setGraphic(null);
                    return;
                }
                var candidate = candidatesByFileId.get(preview.fileId());
                boolean isFile = candidate == null || !java.nio.file.Files.isDirectory(candidate.path());
                if (isFile) {
                    String name = preview.originalName() != null ? preview.originalName() : "";
                    int dot = name.lastIndexOf('.');
                    String ext = (dot >= 0 && dot < name.length() - 1) ? name.substring(dot + 1) : "";
                    if (!ext.isEmpty()) {
                        var chip = new javafx.scene.control.Label(ext.toLowerCase());
                        chip.getStyleClass().add("type-chip");
                        setGraphic(chip);
                        return;
                    }
                    setGraphic(null);
                    return;
                }
                var folderChip = new javafx.scene.control.Label("folder");
                folderChip.getStyleClass().add("type-chip-folder");
                setGraphic(folderChip);
            }
        });
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
        progressLabel.setText("");
    }

    private void updateFileCountLabel() {
        int total = fxStateMirror.files().size();
        long renamed = renameResultsByFileId.values().stream()
                .filter(r -> r.status() == RenameStatus.SUCCESS).count();
        if (total == 0) {
            fileCountLabel.setText("");
        } else if (renamed > 0) {
            fileCountLabel.setText(MessageFormat.format(
                    languageTextRetriever.getString(TextKeys.FILE_COUNT_RENAMED), total, renamed));
        } else {
            fileCountLabel.setText(MessageFormat.format(
                    languageTextRetriever.getString(TextKeys.FILE_COUNT_SUMMARY), total));
        }
    }

    private void configureFxStateMirrorListeners() {
        log.info("Configuring FxStateMirror listeners (V2 path)");
        fxStateMirror.files().addListener((ListChangeListener<RenameCandidate>) change -> {
            candidatesByFileId = fxStateMirror.files().stream()
                    .collect(Collectors.toMap(RenameCandidate::fileId, c -> c));
            configureControlWidgetsState();
            updateFileCountLabel();
        });
        fxStateMirror.renameResults().addListener((ListChangeListener<RenameSessionResult>) change -> {
            if (!fxStateMirror.renameResults().isEmpty()) {
                renameResultsByFileId = fxStateMirror.renameResults().stream()
                        .collect(Collectors.toMap(RenameSessionResult::fileId, r -> r));
                areFilesRenamed = true;
                configureControlWidgetsState();
                filesTableView.refresh();
                updateFileCountLabel();
            }
        });
    }

    private void configureControlWidgetsState() {
        log.info("Configuring controlWidgetsState");
        reloadBtn.setDisable(!areFilesRenamed);

        if (fxStateMirror.files().isEmpty()) {
            renameBtn.setDisable(true);
            clearBtn.setDisable(true);
        } else {
            renameBtn.setDisable(false);
            clearBtn.setDisable(false);
            renameBtn.setDisable(areFilesRenamed);
        }
    }

    private void handleModeChanged(TransformationMode mode) {
        log.debug("handleModeChanged");
        modeViewRegistry.getView(mode).ifPresent(view -> {
            StackPane.setMargin(view, Insets.EMPTY);
            if (!appModeContainer.getChildren().isEmpty()) {
                var current = appModeContainer.getChildren().getFirst();
                var fadeOut = new FadeTransition(Duration.millis(80), current);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    appModeContainer.getChildren().setAll(view);
                    view.setOpacity(0.0);
                    var fadeIn = new FadeTransition(Duration.millis(120), view);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });
                fadeOut.play();
            } else {
                appModeContainer.getChildren().add(view);
            }
        });

        log.debug("handleModeChanged: {}", mode.name());

        modeViewRegistry.getController(mode).ifPresent(v2ctrl ->
                sessionApi.selectMode(v2ctrl.supportedMode())
                        .thenAcceptAsync(modeApi -> {
                            currentModeApi = modeApi;
                            callBind(v2ctrl, modeApi);
                            updatePreview(modeApi);
                            modeApi.addParameterListener(p -> Platform.runLater(() -> updatePreview(modeApi)));
                        }, Platform::runLater)
        );
    }

    private void updatePreview(ModeApi<?> modeApi) {
        modeApi.previewSingleFile("photo", "jpg")
                .ifPresentOrElse(
                        newName -> mainPreviewLabel.setText(MessageFormat.format(
                                languageTextRetriever.getString(TextKeys.PREVIEW_FORMAT), "photo.jpg", newName)),
                        () -> mainPreviewLabel.setText("—")
                );
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
            var allPaths = dragboard.getFiles().stream()
                    .map(java.io.File::toPath)
                    .toList();

            var dirs = allPaths.stream().filter(Files::isDirectory).toList();
            var files = allPaths.stream().filter(p -> !Files.isDirectory(p)).toList();

            var toAdd = new java.util.ArrayList<>(files);

            if (!dirs.isEmpty()) {
                var opts = FolderDropDialogController.show(dirs.size(), languageTextRetriever::getString, appResources.getDialogStylesheets());

                if (opts.action() == FolderDropOptions.Action.CANCEL) {
                    event.setDropCompleted(false);
                    event.consume();
                    return;
                } else if (opts.action() == FolderDropOptions.Action.USE_AS_ITEM) {
                    toAdd.addAll(dirs);
                } else { // USE_CONTENTS
                    for (var dir : dirs) {
                        var expanded = folderExpansionService.expand(dir, opts);
                        if (expanded.isEmpty()) {
                            log.info("Folder '{}' is empty or yielded no files — skipping", dir);
                        }
                        toAdd.addAll(expanded);
                    }
                }
            }

            if (toAdd.isEmpty()) {
                event.setDropCompleted(false);
                event.consume();
                return;
            }

            var pathsToAdd = java.util.List.copyOf(toAdd);
            appProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            progressLabel.setText(languageTextRetriever.getString(TextKeys.PROGRESS_LOADING));
            sessionApi.addFiles(pathsToAdd).thenRunAsync(() -> {
                appProgressBar.setProgress(0);
                progressLabel.setText("");
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
            clearFileInfoPanel();
            return;
        }
        var candidate = candidatesByFileId.get(preview.fileId());
        updateFileInfoPanel(preview, candidate);
    }

    private void clearFileInfoPanel() {
        lastFileInfoPreview = null;
        lastFileInfoCandidate = null;
        fileInfoPanel.getChildren().clear();
        var placeholder = new Label(
                languageTextRetriever.getString(TextKeys.FILE_INFO_NO_SELECTION));
        placeholder.getStyleClass().add("file-info-placeholder");
        fileInfoPanel.getChildren().add(placeholder);
    }

    private void updateFileInfoPanel(RenamePreview preview, RenameCandidate candidate) {
        lastFileInfoPreview = preview;
        lastFileInfoCandidate = candidate;
        fileInfoPanel.getChildren().clear();
        if (preview == null) {
            clearFileInfoPanel();
            return;
        }

        var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Two column VBoxes
        var leftCol = new VBox();
        leftCol.getStyleClass().add("file-info-col");
        HBox.setHgrow(leftCol, javafx.scene.layout.Priority.ALWAYS);

        var rightCol = new VBox();
        rightCol.getStyleClass().add("file-info-col");
        HBox.setHgrow(rightCol, javafx.scene.layout.Priority.ALWAYS);

        java.util.function.BiConsumer<VBox, String[]> addRow = (col, kv) -> {
            var row = new HBox();
            row.getStyleClass().add("file-info-row");
            var labelKey = new Label(kv[0] + ":");
            labelKey.getStyleClass().add("file-info-label-key");
            var labelVal = new Label(kv[1] != null ? kv[1] : "");
            labelVal.getStyleClass().add("file-info-label-value");
            row.getChildren().addAll(labelKey, labelVal);
            col.getChildren().add(row);
        };

        // ── LEFT COLUMN: identity info ──
        addRow.accept(leftCol, new String[]{
                languageTextRetriever.getString(TextKeys.FILE_NAME), preview.originalName()});

        if (preview.newName() != null && !preview.newName().equals(preview.originalName())) {
            addRow.accept(leftCol, new String[]{
                    languageTextRetriever.getString(TextKeys.FILE_NEW_NAME), preview.newName()});
        }

        if (candidate != null) {
            Path candidateParent = candidate.path().getParent();
            if (candidateParent != null) {
                addRow.accept(leftCol, new String[]{
                        languageTextRetriever.getString(TextKeys.ABSOLUTE_PATH),
                        candidateParent.toString()});
            }
            boolean isFile = !Files.isDirectory(candidate.path());
            addRow.accept(leftCol, new String[]{
                    languageTextRetriever.getString(TextKeys.FILE_TYPE),
                    languageTextRetriever.getString(isFile ? TextKeys.IS_FILE : TextKeys.IS_FOLDER)});
            if (!candidate.extension().isBlank()) {
                addRow.accept(leftCol, new String[]{
                        languageTextRetriever.getString(TextKeys.FILE_EXTENSION),
                        candidate.extension()});
            }
            if (!isFile) {
                try (var stream = Files.list(candidate.path())) {
                    long itemCount = stream.count();
                    addRow.accept(leftCol, new String[]{
                            languageTextRetriever.getString(TextKeys.FILE_ITEM_COUNT),
                            itemCount + " items"});
                } catch (IOException e) {
                    log.debug("Failed to count items in directory '{}': {}", candidate.path(), e.getMessage());
                }
            } else {
                try {
                    long bytes = Files.size(candidate.path());
                    addRow.accept(leftCol, new String[]{
                            languageTextRetriever.getString(TextKeys.FILE_SIZE), formatFileSize(bytes)});
                } catch (IOException e) {
                    log.debug("Failed to read file size for '{}': {}", candidate.path(), e.getMessage());
                }
            }
        }

        // ── RIGHT COLUMN: temporal + media info ──
        if (candidate != null) {
            try {
                var attrs = Files.readAttributes(candidate.path(), BasicFileAttributes.class);
                var modTime = attrs.lastModifiedTime().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
                var creTime = attrs.creationTime().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
                addRow.accept(rightCol, new String[]{
                        languageTextRetriever.getString(TextKeys.FILE_MODIFICATION_TIME),
                        modTime.format(fmt)});
                addRow.accept(rightCol, new String[]{
                        languageTextRetriever.getString(TextKeys.FILE_CREATION_TIME),
                        creTime.format(fmt)});
            } catch (IOException e) {
                log.debug("Failed to read file attributes for '{}': {}", candidate.path(), e.getMessage());
            }
        }

        var metaOpt = sessionApi.getFileMetadata(preview.fileId());
        metaOpt.ifPresent(dto -> {
            String na = languageTextRetriever.getString(TextKeys.METADATA_NOT_AVAILABLE);
            boolean isDir = candidate != null && Files.isDirectory(candidate.path());
            if (dto.mimeType() != null && !dto.mimeType().isBlank()) {
                addRow.accept(leftCol, new String[]{
                        languageTextRetriever.getString(TextKeys.FILE_MIME_TYPE), dto.mimeType()});
            }
            if (!isDir && ("IMAGE".equals(dto.category()) || "VIDEO".equals(dto.category()))) {
                addRow.accept(rightCol, new String[]{
                        languageTextRetriever.getString(TextKeys.FILE_CONTENT_CREATION_TIME),
                        dto.contentCreationDate() != null ? dto.contentCreationDate().format(fmt) : na});
                addRow.accept(rightCol, new String[]{
                        languageTextRetriever.getString(TextKeys.WIDTH),
                        dto.widthPx() != null ? dto.widthPx() + " px" : na});
                addRow.accept(rightCol, new String[]{
                        languageTextRetriever.getString(TextKeys.HEIGHT),
                        dto.heightPx() != null ? dto.heightPx() + " px" : na});
            }
            if (!isDir && "AUDIO".equals(dto.category())) {
                addRow.accept(rightCol, new String[]{
                        languageTextRetriever.getString(TextKeys.SONG_AUTHOR),
                        dto.audioArtist() != null ? dto.audioArtist() : na});
                addRow.accept(rightCol, new String[]{
                        languageTextRetriever.getString(TextKeys.SONG_NAME),
                        dto.audioTitle() != null ? dto.audioTitle() : na});
                addRow.accept(rightCol, new String[]{
                        languageTextRetriever.getString(TextKeys.SONG_ALBUM),
                        dto.audioAlbum() != null ? dto.audioAlbum() : na});
                addRow.accept(rightCol, new String[]{
                        languageTextRetriever.getString(TextKeys.SONG_YEAR),
                        dto.audioYear() != null ? String.valueOf(dto.audioYear()) : na});
            }
        });

        if (preview.hasError() && preview.errorMessage() != null) {
            var errLabel = new Label(
                    languageTextRetriever.getString(TextKeys.FILE_INFO_ERROR) + ": " + preview.errorMessage());
            errLabel.getStyleClass().add("file-info-error");
            rightCol.getChildren().add(errLabel);
        }

        if (fileInfoPanel.getWidth() >= 430) {
            var columns = new HBox(leftCol, rightCol);
            columns.getStyleClass().add("file-info-columns");
            leftCol.prefWidthProperty().bind(columns.widthProperty().multiply(0.5));
            rightCol.prefWidthProperty().bind(columns.widthProperty().multiply(0.5));
            fileInfoPanel.getChildren().add(columns);
        } else {
            var singleCol = new VBox(leftCol, rightCol);
            fileInfoPanel.getChildren().add(singleCol);
        }
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
                        progressLabel.setText(MessageFormat.format(
                                languageTextRetriever.getString(TextKeys.PROGRESS_RENAMING_N),
                                (long) workDone, (long) totalWork));
                    } else {
                        appProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                        progressLabel.setText(languageTextRetriever.getString(TextKeys.PROGRESS_RENAMING));
                    }
                })
        );
        handle.result().thenRunAsync(() -> {
            appProgressBar.setProgress(0);
            progressLabel.setText(languageTextRetriever.getString(TextKeys.PROGRESS_DONE));
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
        alert.getButtonTypes().setAll(cancelButton, confirmButton);
        alert.setOnShowing(e -> {
            var bar = alert.getDialogPane().lookup(".button-bar");
            if (bar instanceof ButtonBar buttonBar) {
                buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_NONE);
            }
            var btnOk = alert.getDialogPane().lookupButton(confirmButton);
            var btnCancel = alert.getDialogPane().lookupButton(cancelButton);
            if (btnOk != null) {
                btnOk.getStyleClass().add("btn-primary");
            }
            if (btnCancel != null) {
                btnCancel.getStyleClass().add("btn-ghost");
            }
        });
        alert.getDialogPane().getStylesheets().addAll(appResources.getDialogStylesheets());
        alert.setGraphic(null);
        alert.showAndWait();
        return alert.getResult() == confirmButton;
    }

    private void handleBtnClickedClear() {
        log.debug("handleClearBtnClicked");
        sessionApi.clearFiles().thenRunAsync(() -> {
            areFilesRenamed = false;
            clearFileInfoPanel();
            configureControlWidgetsState();
            updateFileCountLabel();
        }, Platform::runLater);
    }

    @FXML
    private void onOpenSettings() {
        settingsDialogController.show(appModeContainer.getScene().getWindow());
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
                .thenRunAsync(() -> {
                    configureControlWidgetsState();
                    updateFileCountLabel();
                }, Platform::runLater);
    }


}
