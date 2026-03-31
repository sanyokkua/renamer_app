package ua.renamer.app.metadata.extractor.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.metadata.extractor.strategy.format.video.AviFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.video.Mp4FileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.video.QuickTimeFileMetadataExtractor;
import ua.renamer.app.api.model.meta.FileMeta;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoFileMetadataExtractorTest {

    @Mock
    private AviFileMetadataExtractor aviFileMetadataExtractor;
    @Mock
    private Mp4FileMetadataExtractor mp4FileMetadataExtractor;
    @Mock
    private QuickTimeFileMetadataExtractor quickTimeFileMetadataExtractor;
    @Mock
    private File mockFile;

    private VideoFileMetadataExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new VideoFileMetadataExtractor(aviFileMetadataExtractor, mp4FileMetadataExtractor, quickTimeFileMetadataExtractor);
        when(mockFile.getName()).thenReturn("test.video");
    }

    @Test
    void testExtract_AviMimeType_DelegatesToAvi() {
        when(aviFileMetadataExtractor.extract(any(), any())).thenReturn(FileMeta.empty());

        FileMeta result = extractor.extract(mockFile, "video/x-msvideo");

        assertNotNull(result);
        verify(aviFileMetadataExtractor).extract(mockFile, "video/x-msvideo");
        verify(mp4FileMetadataExtractor, never()).extract(any(), any());
        verify(quickTimeFileMetadataExtractor, never()).extract(any(), any());
    }

    @Test
    void testExtract_Mp4MimeType_DelegatesToMp4() {
        when(mp4FileMetadataExtractor.extract(any(), any())).thenReturn(FileMeta.empty());

        FileMeta result = extractor.extract(mockFile, "video/mp4");

        assertNotNull(result);
        verify(mp4FileMetadataExtractor).extract(mockFile, "video/mp4");
        verify(aviFileMetadataExtractor, never()).extract(any(), any());
        verify(quickTimeFileMetadataExtractor, never()).extract(any(), any());
    }

    @Test
    void testExtract_QuickTimeMimeType_DelegatesToQuickTime() {
        when(quickTimeFileMetadataExtractor.extract(any(), any())).thenReturn(FileMeta.empty());

        FileMeta result = extractor.extract(mockFile, "video/quicktime");

        assertNotNull(result);
        verify(quickTimeFileMetadataExtractor).extract(mockFile, "video/quicktime");
        verify(aviFileMetadataExtractor, never()).extract(any(), any());
        verify(mp4FileMetadataExtractor, never()).extract(any(), any());
    }

    @Test
    void testExtract_UnsupportedMimeType_ReturnsError() {
        FileMeta result = extractor.extract(mockFile, "video/unknown");

        assertNotNull(result);
        assertFalse(result.getErrors().isEmpty(), "Should return error for unsupported MIME type");
        verify(aviFileMetadataExtractor, never()).extract(any(), any());
        verify(mp4FileMetadataExtractor, never()).extract(any(), any());
        verify(quickTimeFileMetadataExtractor, never()).extract(any(), any());
    }
}
