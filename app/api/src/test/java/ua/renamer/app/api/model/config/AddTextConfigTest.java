package ua.renamer.app.api.model.config;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.ItemPosition;

import static org.junit.jupiter.api.Assertions.*;

class AddTextConfigTest {

    @Test
    void givenValidParams_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            AddTextConfig.builder()
                .withTextToAdd("prefix_")
                .withPosition(ItemPosition.BEGIN)
                .build()
        );
    }

    @Test
    void givenNullPosition_whenBuild_thenNullPointerException() {
        var builder = AddTextConfig.builder()
            .withTextToAdd("prefix_")
            .withPosition(null);

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenNullTextToAdd_whenBuild_thenNullPointerException() {
        var builder = AddTextConfig.builder()
            .withTextToAdd(null)
            .withPosition(ItemPosition.END);

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenEndPosition_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            AddTextConfig.builder()
                .withTextToAdd("_suffix")
                .withPosition(ItemPosition.END)
                .build()
        );
    }

    @Test
    void givenEmptyTextToAdd_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            AddTextConfig.builder()
                .withTextToAdd("")
                .withPosition(ItemPosition.BEGIN)
                .build()
        );
    }
}
