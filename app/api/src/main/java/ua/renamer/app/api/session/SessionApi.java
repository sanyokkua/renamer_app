package ua.renamer.app.api.session;

import ua.renamer.app.api.model.TransformationMode;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Primary API contract for a rename session.
 *
 * <p>A session progresses through the lifecycle described by {@link SessionStatus}.
 * Callers interact exclusively through this interface; no JavaFX or implementation
 * types are exposed. All mutating methods return a {@link CompletableFuture} so
 * implementations may execute work on a background thread without blocking the caller.
 */
public interface SessionApi {

    /**
     * Adds the files at the given paths to the session's file list.
     *
     * @param paths file paths to add; must not be null; must not contain null elements
     * @return future completing with {@link CommandResult#succeeded()} on success,
     * or {@link CommandResult#failure(String)} on session-level error; never null
     */
    CompletableFuture<CommandResult> addFiles(List<Path> paths);

    /**
     * Removes files with the given IDs. Unknown IDs are silently ignored.
     *
     * @param fileIds stable file identifiers to remove; must not be null
     * @return future completing with {@link CommandResult#succeeded()} on success; never null
     */
    CompletableFuture<CommandResult> removeFiles(List<String> fileIds);

    /**
     * Removes all files, resetting the session to {@link SessionStatus#EMPTY}.
     *
     * @return future completing with {@link CommandResult#succeeded()} on success; never null
     */
    CompletableFuture<CommandResult> clearFiles();

    /**
     * Selects the given transformation mode with default parameters and returns a
     * typed {@link ModeApi}. A previous selection is replaced; any previously returned
     * {@link ModeApi} instance should be considered invalid after this call.
     *
     * @param <P>  the concrete {@link ModeParameters} subtype for {@code mode}
     * @param mode the mode to select; must not be null
     * @return future completing with a {@link ModeApi} for the selected mode; never null
     */
    <P extends ModeParameters> CompletableFuture<ModeApi<P>> selectMode(TransformationMode mode);

    /**
     * Begins rename execution for all loaded files using the active mode and parameters.
     * Callers should verify {@link #canExecute()} before calling this method.
     *
     * @return a {@link TaskHandle} for monitoring progress, requesting cancellation,
     * and obtaining results; never null
     */
    TaskHandle<List<RenameSessionResult>> execute();

    /**
     * Returns {@code true} if the session is in {@link SessionStatus#MODE_CONFIGURED}
     * and a call to {@link #execute()} is permitted.
     */
    boolean canExecute();

    /**
     * Returns an immutable snapshot of the session's current observable state.
     * Safe to read from any thread after it is returned.
     *
     * @return the current snapshot; never null
     */
    SessionSnapshot snapshot();

    /**
     * Returns the set of actions currently permitted given the session's state.
     *
     * @return unmodifiable list of available actions; never null; may be empty
     */
    List<AvailableAction> availableActions();
}
