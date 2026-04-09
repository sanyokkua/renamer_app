package ua.renamer.app.api.settings;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Manage persistent application settings: load from disk, save to disk, and
 * provide the current in-memory state.
 *
 * <p>Implementations must be thread-safe. The in-memory cache is updated
 * atomically on every successful {@link #load()} or {@link #save(AppSettings)} call.
 */
public interface SettingsService {

    /**
     * Return the current in-memory settings without performing any I/O.
     *
     * @return the cached settings snapshot; never null
     */
    AppSettings getCurrent();

    /**
     * Read settings from the persistent file, update the in-memory cache, and
     * return the result. Falls back to {@link AppSettings#defaults()} if the
     * file does not exist or cannot be parsed.
     *
     * @return the loaded (or default) settings; never null
     */
    AppSettings load();

    /**
     * Persist the given settings to disk and update the in-memory cache.
     * Writes atomically via a temporary file to prevent corruption on crash.
     *
     * @param settings the settings to persist; must not be null
     * @throws IOException if the write fails (disk full, permission denied, etc.)
     */
    void save(AppSettings settings) throws IOException;

    /**
     * Return the OS-specific path to the settings file.
     * The file may not exist yet; callers must not assume it is readable.
     *
     * @return the expected settings file path; never null
     */
    Path getSettingsFilePath();
}
