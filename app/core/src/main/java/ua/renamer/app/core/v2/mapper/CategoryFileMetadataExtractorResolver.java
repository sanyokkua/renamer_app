package ua.renamer.app.core.v2.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import ua.renamer.app.core.v2.interfaces.FileMetadataExtractor;
import ua.renamer.app.core.v2.interfaces.FileMetadataExtractorResolver;
import ua.renamer.app.core.v2.mapper.strategy.AudioFileMetadataExtractor;
import ua.renamer.app.core.v2.mapper.strategy.GenericFileMetadataExtractor;
import ua.renamer.app.core.v2.mapper.strategy.ImageFileMetadataExtractionExtractor;
import ua.renamer.app.core.v2.mapper.strategy.VideoFileMetadataExtractor;
import ua.renamer.app.core.v2.model.Category;

@Slf4j
@RequiredArgsConstructor
public class CategoryFileMetadataExtractorResolver implements FileMetadataExtractorResolver {
    private final GenericFileMetadataExtractor genericFileMetadataExtractor;
    private final ImageFileMetadataExtractionExtractor imageFileMetadataExtractor;
    private final AudioFileMetadataExtractor audioFileMetadataExtractor;
    private final VideoFileMetadataExtractor videoFileMetadataExtractor;


    @Override
    public FileMetadataExtractor getFileMetadataExtractor(@NonNull Category category) {
        if (category == Category.IMAGE) {
            log.debug("Image extractor group selected");
            return imageFileMetadataExtractor;
        } else if (category == Category.AUDIO) {
            log.debug("Audio extractor group selected");
            return audioFileMetadataExtractor;
        } else if (category == Category.VIDEO) {
            log.debug("Video extractor group selected");
            return videoFileMetadataExtractor;
        }
        log.debug("Generic extractor group selected");
        return genericFileMetadataExtractor;
    }
}
