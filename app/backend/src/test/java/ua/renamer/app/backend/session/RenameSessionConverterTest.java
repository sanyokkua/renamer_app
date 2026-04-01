package ua.renamer.app.backend.session;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.session.RenameCandidate;
import ua.renamer.app.api.session.RenamePreview;
import ua.renamer.app.api.session.RenameSessionResult;

import java.io.File;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RenameSessionConverter}.
 * All objects are built using real builders — no mocks.
 */
class RenameSessionConverterTest {

    // -------------------------------------------------------------------------
    // Shared test fixtures
    // -------------------------------------------------------------------------

    private static FileModel buildFileModel(String absolutePath, String name, String extension) {
        return FileModel.builder()
                        .withFile(new File(absolutePath))
                        .withName(name)
                        .withExtension(extension)
                        .withAbsolutePath(absolutePath)
                        .withIsFile(true)
                        .withFileSize(2048L)
                        .build();
    }

    private static PreparedFileModel buildPreparedModel(FileModel original,
                                                         String newName,
                                                         String newExtension,
                                                         boolean hasError,
                                                         String errorMessage) {
        return PreparedFileModel.builder()
                                .withOriginalFile(original)
                                .withNewName(newName)
                                .withNewExtension(newExtension)
                                .withHasError(hasError)
                                .withErrorMessage(errorMessage)
                                .withTransformationMeta(null)
                                .build();
    }

    private static RenameResult buildRenameResult(PreparedFileModel prepared,
                                                   RenameStatus status,
                                                   String errorMessage) {
        return RenameResult.builder()
                           .withPreparedFile(prepared)
                           .withStatus(status)
                           .withErrorMessage(errorMessage)
                           .withExecutedAt(LocalDateTime.of(2026, 1, 15, 10, 0))
                           .build();
    }

    // =========================================================================
    // toCandidate
    // =========================================================================

    @Nested
    class ToCandidateTests {

        @Test
        void givenFileModel_whenToCandidate_thenFileIdIsAbsolutePath() {
            // Arrange
            FileModel model = buildFileModel("/home/user/photos/photo.jpg", "photo", "jpg");

            // Act
            RenameCandidate candidate = RenameSessionConverter.toCandidate(model);

            // Assert
            assertThat(candidate.fileId()).isEqualTo("/home/user/photos/photo.jpg");
        }

        @Test
        void givenFileModel_whenToCandidate_thenNameAndExtensionMapped() {
            // Arrange
            FileModel model = buildFileModel("/tmp/report.pdf", "report", "pdf");

            // Act
            RenameCandidate candidate = RenameSessionConverter.toCandidate(model);

            // Assert
            assertThat(candidate.name()).isEqualTo("report");
            assertThat(candidate.extension()).isEqualTo("pdf");
        }

        @Test
        void givenFileModel_whenToCandidate_thenPathIsFileToPath() {
            // Arrange
            File file = new File("/tmp/video.mp4");
            FileModel model = FileModel.builder()
                                       .withFile(file)
                                       .withName("video")
                                       .withExtension("mp4")
                                       .withAbsolutePath("/tmp/video.mp4")
                                       .withIsFile(true)
                                       .withFileSize(512L)
                                       .build();

            // Act
            RenameCandidate candidate = RenameSessionConverter.toCandidate(model);

            // Assert
            assertThat(candidate.path()).isEqualTo(file.toPath());
        }

        @Test
        void givenFileModelWithEmptyExtension_whenToCandidate_thenExtensionIsEmpty() {
            // Arrange
            FileModel model = buildFileModel("/tmp/Makefile", "Makefile", "");

            // Act
            RenameCandidate candidate = RenameSessionConverter.toCandidate(model);

            // Assert
            assertThat(candidate.extension()).isEmpty();
        }
    }

    // =========================================================================
    // toPreview
    // =========================================================================

    @Nested
    class ToPreviewTests {

        @Test
        void givenNoError_whenToPreview_thenNewNameIsPresent() {
            // Arrange
            FileModel original = buildFileModel("/tmp/doc.txt", "doc", "txt");
            PreparedFileModel prepared = buildPreparedModel(original, "doc_renamed", "txt", false, null);

            // Act
            RenamePreview preview = RenameSessionConverter.toPreview(prepared);

            // Assert
            // getNewFullName() returns "doc_renamed.txt"
            assertThat(preview.newName()).isEqualTo("doc_renamed.txt");
        }

        @Test
        void givenNoError_whenToPreview_thenFileIdMatchesToCandidate() {
            // Arrange
            FileModel original = buildFileModel("/data/music/song.mp3", "song", "mp3");
            PreparedFileModel prepared = buildPreparedModel(original, "song_v2", "mp3", false, null);

            // Act
            RenamePreview preview = RenameSessionConverter.toPreview(prepared);
            RenameCandidate candidate = RenameSessionConverter.toCandidate(original);

            // Assert
            assertThat(preview.fileId()).isEqualTo(candidate.fileId());
        }

