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
        bind(ModeAddCustomTextController.class).in(Singleton.class);
        bind(ModeAddSequenceController.class).in(Singleton.class);
        bind(ModeChangeCaseController.class).in(Singleton.class);
        bind(ModeChangeExtensionController.class).in(Singleton.class);
        bind(ModeRemoveCustomTextController.class).in(Singleton.class);
        bind(ModeReplaceCustomTextController.class).in(Singleton.class);
        bind(ModeTruncateFileNameController.class).in(Singleton.class);
        bind(ModeUseDatetimeController.class).in(Singleton.class);
        bind(ModeUseImageDimensionsController.class).in(Singleton.class);
        bind(ModeUseParentFolderNameController.class).in(Singleton.class);
        bind(ApplicationMainViewController.class).in(Singleton.class);
    }

    /**
     * Provides a fully populated {@link ModeViewRegistry} by loading each mode's FXML
     * and pairing its loaded {@link Parent} view with its injected controller.
     *
     * @param viewLoaderApi      the service that resolves FXML files by view name
     * @param addCustomText      controller for ADD_TEXT mode
     * @param changeCase         controller for CHANGE_CASE mode
     * @param useDatetime        controller for USE_DATETIME mode
     * @param useImageDimensions controller for USE_IMAGE_DIMENSIONS mode
     * @param useParentFolderName controller for USE_PARENT_FOLDER_NAME mode
     * @param removeCustomText   controller for REMOVE_TEXT mode
     * @param replaceCustomText  controller for REPLACE_TEXT mode
     * @param addSequence        controller for ADD_SEQUENCE mode
     * @param truncateFileName   controller for TRUNCATE_FILE_NAME mode
     * @param changeExtension    controller for CHANGE_EXTENSION mode
     * @return a populated registry; never null
     * @throws IOException if any FXML file cannot be loaded
     */
    @Provides
    @Singleton
    public ModeViewRegistry provideModeViewRegistry(
            ViewLoaderApi viewLoaderApi,
            ModeAddCustomTextController addCustomText,
            ModeChangeCaseController changeCase,
            ModeUseDatetimeController useDatetime,
            ModeUseImageDimensionsController useImageDimensions,
            ModeUseParentFolderNameController useParentFolderName,
            ModeRemoveCustomTextController removeCustomText,
            ModeReplaceCustomTextController replaceCustomText,
            ModeAddSequenceController addSequence,
            ModeTruncateFileNameController truncateFileName,
            ModeChangeExtensionController changeExtension) throws IOException {
        var registry = new ModeViewRegistry();
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_ADD_CUSTOM_TEXT, addCustomText);
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_CHANGE_CASE, changeCase);
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_USE_DATETIME, useDatetime);
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_USE_IMAGE_DIMENSIONS, useImageDimensions);
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_USE_PARENT_FOLDER_NAME, useParentFolderName);
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_REMOVE_CUSTOM_TEXT, removeCustomText);
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_REPLACE_CUSTOM_TEXT, replaceCustomText);
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_ADD_SEQUENCE, addSequence);
        loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_TRUNCATE_FILE_NAME, truncateFileName);
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
