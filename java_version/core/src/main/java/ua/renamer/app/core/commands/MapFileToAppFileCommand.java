package ua.renamer.app.core.commands;

import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.ListProcessingCommand;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.utils.FileInformationUtils;

import java.io.File;

@NoArgsConstructor
public class MapFileToAppFileCommand extends ListProcessingCommand<File, FileInformation> {

    @Override
    public FileInformation processItem(File item) {
        return FileInformationUtils.createFileInformationFromFile(item);
    }

}
