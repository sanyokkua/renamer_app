package ua.renamer.app.api.exception;

import java.io.IOException;

/**
 * Thrown when file attributes cannot be read from the filesystem.
 */
public class FileAttributesReadException extends RuntimeException {
    public FileAttributesReadException(String message, IOException cause) {
        super(message, cause);
    }
}
