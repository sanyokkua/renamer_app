package ua.renamer.app.metadata.extractor.strategy;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.enums.AppMimeTypes;
import ua.renamer.app.api.interfaces.FileMetadataExtractor;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.metadata.extractor.strategy.format.image.ArwFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.AvifFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.BmpFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.Cr2FileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.Cr3FileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.DngFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.EpsFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.GifFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.HeifFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.IcoFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.JpegFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.NefFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.OrfFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.PcxFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.PngFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.PsdFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.RafFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.Rw2FileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.TiffFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.image.WebPFileMetadataExtractor;

import java.io.File;

/**
 * Dispatches image metadata extraction to the appropriate format-level extractor based on MIME type.
 */
@Slf4j
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

    @Inject
    public ImageFileMetadataExtractionExtractor(BmpFileMetadataExtractor bmpFileMetadataExtractor,
                                                EpsFileMetadataExtractor epsFileMetadataExtractor,
                                                GifFileMetadataExtractor gifFileMetadataExtractor,
                                                HeifFileMetadataExtractor heifFileMetadataExtractor,
                                                IcoFileMetadataExtractor icoFileMetadataExtractor,
                                                JpegFileMetadataExtractor jpegFileMetadataExtractor,
                                                PcxFileMetadataExtractor pcxFileMetadataExtractor,
                                                PngFileMetadataExtractor pngFileMetadataExtractor,
                                                PsdFileMetadataExtractor psdFileMetadataExtractor,
                                                TiffFileMetadataExtractor tiffFileMetadataExtractor,
                                                WebPFileMetadataExtractor webPFileMetadataExtractor,
                                                AvifFileMetadataExtractor avifFileMetadataExtractor,
                                                ArwFileMetadataExtractor arwFileMetadataExtractor,
                                                Cr2FileMetadataExtractor cr2FileMetadataExtractor,
                                                Cr3FileMetadataExtractor cr3FileMetadataExtractor,
                                                NefFileMetadataExtractor nefFileMetadataExtractor,
                                                OrfFileMetadataExtractor orfFileMetadataExtractor,
                                                RafFileMetadataExtractor rafFileMetadataExtractor,
                                                Rw2FileMetadataExtractor rw2FileMetadataExtractor,
                                                DngFileMetadataExtractor dngFileMetadataExtractor) {
        this.bmpFileMetadataExtractor = bmpFileMetadataExtractor;
        this.epsFileMetadataExtractor = epsFileMetadataExtractor;
        this.gifFileMetadataExtractor = gifFileMetadataExtractor;
        this.heifFileMetadataExtractor = heifFileMetadataExtractor;
        this.icoFileMetadataExtractor = icoFileMetadataExtractor;
        this.jpegFileMetadataExtractor = jpegFileMetadataExtractor;
        this.pcxFileMetadataExtractor = pcxFileMetadataExtractor;
        this.pngFileMetadataExtractor = pngFileMetadataExtractor;
        this.psdFileMetadataExtractor = psdFileMetadataExtractor;
        this.tiffFileMetadataExtractor = tiffFileMetadataExtractor;
        this.webPFileMetadataExtractor = webPFileMetadataExtractor;
        this.avifFileMetadataExtractor = avifFileMetadataExtractor;
        this.arwFileMetadataExtractor = arwFileMetadataExtractor;
        this.cr2FileMetadataExtractor = cr2FileMetadataExtractor;
        this.cr3FileMetadataExtractor = cr3FileMetadataExtractor;
        this.nefFileMetadataExtractor = nefFileMetadataExtractor;
        this.orfFileMetadataExtractor = orfFileMetadataExtractor;
        this.rafFileMetadataExtractor = rafFileMetadataExtractor;
        this.rw2FileMetadataExtractor = rw2FileMetadataExtractor;
        this.dngFileMetadataExtractor = dngFileMetadataExtractor;
    }

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
