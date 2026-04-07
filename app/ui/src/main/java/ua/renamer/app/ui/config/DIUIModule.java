package ua.renamer.app.ui.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.util.BuilderFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.session.StatePublisher;
import ua.renamer.app.backend.config.DIBackendModule;
import ua.renamer.app.ui.controller.ApplicationMainViewController;
import ua.renamer.app.ui.controller.SettingsDialogController;
import ua.renamer.app.ui.controller.mode.ModeControllerV2Api;
import ua.renamer.app.ui.controller.mode.impl.*;
import ua.renamer.app.ui.converter.*;
import ua.renamer.app.ui.enums.ViewNames;
import ua.renamer.app.ui.service.ViewLoaderApi;
import ua.renamer.app.ui.service.impl.ViewLoaderService;
import ua.renamer.app.ui.state.FxStateMirror;
import ua.renamer.app.ui.view.ModeViewRegistry;
import ua.renamer.app.ui.widget.builder.ItemPositionExtendedRadioSelectorBuilder;
import ua.renamer.app.ui.widget.builder.ItemPositionRadioSelectorBuilder;
import ua.renamer.app.ui.widget.builder.ItemPositionTruncateRadioSelectorBuilder;
import ua.renamer.app.ui.widget.builder.ItemPositionWithReplacementRadioSelectorBuilder;
import ua.renamer.app.ui.widget.factory.RadioSelectorFactory;
import ua.renamer.app.ui.widget.impl.ItemPositionExtendedRadioSelector;
import ua.renamer.app.ui.widget.impl.ItemPositionRadioSelector;
import ua.renamer.app.ui.widget.impl.ItemPositionTruncateRadioSelector;
import ua.renamer.app.ui.widget.impl.ItemPositionWithReplacementRadioSelector;

import java.io.IOException;
import java.util.Optional;

/**
 * Dependency Injection module configuration for the application. Includes only UI module dependencies.
 */
