package ua.renamer.app.backend.service.impl;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.FolderDropOptions;
import ua.renamer.app.backend.service.FolderExpansionService;

import java.io.IOException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Default implementation of {@link FolderExpansionService}.
 *
 * <p>Uses {@link Files#list} for flat traversal and {@link Files#walk} (with
 * {@link FileVisitOption#FOLLOW_LINKS}) for recursive traversal. Hidden entries
 * (as reported by {@link Files#isHidden}) are always excluded. Symlink cycles
 * are caught and logged; all other per-entry {@link IOException}s are likewise
 * caught and logged so that the service never propagates an exception to callers.
 */
@Slf4j
@Singleton
public class FolderExpansionServiceImpl implements FolderExpansionService {

    @Override
    public List<Path> expand(Path folder, FolderDropOptions options) {
        if (options.recursive()) {
            return collectRecursive(folder, options.includeFoldersAsItems());
        }
        return collectFlat(folder, options.includeFoldersAsItems());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private List<Path> collectFlat(Path folder, boolean includeFolders) {
        List<Path> result = new ArrayList<>();
        try (Stream<Path> entries = Files.list(folder)) {
            entries.forEach(entry -> addIfEligible(entry, includeFolders, result));
        } catch (IOException e) {
            log.warn("Cannot list directory '{}': {}", folder, e.getMessage());
        }
        return result;
    }

    private List<Path> collectRecursive(Path folder, boolean includeFolders) {
        List<Path> result = new ArrayList<>();
        try (Stream<Path> entries = Files.walk(folder, FileVisitOption.FOLLOW_LINKS)) {
            entries.filter(entry -> !entry.equals(folder))
                    .forEach(entry -> addIfEligible(entry, includeFolders, result));
        } catch (FileSystemLoopException e) {
            log.warn("Symlink cycle detected under '{}': {}", folder, e.getMessage());
        } catch (IOException e) {
            log.warn("Cannot walk directory '{}': {}", folder, e.getMessage());
        }
        return result;
    }

    private void addIfEligible(Path entry, boolean includeFolders, List<Path> result) {
        try {
            if (Files.isHidden(entry)) {
                return;
            }
            boolean isDir = Files.isDirectory(entry);
            if (isDir && !includeFolders) {
                return;
            }
            result.add(entry);
        } catch (IOException e) {
            log.warn("Skipping inaccessible entry '{}': {}", entry, e.getMessage());
        }
    }
}
