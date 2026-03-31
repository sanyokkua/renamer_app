package ua.renamer.app.metadata.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import ua.renamer.app.metadata.extractor.CategoryFileMetadataExtractorResolver;
import ua.renamer.app.metadata.extractor.ThreadAwareFileMetadataMapper;
import ua.renamer.app.metadata.extractor.strategy.AudioFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.GenericFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.ImageFileMetadataExtractionExtractor;
import ua.renamer.app.metadata.extractor.strategy.VideoFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.audio.UnifiedAudioFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.*;
import ua.renamer.app.metadata.extractor.strategy.format.video.AviFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.video.Mp4FileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.video.QuickTimeFileMetadataExtractor;
import ua.renamer.app.metadata.interfaces.DateTimeUtils;
import ua.renamer.app.metadata.interfaces.FileMetadataExtractorResolver;
import ua.renamer.app.metadata.interfaces.FileMetadataMapper;
import ua.renamer.app.metadata.interfaces.FileUtils;
import ua.renamer.app.metadata.util.CommonFileUtils;
import ua.renamer.app.metadata.util.DateTimeConverter;

public class DIMetadataModule extends AbstractModule {

    @Override
    protected void configure() {
        // Core utilities
        bind(DateTimeConverter.class).in(Singleton.class);
        bind(DateTimeUtils.class).to(DateTimeConverter.class).in(Singleton.class);
        bind(CommonFileUtils.class).in(Singleton.class);
        bind(FileUtils.class).to(CommonFileUtils.class).in(Singleton.class);

        // Top-level mapper and resolver
        bind(FileMetadataMapper.class).to(ThreadAwareFileMetadataMapper.class).in(Singleton.class);
        bind(FileMetadataExtractorResolver.class).to(CategoryFileMetadataExtractorResolver.class).in(Singleton.class);

        // Category-level strategy extractors
        bind(GenericFileMetadataExtractor.class).in(Singleton.class);
        bind(ImageFileMetadataExtractionExtractor.class).in(Singleton.class);
        bind(AudioFileMetadataExtractor.class).in(Singleton.class);
        bind(VideoFileMetadataExtractor.class).in(Singleton.class);

        // Format-level image extractors
        bind(ArwFileMetadataExtractor.class).in(Singleton.class);
        bind(AvifFileMetadataExtractor.class).in(Singleton.class);
        bind(BmpFileMetadataExtractor.class).in(Singleton.class);
        bind(Cr2FileMetadataExtractor.class).in(Singleton.class);
        bind(Cr3FileMetadataExtractor.class).in(Singleton.class);
        bind(DngFileMetadataExtractor.class).in(Singleton.class);
        bind(EpsFileMetadataExtractor.class).in(Singleton.class);
        bind(GifFileMetadataExtractor.class).in(Singleton.class);
        bind(HeifFileMetadataExtractor.class).in(Singleton.class);
        bind(IcoFileMetadataExtractor.class).in(Singleton.class);
        bind(JpegFileMetadataExtractor.class).in(Singleton.class);
        bind(NefFileMetadataExtractor.class).in(Singleton.class);
        bind(OrfFileMetadataExtractor.class).in(Singleton.class);
        bind(PcxFileMetadataExtractor.class).in(Singleton.class);
        bind(PngFileMetadataExtractor.class).in(Singleton.class);
        bind(PsdFileMetadataExtractor.class).in(Singleton.class);
        bind(RafFileMetadataExtractor.class).in(Singleton.class);
        bind(Rw2FileMetadataExtractor.class).in(Singleton.class);
        bind(TiffFileMetadataExtractor.class).in(Singleton.class);
        bind(WebPFileMetadataExtractor.class).in(Singleton.class);

        // Format-level video extractors
        bind(AviFileMetadataExtractor.class).in(Singleton.class);
        bind(Mp4FileMetadataExtractor.class).in(Singleton.class);
        bind(QuickTimeFileMetadataExtractor.class).in(Singleton.class);

        // Audio extractor (unified, handles all 19+ audio formats)
        bind(UnifiedAudioFileMetadataExtractor.class).in(Singleton.class);
    }
}
