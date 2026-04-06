package ua.renamer.app.backend.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.AddTextParams;
import ua.renamer.app.api.session.RenamePreview;
import ua.renamer.app.api.session.SessionSnapshot;
import ua.renamer.app.api.session.SessionStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for {@link RenameSession}.
 * All state mutations are tested from a fresh session unless otherwise noted.
 */
class RenameSessionTest {

    private static final FileModel FILE_A = fileModel("/tmp/a.txt", "a", "txt");
    private static final FileModel FILE_B = fileModel("/tmp/b.txt", "b", "txt");

    // -------------------------------------------------------------------------
    // Shared fixture helpers
    // -------------------------------------------------------------------------
    private static final AddTextParams DEFAULT_PARAMS =
            new AddTextParams("prefix_", ItemPosition.BEGIN);
    private RenameSession session;

    private static FileModel fileModel(String absolutePath, String name, String extension) {
        return FileModel.builder()
                .withFile(new File(absolutePath))
                .withName(name)
                .withExtension(extension)
                .withAbsolutePath(absolutePath)
                .withIsFile(true)
                .withFileSize(1024L)
                .build();
    }

    private static PreparedFileModel preparedModel(FileModel original) {
        return PreparedFileModel.builder()
                .withOriginalFile(original)
                .withNewName(original.getName() + "_new")
                .withNewExtension(original.getExtension())
                .withHasError(false)
                .withErrorMessage(null)
                .withTransformationMeta(null)
                .build();
    }

    @BeforeEach
    void setUp() {
        session = new RenameSession();
    }

    // =========================================================================
    // Initial state
    // =========================================================================

    @Nested
    class InitialStateTests {

        @Test
        void givenNewSession_thenStatusIsEmpty() {
            assertThat(session.getStatus()).isEqualTo(SessionStatus.EMPTY);
        }

        @Test
        void givenNewSession_thenFilesIsEmpty() {
            assertThat(session.getFiles()).isEmpty();
        }

        @Test
        void givenNewSession_thenActiveModeIsNull() {
            assertThat(session.getActiveMode()).isNull();
        }

        @Test
        void givenNewSession_thenLastPreviewIsEmpty() {
            assertThat(session.getLastPreview()).isEmpty();
        }
    }

    // =========================================================================
    // addFiles
    // =========================================================================

    @Nested
    class AddFilesTests {

        @Test
        void givenEmptySession_whenAddFiles_thenStatusIsFilesLoaded() {
            // Act
            session.addFiles(List.of(FILE_A));

            // Assert
            assertThat(session.getStatus()).isEqualTo(SessionStatus.FILES_LOADED);
        }

        @Test
        void givenModeAlreadySet_whenAddFiles_thenStatusIsModeConfigured() {
            // Arrange — set mode first (no files yet, status stays EMPTY)
            session.setActiveMode(TransformationMode.ADD_TEXT, DEFAULT_PARAMS);

            // Act
            session.addFiles(List.of(FILE_A));

            // Assert
            assertThat(session.getStatus()).isEqualTo(SessionStatus.MODE_CONFIGURED);
        }

        @Test
        void givenAddFiles_thenLastPreviewIsCleared() {
            // Arrange — set a preview first
            session.addFiles(List.of(FILE_A));
            session.setLastPreview(List.of(preparedModel(FILE_A)));
            assertThat(session.getLastPreview()).hasSize(1);

            // Act
            session.addFiles(List.of(FILE_B));

            // Assert
            assertThat(session.getLastPreview()).isEmpty();
        }

        @Test
        void addFiles_withDuplicatePaths_doesNotAddDuplicates() {
            session.addFiles(List.of(FILE_A));
            session.addFiles(List.of(FILE_A));

            assertThat(session.getFiles()).hasSize(1);
        }

        @Test
        void addFiles_withDuplicateWithinBatch_addsOnlyOne() {
            session.addFiles(List.of(FILE_A, FILE_A));

            assertThat(session.getFiles()).hasSize(1);
        }

