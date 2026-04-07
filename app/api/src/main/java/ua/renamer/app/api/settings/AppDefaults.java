package ua.renamer.app.api.settings;

import lombok.experimental.UtilityClass;

/**
 * Provide compile-time constants for default application settings values.
 * All consumers should reference these constants rather than hard-coding values.
 */
@UtilityClass
public class AppDefaults {

    /** Schema version written to the settings file; increment on breaking schema changes. */
    public static final int SETTINGS_VERSION = 1;

    /** BCP-47 language tag used when no user preference has been saved. */
    public static final String DEFAULT_LANGUAGE = "en";

    /** Log level applied to the application logger when logging is first enabled. */
    public static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.INFO;

    /** Base filename of the persistent settings file stored in the application directory. */
    public static final String SETTINGS_FILE_NAME = "settings.json";

    /** OS-specific application directory name used under the platform config root. */
    public static final String APP_DIR_NAME = "Renamer";
}
