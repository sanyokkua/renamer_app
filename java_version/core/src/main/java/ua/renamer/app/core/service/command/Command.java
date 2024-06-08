package ua.renamer.app.core.service.command;

import ua.renamer.app.core.service.ProgressCallback;

/**
 * An interface representing a command with input and output types.
 *
 * @param <I> the type of input for the command.
 * @param <O> the type of output for the command.
 */
public interface Command<I, O> {

    /**
     * Executes the command with the given input and progress callback.
     *
     * @param input    the input for the command.
     * @param callback the progress callback to track the execution progress.
     * @return the output of the command.
     */
    O execute(I input, ProgressCallback callback);

}
