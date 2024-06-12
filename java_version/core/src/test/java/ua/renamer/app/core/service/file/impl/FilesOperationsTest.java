package ua.renamer.app.core.service.file.impl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.core.enums.RenameResult;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.core.service.file.BasicFileAttributesExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilesOperationsTest {

    static Path baseTestFolder;

    @Mock
    BasicFileAttributesExtractor basicFileAttributesExtractor;

    @BeforeAll
    static void setup() throws IOException {
        baseTestFolder = Files.createTempDirectory("renameAppTest");
    }

    @AfterAll
    static void tearDown() throws IOException {
        Files.walkFileTree(baseTestFolder, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    static Stream<Arguments> getParentFoldersArguments() {
        // @formatter:off
        return Stream.of(arguments("", List.of()),
                         arguments("/", List.of()),
                         arguments("/file", List.of()),
                         arguments("/file/path", List.of("file")),
                         arguments("/root/user/home/projects/sources/config/app.json", List.of("root", "user", "home", "projects", "sources", "config")),
                         arguments("\\root\\user\\home\\projects\\sources\\config\\app.json", List.of("root", "user", "home", "projects", "sources", "config")),
                         arguments("c:\\root\\user\\home\\projects\\sources\\config\\app.json", List.of("root", "user", "home", "projects", "sources", "config"))
                        );
        // @formatter:on
    }

    @Test
    void validateFileInstance_NullArgument_ThrowsNullPointerException() {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        NullPointerException ex = assertThrows(NullPointerException.class,
                                               () -> filesOperations.validateFileInstance(null),
                                               "Expected that NullPointer exception will be thrown");
        assertNotNull(ex);
    }

    @Test
    void validateFileInstance_NonexistentFile_ThrowsIllegalArgumentException() {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        var mock = mock(File.class);
        when(mock.exists()).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                   () -> filesOperations.validateFileInstance(mock),
                                                   "Expected that IllegalArgumentException exception will be thrown");
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("File does not exist"));
        verify(mock, Mockito.times(1)).exists();
    }

    @Test
    void getFileAbsolutePath_ExistingFile_ReturnsAbsolutePath() {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        var mock = mock(File.class);
        var path = "/absolute/path";

        when(mock.exists()).thenReturn(true);
        when(mock.getAbsolutePath()).thenReturn(path);

        var result = filesOperations.getFileAbsolutePath(mock);

        assertNotNull(result);
        assertEquals(path, result);
        verify(mock, Mockito.times(1)).getAbsolutePath();
    }

    @Test
    void getFileNameWithoutExtension_Directory_ReturnsDirectoryName() {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        var mock = mock(File.class);
        var fileName = "FileName";

        when(mock.exists()).thenReturn(true);
        when(mock.isDirectory()).thenReturn(true);
        when(mock.getName()).thenReturn(fileName);

        var result = filesOperations.getFileNameWithoutExtension(mock);

        assertNotNull(result);
        assertEquals(fileName, result);
        verify(mock, Mockito.times(1)).isDirectory();
        verify(mock, Mockito.times(1)).getName();
    }

    @Test
    void testGetFileNameWithoutExtensionForFileWithoutExtension() {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        var mock = mock(File.class);
        var fileName = "FileName";

        when(mock.exists()).thenReturn(true);
        when(mock.isDirectory()).thenReturn(false);
        when(mock.getName()).thenReturn(fileName);

        var result = filesOperations.getFileNameWithoutExtension(mock);

        assertNotNull(result);
        assertEquals(fileName, result);
        verify(mock, Mockito.times(1)).isDirectory();
        verify(mock, Mockito.times(1)).getName();
    }

    @Test
    void testGetFileNameWithoutExtensionForFileWithExtension() {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        var mock = mock(File.class);
        var separator = ".";
        var extension = "jpg";
        var fileName = "FileName";
        var fileFullName = fileName + separator + extension;

        when(mock.exists()).thenReturn(true);
        when(mock.isDirectory()).thenReturn(false);
        when(mock.getName()).thenReturn(fileFullName);

        var result = filesOperations.getFileNameWithoutExtension(mock);

        assertNotNull(result);
        assertEquals(fileName, result);
        assertFalse(result.contains(extension));
        verify(mock, Mockito.times(1)).isDirectory();
        verify(mock, Mockito.times(1)).getName();
    }

    @Test
    void testGetFileExtensionForDirectory() {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        var mock = mock(File.class);

        when(mock.exists()).thenReturn(true);
        when(mock.isDirectory()).thenReturn(true);

        var result = filesOperations.getFileExtension(mock);

        assertNotNull(result);
        assertEquals("", result);
        verify(mock, Mockito.times(1)).isDirectory();
        verify(mock, Mockito.times(0)).getName();
    }

    @Test
    void testGetFileExtensionForFileWithoutExtension() {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        var mock = mock(File.class);
        var fileFullName = "FileName";

        when(mock.exists()).thenReturn(true);
        when(mock.isDirectory()).thenReturn(false);
        when(mock.getName()).thenReturn(fileFullName);

        var result = filesOperations.getFileExtension(mock);

        assertNotNull(result);
        assertEquals("", result);
        verify(mock, Mockito.times(1)).isDirectory();
        verify(mock, Mockito.times(1)).getName();
    }

    @Test
    void testGetFileExtensionForFileWithExtensionInUpperCase() {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        var mock = mock(File.class);
        var extension = ".JPG";
        var fileName = "FileName";
        var fileFullName = fileName + extension;

        when(mock.exists()).thenReturn(true);
        when(mock.isDirectory()).thenReturn(false);
        when(mock.getName()).thenReturn(fileFullName);

        var result = filesOperations.getFileExtension(mock);

        assertNotNull(result);
        assertEquals(extension.toLowerCase(), result);
        verify(mock, Mockito.times(1)).isDirectory();
        verify(mock, Mockito.times(1)).getName();
    }

    @Test
    void testGetFileExtensionForFileWithExtensionInLowerCase() {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        var mock = mock(File.class);
        var extension = ".jpg";
        var fileName = "FileName";
        var fileFullName = fileName + extension;

        when(mock.exists()).thenReturn(true);
        when(mock.isDirectory()).thenReturn(false);
        when(mock.getName()).thenReturn(fileFullName);

        var result = filesOperations.getFileExtension(mock);

        assertNotNull(result);
        assertEquals(extension, result);
        verify(mock, Mockito.times(1)).isDirectory();
        verify(mock, Mockito.times(1)).getName();
    }

    @Test
    void testIsFile() {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        var mock = mock(File.class);

        when(mock.exists()).thenReturn(true);
        when(mock.isFile()).thenReturn(true);

        var result = filesOperations.isFile(mock);

        assertTrue(result);
    }

    @Test
    void testGetFileCreationTimeSuccess() throws IOException {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        var mockFile = mock(File.class);
        var mockPath = mock(Path.class);
        var mockAttributes = mock(BasicFileAttributes.class);
        var mockFileTime = mock(FileTime.class);
        var instantNow = Instant.now();
        var currentLocalDateTime = LocalDateTime.ofInstant(instantNow, ZoneId.systemDefault());

        when(mockFile.exists()).thenReturn(true);
        when(mockFile.toPath()).thenReturn(mockPath);
        when(mockAttributes.creationTime()).thenReturn(mockFileTime);
        when(mockFileTime.toInstant()).thenReturn(instantNow);
        when(basicFileAttributesExtractor.getAttributes(mockPath,
                                                        BasicFileAttributes.class)).thenReturn(mockAttributes);

        var result = filesOperations.getFileCreationTime(mockFile);

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(currentLocalDateTime, result.get());
        verify(mockFile, Mockito.times(2)).exists();
        verify(mockFile, Mockito.times(1)).toPath();
        verify(mockAttributes, Mockito.times(1)).creationTime();
        verify(mockFileTime, Mockito.times(1)).toInstant();
        verify(basicFileAttributesExtractor, times(1)).getAttributes(mockPath, BasicFileAttributes.class);
    }

    @Test
    void testGetFileCreationTimeReturnedNullAttributes() throws IOException {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        var mockFile = mock(File.class);
        var mockPath = mock(Path.class);
        var mockAttributes = mock(BasicFileAttributes.class);
        var mockFileTime = mock(FileTime.class);

        when(mockFile.exists()).thenReturn(true);
        when(mockFile.toPath()).thenReturn(mockPath);
        when(basicFileAttributesExtractor.getAttributes(mockPath, BasicFileAttributes.class)).thenReturn(null);

        var result = filesOperations.getFileCreationTime(mockFile);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mockFile, Mockito.times(2)).exists();
        verify(mockFile, Mockito.times(1)).toPath();
        verify(mockAttributes, Mockito.times(0)).creationTime();
        verify(mockFileTime, Mockito.times(0)).toInstant();
        verify(basicFileAttributesExtractor, times(1)).getAttributes(mockPath, BasicFileAttributes.class);
    }

    @Test
    void testGetFileCreationTimeExceptionHappen() throws IOException {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        var mockFile = mock(File.class);
        var mockPath = mock(Path.class);
        var mockAttributes = mock(BasicFileAttributes.class);
        var mockFileTime = mock(FileTime.class);

        when(mockFile.exists()).thenReturn(true);
        when(mockFile.toPath()).thenReturn(mockPath);
        when(basicFileAttributesExtractor.getAttributes(mockPath,
                                                        BasicFileAttributes.class)).thenThrow(new IOException());

        var result = filesOperations.getFileCreationTime(mockFile);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mockFile, Mockito.times(2)).exists();
        verify(mockFile, Mockito.times(1)).toPath();
        verify(mockAttributes, Mockito.times(0)).creationTime();
        verify(mockFileTime, Mockito.times(0)).toInstant();
        verify(basicFileAttributesExtractor, times(1)).getAttributes(mockPath, BasicFileAttributes.class);
    }

    @Test
    void testGetFileModificationTime() throws IOException {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        var mockFile = mock(File.class);
        var mockPath = mock(Path.class);
        var mockAttributes = mock(BasicFileAttributes.class);
        var mockFileTime = mock(FileTime.class);

        when(mockFile.exists()).thenReturn(true);
        when(mockFile.toPath()).thenReturn(mockPath);
        when(basicFileAttributesExtractor.getAttributes(mockPath, BasicFileAttributes.class)).thenReturn(null);

        var result = filesOperations.getFileModificationTime(mockFile);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mockFile, Mockito.times(2)).exists();
        verify(mockFile, Mockito.times(1)).toPath();
        verify(mockAttributes, Mockito.times(0)).creationTime();
        verify(mockFileTime, Mockito.times(0)).toInstant();
        verify(basicFileAttributesExtractor, times(1)).getAttributes(mockPath, BasicFileAttributes.class);
    }

    @Test
    void testGetFileModificationTimeReturnedNullAttributes() throws IOException {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        var mockFile = mock(File.class);
        var mockPath = mock(Path.class);
        var mockAttributes = mock(BasicFileAttributes.class);
        var mockFileTime = mock(FileTime.class);

        when(mockFile.exists()).thenReturn(true);
        when(mockFile.toPath()).thenReturn(mockPath);
        when(basicFileAttributesExtractor.getAttributes(mockPath, BasicFileAttributes.class)).thenReturn(null);

        var result = filesOperations.getFileModificationTime(mockFile);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mockFile, Mockito.times(2)).exists();
        verify(mockFile, Mockito.times(1)).toPath();
        verify(mockAttributes, Mockito.times(0)).creationTime();
        verify(mockFileTime, Mockito.times(0)).toInstant();
        verify(basicFileAttributesExtractor, times(1)).getAttributes(mockPath, BasicFileAttributes.class);
    }

    @Test
    void testGetFileModificationTimeExceptionHappen() throws IOException {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        var mockFile = mock(File.class);
        var mockPath = mock(Path.class);
        var mockAttributes = mock(BasicFileAttributes.class);
        var mockFileTime = mock(FileTime.class);

        when(mockFile.exists()).thenReturn(true);
        when(mockFile.toPath()).thenReturn(mockPath);
        when(basicFileAttributesExtractor.getAttributes(mockPath,
                                                        BasicFileAttributes.class)).thenThrow(new IOException());

        var result = filesOperations.getFileModificationTime(mockFile);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mockFile, Mockito.times(2)).exists();
        verify(mockFile, Mockito.times(1)).toPath();
        verify(mockAttributes, Mockito.times(0)).creationTime();
        verify(mockFileTime, Mockito.times(0)).toInstant();
        verify(basicFileAttributesExtractor, times(1)).getAttributes(mockPath, BasicFileAttributes.class);
    }

    @Test
    void testGetFileSize() {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);
        long size = 1000L;
        var mock = mock(File.class);

        when(mock.exists()).thenReturn(true);
        when(mock.length()).thenReturn(size);

        var result = filesOperations.getFileSize(mock);

        assertEquals(size, result);
    }

    @ParameterizedTest
    @MethodSource("getParentFoldersArguments")
    void getParentFolders_VariousPaths_ReturnsExpectedParentFolders(String path, List<String> parents) {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);

        var result = filesOperations.getParentFolders(path);

        assertNotNull(result);
        assertEquals(parents.size(), result.size());
        for (int i = 0; i < parents.size(); i++) {
            assertEquals(parents.get(i), result.get(i));
        }
    }

    @Test
    void testRenameWhenNotNeeded() {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);
        var model = mock(RenameModel.class);

        when(model.isNeedRename()).thenReturn(false);

        var result = filesOperations.renameFile(model);
        assertNotNull(result);
        assertEquals(model, result);
        verify(model, times(1)).setRenamed(false);
        verify(model, times(1)).setRenameResult(RenameResult.NOT_RENAMED_BECAUSE_NOT_NEEDED);
    }

    @Test
    void testRenameWhenThrowsError() throws IOException {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);
        var pathToTmp = baseTestFolder.toAbsolutePath() + File.pathSeparator;
        var oldName = "fileNameOld.txt";
        var newName = "fileNameNew.txt";
