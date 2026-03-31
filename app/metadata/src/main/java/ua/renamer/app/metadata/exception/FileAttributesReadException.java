package ua.renamer.app.metadata.exception;

import java.io.IOException;

public class FileAttributesReadException extends RuntimeException {
    public FileAttributesReadException(String format, IOException message) {
        super(format, message);
    }
}
