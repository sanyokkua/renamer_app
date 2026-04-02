package ua.renamer.app.backend.session;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.service.FileRenameOrchestrator;
import ua.renamer.app.api.service.ProgressCallback;
import ua.renamer.app.api.session.*;
import ua.renamer.app.backend.service.BackendExecutor;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RenameSessionService}.
 *
 * <p>Uses a real {@link BackendExecutor} so threading behaviour is exercised.
 * {@link FileRenameOrchestrator} and {@link StatePublisher} are mocked.
 *
 * <p>Every future is resolved with {@code .get(5, TimeUnit.SECONDS)} to block
 * the test thread until async work completes, preventing race conditions in assertions.
 */
@ExtendWith(MockitoExtension.class)
class RenameSessionServiceTest {

    @Mock
    private FileRenameOrchestrator orchestrator;

    @Mock
    private StatePublisher publisher;

    private BackendExecutor executor;
    private RenameSessionService service;

    // -------------------------------------------------------------------------
    // Fixture helpers
    // -------------------------------------------------------------------------

    private static FileModel buildFileModel(File file) {
        return FileModel.builder()
                .withFile(file)
                .withName("test")
                .withExtension("txt")
                .withAbsolutePath(file.getAbsolutePath())
                .withIsFile(true)
                .withFileSize(100L)
                .build();
    }

    private static PreparedFileModel buildPreparedModel(FileModel fileModel) {
        return PreparedFileModel.builder()
                .withOriginalFile(fileModel)
                .withNewName("test_renamed")
                .withNewExtension("txt")
                .withHasError(false)
                .withErrorMessage(null)
                .withTransformationMeta(null)
                .build();
    }

    /**
     * Stubs {@code extractMetadata} to return the given list, matching any File list
     * and any (including null) ProgressCallback.
     *
     * <p>Mockito's lenient stubbing is used via {@code doReturn} to avoid strict-stubbing
     * failures when both null and non-null callback variants are pre-registered.
     */
    private void stubExtractMetadata(List<FileModel> result) {
        // org.mockito.Mockito.lenient() avoids "unnecessary stubbing" failures when one
        // of the two variants is not triggered in a particular test.
        org.mockito.Mockito.lenient()
                .doReturn(result)
                .when(orchestrator)
                .extractMetadata(org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(ProgressCallback.class));
        org.mockito.Mockito.lenient()
                .doReturn(result)
                .when(orchestrator)
                .extractMetadata(org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.isNull());
    }

    private void stubComputePreview(TransformationMode mode, List<PreparedFileModel> result) {
        org.mockito.Mockito.lenient()
                .doReturn(result)
                .when(orchestrator)
                .computePreview(
                        org.mockito.ArgumentMatchers.any(),
                        eq(mode),
                        any(),
                        org.mockito.ArgumentMatchers.isNull());
    }

    private void stubComputePreviewAnyMode(List<PreparedFileModel> result) {
        org.mockito.Mockito.lenient()
                .doReturn(result)
                .when(orchestrator)
                .computePreview(
                        org.mockito.ArgumentMatchers.any(),
                        any(TransformationMode.class),
                        any(),
                        org.mockito.ArgumentMatchers.isNull());
    }

    @BeforeEach
    void setUp() {
        executor = new BackendExecutor();
        service = new RenameSessionService(orchestrator, executor, publisher);
    }

    @AfterEach
    void tearDown() {
        executor.close();
    }

    // =========================================================================
    // addFiles
    // =========================================================================

    @Nested
    class AddFilesTests {

        @Test
        void givenFilesAdded_whenAddFiles_thenPublisherReceivesFilesChangedEvent() throws Exception {
            // Arrange
            File fileA = new File("/tmp/test_a.txt");
            File fileB = new File("/tmp/test_b.txt");
            FileModel modelA = buildFileModel(fileA);
            FileModel modelB = buildFileModel(fileB);
            stubExtractMetadata(List.of(modelA, modelB));

            // Act
            CommandResult result = service.addFiles(
                    List.of(Path.of("/tmp/test_a.txt"), Path.of("/tmp/test_b.txt"))
            ).get(5, TimeUnit.SECONDS);

            // Assert — command succeeds
            assertThat(result.success()).isTrue();

            // Assert — publisher called; capture the arguments to check sizes
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<RenameCandidate>> candidatesCaptor =
                    ArgumentCaptor.forClass(List.class);
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<RenamePreview>> previewCaptor =
                    ArgumentCaptor.forClass(List.class);

            verify(publisher).publishFilesChanged(
                    candidatesCaptor.capture(), previewCaptor.capture());

            assertThat(candidatesCaptor.getValue()).hasSize(2);
            // No mode configured yet — preview must be empty
            assertThat(previewCaptor.getValue()).isEmpty();
        }

