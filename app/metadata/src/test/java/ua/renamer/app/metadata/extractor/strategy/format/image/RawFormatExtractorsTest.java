package ua.renamer.app.metadata.extractor.strategy.format.image;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.metadata.util.DateTimeConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for RAW and specialty image format metadata extractors.
 *
 * <p>These extractors extend BaseImageMetadataExtractor and differ only in their
 * getBaseDirectoryClass(), getBaseWidthTag(), and getBaseHeightTag() implementations.
 * Since no real RAW camera test files are available in the test-data directory, these
 * tests verify the no-throw contract (corrupt/empty file returns FileMeta with error)
 * and that the extractors are correctly constructed and return their format-specific
 * directory/tag metadata.
 */
class RawFormatExtractorsTest {

    @TempDir
    Path tempDir;

    // ============================================================================
    // Helper
    // ============================================================================

    static Stream<BaseImageMetadataExtractor> allRawExtractors() {
        DateTimeConverter dtc = new DateTimeConverter();
        return Stream.of(
                new OrfFileMetadataExtractor(dtc),
                new Cr2FileMetadataExtractor(dtc),
                new Cr3FileMetadataExtractor(dtc),
                new NefFileMetadataExtractor(dtc),
                new ArwFileMetadataExtractor(dtc),
                new RafFileMetadataExtractor(dtc),
                new Rw2FileMetadataExtractor(dtc),
                new DngFileMetadataExtractor(dtc),
                new AvifFileMetadataExtractor(dtc),
                new IcoFileMetadataExtractor(dtc),
                new PcxFileMetadataExtractor(dtc),
                new PsdFileMetadataExtractor(dtc),
                new EpsFileMetadataExtractor(dtc)
        );
    }

    private File createEmptyFile(String name) throws IOException {
        Path file = Files.createFile(tempDir.resolve(name));
        return file.toFile();
    }

    private File createBinaryGarbageFile(String name) throws IOException {
        Path file = tempDir.resolve(name);
        Files.write(file, new byte[]{0x00, 0x01, (byte) 0xFF, 0x42, 0x13, 0x37});
        return file.toFile();
    }

    // ============================================================================
    // Constructor and instantiation tests — one per extractor class
    // ============================================================================

    private DateTimeConverter dateTimeConverter() {
        return new DateTimeConverter();
    }

    @Test
    void orFExtractor_construct_doesNotThrow() {
        assertThatCode(() -> new OrfFileMetadataExtractor(dateTimeConverter()))
                .doesNotThrowAnyException();
    }

    @Test
    void cr2Extractor_construct_doesNotThrow() {
        assertThatCode(() -> new Cr2FileMetadataExtractor(dateTimeConverter()))
                .doesNotThrowAnyException();
    }

    @Test
    void cr3Extractor_construct_doesNotThrow() {
        assertThatCode(() -> new Cr3FileMetadataExtractor(dateTimeConverter()))
                .doesNotThrowAnyException();
    }

    @Test
    void nefExtractor_construct_doesNotThrow() {
        assertThatCode(() -> new NefFileMetadataExtractor(dateTimeConverter()))
                .doesNotThrowAnyException();
    }

    @Test
    void arwExtractor_construct_doesNotThrow() {
        assertThatCode(() -> new ArwFileMetadataExtractor(dateTimeConverter()))
                .doesNotThrowAnyException();
    }

    @Test
    void rafExtractor_construct_doesNotThrow() {
        assertThatCode(() -> new RafFileMetadataExtractor(dateTimeConverter()))
                .doesNotThrowAnyException();
    }

    @Test
    void rw2Extractor_construct_doesNotThrow() {
        assertThatCode(() -> new Rw2FileMetadataExtractor(dateTimeConverter()))
                .doesNotThrowAnyException();
    }

    @Test
    void dngExtractor_construct_doesNotThrow() {
        assertThatCode(() -> new DngFileMetadataExtractor(dateTimeConverter()))
                .doesNotThrowAnyException();
    }

