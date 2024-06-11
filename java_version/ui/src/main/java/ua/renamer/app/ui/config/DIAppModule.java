package ua.renamer.app.ui.config;

import com.google.inject.*;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.util.BuilderFactory;
import lombok.Getter;
import lombok.Setter;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.ListProcessingCommand;
import ua.renamer.app.core.service.command.impl.MapFileToFileInformation;
import ua.renamer.app.core.service.file.BasicFileAttributesExtractor;
import ua.renamer.app.core.service.helper.DateTimeOperations;
import ua.renamer.app.core.service.mapper.DataMapper;
import ua.renamer.app.core.service.mapper.FileToMetadataMapper;
import ua.renamer.app.core.service.mapper.impl.FileInformationToHtmlMapper;
import ua.renamer.app.core.service.mapper.impl.FileToFileInformationMapper;
import ua.renamer.app.core.service.mapper.impl.metadata.NullMapper;
import ua.renamer.app.core.service.mapper.impl.metadata.audio.Mp3Mapper;
import ua.renamer.app.core.service.mapper.impl.metadata.audio.WavMapper;
import ua.renamer.app.core.service.mapper.impl.metadata.images.*;
import ua.renamer.app.core.service.mapper.impl.metadata.video.AviMapper;
import ua.renamer.app.core.service.mapper.impl.metadata.video.Mp4Mapper;
import ua.renamer.app.core.service.mapper.impl.metadata.video.QuickTimeMapper;
import ua.renamer.app.ui.controller.ApplicationMainViewController;
import ua.renamer.app.ui.controller.mode.impl.*;
import ua.renamer.app.ui.converter.*;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;
import ua.renamer.app.ui.service.ViewLoaderApi;
import ua.renamer.app.ui.service.impl.LanguageTextRetrieverService;
import ua.renamer.app.ui.service.impl.ViewLoaderService;
import ua.renamer.app.ui.widget.builder.ItemPositionExtendedRadioSelectorBuilder;
import ua.renamer.app.ui.widget.builder.ItemPositionRadioSelectorBuilder;
import ua.renamer.app.ui.widget.builder.ItemPositionTruncateRadioSelectorBuilder;
import ua.renamer.app.ui.widget.builder.ItemPositionWithReplacementRadioSelectorBuilder;
import ua.renamer.app.ui.widget.factory.RadioSelectorFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Dependency Injection module configuration for the application.
 */
@Getter
public class DIAppModule extends AbstractModule {

    @Setter(onMethod_ = {@Inject})
    private Injector injector;