        @Test
        void addFiles_withOverlappingBatches_deduplicatesAcrossCalls() {
            // First batch: A + B, second batch: B + C → final size should be 3
            session.addFiles(List.of(FILE_A, FILE_B));
            FileModel fileC = fileModel("/tmp/c.txt", "c", "txt");
            session.addFiles(List.of(FILE_B, fileC));

            assertThat(session.getFiles()).hasSize(3);
        }

        @Test
        void addFiles_allDuplicates_doesNotChangeSizeButClearsPreview() {
            session.addFiles(List.of(FILE_A));
            session.setLastPreview(List.of(preparedModel(FILE_A)));
            assertThat(session.getLastPreview()).hasSize(1);

            session.addFiles(List.of(FILE_A));

            assertThat(session.getFiles()).hasSize(1);
            assertThat(session.getLastPreview()).isEmpty();
        }

        @Test
        void givenEmptyList_whenAddFiles_thenStatusIsFilesLoaded() {
            // The implementation always transitions to FILES_LOADED (or MODE_CONFIGURED)
            // even when an empty list is added. With no mode set and no files added,
            // the status becomes FILES_LOADED because the addFiles branch sets it unconditionally.

            // Act
            session.addFiles(List.of());

            // Assert — implementation transitions status regardless of whether any file was added
            assertThat(session.getStatus()).isEqualTo(SessionStatus.FILES_LOADED);
        }
    }

    // =========================================================================
    // removeFiles
    // =========================================================================

    @Nested
    class RemoveFilesTests {

        @Test
        void givenTwoFiles_whenRemoveOne_thenOneFileRemains() {
            // Arrange
            session.addFiles(List.of(FILE_A, FILE_B));

            // Act
            session.removeFiles(List.of(FILE_A.getAbsolutePath()));

            // Assert
            assertThat(session.getFiles()).hasSize(1);
            assertThat(session.getFiles().get(0).getAbsolutePath()).isEqualTo(FILE_B.getAbsolutePath());
        }

        @Test
        void givenTwoFiles_noMode_whenRemoveOne_thenStatusIsFilesLoaded() {
            // Arrange
            session.addFiles(List.of(FILE_A, FILE_B));

            // Act
            session.removeFiles(List.of(FILE_A.getAbsolutePath()));

            // Assert
            assertThat(session.getStatus()).isEqualTo(SessionStatus.FILES_LOADED);
        }

        @Test
        void givenOneFile_whenRemoveLast_thenStatusIsEmpty() {
            // Arrange
            session.addFiles(List.of(FILE_A));

            // Act
            session.removeFiles(List.of(FILE_A.getAbsolutePath()));

            // Assert
            assertThat(session.getStatus()).isEqualTo(SessionStatus.EMPTY);
        }

        @Test
        void givenOneFile_modeSet_whenRemoveLast_thenModePreserved() {
            // Arrange
            session.addFiles(List.of(FILE_A));
            session.setActiveMode(TransformationMode.ADD_TEXT, DEFAULT_PARAMS);

            // Act
            session.removeFiles(List.of(FILE_A.getAbsolutePath()));

            // Assert — mode and params must survive removeFiles even when list becomes empty
            assertThat(session.getActiveMode()).isEqualTo(TransformationMode.ADD_TEXT);
            assertThat(session.getCurrentParams()).isEqualTo(DEFAULT_PARAMS);
        }

        @Test
        void givenUnknownFileId_whenRemoveFiles_thenFilesUnchanged() {
            // Arrange
            session.addFiles(List.of(FILE_A));

            // Act
            session.removeFiles(List.of("/nonexistent/path.txt"));

            // Assert
            assertThat(session.getFiles()).hasSize(1);
        }

        @Test
        void givenRemoveFiles_thenLastPreviewIsCleared() {
            // Arrange
            session.addFiles(List.of(FILE_A, FILE_B));
            session.setLastPreview(List.of(preparedModel(FILE_A), preparedModel(FILE_B)));
            assertThat(session.getLastPreview()).hasSize(2);

            // Act
            session.removeFiles(List.of(FILE_A.getAbsolutePath()));

            // Assert
            assertThat(session.getLastPreview()).isEmpty();
        }
    }

    // =========================================================================
    // clearFiles
    // =========================================================================

    @Nested
    class ClearFilesTests {

