package ua.renamer.app.core.v2.service.impl;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.core.service.validator.impl.NameValidator;
import ua.renamer.app.core.v2.service.RenameExecutionService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Implementation of RenameExecutionService that performs physical file renames.
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class RenameExecutionServiceImpl implements RenameExecutionService {

    private static final int MAX_SUFFIX_ATTEMPTS = 999;

    private final NameValidator nameValidator;

    @Override
    public RenameResult execute(PreparedFileModel preparedFile) {
        // Skip if already has error from previous phases
        if (preparedFile.isHasError()) {
            log.debug("Skipping file with error: {}", preparedFile.getOldFullName());

            // Determine error type: extraction errors have isFile=false in original
            RenameStatus status = !preparedFile.getOriginalFile().isFile()
                    ? RenameStatus.ERROR_EXTRACTION
                    : RenameStatus.SKIPPED;

            return RenameResult.builder()
                               .withPreparedFile(preparedFile)
                               .withStatus(status)
                               .withErrorMessage(preparedFile.getErrorMessage().orElse(null))
                               .withExecutedAt(LocalDateTime.now())
                               .build();
        }

        // Skip if no rename needed
        if (!preparedFile.needsRename()) {
            log.debug("Skipping file (same name): {}", preparedFile.getOldFullName());
            return RenameResult.builder()
                               .withPreparedFile(preparedFile)
                               .withStatus(RenameStatus.SKIPPED)
                               .withErrorMessage("Same name, no rename needed")
                               .withExecutedAt(LocalDateTime.now())
                               .build();
        }

        // Execute physical rename
        try {
            Path oldPath = preparedFile.getOldPath();
            Path newPath = preparedFile.getNewPath();

            log.debug("Renaming: {} -> {}", oldPath.getFileName(), newPath.getFileName());

            // Check if source file exists
            if (!Files.exists(oldPath)) {
                return RenameResult.builder()
                                   .withPreparedFile(preparedFile)
                                   .withStatus(RenameStatus.ERROR_EXECUTION)
                                   .withErrorMessage("Source file does not exist")
                                   .withExecutedAt(LocalDateTime.now())
                                   .build();
            }

            var isCaseChange = oldPath.toAbsolutePath().toString().equalsIgnoreCase(newPath.toAbsolutePath().toString());

            // Resolve disk conflict: retry with suffix if target already exists
            if (!isCaseChange) {
                Path resolvedPath = resolveConflictWithDisk(newPath);
                if (resolvedPath == null) {
                    return RenameResult.builder()
                                       .withPreparedFile(preparedFile)
                                       .withStatus(RenameStatus.ERROR_EXECUTION)
                                       .withErrorMessage("Target file already exists and all suffix attempts are exhausted: " + newPath.getFileName())
                                       .withExecutedAt(LocalDateTime.now())
                                       .build();
                }
                newPath = resolvedPath; // May be original path (no conflict) or suffixed path
            }

            // Validate the final filename before physical rename
            String finalName = preparedFile.getNewFullName();
            if (!nameValidator.isValid(finalName)) {
                log.warn("Generated filename contains invalid characters: {}", finalName);
                return RenameResult.builder()
                                   .withPreparedFile(preparedFile)
                                   .withStatus(RenameStatus.ERROR_TRANSFORMATION)
                                   .withErrorMessage("Generated filename contains invalid characters: " + finalName)
                                   .withExecutedAt(LocalDateTime.now())
                                   .build();
            }

            // Perform rename
            if (isCaseChange) {
                Path absolutePath = oldPath.toAbsolutePath();
                File oldFile = absolutePath.toFile();
                boolean success = oldFile.renameTo(newPath.toFile());
                if (!success) {
                    return RenameResult.builder()
                                       .withPreparedFile(preparedFile)
                                       .withStatus(RenameStatus.ERROR_EXECUTION)
                                       .withErrorMessage("File renaming failed: " + newPath.getFileName())
                                       .withExecutedAt(LocalDateTime.now())
                                       .build();
                }
            } else {
                Files.move(oldPath, newPath);
            }

            log.info("Successfully renamed: {} -> {}",
                     oldPath.getFileName(), newPath.getFileName());

            return RenameResult.builder()
                               .withPreparedFile(preparedFile)
                               .withStatus(RenameStatus.SUCCESS)
                               .withErrorMessage(null)
                               .withExecutedAt(LocalDateTime.now())
                               .build();

        } catch (IOException e) {
            log.error("Failed to rename file: {}", preparedFile.getOldPath(), e);

            return RenameResult.builder()
                               .withPreparedFile(preparedFile)
                               .withStatus(RenameStatus.ERROR_EXECUTION)
                               .withErrorMessage("I/O error: " + e.getMessage())
                               .withExecutedAt(LocalDateTime.now())
                               .build();
        } catch (Exception e) {
            log.error("Unexpected error during rename: {}", preparedFile.getOldPath(), e);

            return RenameResult.builder()
                               .withPreparedFile(preparedFile)
                               .withStatus(RenameStatus.ERROR_EXECUTION)
                               .withErrorMessage("Unexpected error: " + e.getMessage())
                               .withExecutedAt(LocalDateTime.now())
                               .build();
        }
    }

    /**
     * Resolves a path conflict with a pre-existing disk file by appending
     * numbered suffixes: {@code name (001).ext}, {@code name (002).ext}, …
     *
     * @param targetPath the originally desired target path
     * @return a conflict-free path, or {@code null} if all 999 attempts are taken
     */
    private Path resolveConflictWithDisk(Path targetPath) {
        if (!Files.exists(targetPath)) {
            return targetPath;
        }

        String fullName = targetPath.getFileName().toString();
        int dotIndex = fullName.lastIndexOf('.');
        String baseName = (dotIndex > 0) ? fullName.substring(0, dotIndex) : fullName;
        String ext      = (dotIndex > 0) ? fullName.substring(dotIndex)    : "";
        Path   parent   = targetPath.getParent();
        int    digits   = String.valueOf(MAX_SUFFIX_ATTEMPTS).length(); // 3

        for (int i = 1; i <= MAX_SUFFIX_ATTEMPTS; i++) {
            String suffix    = String.format(" (%0" + digits + "d)", i);
            Path   candidate = parent.resolve(baseName + suffix + ext);
            if (!Files.exists(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}
