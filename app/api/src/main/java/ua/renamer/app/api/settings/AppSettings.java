package ua.renamer.app.api.settings;

import lombok.Builder;
import lombok.Value;
import org.jspecify.annotations.Nullable;

/**
 * Immutable snapshot of all application settings.
 * Construct via {@link #builder()} or obtain a pre-populated instance from {@link #defaults()}.
 */
@Value
@Builder(setterPrefix = "with")
public class AppSettings {

    /** Settings schema version; used to detect and migrate old files. */
    int version;

    /** BCP-47 language tag for the UI locale (e.g. {@code "en"}, {@code "uk"}). */
    String language;

    /** {@code true} if the user has enabled a custom Logback configuration file. */
    boolean customConfigEnabled;

    /** Absolute path to the custom Logback config file; {@code null} when not set. */
    @Nullable String customConfigPath;

    /** {@code true} if file-based logging is active. */
    boolean loggingEnabled;

    /** Severity threshold applied to the {@code ua.renamer.app} logger when logging is enabled. */
    LogLevel logLevel;

    /**
     * Return an {@link AppSettings} instance populated with all factory defaults.
     *
     * @return a new settings object with default values; never null
     */
    public static AppSettings defaults() {
        return AppSettings.builder()
                          .withVersion(AppDefaults.SETTINGS_VERSION)
                          .withLanguage(AppDefaults.DEFAULT_LANGUAGE)
                          .withCustomConfigEnabled(false)
                          .withCustomConfigPath(null)
                          .withLoggingEnabled(false)
                          .withLogLevel(AppDefaults.DEFAULT_LOG_LEVEL)
                          .build();
    }
}
