package ua.renamer.app.api.model.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionChangeConfigTest {

    @Test
    void givenValidParams_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            ExtensionChangeConfig.builder()
                .withNewExtension("mp4")
                .build()
        );
    }

    @Test
    void givenNullNewExtension_whenBuild_thenNullPointerException() {
        var builder = ExtensionChangeConfig.builder()
            .withNewExtension(null);

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenBlankExtension_whenBuild_thenIllegalArgumentException() {
        var builder = ExtensionChangeConfig.builder()
            .withNewExtension("");

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void givenWhitespaceOnlyExtension_whenBuild_thenIllegalArgumentException() {
        var builder = ExtensionChangeConfig.builder()
            .withNewExtension("   ");

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void givenExtensionWithLeadingDot_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            ExtensionChangeConfig.builder()
                .withNewExtension(".jpg")
                .build()
        );
    }

    @Test
    void givenTabOnlyExtension_whenBuild_thenIllegalArgumentException() {
        var builder = ExtensionChangeConfig.builder()
            .withNewExtension("\t");

        assertThrows(IllegalArgumentException.class, builder::build);
    }
}
