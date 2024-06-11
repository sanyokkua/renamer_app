package ua.renamer.app.core.service.command.impl;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.ListProcessingCommand;
import ua.renamer.app.core.service.mapper.DataMapper;

import java.io.File;

/**
 * Command to map a list of File objects to FileInformation objects.
 */
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MapFileToFileInformation extends ListProcessingCommand<File, FileInformation> {

    private final DataMapper<File, FileInformation> fileToFileInformationMapper;

    /**
     * Processes a single File item to map it to a FileInformation object.
     *
     * @param item The File object to be processed.
     *
     * @return The resulting FileInformation object.
     */
    @Override
    public FileInformation processItem(File item) {
        return fileToFileInformationMapper.map(item);
    }
}
