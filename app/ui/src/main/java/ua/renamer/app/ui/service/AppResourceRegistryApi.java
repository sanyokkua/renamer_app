package ua.renamer.app.ui.service;

import javafx.scene.image.Image;

import java.util.List;

/**
 * Provides pre-validated application resources (stylesheets, icon).
 *
 * <p>All resources are loaded and validated at application startup.
 * If any resource is missing the injector creation fails and the app will not start.
 */
public interface AppResourceRegistryApi {

    /**
     * Returns the external-form URLs of all 7 stylesheets applied to the main {@link javafx.scene.Scene}.
     */
    List<String> getSceneStylesheets();

    /**
     * Returns the external-form URLs of the 3 shared stylesheets applied to all standard dialogs and alerts
     * (base, buttons, components).
     */
    List<String> getDialogStylesheets();

    /**
     * Returns the external-form URLs of the 4 stylesheets applied to the Settings dialog
     * (base, buttons, components, settings-dialog).
     */
    List<String> getSettingsDialogStylesheets();

    /**
     * Returns the pre-loaded application window icon.
     */
    Image getAppIcon();

}
