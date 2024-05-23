package ua.renamer.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.FileToMetadataMapper;
import ua.renamer.app.core.mappers.audio.Mp3Mapper;
import ua.renamer.app.core.mappers.audio.WavMapper;
import ua.renamer.app.core.mappers.images.*;
import ua.renamer.app.core.mappers.video.AviMapper;
import ua.renamer.app.core.mappers.video.Mp4Mapper;
import ua.renamer.app.core.mappers.video.QuickTimeMapper;
import ua.renamer.app.core.model.FileInformation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import static ua.renamer.app.core.utils.FileUtils.getFileExtension;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileInformationUtils {

    public static final AviMapper AVI_MAPPER = new AviMapper();
    public static final BmpMapper BMP_MAPPER = new BmpMapper();
    public static final EpsMapper EPS_MAPPER = new EpsMapper();
    public static final GifMapper GIF_MAPPER = new GifMapper();
    public static final HeifMapper HEIF_MAPPER = new HeifMapper();
    public static final IcoMapper ICO_MAPPER = new IcoMapper();
    public static final JpegMapper JPEG_MAPPER = new JpegMapper();
    public static final Mp3Mapper MP_3_MAPPER = new Mp3Mapper();
    public static final Mp4Mapper MP_4_MAPPER = new Mp4Mapper();
    public static final PcxMapper PCX_MAPPER = new PcxMapper();
    public static final PngMapper PNG_MAPPER = new PngMapper();
    public static final PsdMapper PSD_MAPPER = new PsdMapper();
    public static final QuickTimeMapper QUICK_TIME_MAPPER = new QuickTimeMapper();
    public static final TiffMapper TIFF_MAPPER = new TiffMapper();
    public static final WavMapper WAV_MAPPER = new WavMapper();
    public static final WebPMapper WEB_PMAPPER = new WebPMapper();

    public static final FileToMetadataMapper MAIN_FILE_TO_METADATA_MAPPER = new NullMapper();

    static {
        MAIN_FILE_TO_METADATA_MAPPER.setNext(AVI_MAPPER);
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
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }

        try {
            final var filePath = file.toPath();
            final var fileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
            final var fileNameAndExtension = file.getName();
            final var fileAbsolutePath = file.getAbsolutePath();
            final var isFile = file.isFile();
            final var fileNameWithoutExtension = removeFileExtension(fileNameAndExtension);
            final var fileExtension = getFileExtension(file);
            final var fsCreationDate = getTimeValue(fileAttributes.creationTime());
            final var fsModificationDate = getTimeValue(fileAttributes.lastModifiedTime());
            final var fileSize = file.length();
            final var metadata = MAIN_FILE_TO_METADATA_MAPPER.map(file);

            return FileInformation.builder()
                                  .originalFile(file)
                                  .fileAbsolutePath(fileAbsolutePath)
                                  .isFile(isFile)
                                  .fileName(fileNameWithoutExtension)
                                  .newName(fileNameWithoutExtension) // Initial value is current file name
                                  .fileExtension(fileExtension)
                                  .newExtension(fileExtension) // Initial value is current file extension
                                  .fileSize(fileSize)
                                  .fsCreationDate(fsCreationDate)
                                  .fsModificationDate(fsModificationDate)
                                  .metadata(metadata)
                                  .build();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to read file information", ex);
        }
    }

    private static Long getTimeValue(FileTime fileTime) {
        var result = fileTime.toMillis();
        if (result < 0 || result == 0) {
            return null;
        }
        return result;
    }

    private static String removeFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

}
