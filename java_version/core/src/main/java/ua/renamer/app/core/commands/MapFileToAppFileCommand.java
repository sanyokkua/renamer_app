package ua.renamer.app.core.commands;

import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.ListProcessingCommand;
import ua.renamer.app.core.model.AppFile;
import ua.renamer.app.core.utils.AppFileUtils;

import java.io.File;

@NoArgsConstructor
public class MapFileToAppFileCommand extends ListProcessingCommand<File, AppFile> {

    @Override
    public AppFile processItem(File item) {
        return AppFileUtils.createAppFile(item);
    }
}