        @Test
        void givenExtractMetadataThrows_whenAddFiles_thenCommandResultIsFailure() throws Exception {
            // Arrange
            when(orchestrator.extractMetadata(any(List.class), isNull()))
                    .thenThrow(new RuntimeException("metadata extraction failed"));

            // Act
            CommandResult result = service.addFiles(
                    List.of(Path.of("/tmp/bad.txt"))
            ).get(5, TimeUnit.SECONDS);

            // Assert — exception is captured; result is failure, not a thrown exception
            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).contains("metadata extraction failed");
        }

        @Test
        void givenNullPaths_whenAddFiles_thenNullPointerExceptionThrown() {
            // Contract: paths must not be null; service throws NPE immediately (synchronously)
            assertThatThrownBy(() -> service.addFiles(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // =========================================================================
    // selectMode
    // =========================================================================

    @Nested
    class SelectModeTests {

        @Test
        void givenModeSelected_whenSelectMode_thenPreviewComputedAndPublished() throws Exception {
            // Arrange — add 2 files first
            File fileA = new File("/tmp/sel_a.txt");
            File fileB = new File("/tmp/sel_b.txt");
            FileModel modelA = buildFileModel(fileA);
            FileModel modelB = buildFileModel(fileB);
            stubExtractMetadata(List.of(modelA, modelB));
            service.addFiles(
                    List.of(Path.of("/tmp/sel_a.txt"), Path.of("/tmp/sel_b.txt"))
            ).get(5, TimeUnit.SECONDS);

            // Arrange — stub computePreview for the upcoming selectMode call
            PreparedFileModel prepA = buildPreparedModel(modelA);
            PreparedFileModel prepB = buildPreparedModel(modelB);
            stubComputePreview(TransformationMode.ADD_TEXT, List.of(prepA, prepB));

            // Act
            service.selectMode(TransformationMode.ADD_TEXT).get(5, TimeUnit.SECONDS);

            // Assert — mode change published
            verify(publisher).publishModeChanged(eq(TransformationMode.ADD_TEXT), any());

            // Assert — preview published with 2 items
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<RenamePreview>> previewCaptor =
                    ArgumentCaptor.forClass(List.class);
            verify(publisher).publishPreviewChanged(previewCaptor.capture());
            assertThat(previewCaptor.getValue()).hasSize(2);
        }

        @Test
        void givenNoFiles_whenSelectMode_thenPreviewNotPublished() throws Exception {
            // Act — select a mode when no files are loaded
            service.selectMode(TransformationMode.ADD_TEXT).get(5, TimeUnit.SECONDS);

            // Assert — mode change is still published
            verify(publisher).publishModeChanged(eq(TransformationMode.ADD_TEXT), any());

            // Assert — preview must NOT be published because file list is empty
            verify(publisher, never()).publishPreviewChanged(any(List.class));
        }

        @Test
        void givenNullMode_whenSelectMode_thenNullPointerExceptionThrown() {
            assertThatThrownBy(() -> service.selectMode(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // =========================================================================
    // updateParameters (package-private — called directly in same package)
    // =========================================================================

    @Nested
    class UpdateParametersTests {

        @Test
        void givenValidParams_whenUpdateParameters_thenValidationOkAndPreviewRecomputed()
                throws Exception {
            // Arrange — session needs files + mode for computePreview to run
            File fileA = new File("/tmp/upd_a.txt");
            FileModel modelA = buildFileModel(fileA);
            stubExtractMetadata(List.of(modelA));
            service.addFiles(List.of(Path.of("/tmp/upd_a.txt"))).get(5, TimeUnit.SECONDS);

            stubComputePreviewAnyMode(List.of(buildPreparedModel(modelA)));
            service.selectMode(TransformationMode.ADD_TEXT).get(5, TimeUnit.SECONDS);

            // Act — update with valid params (non-null text, non-null position)
            AddTextParams validParams = new AddTextParams("_suffix", ItemPosition.END);
            ValidationResult result = service.updateParameters(validParams).get(5, TimeUnit.SECONDS);

            // Assert — validation passed
            assertThat(result.ok()).isTrue();
            assertThat(result.isError()).isFalse();

            // Assert — preview published at least once (by updateParameters; may also be by selectMode)
            verify(publisher, atLeast(1)).publishPreviewChanged(any(List.class));
        }

        @Test
        void givenInvalidParams_whenUpdateParameters_thenValidationErrorReturnedAndNoPreviewUpdate()
                throws Exception {
            // Arrange — position == null triggers validation error in AddTextParams.validate()
            AddTextParams invalidParams = new AddTextParams(null, null);

            // Act
            ValidationResult result = service.updateParameters(invalidParams).get(5, TimeUnit.SECONDS);

            // Assert — validation failed
            assertThat(result.isError()).isTrue();
            assertThat(result.ok()).isFalse();

            // Assert — preview must never be published because validation short-circuited
            verify(publisher, never()).publishPreviewChanged(any(List.class));
        }

        @Test
        void givenNullParams_whenUpdateParameters_thenNullPointerExceptionThrown() {
            assertThatThrownBy(() -> service.updateParameters(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // =========================================================================
    // canExecute
    // =========================================================================

    @Nested
    class CanExecuteTests {

        @Test
        void givenModeConfigured_whenCanExecute_thenTrue() throws Exception {
            // Arrange — files loaded + mode selected transitions to MODE_CONFIGURED
            File fileA = new File("/tmp/can_a.txt");
            FileModel modelA = buildFileModel(fileA);
            stubExtractMetadata(List.of(modelA));
            service.addFiles(List.of(Path.of("/tmp/can_a.txt"))).get(5, TimeUnit.SECONDS);

            stubComputePreviewAnyMode(List.of(buildPreparedModel(modelA)));
            service.selectMode(TransformationMode.ADD_TEXT).get(5, TimeUnit.SECONDS);

            // Act + Assert
            assertThat(service.canExecute()).isTrue();
        }

        @Test
        void givenEmptySession_whenCanExecute_thenFalse() {
            // Empty session is in EMPTY status — cannot execute
            assertThat(service.canExecute()).isFalse();
        }

        @Test
        void givenFilesLoadedButNoMode_whenCanExecute_thenFalse() throws Exception {
            // Arrange — files present but no mode selected → FILES_LOADED status
            File fileA = new File("/tmp/nomode_a.txt");
            stubExtractMetadata(List.of(buildFileModel(fileA)));
            service.addFiles(List.of(Path.of("/tmp/nomode_a.txt"))).get(5, TimeUnit.SECONDS);

            // Act + Assert
            assertThat(service.canExecute()).isFalse();
        }
    }

    // =========================================================================
    // snapshot
    // =========================================================================

    @Nested
    class SnapshotTests {

        @Test
        void givenEmptySession_whenSnapshot_thenStatusIsEmpty() {
            // Act
            SessionSnapshot snapshot = service.snapshot();

            // Assert
            assertThat(snapshot.status()).isEqualTo(SessionStatus.EMPTY);
            assertThat(snapshot.files()).isEmpty();
            assertThat(snapshot.preview()).isEmpty();
            assertThat(snapshot.activeMode()).isNull();
            assertThat(snapshot.currentParameters()).isNull();
        }

        @Test
        void givenFilesAdded_whenSnapshot_thenFilesArePresent() throws Exception {
            // Arrange
            File fileA = new File("/tmp/snap_a.txt");
            File fileB = new File("/tmp/snap_b.txt");
            FileModel modelA = buildFileModel(fileA);
            FileModel modelB = buildFileModel(fileB);
            stubExtractMetadata(List.of(modelA, modelB));

            // Act
            service.addFiles(
                    List.of(Path.of("/tmp/snap_a.txt"), Path.of("/tmp/snap_b.txt"))
            ).get(5, TimeUnit.SECONDS);

            // Assert — snapshot must reflect the 2 loaded files
            SessionSnapshot snapshot = service.snapshot();
            assertThat(snapshot.files()).hasSize(2);
            assertThat(snapshot.status()).isEqualTo(SessionStatus.FILES_LOADED);
        }

        @Test
        void givenModeSelected_whenSnapshot_thenActiveModeIsSet() throws Exception {
            // Arrange
            File fileA = new File("/tmp/snap_mode_a.txt");
            FileModel modelA = buildFileModel(fileA);
            stubExtractMetadata(List.of(modelA));
            service.addFiles(List.of(Path.of("/tmp/snap_mode_a.txt"))).get(5, TimeUnit.SECONDS);

            stubComputePreviewAnyMode(List.of(buildPreparedModel(modelA)));
            service.selectMode(TransformationMode.CHANGE_CASE).get(5, TimeUnit.SECONDS);

            // Act + Assert
            SessionSnapshot snapshot = service.snapshot();
            assertThat(snapshot.activeMode()).isEqualTo(TransformationMode.CHANGE_CASE);
            assertThat(snapshot.status()).isEqualTo(SessionStatus.MODE_CONFIGURED);
        }
    }

    // =========================================================================
    // removeFiles
    // =========================================================================

    @Nested
    class RemoveFilesTests {

        @Test
        void givenExecuting_whenRemoveFiles_thenCommandResultIsFailure() throws Exception {
            // Arrange — reach MODE_CONFIGURED so canExecute() == true
            File fileA = new File("/tmp/rem_a.txt");
            FileModel modelA = buildFileModel(fileA);
            stubExtractMetadata(List.of(modelA));
            service.addFiles(List.of(Path.of("/tmp/rem_a.txt"))).get(5, TimeUnit.SECONDS);

            stubComputePreviewAnyMode(List.of(buildPreparedModel(modelA)));
            service.selectMode(TransformationMode.ADD_TEXT).get(5, TimeUnit.SECONDS);

            // Arrange — make orchestrator.execute() block until removeFiles completes
            CountDownLatch executeStarted = new CountDownLatch(1);
            CountDownLatch removeDone = new CountDownLatch(1);

            when(orchestrator.execute(any(List.class), any(), any(), any()))
                    .thenAnswer(invocation -> {
                        // Signal that the pipeline has entered the execute() work phase
                        executeStarted.countDown();
                        // Block until the test's removeFiles call has been dispatched
                        removeDone.await(5, TimeUnit.SECONDS);
                        return List.of();
                    });

            // Act — fire execute() without blocking; the state thread will set EXECUTING first,
            // then the virtual pool will call orchestrator.execute() (which blocks here)
            service.execute();

            // Wait until orchestrator.execute() is reached (EXECUTING state is already set
            // because submitStateChange precedes submitWork in the pipeline)
            assertThat(executeStarted.await(5, TimeUnit.SECONDS))
                    .as("Execute must reach orchestrator.execute() within 5 s")
                    .isTrue();

            // Act — attempt removeFiles while session is EXECUTING
            CommandResult removeResult = service.removeFiles(
                    List.of(fileA.getAbsolutePath())
            ).get(5, TimeUnit.SECONDS);

            // Release the blocked execute() call
            removeDone.countDown();

            // Assert — removeFiles must fail because session is EXECUTING
            assertThat(removeResult.success()).isFalse();
            assertThat(removeResult.errorMessage()).contains("Cannot remove files during execution");
        }

        @Test
        void givenNullFileIds_whenRemoveFiles_thenNullPointerExceptionThrown() {
            assertThatThrownBy(() -> service.removeFiles(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void givenFilesLoaded_whenRemoveFiles_thenCommandSucceeds() throws Exception {
            // Arrange
            File fileA = new File("/tmp/rem_ok_a.txt");
            FileModel modelA = buildFileModel(fileA);
            stubExtractMetadata(List.of(modelA));
            service.addFiles(List.of(Path.of("/tmp/rem_ok_a.txt"))).get(5, TimeUnit.SECONDS);

            // Act
            CommandResult result = service.removeFiles(
                    List.of(fileA.getAbsolutePath())
            ).get(5, TimeUnit.SECONDS);

            // Assert
            assertThat(result.success()).isTrue();

            // Assert — snapshot reflects empty file list
            assertThat(service.snapshot().files()).isEmpty();
        }
    }

    // =========================================================================
    // clearFiles
    // =========================================================================

    @Nested
    class ClearFilesTests {

        @Test
        void givenFilesLoaded_whenClearFiles_thenSessionIsEmpty() throws Exception {
            // Arrange
            File fileA = new File("/tmp/clr_a.txt");
            stubExtractMetadata(List.of(buildFileModel(fileA)));
            service.addFiles(List.of(Path.of("/tmp/clr_a.txt"))).get(5, TimeUnit.SECONDS);

            // Act
            CommandResult result = service.clearFiles().get(5, TimeUnit.SECONDS);

            // Assert
            assertThat(result.success()).isTrue();
            assertThat(service.snapshot().status()).isEqualTo(SessionStatus.EMPTY);
            assertThat(service.snapshot().files()).isEmpty();
        }

        @Test
        void givenFilesLoaded_whenClearFiles_thenPublisherReceivesEmptyFilesChanged()
                throws Exception {
            // Arrange
            File fileA = new File("/tmp/clr_pub_a.txt");
            stubExtractMetadata(List.of(buildFileModel(fileA)));
            service.addFiles(List.of(Path.of("/tmp/clr_pub_a.txt"))).get(5, TimeUnit.SECONDS);

            // Act
            service.clearFiles().get(5, TimeUnit.SECONDS);

            // Assert — the last publishFilesChanged call must carry empty lists
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<RenameCandidate>> candidatesCaptor =
                    ArgumentCaptor.forClass(List.class);
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<RenamePreview>> previewCaptor =
                    ArgumentCaptor.forClass(List.class);

            // publishFilesChanged is called twice: once by addFiles, once by clearFiles.
            // We verify the last invocation carries empty lists.
            verify(publisher, atLeast(2)).publishFilesChanged(
                    candidatesCaptor.capture(), previewCaptor.capture());

            List<List<RenameCandidate>> allCandidates = candidatesCaptor.getAllValues();
            List<List<RenamePreview>> allPreviews = previewCaptor.getAllValues();

            // The last call (index -1) is from clearFiles
            assertThat(allCandidates.get(allCandidates.size() - 1)).isEmpty();
            assertThat(allPreviews.get(allPreviews.size() - 1)).isEmpty();
        }
    }

    // =========================================================================
    // availableActions
    // =========================================================================

    @Nested
    class AvailableActionsTests {

        @Test
        void givenEmptySession_whenAvailableActions_thenOnlyAddFilesAllowed() {
            assertThat(service.availableActions())
                    .containsExactly(AvailableAction.ADD_FILES);
        }

        @Test
        void givenFilesLoaded_whenAvailableActions_thenSelectModeAllowed() throws Exception {
            // Arrange
            File fileA = new File("/tmp/avail_a.txt");
            stubExtractMetadata(List.of(buildFileModel(fileA)));
            service.addFiles(List.of(Path.of("/tmp/avail_a.txt"))).get(5, TimeUnit.SECONDS);

            // Act + Assert
            assertThat(service.availableActions())
                    .contains(AvailableAction.SELECT_MODE);
        }

        @Test
        void givenModeConfigured_whenAvailableActions_thenExecuteAllowed() throws Exception {
            // Arrange
            File fileA = new File("/tmp/avail_mode_a.txt");
            FileModel modelA = buildFileModel(fileA);
            stubExtractMetadata(List.of(modelA));
            service.addFiles(List.of(Path.of("/tmp/avail_mode_a.txt"))).get(5, TimeUnit.SECONDS);

            stubComputePreviewAnyMode(List.of(buildPreparedModel(modelA)));
            service.selectMode(TransformationMode.ADD_TEXT).get(5, TimeUnit.SECONDS);

            // Act + Assert
            assertThat(service.availableActions())
                    .contains(AvailableAction.EXECUTE);
        }
    }

    // =========================================================================
    // No-throw contract
    // =========================================================================

    @Nested
    class NoThrowContractTests {

        @Test
        void givenAnyState_whenSnapshot_thenNeverThrows() {
            assertThatCode(() -> service.snapshot())
                    .doesNotThrowAnyException();
        }

        @Test
        void givenAnyState_whenCanExecute_thenNeverThrows() {
            assertThatCode(() -> service.canExecute())
                    .doesNotThrowAnyException();
        }

        @Test
        void givenAnyState_whenAvailableActions_thenNeverThrows() {
            assertThatCode(() -> service.availableActions())
                    .doesNotThrowAnyException();
        }
    }
}
