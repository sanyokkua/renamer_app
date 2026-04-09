package ua.renamer.app.api.model.config;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.SortSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SequenceConfigTest {

    @Test
    void givenNegativePadding_whenConfigConstructed_thenIllegalArgumentExceptionThrown() {
        var builder = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(-1)
                .withSortSource(SortSource.FILE_NAME);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void givenZeroPadding_whenConfigConstructed_thenSucceeds() {
        assertDoesNotThrow(() ->
                SequenceConfig.builder()
                        .withStartNumber(1)
                        .withStepValue(1)
                        .withPadding(0)
                        .withSortSource(SortSource.FILE_NAME)
                        .build()
        );
    }

    @Test
    void givenPositivePadding_whenConfigConstructed_thenSucceeds() {
        assertDoesNotThrow(() ->
                SequenceConfig.builder()
                        .withStartNumber(1)
                        .withStepValue(1)
                        .withPadding(3)
                        .withSortSource(SortSource.FILE_NAME)
                        .build()
        );
    }
}
