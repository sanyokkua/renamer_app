package ua.renamer.app.metadata.extractor;

import jakarta.inject.Inject;
import lombok.NonNull;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.interfaces.FileMetadataExtractor;
import ua.renamer.app.api.interfaces.FileMetadataExtractorResolver;
import ua.renamer.app.api.interfaces.FileMetadataMapper;
import ua.renamer.app.api.model.meta.FileMeta;

import java.io.File;

public class ThreadAwareFileMetadataMapper implements FileMetadataMapper {
    private final FileMetadataExtractorResolver fileMetadataExtractorResolver;

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
            return FileMeta.withError(e);
        }
    }
}
