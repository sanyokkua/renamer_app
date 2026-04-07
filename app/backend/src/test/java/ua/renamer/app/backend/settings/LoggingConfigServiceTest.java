package ua.renamer.app.backend.settings;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ua.renamer.app.api.settings.AppDefaults;
import ua.renamer.app.api.settings.AppSettings;
import ua.renamer.app.api.settings.LogLevel;
import ua.renamer.app.api.settings.SettingsService;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LoggingConfigService}.
 *
 * <p>Logback is live on the test classpath so the real {@code LoggerContext} IS the
 * SLF4J factory. The "not a LoggerContext" early-return branches are not reachable in
 * normal test execution. Coverage is achieved by exercising the full happy paths and
 * verifying that every public method never throws.
 *
 * <p>{@link MockitoSettings} is set to {@code LENIENT} because the service constructor
 * eagerly calls {@code configure()} → {@code settingsService.getCurrent()}, which
 * means every test must stub {@code getCurrent()} before constructing the service.
 * Strict mode would flag that stub as "unnecessary" in tests that only call
 * {@code reconfigure()} afterward (which accepts settings as a parameter and never
 * calls {@code getCurrent()} again). Lenient mode keeps strict verification of
 * unused mocks while relaxing the "consumed exactly once" requirement for stubs.
 *
 * <p>File-logging paths are redirected to {@link TempDir} via the mocked
 * {@code getSettingsFilePath()} return value.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoggingConfigServiceTest {

    @TempDir
    Path tempDir;
    @Mock
    private SettingsService settingsService;

    // -------------------------------------------------------------------------
    // Build helpers
    // -------------------------------------------------------------------------

    private static AppSettings settings(boolean loggingEnabled, LogLevel level) {
        return AppSettings.builder()
                .withVersion(AppDefaults.SETTINGS_VERSION)
                .withLanguage(AppDefaults.DEFAULT_LANGUAGE)
                .withCustomConfigEnabled(false)
                .withCustomConfigPath(null)
                .withLoggingEnabled(loggingEnabled)
                .withLogLevel(level)
                .build();
    }

    /**
     * Returns the path that {@code enableFileLogging} will use for the log directory.
     * Points inside the {@link TempDir} so real directory creation is safe and isolated.
     */
    private Path settingsPath() {
        return tempDir.resolve(AppDefaults.SETTINGS_FILE_NAME);
    }

    /**
     * Build a service whose constructor has logging DISABLED.
     * Only {@code getCurrent()} is called during construction; {@code getSettingsFilePath()}
     * is not invoked, but is pre-stubbed here for convenience in tests that will later
     * call {@code reconfigure(loggingEnabled=true)}.
     */
    private LoggingConfigService serviceWithLoggingOff() {
        when(settingsService.getCurrent()).thenReturn(settings(false, LogLevel.INFO));
        when(settingsService.getSettingsFilePath()).thenReturn(settingsPath());
        return new LoggingConfigService(settingsService);
    }

    /**
     * Build a service whose constructor has logging ENABLED.
     * Both {@code getCurrent()} and {@code getSettingsFilePath()} are called during
     * construction.
     */
    private LoggingConfigService serviceWithLoggingOn(LogLevel level) {
        when(settingsService.getCurrent()).thenReturn(settings(true, level));
        when(settingsService.getSettingsFilePath()).thenReturn(settingsPath());
        return new LoggingConfigService(settingsService);
    }

    // =========================================================================
    // configure() — @Inject method; called by Guice on first creation
    // =========================================================================

    @Nested
    class ConfigureTests {

        @Test
        void configure_whenLoggingDisabled_doesNotThrow() {
            // Arrange — constructor calls configure() once; we call it a second time explicitly
            when(settingsService.getCurrent()).thenReturn(settings(false, LogLevel.INFO));
            LoggingConfigService service = new LoggingConfigService(settingsService);

            // Act + Assert
            assertThatCode(service::configure).doesNotThrowAnyException();
        }

        @Test
        void configure_whenLoggingEnabled_doesNotThrow() {
            // Arrange
            LoggingConfigService service = serviceWithLoggingOn(LogLevel.DEBUG);

            // Act + Assert — second configure() call; path stub already in place
            assertThatCode(service::configure).doesNotThrowAnyException();
        }

        @Test
        void configure_whenLoggingDisabled_doesNotCallGetSettingsFilePath() {
            // Arrange — service constructed with logging off; reset interaction tracking
            when(settingsService.getCurrent()).thenReturn(settings(false, LogLevel.INFO));
            LoggingConfigService service = new LoggingConfigService(settingsService);
            clearInvocations(settingsService);

            // Act — explicit second configure() call with logging still off
            service.configure();

            // Assert — enableFileLogging was NOT reached
            verify(settingsService, never()).getSettingsFilePath();
        }

        @Test
        void configure_whenLoggingEnabled_callsGetSettingsFilePath() {
            // Arrange — service constructed with logging on; reset counts
            LoggingConfigService service = serviceWithLoggingOn(LogLevel.INFO);
            clearInvocations(settingsService);

            // Act — explicit second configure() call
            service.configure();

            // Assert
            verify(settingsService, atLeastOnce()).getSettingsFilePath();
        }

        @ParameterizedTest
        @EnumSource(LogLevel.class)
        void configure_withLoggingDisabledAndEachLogLevel_doesNotThrow(LogLevel level) {
            when(settingsService.getCurrent()).thenReturn(settings(false, level));
            LoggingConfigService service = new LoggingConfigService(settingsService);
            assertThatCode(service::configure).doesNotThrowAnyException();
        }

        @ParameterizedTest
        @EnumSource(LogLevel.class)
        void configure_withLoggingEnabledAndEachLogLevel_doesNotThrow(LogLevel level) {
            LoggingConfigService service = serviceWithLoggingOn(level);
            assertThatCode(service::configure).doesNotThrowAnyException();
        }
    }

    // =========================================================================
    // reconfigure() — invoked on every user settings change
    // =========================================================================

    @Nested
    class ReconfigureTests {

        @Test
        void reconfigure_whenLoggingDisabled_doesNotThrow() {
            LoggingConfigService service = serviceWithLoggingOff();

            assertThatCode(() -> service.reconfigure(settings(false, LogLevel.WARN)))
                    .doesNotThrowAnyException();
        }

        @Test
        void reconfigure_whenLoggingEnabled_doesNotThrow() {
            LoggingConfigService service = serviceWithLoggingOff();

            assertThatCode(() -> service.reconfigure(settings(true, LogLevel.DEBUG)))
                    .doesNotThrowAnyException();
        }

        @Test
        void reconfigure_whenLoggingDisabled_doesNotCallGetSettingsFilePath() {
            // Arrange — construct and reset interaction tracking
            when(settingsService.getCurrent()).thenReturn(settings(false, LogLevel.INFO));
            LoggingConfigService service = new LoggingConfigService(settingsService);
            clearInvocations(settingsService);

            // Act
            service.reconfigure(settings(false, LogLevel.ERROR));

            // Assert — logging disabled means enableFileLogging is never reached
            verify(settingsService, never()).getSettingsFilePath();
        }

        @Test
        void reconfigure_whenLoggingEnabled_callsGetSettingsFilePath() {
            // Arrange — construct with logging off, then reconfigure to on
            LoggingConfigService service = serviceWithLoggingOff();
            clearInvocations(settingsService);

            // Act
            service.reconfigure(settings(true, LogLevel.INFO));

            // Assert
            verify(settingsService, atLeastOnce()).getSettingsFilePath();
        }

        @Test
        void reconfigure_disablesLogging_afterPreviouslyEnabled() {
            // Arrange — construct with logging on; getSettingsFilePath already stubbed
            LoggingConfigService service = serviceWithLoggingOn(LogLevel.INFO);
            clearInvocations(settingsService);

            // Act — turn logging off
            service.reconfigure(settings(false, LogLevel.INFO));

            // Assert — no path lookup needed when disabling file logging
            verify(settingsService, never()).getSettingsFilePath();
        }

        @ParameterizedTest
        @EnumSource(LogLevel.class)
        void reconfigure_withLoggingDisabledAndEachLogLevel_doesNotThrow(LogLevel level) {
            LoggingConfigService service = serviceWithLoggingOff();
            assertThatCode(() -> service.reconfigure(settings(false, level)))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @EnumSource(LogLevel.class)
        void reconfigure_withLoggingEnabledAndEachLogLevel_doesNotThrow(LogLevel level) {
            LoggingConfigService service = serviceWithLoggingOff();
            assertThatCode(() -> service.reconfigure(settings(true, level)))
                    .doesNotThrowAnyException();
        }
    }

    // =========================================================================
    // No-throw contract
    // =========================================================================

    @Nested
    class NoThrowContractTests {

        @Test
        void constructor_withLoggingDisabled_doesNotThrow() {
            when(settingsService.getCurrent()).thenReturn(settings(false, LogLevel.INFO));

            assertThatCode(() -> new LoggingConfigService(settingsService))
                    .doesNotThrowAnyException();
        }

        @Test
        void constructor_withLoggingEnabled_doesNotThrow() {
            when(settingsService.getCurrent()).thenReturn(settings(true, LogLevel.INFO));
            when(settingsService.getSettingsFilePath()).thenReturn(settingsPath());

            assertThatCode(() -> new LoggingConfigService(settingsService))
                    .doesNotThrowAnyException();
        }

        @Test
        void reconfigure_neverThrows_withLoggingDisabled() {
            LoggingConfigService service = serviceWithLoggingOff();

            assertThatCode(() -> service.reconfigure(settings(false, LogLevel.ERROR)))
                    .doesNotThrowAnyException();
        }

        @Test
        void reconfigure_neverThrows_withLoggingEnabled() {
            LoggingConfigService service = serviceWithLoggingOff();

            assertThatCode(() -> service.reconfigure(settings(true, LogLevel.DEBUG)))
                    .doesNotThrowAnyException();
        }
    }

    // =========================================================================
    // Repeated enable/disable cycling — covers removeFileAppender → re-addAppender path
    // =========================================================================

    @Nested
    class EnableDisableCycleTests {

        @Test
        void reconfigure_toggleOnOffOn_doesNotThrow() {
            // Arrange — logging starts off; path stub covers both ON cycles
            LoggingConfigService service = serviceWithLoggingOff();

            // Act — on → off → on
            assertThatCode(() -> {
                service.reconfigure(settings(true, LogLevel.INFO));
                service.reconfigure(settings(false, LogLevel.INFO));
                service.reconfigure(settings(true, LogLevel.DEBUG));
            }).doesNotThrowAnyException();
        }

        @Test
        void reconfigure_toggleOffOnOffOn_doesNotThrow() {
            // Arrange
            LoggingConfigService service = serviceWithLoggingOff();

            // Act — two full on/off cycles
            assertThatCode(() -> {
                service.reconfigure(settings(false, LogLevel.WARN));
                service.reconfigure(settings(true, LogLevel.WARN));
                service.reconfigure(settings(false, LogLevel.ERROR));
                service.reconfigure(settings(true, LogLevel.ERROR));
            }).doesNotThrowAnyException();
        }
    }
}
