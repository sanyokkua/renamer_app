package ua.renamer.app.core.v2.exception;

import java.io.IOException;

public class FileAttributesReadException extends RuntimeException {
    public FileAttributesReadException(String format, IOException message) {
        super(format, message);
    }
}
