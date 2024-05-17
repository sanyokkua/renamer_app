package ua.renamer.app;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ua.renamer.app.core.lang.LanguageManager;
import ua.renamer.app.core.lang.TextKeys;
import ua.renamer.app.ui.ViewUtils;
import ua.renamer.app.ui.constants.ViewNames;

import java.io.IOException;
import java.util.Locale;

public class RenamerApplication extends Application {

    // Define minimum width and height for the application window
    public static final int MINIMAL_WIDTH = 900;
    public static final int MINIMAL_HEIGHT = 500;

    // Main method to launch the application
    public static void main(String[] args) {
        // Set the default locale to English and initialize the language manager
        Locale.setDefault(Locale.ENGLISH);
        LanguageManager.setLocale(Locale.ENGLISH);

        // Launch the JavaFX application
        launch();
    }

    // The start method is the entry point for the JavaFX application
    @Override
    public void start(Stage stage) throws IOException {
        // Retrieve the application title from the language resource bundle
        var title = LanguageManager.getString(TextKeys.APP_HEADER);

        // Set the stage title and minimum dimensions
        stage.setTitle(title);
        stage.setMinWidth(MINIMAL_WIDTH);
        stage.setMinHeight(MINIMAL_HEIGHT);

        // Load the main view from the FXML file
        Parent root = ViewUtils.loadFXML(ViewNames.APP_MAIN_VIEW);

        // Create a scene with the root node and set it on the stage
        var scene = new Scene(root, MINIMAL_WIDTH, MINIMAL_HEIGHT);
        stage.setScene(scene);

        // Show the stage
        stage.show();
    }
}
