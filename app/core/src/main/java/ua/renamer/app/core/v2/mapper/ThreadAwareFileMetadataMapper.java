package ua.renamer.app.core.v2.mapper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.core.v2.interfaces.FileMetadataExtractor;
import ua.renamer.app.core.v2.interfaces.FileMetadataExtractorResolver;
import ua.renamer.app.core.v2.interfaces.FileMetadataMapper;
import ua.renamer.app.core.v2.model.Category;
import ua.renamer.app.core.v2.model.meta.FileMeta;

import java.io.File;

@RequiredArgsConstructor
public class ThreadAwareFileMetadataMapper implements FileMetadataMapper {
    private final FileMetadataExtractorResolver fileMetadataExtractorResolver;

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
