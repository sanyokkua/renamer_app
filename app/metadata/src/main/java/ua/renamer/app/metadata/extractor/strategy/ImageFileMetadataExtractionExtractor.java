package ua.renamer.app.metadata.extractor.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.enums.AppMimeTypes;
import ua.renamer.app.api.interfaces.FileMetadataExtractor;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.metadata.extractor.strategy.format.image.*;

import java.io.File;

@Slf4j
@RequiredArgsConstructor
public class ImageFileMetadataExtractionExtractor implements FileMetadataExtractor {
    private final BmpFileMetadataExtractor bmpFileMetadataExtractor;
    private final EpsFileMetadataExtractor epsFileMetadataExtractor;
    private final GifFileMetadataExtractor gifFileMetadataExtractor;
    private final HeifFileMetadataExtractor heifFileMetadataExtractor;
    private final IcoFileMetadataExtractor icoFileMetadataExtractor;
    private final JpegFileMetadataExtractor jpegFileMetadataExtractor;
    private final PcxFileMetadataExtractor pcxFileMetadataExtractor;
    private final PngFileMetadataExtractor pngFileMetadataExtractor;
    private final PsdFileMetadataExtractor psdFileMetadataExtractor;
    private final TiffFileMetadataExtractor tiffFileMetadataExtractor;
    private final WebPFileMetadataExtractor webPFileMetadataExtractor;
    private final AvifFileMetadataExtractor avifFileMetadataExtractor;
    private final ArwFileMetadataExtractor arwFileMetadataExtractor;
    private final Cr2FileMetadataExtractor cr2FileMetadataExtractor;
    private final Cr3FileMetadataExtractor cr3FileMetadataExtractor;
    private final NefFileMetadataExtractor nefFileMetadataExtractor;
    private final OrfFileMetadataExtractor orfFileMetadataExtractor;
    private final RafFileMetadataExtractor rafFileMetadataExtractor;
    private final Rw2FileMetadataExtractor rw2FileMetadataExtractor;
    private final DngFileMetadataExtractor dngFileMetadataExtractor;

    @Override
    public FileMeta extract(File file, String mimeType) {
        log.debug("Extracting image metadata for file: {}, mimeType: {}", file.getName(), mimeType);

        if (AppMimeTypes.IMAGE_BMP.getMime().equals(mimeType)) {
            return bmpFileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.APPLICATION_POSTSCRIPT.getMime().equals(mimeType)) {
            return epsFileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_GIF.getMime().equals(mimeType)) {
            return gifFileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_HEIC.getMime().equals(mimeType) ||
                AppMimeTypes.IMAGE_HEIF.getMime().equals(mimeType)) {
            return heifFileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_X_ICON.getMime().equals(mimeType)) {
            return icoFileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_JPEG.getMime().equals(mimeType)) {
            return jpegFileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_X_PCX.getMime().equals(mimeType)) {
            return pcxFileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_PNG.getMime().equals(mimeType)) {
            return pngFileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_VND_ADOBE_PHOTOSHOP.getMime().equals(mimeType)) {
            return psdFileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_TIFF.getMime().equals(mimeType)) {
            return tiffFileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_WEBP.getMime().equals(mimeType)) {
            return webPFileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_AVIF.getMime().equals(mimeType)) {
            return avifFileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_X_SONY_ARW.getMime().equals(mimeType)) {
            return arwFileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_X_CANON_CR2.getMime().equals(mimeType)) {
            return cr2FileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_X_CANON_CR3.getMime().equals(mimeType)) {
            return cr3FileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_X_NIKON_NEF.getMime().equals(mimeType)) {
            return nefFileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_X_OLYMPUS_ORF.getMime().equals(mimeType)) {
            return orfFileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_X_FUJIFILM_RAF.getMime().equals(mimeType)) {
            return rafFileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_X_PANASONIC_RW2.getMime().equals(mimeType)) {
            return rw2FileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.IMAGE_X_ADOBE_DNG.getMime().equals(mimeType)) {
            return dngFileMetadataExtractor.extract(file, mimeType);
        }

        log.warn("Unsupported image MIME type: {} for file: {}", mimeType, file.getName());
        return FileMeta.withError("Not Supported File MimeType: " + mimeType);
    }
}
