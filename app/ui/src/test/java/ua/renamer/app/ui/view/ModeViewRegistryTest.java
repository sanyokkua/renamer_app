package ua.renamer.app.ui.view;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Parent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ua.renamer.app.api.model.TransformationMode;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ModeViewRegistryTest {

    private static final long TOOLKIT_TIMEOUT_MS = 5_000;

    private ModeViewRegistry registry;

    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException ignored) {
            // Toolkit already started in this JVM (e.g., prior test class).
            latch.countDown();
        }
        assertThat(latch.await(TOOLKIT_TIMEOUT_MS, TimeUnit.MILLISECONDS))
                .as("JavaFX toolkit must start within timeout").isTrue();
    }

    @BeforeEach
    void setUp() {
        registry = new ModeViewRegistry();
    }

    // -----------------------------------------------------------------------
    // Unregistered lookup
    // -----------------------------------------------------------------------

    @Nested
    class WhenNoFactoryRegistered {

        @Test
        void getView_unregisteredMode_returnsEmpty() {
            Optional<Parent> result = registry.getView(TransformationMode.ADD_TEXT);

            assertThat(result).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(TransformationMode.class)
        void getView_anyUnregisteredMode_returnsEmpty(TransformationMode mode) {
            Optional<Parent> result = registry.getView(mode);

            assertThat(result).isEmpty();
        }
    }

    // -----------------------------------------------------------------------
    // Register then retrieve
    // -----------------------------------------------------------------------

    @Nested
    class WhenFactoryRegistered {

        @Test
        void register_thenGetView_returnsSupplierResult() {
            Parent expectedView = new Group();
            registry.register(TransformationMode.ADD_TEXT, () -> expectedView);

            Optional<Parent> result = registry.getView(TransformationMode.ADD_TEXT);

            assertThat(result).isPresent();
            assertThat(result.get()).isSameAs(expectedView);
        }

        @Test
        void register_thenGetView_doesNotReturnViewForDifferentMode() {
            registry.register(TransformationMode.ADD_TEXT, Group::new);

            Optional<Parent> result = registry.getView(TransformationMode.REMOVE_TEXT);

            assertThat(result).isEmpty();
        }

        @Test
        void register_overwritesPreviousFactory() {
            Parent firstView = new Group();
            Parent secondView = new Group();
            registry.register(TransformationMode.CHANGE_CASE, () -> firstView);
            registry.register(TransformationMode.CHANGE_CASE, () -> secondView);

            Optional<Parent> result = registry.getView(TransformationMode.CHANGE_CASE);

            assertThat(result).isPresent();
            assertThat(result.get()).isSameAs(secondView);
        }

        @Test
        void getView_invokesSupplierEachCall() {
            AtomicInteger callCount = new AtomicInteger(0);
            registry.register(TransformationMode.ADD_DATETIME, () -> {
                callCount.incrementAndGet();
                return new Group();
            });

            registry.getView(TransformationMode.ADD_DATETIME);
            registry.getView(TransformationMode.ADD_DATETIME);
            registry.getView(TransformationMode.ADD_DATETIME);

            assertThat(callCount.get()).isEqualTo(3);
        }

        @Test
        void register_allTenModes_allRetrievable() {
            for (TransformationMode mode : TransformationMode.values()) {
                registry.register(mode, Group::new);
            }

            for (TransformationMode mode : TransformationMode.values()) {
                assertThat(registry.getView(mode))
                        .as("view for mode %s must be present", mode)
                        .isPresent();
            }
        }
    }

    // -----------------------------------------------------------------------
    // Registration isolation — one mode does not affect others
    // -----------------------------------------------------------------------

    @Nested
    class RegistrationIsolation {

        @Test
        void register_oneMode_doesNotAffectOtherModes() {
            registry.register(TransformationMode.NUMBER_FILES, Group::new);

            assertThat(registry.getView(TransformationMode.TRIM_NAME)).isEmpty();
            assertThat(registry.getView(TransformationMode.CHANGE_EXTENSION)).isEmpty();
            assertThat(registry.getView(TransformationMode.REPLACE_TEXT)).isEmpty();
        }

        @Test
        void register_twoDistinctModes_eachReturnsOwnView() {
            Parent viewA = new Group();
            Parent viewB = new Group();
            registry.register(TransformationMode.ADD_DIMENSIONS, () -> viewA);
            registry.register(TransformationMode.ADD_FOLDER_NAME, () -> viewB);

            assertThat(registry.getView(TransformationMode.ADD_DIMENSIONS).orElseThrow())
                    .isSameAs(viewA);
            assertThat(registry.getView(TransformationMode.ADD_FOLDER_NAME).orElseThrow())
                    .isSameAs(viewB);
        }
    }
}
