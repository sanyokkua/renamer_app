package ua.renamer.app.core.service.command.impl;

import org.junit.jupiter.api.Test;
import ua.renamer.app.core.enums.RenameResult;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.mapper.impl.FileInformationToRenameModelMapper;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapFileInformationToRenameModelCommandTest {

    private static FileInformation createFileInfo(String root, String name, String newName) {
        return FileInformation.builder()
                              .originalFile(new File(name))
                              .fileAbsolutePath(root)
                              .isFile(true)
                              .fileName(name)
                              .fileExtension(".ext")
                              .fileSize(1000L)
                              .fsCreationDate(LocalDateTime.now())
                              .fsModificationDate(LocalDateTime.now())
                              .metadata(null)
                              .newName(newName)
                              .newExtension(".ext")
                              .build();
    }

    @Test
    void testMapper() {
        var fileInfo1 = createFileInfo("/root/name.ext", "name", "name");
        var fileInfo2 = createFileInfo("/root/name.ext", "name", "newName");
        var fileInfo3 = createFileInfo("/root/name", "name", "newName2");
        var listOfItems = List.of(fileInfo1, fileInfo2, fileInfo3);

        var cmd = new MapFileInformationToRenameModelCommand(new FileInformationToRenameModelMapper());

        var result = cmd.execute(listOfItems, null);

        assertNotNull(result);
        assertEquals(listOfItems.size(), result.size());
        assertEquals(fileInfo1, result.get(0).getFileInformation());
        assertEquals(fileInfo2, result.get(1).getFileInformation());
        assertEquals(fileInfo3, result.get(2).getFileInformation());

        assertFalse(result.get(0).isNeedRename());
        assertFalse(result.get(0).isRenamed());
        assertFalse(result.get(0).isHasRenamingError());
        assertEquals("name.ext", result.get(0).getOldName());
        assertEquals("name.ext", result.get(0).getNewName());
        assertEquals("/root/", result.get(0).getAbsolutePathWithoutName());
        assertEquals("", result.get(0).getRenamingErrorMessage());
        assertEquals(RenameResult.NO_ACTIONS_HAPPEN, result.get(0).getRenameResult());

        assertTrue(result.get(1).isNeedRename());
        assertFalse(result.get(1).isRenamed());
        assertFalse(result.get(1).isHasRenamingError());
        assertEquals("name.ext", result.get(1).getOldName());
        assertEquals("newName.ext", result.get(1).getNewName());
        assertEquals("/root/", result.get(1).getAbsolutePathWithoutName());
        assertEquals("", result.get(1).getRenamingErrorMessage());
        assertEquals(RenameResult.NO_ACTIONS_HAPPEN, result.get(1).getRenameResult());

        assertTrue(result.get(2).isNeedRename());
        assertFalse(result.get(2).isRenamed());
        assertTrue(result.get(2).isHasRenamingError());
        assertEquals("name.ext", result.get(2).getOldName());
        assertEquals("newName2.ext", result.get(2).getNewName());
        assertEquals("", result.get(2).getAbsolutePathWithoutName());
        assertEquals(
                "Check if the file name (name.ext) and absolute path (/root/name) is correct. File Name (name.ext) is not found in the path (/root/name)",
                result.get(2).getRenamingErrorMessage());
        assertEquals(RenameResult.NOT_RENAMED_BECAUSE_OF_ERROR, result.get(2).getRenameResult());
    }
}