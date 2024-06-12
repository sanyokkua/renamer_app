package ua.renamer.app.core.service.command.impl;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.core.service.command.ListProcessingCommand;
import ua.renamer.app.core.service.file.impl.FilesOperations;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class RenameCommand extends ListProcessingCommand<RenameModel, RenameModel> {

    private final FilesOperations filesOperations;

    @Override
    public RenameModel processItem(RenameModel item) {
        return filesOperations.renameFile(item);
    }
}
