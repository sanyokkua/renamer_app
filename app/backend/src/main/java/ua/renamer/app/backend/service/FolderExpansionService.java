package ua.renamer.app.backend.service;

import ua.renamer.app.api.model.FolderDropOptions;

import java.nio.file.Path;
import java.util.List;

/**
 * Expands a directory into a flat list of {@link Path} entries for session ingestion.
 *
 * <p>Behaviour is controlled by {@link FolderDropOptions}: depth (flat vs. recursive)
 * and whether sub-directories are themselves added as renamable items.
 *
 * <p>Implementations must never throw — inaccessible entries are logged and skipped,
 * and a partial result is returned.
 */
@FunctionalInterface
public interface FolderExpansionService {

    /**
     * Expands {@code folder} into an ordered list of paths according to {@code options}.
     *
     * @param folder  the root directory to expand; must be an existing directory
     * @param options controls traversal depth and sub-directory inclusion; must not be null
     * @return ordered list of paths; never null, may be empty
     */
    List<Path> expand(Path folder, FolderDropOptions options);
}
