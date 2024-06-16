package ua.renamer.app.ui.service.impl;

import com.google.inject.Inject;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.core.service.ProgressCallback;
import ua.renamer.app.core.service.command.FileInformationCommand;
import ua.renamer.app.core.service.command.impl.*;
import ua.renamer.app.core.service.mapper.impl.RenameModelToHtmlMapper;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;
import ua.renamer.app.ui.service.ListCallback;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class CoreFunctionalityHelper {

    private final ExecutorService executorService;
    private final LanguageTextRetrieverApi languageTextRetriever;
    private final RenameModelToHtmlMapper renameModelToHtmlMapper;
    private final MapFileToFileInformationCommand mapFileToFileInformationCommand;
    private final MapFileInformationToRenameModelCommand mapFileInformationToRenameModelCommand;
    private final RenameCommand renameCommand;
    private final FixEqualNamesCommand fixEqualNamesCommand;
    private final ResetRenameModelsCommand resetRenameModelsCommand;

    private static <T> Task<List<T>> buildTaskWithCallbackOnUIThread(Function<ProgressCallback, List<T>> runnable,
                                                                     ProgressBar progressBar,
                                                                     ListCallback<T> resultCallback) {
        Task<List<T>> runCommandTask = new Task<>() {
            @Override
            protected List<T> call() {
                log.debug("Background task with OnSucceeded callback is started");
                updateProgress(0, 0);
                List<T> executionResult = runnable.apply(this::updateProgress);
                log.debug("Background task with OnSucceeded callback is finished");
                return executionResult;
            }
        };
        runCommandTask.setOnFailed(event -> {
            Throwable exception = runCommandTask.getException();
            log.error("Background task with OnSucceeded callback is failed", exception);
        });
        runCommandTask.setOnSucceeded(event -> {
            log.debug("Background task with OnSucceeded will call callback");
            resultCallback.accept(runCommandTask.getValue());
            log.debug("Background task with OnSucceeded has called callback");
        });

        progressBar.progressProperty().bind(runCommandTask.progressProperty());
        return runCommandTask;
    }

    public void mapFileToRenameModel(List<File> list, ProgressBar bar, ListCallback<RenameModel> resultCallback) {
        var task = buildTaskWithCallbackOnUIThread(progressCallback -> {
            var fileInfoList = mapFileToFileInformationCommand.execute(list, progressCallback);
            return mapFileInformationToRenameModelCommand.execute(fileInfoList, progressCallback);
        }, bar, resultCallback);
        executorService.execute(task);
    }

    public void resetModels(List<RenameModel> list, FileInformationCommand cmd, ProgressBar bar,
                            ListCallback<RenameModel> resultCallback) {
        var task = buildTaskWithCallbackOnUIThread(progressCallback -> {
            var resetResult = resetRenameModelsCommand.execute(list, progressCallback);
            var fileInformationList = resetResult.stream().map(RenameModel::getFileInformation).toList();
            var newCmdResult = cmd.execute(fileInformationList, progressCallback);
            return mapFileInformationToRenameModelCommand.execute(newCmdResult, progressCallback);
        }, bar, resultCallback);
        executorService.execute(task);
    }

    public void prepareFiles(List<RenameModel> list, FileInformationCommand cmd, ProgressBar bar,
                             ListCallback<RenameModel> resultCallback) {
        var task = buildTaskWithCallbackOnUIThread(progressCallback -> {
            var resetResult = resetRenameModelsCommand.execute(list, progressCallback);
            var listOfFileInfo = resetResult.stream().map(RenameModel::getFileInformation).toList();
            var fileInfoList = cmd.execute(listOfFileInfo, progressCallback);
            var fixedNames = fixEqualNamesCommand.execute(fileInfoList, progressCallback);
            return mapFileInformationToRenameModelCommand.execute(fixedNames, progressCallback);
        }, bar, resultCallback);
        executorService.execute(task);
    }

    public void renameFiles(List<RenameModel> files, ProgressBar bar, ListCallback<RenameModel> resultCallback) {
        var task = buildTaskWithCallbackOnUIThread(progressCallback -> renameCommand.execute(files, progressCallback),
                                                   bar,
                                                   resultCallback);
        executorService.execute(task);
    }

    public void reloadFiles(List<RenameModel> files, ProgressBar bar, ListCallback<RenameModel> resultCallback) {
        var task = buildTaskWithCallbackOnUIThread(progressCallback -> {
            var listOfFilesWithNewNames = files.stream().map(model -> {
                if (model.isRenamed()) {
                    return model.getAbsolutePathWithoutName() + model.getNewName();
                } else {
                    return model.getAbsolutePathWithoutName() + model.getOldName();
                }
            }).map(File::new).toList();
            var mappedFilesToFileInfo = mapFileToFileInformationCommand.execute(listOfFilesWithNewNames,
                                                                                progressCallback);
            return mapFileInformationToRenameModelCommand.execute(mappedFilesToFileInfo, progressCallback);
        }, bar, resultCallback);
        executorService.execute(task);
    }

    public String mapRenameModelToHtmlString(RenameModel renameModel) {
        return renameModelToHtmlMapper.map(renameModel);
    }

    public String getFileType(RenameModel fileInformation) {
        return fileInformation.getFileInformation().isFile()
                ? languageTextRetriever.getString(TextKeys.TYPE_FILE)
                : languageTextRetriever.getString(TextKeys.TYPE_FOLDER);
    }

    public String getFileStatus(RenameModel fileInformation) {
        if (fileInformation.isHasRenamingError()) {
            return fileInformation.getRenamingErrorMessage();
        }
        return languageTextRetriever.getString(fileInformation.getRenameResult().getValue());
    }

    public String getOldName(RenameModel renameModel) {
        return renameModel.getOldName();
    }

    public String getNewName(RenameModel renameModel) {
        return renameModel.getNewName();
    }

}