    /**
     * Configures the bindings for dependency injection.
     */
    @Override
    protected void configure() {
        // Request injection for this module
        requestInjection(this);

        // Bind BasicFileAttributesExtractor to a lambda using Files.readAttributes
        bind(BasicFileAttributesExtractor.class).toInstance(Files::readAttributes);

        // Bind DataMapper<FileInformation, String> to FileInformationToHtmlMapper with Singleton scope
        bind(new TypeLiteral<DataMapper<FileInformation, String>>() {
        }).to(FileInformationToHtmlMapper.class).in(Singleton.class);

        // Bind DataMapper<File, FileInformation> to FileToFileInformationMapper with Singleton scope
        bind(new TypeLiteral<DataMapper<File, FileInformation>>() {
        }).to(FileToFileInformationMapper.class).in(Singleton.class);

        // Bind ListProcessingCommand<File, FileInformation> to MapFileToFileInformation with Singleton scope
        bind(new TypeLiteral<ListProcessingCommand<File, FileInformation>>() {
        }).to(MapFileToFileInformation.class).in(Singleton.class);

        // Bind ResourceBundle to the instance created by createResourceBundle
        bind(ResourceBundle.class).toInstance(createResourceBundle());

        // Bind LanguageTextRetrieverApi to LanguageTextRetrieverService with Singleton scope
        bind(LanguageTextRetrieverApi.class).to(LanguageTextRetrieverService.class).in(Singleton.class);

        // Bind various builders and factories with Singleton scope
        bind(ItemPositionExtendedRadioSelectorBuilder.class).in(Singleton.class);
        bind(ItemPositionRadioSelectorBuilder.class).in(Singleton.class);
        bind(ItemPositionTruncateRadioSelectorBuilder.class).in(Singleton.class);
        bind(ItemPositionWithReplacementRadioSelectorBuilder.class).in(Singleton.class);
        bind(JavaFXBuilderFactory.class).in(Singleton.class);
        bind(BuilderFactory.class).to(RadioSelectorFactory.class).in(Singleton.class);
        bind(ViewLoaderApi.class).to(ViewLoaderService.class).in(Singleton.class);

        // Bind DateTimeOperations with Singleton scope
        bind(DateTimeOperations.class).in(Singleton.class);

        // Bind various mappers with Singleton scope
        bind(AviMapper.class).in(Singleton.class);
        bind(BmpMapper.class).in(Singleton.class);
        bind(EpsMapper.class).in(Singleton.class);
        bind(GifMapper.class).in(Singleton.class);
        bind(HeifMapper.class).in(Singleton.class);
        bind(IcoMapper.class).in(Singleton.class);
        bind(JpegMapper.class).in(Singleton.class);
        bind(Mp3Mapper.class).in(Singleton.class);
        bind(Mp4Mapper.class).in(Singleton.class);
        bind(PcxMapper.class).in(Singleton.class);
        bind(PngMapper.class).in(Singleton.class);
        bind(PsdMapper.class).in(Singleton.class);
        bind(QuickTimeMapper.class).in(Singleton.class);
        bind(TiffMapper.class).in(Singleton.class);
        bind(WavMapper.class).in(Singleton.class);
        bind(WebPMapper.class).in(Singleton.class);
        bind(NullMapper.class).in(Singleton.class);

        // Bind various converters with Singleton scope
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

        // Bind various mode controllers with Singleton scope
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
     * Creates a ResourceBundle based on the default locale.
     *
     * @return The created ResourceBundle.
     */
    private ResourceBundle createResourceBundle() {
        Locale locale = Locale.getDefault();
        return ResourceBundle.getBundle("langs/lang", locale);
    }

    /**
     * Provides a FileToMetadataMapper with a chain of responsibility pattern.
     *
     * @param aviMapper       The AVI metadata mapper.
     * @param bmpMapper       The BMP metadata mapper.
     * @param epsMapper       The EPS metadata mapper.
     * @param gifMapper       The GIF metadata mapper.
     * @param heifMapper      The HEIF metadata mapper.
     * @param icoMapper       The ICO metadata mapper.
     * @param jpegMapper      The JPEG metadata mapper.
     * @param mp3Mapper       The MP3 metadata mapper.
     * @param mp4Mapper       The MP4 metadata mapper.
     * @param pcxMapper       The PCX metadata mapper.
     * @param pngMapper       The PNG metadata mapper.
     * @param psdMapper       The PSD metadata mapper.
     * @param quickTimeMapper The QuickTime metadata mapper.
     * @param tiffMapper      The TIFF metadata mapper.
     * @param wavMapper       The WAV metadata mapper.
     * @param webPmapper      The WebP metadata mapper.
     * @param nullMapper      The Null metadata mapper used as the starting point in the chain.
     *
     * @return The configured FileToMetadataMapper.
     */
    @Provides
    @Singleton
    public FileToMetadataMapper provideFileToMetadataMapper(AviMapper aviMapper, BmpMapper bmpMapper, EpsMapper epsMapper, GifMapper gifMapper, HeifMapper heifMapper, IcoMapper icoMapper, JpegMapper jpegMapper, Mp3Mapper mp3Mapper, Mp4Mapper mp4Mapper, PcxMapper pcxMapper, PngMapper pngMapper, PsdMapper psdMapper, QuickTimeMapper quickTimeMapper, TiffMapper tiffMapper, WavMapper wavMapper, WebPMapper webPmapper, NullMapper nullMapper) {
        // Set the chain of responsibility for metadata mappers
        nullMapper.setNext(aviMapper);
        aviMapper.setNext(bmpMapper);
        bmpMapper.setNext(epsMapper);
        epsMapper.setNext(gifMapper);
        gifMapper.setNext(heifMapper);
        heifMapper.setNext(icoMapper);
        icoMapper.setNext(jpegMapper);
        jpegMapper.setNext(mp3Mapper);
        mp3Mapper.setNext(mp4Mapper);
        mp4Mapper.setNext(pcxMapper);
        pcxMapper.setNext(pngMapper);
        pngMapper.setNext(psdMapper);
        psdMapper.setNext(quickTimeMapper);
        quickTimeMapper.setNext(tiffMapper);
        tiffMapper.setNext(wavMapper);
        wavMapper.setNext(webPmapper);
        return nullMapper;
    }

}
