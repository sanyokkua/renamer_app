package ua.renamer.app.api.session;

import java.nio.file.Path;

/**
 * FX-safe snapshot of a file eligible for renaming.
 * Contains only {@code java.base} types — no V2 internal model references.
 *
 * @param fileId    stable identifier for this file within the session; never null
 * @param name      the base filename without extension; never null
 * @param extension the file extension without leading dot; may be empty, never null
 * @param path      the absolute path to the file on disk; never null
 */
public record RenameCandidate(String fileId, String name, String extension, Path path) {
}
