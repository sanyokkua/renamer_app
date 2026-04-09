package ua.renamer.app.ui.service.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.ui.service.AppResourceRegistryApi;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;

/**
 * Eagerly loads and validates all application resources (CSS stylesheets, app icon) at startup.
 *
 * <p>Bound as an eager singleton so Guice constructs it during {@code createInjector()}.
 * Any missing resource throws {@link IllegalStateException}, preventing the app from starting.
 */
@Slf4j
@Singleton
public class AppResourceRegistryService implements AppResourceRegistryApi {

    private static final String CSS_BASE = "/styles/base.css";
    private static final String CSS_TABLE = "/styles/table.css";
    private static final String CSS_BUTTONS = "/styles/buttons.css";
    private static final String CSS_TYPOGRAPHY = "/styles/typography.css";
    private static final String CSS_ACCESSIBILITY = "/styles/accessibility.css";
    private static final String CSS_FILE_INFO = "/styles/file-info.css";
    private static final String CSS_COMPONENTS = "/styles/components.css";
    private static final String CSS_SETTINGS_DIALOG = "/styles/settings-dialog.css";
    private static final String IMAGE_ICON = "images/icon.png";

    private final List<String> sceneStylesheets;
    private final List<String> dialogStylesheets;
    private final List<String> settingsDialogStylesheets;
    private final Image appIcon;

    @Inject
    public AppResourceRegistryService() {
        log.debug("Loading application resources");

        var base = loadCss(CSS_BASE);
        var table = loadCss(CSS_TABLE);
        var buttons = loadCss(CSS_BUTTONS);
        var typography = loadCss(CSS_TYPOGRAPHY);
        var accessibility = loadCss(CSS_ACCESSIBILITY);
        var fileInfo = loadCss(CSS_FILE_INFO);
        var components = loadCss(CSS_COMPONENTS);
        var settingsDialog = loadCss(CSS_SETTINGS_DIALOG);

        sceneStylesheets = List.of(base, table, buttons, typography, accessibility, fileInfo, components);
        dialogStylesheets = List.of(base, buttons, components);
        settingsDialogStylesheets = List.of(base, buttons, components, settingsDialog);
        appIcon = loadIcon();

        log.debug("All application resources loaded successfully");
    }

    private static String loadCss(String path) {
        URL url = AppResourceRegistryService.class.getResource(path);
        if (url == null) {
            throw new IllegalStateException("Required CSS resource not found: " + path);
        }
        log.debug("Loaded CSS: {}", path);
        return url.toExternalForm();
    }

    private static Image loadIcon() {
        InputStream stream = AppResourceRegistryService.class.getClassLoader().getResourceAsStream(AppResourceRegistryService.IMAGE_ICON);
        Objects.requireNonNull(stream, "Required icon resource not found: " + AppResourceRegistryService.IMAGE_ICON);
        log.debug("Loaded icon: {}", AppResourceRegistryService.IMAGE_ICON);
        return new Image(stream);
    }

    @Override
    public List<String> getSceneStylesheets() {
        return sceneStylesheets;
    }

    @Override
    public List<String> getDialogStylesheets() {
        return dialogStylesheets;
    }

    @Override
    public List<String> getSettingsDialogStylesheets() {
        return settingsDialogStylesheets;
    }

    @Override
    public Image getAppIcon() {
        return appIcon;
    }

}
