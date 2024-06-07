package ua.renamer.app.core.utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUtilsTest {

    @Test
    void testValidateFileInstanceNull() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                                               () -> FileUtils.validateFileInstance(null),
                                               "Expected that NullPointer exception will be thrown"
                                              );
        assertNotNull(ex);
    }

    @Test
    void testValidateFileInstanceDoesNotExist() {
        var mock = mock(File.class);
        when(mock.exists()).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                   () -> FileUtils.validateFileInstance(mock),
                                                   "Expected that IllegalArgumentException exception will be thrown"
                                                  );
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("File does not exist"));
        verify(mock, Mockito.times(1)).exists();
    }

    @Test
    void testGetFileAbsolutePath() {
        var mock = mock(File.class);
        var path = "/absolute/path";

        when(mock.exists()).thenReturn(true);
        when(mock.getAbsolutePath()).thenReturn(path);

        var result = FileUtils.getFileAbsolutePath(mock);

        assertNotNull(result);
        assertEquals(path, result);
        verify(mock, Mockito.times(1)).getAbsolutePath();
    }

    @Test
    void testGetFileNameWithoutExtensionForDirectory() {
        var mock = mock(File.class);
        var fileName = "FileName";

        when(mock.exists()).thenReturn(true);
        when(mock.isDirectory()).thenReturn(true);
        when(mock.getName()).thenReturn(fileName);

        var result = FileUtils.getFileNameWithoutExtension(mock);

        assertNotNull(result);
        assertEquals(fileName, result);
        verify(mock, Mockito.times(1)).isDirectory();
        verify(mock, Mockito.times(1)).getName();
    }

    @Test
    void testGetFileNameWithoutExtensionForFileWithoutExtension() {
        var mock = mock(File.class);
        var fileName = "FileName";

        when(mock.exists()).thenReturn(true);
        when(mock.isDirectory()).thenReturn(false);
        when(mock.getName()).thenReturn(fileName);

        var result = FileUtils.getFileNameWithoutExtension(mock);

        assertNotNull(result);
        assertEquals(fileName, result);
        verify(mock, Mockito.times(1)).isDirectory();
        verify(mock, Mockito.times(1)).getName();
    }

    @Test
    void testGetFileNameWithoutExtensionForFileWithExtension() {
        var mock = mock(File.class);
        var separator = ".";
        var extension = "jpg";
        var fileName = "FileName";
        var fileFullName = fileName + separator + extension;

        when(mock.exists()).thenReturn(true);
        when(mock.isDirectory()).thenReturn(false);
        when(mock.getName()).thenReturn(fileFullName);

        var result = FileUtils.getFileNameWithoutExtension(mock);

        assertNotNull(result);
        assertEquals(fileName, result);
        assertFalse(result.contains(extension));
        verify(mock, Mockito.times(1)).isDirectory();
        verify(mock, Mockito.times(1)).getName();
    }

    @Test
    void testGetFileExtensionForDirectory() {
        var mock = mock(File.class);

        when(mock.exists()).thenReturn(true);
        when(mock.isDirectory()).thenReturn(true);

        var result = FileUtils.getFileExtension(mock);

        assertNotNull(result);
        assertEquals("", result);
        verify(mock, Mockito.times(1)).isDirectory();
        verify(mock, Mockito.times(0)).getName();
    }

    @Test
    void testGetFileExtensionForFileWithoutExtension() {
        var mock = mock(File.class);
        var fileFullName = "FileName";

        when(mock.exists()).thenReturn(true);
        when(mock.isDirectory()).thenReturn(false);
        when(mock.getName()).thenReturn(fileFullName);

        var result = FileUtils.getFileExtension(mock);

        assertNotNull(result);
        assertEquals("", result);
        verify(mock, Mockito.times(1)).isDirectory();
        verify(mock, Mockito.times(1)).getName();
    }

    @Test
    void testGetFileExtensionForFileWithExtensionInUpperCase() {
        var mock = mock(File.class);
        var extension = ".JPG";
        var fileName = "FileName";
        var fileFullName = fileName + extension;

        when(mock.exists()).thenReturn(true);
        when(mock.isDirectory()).thenReturn(false);
        when(mock.getName()).thenReturn(fileFullName);

        var result = FileUtils.getFileExtension(mock);

        assertNotNull(result);
        assertEquals(extension.toLowerCase(), result);
        verify(mock, Mockito.times(1)).isDirectory();
        verify(mock, Mockito.times(1)).getName();
    }

    @Test
    void testGetFileExtensionForFileWithExtensionInLowerCase() {
        var mock = mock(File.class);
        var extension = ".jpg";
        var fileName = "FileName";
        var fileFullName = fileName + extension;

        when(mock.exists()).thenReturn(true);
        when(mock.isDirectory()).thenReturn(false);
        when(mock.getName()).thenReturn(fileFullName);

        var result = FileUtils.getFileExtension(mock);

        assertNotNull(result);
        assertEquals(extension, result);
        verify(mock, Mockito.times(1)).isDirectory();
        verify(mock, Mockito.times(1)).getName();
    }

    @Test
    void testIsFile() {
        var mock = mock(File.class);

        when(mock.exists()).thenReturn(true);
        when(mock.isFile()).thenReturn(true);

        var result = FileUtils.isFile(mock);

        assertTrue(result);
    }

    @Test
    @Disabled
    void getFileCreationTime() {
        try (var mockedStaticFiles = mockStatic(Files.class)) {
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
            mockedStaticFiles.when(() -> Files.readAttributes(mockPath, BasicFileAttributes.class))
                             .thenReturn(mockAttributes);

            var result = FileUtils.getFileCreationTime(mockFile);

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(currentLocalDateTime, result.get());
            verify(mockFile, Mockito.times(1)).exists();
            verify(mockFile, Mockito.times(1)).toPath();
            verify(mockAttributes, Mockito.times(1)).creationTime();
            verify(mockFileTime, Mockito.times(1)).toInstant();
            mockedStaticFiles.verify(() -> Files.readAttributes(mockPath, BasicFileAttributes.class), times(1));
        }
    }

    @Test
    @Disabled
    void getFileModificationTime() {
        var mock = mock(File.class);

        when(mock.exists()).thenReturn(true);

        var result = FileUtils.getFileModificationTime(mock);
    }

    @Test
    @Disabled
    void getFileSize() {
        var mock = mock(File.class);

        when(mock.exists()).thenReturn(true);

        var result = FileUtils.getFileSize(mock);
    }

    @Test
    @Disabled
    void getParentFolders() {
        var mock = mock(File.class);

        when(mock.exists()).thenReturn(true);

        var result = FileUtils.getParentFolders("");
    }

}