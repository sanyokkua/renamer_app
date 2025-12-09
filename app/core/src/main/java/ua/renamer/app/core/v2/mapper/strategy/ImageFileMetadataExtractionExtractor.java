package ua.renamer.app.core.v2.mapper.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.v2.enums.AppMimeTypes;
import ua.renamer.app.core.v2.interfaces.FileMetadataExtractor;
import ua.renamer.app.core.v2.mapper.strategy.format.image.*;
import ua.renamer.app.core.v2.model.meta.FileMeta;

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

        log.warn("Unsupported image MIME type: {} for file: {}", mimeType, file.getName());
        return FileMeta.withError("Not Supported File MimeType: " + mimeType);
    }
}
