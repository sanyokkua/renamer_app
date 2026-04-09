package ua.renamer.app.backend.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.AddTextParams;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.ValidationResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ModeApiImpl}.
 *
 * <p>{@link RenameSessionService} is mocked; no real threading infrastructure is required
 * except in the concurrency test which spawns virtual threads directly.
 */
@ExtendWith(MockitoExtension.class)
class ModeApiImplTest {

    private static final TransformationMode MODE = TransformationMode.ADD_TEXT;
    private static final AddTextParams DEFAULT_PARAMS = new AddTextParams("", ItemPosition.BEGIN);
    @Mock
    private RenameSessionService service;
    private ModeApiImpl<AddTextParams> impl;

    @BeforeEach
    void setUp() {
        impl = new ModeApiImpl<>(DEFAULT_PARAMS, MODE, service);
    }

    // =========================================================================
    // mode() and currentParameters()
    // =========================================================================

    @Nested
    class ModeTests {

        @Test
        void givenImpl_whenMode_thenReturnsBoundMode() {
            assertThat(impl.mode()).isEqualTo(MODE);
        }

        @Test
        void givenImpl_whenCurrentParameters_thenReturnsInitialParams() {
            assertThat(impl.currentParameters()).isEqualTo(DEFAULT_PARAMS);
        }
    }

    // =========================================================================
    // Listener registration
    // =========================================================================

    @Nested
    class ListenerTests {

        @Test
        void givenListener_whenAddTwice_thenOnlyRegisteredOnce() throws Exception {
            when(service.updateParameters(any())).thenReturn(completedFuture(ValidationResult.valid()));
            List<AddTextParams> collected = new CopyOnWriteArrayList<>();
            ModeApi.ParameterListener<AddTextParams> listener = collected::add;

            impl.addParameterListener(listener);
            impl.addParameterListener(listener); // duplicate — must be ignored

            impl.updateParameters(p -> p.withTextToAdd("x")).get(5, TimeUnit.SECONDS);

            assertThat(collected).hasSize(1);
        }

        @Test
        void givenListener_whenRemoveUnregistered_thenNoOp() throws Exception {
            when(service.updateParameters(any())).thenReturn(completedFuture(ValidationResult.valid()));
            List<AddTextParams> collected = new CopyOnWriteArrayList<>();
            ModeApi.ParameterListener<AddTextParams> listener = collected::add;

            impl.removeParameterListener(listener); // not registered — must not throw
            impl.updateParameters(p -> p.withTextToAdd("x")).get(5, TimeUnit.SECONDS);

            assertThat(collected).isEmpty();
        }

        @Test
        void givenUpdateSucceeds_whenUpdateParameters_thenListenerNotified() throws Exception {
            when(service.updateParameters(any())).thenReturn(completedFuture(ValidationResult.valid()));
            List<AddTextParams> collected = new CopyOnWriteArrayList<>();
            impl.addParameterListener(collected::add);

            impl.updateParameters(p -> p.withTextToAdd("hello")).get(5, TimeUnit.SECONDS);

            assertThat(collected).hasSize(1);
            assertThat(collected.get(0).textToAdd()).isEqualTo("hello");
        }

        @Test
        void givenUpdateFails_whenUpdateParameters_thenListenerNotNotified() throws Exception {
            when(service.updateParameters(any()))
                    .thenReturn(completedFuture(ValidationResult.fieldError("textToAdd", "invalid")));
            List<AddTextParams> collected = new CopyOnWriteArrayList<>();
            impl.addParameterListener(collected::add);

            impl.updateParameters(p -> p.withTextToAdd(null)).get(5, TimeUnit.SECONDS);

            assertThat(collected).isEmpty();
        }

