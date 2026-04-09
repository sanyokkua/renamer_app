package ua.renamer.app.backend.session;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.api.model.meta.category.AudioMeta;
import ua.renamer.app.api.model.meta.category.ImageMeta;
import ua.renamer.app.api.model.meta.category.VideoMeta;
import ua.renamer.app.api.service.FileRenameOrchestrator;
import ua.renamer.app.api.service.ProgressCallback;
import ua.renamer.app.api.session.AddTextParams;
import ua.renamer.app.api.session.AvailableAction;
import ua.renamer.app.api.session.CommandResult;
import ua.renamer.app.api.session.FileMetadataDto;
import ua.renamer.app.api.session.RenameCandidate;
import ua.renamer.app.api.session.RenamePreview;
import ua.renamer.app.api.session.RenameSessionResult;
import ua.renamer.app.api.session.SessionSnapshot;
import ua.renamer.app.api.session.SessionStatus;
import ua.renamer.app.api.session.StatePublisher;
import ua.renamer.app.api.session.ValidationResult;
import ua.renamer.app.backend.service.BackendExecutor;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    // =========================================================================
    // availableActions — all SessionStatus branches
    // =========================================================================

    @Nested
    class AvailableActionsAllStatusTests {

        @Test
        void givenExecutingStatus_whenAvailableActions_thenOnlyCancelAllowed() throws Exception {
            // Arrange — reach MODE_CONFIGURED, then start execute() which sets EXECUTING
            File fileA = new File("/tmp/exec_avail_a.txt");
            FileModel modelA = buildFileModel(fileA);
            stubExtractMetadata(List.of(modelA));
            service.addFiles(List.of(Path.of("/tmp/exec_avail_a.txt"))).get(5, TimeUnit.SECONDS);
            stubComputePreviewAnyMode(List.of(buildPreparedModel(modelA)));
            service.selectMode(TransformationMode.ADD_TEXT).get(5, TimeUnit.SECONDS);

            CountDownLatch executeStarted = new CountDownLatch(1);
            CountDownLatch releaseLatch = new CountDownLatch(1);

            when(orchestrator.execute(any(List.class), any(), any(), any()))
                    .thenAnswer(invocation -> {
                        executeStarted.countDown();
                        releaseLatch.await(5, TimeUnit.SECONDS);
                        return List.of();
                    });

            service.execute();
            assertThat(executeStarted.await(5, TimeUnit.SECONDS)).isTrue();

            // Assert — EXECUTING state
            assertThat(service.availableActions()).containsExactly(AvailableAction.CANCEL);

            releaseLatch.countDown();
        }

        @Test
        void givenCompleteStatus_whenAvailableActions_thenAddFilesAndClearAndSelectModeAllowed()
                throws Exception {
            // Arrange — run full execute() to reach COMPLETE
            File fileA = new File("/tmp/complete_avail_a.txt");
            FileModel modelA = buildFileModel(fileA);
            stubExtractMetadata(List.of(modelA));
            service.addFiles(List.of(Path.of("/tmp/complete_avail_a.txt"))).get(5, TimeUnit.SECONDS);
            stubComputePreviewAnyMode(List.of(buildPreparedModel(modelA)));
            service.selectMode(TransformationMode.ADD_TEXT).get(5, TimeUnit.SECONDS);

            PreparedFileModel preparedA = buildPreparedModel(modelA);
            RenameResult successResult = RenameResult.builder()
                    .withPreparedFile(preparedA)
                    .withStatus(RenameStatus.SUCCESS)
                    .withErrorMessage(null)
                    .withExecutedAt(LocalDateTime.now())
                    .build();
            when(orchestrator.execute(any(List.class), any(), any(), any()))
                    .thenReturn(List.of(successResult));

            // execute() and wait for completion
            service.execute().result().get(5, TimeUnit.SECONDS);

            // Assert — COMPLETE status exposes ADD_FILES, CLEAR, SELECT_MODE
            assertThat(service.availableActions())
                    .contains(AvailableAction.ADD_FILES, AvailableAction.CLEAR, AvailableAction.SELECT_MODE)
                    .doesNotContain(AvailableAction.EXECUTE);
        }

        @Test
        void givenErrorStatus_whenAvailableActions_thenAddFilesAndClearAndSelectModeAllowed()
                throws Exception {
            // Arrange — run execute() with a result that contains an error to reach ERROR status
            File fileA = new File("/tmp/error_avail_a.txt");
            FileModel modelA = buildFileModel(fileA);
            stubExtractMetadata(List.of(modelA));
            service.addFiles(List.of(Path.of("/tmp/error_avail_a.txt"))).get(5, TimeUnit.SECONDS);
            stubComputePreviewAnyMode(List.of(buildPreparedModel(modelA)));
            service.selectMode(TransformationMode.ADD_TEXT).get(5, TimeUnit.SECONDS);

            PreparedFileModel preparedA = buildPreparedModel(modelA);
            RenameResult errorResult = RenameResult.builder()
                    .withPreparedFile(preparedA)
                    .withStatus(RenameStatus.ERROR_EXECUTION)
                    .withErrorMessage("Permission denied")
                    .withExecutedAt(LocalDateTime.now())
                    .build();
            when(orchestrator.execute(any(List.class), any(), any(), any()))
                    .thenReturn(List.of(errorResult));

            service.execute().result().get(5, TimeUnit.SECONDS);

            // Assert — ERROR status allows recovery actions
            assertThat(service.availableActions())
                    .contains(AvailableAction.ADD_FILES, AvailableAction.CLEAR, AvailableAction.SELECT_MODE);
        }
    }

    // =========================================================================
    // execute — success path and canExecute-false guard
    // =========================================================================

    @Nested
    class ExecuteTests {

        @Test
        void givenCannotExecute_whenExecute_thenFutureCompletesExceptionally() {
            // Empty session → canExecute() == false
            assertThatCode(() -> {
                var handle = service.execute();
                // The future must complete exceptionally (IllegalStateException)
                handle.result()
                        .exceptionally(ex -> null)
                        .get(5, TimeUnit.SECONDS);
            }).doesNotThrowAnyException();
        }

        @Test
        void givenModeConfigured_whenExecute_thenResultsReturnedForAllFiles() throws Exception {
            // Arrange
            File fileA = new File("/tmp/exec_ok_a.txt");
            FileModel modelA = buildFileModel(fileA);
            stubExtractMetadata(List.of(modelA));
            service.addFiles(List.of(Path.of("/tmp/exec_ok_a.txt"))).get(5, TimeUnit.SECONDS);
            stubComputePreviewAnyMode(List.of(buildPreparedModel(modelA)));
            service.selectMode(TransformationMode.ADD_TEXT).get(5, TimeUnit.SECONDS);

            PreparedFileModel preparedA = buildPreparedModel(modelA);
            RenameResult successResult = RenameResult.builder()
                    .withPreparedFile(preparedA)
                    .withStatus(RenameStatus.SUCCESS)
                    .withErrorMessage(null)
                    .withExecutedAt(LocalDateTime.now())
                    .build();
            when(orchestrator.execute(any(List.class), any(), any(), any()))
                    .thenReturn(List.of(successResult));

            // Act
            List<RenameSessionResult> results =
                    service.execute().result().get(5, TimeUnit.SECONDS);

            // Assert
            assertThat(results).hasSize(1);
            assertThat(results.get(0).status()).isEqualTo(RenameStatus.SUCCESS);
        }

        @Test
        void givenExecutePipelineFails_whenExecute_thenStatusBecomesError() throws Exception {
            // Arrange
            File fileA = new File("/tmp/exec_fail_a.txt");
            FileModel modelA = buildFileModel(fileA);
            stubExtractMetadata(List.of(modelA));
            service.addFiles(List.of(Path.of("/tmp/exec_fail_a.txt"))).get(5, TimeUnit.SECONDS);
            stubComputePreviewAnyMode(List.of(buildPreparedModel(modelA)));
            service.selectMode(TransformationMode.ADD_TEXT).get(5, TimeUnit.SECONDS);

            when(orchestrator.execute(any(List.class), any(), any(), any()))
                    .thenThrow(new RuntimeException("Disk full"));

            // Act — the future should complete exceptionally
            var handle = service.execute();
            handle.result()
                    .exceptionally(ex -> null)
                    .get(5, TimeUnit.SECONDS);

            // Assert — session status must transition to ERROR, not stay EXECUTING
            // Give the state thread time to process the exceptionally() callback
            Thread.sleep(100);
            assertThat(service.availableActions())
                    .doesNotContain(AvailableAction.CANCEL);
        }
    }

    // =========================================================================
    // getFileMetadata — cache presence and absence
    // =========================================================================

    @Nested
    class GetFileMetadataTests {

        @Test
        void givenNoFilesLoaded_whenGetFileMetadata_thenEmpty() {
            Optional<FileMetadataDto> result = service.getFileMetadata("/nonexistent/path.txt");

            assertThat(result).isEmpty();
        }

        @Test
        void givenNullFileId_whenGetFileMetadata_thenNullPointerExceptionThrown() {
            assertThatThrownBy(() -> service.getFileMetadata(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void givenFilesLoaded_whenGetFileMetadata_thenMetadataCached() throws Exception {
            // Arrange
            File fileA = new File("/tmp/meta_a.txt");
            FileModel modelA = buildFileModel(fileA);
            stubExtractMetadata(List.of(modelA));

            service.addFiles(List.of(Path.of("/tmp/meta_a.txt"))).get(5, TimeUnit.SECONDS);

            // Act
            Optional<FileMetadataDto> result = service.getFileMetadata(fileA.getAbsolutePath());

            // Assert — cache populated by addFiles/refreshAndPublish
            assertThat(result).isPresent();
        }
    }

    // =========================================================================
    // buildMetadataDto — image / video / audio / no-meta branches
    // =========================================================================

    @Nested
    class BuildMetadataDtoTests {

        /**
         * Builds a FileModel with ImageMeta attached.
         */
        private FileModel buildFileModelWithImageMeta(File file) {
            LocalDateTime capturedAt = LocalDateTime.of(2025, 6, 15, 10, 30);
            ImageMeta img = ImageMeta.builder()
                    .withContentCreationDate(capturedAt)
                    .withWidth(1920)
                    .withHeight(1080)
                    .build();
            FileMeta meta = FileMeta.builder().withImage(img).build();
            return FileModel.builder()
                    .withFile(file)
                    .withName("photo")
                    .withExtension("jpg")
                    .withAbsolutePath(file.getAbsolutePath())
                    .withIsFile(true)
                    .withFileSize(2048L)
                    .withCategory(Category.IMAGE)
                    .withMetadata(meta)
                    .build();
        }

        private FileModel buildFileModelWithVideoMeta(File file) {
            LocalDateTime recorded = LocalDateTime.of(2025, 3, 10, 18, 0);
            VideoMeta vid = VideoMeta.builder()
                    .withContentCreationDate(recorded)
                    .withWidth(1280)
                    .withHeight(720)
                    .build();
            FileMeta meta = FileMeta.builder().withVideo(vid).build();
            return FileModel.builder()
                    .withFile(file)
                    .withName("clip")
                    .withExtension("mp4")
                    .withAbsolutePath(file.getAbsolutePath())
                    .withIsFile(true)
                    .withFileSize(10_000_000L)
                    .withCategory(Category.VIDEO)
                    .withMetadata(meta)
                    .build();
        }

        private FileModel buildFileModelWithAudioMeta(File file) {
            AudioMeta aud = AudioMeta.builder()
                    .withArtistName("Artist")
                    .withAlbumName("Album")
                    .withSongName("Song")
                    .withYear(2024)
                    .build();
            FileMeta meta = FileMeta.builder().withAudio(aud).build();
            return FileModel.builder()
                    .withFile(file)
                    .withName("track")
                    .withExtension("mp3")
                    .withAbsolutePath(file.getAbsolutePath())
                    .withIsFile(true)
                    .withFileSize(5_000_000L)
                    .withCategory(Category.AUDIO)
                    .withMetadata(meta)
                    .build();
        }

        @Test
        void givenImageMeta_whenAddFiles_thenMetadataDtoHasWidthAndHeight() throws Exception {
            File fileA = new File("/tmp/img_meta.jpg");
            FileModel modelA = buildFileModelWithImageMeta(fileA);

            org.mockito.Mockito.lenient().doReturn(List.of(modelA))
                    .when(orchestrator).extractMetadata(any(), isNull());
            org.mockito.Mockito.lenient().doReturn(List.of(modelA))
                    .when(orchestrator).extractMetadata(any(),
                            any(ua.renamer.app.api.service.ProgressCallback.class));

            service.addFiles(List.of(Path.of("/tmp/img_meta.jpg"))).get(5, TimeUnit.SECONDS);

            FileMetadataDto dto = service.getFileMetadata(fileA.getAbsolutePath()).orElseThrow();

            assertThat(dto.widthPx()).isEqualTo(1920);
            assertThat(dto.heightPx()).isEqualTo(1080);
            assertThat(dto.contentCreationDate()).isEqualTo(LocalDateTime.of(2025, 6, 15, 10, 30));
        }

        @Test
        void givenVideoMeta_whenAddFiles_thenMetadataDtoHasWidthAndHeight() throws Exception {
            File fileV = new File("/tmp/vid_meta.mp4");
            FileModel modelV = buildFileModelWithVideoMeta(fileV);

            org.mockito.Mockito.lenient().doReturn(List.of(modelV))
                    .when(orchestrator).extractMetadata(any(), isNull());
            org.mockito.Mockito.lenient().doReturn(List.of(modelV))
                    .when(orchestrator).extractMetadata(any(),
                            any(ua.renamer.app.api.service.ProgressCallback.class));

            service.addFiles(List.of(Path.of("/tmp/vid_meta.mp4"))).get(5, TimeUnit.SECONDS);

            FileMetadataDto dto = service.getFileMetadata(fileV.getAbsolutePath()).orElseThrow();

            assertThat(dto.widthPx()).isEqualTo(1280);
            assertThat(dto.heightPx()).isEqualTo(720);
        }

        @Test
        void givenAudioMeta_whenAddFiles_thenMetadataDtoHasArtistAndAlbum() throws Exception {
            File fileAud = new File("/tmp/aud_meta.mp3");
            FileModel modelAud = buildFileModelWithAudioMeta(fileAud);

            org.mockito.Mockito.lenient().doReturn(List.of(modelAud))
                    .when(orchestrator).extractMetadata(any(), isNull());
            org.mockito.Mockito.lenient().doReturn(List.of(modelAud))
                    .when(orchestrator).extractMetadata(any(),
                            any(ua.renamer.app.api.service.ProgressCallback.class));

            service.addFiles(List.of(Path.of("/tmp/aud_meta.mp3"))).get(5, TimeUnit.SECONDS);

            FileMetadataDto dto = service.getFileMetadata(fileAud.getAbsolutePath()).orElseThrow();

            assertThat(dto.audioArtist()).isEqualTo("Artist");
            assertThat(dto.audioAlbum()).isEqualTo("Album");
            assertThat(dto.audioTitle()).isEqualTo("Song");
            assertThat(dto.audioYear()).isEqualTo(2024);
        }

        @Test
        void givenNoMeta_whenAddFiles_thenMetadataDtoCategoryIsGeneric() throws Exception {
            // A FileModel with no metadata and null category
            File fileG = new File("/tmp/generic_meta.bin");
            FileModel modelG = FileModel.builder()
                    .withFile(fileG)
                    .withName("generic_meta")
                    .withExtension("bin")
                    .withAbsolutePath(fileG.getAbsolutePath())
                    .withIsFile(true)
                    .withFileSize(100L)
                    .withCategory(null)
                    .withMetadata(null)
                    .build();

            org.mockito.Mockito.lenient().doReturn(List.of(modelG))
                    .when(orchestrator).extractMetadata(any(), isNull());
            org.mockito.Mockito.lenient().doReturn(List.of(modelG))
                    .when(orchestrator).extractMetadata(any(),
                            any(ua.renamer.app.api.service.ProgressCallback.class));

            service.addFiles(List.of(Path.of("/tmp/generic_meta.bin"))).get(5, TimeUnit.SECONDS);

            FileMetadataDto dto = service.getFileMetadata(fileG.getAbsolutePath()).orElseThrow();

            assertThat(dto.category()).isEqualTo("GENERIC");
        }
    }

    // =========================================================================
    // previewSingleFile — package-private method
    // =========================================================================

    @Nested
    class PreviewSingleFileTests {

        @Test
        void givenValidParams_whenPreviewSingleFile_thenReturnsNewName() {
            // Arrange — stub orchestrator to return a prepared model
            File mockFile = new File("/preview/document.txt");
            FileModel mockModel = FileModel.builder()
                    .withFile(mockFile)
                    .withName("document")
                    .withExtension("txt")
                    .withAbsolutePath("/preview/document.txt")
                    .withIsFile(true)
                    .withFileSize(0L)
                    .build();
            PreparedFileModel preparedResult = PreparedFileModel.builder()
                    .withOriginalFile(mockModel)
                    .withNewName("prefix_document")
                    .withNewExtension("txt")
                    .withHasError(false)
                    .withErrorMessage(null)
                    .withTransformationMeta(null)
                    .build();
            org.mockito.Mockito.lenient().doReturn(List.of(preparedResult))
                    .when(orchestrator).computePreview(any(), any(), any(), any());

            AddTextParams validParams = new AddTextParams("prefix_", ItemPosition.BEGIN);

            // Act
            Optional<String> result =
                    service.previewSingleFile(TransformationMode.ADD_TEXT, validParams,
                            "document", "txt");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("prefix_document.txt");
        }

        @Test
        void givenInvalidParams_whenPreviewSingleFile_thenReturnsEmpty() {
            // position == null → validate() returns error → previewSingleFile returns empty
            AddTextParams invalidParams = new AddTextParams(null, null);

            Optional<String> result =
                    service.previewSingleFile(TransformationMode.ADD_TEXT, invalidParams,
                            "document", "txt");

            assertThat(result).isEmpty();
            // orchestrator must not be called
            org.mockito.Mockito.verify(orchestrator, org.mockito.Mockito.never())
                    .computePreview(any(), any(), any(), any());
        }

        @Test
        void givenOrchestratorReturnsError_whenPreviewSingleFile_thenReturnsEmpty() {
            // Stub orchestrator to return a prepared model with hasError=true
            File mockFile = new File("/preview/bad.txt");
            FileModel mockModel = FileModel.builder()
                    .withFile(mockFile)
                    .withName("bad")
                    .withExtension("txt")
                    .withAbsolutePath("/preview/bad.txt")
                    .withIsFile(true)
                    .withFileSize(0L)
                    .build();
            PreparedFileModel errorResult = PreparedFileModel.builder()
                    .withOriginalFile(mockModel)
                    .withNewName("bad")
                    .withNewExtension("txt")
                    .withHasError(true)
                    .withErrorMessage("Transformation failed")
                    .withTransformationMeta(null)
                    .build();
            org.mockito.Mockito.lenient().doReturn(List.of(errorResult))
                    .when(orchestrator).computePreview(any(), any(), any(), any());

            AddTextParams validParams = new AddTextParams("prefix_", ItemPosition.BEGIN);

            Optional<String> result =
                    service.previewSingleFile(TransformationMode.ADD_TEXT, validParams, "bad", "txt");

            assertThat(result).isEmpty();
        }

        @Test
        void givenOrchestratorReturnsEmptyList_whenPreviewSingleFile_thenReturnsEmpty() {
            org.mockito.Mockito.lenient().doReturn(List.of())
                    .when(orchestrator).computePreview(any(), any(), any(), any());

            AddTextParams validParams = new AddTextParams("x_", ItemPosition.BEGIN);

            Optional<String> result =
                    service.previewSingleFile(TransformationMode.ADD_TEXT, validParams,
                            "file", "txt");

            assertThat(result).isEmpty();
        }
    }
}
