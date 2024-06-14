package ua.renamer.app.core.service.command.impl;

import ua.renamer.app.core.enums.RenameResult;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.core.service.command.ListProcessingCommand;
import ua.renamer.app.core.util.FileInformationUtils;

/**
 * This class implements the ListProcessingCommand interface to map FileInformation objects to RenameModel objects.
 * It analyzes each FileInformation object to determine if renaming is necessary and constructs a corresponding RenameModel object.
 */
public class MapFileInformationToRenameModelCommand extends ListProcessingCommand<FileInformation, RenameModel> {

    /**
     * Processes a single FileInformation object and transforms it into a corresponding RenameModel object.
     * <p>
     * This method analyzes the provided FileInformation object to determine if the file needs to be renamed
     * based on changes in the original and new name/extension information. It then constructs a RenameModel
     * containing details about the file and its renaming requirements.
     *
     * @param item the FileInformation object to be processed
     *
     * @return a RenameModel object representing the processed file information
     */
    @Override
    public RenameModel processItem(FileInformation item) {
        var isNeedRename = FileInformationUtils.isFileHasChangedName(item);
        var oldName = FileInformationUtils.getFileFullName(item);
        var newName = FileInformationUtils.getFileNewFullName(item);

        try {
            var absolutePathWithoutName = FileInformationUtils.getFileAbsolutePathWithoutName(item,
                                                                                              FileInformationUtils::getFileFullName);
            return RenameModel.builder()
                              .fileInformation(item)
                              .isNeedRename(isNeedRename)
                              .oldName(oldName)
                              .newName(newName)
                              .absolutePathWithoutName(absolutePathWithoutName)
                              .build();
        } catch (IllegalArgumentException e) {
            return RenameModel.builder()
                              .fileInformation(item)
                              .isNeedRename(isNeedRename)
                              .oldName(oldName)
                              .newName(newName)
                              .absolutePathWithoutName("")
                              .hasRenamingError(true)
                              .renamingErrorMessage(e.getMessage())
                              .isRenamed(false)
                              .renameResult(RenameResult.NOT_RENAMED_BECAUSE_OF_ERROR)
                              .build();
        }
    }
}
