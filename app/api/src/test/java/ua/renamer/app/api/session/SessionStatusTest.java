package ua.renamer.app.api.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionStatusTest {

    @Test
    void values_thenExactlySixConstants() {
        assertEquals(6, SessionStatus.values().length);
    }

    @Test
    void valueOf_empty_thenReturnsEmptyConstant() {
        assertEquals(SessionStatus.EMPTY, SessionStatus.valueOf("EMPTY"));
    }

    @Test
    void valueOf_filesLoaded_thenReturnsFilesLoadedConstant() {
        assertEquals(SessionStatus.FILES_LOADED, SessionStatus.valueOf("FILES_LOADED"));
    }

    @Test
    void valueOf_modeConfigured_thenReturnsModeConfiguredConstant() {
        assertEquals(SessionStatus.MODE_CONFIGURED, SessionStatus.valueOf("MODE_CONFIGURED"));
    }

    @Test
    void valueOf_executing_thenReturnsExecutingConstant() {
        assertEquals(SessionStatus.EXECUTING, SessionStatus.valueOf("EXECUTING"));
    }

    @Test
    void valueOf_complete_thenReturnsCompleteConstant() {
        assertEquals(SessionStatus.COMPLETE, SessionStatus.valueOf("COMPLETE"));
    }

    @Test
    void valueOf_error_thenReturnsErrorConstant() {
        assertEquals(SessionStatus.ERROR, SessionStatus.valueOf("ERROR"));
    }

    @Test
    void valueOf_unknownName_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> SessionStatus.valueOf("UNKNOWN"));
    }

    @Test
    void ordinals_matchDeclarationOrder() {
        // Verifies insertion order has not been accidentally changed.
        assertEquals(0, SessionStatus.EMPTY.ordinal());
        assertEquals(1, SessionStatus.FILES_LOADED.ordinal());
        assertEquals(2, SessionStatus.MODE_CONFIGURED.ordinal());
        assertEquals(3, SessionStatus.EXECUTING.ordinal());
        assertEquals(4, SessionStatus.COMPLETE.ordinal());
        assertEquals(5, SessionStatus.ERROR.ordinal());
    }

    @Test
    void name_matchesDeclarationNames() {
        assertEquals("EMPTY", SessionStatus.EMPTY.name());
        assertEquals("FILES_LOADED", SessionStatus.FILES_LOADED.name());
        assertEquals("MODE_CONFIGURED", SessionStatus.MODE_CONFIGURED.name());
        assertEquals("EXECUTING", SessionStatus.EXECUTING.name());
        assertEquals("COMPLETE", SessionStatus.COMPLETE.name());
        assertEquals("ERROR", SessionStatus.ERROR.name());
    }

    @Test
    void eachConstant_isSameInstance_whenRetrievedTwice() {
        assertSame(SessionStatus.EMPTY, SessionStatus.valueOf("EMPTY"));
        assertSame(SessionStatus.FILES_LOADED, SessionStatus.valueOf("FILES_LOADED"));
        assertSame(SessionStatus.MODE_CONFIGURED, SessionStatus.valueOf("MODE_CONFIGURED"));
        assertSame(SessionStatus.EXECUTING, SessionStatus.valueOf("EXECUTING"));
        assertSame(SessionStatus.COMPLETE, SessionStatus.valueOf("COMPLETE"));
        assertSame(SessionStatus.ERROR, SessionStatus.valueOf("ERROR"));
    }
}
