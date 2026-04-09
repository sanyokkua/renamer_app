package ua.renamer.app.ui.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import ua.renamer.app.ui.enums.ViewNames;

import java.util.Optional;

/**
 * Interface for loading FXML views.
 */
public interface ViewLoaderApi {

    /**
     * Loads the FXML view corresponding to the provided view name.
     *
     * @param viewName The name of the view to load.
     * @return An optional containing the loaded parent node, or an empty optional if the view could not be loaded.
     */
    Optional<Parent> loadFXML(ViewNames viewName);

    /**
     * Creates an FXMLLoader instance for the FXML view corresponding to the provided view name.
     *
     * @param viewName The name of the view to load.
     * @return An optional containing the FXMLLoader instance, or an empty optional if the view could not be loaded.
     */
    Optional<FXMLLoader> createLoader(ViewNames viewName);

}