@Slf4j
@Getter
public class DIUIModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new DIBackendModule());
        bindServices();
        bindCustomWidgets();
        bindStringConverters();
        bindViewControllers();
    }

    private void bindServices() {
        bind(ViewLoaderApi.class).to(ViewLoaderService.class).in(Singleton.class);
    }

    private void bindCustomWidgets() {
        bind(ItemPositionExtendedRadioSelector.class);
        bind(ItemPositionRadioSelector.class);
        bind(ItemPositionTruncateRadioSelector.class);
        bind(ItemPositionWithReplacementRadioSelector.class);

        bind(ItemPositionExtendedRadioSelectorBuilder.class);
        bind(ItemPositionRadioSelectorBuilder.class);
        bind(ItemPositionTruncateRadioSelectorBuilder.class);
        bind(ItemPositionWithReplacementRadioSelectorBuilder.class);

        bind(JavaFXBuilderFactory.class).in(Singleton.class);
        bind(BuilderFactory.class).to(RadioSelectorFactory.class).in(Singleton.class);
    }

    private void bindStringConverters() {
        bind(AppModesConverter.class).in(Singleton.class);
        bind(DateFormatConverter.class).in(Singleton.class);
        bind(DateTimeFormatConverter.class).in(Singleton.class);
        bind(DateTimeSourceConverter.class).in(Singleton.class);
        bind(ImageDimensionOptionsConverter.class).in(Singleton.class);
        bind(ItemPositionConverter.class).in(Singleton.class);
        bind(ItemPositionExtendedConverter.class).in(Singleton.class);
        bind(ItemPositionWithReplacementConverter.class).in(Singleton.class);
        bind(SortSourceConverter.class).in(Singleton.class);
        bind(TimeFormatConverter.class).in(Singleton.class);
        bind(TruncateOptionsConverter.class).in(Singleton.class);
    }

    private void bindViewControllers() {
        bind(ModeAddTextController.class).in(Singleton.class);
        bind(ModeNumberFilesController.class).in(Singleton.class);
        bind(ModeChangeCaseController.class).in(Singleton.class);
        bind(ModeChangeExtensionController.class).in(Singleton.class);
        bind(ModeRemoveTextController.class).in(Singleton.class);
        bind(ModeReplaceTextController.class).in(Singleton.class);
        bind(ModeTrimNameController.class).in(Singleton.class);
        bind(ModeAddDatetimeController.class).in(Singleton.class);
        bind(ModeAddDimensionsController.class).in(Singleton.class);
        bind(ModeAddFolderNameController.class).in(Singleton.class);
        bind(ApplicationMainViewController.class).in(Singleton.class);
        bind(SettingsDialogController.class).in(Singleton.class);
    }

    /**
     * Provides a fully populated {@link ModeViewRegistry} by loading each mode's FXML
     * and pairing its loaded {@link Parent} view with its injected controller.
     *
     * @param viewLoaderApi  the service that resolves FXML files by view name
     * @param addText        controller for ADD_TEXT mode
     * @param changeCase     controller for CHANGE_CASE mode
     * @param addDatetime    controller for ADD_DATETIME mode
     * @param addDimensions  controller for ADD_DIMENSIONS mode
     * @param addFolderName  controller for ADD_FOLDER_NAME mode
     * @param removeText     controller for REMOVE_TEXT mode
     * @param replaceText    controller for REPLACE_TEXT mode
     * @param numberFiles    controller for NUMBER_FILES mode
     * @param trimName       controller for TRIM_NAME mode
     * @param changeExtension controller for CHANGE_EXTENSION mode
     * @return a populated registry; never null
     * @throws IOException if any FXML file cannot be loaded
     */
    @Provides
    @Singleton
    public ModeViewRegistry provideModeViewRegistry(
            ViewLoaderApi viewLoaderApi,
            ModeAddTextController addText,
            ModeChangeCaseController changeCase,
            ModeAddDatetimeController addDatetime,
            ModeAddDimensionsController addDimensions,
            ModeAddFolderNameController addFolderName,
            ModeRemoveTextController removeText,
            ModeReplaceTextController replaceText,
            ModeNumberFilesController numberFiles,
            ModeTrimNameController trimName,
            ModeChangeExtensionController changeExtension) throws IOException {
        var registry = new ModeViewRegistry();
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_ADD_TEXT, addText);
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_CHANGE_CASE, changeCase);
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_ADD_DATETIME, addDatetime);
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_ADD_DIMENSIONS, addDimensions);
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_ADD_FOLDER_NAME, addFolderName);
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_REMOVE_TEXT, removeText);
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_REPLACE_TEXT, replaceText);
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_NUMBER_FILES, numberFiles);
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_TRIM_NAME, trimName);
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_CHANGE_EXTENSION, changeExtension);
        return registry;
    }

    private void loadAndRegister(ModeViewRegistry registry, ViewLoaderApi viewLoaderApi,
                                 ViewNames viewName, ModeControllerV2Api<?> controller) throws IOException {
        Optional<javafx.fxml.FXMLLoader> loaderOpt = viewLoaderApi.createLoader(viewName);
        if (loaderOpt.isEmpty()) {
            throw new IllegalStateException("Could not find FXMLLoader for " + viewName);
        }
        var fxmlLoader = loaderOpt.get();
        Parent parent = fxmlLoader.load();
        registry.register(controller.supportedMode(), () -> parent, controller);
    }

    /**
     * Provides a {@link FxStateMirror} singleton.
     *
     * @return a new {@link FxStateMirror}; never null
     */
    @Provides
    @Singleton
    public FxStateMirror provideFxStateMirror() {
        return new FxStateMirror();
    }

    /**
     * Provides the {@link StatePublisher} backed by the {@link FxStateMirror}.
     *
     * @param mirror the {@link FxStateMirror} to delegate to; never null
     * @return the mirror as a {@link StatePublisher}; never null
     */
    @Provides
    @Singleton
    public StatePublisher provideStatePublisher(FxStateMirror mirror) {
        return mirror;
    }

}
