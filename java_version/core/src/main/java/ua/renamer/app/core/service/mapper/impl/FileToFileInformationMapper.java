package ua.renamer.app.core.service.mapper.impl;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.core.service.mapper.DataMapper;
import ua.renamer.app.core.service.mapper.FileToMetadataMapper;

import java.io.File;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class FileToFileInformationMapper implements DataMapper<File, FileInformation> {

    private final FileToMetadataMapper fileToMetadataMapper;
    private final FilesOperations filesOperations;

//    private static final AviMapper AVI_MAPPER = new AviMapper();
//    private static final BmpMapper BMP_MAPPER = new BmpMapper();
//    private static final EpsMapper EPS_MAPPER = new EpsMapper();
//    private static final GifMapper GIF_MAPPER = new GifMapper();
//    private static final HeifMapper HEIF_MAPPER = new HeifMapper();
//    private static final IcoMapper ICO_MAPPER = new IcoMapper();
//    private static final JpegMapper JPEG_MAPPER = new JpegMapper();
//    private static final Mp3Mapper MP_3_MAPPER = new Mp3Mapper();
//    private static final Mp4Mapper MP_4_MAPPER = new Mp4Mapper();
//    private static final PcxMapper PCX_MAPPER = new PcxMapper();
//    private static final PngMapper PNG_MAPPER = new PngMapper();
//    private static final PsdMapper PSD_MAPPER = new PsdMapper();
//    private static final QuickTimeMapper QUICK_TIME_MAPPER = new QuickTimeMapper();
//    private static final TiffMapper TIFF_MAPPER = new TiffMapper();
//    private static final WavMapper WAV_MAPPER = new WavMapper();
//    private static final WebPMapper WEB_PMAPPER = new WebPMapper();
//    private static final FileToMetadataMapper FILE_TO_METADATA_MAPPER = new NullMapper();

//    static {
//        FILE_TO_METADATA_MAPPER.setNext(AVI_MAPPER);
//        AVI_MAPPER.setNext(BMP_MAPPER);
//        BMP_MAPPER.setNext(EPS_MAPPER);
//        EPS_MAPPER.setNext(GIF_MAPPER);
//        GIF_MAPPER.setNext(HEIF_MAPPER);
//        HEIF_MAPPER.setNext(ICO_MAPPER);
//        ICO_MAPPER.setNext(JPEG_MAPPER);
//        JPEG_MAPPER.setNext(MP_3_MAPPER);
//        MP_3_MAPPER.setNext(MP_4_MAPPER);
//        MP_4_MAPPER.setNext(PCX_MAPPER);
//        PCX_MAPPER.setNext(PNG_MAPPER);
//        PNG_MAPPER.setNext(PSD_MAPPER);
//        PSD_MAPPER.setNext(QUICK_TIME_MAPPER);
//        QUICK_TIME_MAPPER.setNext(TIFF_MAPPER);
//        TIFF_MAPPER.setNext(WAV_MAPPER);
//        WAV_MAPPER.setNext(WEB_PMAPPER);
//    }

    @Override
    public FileInformation map(File file) {
        filesOperations.validateFileInstance(file);

        final var fileNameWithoutExtension = filesOperations.getFileNameWithoutExtension(file);
        final var fileAbsolutePath = filesOperations.getFileAbsolutePath(file);
        final var isFile = filesOperations.isFile(file);
        final var fileExtension = filesOperations.getFileExtension(file);
        final var fsCreationDate = filesOperations.getFileCreationTime(file);
        final var fsModificationDate = filesOperations.getFileModificationTime(file);
        final var fileSize = filesOperations.getFileSize(file);
        final var metadata = fileToMetadataMapper.map(file);

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
