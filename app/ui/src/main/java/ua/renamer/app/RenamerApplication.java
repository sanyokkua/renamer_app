package ua.renamer.app;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.ui.config.DIAppModule;
import ua.renamer.app.ui.config.DICoreModule;
import ua.renamer.app.ui.config.DIUIModule;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.enums.ViewNames;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;
import ua.renamer.app.ui.service.ViewLoaderApi;

import java.util.Objects;
import java.util.Optional;

@Slf4j
public class RenamerApplication extends Application {

    // Define minimum width and height for the application window
    public static final int MINIMAL_WIDTH = 900;
    public static final int MINIMAL_HEIGHT = 500;
    private static Injector injector;

    // Main method to launch the application
    public static void main(String[] args) {
        // Launch the JavaFX application
        injector = Guice.createInjector(new DIAppModule(), new DICoreModule(), new DIUIModule());
        log.debug("Application started");
        launch();
    }

    // The start method is the entry point for the JavaFX application
    @Override
    public void start(Stage stage) {
        var iconInputStream = RenamerApplication.class.getClassLoader().getResourceAsStream("images/icon.png");
        Objects.requireNonNull(iconInputStream); // If icon can't be loaded, then we do not need to try start app

        var languageTextRetriever = injector.getInstance(LanguageTextRetrieverApi.class);
        var viewLoader = injector.getInstance(ViewLoaderApi.class);

        // Retrieve the application title from the language resource bundle
        var title = languageTextRetriever.getString(TextKeys.APP_HEADER);

        // Set the stage title and minimum dimensions
        stage.setTitle(title);
        stage.setMinWidth(MINIMAL_WIDTH);
        stage.setMinHeight(MINIMAL_HEIGHT);
        stage.getIcons().add(new Image(iconInputStream));

        log.debug("title: {}", title);
        log.debug("minimal width: {}", MINIMAL_WIDTH);
        log.debug("minimal height: {}", MINIMAL_HEIGHT);

        // Load the main view from the FXML file
        Optional<Parent> root = viewLoader.loadFXML(ViewNames.APP_MAIN_VIEW);

        if (root.isEmpty()) {
            log.debug("No root view found");
            throw new IllegalStateException("Failed to load parent view");
        }

        // Create a scene with the root node and set it on the stage
        var scene = new Scene(root.get(), MINIMAL_WIDTH, MINIMAL_HEIGHT);
        var baseCss = RenamerApplication.class.getResource("/styles/base.css");
        if (baseCss != null) {
            scene.getStylesheets().add(baseCss.toExternalForm());
        }
        var tableCss = RenamerApplication.class.getResource("/styles/table.css");
        if (tableCss != null) {
            scene.getStylesheets().add(tableCss.toExternalForm());
        }
        var buttonsCss = RenamerApplication.class.getResource("/styles/buttons.css");
        if (buttonsCss != null) {
            scene.getStylesheets().add(buttonsCss.toExternalForm());
        }
        var typoCss = RenamerApplication.class.getResource("/styles/typography.css");
        if (typoCss != null) {
            scene.getStylesheets().add(typoCss.toExternalForm());
        }
        var a11yCss = RenamerApplication.class.getResource("/styles/accessibility.css");
        if (a11yCss != null) {
            scene.getStylesheets().add(a11yCss.toExternalForm());
        }
        var fileInfoCss = RenamerApplication.class.getResource("/styles/file-info.css");
        if (fileInfoCss != null) {
            scene.getStylesheets().add(fileInfoCss.toExternalForm());
        }
        var componentsCss = RenamerApplication.class.getResource("/styles/components.css");
        if (componentsCss != null) {
            scene.getStylesheets().add(componentsCss.toExternalForm());
        }
        stage.setScene(scene);

        // Show the stage
        stage.show();
    }

}
