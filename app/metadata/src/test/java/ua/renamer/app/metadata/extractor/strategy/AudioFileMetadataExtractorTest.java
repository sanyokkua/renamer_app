package ua.renamer.app.metadata.extractor.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ua.renamer.app.metadata.extractor.strategy.format.audio.UnifiedAudioFileMetadataExtractor;
import ua.renamer.app.metadata.model.meta.FileMeta;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AudioFileMetadataExtractorTest {

    @Mock
    private UnifiedAudioFileMetadataExtractor unifiedAudioFileMetadataExtractor;

    @Mock
    private File mockFile;

    private AudioFileMetadataExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new AudioFileMetadataExtractor(unifiedAudioFileMetadataExtractor);
        when(mockFile.getName()).thenReturn("test.audio");
        when(unifiedAudioFileMetadataExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "audio/mp4",
        "audio/mpeg",
        "audio/mp2",
        "audio/wav",
        "audio/flac",
        "audio/ogg",
        "audio/x-ms-wma",
        "audio/aiff",
        "audio/x-aiff",
        "audio/x-ape",
        "audio/x-musepack",
        "audio/x-wavpack",
        "audio/speex",
        "audio/opus",
        "audio/basic",
        "audio/dsf",
        "audio/x-pn-realaudio",
        "audio/x-optimfrog",
        "audio/x-tta"
    })
    void testExtract_SupportedMimeType_DelegatesToUnified(String mimeType) {
        FileMeta result = extractor.extract(mockFile, mimeType);

        assertNotNull(result);
        verify(unifiedAudioFileMetadataExtractor).extract(mockFile, mimeType);
    }

    @Test
    void testExtract_UnsupportedMimeType_ReturnsError() {
        String unsupported = "audio/unknown-format";

        FileMeta result = extractor.extract(mockFile, unsupported);

        assertNotNull(result);
        assertFalse(result.getErrors().isEmpty(), "Should return error for unsupported MIME type");
        verify(unifiedAudioFileMetadataExtractor, never()).extract(any(), any());
    }
}
