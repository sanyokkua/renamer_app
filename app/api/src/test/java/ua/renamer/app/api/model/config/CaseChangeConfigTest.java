package ua.renamer.app.api.model.config;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.TextCaseOptions;

import static org.junit.jupiter.api.Assertions.*;

class CaseChangeConfigTest {

    @Test
    void givenValidParams_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.LOWERCASE)
                .withCapitalizeFirstLetter(false)
                .build()
        );
    }

    @Test
    void givenNullCaseOption_whenBuild_thenNullPointerException() {
        var builder = CaseChangeConfig.builder()
            .withCaseOption(null)
            .withCapitalizeFirstLetter(false);

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenUppercaseOption_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.UPPERCASE)
                .withCapitalizeFirstLetter(true)
                .build()
        );
    }

    @Test
    void givenTitleCaseOption_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.TITLE_CASE)
                .withCapitalizeFirstLetter(false)
                .build()
        );
    }
}
