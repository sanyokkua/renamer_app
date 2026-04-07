package ua.renamer.app.metadata.extractor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.interfaces.FileMetadataExtractor;
import ua.renamer.app.api.interfaces.FileMetadataExtractorResolver;
import ua.renamer.app.api.model.meta.FileMeta;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ThreadAwareFileMetadataMapperTest {

    @Mock
    private FileMetadataExtractorResolver resolver;
    @Mock
    private FileMetadataExtractor strategyExtractor;
    @Mock
    private File mockFile;

    private ThreadAwareFileMetadataMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ThreadAwareFileMetadataMapper(resolver);
        when(mockFile.getName()).thenReturn("test.jpg");
    }

    @Test
    void testExtract_NormalFlow_ReturnsResult() {
        FileMeta expected = FileMeta.empty();
        when(resolver.getFileMetadataExtractor(Category.IMAGE)).thenReturn(strategyExtractor);
        when(strategyExtractor.extract(mockFile, "image/jpeg")).thenReturn(expected);

        FileMeta result = mapper.extract(mockFile, Category.IMAGE, "image/jpeg");

        assertNotNull(result);
        assertSame(expected, result);
        verify(resolver).getFileMetadataExtractor(Category.IMAGE);
        verify(strategyExtractor).extract(mockFile, "image/jpeg");
    }

    @Test
    void testExtract_ExtractorThrowsException_ReturnsErrorFileMeta() {
        RuntimeException ex = new RuntimeException("Extraction failed");
        when(resolver.getFileMetadataExtractor(any())).thenReturn(strategyExtractor);
        when(strategyExtractor.extract(any(), any())).thenThrow(ex);

        FileMeta result = mapper.extract(mockFile, Category.AUDIO, "audio/mpeg");

        assertNotNull(result);
        assertFalse(result.getErrors().isEmpty(), "Should capture exception as error");
    }

    @Test
    void testExtract_DifferentCategories_CorrectResolverCalled() {
        FileMeta expected = FileMeta.empty();
        when(resolver.getFileMetadataExtractor(Category.VIDEO)).thenReturn(strategyExtractor);
        when(strategyExtractor.extract(any(), any())).thenReturn(expected);

        FileMeta result = mapper.extract(mockFile, Category.VIDEO, "video/mp4");

        assertNotNull(result);
        verify(resolver).getFileMetadataExtractor(Category.VIDEO);
    }
}
