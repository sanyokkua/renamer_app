package ua.renamer.app.ui.service.impl;

import com.google.inject.Inject;
import com.google.inject.Injector;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.BuilderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.RenamerApplication;
import ua.renamer.app.ui.enums.ViewNames;
import ua.renamer.app.ui.service.ViewLoaderApi;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ViewLoaderService implements ViewLoaderApi {

    private final ResourceBundle resourceBundle;
    private final Injector injector;
    private final BuilderFactory builderFactory;

    @Override
    public Optional<Parent> loadFXML(ViewNames viewName) {
        log.debug("Loading FXML file: {}", viewName.getViewName());
        if (viewName.getViewName().isBlank()) {
            return Optional.empty();
        }

        var loaderOptional = createLoader(viewName);
        if (loaderOptional.isEmpty()) {
            return Optional.empty();
        }

        try {
            FXMLLoader loader = loaderOptional.get();
            return Optional.ofNullable(loader.load());
        } catch (Exception ex) {
            log.warn("Failed to load FXML file: {}", viewName.getViewName(), ex);
            return Optional.empty();
        }
    }

    @Override
    public Optional<FXMLLoader> createLoader(ViewNames viewName) {
        log.debug("Creating FXML loader: {}", viewName.getViewName());
        if (viewName.getViewName().isBlank()) {
            log.warn("View name is blank in enum: {}", viewName.name());
            return Optional.empty();
        }

        if (Objects.isNull(resourceBundle)) {
            log.warn("Resource bundle is empty");
            return Optional.empty();
        }

        var fxmlName = viewName.getViewName();
        var pathToView = "fxml/" + fxmlName;
        URL fxmlResourceUrl = RenamerApplication.class.getClassLoader().getResource(pathToView);

        if (Objects.isNull(fxmlResourceUrl)) {
            log.warn("Failed to load FXML");
            return Optional.empty();
        }

        FXMLLoader loader = new FXMLLoader(fxmlResourceUrl);
        loader.setResources(resourceBundle);
        loader.setControllerFactory(injector::getInstance);
        loader.setBuilderFactory(builderFactory);

        return Optional.of(loader);
    }

}
