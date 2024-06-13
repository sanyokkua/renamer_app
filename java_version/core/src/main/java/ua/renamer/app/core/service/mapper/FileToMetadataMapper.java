package ua.renamer.app.core.service.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.file.impl.FilesOperations;

import java.io.File;
import java.util.Optional;
import java.util.Set;

/**
 * An abstract class for mapping File objects to FileInformationMetadata objects.
 * It implements the ChainedDataMapper interface.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class FileToMetadataMapper implements ChainedDataMapper<File, FileInformationMetadata> {

    protected final FilesOperations filesOperations;
    private ChainedDataMapper<File, FileInformationMetadata> next;

    /**
     * Maps the given File input to a FileInformationMetadata object.
     * If this mapper can handle the input, it processes it; otherwise, it delegates to the next mapper in the chain.
     *
     * @param input the input File to be mapped
     *
     * @return the mapped FileInformationMetadata object
     */
    @Override
    public FileInformationMetadata map(File input) {
        if (canHandle(input)) {
            log.debug("Mapping of the file is possible: {}", input);
            return process(input);
        }

        var nextChain = getNext();
        log.debug("Mapping will be transferred to the data mapper: {}", nextChain);
        return nextChain.map(mapper -> mapper.map(input)).orElse(null);
    }

    /**
     * Retrieves the next data mapper in the chain.
     *
     * @return an Optional containing the next ChainedDataMapper, or an empty Optional if there is no next mapper
     */
    @Override
    public Optional<ChainedDataMapper<File, FileInformationMetadata>> getNext() {
        return Optional.ofNullable(next);
    }

    /**
     * Sets the next data mapper in the chain.
     *
     * @param next the next ChainedDataMapper in the chain
     */
    @Override
    public void setNext(ChainedDataMapper<File, FileInformationMetadata> next) {
        log.debug("Next data mapper: {}", next);
        this.next = next;
    }

    /**
     * Checks if this mapper can handle the given input File.
     *
     * @param input the input File to be checked
     *
     * @return true if this mapper can handle the input, false otherwise
     */
    @Override
    public boolean canHandle(File input) {
        log.debug("Can handle: {}", input);
        var mime = filesOperations.getMimeType(input);
        var mimeExtensions = filesOperations.getExtensionsFromMimeType(mime);
        var fileExtension = filesOperations.getFileExtension(input).toLowerCase();
        Set<String> extensionsToUse;

        if (!mimeExtensions.isEmpty() && fileExtension.isEmpty()) {
            extensionsToUse = mimeExtensions;
        } else if (!mimeExtensions.isEmpty()) {
            extensionsToUse = mimeExtensions;
        } else {
            extensionsToUse = Set.of(fileExtension);
        }

        var supportedExtensions = getSupportedExtensions();

        if (extensionsToUse.isEmpty() && supportedExtensions.isEmpty()) {
            log.debug("canHandle -> extension is empty and Mapper doesn't require extension");
            return true;
        }

        if (extensionsToUse.isEmpty()) {
            log.debug("canHandle -> file extension is empty, but expected, can't handle");
            return false;
        }

        var extensionFound = supportedExtensions.stream().filter(extensionsToUse::contains).findAny();

        log.debug("canHandle -> For file {}, was found extension: {}", input.getAbsolutePath(), extensionFound);
        return extensionFound.isPresent();
    }

    /**
     * Retrieves the set of supported file extensions for this mapper.
     *
     * @return a set containing the supported file extensions
     */
    public abstract Set<String> getSupportedExtensions();

}
