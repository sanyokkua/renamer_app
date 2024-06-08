package ua.renamer.app.core.service.command.impl;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.ListProcessingCommand;
import ua.renamer.app.core.service.mapper.DataMapper;

import java.io.File;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MapFileToAppFileCommand extends ListProcessingCommand<File, FileInformation> {

    private final DataMapper<File, FileInformation> fileToFileInformationMapper;

    @Override
    public FileInformation processItem(File item) {
        return fileToFileInformationMapper.map(item);
    }

}
