package ua.renamer.app.api.model;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PreparedFileModelTest {

    private static FileModel fileModelWith(String name, String extension) {
        return FileModel.builder()
                .withFile(new File("/tmp/" + name + (extension.isEmpty() ? "" : "." + extension)))
                .withName(name)
                .withExtension(extension)
                .withAbsolutePath("/tmp/" + name + (extension.isEmpty() ? "" : "." + extension))
                .withIsFile(true)
                .withFileSize(0L)
                .build();
    }

    private static PreparedFileModel preparedWith(FileModel original, String newName, String newExtension) {
        return PreparedFileModel.builder()
                .withOriginalFile(original)
                .withNewName(newName)
                .withNewExtension(newExtension)
                .withHasError(false)
                .withErrorMessage(null)
                .withTransformationMeta(null)
                .build();
    }

    @Test
    void getOldFullName_withExtension_returnsNameDotExtension() {
        FileModel fm = fileModelWith("file", "txt");
        PreparedFileModel pm = preparedWith(fm, "file", "txt");

        assertEquals("file.txt", pm.getOldFullName());
    }

    @Test
    void getOldFullName_withEmptyExtension_returnsNameOnly() {
        FileModel fm = fileModelWith("Makefile", "");
        PreparedFileModel pm = preparedWith(fm, "Makefile", "");

        assertEquals("Makefile", pm.getOldFullName());
    }

    @Test
    void getOldFullName_andGetNewFullName_areSymmetric_whenNoTransformApplied() {
        // No transform: newName == originalName, newExtension == originalExtension (both empty)
        FileModel fm = fileModelWith("my_folder", "");
        PreparedFileModel pm = preparedWith(fm, "my_folder", "");

        assertEquals(pm.getOldFullName(), pm.getNewFullName());
    }

    @Test
    void needsRename_returnsFalse_forItemWithNoExtension_andNoTransform() {
        // Regression: trailing-dot scenario — folder with no extension must not show Pending
        FileModel fm = fileModelWith("my_folder", "");
        PreparedFileModel pm = preparedWith(fm, "my_folder", "");

        assertFalse(pm.needsRename());
    }
}
