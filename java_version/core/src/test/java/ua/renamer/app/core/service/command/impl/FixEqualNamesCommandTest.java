package ua.renamer.app.core.service.command.impl;

import org.junit.jupiter.api.Test;
import ua.renamer.app.core.model.FileInformation;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ua.renamer.app.core.TestUtilities.TEST_ABSOLUTE_PATH;

class FixEqualNamesCommandTest {

    @Test
    void testCommandAddsUniquesSymbolsToTheSameNames() {
        var file1ChangedAddedUniqueSymbols = createFileInfo("Name", "NewName", ".jpg", ".jpg");

        var file2NotChanged = createFileInfo("NewName", "NewName", ".jpg", ".jpg");
        var file3NotChanged = createFileInfo("Name", "NewName", ".png", ".png");
        var file4NotChanged = createFileInfo("Name", "NewName", ".png", "");

        var file5NotChanged = createFileInfo("Name", "Name1", ".txt", ".txt");
        var file6NotChanged = createFileInfo("Name", "Name2", ".txt", ".txt");

        var file7NotChanged = createFileInfo("Name3", "Name3", ".txt", ".txt");
        var file8ChangedAddedUniqueSymbols = createFileInfo("Name", "Name3", ".txt", ".txt");
        var file9ChangedAddedUniqueSymbols = createFileInfo("Name", "Name3", ".txt", ".txt");
        var file10ChangedAddedUniqueSymbols = createFileInfo("Name", "Name3", ".txt", ".txt");
        var file11ChangedAddedUniqueSymbols = createFileInfo("Name", "Name3", ".txt", ".txt");
        var file12ChangedAddedUniqueSymbols = createFileInfo("Name", "Name3", ".txt", "txt");
        var file13ChangedAddedUniqueSymbols = createFileInfo("Name", "Name3", ".txt", "txt");
        var file14ChangedAddedUniqueSymbols = createFileInfo("Name", "Name3", ".txt", "txt");
        var file15ChangedAddedUniqueSymbols = createFileInfo("Name", "Name3", ".txt", "txt");
        var file16ChangedAddedUniqueSymbols = createFileInfo("Name", "Name3", ".txt", "txt");
        var file17ChangedAddedUniqueSymbols = createFileInfo("Name", "Name3", ".txt", "txt");
        var file18ChangedAddedUniqueSymbols = createFileInfo("Name", "Name3", ".txt", "txt");
        var file19ChangedAddedUniqueSymbols = createFileInfo("Name", "Name3", ".txt", "txt");
        var files = List.of(file1ChangedAddedUniqueSymbols, file2NotChanged, file3NotChanged, file4NotChanged, file5NotChanged, file6NotChanged, file7NotChanged, file8ChangedAddedUniqueSymbols, file9ChangedAddedUniqueSymbols, file10ChangedAddedUniqueSymbols, file11ChangedAddedUniqueSymbols, file12ChangedAddedUniqueSymbols, file13ChangedAddedUniqueSymbols, file14ChangedAddedUniqueSymbols, file15ChangedAddedUniqueSymbols, file16ChangedAddedUniqueSymbols, file17ChangedAddedUniqueSymbols, file18ChangedAddedUniqueSymbols, file19ChangedAddedUniqueSymbols);

        var cmd = new FixEqualNamesCommand();

        var result = cmd.execute(files, (curr, max) -> {
        });

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(files.size(), result.size());
        for (int i = 0; i < files.size(); i++) {
            assertEquals(files.get(i), result.get(i));
        }

        assertEquals("NewName (1)", file1ChangedAddedUniqueSymbols.getNewName());
        assertEquals("NewName", file2NotChanged.getNewName());
        assertEquals("NewName", file3NotChanged.getNewName());
        assertEquals("NewName", file4NotChanged.getNewName());
        assertEquals("Name1", file5NotChanged.getNewName());
        assertEquals("Name2", file6NotChanged.getNewName());
        assertEquals("Name3", file7NotChanged.getNewName());
        assertEquals("Name3 (01)", file8ChangedAddedUniqueSymbols.getNewName());
        assertEquals("Name3 (02)", file9ChangedAddedUniqueSymbols.getNewName());
        assertEquals("Name3 (03)", file10ChangedAddedUniqueSymbols.getNewName());
        assertEquals("Name3 (04)", file11ChangedAddedUniqueSymbols.getNewName());
        assertEquals("Name3 (05)", file12ChangedAddedUniqueSymbols.getNewName());
        assertEquals("Name3 (06)", file13ChangedAddedUniqueSymbols.getNewName());
        assertEquals("Name3 (07)", file14ChangedAddedUniqueSymbols.getNewName());
        assertEquals("Name3 (08)", file15ChangedAddedUniqueSymbols.getNewName());
        assertEquals("Name3 (09)", file16ChangedAddedUniqueSymbols.getNewName());
        assertEquals("Name3 (10)", file17ChangedAddedUniqueSymbols.getNewName());
        assertEquals("Name3 (11)", file18ChangedAddedUniqueSymbols.getNewName());
        assertEquals("Name3 (12)", file19ChangedAddedUniqueSymbols.getNewName());
    }

    static FileInformation createFileInfo(String name, String newName, String ext, String newExt) {
        return FileInformation.builder()
                              .originalFile(new File(TEST_ABSOLUTE_PATH))
                              .fileAbsolutePath(TEST_ABSOLUTE_PATH)
                              .fileName(name)
                              .newName(newName)
                              .fileExtension(ext)
                              .newExtension(newExt)
                              .build();
    }

}