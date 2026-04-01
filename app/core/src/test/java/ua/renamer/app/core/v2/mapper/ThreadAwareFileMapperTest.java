package ua.renamer.app.core.v2.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.renamer.app.api.interfaces.DateTimeUtils;
import ua.renamer.app.api.interfaces.FileMetadataMapper;
import ua.renamer.app.api.interfaces.FileUtils;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.core.v2.util.TestDateTimeUtils;
import ua.renamer.app.core.v2.util.TestFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ThreadAwareFileMapper} null-safety when the
 * {@code FileMetadataMapper} chain returns {@code null} for unrecognized file types.
 */
class ThreadAwareFileMapperTest {

    @TempDir
    Path tempDir;

    private ThreadAwareFileMapper createMapper(FileMetadataMapper metadataMapper) {
        DateTimeUtils dateTimeUtils = new TestDateTimeUtils();
        FileUtils fileUtils = new TestFileUtils(dateTimeUtils);
        return new ThreadAwareFileMapper(fileUtils, metadataMapper);
    }

    @Test
    void givenNullFromMetadataChain_whenMapped_thenFileModelMetadataIsEmptyFileMeta() throws IOException {
        // Arrange
        FileMetadataMapper nullReturningMapper = (file, category, mimeType) -> null;
        ThreadAwareFileMapper mapper = createMapper(nullReturningMapper);
        Path testFile = tempDir.resolve("unknown.xyz");
        Files.writeString(testFile, "content");

        // Act
        FileModel result = mapper.mapFrom(testFile.toFile());

        // Assert — null from the chain is replaced by FileMeta.empty() (non-null sentinel),
        // so getMetadata() returns a present Optional wrapping FileMeta.empty().
        assertNotNull(result);
        assertTrue(result.getMetadata().isPresent());
        assertEquals(FileMeta.empty(), result.getMetadata().get());
        assertDoesNotThrow(() -> result.getMetadata().orElse(null));
    }

    @Test
    void givenUnknownFileType_whenMapped_thenMetadataIsEmptyFileMeta() throws IOException {
        // Arrange — simulate chain returning null for unrecognized type
        FileMetadataMapper unknownTypeMapper = (file, category, mimeType) -> null;
        ThreadAwareFileMapper mapper = createMapper(unknownTypeMapper);
        Path testFile = tempDir.resolve("file.unknownxyz");
        Files.writeString(testFile, "data");

        // Act
        FileModel result = mapper.mapFrom(testFile.toFile());

        // Assert — null is replaced by FileMeta.empty(); metadata Optional is present, not empty
        assertTrue(result.getMetadata().isPresent());
        assertEquals(FileMeta.empty(), result.getMetadata().get());
    }

    @Test
    void givenKnownFileType_whenMapped_thenMetadataIsPresentOptional() throws IOException {
        // Arrange — chain returns a valid FileMeta
        FileMeta expectedMeta = FileMeta.empty();
        FileMetadataMapper knownTypeMapper = (file, category, mimeType) -> expectedMeta;
        ThreadAwareFileMapper mapper = createMapper(knownTypeMapper);
        Path testFile = tempDir.resolve("image.jpg");
        Files.writeString(testFile, "fake jpg content");

        // Act
        FileModel result = mapper.mapFrom(testFile.toFile());

        // Assert
        assertTrue(result.getMetadata().isPresent());
        assertEquals(expectedMeta, result.getMetadata().get());
    }
}
