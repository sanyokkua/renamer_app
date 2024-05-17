package ua.renamer.app.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ua.renamer.app.RenamerApplication;
import ua.renamer.app.core.lang.LanguageManager;
import ua.renamer.app.ui.constants.ViewNames;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Utility class for loading FXML files with associated resource bundles.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ViewUtils {

    /**
     * Loads an FXML file based on the provided view name.
     *
     * @param viewName the name of the view to load.
     * @return the loaded FXML Parent node.
     * @throws IOException if the FXML file cannot be loaded.
     */
    public static Parent loadFXML(ViewNames viewName) throws IOException {
        return loadFXML(viewName.getViewName());
    }

    /**
     * Loads an FXML file based on the provided FXML filename.
     *
     * @param fxml the name of the FXML file to load.
     * @return the loaded FXML Parent node.
     * @throws IOException if the FXML file cannot be loaded.
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader loader = createLoader(fxml);
        return loader.load();
    }

    /**
     * Creates an FXMLLoader instance for the provided view name.
     *
     * @param viewName the name of the view.
     * @return the created FXMLLoader instance.
     */
    public static FXMLLoader createLoader(ViewNames viewName) {
        return createLoader(viewName.getViewName());
    }

    /**
     * Creates an FXMLLoader instance for the provided FXML filename.
     *
     * @param fxml the name of the FXML file.
     * @return the created FXMLLoader instance.
     * @throws IllegalArgumentException if the FXML filename or resource bundle is invalid.
     */
    private static FXMLLoader createLoader(String fxml) {
        if (Objects.isNull(fxml) || fxml.isBlank()) {
            throw new IllegalArgumentException("Name of the FXML file cannot be empty");
        }

        var resourceBundle = LanguageManager.getResourceBundle();

        if (Objects.isNull(resourceBundle)) {
            throw new IllegalArgumentException("Resource bundle cannot be empty");
        }

        var pathToView = "fxml/" + fxml;
        URL fxmlResourceUrl = RenamerApplication.class.getClassLoader().getResource(pathToView);

        if (Objects.isNull(fxmlResourceUrl)) {
            throw new IllegalArgumentException("Failed to load FXML");
        }

        FXMLLoader loader = new FXMLLoader(fxmlResourceUrl);
        loader.setResources(resourceBundle);

        return loader;
    }
}
