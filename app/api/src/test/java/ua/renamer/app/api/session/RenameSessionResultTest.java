package ua.renamer.app.api.session;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.model.RenameStatus;

import static org.junit.jupiter.api.Assertions.*;

class RenameSessionResultTest {

    private static final String FILE_ID = "sess-42";
    private static final String ORIGINAL_NAME = "report.docx";
    private static final String FINAL_NAME = "report_2024.docx";

    // --- happy path: SUCCESS ---

    @Test
    void fileId_whenSuccess_thenReturnsExpectedValue() {
        RenameSessionResult result =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, FINAL_NAME, RenameStatus.SUCCESS, null);

        assertEquals(FILE_ID, result.fileId());
    }

    @Test
    void originalName_whenSuccess_thenReturnsExpectedValue() {
        RenameSessionResult result =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, FINAL_NAME, RenameStatus.SUCCESS, null);

        assertEquals(ORIGINAL_NAME, result.originalName());
    }

    @Test
    void finalName_whenSuccess_thenReturnsExpectedValue() {
        RenameSessionResult result =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, FINAL_NAME, RenameStatus.SUCCESS, null);

        assertEquals(FINAL_NAME, result.finalName());
    }

    @Test
    void status_whenSuccess_thenReturnsSuccess() {
        RenameSessionResult result =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, FINAL_NAME, RenameStatus.SUCCESS, null);

        assertEquals(RenameStatus.SUCCESS, result.status());
    }

    @Test
    void errorMessage_whenSuccess_thenReturnsNull() {
        RenameSessionResult result =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, FINAL_NAME, RenameStatus.SUCCESS, null);

        assertNull(result.errorMessage());
    }

    // --- SKIPPED: finalName equals originalName ---

    @Test
    void finalName_whenSkipped_thenEqualToOriginalName() {
        RenameSessionResult result =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, ORIGINAL_NAME, RenameStatus.SKIPPED, null);

        assertEquals(ORIGINAL_NAME, result.finalName());
    }

    @Test
    void status_whenSkipped_thenReturnsSkipped() {
        RenameSessionResult result =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, ORIGINAL_NAME, RenameStatus.SKIPPED, null);

        assertEquals(RenameStatus.SKIPPED, result.status());
    }

    // --- each error status carries a message ---

    @Test
    void status_andErrorMessage_whenErrorExtraction() {
        RenameSessionResult result =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, ORIGINAL_NAME,
                        RenameStatus.ERROR_EXTRACTION, "could not read metadata");

        assertEquals(RenameStatus.ERROR_EXTRACTION, result.status());
        assertEquals("could not read metadata", result.errorMessage());
    }

    @Test
    void status_andErrorMessage_whenErrorTransformation() {
        RenameSessionResult result =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, ORIGINAL_NAME,
                        RenameStatus.ERROR_TRANSFORMATION, "pattern produced empty name");

        assertEquals(RenameStatus.ERROR_TRANSFORMATION, result.status());
        assertEquals("pattern produced empty name", result.errorMessage());
    }

    @Test
    void status_andErrorMessage_whenErrorExecution() {
        RenameSessionResult result =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, ORIGINAL_NAME,
                        RenameStatus.ERROR_EXECUTION, "permission denied");

        assertEquals(RenameStatus.ERROR_EXECUTION, result.status());
        assertEquals("permission denied", result.errorMessage());
    }

    // --- RenameStatus enum: all 5 values are present ---

    @Test
    void renameStatus_values_thenExactlyFiveConstants() {
        assertEquals(5, RenameStatus.values().length);
    }

    @Test
    void renameStatus_allExpectedNamesArePresent() {
        assertAll(
                () -> assertEquals(RenameStatus.SUCCESS, RenameStatus.valueOf("SUCCESS")),
                () -> assertEquals(RenameStatus.SKIPPED, RenameStatus.valueOf("SKIPPED")),
                () -> assertEquals(RenameStatus.ERROR_EXTRACTION, RenameStatus.valueOf("ERROR_EXTRACTION")),
                () -> assertEquals(RenameStatus.ERROR_TRANSFORMATION, RenameStatus.valueOf("ERROR_TRANSFORMATION")),
                () -> assertEquals(RenameStatus.ERROR_EXECUTION, RenameStatus.valueOf("ERROR_EXECUTION"))
        );
    }

    // --- record equality ---

    @Test
    void equality_whenSameFields_thenRecordsAreEqual() {
        RenameSessionResult a =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, FINAL_NAME, RenameStatus.SUCCESS, null);
        RenameSessionResult b =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, FINAL_NAME, RenameStatus.SUCCESS, null);

        assertEquals(a, b);
    }

    @Test
    void equality_whenDifferentStatus_thenRecordsAreNotEqual() {
        RenameSessionResult a =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, ORIGINAL_NAME, RenameStatus.SUCCESS, null);
        RenameSessionResult b =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, ORIGINAL_NAME, RenameStatus.SKIPPED, null);

        assertNotEquals(a, b);
    }

    @Test
    void equality_whenDifferentFinalName_thenRecordsAreNotEqual() {
        RenameSessionResult a =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, "alpha.docx", RenameStatus.SUCCESS, null);
        RenameSessionResult b =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, "beta.docx", RenameStatus.SUCCESS, null);

        assertNotEquals(a, b);
    }

    // --- hashCode consistency ---

    @Test
    void hashCode_whenEqualRecords_thenSameHashCode() {
        RenameSessionResult a =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, FINAL_NAME, RenameStatus.SUCCESS, null);
        RenameSessionResult b =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, FINAL_NAME, RenameStatus.SUCCESS, null);

        assertEquals(a.hashCode(), b.hashCode());
    }

    // --- toString is non-null ---

    @Test
    void toString_whenCalled_thenNonNull() {
        RenameSessionResult result =
                new RenameSessionResult(FILE_ID, ORIGINAL_NAME, FINAL_NAME, RenameStatus.SUCCESS, null);

        assertNotNull(result.toString());
    }
}
