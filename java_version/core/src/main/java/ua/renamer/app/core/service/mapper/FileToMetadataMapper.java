package ua.renamer.app.core.service.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.file.impl.FilesOperations;

import java.io.File;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public abstract class FileToMetadataMapper implements ChainedDataMapper<File, FileInformationMetadata> {

    protected final FilesOperations filesOperations;
    private ChainedDataMapper<File, FileInformationMetadata> next;

    @Override
    public Optional<ChainedDataMapper<File, FileInformationMetadata>> getNext() {
        return Optional.ofNullable(next);
    }

    @Override
    public void setNext(ChainedDataMapper<File, FileInformationMetadata> next) {
        log.debug("Next data mapper: {}", next);
        this.next = next;
    }

    @Override
    public boolean canHandle(File input) {
        log.debug("Can handle: {}", input);
        var extension = filesOperations.getFileExtension(input);
        var supportedExtensions = getSupportedExtensions();

        if (extension.isEmpty() && supportedExtensions.isEmpty()) {
            log.debug("canHandle -> extension is empty and Mapper doesn't require extension");
            return true;
        }

        if (extension.isEmpty()) {
            log.debug("canHandle -> file extension is empty, but expected, can't handle");
            return false;
        }

        var extensionFound = supportedExtensions.stream().filter(extension::contains).findAny();

        log.debug("canHandle -> extension found result: {}", extensionFound);
        return extensionFound.isPresent();
    }

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

    protected abstract Set<String> getSupportedExtensions();

}