//        var file = Files.createFile(Paths.get(oldName));

        var model = mock(RenameModel.class);

        when(model.isNeedRename()).thenReturn(true);
        when(model.getOldName()).thenReturn(oldName);
        when(model.getNewName()).thenReturn(newName);
        when(model.getAbsolutePathWithoutName()).thenReturn(pathToTmp);

        var result = filesOperations.renameFile(model);

        assertNotNull(result);
        assertEquals(model, result);
        verify(model, times(1)).setRenamed(false);
        verify(model, times(1)).setRenameResult(RenameResult.NOT_RENAMED_BECAUSE_OF_ERROR);
        verify(model, times(1)).setHasRenamingError(true);
        verify(model, times(1)).setRenamingErrorMessage(anyString());
    }

    @Test
    void testRenameSuccess() throws IOException {
        var filesOperations = new FilesOperations(basicFileAttributesExtractor);
        var pathToTmp = baseTestFolder.toAbsolutePath() + File.separator;
        var oldName = "fileNameOld.txt";
        var newName = "fileNameNew.txt";
        var fileOld = Paths.get(pathToTmp + oldName);
        var fileNew = Paths.get(pathToTmp + newName);

        Files.createFile(fileOld);

        assertTrue(fileOld.toFile().exists());
        assertFalse(fileNew.toFile().exists());

        var model = mock(RenameModel.class);

        when(model.isNeedRename()).thenReturn(true);
        when(model.getOldName()).thenReturn(oldName);
        when(model.getNewName()).thenReturn(newName);
        when(model.getAbsolutePathWithoutName()).thenReturn(pathToTmp);

        var result = filesOperations.renameFile(model);

        assertNotNull(result);
        assertEquals(model, result);
        verify(model, times(1)).setRenamed(true);
        verify(model, times(1)).setRenameResult(RenameResult.RENAMED_WITHOUT_ERRORS);

        assertFalse(fileOld.toFile().exists());
        assertTrue(fileNew.toFile().exists());
    }
}