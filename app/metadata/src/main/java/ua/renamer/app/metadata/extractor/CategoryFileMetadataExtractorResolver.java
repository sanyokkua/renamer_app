package ua.renamer.app.metadata.extractor;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.interfaces.FileMetadataExtractor;
import ua.renamer.app.api.interfaces.FileMetadataExtractorResolver;
import ua.renamer.app.metadata.extractor.strategy.AudioFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.GenericFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.ImageFileMetadataExtractionExtractor;
import ua.renamer.app.metadata.extractor.strategy.VideoFileMetadataExtractor;

/**
 * Resolves the correct {@link FileMetadataExtractor} strategy based on the file {@link Category}.
 */
@Slf4j
public class CategoryFileMetadataExtractorResolver implements FileMetadataExtractorResolver {
    private final GenericFileMetadataExtractor genericFileMetadataExtractor;
    private final ImageFileMetadataExtractionExtractor imageFileMetadataExtractor;
    private final AudioFileMetadataExtractor audioFileMetadataExtractor;
    private final VideoFileMetadataExtractor videoFileMetadataExtractor;


    /**
     * @param genericFileMetadataExtractor fallback extractor for unknown categories
     * @param imageFileMetadataExtractor   extractor for image files
     * @param audioFileMetadataExtractor   extractor for audio files
     * @param videoFileMetadataExtractor   extractor for video files
     */
    @Inject
    public CategoryFileMetadataExtractorResolver(GenericFileMetadataExtractor genericFileMetadataExtractor,
                                                 ImageFileMetadataExtractionExtractor imageFileMetadataExtractor,
                                                 AudioFileMetadataExtractor audioFileMetadataExtractor,
                                                 VideoFileMetadataExtractor videoFileMetadataExtractor) {
        this.genericFileMetadataExtractor = genericFileMetadataExtractor;
        this.imageFileMetadataExtractor = imageFileMetadataExtractor;
        this.audioFileMetadataExtractor = audioFileMetadataExtractor;
        this.videoFileMetadataExtractor = videoFileMetadataExtractor;
    }


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
