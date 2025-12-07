package ua.renamer.app.core.v2.exception;

import java.io.IOException;

public class MimeTypeNotFoundException extends RuntimeException {
    public MimeTypeNotFoundException(String message, IOException e) {
        super(message, e);
    }
}
