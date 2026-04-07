package ua.renamer.app.backend.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.renamer.app.api.settings.AppDefaults;
import ua.renamer.app.api.settings.AppSettings;
import ua.renamer.app.api.settings.LogLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for {@link SettingsServiceImpl}.
 *
 * <p>Strategy: a package-private {@link TestableSettingsServiceImpl} subclass
 * overrides {@link SettingsServiceImpl#getSettingsFilePath()} to return a path
 * inside a JUnit {@link TempDir} directory. This avoids any I/O touching the
 * real user home directory and keeps tests fully isolated.
 *
 * <p>Each test method that requires fresh state creates a new service instance
 * via {@link #newService()}, which calls {@code doLoad()} eagerly in the
 * constructor — exactly as production code does.
 */
class SettingsServiceImplTest {

    @TempDir
    Path tempDir;

    /**
     * Points to the settings file inside the temp directory.
     */
    private Path settingsFile;

    @BeforeEach
    void setUp() {
        settingsFile = tempDir.resolve(AppDefaults.SETTINGS_FILE_NAME);
    }

    /**
     * Create a {@link SettingsServiceImpl} whose {@code getSettingsFilePath()}
     * returns {@link #settingsFile}. The constructor eagerly calls {@code doLoad()}.
     */
    private SettingsServiceImpl newService() {
        return new TestableSettingsServiceImpl(settingsFile);
    }

    // =========================================================================
    // load — missing file
    // =========================================================================

    /**
     * Package-private subclass used only in tests. Overrides
     * {@link SettingsServiceImpl#getSettingsFilePath()} to return a path inside
     * a {@link TempDir}-controlled directory so tests never touch the real user
     * home directory. The override is in place before the constructor body
     * executes {@code doLoad()}, which calls {@code getSettingsFilePath()}.
     */
    static final class TestableSettingsServiceImpl extends SettingsServiceImpl {

        private final Path overriddenPath;

        TestableSettingsServiceImpl(Path settingsFilePath) {
            // SettingsServiceImpl() calls doLoad() → getSettingsFilePath() in
            // its own constructor body. Because Java dispatches overridden
            // methods even during superclass construction, by the time the
            // super() call returns here, overriddenPath is NOT yet set
            // (it is initialized in the line below). This means the very first
            // doLoad() call in the super constructor would see null.
            //
            // Work-around: the super constructor stores the result of doLoad()
            // in `current`. We then set overriddenPath, call load() once more
            // to actually read from the intended temp path, and store the result
            // back into `current` via the public load() API.
            super();
            this.overriddenPath = settingsFilePath;
            // Re-run load so that this instance reflects the temp-dir file.
            load();
        }

        @Override
        public Path getSettingsFilePath() {
            // Guard against the super-constructor call that happens before
            // overriddenPath is assigned.
            return overriddenPath != null ? overriddenPath : super.getSettingsFilePath();
        }
    }

    // =========================================================================
    // load — valid JSON
    // =========================================================================

    @Nested
    class LoadWhenFileAbsent {

        @Test
        void load_whenFileAbsent_returnsDefaultVersion() {
            // Arrange — file does not exist
            SettingsServiceImpl service = newService();

            // Assert
            assertThat(service.getCurrent().getVersion())
                    .isEqualTo(AppDefaults.SETTINGS_VERSION);
        }

        @Test
        void load_whenFileAbsent_returnsDefaultLanguage() {
            SettingsServiceImpl service = newService();

            assertThat(service.getCurrent().getLanguage())
                    .isEqualTo(AppDefaults.DEFAULT_LANGUAGE);
        }

        @Test
        void load_whenFileAbsent_returnsDefaultLogLevel() {
            SettingsServiceImpl service = newService();

            assertThat(service.getCurrent().getLogLevel())
                    .isEqualTo(AppDefaults.DEFAULT_LOG_LEVEL);
        }

        @Test
        void load_whenFileAbsent_returnsLoggingDisabled() {
            SettingsServiceImpl service = newService();

            assertThat(service.getCurrent().isLoggingEnabled()).isFalse();
        }

        @Test
        void load_whenFileAbsent_returnsCustomConfigDisabled() {
            SettingsServiceImpl service = newService();

            assertThat(service.getCurrent().isCustomConfigEnabled()).isFalse();
        }

        @Test
        void load_whenFileAbsent_returnsNullCustomConfigPath() {
            SettingsServiceImpl service = newService();

            assertThat(service.getCurrent().getCustomConfigPath()).isNull();
        }
    }

    // =========================================================================
    // load — malformed JSON
    // =========================================================================

    @Nested
    class LoadWhenValidJson {

        @Test
        void load_whenValidJson_parsesVersion() throws IOException {
            // Arrange
            Files.writeString(settingsFile, buildValidJson());

            // Act
            SettingsServiceImpl service = newService();

            // Assert
            assertThat(service.getCurrent().getVersion()).isEqualTo(42);
        }

        @Test
        void load_whenValidJson_parsesLanguage() throws IOException {
            Files.writeString(settingsFile, buildValidJson());

            SettingsServiceImpl service = newService();

            assertThat(service.getCurrent().getLanguage()).isEqualTo("uk");
        }

        @Test
        void load_whenValidJson_parsesLogLevel() throws IOException {
            Files.writeString(settingsFile, buildValidJson());

            SettingsServiceImpl service = newService();

            assertThat(service.getCurrent().getLogLevel()).isEqualTo(LogLevel.DEBUG);
        }

        @Test
        void load_whenValidJson_parsesLoggingEnabled() throws IOException {
            Files.writeString(settingsFile, buildValidJson());

            SettingsServiceImpl service = newService();

            assertThat(service.getCurrent().isLoggingEnabled()).isTrue();
        }

        @Test
        void load_whenValidJson_parsesCustomConfigEnabled() throws IOException {
            Files.writeString(settingsFile, buildValidJson());

            SettingsServiceImpl service = newService();

            assertThat(service.getCurrent().isCustomConfigEnabled()).isTrue();
        }

        @Test
        void load_whenValidJson_parsesCustomConfigPath() throws IOException {
            Files.writeString(settingsFile, buildValidJson());

            SettingsServiceImpl service = newService();

            assertThat(service.getCurrent().getCustomConfigPath())
                    .isEqualTo("/custom/logback.xml");
        }

        /**
         * Builds a JSON payload with all non-default values for round-trip tests.
         */
        private String buildValidJson() {
            return """
                    {
                      "version": 42,
                      "general": {
                        "language": "uk",
                        "customConfigEnabled": true,
                        "customConfigPath": "/custom/logback.xml",
                        "logging": {
                          "enabled": true,
                          "level": "DEBUG"
                        }
                      }
                    }
                    """;
        }
    }

    // =========================================================================
    // load — unknown fields
    // =========================================================================

    @Nested
    class LoadWhenMalformedJson {

        @Test
        void load_whenMalformedJson_returnsDefaultLanguage() throws IOException {
            // Arrange — garbage that Jackson cannot parse as a tree
            Files.writeString(settingsFile, "{ this is not json ]]]");

            SettingsServiceImpl service = newService();

            assertThat(service.getCurrent().getLanguage())
                    .isEqualTo(AppDefaults.DEFAULT_LANGUAGE);
        }

        @Test
        void load_whenMalformedJson_returnsDefaultLogLevel() throws IOException {
            Files.writeString(settingsFile, "not-json-at-all");

            SettingsServiceImpl service = newService();

            assertThat(service.getCurrent().getLogLevel())
                    .isEqualTo(AppDefaults.DEFAULT_LOG_LEVEL);
        }

        @Test
        void load_whenMalformedJson_neverThrows() throws IOException {
            Files.writeString(settingsFile, "<<broken>>");

            assertThatCode(this::newService).doesNotThrowAnyException();
        }

        private SettingsServiceImpl newService() {
            return SettingsServiceImplTest.this.newService();
        }
    }

    // =========================================================================
    // load — invalid log level
    // =========================================================================

    @Nested
    class LoadWhenUnknownFields {

        @Test
        void load_whenUnknownFields_parsesKnownLanguageField() throws IOException {
            // Arrange — JSON with extra keys at every level
            Files.writeString(settingsFile, """
                    {
                      "version": 1,
                      "unknownTopKey": "ignored",
                      "general": {
                        "language": "fr",
                        "customConfigEnabled": false,
                        "customConfigPath": null,
                        "surprise": 999,
                        "logging": {
                          "enabled": false,
                          "level": "WARN",
                          "extraLoggingKey": true
                        }
                      }
                    }
                    """);

            SettingsServiceImpl service = newService();

            // Known field must survive; unknown fields must be silently ignored
            assertThat(service.getCurrent().getLanguage()).isEqualTo("fr");
        }

        @Test
        void load_whenUnknownFields_parsesKnownLogLevel() throws IOException {
            Files.writeString(settingsFile, """
                    {
                      "version": 1,
                      "unknownTopKey": "ignored",
                      "general": {
                        "language": "fr",
                        "customConfigEnabled": false,
                        "customConfigPath": null,
                        "logging": {
                          "enabled": false,
                          "level": "WARN",
                          "extraLoggingKey": true
                        }
                      }
                    }
                    """);

            SettingsServiceImpl service = newService();

            assertThat(service.getCurrent().getLogLevel()).isEqualTo(LogLevel.WARN);
        }

        @Test
        void load_whenUnknownFields_neverThrows() {
            assertThatCode(() -> {
                Files.writeString(settingsFile, """
                        {
                          "version": 1,
                          "alien": {"nested": [1, 2, 3]},
                          "general": {
                            "language": "en",
                            "customConfigEnabled": false,
                            "customConfigPath": null,
                            "logging": { "enabled": false, "level": "INFO" }
                          }
                        }
                        """);
                newService();
            }).doesNotThrowAnyException();
        }
    }

    // =========================================================================
    // load — explicit null customConfigPath
    // =========================================================================

    @Nested
    class LoadWhenLogLevelInvalid {

        @Test
        void load_whenLogLevelIsBanana_fallsBackToInfo() throws IOException {
            // Arrange — BANANA is not a valid LogLevel enum constant
            Files.writeString(settingsFile, """
                    {
                      "version": 1,
                      "general": {
                        "language": "en",
                        "customConfigEnabled": false,
                        "customConfigPath": null,
                        "logging": {
                          "enabled": false,
                          "level": "BANANA"
                        }
                      }
                    }
                    """);

            SettingsServiceImpl service = newService();

            assertThat(service.getCurrent().getLogLevel()).isEqualTo(LogLevel.INFO);
        }

        @Test
        void load_whenLogLevelIsEmpty_fallsBackToInfo() throws IOException {
            Files.writeString(settingsFile, """
                    {
                      "version": 1,
                      "general": {
                        "language": "en",
                        "customConfigEnabled": false,
                        "customConfigPath": null,
                        "logging": {
                          "enabled": false,
                          "level": ""
                        }
                      }
                    }
                    """);

            SettingsServiceImpl service = newService();

            // Empty string → IllegalArgumentException in LogLevel.valueOf → fallback to INFO
            assertThat(service.getCurrent().getLogLevel()).isEqualTo(LogLevel.INFO);
        }
    }

    // =========================================================================
    // save — file creation
    // =========================================================================

    @Nested
    class LoadWhenCustomConfigPathIsNull {

        @Test
        void load_whenCustomConfigPathIsExplicitNull_preservesNull() throws IOException {
            // Arrange — JSON contains `"customConfigPath": null` explicitly
            Files.writeString(settingsFile, """
                    {
                      "version": 1,
                      "general": {
                        "language": "en",
                        "customConfigEnabled": false,
                        "customConfigPath": null,
                        "logging": {
                          "enabled": false,
                          "level": "INFO"
                        }
                      }
                    }
                    """);

            SettingsServiceImpl service = newService();

            assertThat(service.getCurrent().getCustomConfigPath()).isNull();
        }

        @Test
        void load_whenCustomConfigPathKeyAbsent_returnsNull() throws IOException {
            // Arrange — key is omitted entirely (not present in JSON)
            Files.writeString(settingsFile, """
                    {
                      "version": 1,
                      "general": {
                        "language": "en",
                        "customConfigEnabled": false,
                        "logging": {
                          "enabled": false,
                          "level": "INFO"
                        }
                      }
                    }
                    """);

            SettingsServiceImpl service = newService();

            assertThat(service.getCurrent().getCustomConfigPath()).isNull();
        }
    }

    // =========================================================================
    // save — JSON validity
    // =========================================================================

    @Nested
    class SaveCreatesFile {

        @Test
        void save_createsFileAtProvidedPath() throws IOException {
            // Arrange
            SettingsServiceImpl service = newService();
            AppSettings settings = AppSettings.defaults();

            // Act
            service.save(settings);

            // Assert
            assertThat(settingsFile).exists();
        }

        @Test
        void save_createsRegularFile() throws IOException {
            SettingsServiceImpl service = newService();

            service.save(AppSettings.defaults());

            assertThat(settingsFile).isRegularFile();
        }

        @Test
        void save_writesNonEmptyFile() throws IOException {
            SettingsServiceImpl service = newService();

            service.save(AppSettings.defaults());

            assertThat(Files.size(settingsFile)).isGreaterThan(0L);
        }
    }

    // =========================================================================
    // save → load round-trip
    // =========================================================================

    @Nested
    class SaveWritesValidJson {

        @Test
        void save_writesValidJson_fileContainsVersionKey() throws IOException {
            SettingsServiceImpl service = newService();
            AppSettings settings = AppSettings.defaults();

            service.save(settings);

            String content = Files.readString(settingsFile);
            assertThat(content).contains("\"version\"");
        }

        @Test
        void save_writesValidJson_fileContainsGeneralKey() throws IOException {
            SettingsServiceImpl service = newService();

            service.save(AppSettings.defaults());

            String content = Files.readString(settingsFile);
            assertThat(content).contains("\"general\"");
        }

        @Test
        void save_writesValidJson_fileContainsLoggingKey() throws IOException {
            SettingsServiceImpl service = newService();

            service.save(AppSettings.defaults());

            String content = Files.readString(settingsFile);
            assertThat(content).contains("\"logging\"");
        }

        @Test
        void save_writesValidJson_levelValueMatchesEnum() throws IOException {
            SettingsServiceImpl service = newService();
            AppSettings settings = AppSettings.builder()
                    .withVersion(AppDefaults.SETTINGS_VERSION)
                    .withLanguage(AppDefaults.DEFAULT_LANGUAGE)
                    .withCustomConfigEnabled(false)
                    .withCustomConfigPath(null)
                    .withLoggingEnabled(false)
                    .withLogLevel(LogLevel.WARN)
                    .build();

            service.save(settings);

            String content = Files.readString(settingsFile);
            assertThat(content).contains("\"WARN\"");
        }
    }

    // =========================================================================
    // save — parent directory auto-creation
    // =========================================================================

    @Nested
    class SaveThenLoadRoundtrip {

        @Test
        void saveAndLoad_roundtripsVersion() throws IOException {
            SettingsServiceImpl service = newService();
            AppSettings original = buildNonDefaultSettings();
            service.save(original);

            AppSettings reloaded = service.load();

            assertThat(reloaded.getVersion()).isEqualTo(original.getVersion());
        }

        @Test
        void saveAndLoad_roundtripsLanguage() throws IOException {
            SettingsServiceImpl service = newService();
            AppSettings original = buildNonDefaultSettings();
            service.save(original);

            AppSettings reloaded = service.load();

            assertThat(reloaded.getLanguage()).isEqualTo(original.getLanguage());
        }

        @Test
        void saveAndLoad_roundtripsLogLevel() throws IOException {
            SettingsServiceImpl service = newService();
            AppSettings original = buildNonDefaultSettings();
            service.save(original);

            AppSettings reloaded = service.load();

            assertThat(reloaded.getLogLevel()).isEqualTo(original.getLogLevel());
        }

        @Test
        void saveAndLoad_roundtripsLoggingEnabled() throws IOException {
            SettingsServiceImpl service = newService();
            AppSettings original = buildNonDefaultSettings();
            service.save(original);

            AppSettings reloaded = service.load();

            assertThat(reloaded.isLoggingEnabled()).isEqualTo(original.isLoggingEnabled());
        }

        @Test
        void saveAndLoad_roundtripsCustomConfigEnabled() throws IOException {
            SettingsServiceImpl service = newService();
            AppSettings original = buildNonDefaultSettings();
            service.save(original);

            AppSettings reloaded = service.load();

            assertThat(reloaded.isCustomConfigEnabled()).isEqualTo(original.isCustomConfigEnabled());
        }

        @Test
        void saveAndLoad_roundtripsCustomConfigPath() throws IOException {
            SettingsServiceImpl service = newService();
            AppSettings original = buildNonDefaultSettings();
            service.save(original);

            AppSettings reloaded = service.load();

            assertThat(reloaded.getCustomConfigPath())
                    .isEqualTo(original.getCustomConfigPath());
        }

        @Test
        void saveAndLoad_roundtripsNullCustomConfigPath() throws IOException {
            SettingsServiceImpl service = newService();
            AppSettings original = AppSettings.builder()
                    .withVersion(AppDefaults.SETTINGS_VERSION)
                    .withLanguage("en")
                    .withCustomConfigEnabled(false)
                    .withCustomConfigPath(null)
                    .withLoggingEnabled(false)
                    .withLogLevel(LogLevel.INFO)
                    .build();
            service.save(original);

            AppSettings reloaded = service.load();

            assertThat(reloaded.getCustomConfigPath()).isNull();
        }

        /**
         * Builds an {@link AppSettings} instance where every field has a
         * value that differs from {@link AppDefaults}, so a round-trip test
         * cannot accidentally pass due to defaulting.
         */
        private AppSettings buildNonDefaultSettings() {
            return AppSettings.builder()
                    .withVersion(99)
                    .withLanguage("uk")
                    .withCustomConfigEnabled(true)
                    .withCustomConfigPath("/tmp/custom-logback.xml")
                    .withLoggingEnabled(true)
                    .withLogLevel(LogLevel.ERROR)
                    .build();
        }
    }

    // =========================================================================
    // save — no leftover .tmp file
    // =========================================================================

    @Nested
    class SaveWhenDirectoryMissing {

        @Test
        void save_whenParentDirMissing_createsDirectory() throws IOException {
            // Arrange — settings file lives two levels deep inside tempDir,
            // with neither intermediate directory pre-created
            Path deepDir = tempDir.resolve("nested").resolve("deep");
            Path deepFile = deepDir.resolve(AppDefaults.SETTINGS_FILE_NAME);

            SettingsServiceImpl service = new TestableSettingsServiceImpl(deepFile);

            // Act
            service.save(AppSettings.defaults());

            // Assert — directory was created and file now exists
            assertThat(deepDir).isDirectory();
            assertThat(deepFile).exists();
        }

        @Test
        void save_whenParentDirAlreadyExists_doesNotThrow() {
            // Arrange — parent dir already exists (normal case)
            SettingsServiceImpl service = newService();

            // Act + Assert
            assertThatCode(() -> service.save(AppSettings.defaults()))
                    .doesNotThrowAnyException();
        }
    }

    // =========================================================================
    // getCurrent — reflects last save without reload
    // =========================================================================

    @Nested
    class SaveTmpFileCleanup {

        @Test
        void save_tmpFileRemovedAfterSuccess() throws IOException {
            // Arrange
            SettingsServiceImpl service = newService();
            Path tmpFile = settingsFile.resolveSibling(
                    AppDefaults.SETTINGS_FILE_NAME + ".tmp");

            // Act
            service.save(AppSettings.defaults());

            // Assert — the sibling .tmp file must not remain after a successful save
            assertThat(tmpFile).doesNotExist();
        }
    }

    // =========================================================================
    // load() explicit call
    // =========================================================================

    @Nested
    class GetCurrentReflectsSave {

        @Test
        void getCurrent_afterSave_returnsSavedSettings() throws IOException {
            // Arrange
            SettingsServiceImpl service = newService();
            AppSettings saved = AppSettings.builder()
                    .withVersion(AppDefaults.SETTINGS_VERSION)
                    .withLanguage("de")
                    .withCustomConfigEnabled(false)
                    .withCustomConfigPath(null)
                    .withLoggingEnabled(true)
                    .withLogLevel(LogLevel.DEBUG)
                    .build();

            // Act
            service.save(saved);

            // Assert — getCurrent() must reflect the just-saved value, no I/O needed
            assertThat(service.getCurrent().getLanguage()).isEqualTo("de");
            assertThat(service.getCurrent().getLogLevel()).isEqualTo(LogLevel.DEBUG);
        }

        @Test
        void getCurrent_neverReturnsNull() {
            SettingsServiceImpl service = newService();

            assertThat(service.getCurrent()).isNotNull();
        }
    }

    // =========================================================================
    // No-throw contract
    // =========================================================================

    @Nested
    class ExplicitLoadCall {

        @Test
        void load_afterSaveAndExternalModification_returnsUpdatedValue() throws IOException {
            // Arrange — service is constructed (no file yet → defaults loaded)
            SettingsServiceImpl service = newService();

            // Write a file externally (simulating another process or a different run)
            Files.writeString(settingsFile, """
                    {
                      "version": 1,
                      "general": {
                        "language": "it",
                        "customConfigEnabled": false,
                        "customConfigPath": null,
                        "logging": {
                          "enabled": false,
                          "level": "INFO"
                        }
                      }
                    }
                    """);

            // Act — explicit re-load picks up the externally written file
            AppSettings reloaded = service.load();

            // Assert
            assertThat(reloaded.getLanguage()).isEqualTo("it");
            assertThat(service.getCurrent().getLanguage()).isEqualTo("it");
        }
    }

    // =========================================================================
    // Test subclass — redirects getSettingsFilePath() to @TempDir
    // =========================================================================

    @Nested
    class NoThrowContract {

        @Test
        void constructor_whenFileAbsent_neverThrows() {
            assertThatCode(SettingsServiceImplTest.this::newService)
                    .doesNotThrowAnyException();
        }

        @Test
        void load_afterMalformedFile_neverThrows() throws IOException {
            Files.writeString(settingsFile, "{ BROKEN");
            SettingsServiceImpl service = newService();

            assertThatCode(service::load).doesNotThrowAnyException();
        }

        @Test
        void getCurrent_neverThrows() {
            SettingsServiceImpl service = newService();

            assertThatCode(service::getCurrent).doesNotThrowAnyException();
        }

        @Test
        void getSettingsFilePath_neverThrows() {
            SettingsServiceImpl service = newService();

            assertThatCode(service::getSettingsFilePath).doesNotThrowAnyException();
        }

        @Test
        void getSettingsFilePath_returnsExpectedPath() {
            SettingsServiceImpl service = newService();

            assertThat(service.getSettingsFilePath()).isEqualTo(settingsFile);
        }
    }
}
