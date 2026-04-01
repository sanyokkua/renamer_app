package ua.renamer.app.api.model.config;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.ItemPosition;

import static org.junit.jupiter.api.Assertions.*;

class RemoveTextConfigTest {

    @Test
    void givenValidParams_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            RemoveTextConfig.builder()
                .withTextToRemove("prefix_")
                .withPosition(ItemPosition.BEGIN)
                .build()
        );
    }

    @Test
    void givenNullPosition_whenBuild_thenNullPointerException() {
        var builder = RemoveTextConfig.builder()
            .withTextToRemove("prefix_")
            .withPosition(null);

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenNullTextToRemove_whenBuild_thenNullPointerException() {
        var builder = RemoveTextConfig.builder()
            .withTextToRemove(null)
            .withPosition(ItemPosition.END);

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenEndPosition_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            RemoveTextConfig.builder()
                .withTextToRemove("_suffix")
                .withPosition(ItemPosition.END)
                .build()
        );
    }

    @Test
    void givenEmptyTextToRemove_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            RemoveTextConfig.builder()
                .withTextToRemove("")
                .withPosition(ItemPosition.BEGIN)
                .build()
        );
    }
}
