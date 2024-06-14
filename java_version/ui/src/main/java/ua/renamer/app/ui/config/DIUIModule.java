package ua.renamer.app.ui.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.util.BuilderFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.ui.controller.ApplicationMainViewController;
import ua.renamer.app.ui.controller.mode.ModeControllerApi;
import ua.renamer.app.ui.controller.mode.impl.*;
import ua.renamer.app.ui.converter.*;
import ua.renamer.app.ui.enums.ViewNames;
import ua.renamer.app.ui.service.ViewLoaderApi;
import ua.renamer.app.ui.service.impl.CoreFunctionalityHelper;
import ua.renamer.app.ui.service.impl.MainViewControllerHelper;
import ua.renamer.app.ui.service.impl.ViewLoaderService;
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

import static ua.renamer.app.ui.config.InjectQualifiers.*;

/**
 * Dependency Injection module configuration for the application. Includes only UI module dependencies.
 */
@Slf4j
@Getter
public class DIUIModule extends AbstractModule {

    @Override
    protected void configure() {
        bindServices();
        bindCustomWidgets();
        bindStringConverters();
        bindViewControllers();
    }

    private void bindServices() {
        bind(ViewLoaderApi.class).to(ViewLoaderService.class).in(Singleton.class);
        bind(CoreFunctionalityHelper.class).in(Singleton.class);
        bind(MainViewControllerHelper.class).in(Singleton.class);
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

    @Provides
    @Singleton
    @AddCustomTextFxmlLoader
    public FXMLLoader provideAddCustomTextFxmlLoader(ViewLoaderApi viewLoaderApi) {
        return createFXMLLoader(viewLoaderApi, ViewNames.MODE_ADD_CUSTOM_TEXT);
    }

    private FXMLLoader createFXMLLoader(ViewLoaderApi viewLoaderApi, ViewNames viewName) {
        Optional<FXMLLoader> loaderOptional = viewLoaderApi.createLoader(viewName);
        if (loaderOptional.isEmpty()) {
            throw new IllegalStateException("Could not find FXMLLoader for " + viewName);
        }

        return loaderOptional.get();
    }

    @Provides
    @Singleton
    @ChangeCaseFxmlLoader
    public FXMLLoader provideChangeCaseFxmlLoader(ViewLoaderApi viewLoaderApi) {
        return createFXMLLoader(viewLoaderApi, ViewNames.MODE_CHANGE_CASE);
    }

    @Provides
    @Singleton
    @UseDatetimeFxmlLoader
    public FXMLLoader provideUseDatetimeFxmlLoader(ViewLoaderApi viewLoaderApi) {
        return createFXMLLoader(viewLoaderApi, ViewNames.MODE_USE_DATETIME);
    }

    @Provides
    @Singleton
    @UseImageDimensionsFxmlLoader
    public FXMLLoader provideUseImageDimensionsFxmlLoader(ViewLoaderApi viewLoaderApi) {
        return createFXMLLoader(viewLoaderApi, ViewNames.MODE_USE_IMAGE_DIMENSIONS);
    }

    @Provides
    @Singleton
    @UseParentFolderNameFxmlLoader
    public FXMLLoader provideUseParentFolderNameFxmlLoader(ViewLoaderApi viewLoaderApi) {
        return createFXMLLoader(viewLoaderApi, ViewNames.MODE_USE_PARENT_FOLDER_NAME);
    }

    @Provides
    @Singleton
    @RemoveCustomTextFxmlLoader
    public FXMLLoader provideRemoveCustomTextFxmlLoader(ViewLoaderApi viewLoaderApi) {
        return createFXMLLoader(viewLoaderApi, ViewNames.MODE_REMOVE_CUSTOM_TEXT);
    }

    @Provides
    @Singleton
    @ReplaceCustomTextFxmlLoader
    public FXMLLoader provideReplaceCustomTextFxmlLoader(ViewLoaderApi viewLoaderApi) {
        return createFXMLLoader(viewLoaderApi, ViewNames.MODE_REPLACE_CUSTOM_TEXT);
    }

    @Provides
    @Singleton
    @AddSequenceFxmlLoader
    public FXMLLoader provideAddSequenceFxmlLoader(ViewLoaderApi viewLoaderApi) {
        return createFXMLLoader(viewLoaderApi, ViewNames.MODE_ADD_SEQUENCE);
    }

    @Provides
    @Singleton
    @TruncateFileNameFxmlLoader
    public FXMLLoader provideTruncateFileNameFxmlLoader(ViewLoaderApi viewLoaderApi) {
        return createFXMLLoader(viewLoaderApi, ViewNames.MODE_TRUNCATE_FILE_NAME);
    }

    @Provides
    @Singleton
    @ChangeExtensionFxmlLoader
    public FXMLLoader provideChangeExtensionFxmlLoader(ViewLoaderApi viewLoaderApi) {
        return createFXMLLoader(viewLoaderApi, ViewNames.MODE_CHANGE_EXTENSION);
    }

    @Provides
    @Singleton
    @AddCustomTextParent
    public Parent provideAddCustomTextParent(@AddCustomTextFxmlLoader FXMLLoader loader) throws IOException {
        return loader.load();
    }

    @Provides
    @Singleton
    @ChangeCaseParent
    public Parent provideChangeCaseParent(@ChangeCaseFxmlLoader FXMLLoader loader) throws IOException {
        return loader.load();
    }

    @Provides
    @Singleton
    @UseDatetimeParent
    public Parent provideUseDatetimeParent(@UseDatetimeFxmlLoader FXMLLoader loader) throws IOException {
        return loader.load();
    }

    @Provides
    @Singleton
    @UseImageDimensionsParent
    public Parent provideUseImageDimensionsParent(@UseImageDimensionsFxmlLoader FXMLLoader loader) throws IOException {
        return loader.load();
    }

    @Provides
    @Singleton
    @UseParentFolderNameParent
    public Parent provideUseParentFolderNameParent(
            @UseParentFolderNameFxmlLoader FXMLLoader loader) throws IOException {
        return loader.load();
    }

    @Provides
    @Singleton
    @RemoveCustomTextParent
    public Parent provideRemoveCustomTextParent(@RemoveCustomTextFxmlLoader FXMLLoader loader) throws IOException {
        return loader.load();
    }

    @Provides
    @Singleton
    @ReplaceCustomTextParent
    public Parent provideReplaceCustomTextParent(@ReplaceCustomTextFxmlLoader FXMLLoader loader) throws IOException {
        return loader.load();
    }

    @Provides
    @Singleton
    @AddSequenceParent
    public Parent provideAddSequenceParent(@AddSequenceFxmlLoader FXMLLoader loader) throws IOException {
        return loader.load();
    }

    @Provides
    @Singleton
    @TruncateFileNameParent
    public Parent provideTruncateFileNameParent(@TruncateFileNameFxmlLoader FXMLLoader loader) throws IOException {
        return loader.load();
    }

    @Provides
    @Singleton
    @ChangeExtensionParent
    public Parent provideChangeExtensionParent(@ChangeExtensionFxmlLoader FXMLLoader loader) throws IOException {
        return loader.load();
    }

    @Provides
    @Singleton
    @AddCustomTextController
    public ModeControllerApi provideAddCustomTextController(@AddCustomTextFxmlLoader FXMLLoader loader,
                                                            @AddCustomTextParent Parent parent) {
        return loader.getController();
    }

    @Provides
    @Singleton
    @ChangeCaseController
    public ModeControllerApi provideChangeCaseController(@ChangeCaseFxmlLoader FXMLLoader loader,
                                                         @ChangeCaseParent Parent parent) {
        return loader.getController();
    }

    @Provides
    @Singleton
    @UseDatetimeController
    public ModeControllerApi provideUseDatetimeController(@UseDatetimeFxmlLoader FXMLLoader loader,
                                                          @UseDatetimeParent Parent parent) {
        return loader.getController();
    }

    @Provides
    @Singleton
    @UseImageDimensionsController
    public ModeControllerApi provideUseImageDimensionsController(@UseImageDimensionsFxmlLoader FXMLLoader loader,
                                                                 @UseImageDimensionsParent Parent parent) {
        return loader.getController();
    }

    @Provides
    @Singleton
    @UseParentFolderNameController
    public ModeControllerApi provideUseParentFolderNameController(@UseParentFolderNameFxmlLoader FXMLLoader loader,
                                                                  @UseParentFolderNameParent Parent parent) {
        return loader.getController();
    }

    @Provides
    @Singleton
    @RemoveCustomTextController
    public ModeControllerApi provideRemoveCustomTextController(@RemoveCustomTextFxmlLoader FXMLLoader loader,
                                                               @RemoveCustomTextParent Parent parent) {
        return loader.getController();
    }

    @Provides
    @Singleton
    @ReplaceCustomTextController
    public ModeControllerApi provideReplaceCustomTextController(@ReplaceCustomTextFxmlLoader FXMLLoader loader,
                                                                @ReplaceCustomTextParent Parent parent) {
        return loader.getController();
    }

    @Provides
    @Singleton
    @AddSequenceController
    public ModeControllerApi provideAddSequenceController(@AddSequenceFxmlLoader FXMLLoader loader,
                                                          @AddSequenceParent Parent parent) {
        return loader.getController();
    }

    @Provides
    @Singleton
    @TruncateFileNameController
    public ModeControllerApi provideTruncateFileNameController(@TruncateFileNameFxmlLoader FXMLLoader loader,
                                                               @TruncateFileNameParent Parent parent) {
        return loader.getController();
    }

    @Provides
    @Singleton
    @ChangeExtensionController
    public ModeControllerApi provideChangeExtensionController(@ChangeExtensionFxmlLoader FXMLLoader loader,
                                                              @ChangeExtensionParent Parent parent) {
        return loader.getController();
    }

    @Provides
    @Singleton
    public ObservableList<RenameModel> provideAppGlobalRenameModelList() {
        return FXCollections.observableArrayList();
    }

}