        @Test
        void givenModeConfigured_whenClearFiles_thenStatusIsEmptyAndModeIsPreserved() {
            // Arrange
            session.addFiles(List.of(FILE_A));
            session.setActiveMode(TransformationMode.ADD_TEXT, DEFAULT_PARAMS);
            assertThat(session.getStatus()).isEqualTo(SessionStatus.MODE_CONFIGURED);

            // Act
            session.clearFiles();

            // Assert — mode and params survive clearFiles so the next addFiles auto-previews
            assertThat(session.getStatus()).isEqualTo(SessionStatus.EMPTY);
            assertThat(session.getActiveMode()).isEqualTo(TransformationMode.ADD_TEXT);
            assertThat(session.getCurrentParams()).isEqualTo(DEFAULT_PARAMS);
        }

        @Test
        void givenClearFiles_thenFilesIsEmpty() {
            // Arrange
            session.addFiles(List.of(FILE_A, FILE_B));

            // Act
            session.clearFiles();

            // Assert
            assertThat(session.getFiles()).isEmpty();
        }

        @Test
        void givenEmptySession_whenClearFiles_thenNoException() {
            // Act + Assert — clearing an already-empty session must not throw
            assertThatCode(() -> session.clearFiles()).doesNotThrowAnyException();
            assertThat(session.getStatus()).isEqualTo(SessionStatus.EMPTY);
        }
    }

    // =========================================================================
    // setActiveMode
    // =========================================================================

    @Nested
    class SetActiveModeTests {

        @Test
        void givenFilesPresent_whenSetActiveMode_thenStatusIsModeConfigured() {
            // Arrange
            session.addFiles(List.of(FILE_A));

            // Act
            session.setActiveMode(TransformationMode.ADD_TEXT, DEFAULT_PARAMS);

            // Assert
            assertThat(session.getStatus()).isEqualTo(SessionStatus.MODE_CONFIGURED);
        }

        @Test
        void givenNoFiles_whenSetActiveMode_thenStatusStaysEmpty() {
            // Act
            session.setActiveMode(TransformationMode.ADD_TEXT, DEFAULT_PARAMS);

            // Assert — files list is empty; status must not advance
            assertThat(session.getStatus()).isEqualTo(SessionStatus.EMPTY);
        }

        @Test
        void givenSetActiveMode_thenLastPreviewIsCleared() {
            // Arrange
            session.addFiles(List.of(FILE_A));
            session.setLastPreview(List.of(preparedModel(FILE_A)));
            assertThat(session.getLastPreview()).hasSize(1);

            // Act
            session.setActiveMode(TransformationMode.ADD_TEXT, DEFAULT_PARAMS);

            // Assert
            assertThat(session.getLastPreview()).isEmpty();
        }

        @Test
        void givenSetActiveMode_thenActiveModeAndParamsSet() {
            // Act
            session.setActiveMode(TransformationMode.ADD_TEXT, DEFAULT_PARAMS);

            // Assert
            assertThat(session.getActiveMode()).isEqualTo(TransformationMode.ADD_TEXT);
            assertThat(session.getCurrentParams()).isEqualTo(DEFAULT_PARAMS);
        }
    }

    // =========================================================================
    // setParameters
    // =========================================================================

    @Nested
    class SetParametersTests {

        @Test
        void givenModeConfigured_whenSetParameters_thenParamsUpdated() {
            // Arrange
            session.addFiles(List.of(FILE_A));
            session.setActiveMode(TransformationMode.ADD_TEXT, DEFAULT_PARAMS);
            AddTextParams updatedParams = new AddTextParams("suffix_", ItemPosition.END);

            // Act
            session.setParameters(updatedParams);

            // Assert
            assertThat(session.getCurrentParams()).isEqualTo(updatedParams);
        }

        @Test
        void givenSetParameters_thenLastPreviewIsCleared() {
            // Arrange
            session.addFiles(List.of(FILE_A));
            session.setActiveMode(TransformationMode.ADD_TEXT, DEFAULT_PARAMS);
            session.setLastPreview(List.of(preparedModel(FILE_A)));
            assertThat(session.getLastPreview()).hasSize(1);

            // Act
            session.setParameters(new AddTextParams("x_", ItemPosition.BEGIN));

            // Assert
            assertThat(session.getLastPreview()).isEmpty();
        }

