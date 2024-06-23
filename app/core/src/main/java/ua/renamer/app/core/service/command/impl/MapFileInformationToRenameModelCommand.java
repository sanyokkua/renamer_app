package ua.renamer.app.core.service.command.impl;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.core.service.command.ListProcessingCommand;
import ua.renamer.app.core.service.mapper.impl.FileInformationToRenameModelMapper;

/**
 * This class implements the ListProcessingCommand interface to map FileInformation objects to RenameModel objects.
 * It analyzes each FileInformation object to determine if renaming is necessary and constructs a corresponding RenameModel object.
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MapFileInformationToRenameModelCommand extends ListProcessingCommand<FileInformation, RenameModel> {

    private final FileInformationToRenameModelMapper mapper;

    @Override
    public RenameModel processItem(FileInformation item) {
        return mapper.map(item);
    }
}
