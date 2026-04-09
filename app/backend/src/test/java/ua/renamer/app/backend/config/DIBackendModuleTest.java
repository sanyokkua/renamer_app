package ua.renamer.app.backend.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.renamer.app.api.session.SessionApi;
import ua.renamer.app.api.session.StatePublisher;
import ua.renamer.app.backend.service.BackendExecutor;
import ua.renamer.app.backend.session.RenameSessionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link DIBackendModule}.
 *
 * <p>Verifies that all Guice bindings resolve at injector-creation time and
 * that singleton-scoped bindings return the same instance on repeated calls.
 *
 * <p>The test-local injector supplements {@link DIBackendModule} with a stub
 * binding for the one runtime dependency that {@code DIBackendModule}
 * intentionally leaves unbound:
 * <ul>
 *   <li>{@link StatePublisher} — provided in production by {@code DIUIModule}</li>
 * </ul>
 */
class DIBackendModuleTest {

    private Injector injector;

    /**
     * Stop and detach the FILE appender created when Guice eagerly initialises
     * {@link ua.renamer.app.backend.settings.LoggingConfigService}. Without this
     * cleanup the appender (pointing at the real APPDATA log directory) remains
     * attached to the shared Logback root logger and leaks into subsequent test
     * classes in the same JVM.
     */
    @AfterEach
    void tearDown() {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext ctx)) {
            return;
        }
        ch.qos.logback.classic.Logger root = ctx.getLogger(Logger.ROOT_LOGGER_NAME);
        Appender<ILoggingEvent> appender = root.getAppender("FILE");
        if (appender != null) {
            appender.stop();
        }
        root.detachAppender("FILE");
    }

    @BeforeEach
    void setUp() {
        injector = Guice.createInjector(
                new DIBackendModule(),
                binder -> binder.bind(StatePublisher.class).toInstance(mock(StatePublisher.class))
        );
    }

    // =========================================================================
    // Binding resolution
    // =========================================================================

    @Nested
    class BindingResolution {

        @Test
        void sessionApiBinding_whenRequested_resolvesNonNull() {
            // Act
            SessionApi instance = injector.getInstance(SessionApi.class);

            // Assert
            assertThat(instance).isNotNull();
        }

        @Test
        void sessionApiBinding_whenRequested_resolvesToRenameSessionService() {
            // Act
            SessionApi instance = injector.getInstance(SessionApi.class);

            // Assert
            assertThat(instance).isInstanceOf(RenameSessionService.class);
        }

        @Test
        void backendExecutorBinding_whenRequested_resolvesNonNull() {
            // Act
            BackendExecutor instance = injector.getInstance(BackendExecutor.class);

            // Assert
            assertThat(instance).isNotNull();
        }
    }

    // =========================================================================
    // Singleton scope
    // =========================================================================

    @Nested
    class SingletonScope {

        @Test
        void sessionApiBinding_whenRequestedTwice_returnsSameInstance() {
            // Act
            SessionApi first = injector.getInstance(SessionApi.class);
            SessionApi second = injector.getInstance(SessionApi.class);

            // Assert
            assertThat(first).isSameAs(second);
        }

        @Test
        void backendExecutorBinding_whenRequestedTwice_returnsSameInstance() {
            // Act
            BackendExecutor first = injector.getInstance(BackendExecutor.class);
            BackendExecutor second = injector.getInstance(BackendExecutor.class);

            // Assert
            assertThat(first).isSameAs(second);
        }
    }
}
