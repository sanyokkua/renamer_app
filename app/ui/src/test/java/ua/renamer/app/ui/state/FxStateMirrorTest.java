package ua.renamer.app.ui.state;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.*;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class FxStateMirrorTest {

    private static final long TIMEOUT_MS = 5_000;
    private FxStateMirror mirror;

    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException ignored) {
            // Toolkit already running in this JVM (e.g., prior test class)
            latch.countDown();
        }
        assertThat(latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS))
                .as("JavaFX toolkit must start within timeout").isTrue();
    }

    @BeforeEach
    void setUp() {
        mirror = new FxStateMirror();
    }

    // --- Initial state ---

    @Test
    void givenNewMirror_initialState_isEmpty() {
        assertThat(mirror.status().get()).isEqualTo(SessionStatus.EMPTY);
        assertThat(mirror.files().get()).isEmpty();
        assertThat(mirror.preview().get()).isEmpty();
        assertThat(mirror.renameResults().get()).isEmpty();
        assertThat(mirror.activeMode().get()).isNull();
        assertThat(mirror.currentParameters().get()).isNull();
    }

    // --- publishFilesChanged ---

    @Test
    void publishFilesChanged_fromNonFxThread_doesNotThrow() {
        assertThatCode(() -> mirror.publishFilesChanged(List.of(), List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void publishFilesChanged_updatesFilesAndPreviewLists() throws InterruptedException {
        var candidate = new RenameCandidate("id1", "photo", "jpg", Path.of("/tmp/photo.jpg"));
        var preview = new RenamePreview("id1", "photo.jpg", "photo_v2.jpg", false, null);
        CountDownLatch latch = new CountDownLatch(1);

        mirror.publishFilesChanged(List.of(candidate), List.of(preview));
        Platform.runLater(latch::countDown);
        assertThat(latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)).isTrue();

        assertThat(mirror.files().get()).containsExactly(candidate);
        assertThat(mirror.preview().get()).containsExactly(preview);
    }

    // --- publishPreviewChanged ---

    @Test
    void publishPreviewChanged_updatesPreviewList() throws InterruptedException {
        var preview = new RenamePreview("id2", "old.txt", "new.txt", false, null);
        CountDownLatch latch = new CountDownLatch(1);

        mirror.publishPreviewChanged(List.of(preview));
        Platform.runLater(latch::countDown);
        assertThat(latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)).isTrue();

        assertThat(mirror.preview().get()).containsExactly(preview);
    }

    @Test
    void publishPreviewChanged_withEmptyList_clearsPreview() throws InterruptedException {
        // Seed
        CountDownLatch seed = new CountDownLatch(1);
        mirror.publishPreviewChanged(List.of(new RenamePreview("x", "a", "b", false, null)));
        Platform.runLater(seed::countDown);
        seed.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);

        // Clear
        CountDownLatch latch = new CountDownLatch(1);
        mirror.publishPreviewChanged(List.of());
        Platform.runLater(latch::countDown);
        assertThat(latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)).isTrue();

        assertThat(mirror.preview().get()).isEmpty();
    }

    // --- publishModeChanged ---

    @Test
    void publishModeChanged_updatesModeAndParamsProperties() throws InterruptedException {
        var params = new AddTextParams("_v2", ItemPosition.END);
        CountDownLatch latch = new CountDownLatch(1);

        mirror.publishModeChanged(TransformationMode.ADD_TEXT, params);
        Platform.runLater(latch::countDown);
        assertThat(latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)).isTrue();

        assertThat(mirror.activeMode().get()).isEqualTo(TransformationMode.ADD_TEXT);
        assertThat(mirror.currentParameters().get()).isEqualTo(params);
    }

    // --- publishRenameComplete ---

    @Test
    void publishRenameComplete_updatesResultsListAndStatus() throws InterruptedException {
        var result = new RenameSessionResult("id1", "photo.jpg", "photo_v2.jpg",
                RenameStatus.SUCCESS, null);
        CountDownLatch latch = new CountDownLatch(1);

        mirror.publishRenameComplete(List.of(result), SessionStatus.COMPLETE);
        Platform.runLater(latch::countDown);
        assertThat(latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)).isTrue();

        assertThat(mirror.status().get()).isEqualTo(SessionStatus.COMPLETE);
        assertThat(mirror.renameResults().get()).containsExactly(result);
    }

    @Test
    void publishRenameComplete_resultsSetBeforeStatus_listVisibleInStatusListener()
            throws InterruptedException {
        var result = new RenameSessionResult("id2", "a.txt", "b.txt",
                RenameStatus.SUCCESS, null);
        AtomicBoolean resultsVisibleOnStatusChange = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        mirror.status().addListener((obs, oldVal, newVal) -> {
            resultsVisibleOnStatusChange.set(!mirror.renameResults().isEmpty());
            latch.countDown();
        });

        mirror.publishRenameComplete(List.of(result), SessionStatus.COMPLETE);
        assertThat(latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)).isTrue();
        assertThat(resultsVisibleOnStatusChange.get())
                .as("renameResults must be populated before status fires").isTrue();
    }

    // --- publishStatusChanged ---

    @Test
    void publishStatusChanged_updatesStatusProperty() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        mirror.publishStatusChanged(SessionStatus.EXECUTING);
        Platform.runLater(latch::countDown);
        assertThat(latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)).isTrue();

        assertThat(mirror.status().get()).isEqualTo(SessionStatus.EXECUTING);
    }

    @Test
    void publishStatusChanged_allStatusValues_setCorrectly() throws InterruptedException {
        for (SessionStatus s : SessionStatus.values()) {
            CountDownLatch latch = new CountDownLatch(1);
            mirror.publishStatusChanged(s);
            Platform.runLater(latch::countDown);
            assertThat(latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)).isTrue();
            assertThat(mirror.status().get()).isEqualTo(s);
        }
    }

    // --- FX-thread guarantee ---

    @Test
    void publishStatusChanged_fromNonFxThread_mutationOccursOnFxThread()
            throws InterruptedException {
        AtomicBoolean wasOnFxThread = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        mirror.status().addListener((obs, oldVal, newVal) -> {
            wasOnFxThread.set(Platform.isFxApplicationThread());
            latch.countDown();
        });

        mirror.publishStatusChanged(SessionStatus.FILES_LOADED);  // called from test thread

        assertThat(latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS))
                .as("status listener must fire within timeout").isTrue();
        assertThat(wasOnFxThread.get())
                .as("property mutation must occur on FX Application Thread").isTrue();
    }

    // --- Accessor stability ---

    @Test
    void accessors_returnSamePropertyInstance_onRepeatedCalls() {
        assertThat(mirror.files()).isSameAs(mirror.files());
        assertThat(mirror.preview()).isSameAs(mirror.preview());
        assertThat(mirror.renameResults()).isSameAs(mirror.renameResults());
        assertThat(mirror.status()).isSameAs(mirror.status());
        assertThat(mirror.activeMode()).isSameAs(mirror.activeMode());
        assertThat(mirror.currentParameters()).isSameAs(mirror.currentParameters());
    }
}
