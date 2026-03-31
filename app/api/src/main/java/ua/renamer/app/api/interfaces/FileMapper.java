package ua.renamer.app.api.interfaces;

import ua.renamer.app.api.model.FileModel;

import java.io.File;

/**
 * Port interface for mapping a raw {@link File} to an immutable {@link FileModel}.
 * Implementations perform metadata extraction and file attribute reading.
 */
public interface FileMapper {

    /**
     * Maps a raw file to a fully-populated immutable model.
     *
     * @param file the file to map; must not be null and must exist
     * @return the populated file model; never null
     */
    FileModel mapFrom(File file);
}
