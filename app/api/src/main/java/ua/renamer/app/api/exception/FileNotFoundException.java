package ua.renamer.app.api.exception;

/**
 * Thrown when a required file does not exist on the filesystem.
 */
public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String message) {
        super(message);
    }
}
