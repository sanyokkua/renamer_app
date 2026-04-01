package ua.renamer.app.api.session;

/**
 * Result of a session command execution (e.g. load files, apply mode, execute rename).
 *
 * @param success      {@code true} if the command completed without error
 * @param errorMessage a human-readable error description; {@code null} when {@code success} is {@code true}
 */
public record CommandResult(boolean success, String errorMessage) {

    /**
     * Creates a successful command result.
     *
     * @return a {@link CommandResult} with {@code success} set to {@code true}
     */
    public static CommandResult succeeded() {
        return new CommandResult(true, null);
    }

    /**
     * Creates a failed command result.
     *
     * @param message a human-readable description of the failure; must not be null
     * @return a {@link CommandResult} with {@code success} set to {@code false}
     */
    public static CommandResult failure(String message) {
        return new CommandResult(false, message);
    }
}
