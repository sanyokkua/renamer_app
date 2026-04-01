package ua.renamer.app.api.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AvailableActionTest {

    @Test
    void values_thenExactlySixConstants() {
        assertEquals(6, AvailableAction.values().length);
    }

    @Test
    void valueOf_addFiles_thenCorrectConstant() {
        assertEquals(AvailableAction.ADD_FILES, AvailableAction.valueOf("ADD_FILES"));
    }

    @Test
    void valueOf_removeFiles_thenCorrectConstant() {
        assertEquals(AvailableAction.REMOVE_FILES, AvailableAction.valueOf("REMOVE_FILES"));
    }

    @Test
    void valueOf_clear_thenCorrectConstant() {
        assertEquals(AvailableAction.CLEAR, AvailableAction.valueOf("CLEAR"));
    }

    @Test
    void valueOf_selectMode_thenCorrectConstant() {
        assertEquals(AvailableAction.SELECT_MODE, AvailableAction.valueOf("SELECT_MODE"));
    }

    @Test
    void valueOf_execute_thenCorrectConstant() {
        assertEquals(AvailableAction.EXECUTE, AvailableAction.valueOf("EXECUTE"));
    }

    @Test
    void valueOf_cancel_thenCorrectConstant() {
        assertEquals(AvailableAction.CANCEL, AvailableAction.valueOf("CANCEL"));
    }

    @Test
    void valueOf_unknownName_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> AvailableAction.valueOf("UNKNOWN"));
    }

    @Test
    void ordinals_matchDeclarationOrder() {
        assertEquals(0, AvailableAction.ADD_FILES.ordinal());
        assertEquals(1, AvailableAction.REMOVE_FILES.ordinal());
        assertEquals(2, AvailableAction.CLEAR.ordinal());
        assertEquals(3, AvailableAction.SELECT_MODE.ordinal());
        assertEquals(4, AvailableAction.EXECUTE.ordinal());
        assertEquals(5, AvailableAction.CANCEL.ordinal());
    }

    @Test
    void eachConstant_isSameInstance_whenRetrievedTwice() {
        assertSame(AvailableAction.ADD_FILES, AvailableAction.valueOf("ADD_FILES"));
        assertSame(AvailableAction.REMOVE_FILES, AvailableAction.valueOf("REMOVE_FILES"));
        assertSame(AvailableAction.CLEAR, AvailableAction.valueOf("CLEAR"));
        assertSame(AvailableAction.SELECT_MODE, AvailableAction.valueOf("SELECT_MODE"));
        assertSame(AvailableAction.EXECUTE, AvailableAction.valueOf("EXECUTE"));
        assertSame(AvailableAction.CANCEL, AvailableAction.valueOf("CANCEL"));
    }
}
