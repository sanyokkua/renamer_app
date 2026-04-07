package ua.renamer.app.backend.settings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.settings.AppDefaults;
import ua.renamer.app.api.settings.AppSettings;
import ua.renamer.app.api.settings.LogLevel;
import ua.renamer.app.api.settings.SettingsService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Persist and retrieve {@link AppSettings} as a JSON file in the
 * OS-specific application configuration directory.
 *
 * <p>On write, the file is replaced atomically via a sibling {@code .tmp}
 * file to prevent corruption on crash. If atomic move is not supported by
 * the filesystem, a non-atomic replace is performed as a fallback.
 *
 * <p>On read failure or missing file the service transparently falls back
 * to {@link AppSettings#defaults()}, so callers never receive {@code null}.
 */
@Slf4j
@Singleton
public class SettingsServiceImpl implements SettingsService {

    private static final String CUSTOM_CONFIG_PATH = "customConfigPath";
    /**
     * Reusable Jackson mapper; ObjectMapper is thread-safe after construction.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Most-recently loaded or saved settings snapshot; updated atomically.
     */
    private volatile AppSettings current;

    /**
     * Construct the service and immediately load settings from disk.
     * Falls back to defaults if the settings file does not exist or
     * cannot be read.
     */
    @Inject
    public SettingsServiceImpl() {
        this.current = doLoad();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppSettings getCurrent() {
        return current;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppSettings load() {
        current = doLoad();
        return current;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(final AppSettings settings) throws IOException {
        Path path = getSettingsFilePath();
        Path dir = path.getParent();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        String json = serialize(settings);
        Path tmp = path.resolveSibling(AppDefaults.SETTINGS_FILE_NAME + ".tmp");
        Files.writeString(tmp, json);
        try {
            Files.move(tmp, path,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Files.move atomic failed with exception {}, fallback move is used", ex.getMessage());
        }
        current = settings;
        log.info("Settings saved to: {}", path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getSettingsFilePath() {
        return resolveAppDir().resolve(AppDefaults.SETTINGS_FILE_NAME);
    }

    private AppSettings doLoad() {
        Path path = getSettingsFilePath();
        if (!Files.exists(path)) {
            log.debug("Settings file not found, using defaults: {}", path);
            return AppSettings.defaults();
        }
        try {
            String json = Files.readString(path);
            return parse(json);
        } catch (IOException e) {
            log.warn("Failed to read settings file, using defaults: {}", path, e);
            return AppSettings.defaults();
        }
    }

    private AppSettings parse(final String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode general = root.path("general");
            JsonNode logging = general.path("logging");
            String rawLevel = logging.path("level")
                    .asText(AppDefaults.DEFAULT_LOG_LEVEL.name());
            LogLevel level = safeLogLevel(rawLevel);

            String rawCustomPath = general.path(CUSTOM_CONFIG_PATH).isNull()
                    ? null
                    : general.path(CUSTOM_CONFIG_PATH).asText(null);
            return AppSettings.builder()
                    .withVersion(root.path("version")
                            .asInt(AppDefaults.SETTINGS_VERSION))
                    .withLanguage(general.path("language")
                            .asText(AppDefaults.DEFAULT_LANGUAGE))
                    .withCustomConfigEnabled(
                            general.path("customConfigEnabled").asBoolean(false))
                    .withCustomConfigPath(rawCustomPath)
                    .withLoggingEnabled(
                            logging.path("enabled").asBoolean(false))
                    .withLogLevel(level)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse settings JSON, using defaults", e);
            return AppSettings.defaults();
        }
    }

    private LogLevel safeLogLevel(final String raw) {
        try {
            return LogLevel.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown log level '{}', falling back to INFO", raw);
            log.debug("safeLogLevel failed with exception: {}", e.getMessage());
            return AppDefaults.DEFAULT_LOG_LEVEL;
        }
    }

    private String serialize(final AppSettings s) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("version", s.getVersion());
        ObjectNode general = root.putObject("general");
        general.put("language", s.getLanguage());
        general.put("customConfigEnabled", s.isCustomConfigEnabled());
        if (s.getCustomConfigPath() != null) {
            general.put(CUSTOM_CONFIG_PATH, s.getCustomConfigPath());
        } else {
            general.putNull(CUSTOM_CONFIG_PATH);
        }
        ObjectNode logging = general.putObject("logging");
        logging.put("enabled", s.isLoggingEnabled());
        logging.put("level", s.getLogLevel().name());
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }

    private Path resolveAppDir() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home");
        if (os.contains("mac")) {
            return Path.of(home, "Library", "Application Support",
                    AppDefaults.APP_DIR_NAME);
        } else if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            return Path.of(appData != null ? appData : home,
                    AppDefaults.APP_DIR_NAME);
        } else {
            String xdg = System.getenv("XDG_CONFIG_HOME");
            return Path.of(xdg != null ? xdg : home + "/.config",
                    AppDefaults.APP_DIR_NAME.toLowerCase());
        }
    }
}
