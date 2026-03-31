package ua.renamer.app.api.exception;

import java.io.IOException;

/**
 * Thrown when MIME type detection fails for a file.
 */
public class MimeTypeNotFoundException extends RuntimeException {
    public MimeTypeNotFoundException(String message, IOException cause) {
        super(message, cause);
    }
}