        @Test
        void givenHasError_whenToPreview_thenNewNameIsNull() {
            // Arrange
            FileModel original = buildFileModel("/tmp/corrupt.bin", "corrupt", "bin");
            PreparedFileModel prepared = buildPreparedModel(original, "corrupt", "bin", true, "Transformation failed");

            // Act
            RenamePreview preview = RenameSessionConverter.toPreview(prepared);

            // Assert
            assertThat(preview.newName()).isNull();
        }

        @Test
        void givenHasError_whenToPreview_thenErrorMessagePresent() {
            // Arrange
            FileModel original = buildFileModel("/tmp/bad.dat", "bad", "dat");
            PreparedFileModel prepared = buildPreparedModel(original, "bad", "dat", true, "Unsupported format");

            // Act
            RenamePreview preview = RenameSessionConverter.toPreview(prepared);

            // Assert
            assertThat(preview.hasError()).isTrue();
            assertThat(preview.errorMessage()).isEqualTo("Unsupported format");
        }

        @Test
        void givenNoError_whenToPreview_thenErrorMessageIsNull() {
            // Arrange
            FileModel original = buildFileModel("/tmp/clean.jpg", "clean", "jpg");
            PreparedFileModel prepared = buildPreparedModel(original, "clean_v2", "jpg", false, null);

            // Act
            RenamePreview preview = RenameSessionConverter.toPreview(prepared);

            // Assert
            assertThat(preview.hasError()).isFalse();
            assertThat(preview.errorMessage()).isNull();
        }
    }

    // =========================================================================
    // toSessionResult
    // =========================================================================

    @Nested
    class ToSessionResultTests {

        @Test
        void givenSuccessResult_whenToSessionResult_thenFinalNameIsNewName() {
            // Arrange
            FileModel original = buildFileModel("/photos/img001.jpg", "img001", "jpg");
            PreparedFileModel prepared = buildPreparedModel(original, "2026-01-15_img001", "jpg", false, null);
            RenameResult result = buildRenameResult(prepared, RenameStatus.SUCCESS, null);

            // Act
            RenameSessionResult sessionResult = RenameSessionConverter.toSessionResult(result);

            // Assert — finalName must be the new full name "2026-01-15_img001.jpg"
            assertThat(sessionResult.finalName()).isEqualTo("2026-01-15_img001.jpg");
        }

        @Test
        void givenSkippedResult_whenToSessionResult_thenFinalNameIsOriginalName() {
            // Arrange
            FileModel original = buildFileModel("/photos/img002.jpg", "img002", "jpg");
            PreparedFileModel prepared = buildPreparedModel(original, "img002", "jpg", false, null);
            RenameResult result = buildRenameResult(prepared, RenameStatus.SKIPPED, null);

            // Act
            RenameSessionResult sessionResult = RenameSessionConverter.toSessionResult(result);

            // Assert — no rename happened; finalName = original "img002.jpg"
            assertThat(sessionResult.finalName()).isEqualTo("img002.jpg");
        }

        @Test
        void givenErrorExecutionResult_whenToSessionResult_thenFinalNameIsOriginalName() {
            // Arrange
            FileModel original = buildFileModel("/data/locked.doc", "locked", "doc");
            PreparedFileModel prepared = buildPreparedModel(original, "locked_new", "doc", false, null);
            RenameResult result = buildRenameResult(prepared, RenameStatus.ERROR_EXECUTION, "Permission denied");

            // Act
            RenameSessionResult sessionResult = RenameSessionConverter.toSessionResult(result);

            // Assert — execution failed; disk state unchanged; finalName = original "locked.doc"
            assertThat(sessionResult.finalName()).isEqualTo("locked.doc");
        }

        @Test
        void givenErrorTransformationResult_whenToSessionResult_thenErrorMessagePresent() {
            // Arrange
            FileModel original = buildFileModel("/data/img.png", "img", "png");
            PreparedFileModel prepared = buildPreparedModel(original, "img", "png", true, "Regex error");
            RenameResult result = buildRenameResult(prepared, RenameStatus.ERROR_TRANSFORMATION, "Regex error");

            // Act
            RenameSessionResult sessionResult = RenameSessionConverter.toSessionResult(result);

            // Assert
            assertThat(sessionResult.errorMessage()).isEqualTo("Regex error");
            assertThat(sessionResult.status()).isEqualTo(RenameStatus.ERROR_TRANSFORMATION);
        }

        @Test
        void givenResult_whenToSessionResult_thenFileIdMatchesToCandidate() {
            // Arrange
            FileModel original = buildFileModel("/archive/file.zip", "file", "zip");
            PreparedFileModel prepared = buildPreparedModel(original, "file_backup", "zip", false, null);
            RenameResult result = buildRenameResult(prepared, RenameStatus.SUCCESS, null);

            // Act
            RenameSessionResult sessionResult = RenameSessionConverter.toSessionResult(result);
            RenameCandidate candidate = RenameSessionConverter.toCandidate(original);

            // Assert — fileId must be the absolutePath, consistent across all converters
            assertThat(sessionResult.fileId()).isEqualTo(candidate.fileId());
        }
    }
}
