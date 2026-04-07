package ua.renamer.app.ui.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Window;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.settings.AppSettings;
import ua.renamer.app.api.settings.LogLevel;
import ua.renamer.app.api.settings.SettingsService;
import ua.renamer.app.backend.settings.LoggingConfigService;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.enums.ViewNames;
import ua.renamer.app.ui.service.AppResourceRegistryApi;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;
import ua.renamer.app.ui.service.ViewLoaderApi;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class SettingsDialogController implements Initializable {

    private static final Map<String, String> SUPPORTED_LANGUAGES;
    private static final Map<String, String> LANGUAGE_CODE_BY_DISPLAY;
    private static final Map<LogLevel, String> LEVEL_COLORS = Map.of(
            LogLevel.DEBUG, "#A8CFEF",
            LogLevel.INFO, "#2C6FBF",
            LogLevel.WARN, "#F5D98A",
            LogLevel.ERROR, "#F5AAAA"
    );

    static {
        SUPPORTED_LANGUAGES = new LinkedHashMap<>();
        SUPPORTED_LANGUAGES.put("en", "English");
        SUPPORTED_LANGUAGES.put("cs", "Čeština");
        SUPPORTED_LANGUAGES.put("cnr", "Crnogorski");
        SUPPORTED_LANGUAGES.put("de", "Deutsch");
        SUPPORTED_LANGUAGES.put("et", "Eesti");
        SUPPORTED_LANGUAGES.put("es", "Español");
        SUPPORTED_LANGUAGES.put("fr", "Français");
        SUPPORTED_LANGUAGES.put("hr", "Hrvatski");
        SUPPORTED_LANGUAGES.put("it", "Italiano");
        SUPPORTED_LANGUAGES.put("lv", "Latviešu");
        SUPPORTED_LANGUAGES.put("lt", "Lietuvių");
        SUPPORTED_LANGUAGES.put("hu", "Magyar");
        SUPPORTED_LANGUAGES.put("pl", "Polski");
        SUPPORTED_LANGUAGES.put("ro", "Română");
        SUPPORTED_LANGUAGES.put("sq", "Shqip");
        SUPPORTED_LANGUAGES.put("sk", "Slovenčina");
        SUPPORTED_LANGUAGES.put("sl", "Slovenščina");
        SUPPORTED_LANGUAGES.put("bg", "Български");
        SUPPORTED_LANGUAGES.put("uk_UA", "Українська");

        LANGUAGE_CODE_BY_DISPLAY = new LinkedHashMap<>();
        SUPPORTED_LANGUAGES.forEach((code, display) -> LANGUAGE_CODE_BY_DISPLAY.put(display, code));
    }

    private final SettingsService settingsService;
    private final LanguageTextRetrieverApi languageTextRetriever;
    private final LoggingConfigService loggingConfigService;
    private final ViewLoaderApi viewLoader;
    private final AppResourceRegistryApi appResources;

    @FXML
    private CheckBox customConfigCheckbox;
    @FXML
    private HBox customConfigPathRow;
    @FXML
    private TextField customConfigPathField;
    @FXML
    private CheckBox loggingCheckbox;
    @FXML
    private VBox loggingSubOptions;
    @FXML
    private ComboBox<String> languageComboBox;
    @FXML
    private HBox languageRestartBadge;
    @FXML
    private ComboBox<LogLevel> logLevelComboBox;
    @FXML
    private TextField logFilePathField;

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        languageComboBox.getItems().setAll(SUPPORTED_LANGUAGES.values());
        logLevelComboBox.getItems().setAll(LogLevel.values());
        logLevelComboBox.setCellFactory(lv -> new LogLevelCell());
        logLevelComboBox.setButtonCell(new LogLevelCell());
    }

    public void show(Window owner) {
        Optional<Parent> contentOpt = viewLoader.loadFXML(ViewNames.SETTINGS_DIALOG);
        if (contentOpt.isEmpty()) {
            log.error("Failed to load settings dialog FXML");
            return;
        }
        Parent content = contentOpt.get();
        populateForm(settingsService.getCurrent());

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(languageTextRetriever.getString(TextKeys.SETTINGS_DIALOG_TITLE));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getStylesheets().addAll(appResources.getSettingsDialogStylesheets());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.setOnShowing(e -> {
            Button saveBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
            if (saveBtn != null) {
                saveBtn.setText(languageTextRetriever.getString(TextKeys.SETTINGS_BTN_SAVE));
                saveBtn.getStyleClass().add("btn-primary");
            }
            if (cancelBtn != null) {
                cancelBtn.setText(languageTextRetriever.getString(TextKeys.SETTINGS_BTN_CANCEL));
                cancelBtn.getStyleClass().add("btn-ghost");
            }
            ButtonBar bar = (ButtonBar) dialog.getDialogPane().lookup(".button-bar");
            if (bar != null) {
                bar.setButtonOrder(ButtonBar.BUTTON_ORDER_NONE);
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.filter(ButtonType.OK::equals).isPresent()) {
            AppSettings updated = collectSettings();
            try {
                settingsService.save(updated);
                loggingConfigService.reconfigure(updated);
            } catch (IOException e) {
                log.error("Failed to save settings", e);
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        MessageFormat.format(languageTextRetriever.getString(TextKeys.SETTINGS_SAVE_ERROR),
                                e.getMessage()));
                alert.initOwner(owner);
                alert.showAndWait();
            }
        }
    }

    @FXML
    void onCustomConfigCheckboxChanged() {
        boolean checked = customConfigCheckbox.isSelected();
        customConfigPathRow.setVisible(checked);
        customConfigPathRow.setManaged(checked);
        if (checked) {
            customConfigPathField.setText(settingsService.getSettingsFilePath().toString());
        }
    }

    @FXML
    void onLoggingCheckboxChanged() {
        boolean checked = loggingCheckbox.isSelected();
        loggingSubOptions.setVisible(checked);
        loggingSubOptions.setManaged(checked);
        if (checked) {
            Path settingsParent = settingsService.getSettingsFilePath().getParent();
            if (settingsParent != null) {
                Path logPath = settingsParent.resolve("logs").resolve("renamer.log");
                logFilePathField.setText(logPath.toString());
            }
        }
    }

    @FXML
    void onLanguageChanged() {
        String selectedDisplay = languageComboBox.getValue();
        if (selectedDisplay == null) {
            return;
        }
        String selectedCode = LANGUAGE_CODE_BY_DISPLAY.getOrDefault(selectedDisplay, "en");
        String currentLang = Locale.getDefault().getLanguage();
        boolean needsRestart = !selectedCode.startsWith(currentLang);
        languageRestartBadge.setVisible(needsRestart);
        languageRestartBadge.setManaged(needsRestart);
    }

    @FXML
    void onCopyConfigPath() {
        String path = customConfigPathField.getText();
        if (path != null && !path.isBlank()) {
            ClipboardContent clipContent = new ClipboardContent();
            clipContent.putString(path);
            Clipboard.getSystemClipboard().setContent(clipContent);
        }
    }

    @FXML
    void onOpenLogDirectory() {
        Path settingsParent = settingsService.getSettingsFilePath().getParent();
        if (settingsParent == null) {
            return;
        }
        Path logDir = settingsParent.resolve("logs");
        try {
            java.awt.Desktop.getDesktop().open(logDir.toFile());
        } catch (IOException e) {
            log.warn("Cannot open log directory: {}", logDir, e);
        }
    }

    private void populateForm(AppSettings settings) {
        String displayName = SUPPORTED_LANGUAGES.getOrDefault(settings.getLanguage(), "English");
        languageComboBox.setValue(displayName);
        onLanguageChanged();

        customConfigCheckbox.setSelected(settings.isCustomConfigEnabled());
        customConfigPathRow.setVisible(settings.isCustomConfigEnabled());
        customConfigPathRow.setManaged(settings.isCustomConfigEnabled());
        if (settings.isCustomConfigEnabled()) {
            customConfigPathField.setText(settingsService.getSettingsFilePath().toString());
        }

        loggingCheckbox.setSelected(settings.isLoggingEnabled());
        loggingSubOptions.setVisible(settings.isLoggingEnabled());
        loggingSubOptions.setManaged(settings.isLoggingEnabled());
        logLevelComboBox.setValue(settings.getLogLevel());
        if (settings.isLoggingEnabled()) {
            Path settingsParent = settingsService.getSettingsFilePath().getParent();
            if (settingsParent != null) {
                Path logPath = settingsParent.resolve("logs").resolve("renamer.log");
                logFilePathField.setText(logPath.toString());
            }
        }
    }

    private AppSettings collectSettings() {
        String selectedDisplay = languageComboBox.getValue();
        String langCode = LANGUAGE_CODE_BY_DISPLAY.getOrDefault(
                selectedDisplay != null ? selectedDisplay : "English", "en");
        LogLevel level = logLevelComboBox.getValue() != null
                ? logLevelComboBox.getValue() : LogLevel.INFO;
        return AppSettings.builder()
                .withVersion(settingsService.getCurrent().getVersion())
                .withLanguage(langCode)
                .withCustomConfigEnabled(customConfigCheckbox.isSelected())
                .withCustomConfigPath(settingsService.getCurrent().getCustomConfigPath())
                .withLoggingEnabled(loggingCheckbox.isSelected())
                .withLogLevel(level)
                .build();
    }

    private static final class LogLevelCell extends ListCell<LogLevel> {
        @Override
        protected void updateItem(LogLevel level, boolean empty) {
            super.updateItem(level, empty);
            if (empty || level == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            Circle dot = new Circle(5, Color.web(LEVEL_COLORS.getOrDefault(level, "#2C6FBF")));
            setGraphic(dot);
            setText(level.name());
        }
    }
}
