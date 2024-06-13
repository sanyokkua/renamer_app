package ua.renamer.app.ui.service.impl;

import com.google.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.core.service.ProgressCallback;
import ua.renamer.app.core.service.command.FileInformationCommand;
import ua.renamer.app.core.service.command.impl.MapFileInformationToRenameModel;
import ua.renamer.app.core.service.command.impl.MapFileToFileInformation;
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
public class AppCoreFunctionalityHelper {

    private final ExecutorService executorService;
    private final MapFileToFileInformation mapFileToFileInformation;
    private final MapFileInformationToRenameModel mapFileInformationToRenameModel;
    private final RenameModelToHtmlMapper renameModelToHtmlMapper;
    private final RenameCommand renameCommand;
    private final LanguageTextRetrieverApi languageTextRetriever;

    public void mapFileToRenameModel(List<File> list, ProgressBar bar, ListCallback<RenameModel> resultCallback) {
        var task = buildTask(progressCallback -> {
            var fileInfoList = mapFileToFileInformation.execute(list, progressCallback);
            var renameModelList = mapFileInformationToRenameModel.execute(fileInfoList, progressCallback);
            resultCallback.accept(renameModelList);
        }, bar);
        executorService.execute(task);
    }

    private Task<Void> buildTask(Consumer<ProgressCallback> runnable, ProgressBar progressBar) {
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

    public void processCommand(List<RenameModel> list, FileInformationCommand cmd, ProgressBar bar,
                               ListCallback<RenameModel> resultCallback) {
        var task = buildTask(progressCallback -> {
            var listOfFileInfo = list.stream().map(RenameModel::getFileInformation).toList();
            var fileInfoList = cmd.execute(listOfFileInfo, progressCallback);
            var renameModelList = mapFileInformationToRenameModel.execute(fileInfoList, progressCallback);

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

    public SimpleStringProperty extractOriginalNameFromRenameModel(
            TableColumn.CellDataFeatures<RenameModel, String> cell) {
        return new SimpleStringProperty(getOriginalFileName(cell.getValue()));
    }

    private String getOriginalFileName(RenameModel fileInformation) {
        return fileInformation.getOldName();
    }

    public SimpleStringProperty extractFileTypeFromRenameModel(TableColumn.CellDataFeatures<RenameModel, String> cell) {
        return new SimpleStringProperty(getFileType(cell.getValue()));
    }

    private String getFileType(RenameModel fileInformation) {
        return fileInformation.getFileInformation().isFile()
                ? languageTextRetriever.getString(TextKeys.TYPE_FILE)
                : languageTextRetriever.getString(TextKeys.TYPE_FOLDER);
    }

    public SimpleStringProperty extractNewFileNameFromRenameModel(
            TableColumn.CellDataFeatures<RenameModel, String> cell) {
        return new SimpleStringProperty(getNewFileName(cell.getValue()));
    }

    private String getNewFileName(RenameModel fileInformation) {
        return fileInformation.getNewName();
    }

    public SimpleStringProperty extractStatusFromRenameModel(TableColumn.CellDataFeatures<RenameModel, String> cell) {
        RenameModel model = cell.getValue();
        String value = "";
        if (model.isHasRenamingError()) {
            value = model.getRenamingErrorMessage();
        } else {
            value = model.getRenameResult().name();
        }
        return new SimpleStringProperty(value);
    }

}
