package ua.renamer.app.core.service.file;

import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Functional interface for extracting basic file attributes.
 */
@FunctionalInterface
public interface BasicFileAttributesExtractor {

    /**
     * Retrieves the basic file attributes of a file or directory.
     *
     * @param path    The path to the file or directory.
     * @param type    The type of the attributes to retrieve.
     * @param options Optional link options to use when accessing the file.
     * @param <A>     The type of the file attributes.
     *
     * @return The basic file attributes of the specified file or directory.
     *
     * @throws IOException If an I/O error occurs.
     */
    <A extends BasicFileAttributes> A getAttributes(Path path, Class<A> type, LinkOption... options) throws IOException;

}
