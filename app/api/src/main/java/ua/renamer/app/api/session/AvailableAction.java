package ua.renamer.app.api.session;

/**
 * Actions that may be available on a rename session at a given point in its lifecycle.
 *
 * <p>The set of available actions depends on the current {@link SessionStatus}.
 * Callers should query {@link SessionApi#availableActions()} rather than
 * inferring availability from status alone.
 */
public enum AvailableAction {
    /**
     * Add one or more files to the session's file list.
     */
    ADD_FILES,
    /**
     * Remove one or more files from the session's file list by their file IDs.
     */
    REMOVE_FILES,
    /**
     * Remove all files from the session, resetting it to {@link SessionStatus#EMPTY}.
     */
    CLEAR,
    /**
     * Select a transformation mode and configure its parameters.
     */
    SELECT_MODE,
    /**
     * Execute the rename operation; only available when {@link SessionApi#canExecute()}
     * returns {@code true}.
     */
    EXECUTE,
    /**
     * Request cancellation of an in-progress rename execution.
     * Only available when the session is in {@link SessionStatus#EXECUTING}.
     */
    CANCEL
}