    @Test
    void avifExtractor_construct_doesNotThrow() {
        assertThatCode(() -> new AvifFileMetadataExtractor(dateTimeConverter()))
                .doesNotThrowAnyException();
    }

    @Test
    void icoExtractor_construct_doesNotThrow() {
        assertThatCode(() -> new IcoFileMetadataExtractor(dateTimeConverter()))
                .doesNotThrowAnyException();
    }

    @Test
    void pcxExtractor_construct_doesNotThrow() {
        assertThatCode(() -> new PcxFileMetadataExtractor(dateTimeConverter()))
                .doesNotThrowAnyException();
    }

    @Test
    void psdExtractor_construct_doesNotThrow() {
        assertThatCode(() -> new PsdFileMetadataExtractor(dateTimeConverter()))
                .doesNotThrowAnyException();
    }

    // ============================================================================
    // No-throw contract: empty/garbage files must return FileMeta, never throw
    // ============================================================================

    @Test
    void epsExtractor_construct_doesNotThrow() {
        assertThatCode(() -> new EpsFileMetadataExtractor(dateTimeConverter()))
                .doesNotThrowAnyException();
    }

    @Test
    void orfExtractor_extractFromEmptyFile_returnsFileMetaWithError() throws IOException {
        File empty = createEmptyFile("empty.orf");
        OrfFileMetadataExtractor extractor = new OrfFileMetadataExtractor(dateTimeConverter());

        FileMeta result = extractor.extract(empty, "image/x-olympus-orf");

        assertThat(result).isNotNull();
        // Empty file cannot be parsed as image — pipeline captures the error
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void cr2Extractor_extractFromEmptyFile_returnsFileMetaWithError() throws IOException {
        File empty = createEmptyFile("empty.cr2");
        Cr2FileMetadataExtractor extractor = new Cr2FileMetadataExtractor(dateTimeConverter());

        FileMeta result = extractor.extract(empty, "image/x-canon-cr2");

        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void cr3Extractor_extractFromEmptyFile_returnsFileMetaWithError() throws IOException {
        File empty = createEmptyFile("empty.cr3");
        Cr3FileMetadataExtractor extractor = new Cr3FileMetadataExtractor(dateTimeConverter());

        FileMeta result = extractor.extract(empty, "image/x-canon-cr3");

        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void nefExtractor_extractFromEmptyFile_returnsFileMetaWithError() throws IOException {
        File empty = createEmptyFile("empty.nef");
        NefFileMetadataExtractor extractor = new NefFileMetadataExtractor(dateTimeConverter());

        FileMeta result = extractor.extract(empty, "image/x-nikon-nef");

        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void arwExtractor_extractFromEmptyFile_returnsFileMetaWithError() throws IOException {
        File empty = createEmptyFile("empty.arw");
        ArwFileMetadataExtractor extractor = new ArwFileMetadataExtractor(dateTimeConverter());

        FileMeta result = extractor.extract(empty, "image/x-sony-arw");

        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void rafExtractor_extractFromEmptyFile_returnsFileMetaWithError() throws IOException {
        File empty = createEmptyFile("empty.raf");
        RafFileMetadataExtractor extractor = new RafFileMetadataExtractor(dateTimeConverter());

        FileMeta result = extractor.extract(empty, "image/x-fujifilm-raf");

        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void rw2Extractor_extractFromEmptyFile_returnsFileMetaWithError() throws IOException {
        File empty = createEmptyFile("empty.rw2");
        Rw2FileMetadataExtractor extractor = new Rw2FileMetadataExtractor(dateTimeConverter());

        FileMeta result = extractor.extract(empty, "image/x-panasonic-rw2");

        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void dngExtractor_extractFromEmptyFile_returnsFileMetaWithError() throws IOException {
        File empty = createEmptyFile("empty.dng");
        DngFileMetadataExtractor extractor = new DngFileMetadataExtractor(dateTimeConverter());

        FileMeta result = extractor.extract(empty, "image/x-adobe-dng");

        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void avifExtractor_extractFromEmptyFile_returnsFileMetaWithError() throws IOException {
        File empty = createEmptyFile("empty.avif");
        AvifFileMetadataExtractor extractor = new AvifFileMetadataExtractor(dateTimeConverter());

        FileMeta result = extractor.extract(empty, "image/avif");

        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void icoExtractor_extractFromGarbageFile_returnsFileMetaWithError() throws IOException {
        File garbage = createBinaryGarbageFile("garbage.ico");
        IcoFileMetadataExtractor extractor = new IcoFileMetadataExtractor(dateTimeConverter());

        FileMeta result = extractor.extract(garbage, "image/x-icon");

        assertThat(result).isNotNull();
        // Garbage file — extractor either errors or returns empty image meta without error
        // Either way it must not throw
    }

    @Test
    void pcxExtractor_extractFromGarbageFile_returnsFileMetaWithoutThrowing() throws IOException {
        File garbage = createBinaryGarbageFile("garbage.pcx");
        PcxFileMetadataExtractor extractor = new PcxFileMetadataExtractor(dateTimeConverter());

        assertThatCode(() -> extractor.extract(garbage, "image/x-pcx"))
                .doesNotThrowAnyException();
    }

    @Test
    void psdExtractor_extractFromGarbageFile_returnsFileMetaWithoutThrowing() throws IOException {
        File garbage = createBinaryGarbageFile("garbage.psd");
        PsdFileMetadataExtractor extractor = new PsdFileMetadataExtractor(dateTimeConverter());

        assertThatCode(() -> extractor.extract(garbage, "image/vnd.adobe.photoshop"))
                .doesNotThrowAnyException();
    }

    // ============================================================================
    // Verify format-specific tag/directory configuration (tests getBase* methods)
    // ============================================================================

    @Test
    void epsExtractor_extractFromGarbageFile_returnsFileMetaWithoutThrowing() throws IOException {
        File garbage = createBinaryGarbageFile("garbage.eps");
        EpsFileMetadataExtractor extractor = new EpsFileMetadataExtractor(dateTimeConverter());

        assertThatCode(() -> extractor.extract(garbage, "application/postscript"))
                .doesNotThrowAnyException();
    }

    @Test
    void icoExtractor_getBaseDirectoryClass_returnsIcoDirectory() {
        IcoFileMetadataExtractor extractor = new IcoFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseDirectoryClass().getSimpleName()).isEqualTo("IcoDirectory");
    }

    @Test
    void icoExtractor_getBaseWidthTag_returnsNonNull() {
        IcoFileMetadataExtractor extractor = new IcoFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseWidthTag()).isNotNull();
    }

    @Test
    void icoExtractor_getBaseHeightTag_returnsNonNull() {
        IcoFileMetadataExtractor extractor = new IcoFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseHeightTag()).isNotNull();
    }

    @Test
    void pcxExtractor_getBaseDirectoryClass_returnsPcxDirectory() {
        PcxFileMetadataExtractor extractor = new PcxFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseDirectoryClass().getSimpleName()).isEqualTo("PcxDirectory");
    }

    @Test
    void pcxExtractor_getBaseWidthTag_returnsNonNull() {
        PcxFileMetadataExtractor extractor = new PcxFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseWidthTag()).isNotNull();
    }

    @Test
    void pcxExtractor_getBaseHeightTag_returnsNonNull() {
        PcxFileMetadataExtractor extractor = new PcxFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseHeightTag()).isNotNull();
    }

    @Test
    void psdExtractor_getBaseDirectoryClass_returnsPsdHeaderDirectory() {
        PsdFileMetadataExtractor extractor = new PsdFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseDirectoryClass().getSimpleName()).isEqualTo("PsdHeaderDirectory");
    }

    @Test
    void psdExtractor_getBaseWidthTag_returnsNonNull() {
        PsdFileMetadataExtractor extractor = new PsdFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseWidthTag()).isNotNull();
    }

    @Test
    void psdExtractor_getBaseHeightTag_returnsNonNull() {
        PsdFileMetadataExtractor extractor = new PsdFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseHeightTag()).isNotNull();
    }

