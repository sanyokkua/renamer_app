package ua.renamer.app.metadata.extractor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.metadata.enums.Category;
import ua.renamer.app.metadata.extractor.strategy.AudioFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.GenericFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.ImageFileMetadataExtractionExtractor;
import ua.renamer.app.metadata.extractor.strategy.VideoFileMetadataExtractor;
import ua.renamer.app.metadata.interfaces.FileMetadataExtractor;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CategoryFileMetadataExtractorResolverTest {

    @Mock
    private GenericFileMetadataExtractor genericFileMetadataExtractor;
    @Mock
    private ImageFileMetadataExtractionExtractor imageFileMetadataExtractor;
    @Mock
    private AudioFileMetadataExtractor audioFileMetadataExtractor;
    @Mock
    private VideoFileMetadataExtractor videoFileMetadataExtractor;

    private CategoryFileMetadataExtractorResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CategoryFileMetadataExtractorResolver(
                genericFileMetadataExtractor, imageFileMetadataExtractor,
                audioFileMetadataExtractor, videoFileMetadataExtractor);
    }

    @Test
    void testGetFileMetadataExtractor_Image_ReturnsImageExtractor() {
        FileMetadataExtractor result = resolver.getFileMetadataExtractor(Category.IMAGE);
        assertSame(imageFileMetadataExtractor, result);
    }

    @Test
    void testGetFileMetadataExtractor_Audio_ReturnsAudioExtractor() {
        FileMetadataExtractor result = resolver.getFileMetadataExtractor(Category.AUDIO);
        assertSame(audioFileMetadataExtractor, result);
    }

    @Test
    void testGetFileMetadataExtractor_Video_ReturnsVideoExtractor() {
        FileMetadataExtractor result = resolver.getFileMetadataExtractor(Category.VIDEO);
        assertSame(videoFileMetadataExtractor, result);
    }

    @Test
    void testGetFileMetadataExtractor_Generic_ReturnsGenericExtractor() {
        FileMetadataExtractor result = resolver.getFileMetadataExtractor(Category.GENERIC);
        assertSame(genericFileMetadataExtractor, result);
    }
}
