package ua.renamer.app.core.service.command.impl;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.core.service.command.ListProcessingCommand;
import ua.renamer.app.core.service.mapper.impl.FileInformationToRenameModelMapper;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ResetRenameModelsCommand extends ListProcessingCommand<RenameModel, RenameModel> {

    private final FileInformationToRenameModelMapper mapper;

    @Override
    public RenameModel processItem(RenameModel item) {
        FileInformation fileInformation = item.getFileInformation();
        fileInformation.setNewName(fileInformation.getFileName());
        fileInformation.setNewExtension(fileInformation.getFileExtension());
        return mapper.map(fileInformation);
    }
}
