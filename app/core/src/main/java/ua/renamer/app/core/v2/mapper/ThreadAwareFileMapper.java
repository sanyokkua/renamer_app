package ua.renamer.app.core.v2.mapper;

import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.enums.MimeTypes;
import ua.renamer.app.api.interfaces.FileMapper;
import ua.renamer.app.api.interfaces.FileMetadataMapper;
import ua.renamer.app.api.interfaces.FileUtils;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.meta.FileMeta;

import java.io.File;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ThreadAwareFileMapper implements FileMapper {
    private static final ConcurrentHashMap<String, Set<String>> MIME_EXTENSIONS_CACHE = new ConcurrentHashMap<>();

    private final FileUtils fileUtils;
    private final FileMetadataMapper fileMetadataMapper;

    @Override
    public FileModel mapFrom(File file) {
        fileUtils.validateFile(file);

        var path = file.toPath();
        var attributes = fileUtils.getBasicFileAttributes(path);

        var isFile = attributes.isRegularFile();
        var fileSize = attributes.size();

        var name = fileUtils.getFileBaseName(path);
        var absolutePath = fileUtils.getFileAbsolutePath(path);
        var extension = fileUtils.getFileExtension(path);

        var creationDate = fileUtils.getFileCreationDate(attributes);
        var modificationDate = fileUtils.getFileModificationDate(attributes);

        // Only detect MIME type for regular files
        var mimeType = isFile ? fileUtils.getFileMimeType(path) : "application/x-directory";
        var category = determineCategory(mimeType);
        var detectedExtensions = resolveDetectedExtensions(mimeType);
        var rawMeta = fileMetadataMapper.extract(file, category, mimeType);
        var fileMeta = Optional.ofNullable(rawMeta).orElse(FileMeta.empty());

        return FileModel.builder()
                .withFile(file)
                .withName(name)
                .withAbsolutePath(absolutePath)
                .withIsFile(isFile)
                .withExtension(extension)
                .withFileSize(fileSize)
                .withCreationDate(creationDate)
                .withModificationDate(modificationDate)
                .withDetectedMimeType(mimeType)
                .withDetectedExtensions(detectedExtensions)
                .withCategory(category)
                .withMetadata(fileMeta)
                .build();
    }

    private Category determineCategory(String mimeType) {
        if (StringUtils.isBlank(mimeType)) {
            return Category.GENERIC;
        }

        if (mimeType.startsWith("image/")) {
            return Category.IMAGE;
        } else if (mimeType.startsWith("audio/")) {
            return Category.AUDIO;
        } else if (mimeType.startsWith("video/")) {
            return Category.VIDEO;
        }

        return Category.GENERIC;
    }

    private Set<String> resolveDetectedExtensions(String mimeType) {
        if (StringUtils.isBlank(mimeType)) {
            return Collections.emptySet();
        }

        return MIME_EXTENSIONS_CACHE.computeIfAbsent(mimeType, this::findExtensions);
    }

    private Set<String> findExtensions(String mimeType) {
        return Stream.of(MimeTypes.values())
                .filter(mime -> mime.getMime().equalsIgnoreCase(mimeType))
                .findFirst()
                .map(MimeTypes::getExtensions)
                .orElse(Collections.emptySet());
    }
}
