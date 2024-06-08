package ua.renamer.app.core.service.command;

import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.service.ProgressCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An abstract class representing a command for processing lists of items.
 *
 * @param <I> the type of input items for the command.
 * @param <O> the type of output items for the command.
 */
@Slf4j
public abstract class ListProcessingCommand<I, O> implements Command<List<I>, List<O>> {

    /**
     * Executes the command with the given input list and progress callback.
     *
     * @param input    the list of input items for the command.
     * @param callback the progress callback to track the execution progress.
     * @return the list of output items produced by the command.
     */
    @Override
    public List<O> execute(List<I> input, ProgressCallback callback) {
        if (Objects.isNull(input) || input.isEmpty()) {
            log.debug("Command Execution returns an empty list.");
            return List.of();
        }

        final int total = input.size();
        int index = 0;
        updateProgress(index, total, callback);

        List<I> preparedInput = preprocessInput(input);
        List<O> result = new ArrayList<>();

        for (I item : preparedInput) {
            result.add(processItem(item));
            index++;
            log.debug("Processed item: {}, index: {}", item, index);
            updateProgress(index, total, callback);
        }

        updateProgress(0, 0, callback);
        return result;
    }

    /**
     * Updates the progress using the progress callback.
     *
     * @param currentValue the current value of the progress.
     * @param maxValue     the maximum value of the progress.
     * @param callback     the progress callback to be invoked.
     */
    protected void updateProgress(int currentValue, int maxValue, ProgressCallback callback) {
        log.debug("Updating progress of {} to {}", currentValue, maxValue);
        if (Objects.nonNull(callback)) {
            callback.updateProgress(currentValue, maxValue);
        }
    }

    /**
     * Preprocesses the input list before processing each item.
     *
     * @param input the input list to be preprocessed.
     * @return the preprocessed input list.
     */
    protected List<I> preprocessInput(List<I> input) {
        return input;
    }

    /**
     * Processes an individual item from the input list.
     *
     * @param item the item to be processed.
     * @return the result of processing the item.
     */
    public abstract O processItem(I item);

}
