package ua.renamer.app.ui.service.impl;

import com.google.inject.Inject;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.core.service.ProgressCallback;
import ua.renamer.app.core.service.command.FileInformationCommand;
import ua.renamer.app.core.service.command.impl.MapFileInformationToRenameModelCommand;
import ua.renamer.app.core.service.command.impl.MapFileToFileInformationCommand;
import ua.renamer.app.core.service.command.impl.RenameCommand;
import ua.renamer.app.core.service.mapper.impl.RenameModelToHtmlMapper;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;
import ua.renamer.app.ui.service.ListCallback;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class CoreFunctionalityHelper {

    private final ExecutorService executorService;
    private final LanguageTextRetrieverApi languageTextRetriever;
    private final RenameModelToHtmlMapper renameModelToHtmlMapper;
    private final MapFileToFileInformationCommand mapFileToFileInformationCommand;
    private final MapFileInformationToRenameModelCommand mapFileInformationToRenameModelCommand;
    private final RenameCommand renameCommand;

    private static Task<Void> buildTask(Consumer<ProgressCallback> runnable, ProgressBar progressBar) {
        Task<Void> runCommandTask = new Task<>() {
            @Override
            protected Void call() {
                log.debug("Background task is started");
                runnable.accept(this::updateProgress);
                updateProgress(0, 0);
                log.debug("Background task is finished");
                return null;
            }
        };
        runCommandTask.setOnFailed(event -> {
            Throwable exception = runCommandTask.getException();
            log.error("Background task failed", exception);
        });
        progressBar.progressProperty().bind(runCommandTask.progressProperty());
        return runCommandTask;
    }

    public void mapFileToRenameModel(List<File> list, ProgressBar bar, ListCallback<RenameModel> resultCallback) {
        var task = buildTask(progressCallback -> {
            var fileInfoList = mapFileToFileInformationCommand.execute(list, progressCallback);
            var renameModelList = mapFileInformationToRenameModelCommand.execute(fileInfoList, progressCallback);
            resultCallback.accept(renameModelList);
        }, bar);
        executorService.execute(task);
    }

    public void prepareFiles(List<RenameModel> list, FileInformationCommand cmd, ProgressBar bar,
                             ListCallback<RenameModel> resultCallback) {
        var task = buildTask(progressCallback -> {
            var listOfFileInfo = list.stream().map(RenameModel::getFileInformation).toList();
            var fileInfoList = cmd.execute(listOfFileInfo, progressCallback);
            var renameModelList = mapFileInformationToRenameModelCommand.execute(fileInfoList, progressCallback);

            resultCallback.accept(renameModelList);
        }, bar);
        executorService.execute(task);
    }

    public void renameFiles(List<RenameModel> files, ProgressBar bar, ListCallback<RenameModel> resultCallback) {
        var task = buildTask(progressCallback -> {
            var renameResult = renameCommand.execute(files, progressCallback);
            resultCallback.accept(renameResult);
        }, bar);
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