        @Test
        void givenSetParameters_thenStatusUnchanged() {
            // Arrange
            session.addFiles(List.of(FILE_A));
            session.setActiveMode(TransformationMode.ADD_TEXT, DEFAULT_PARAMS);
            assertThat(session.getStatus()).isEqualTo(SessionStatus.MODE_CONFIGURED);

            // Act
            session.setParameters(new AddTextParams("y_", ItemPosition.END));

            // Assert — status must not change
            assertThat(session.getStatus()).isEqualTo(SessionStatus.MODE_CONFIGURED);
        }
    }

    // =========================================================================
    // setLastPreview
    // =========================================================================

    @Nested
    class SetLastPreviewTests {

        @Test
        void givenSetLastPreview_thenDefensiveCopy() {
            // Arrange
            session.addFiles(List.of(FILE_A));
            PreparedFileModel pf = preparedModel(FILE_A);
            List<PreparedFileModel> mutableList = new ArrayList<>();
            mutableList.add(pf);

            // Act
            session.setLastPreview(mutableList);
            // Mutate the source list after storing
            mutableList.clear();

            // Assert — session's lastPreview must still contain the original element
            assertThat(session.getLastPreview()).hasSize(1);
            assertThat(session.getLastPreview().get(0)).isEqualTo(pf);
        }
    }

    // =========================================================================
    // setStatus
    // =========================================================================

    @Nested
    class SetStatusTests {

        @Test
        void givenSetStatusExecuting_thenStatusIsExecuting() {
            // Arrange
            session.addFiles(List.of(FILE_A));
            session.setActiveMode(TransformationMode.ADD_TEXT, DEFAULT_PARAMS);

            // Act
            session.setStatus(SessionStatus.EXECUTING);

            // Assert
            assertThat(session.getStatus()).isEqualTo(SessionStatus.EXECUTING);
        }
    }

    // =========================================================================
    // toSnapshot
    // =========================================================================

    @Nested
    class ToSnapshotTests {

        @Test
        void givenFilesAndMode_whenToSnapshot_thenSnapshotHasConvertedCandidates() {
            // Arrange
            session.addFiles(List.of(FILE_A, FILE_B));
            session.setActiveMode(TransformationMode.ADD_TEXT, DEFAULT_PARAMS);

            // Act
            SessionSnapshot snapshot = session.toSnapshot(List.of());

            // Assert — both files converted to RenameCandidate
            assertThat(snapshot.files()).hasSize(2);
            assertThat(snapshot.files())
                    .extracting(c -> c.fileId())
                    .containsExactly(FILE_A.getAbsolutePath(), FILE_B.getAbsolutePath());
        }

        @Test
        void givenEmptyPreviewArg_whenToSnapshot_thenSnapshotPreviewIsEmpty() {
            // Arrange
            session.addFiles(List.of(FILE_A));

            // Act
            SessionSnapshot snapshot = session.toSnapshot(List.of());

            // Assert
            assertThat(snapshot.preview()).isEmpty();
        }

        @Test
        void givenToSnapshot_thenStatusMatchesSession() {
            // Arrange
            session.addFiles(List.of(FILE_A));
            session.setActiveMode(TransformationMode.ADD_TEXT, DEFAULT_PARAMS);

            // Act
            SessionSnapshot snapshot = session.toSnapshot(List.of());

            // Assert
            assertThat(snapshot.status()).isEqualTo(SessionStatus.MODE_CONFIGURED);
        }

        @Test
        void givenToSnapshot_withPreviewDtos_thenSnapshotContainsPreview() {
            // Arrange
            session.addFiles(List.of(FILE_A));
            RenamePreview previewDto = new RenamePreview(
                    FILE_A.getAbsolutePath(),
                    "a.txt",
                    "prefix_a.txt",
                    false,
                    null
            );

            // Act
            SessionSnapshot snapshot = session.toSnapshot(List.of(previewDto));

            // Assert
            assertThat(snapshot.preview()).hasSize(1);
            assertThat(snapshot.preview().get(0).fileId()).isEqualTo(FILE_A.getAbsolutePath());
        }
    }
}
