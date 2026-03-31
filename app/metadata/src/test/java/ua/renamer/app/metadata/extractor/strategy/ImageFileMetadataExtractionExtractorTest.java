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
import ua.renamer.app.metadata.extractor.strategy.format.image.*;
import ua.renamer.app.api.model.meta.FileMeta;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImageFileMetadataExtractionExtractorTest {

    @Mock private BmpFileMetadataExtractor bmpExtractor;
    @Mock private EpsFileMetadataExtractor epsExtractor;
    @Mock private GifFileMetadataExtractor gifExtractor;
    @Mock private HeifFileMetadataExtractor heifExtractor;
    @Mock private IcoFileMetadataExtractor icoExtractor;
    @Mock private JpegFileMetadataExtractor jpegExtractor;
    @Mock private PcxFileMetadataExtractor pcxExtractor;
    @Mock private PngFileMetadataExtractor pngExtractor;
    @Mock private PsdFileMetadataExtractor psdExtractor;
    @Mock private TiffFileMetadataExtractor tiffExtractor;
    @Mock private WebPFileMetadataExtractor webPExtractor;
    @Mock private AvifFileMetadataExtractor avifExtractor;
    @Mock private ArwFileMetadataExtractor arwExtractor;
    @Mock private Cr2FileMetadataExtractor cr2Extractor;
    @Mock private Cr3FileMetadataExtractor cr3Extractor;
    @Mock private NefFileMetadataExtractor nefExtractor;
    @Mock private OrfFileMetadataExtractor orfExtractor;
    @Mock private RafFileMetadataExtractor rafExtractor;
    @Mock private Rw2FileMetadataExtractor rw2Extractor;
    @Mock private DngFileMetadataExtractor dngExtractor;
    @Mock private File mockFile;

    private ImageFileMetadataExtractionExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new ImageFileMetadataExtractionExtractor(
                bmpExtractor, epsExtractor, gifExtractor, heifExtractor, icoExtractor,
                jpegExtractor, pcxExtractor, pngExtractor, psdExtractor, tiffExtractor,
                webPExtractor, avifExtractor, arwExtractor, cr2Extractor, cr3Extractor,
                nefExtractor, orfExtractor, rafExtractor, rw2Extractor, dngExtractor);
        when(mockFile.getName()).thenReturn("test.img");
        when(bmpExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(epsExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(gifExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(heifExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(icoExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(jpegExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(pcxExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(pngExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(psdExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(tiffExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(webPExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(avifExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(arwExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(cr2Extractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(cr3Extractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(nefExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(orfExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(rafExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(rw2Extractor.extract(any(), any())).thenReturn(FileMeta.empty());
        when(dngExtractor.extract(any(), any())).thenReturn(FileMeta.empty());
    }

    @Test
    void testExtract_Bmp_DelegatesToBmp() {
        extractor.extract(mockFile, "image/bmp");
        verify(bmpExtractor).extract(mockFile, "image/bmp");
    }

    @Test
    void testExtract_Eps_DelegatesToEps() {
        extractor.extract(mockFile, "application/postscript");
        verify(epsExtractor).extract(mockFile, "application/postscript");
    }

    @Test
    void testExtract_Gif_DelegatesToGif() {
        extractor.extract(mockFile, "image/gif");
        verify(gifExtractor).extract(mockFile, "image/gif");
    }

    @Test
    void testExtract_Heic_DelegatesToHeif() {
        extractor.extract(mockFile, "image/heic");
        verify(heifExtractor).extract(mockFile, "image/heic");
    }

    @Test
    void testExtract_Heif_DelegatesToHeif() {
        extractor.extract(mockFile, "image/heif");
        verify(heifExtractor).extract(mockFile, "image/heif");
    }

    @Test
    void testExtract_Ico_DelegatesToIco() {
        extractor.extract(mockFile, "image/x-icon");
        verify(icoExtractor).extract(mockFile, "image/x-icon");
    }

    @Test
    void testExtract_Jpeg_DelegatesToJpeg() {
        extractor.extract(mockFile, "image/jpeg");
        verify(jpegExtractor).extract(mockFile, "image/jpeg");
    }

    @Test
    void testExtract_Pcx_DelegatesToPcx() {
        extractor.extract(mockFile, "image/x-pcx");
        verify(pcxExtractor).extract(mockFile, "image/x-pcx");
    }

    @Test
    void testExtract_Png_DelegatesToPng() {
        extractor.extract(mockFile, "image/png");
        verify(pngExtractor).extract(mockFile, "image/png");
    }

    @Test
    void testExtract_Psd_DelegatesToPsd() {
        extractor.extract(mockFile, "image/vnd.adobe.photoshop");
        verify(psdExtractor).extract(mockFile, "image/vnd.adobe.photoshop");
    }

    @Test
    void testExtract_Tiff_DelegatesToTiff() {
        extractor.extract(mockFile, "image/tiff");
        verify(tiffExtractor).extract(mockFile, "image/tiff");
    }

    @Test
    void testExtract_WebP_DelegatesToWebP() {
        extractor.extract(mockFile, "image/webp");
        verify(webPExtractor).extract(mockFile, "image/webp");
    }

    @Test
    void testExtract_Avif_DelegatesToAvif() {
        extractor.extract(mockFile, "image/avif");
        verify(avifExtractor).extract(mockFile, "image/avif");
    }

    @Test
    void testExtract_Arw_DelegatesToArw() {
        extractor.extract(mockFile, "image/x-sony-arw");
        verify(arwExtractor).extract(mockFile, "image/x-sony-arw");
    }

    @Test
    void testExtract_Cr2_DelegatesToCr2() {
        extractor.extract(mockFile, "image/x-canon-cr2");
        verify(cr2Extractor).extract(mockFile, "image/x-canon-cr2");
    }

    @Test
    void testExtract_Cr3_DelegatesToCr3() {
        extractor.extract(mockFile, "image/x-canon-cr3");
        verify(cr3Extractor).extract(mockFile, "image/x-canon-cr3");
    }

    @Test
    void testExtract_Nef_DelegatesToNef() {
        extractor.extract(mockFile, "image/x-nikon-nef");
        verify(nefExtractor).extract(mockFile, "image/x-nikon-nef");
    }

    @Test
    void testExtract_Orf_DelegatesToOrf() {
        extractor.extract(mockFile, "image/x-olympus-orf");
        verify(orfExtractor).extract(mockFile, "image/x-olympus-orf");
    }

    @Test
    void testExtract_Raf_DelegatesToRaf() {
        extractor.extract(mockFile, "image/x-fujifilm-raf");
        verify(rafExtractor).extract(mockFile, "image/x-fujifilm-raf");
    }

    @Test
    void testExtract_Rw2_DelegatesToRw2() {
        extractor.extract(mockFile, "image/x-panasonic-rw2");
        verify(rw2Extractor).extract(mockFile, "image/x-panasonic-rw2");
    }

    @Test
    void testExtract_Dng_DelegatesToDng() {
        extractor.extract(mockFile, "image/x-adobe-dng");
        verify(dngExtractor).extract(mockFile, "image/x-adobe-dng");
    }

    @Test
    void testExtract_UnsupportedMimeType_ReturnsError() {
        FileMeta result = extractor.extract(mockFile, "image/unknown-format");

        assertNotNull(result);
        assertFalse(result.getErrors().isEmpty(), "Should return error for unsupported MIME type");
    }
}
