package ua.renamer.app.api.session;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.TransformationMode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionSnapshotTest {

    // ─── helpers ────────────────────────────────────────────────────────────

    private static RenameCandidate candidate(String id) {
        return new RenameCandidate(id, "file", "txt", Path.of("/tmp/" + id + ".txt"));
    }

    private static RenamePreview preview(String id) {
        return new RenamePreview(id, "file.txt", "file_new.txt", false, null);
    }

    private static AddTextParams defaultParams() {
        return new AddTextParams("prefix_", ItemPosition.BEGIN);
    }

    private static SessionSnapshot emptySnapshot() {
        return new SessionSnapshot(List.of(), null, null, List.of(), SessionStatus.EMPTY);
    }

    // ─── accessor correctness ────────────────────────────────────────────────

    @Test
    void files_whenConstructed_thenReturnsExpectedList() {
        var files = List.of(candidate("f1"), candidate("f2"));
        var snapshot = new SessionSnapshot(
                files, TransformationMode.ADD_TEXT, defaultParams(), List.of(), SessionStatus.FILES_LOADED);
        assertEquals(files, snapshot.files());
    }

    @Test
    void activeMode_whenSet_thenReturnsMode() {
        var snapshot = new SessionSnapshot(
                List.of(), TransformationMode.CHANGE_CASE, defaultParams(), List.of(), SessionStatus.MODE_CONFIGURED);
        assertEquals(TransformationMode.CHANGE_CASE, snapshot.activeMode());
    }

    @Test
    void activeMode_whenNull_thenReturnsNull() {
        assertNull(emptySnapshot().activeMode());
    }

    @Test
    void currentParameters_whenNull_thenReturnsNull() {
        assertNull(emptySnapshot().currentParameters());
    }

    @Test
    void preview_whenConstructed_thenReturnsExpectedList() {
        var previews = List.of(preview("f1"));
        var snapshot = new SessionSnapshot(
                List.of(candidate("f1")), TransformationMode.ADD_TEXT, defaultParams(),
                previews, SessionStatus.MODE_CONFIGURED);
        assertEquals(previews, snapshot.preview());
    }

    @Test
    void status_whenConstructed_thenReturnsStatus() {
        var snapshot = new SessionSnapshot(List.of(), null, null, List.of(), SessionStatus.ERROR);
        assertEquals(SessionStatus.ERROR, snapshot.status());
    }

    // ─── defensive copy: mutations to original list do not affect snapshot ──

    @Test
    void files_whenOriginalListMutated_thenSnapshotUnchanged() {
        var mutable = new ArrayList<>(List.of(candidate("f1")));
        var snapshot = new SessionSnapshot(mutable, null, null, List.of(), SessionStatus.FILES_LOADED);
        mutable.add(candidate("f2"));
        assertEquals(1, snapshot.files().size());
    }

    @Test
    void preview_whenOriginalListMutated_thenSnapshotUnchanged() {
        var mutable = new ArrayList<>(List.of(preview("f1")));
        var snapshot = new SessionSnapshot(List.of(candidate("f1")), null, null, mutable, SessionStatus.MODE_CONFIGURED);
        mutable.add(preview("f2"));
        assertEquals(1, snapshot.preview().size());
    }

    // ─── returned lists are unmodifiable ────────────────────────────────────

    @Test
    void files_whenReturnedList_thenIsUnmodifiable() {
        var snapshot = new SessionSnapshot(
                List.of(candidate("f1")), null, null, List.of(), SessionStatus.FILES_LOADED);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.files().add(candidate("f99")));
    }

    @Test
    void preview_whenReturnedList_thenIsUnmodifiable() {
        var snapshot = new SessionSnapshot(
                List.of(), null, null, List.of(preview("f1")), SessionStatus.MODE_CONFIGURED);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.preview().add(preview("f99")));
    }

    // ─── null list arguments rejected ───────────────────────────────────────

    @Test
    void constructor_whenFilesIsNull_thenThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new SessionSnapshot(null, null, null, List.of(), SessionStatus.EMPTY));
    }

    @Test
    void constructor_whenPreviewIsNull_thenThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new SessionSnapshot(List.of(), null, null, null, SessionStatus.EMPTY));
    }

    // ─── record equality and hashCode ───────────────────────────────────────

    @Test
    void equality_whenSameFields_thenSnapshotsAreEqual() {
        var files = List.of(candidate("f1"));
        var prevs = List.of(preview("f1"));
        var params = defaultParams();
        var a = new SessionSnapshot(files, TransformationMode.ADD_TEXT, params, prevs, SessionStatus.MODE_CONFIGURED);
        var b = new SessionSnapshot(files, TransformationMode.ADD_TEXT, params, prevs, SessionStatus.MODE_CONFIGURED);
        assertEquals(a, b);
    }

    @Test
    void equality_whenDifferentStatus_thenNotEqual() {
        var a = new SessionSnapshot(List.of(), null, null, List.of(), SessionStatus.EMPTY);
        var b = new SessionSnapshot(List.of(), null, null, List.of(), SessionStatus.FILES_LOADED);
        assertNotEquals(a, b);
    }

    @Test
    void equality_whenDifferentFiles_thenNotEqual() {
        var a = new SessionSnapshot(List.of(candidate("f1")), null, null, List.of(), SessionStatus.FILES_LOADED);
        var b = new SessionSnapshot(List.of(candidate("f2")), null, null, List.of(), SessionStatus.FILES_LOADED);
        assertNotEquals(a, b);
    }

    @Test
    void hashCode_whenEqualSnapshots_thenSameHashCode() {
        assertEquals(emptySnapshot().hashCode(), emptySnapshot().hashCode());
    }

    @Test
    void toString_whenCalled_thenNonNull() {
        assertNotNull(emptySnapshot().toString());
    }
}
