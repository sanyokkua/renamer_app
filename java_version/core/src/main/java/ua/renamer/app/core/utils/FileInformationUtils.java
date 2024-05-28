package ua.renamer.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.abstracts.FileToMetadataMapper;
import ua.renamer.app.core.mappers.NullMapper;
import ua.renamer.app.core.mappers.audio.Mp3Mapper;
import ua.renamer.app.core.mappers.audio.WavMapper;
import ua.renamer.app.core.mappers.images.*;
import ua.renamer.app.core.mappers.video.AviMapper;
import ua.renamer.app.core.mappers.video.Mp4Mapper;
import ua.renamer.app.core.mappers.video.QuickTimeMapper;
import ua.renamer.app.core.model.FileInformation;

import java.io.File;

import static ua.renamer.app.core.utils.FileUtils.validateFileInstance;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileInformationUtils {

    private static final AviMapper AVI_MAPPER = new AviMapper();
    private static final BmpMapper BMP_MAPPER = new BmpMapper();
    private static final EpsMapper EPS_MAPPER = new EpsMapper();
    private static final GifMapper GIF_MAPPER = new GifMapper();
    private static final HeifMapper HEIF_MAPPER = new HeifMapper();
    private static final IcoMapper ICO_MAPPER = new IcoMapper();
    private static final JpegMapper JPEG_MAPPER = new JpegMapper();
    private static final Mp3Mapper MP_3_MAPPER = new Mp3Mapper();
    private static final Mp4Mapper MP_4_MAPPER = new Mp4Mapper();
    private static final PcxMapper PCX_MAPPER = new PcxMapper();
    private static final PngMapper PNG_MAPPER = new PngMapper();
    private static final PsdMapper PSD_MAPPER = new PsdMapper();
    private static final QuickTimeMapper QUICK_TIME_MAPPER = new QuickTimeMapper();
    private static final TiffMapper TIFF_MAPPER = new TiffMapper();
    private static final WavMapper WAV_MAPPER = new WavMapper();
    private static final WebPMapper WEB_PMAPPER = new WebPMapper();
    private static final FileToMetadataMapper FILE_TO_METADATA_MAPPER = new NullMapper();

    static {
        FILE_TO_METADATA_MAPPER.setNext(AVI_MAPPER);
        AVI_MAPPER.setNext(BMP_MAPPER);
        BMP_MAPPER.setNext(EPS_MAPPER);
        EPS_MAPPER.setNext(GIF_MAPPER);
        GIF_MAPPER.setNext(HEIF_MAPPER);
        HEIF_MAPPER.setNext(ICO_MAPPER);
        ICO_MAPPER.setNext(JPEG_MAPPER);
        JPEG_MAPPER.setNext(MP_3_MAPPER);
        MP_3_MAPPER.setNext(MP_4_MAPPER);
        MP_4_MAPPER.setNext(PCX_MAPPER);
        PCX_MAPPER.setNext(PNG_MAPPER);
        PNG_MAPPER.setNext(PSD_MAPPER);
        PSD_MAPPER.setNext(QUICK_TIME_MAPPER);
        QUICK_TIME_MAPPER.setNext(TIFF_MAPPER);
        TIFF_MAPPER.setNext(WAV_MAPPER);
        WAV_MAPPER.setNext(WEB_PMAPPER);
    }

    public static FileInformation createFileInformationFromFile(File file) {
        validateFileInstance(file);

        final var fileNameWithoutExtension = FileUtils.getFileNameWithoutExtension(file);
        final var fileAbsolutePath = FileUtils.getFileAbsolutePath(file);
        final var isFile = FileUtils.isFile(file);
        final var fileExtension = FileUtils.getFileExtension(file);
        final var fsCreationDate = FileUtils.getFileCreationTime(file);
        final var fsModificationDate = FileUtils.getFileModificationTime(file);
        final var fileSize = FileUtils.getFileSize(file);
        final var metadata = FILE_TO_METADATA_MAPPER.map(file);

        final var creationDateTime = fsCreationDate.orElse(null);
        final var modificationDateTime = fsModificationDate.orElse(null);

        return FileInformation.builder()
                              .originalFile(file)
                              .fileAbsolutePath(fileAbsolutePath)
                              .isFile(isFile)
                              .fileName(fileNameWithoutExtension)
                              .newName(fileNameWithoutExtension) // Initial value is current file name
                              .fileExtension(fileExtension)
                              .newExtension(fileExtension) // Initial value is current file extension
                              .fileSize(fileSize)
                              .fsCreationDate(creationDateTime)
                              .fsModificationDate(modificationDateTime)
                              .metadata(metadata)
                              .build();
    }

}
