package ua.renamer.app.ui.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.core.service.TextExtractorByKey;
import ua.renamer.app.core.service.command.ListProcessingCommand;
import ua.renamer.app.core.service.command.impl.FixEqualNamesCommand;
import ua.renamer.app.core.service.command.impl.MapFileInformationToRenameModelCommand;
import ua.renamer.app.core.service.command.impl.MapFileToFileInformationCommand;
import ua.renamer.app.core.service.command.impl.RenameCommand;
import ua.renamer.app.core.service.file.BasicFileAttributesExtractor;
import ua.renamer.app.core.service.helper.DateTimeOperations;
import ua.renamer.app.core.service.mapper.DataMapper;
import ua.renamer.app.core.service.mapper.FileToMetadataMapper;
import ua.renamer.app.core.service.mapper.impl.FileToFileInformationMapper;
import ua.renamer.app.core.service.mapper.impl.RenameModelToHtmlMapper;
import ua.renamer.app.core.service.mapper.impl.metadata.NullMapper;
import ua.renamer.app.core.service.mapper.impl.metadata.audio.Mp3Mapper;
import ua.renamer.app.core.service.mapper.impl.metadata.audio.WavMapper;
import ua.renamer.app.core.service.mapper.impl.metadata.images.*;
import ua.renamer.app.core.service.mapper.impl.metadata.video.AviMapper;
import ua.renamer.app.core.service.mapper.impl.metadata.video.Mp4Mapper;
import ua.renamer.app.core.service.mapper.impl.metadata.video.QuickTimeMapper;
import ua.renamer.app.core.service.validator.impl.NameValidator;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Dependency Injection module configuration for the application. Includes only core module dependencies.
 */
@Slf4j
@Getter
public class DICoreModule extends AbstractModule {

    @Override
    protected void configure() {
        bindExternalDependencies();
        bindApplicationMappers();
        bindApplicationCommands();
        bindApplicationServices();
    }

    private void bindExternalDependencies() {
        bind(Tika.class).in(Singleton.class);
        bind(BasicFileAttributesExtractor.class).toInstance(Files::readAttributes);
    }

    private void bindApplicationMappers() {
        TypeLiteral<DataMapper<RenameModel, String>> renameModelToHtmlMapperLiteral = new TypeLiteral<>() {};
        TypeLiteral<DataMapper<File, FileInformation>> fileToFileInfoMapperLiteral = new TypeLiteral<>() {};
        bind(renameModelToHtmlMapperLiteral).to(RenameModelToHtmlMapper.class).in(Singleton.class);
        bind(fileToFileInfoMapperLiteral).to(FileToFileInformationMapper.class).in(Singleton.class);
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
    }

    private void bindApplicationCommands() {
        TypeLiteral<ListProcessingCommand<File, FileInformation>> fileToFileInfoCmdLiteral = new TypeLiteral<>() {};
        bind(fileToFileInfoCmdLiteral).to(MapFileToFileInformationCommand.class).in(Singleton.class);
        bind(MapFileInformationToRenameModelCommand.class).in(Singleton.class);
        bind(FixEqualNamesCommand.class).in(Singleton.class);
        bind(RenameCommand.class).in(Singleton.class);
    }

    private void bindApplicationServices() {
        bind(NameValidator.class).in(Singleton.class);
        bind(DateTimeOperations.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public FileToMetadataMapper provideFileToMetadataMapper(AviMapper aviMapper, BmpMapper bmpMapper,
                                                            EpsMapper epsMapper, GifMapper gifMapper,
                                                            HeifMapper heifMapper, IcoMapper icoMapper,
                                                            JpegMapper jpegMapper, Mp3Mapper mp3Mapper,
                                                            Mp4Mapper mp4Mapper, PcxMapper pcxMapper,
                                                            PngMapper pngMapper, PsdMapper psdMapper,
                                                            QuickTimeMapper quickTimeMapper, TiffMapper tiffMapper,
                                                            WavMapper wavMapper, WebPMapper webPmapper,
                                                            NullMapper nullMapper) {
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

        var supportedExtensions = Stream.of(nullMapper,
                                            aviMapper,
                                            bmpMapper,
                                            epsMapper,
                                            gifMapper,
                                            heifMapper,
                                            icoMapper,
                                            jpegMapper,
                                            mp3Mapper,
                                            mp4Mapper,
                                            pcxMapper,
                                            pngMapper,
                                            psdMapper,
                                            quickTimeMapper,
                                            tiffMapper,
                                            wavMapper,
                                            webPmapper)
                                        .flatMap(v -> v.getSupportedExtensions().stream())
                                        .collect(Collectors.joining(","));
        log.info("Supported extensions: {}", supportedExtensions);
        return nullMapper;
    }

    @Provides
    @Singleton
    public TextExtractorByKey provideTextExtractorByKey(LanguageTextRetrieverApi languageTextRetrieverApi) {
        return s -> languageTextRetrieverApi.getString(s, "");
    }
}

