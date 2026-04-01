package ua.renamer.app.api.model.config;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.ItemPosition;

import static org.junit.jupiter.api.Assertions.*;

class ParentFolderConfigTest {

    @Test
    void givenValidParams_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            ParentFolderConfig.builder()
                .withPosition(ItemPosition.BEGIN)
                .withNumberOfParentFolders(1)
                .withSeparator("_")
                .build()
        );
    }

    @Test
    void givenNullPosition_whenBuild_thenNullPointerException() {
        var builder = ParentFolderConfig.builder()
            .withPosition(null)
            .withNumberOfParentFolders(1)
            .withSeparator("_");

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenZeroNumberOfParentFolders_whenBuild_thenIllegalArgumentException() {
        var builder = ParentFolderConfig.builder()
            .withPosition(ItemPosition.BEGIN)
            .withNumberOfParentFolders(0)
            .withSeparator("_");

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void givenNegativeNumberOfParentFolders_whenBuild_thenIllegalArgumentException() {
        var builder = ParentFolderConfig.builder()
            .withPosition(ItemPosition.END)
            .withNumberOfParentFolders(-1)
            .withSeparator("_");

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void givenMultipleParentFolders_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            ParentFolderConfig.builder()
                .withPosition(ItemPosition.END)
                .withNumberOfParentFolders(3)
                .withSeparator("/")
                .build()
        );
    }

    @Test
    void givenNullSeparator_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            ParentFolderConfig.builder()
                .withPosition(ItemPosition.BEGIN)
                .withNumberOfParentFolders(1)
                .withSeparator(null)
                .build()
        );
    }
}