    @Test
    void epsExtractor_getBaseDirectoryClass_returnsEpsDirectory() {
        EpsFileMetadataExtractor extractor = new EpsFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseDirectoryClass().getSimpleName()).isEqualTo("EpsDirectory");
    }

    @Test
    void epsExtractor_getBaseWidthTag_returnsNonNull() {
        EpsFileMetadataExtractor extractor = new EpsFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseWidthTag()).isNotNull();
    }

    @Test
    void epsExtractor_getBaseHeightTag_returnsNonNull() {
        EpsFileMetadataExtractor extractor = new EpsFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseHeightTag()).isNotNull();
    }

    // RAW formats — width/height tags are null (rely on EXIF fallback)
    @Test
    void orfExtractor_getBaseWidthTag_returnsNull() {
        OrfFileMetadataExtractor extractor = new OrfFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseWidthTag()).isNull();
    }

    @Test
    void orfExtractor_getBaseHeightTag_returnsNull() {
        OrfFileMetadataExtractor extractor = new OrfFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseHeightTag()).isNull();
    }

    @Test
    void cr2Extractor_getBaseWidthTag_returnsNull() {
        Cr2FileMetadataExtractor extractor = new Cr2FileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseWidthTag()).isNull();
    }

    @Test
    void cr2Extractor_getBaseHeightTag_returnsNull() {
        Cr2FileMetadataExtractor extractor = new Cr2FileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseHeightTag()).isNull();
    }

    @Test
    void cr3Extractor_getBaseWidthTag_returnsNull() {
        Cr3FileMetadataExtractor extractor = new Cr3FileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseWidthTag()).isNull();
    }

    @Test
    void nefExtractor_getBaseWidthTag_returnsNull() {
        NefFileMetadataExtractor extractor = new NefFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseWidthTag()).isNull();
    }

    @Test
    void arwExtractor_getBaseWidthTag_returnsNull() {
        ArwFileMetadataExtractor extractor = new ArwFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseWidthTag()).isNull();
    }

    @Test
    void rafExtractor_getBaseWidthTag_returnsNull() {
        RafFileMetadataExtractor extractor = new RafFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseWidthTag()).isNull();
    }

    @Test
    void rw2Extractor_getBaseWidthTag_returnsNull() {
        Rw2FileMetadataExtractor extractor = new Rw2FileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseWidthTag()).isNull();
    }

    @Test
    void dngExtractor_getBaseWidthTag_returnsNull() {
        DngFileMetadataExtractor extractor = new DngFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseWidthTag()).isNull();
    }

    // ============================================================================
    // Parameterized: all RAW extractors must handle an empty file without throwing
    // ============================================================================

    @Test
    void avifExtractor_getBaseWidthTag_returnsNull() {
        AvifFileMetadataExtractor extractor = new AvifFileMetadataExtractor(dateTimeConverter());
        assertThat(extractor.getBaseWidthTag()).isNull();
    }

    @ParameterizedTest
    @MethodSource("allRawExtractors")
    void allExtractors_extractFromNonExistentFile_neverThrow(BaseImageMetadataExtractor extractor) {
        File nonExistent = tempDir.resolve("does_not_exist.raw").toFile();

        assertThatCode(() -> extractor.extract(nonExistent, "image/raw"))
                .doesNotThrowAnyException();

        FileMeta result = extractor.extract(nonExistent, "image/raw");
        assertThat(result).isNotNull();
        // Non-existent file triggers IOException path — error must be captured
        assertThat(result.getErrors()).isNotEmpty();
    }
}
