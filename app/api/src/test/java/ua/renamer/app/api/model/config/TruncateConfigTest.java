package ua.renamer.app.api.model.config;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.TruncateOptions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TruncateConfigTest {

    @Test
    void givenValidParams_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
                TruncateConfig.builder()
                        .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_FROM_END)
                        .withNumberOfSymbols(5)
                        .build()
        );
    }

    @Test
    void givenNullTruncateOption_whenBuild_thenNullPointerException() {
        var builder = TruncateConfig.builder()
                .withTruncateOption(null)
                .withNumberOfSymbols(5);

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenNegativeNumberOfSymbols_whenBuild_thenIllegalArgumentException() {
        var builder = TruncateConfig.builder()
                .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                .withNumberOfSymbols(-1);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void givenZeroNumberOfSymbols_whenBuild_thenSucceeds() {
        assertDoesNotThrow(() ->
                TruncateConfig.builder()
                        .withTruncateOption(TruncateOptions.TRUNCATE_EMPTY_SYMBOLS)
                        .withNumberOfSymbols(0)
                        .build()
        );
    }

    @Test
    void givenRemoveFromBeginOption_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
                TruncateConfig.builder()
                        .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                        .withNumberOfSymbols(3)
                        .build()
        );
    }
}
