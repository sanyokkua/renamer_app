package ua.renamer.app.api.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandResultTest {

    // --- succeeded() factory ---

    @Test
    void succeeded_whenCalled_thenSuccessIsTrue() {
        CommandResult result = CommandResult.succeeded();

        assertTrue(result.success());
    }

    @Test
    void succeeded_whenCalled_thenErrorMessageIsNull() {
        CommandResult result = CommandResult.succeeded();

        assertNull(result.errorMessage());
    }

    // --- failure() factory ---

    @Test
    void failure_whenCalled_thenSuccessIsFalse() {
        CommandResult result = CommandResult.failure("something went wrong");

        assertFalse(result.success());
    }

    @Test
    void failure_whenCalled_thenErrorMessageMatches() {
        CommandResult result = CommandResult.failure("file not found");

        assertEquals("file not found", result.errorMessage());
    }

    @Test
    void failure_withEmptyMessage_thenErrorMessageIsEmpty() {
        CommandResult result = CommandResult.failure("");

        assertEquals("", result.errorMessage());
    }

    // --- record equality ---

    @Test
    void succeeded_whenCalledTwice_thenResultsAreEqual() {
        CommandResult a = CommandResult.succeeded();
        CommandResult b = CommandResult.succeeded();

        assertEquals(a, b);
    }

    @Test
    void failure_whenSameMessage_thenResultsAreEqual() {
        CommandResult a = CommandResult.failure("oops");
        CommandResult b = CommandResult.failure("oops");

        assertEquals(a, b);
    }

    @Test
    void failure_whenDifferentMessages_thenResultsAreNotEqual() {
        CommandResult a = CommandResult.failure("error A");
        CommandResult b = CommandResult.failure("error B");

        assertNotEquals(a, b);
    }

    @Test
    void succeeded_andFailure_areNotEqual() {
        CommandResult success = CommandResult.succeeded();
        CommandResult fail = CommandResult.failure("err");

        assertNotEquals(success, fail);
    }

    // --- direct record construction (both branches) ---

    @Test
    void directConstruction_withSuccessTrue_thenSuccessIsTrue() {
        CommandResult result = new CommandResult(true, null);

        assertTrue(result.success());
    }

    @Test
    void directConstruction_withSuccessFalse_thenSuccessIsFalse() {
        CommandResult result = new CommandResult(false, "reason");

        assertFalse(result.success());
        assertEquals("reason", result.errorMessage());
    }
}
