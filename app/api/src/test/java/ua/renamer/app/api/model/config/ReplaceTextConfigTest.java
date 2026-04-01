package ua.renamer.app.api.model.config;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.ItemPositionExtended;

import static org.junit.jupiter.api.Assertions.*;

class ReplaceTextConfigTest {

    @Test
    void givenValidParams_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            ReplaceTextConfig.builder()
                .withTextToReplace("old")
                .withReplacementText("new")
                .withPosition(ItemPositionExtended.EVERYWHERE)
                .build()
        );
    }

    @Test
    void givenNullPosition_whenBuild_thenNullPointerException() {
        var builder = ReplaceTextConfig.builder()
            .withTextToReplace("old")
            .withReplacementText("new")
            .withPosition(null);

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenNullTextToReplace_whenBuild_thenNullPointerException() {
        var builder = ReplaceTextConfig.builder()
            .withTextToReplace(null)
            .withReplacementText("new")
            .withPosition(ItemPositionExtended.BEGIN);

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenNullReplacementText_whenBuild_thenNullPointerException() {
        var builder = ReplaceTextConfig.builder()
            .withTextToReplace("old")
            .withReplacementText(null)
            .withPosition(ItemPositionExtended.END);

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenBeginPosition_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            ReplaceTextConfig.builder()
                .withTextToReplace("old")
                .withReplacementText("new")
                .withPosition(ItemPositionExtended.BEGIN)
                .build()
        );
    }

    @Test
    void givenEmptyReplacementText_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            ReplaceTextConfig.builder()
                .withTextToReplace("old")
                .withReplacementText("")
                .withPosition(ItemPositionExtended.EVERYWHERE)
                .build()
        );
    }
}
