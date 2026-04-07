package ua.renamer.app.metadata.extractor;

import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.interfaces.FileMetadataExtractor;
import ua.renamer.app.api.interfaces.FileMetadataExtractorResolver;
import ua.renamer.app.api.interfaces.FileMetadataMapper;
import ua.renamer.app.api.model.meta.FileMeta;

import java.io.File;

/**
 * Dispatches metadata extraction to the appropriate strategy resolved by {@link FileMetadataExtractorResolver}.
 */
@Slf4j
public class ThreadAwareFileMetadataMapper implements FileMetadataMapper {
    private final FileMetadataExtractorResolver fileMetadataExtractorResolver;


    /**
     * @param fileMetadataExtractorResolver resolves the per-category extraction strategy
     */
    @Inject
    public ThreadAwareFileMetadataMapper(FileMetadataExtractorResolver fileMetadataExtractorResolver) {
        this.fileMetadataExtractorResolver = fileMetadataExtractorResolver;
    }

    @Override
    public FileMeta extract(@NonNull File file, @NonNull Category category, @NonNull String mimeType) {
        FileMetadataExtractor strategy = fileMetadataExtractorResolver.getFileMetadataExtractor(category);

        try {
            return strategy.extract(file, mimeType);
        } catch (Exception e) {
            log.debug("Metadata extraction failed for file '{}' (category={}, mimeType={}): {}", file.getName(), category, mimeType, e.getMessage());
            return FileMeta.withError(e);
        }
    }
}
