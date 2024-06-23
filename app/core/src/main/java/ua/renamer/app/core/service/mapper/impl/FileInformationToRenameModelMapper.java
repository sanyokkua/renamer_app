package ua.renamer.app.core.service.mapper.impl;

import ua.renamer.app.core.enums.RenameResult;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.core.service.mapper.DataMapper;
import ua.renamer.app.core.util.FileInformationUtils;

public class FileInformationToRenameModelMapper implements DataMapper<FileInformation, RenameModel> {

    @Override
    public RenameModel map(FileInformation input) {
        var isNeedRename = FileInformationUtils.isFileHasChangedName(input);
        var oldName = FileInformationUtils.getFileFullName(input);
        var newName = FileInformationUtils.getFileNewFullName(input);

        try {
            var absolutePathWithoutName = FileInformationUtils.getFileAbsolutePathWithoutName(input,
                                                                                              FileInformationUtils::getFileFullName);
            return RenameModel.builder()
                              .fileInformation(input)
                              .isNeedRename(isNeedRename)
                              .oldName(oldName)
                              .newName(newName)
                              .absolutePathWithoutName(absolutePathWithoutName)
                              .build();
        } catch (IllegalArgumentException e) {
            return RenameModel.builder()
                              .fileInformation(input)
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
