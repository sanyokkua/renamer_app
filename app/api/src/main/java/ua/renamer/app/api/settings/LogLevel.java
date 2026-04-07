package ua.renamer.app.api.settings;

/**
 * Represent the severity level for application file logging.
 * Maps directly to Logback log levels applied at runtime.
 */
public enum LogLevel {
    /** Finest-grained diagnostic information, suitable for development. */
    DEBUG,
    /** Informational messages confirming normal operation. */
    INFO,
    /** Potentially harmful situations that do not prevent normal operation. */
    WARN,
    /** Error events that may still allow the application to continue running. */
    ERROR
}
