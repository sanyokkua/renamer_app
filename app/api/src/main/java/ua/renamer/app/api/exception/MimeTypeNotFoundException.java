package ua.renamer.app.api.exception;

import java.io.IOException;

/**
 * Thrown when MIME type detection fails for a file.
 */
public class MimeTypeNotFoundException extends RuntimeException {
    /**
     * @param message description of the failure
     * @param cause   the underlying IOException
     */
    public MimeTypeNotFoundException(String message, IOException cause) {
        super(message, cause);
    }
}
