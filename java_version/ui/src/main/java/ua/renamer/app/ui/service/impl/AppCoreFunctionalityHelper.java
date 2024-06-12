package ua.renamer.app.ui.service.impl;

import com.google.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.core.service.command.FileInformationCommand;
import ua.renamer.app.core.service.command.ListProcessingCommand;
import ua.renamer.app.core.service.command.impl.MapFileInformationToRenameModel;
import ua.renamer.app.core.service.command.impl.MapFileToFileInformation;
import ua.renamer.app.core.service.command.impl.RenameCommand;
import ua.renamer.app.core.service.mapper.impl.RenameModelToHtmlMapper;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;
import ua.renamer.app.ui.service.ListCallback;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class AppCoreFunctionalityHelper {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);

    private final MapFileToFileInformation mapFileToFileInformation;
    private final MapFileInformationToRenameModel mapFileInformationToRenameModel;
    private final RenameModelToHtmlMapper renameModelToHtmlMapper;
    private final RenameCommand renameCommand;
    private final LanguageTextRetrieverApi languageTextRetriever;

    public void mapFileInstancesToFileInformation(List<File> files, ListCallback<FileInformation> resultCallback,
                                                  ProgressBar progressBar) {
        executeListProcessingCommand(mapFileToFileInformation, files, progressBar, resultCallback);
    }

    public <I, O> void executeListProcessingCommand(ListProcessingCommand<I, O> command, List<I> items,
                                                    ProgressBar progressBar, ListCallback<O> resultCallback) {
        log.debug("Executing list processing command: {}", command);

        var resultCallbackOptional = Optional.ofNullable(resultCallback);

        Task<Void> runCommandTask = new Task<>() {
            @Override
            protected Void call() {
                log.debug("Background task is started");

                var result = command.execute(items, this::updateProgress);
                resultCallbackOptional.ifPresent(callback -> callback.accept(result));
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

        executorService.execute(runCommandTask);
    }

    public void mapFileInformationToRenameModel(List<FileInformation> files, ListCallback<RenameModel> resultCallback,
                                                ProgressBar progressBar) {
        executeListProcessingCommand(mapFileInformationToRenameModel, files, progressBar, resultCallback);
    }

    public void updateModelListByCommand(ObservableList<RenameModel> listOfModels, FileInformationCommand command,
                                         ProgressBar progressBar, ListCallback<RenameModel> resultCallback) {
        log.debug("updateModelListByCommand");
        ListCallback<RenameModel> renameModelListCallback = listOfRenameModel -> {
            log.debug("renameModelListCallback");
            listOfModels.clear();
            listOfModels.addAll(listOfRenameModel);
            resultCallback.accept(listOfModels);
        };

        ListCallback<FileInformation> fileInformationListCallback = listOfFileItems -> {
            log.debug("fileInformationListCallback");
            executeListProcessingCommand(mapFileInformationToRenameModel,
                                         listOfFileItems,
                                         progressBar,
                                         renameModelListCallback);
        };

        var list = listOfModels.stream().map(RenameModel::getFileInformation).toList();
        executeListProcessingCommand(command, list, progressBar, fileInformationListCallback);
    }

    public void renameFiles(List<RenameModel> files, ListCallback<RenameModel> resultCallback,
                            ProgressBar progressBar) {
        executeListProcessingCommand(renameCommand, files, progressBar, resultCallback);
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

}
