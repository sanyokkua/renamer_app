package ua.renamer.app.api.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RenamePreviewTest {

    private static final String FILE_ID       = "file-99";
    private static final String ORIGINAL_NAME = "old_photo.jpg";
    private static final String NEW_NAME      = "new_photo.jpg";

    // --- happy path: successful transformation preview ---

    @Test
    void fileId_whenNoError_thenReturnsExpectedValue() {
        RenamePreview preview = new RenamePreview(FILE_ID, ORIGINAL_NAME, NEW_NAME, false, null);

        assertEquals(FILE_ID, preview.fileId());
    }

    @Test
    void originalName_whenNoError_thenReturnsExpectedValue() {
        RenamePreview preview = new RenamePreview(FILE_ID, ORIGINAL_NAME, NEW_NAME, false, null);

        assertEquals(ORIGINAL_NAME, preview.originalName());
    }

    @Test
    void newName_whenNoError_thenReturnsExpectedValue() {
        RenamePreview preview = new RenamePreview(FILE_ID, ORIGINAL_NAME, NEW_NAME, false, null);

        assertEquals(NEW_NAME, preview.newName());
    }

    @Test
    void hasError_whenNoError_thenReturnsFalse() {
        RenamePreview preview = new RenamePreview(FILE_ID, ORIGINAL_NAME, NEW_NAME, false, null);

        assertFalse(preview.hasError());
    }

    @Test
    void errorMessage_whenNoError_thenReturnsNull() {
        RenamePreview preview = new RenamePreview(FILE_ID, ORIGINAL_NAME, NEW_NAME, false, null);

        assertNull(preview.errorMessage());
    }

    // --- error path: transformation failed ---

    @Test
    void hasError_whenErrorOccurred_thenReturnsTrue() {
        RenamePreview preview = new RenamePreview(FILE_ID, ORIGINAL_NAME, null, true, "extraction failed");

        assertTrue(preview.hasError());
    }

    @Test
    void newName_whenErrorOccurred_thenReturnsNull() {
        RenamePreview preview = new RenamePreview(FILE_ID, ORIGINAL_NAME, null, true, "extraction failed");

        assertNull(preview.newName());
    }

    @Test
    void errorMessage_whenErrorOccurred_thenReturnsExpectedMessage() {
        RenamePreview preview = new RenamePreview(FILE_ID, ORIGINAL_NAME, null, true, "extraction failed");

        assertEquals("extraction failed", preview.errorMessage());
    }

    // --- boundary: originalName equals newName (no-op transformation) ---

    @Test
    void newName_whenSameAsOriginal_thenReturnsOriginalName() {
        RenamePreview preview = new RenamePreview(FILE_ID, ORIGINAL_NAME, ORIGINAL_NAME, false, null);

        assertEquals(ORIGINAL_NAME, preview.newName());
    }

    // --- record equality ---

    @Test
    void equality_whenSameFields_thenRecordsAreEqual() {
        RenamePreview a = new RenamePreview(FILE_ID, ORIGINAL_NAME, NEW_NAME, false, null);
        RenamePreview b = new RenamePreview(FILE_ID, ORIGINAL_NAME, NEW_NAME, false, null);

        assertEquals(a, b);
    }

    @Test
    void equality_whenDifferentFileId_thenRecordsAreNotEqual() {
        RenamePreview a = new RenamePreview("id-A", ORIGINAL_NAME, NEW_NAME, false, null);
        RenamePreview b = new RenamePreview("id-B", ORIGINAL_NAME, NEW_NAME, false, null);

        assertNotEquals(a, b);
    }

    @Test
    void equality_whenDifferentHasError_thenRecordsAreNotEqual() {
        RenamePreview a = new RenamePreview(FILE_ID, ORIGINAL_NAME, NEW_NAME, false, null);
        RenamePreview b = new RenamePreview(FILE_ID, ORIGINAL_NAME, null,     true,  "err");

        assertNotEquals(a, b);
    }

    @Test
    void equality_whenDifferentNewName_thenRecordsAreNotEqual() {
        RenamePreview a = new RenamePreview(FILE_ID, ORIGINAL_NAME, "alpha.jpg", false, null);
        RenamePreview b = new RenamePreview(FILE_ID, ORIGINAL_NAME, "beta.jpg",  false, null);

        assertNotEquals(a, b);
    }

    // --- hashCode consistency ---

    @Test
    void hashCode_whenEqualRecords_thenSameHashCode() {
        RenamePreview a = new RenamePreview(FILE_ID, ORIGINAL_NAME, NEW_NAME, false, null);
        RenamePreview b = new RenamePreview(FILE_ID, ORIGINAL_NAME, NEW_NAME, false, null);

        assertEquals(a.hashCode(), b.hashCode());
    }

    // --- toString is non-null ---

    @Test
    void toString_whenCalled_thenNonNull() {
        RenamePreview preview = new RenamePreview(FILE_ID, ORIGINAL_NAME, NEW_NAME, false, null);

        assertNotNull(preview.toString());
    }
}
