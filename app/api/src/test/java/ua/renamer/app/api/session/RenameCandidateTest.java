package ua.renamer.app.api.session;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RenameCandidateTest {

    private static final String FILE_ID = "abc-123";
    private static final String NAME = "photo";
    private static final String EXT = "jpg";
    private static final Path PATH = Path.of("/images/photo.jpg");

    // --- accessor correctness ---

    @Test
    void fileId_whenConstructed_thenReturnsExpectedValue() {
        RenameCandidate candidate = new RenameCandidate(FILE_ID, NAME, EXT, PATH);

        assertEquals(FILE_ID, candidate.fileId());
    }

    @Test
    void name_whenConstructed_thenReturnsExpectedValue() {
        RenameCandidate candidate = new RenameCandidate(FILE_ID, NAME, EXT, PATH);

        assertEquals(NAME, candidate.name());
    }

    @Test
    void extension_whenConstructed_thenReturnsExpectedValue() {
        RenameCandidate candidate = new RenameCandidate(FILE_ID, NAME, EXT, PATH);

        assertEquals(EXT, candidate.extension());
    }

    @Test
    void path_whenConstructed_thenReturnsExpectedValue() {
        RenameCandidate candidate = new RenameCandidate(FILE_ID, NAME, EXT, PATH);

        assertEquals(PATH, candidate.path());
    }

    // --- boundary: empty extension (no-extension file) ---

    @Test
    void extension_whenEmptyString_thenReturnsEmptyString() {
        RenameCandidate candidate = new RenameCandidate(FILE_ID, "Makefile", "", PATH);

        assertEquals("", candidate.extension());
    }

    // --- boundary: unicode in name ---

    @Test
    void name_withUnicodeCharacters_thenPreservedExactly() {
        String unicodeName = "\u6771\uAD6D\uC0AC\uC9C4"; // mixed CJK
        RenameCandidate candidate = new RenameCandidate("id-1", unicodeName, "png", PATH);

        assertEquals(unicodeName, candidate.name());
    }

    @Test
    void name_withEmojiInName_thenPreservedExactly() {
        String emojiName = "photo\uD83D\uDE00";
        RenameCandidate candidate = new RenameCandidate("id-2", emojiName, "jpg", PATH);

        assertEquals(emojiName, candidate.name());
    }

    // --- record equality ---

    @Test
    void equality_whenSameFields_thenRecordsAreEqual() {
        RenameCandidate a = new RenameCandidate(FILE_ID, NAME, EXT, PATH);
        RenameCandidate b = new RenameCandidate(FILE_ID, NAME, EXT, PATH);

        assertEquals(a, b);
    }

    @Test
    void equality_whenDifferentFileId_thenRecordsAreNotEqual() {
        RenameCandidate a = new RenameCandidate("id-A", NAME, EXT, PATH);
        RenameCandidate b = new RenameCandidate("id-B", NAME, EXT, PATH);

        assertNotEquals(a, b);
    }

    @Test
    void equality_whenDifferentName_thenRecordsAreNotEqual() {
        RenameCandidate a = new RenameCandidate(FILE_ID, "photo", EXT, PATH);
        RenameCandidate b = new RenameCandidate(FILE_ID, "picture", EXT, PATH);

        assertNotEquals(a, b);
    }

    @Test
    void equality_whenDifferentExtension_thenRecordsAreNotEqual() {
        RenameCandidate a = new RenameCandidate(FILE_ID, NAME, "jpg", PATH);
        RenameCandidate b = new RenameCandidate(FILE_ID, NAME, "png", PATH);

        assertNotEquals(a, b);
    }

    @Test
    void equality_whenDifferentPath_thenRecordsAreNotEqual() {
        RenameCandidate a = new RenameCandidate(FILE_ID, NAME, EXT, Path.of("/a/photo.jpg"));
        RenameCandidate b = new RenameCandidate(FILE_ID, NAME, EXT, Path.of("/b/photo.jpg"));

        assertNotEquals(a, b);
    }

    // --- hashCode consistency with equals ---

    @Test
    void hashCode_whenEqualRecords_thenSameHashCode() {
        RenameCandidate a = new RenameCandidate(FILE_ID, NAME, EXT, PATH);
        RenameCandidate b = new RenameCandidate(FILE_ID, NAME, EXT, PATH);

        assertEquals(a.hashCode(), b.hashCode());
    }

    // --- toString is non-null ---

    @Test
    void toString_whenCalled_thenNonNull() {
        RenameCandidate candidate = new RenameCandidate(FILE_ID, NAME, EXT, PATH);

        assertNotNull(candidate.toString());
    }
}