        @Test
        void givenResetSucceeds_whenResetParameters_thenListenerNotifiedWithDefaults() throws Exception {
            when(service.updateParameters(any())).thenReturn(completedFuture(ValidationResult.valid()));

            // First, advance current params away from default
            impl.updateParameters(p -> p.withTextToAdd("changed")).get(5, TimeUnit.SECONDS);
            assertThat(impl.currentParameters().textToAdd()).isEqualTo("changed");

            List<AddTextParams> collected = new CopyOnWriteArrayList<>();
            impl.addParameterListener(collected::add);

            impl.resetParameters().get(5, TimeUnit.SECONDS);

            assertThat(collected).hasSize(1);
            assertThat(collected.get(0)).isEqualTo(DEFAULT_PARAMS);
        }
    }

    // =========================================================================
    // updateParameters
    // =========================================================================

    @Nested
    class UpdateParametersTests {

        @Test
        void givenValidMutator_whenUpdateParameters_thenCurrentParamsUpdated() throws Exception {
            when(service.updateParameters(any())).thenReturn(completedFuture(ValidationResult.valid()));

            impl.updateParameters(p -> p.withTextToAdd("hello")).get(5, TimeUnit.SECONDS);

            assertThat(impl.currentParameters().textToAdd()).isEqualTo("hello");
        }

        @Test
        void givenValidationError_whenUpdateParameters_thenCurrentParamsUnchanged() throws Exception {
            when(service.updateParameters(any()))
                    .thenReturn(completedFuture(ValidationResult.fieldError("textToAdd", "invalid")));

            impl.updateParameters(p -> p.withTextToAdd("bad")).get(5, TimeUnit.SECONDS);

            assertThat(impl.currentParameters()).isEqualTo(DEFAULT_PARAMS);
        }

        @Test
        void givenUpdateParametersCalledConcurrently_whenMultipleUpdates_thenNoRaceCondition()
                throws Exception {
            when(service.updateParameters(any())).thenReturn(completedFuture(ValidationResult.valid()));

            int threadCount = 20;
            CompletableFuture<?>[] futures = new CompletableFuture[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final String text = "text_" + i;
                CompletableFuture<Void> taskDone = new CompletableFuture<>();
                futures[i] = taskDone;
                Thread.ofVirtual().start(() -> {
                    impl.updateParameters(p -> p.withTextToAdd(text))
                            .whenComplete((r, ex) -> {
                                if (ex != null) {
                                    taskDone.completeExceptionally(ex);
                                } else {
                                    taskDone.complete(null);
                                }
                            });
                });
            }

            CompletableFuture.allOf(futures).get(10, TimeUnit.SECONDS);

            // After all concurrent updates complete, currentParameters must be non-null
            // and must be one of the valid updated values — no race-condition NPE or lost write
            assertThat(impl.currentParameters()).isNotNull();
            assertThat(impl.currentParameters().textToAdd()).startsWith("text_");
        }
    }

    // =========================================================================
    // resetParameters
    // =========================================================================

    @Nested
    class ResetParametersTests {

        @Test
        void givenReset_whenResetParameters_thenCurrentParamsRestoredToDefaults() throws Exception {
            when(service.updateParameters(any())).thenReturn(completedFuture(ValidationResult.valid()));

            // Advance state away from default
            impl.updateParameters(p -> p.withTextToAdd("modified")).get(5, TimeUnit.SECONDS);
            assertThat(impl.currentParameters().textToAdd()).isEqualTo("modified");

            impl.resetParameters().get(5, TimeUnit.SECONDS);

            assertThat(impl.currentParameters()).isEqualTo(DEFAULT_PARAMS);
        }

        @Test
        void givenResetValidationFails_whenResetParameters_thenCurrentParamsUnchanged() throws Exception {
            when(service.updateParameters(any())).thenReturn(completedFuture(ValidationResult.valid()));

            // Advance state away from default so we can observe no rollback
            impl.updateParameters(p -> p.withTextToAdd("modified")).get(5, TimeUnit.SECONDS);

            // Now make reset fail
            when(service.updateParameters(any()))
                    .thenReturn(completedFuture(ValidationResult.fieldError("field", "error")));

            impl.resetParameters().get(5, TimeUnit.SECONDS);

            // Still at "modified", not rolled back to default
            assertThat(impl.currentParameters().textToAdd()).isEqualTo("modified");
        }
    }
}
