package ua.renamer.app.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;

import java.io.File;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileInformationUtilsTest {

    @Mock
    private FileInformation fileInformation;

    static Stream<Arguments> testCasesIsFileHasChangedName() {
        return Stream.of(
                // Descriptive test cases for name/extension changes
                arguments("originalName", ".ext", "newName", ".ext", true),
                arguments("originalName", ".ext", "originalName", ".newExt", true),
                arguments("originalName", ".ext", "originalName", "", true),
                arguments("originalName", "", "originalName", ".ext", true),
                // Cases with empty names or extensions
                arguments("", ".ext", "", ".ext", false),
                arguments("originalName", "", "originalName", "", false));
    }

    static Stream<Arguments> testCasesGetFileAbsolutePathWithoutName() {
        return Stream.of(arguments("fileName", ".jpg", "/path/fileName.jpg", "/path/"),
                         arguments("fileName", ".jpg", "/root/folder/fileName.jpg", "/root/folder/"));
    }

    @Test
    void test_getFileFullName_shouldReturnCombinedNameAndExtension() {
        String fileName = "fileName";
        String fileExtension = ".ext";
        String expectedName = fileName + fileExtension;

        when(fileInformation.getFileName()).thenReturn(fileName);
        when(fileInformation.getFileExtension()).thenReturn(fileExtension);

        var result = FileInformationUtils.getFileFullName(fileInformation);

        assertNotNull(result);
        assertEquals(expectedName, result);
        verify(fileInformation, times(1)).getFileName();
        verify(fileInformation, times(1)).getFileExtension();
    }

    @Test
    void test_getFileNewFullName_shouldReturnCombinedNewNameAndExtension() {
        String fileName = "fileName";
        String fileExtension = ".ext";
        String expectedName = fileName + fileExtension;

        when(fileInformation.getNewName()).thenReturn(fileName);
        when(fileInformation.getNewExtension()).thenReturn(fileExtension);

        var result = FileInformationUtils.getFileNewFullName(fileInformation);

        assertNotNull(result);
        assertEquals(expectedName, result);
        verify(fileInformation, times(1)).getNewName();
        verify(fileInformation, times(1)).getNewExtension();
    }

    @ParameterizedTest
    @MethodSource("testCasesIsFileHasChangedName")
    void test_isFileHasChangedName_shouldIdentifyNameOrExtensionChanges(String fileName, String fileExtension,
                                                                        String newFileName, String newFileExtension,
                                                                        boolean expectedResult) {
        var meta = FileInformationMetadata.builder().build();
        var fileInfo = FileInformation.builder()
                                      .originalFile(new File(fileName))
                                      .isFile(true)
                                      .fileSize(10L)
                                      .fileName(fileName)
                                      .newName(newFileName)
                                      .fileExtension(fileExtension)
                                      .newExtension(newFileExtension)
                                      .fileAbsolutePath(fileName)
                                      .fsCreationDate(null)
                                      .fsModificationDate(null)
                                      .metadata(meta)
                                      .build();

        var result = FileInformationUtils.isFileHasChangedName(fileInfo);

        assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @MethodSource("testCasesGetFileAbsolutePathWithoutName")
    void test_getFileAbsolutePathWithoutName_shouldReturnPathExcludingName(String fileName, String ext,
                                                                           String absolutePath, String expected) {
        var fileInfo = FileInformation.builder()
                                      .originalFile(new File(absolutePath))
                                      .isFile(true)
                                      .fileSize(10L)
                                      .fileName(fileName)
                                      .newName(fileName)
                                      .fileExtension(ext)
                                      .newExtension(ext)
                                      .fileAbsolutePath(absolutePath)
                                      .fsCreationDate(null)
                                      .fsModificationDate(null)
                                      .metadata(null)
                                      .build();

        var result = FileInformationUtils.getFileAbsolutePathWithoutName(fileInfo,
                                                                         FileInformationUtils::getFileFullName);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void test_getFileAbsolutePathWithoutName_shouldThrowExceptionForInvalidPath() {
        var fileInfo = FileInformation.builder()
                                      .originalFile(new File("invalidPath")) // Set an invalid path
                                      .isFile(true)
                                      .fileSize(10L)
                                      .fileName("fileName")
                                      .newName("fileName")
                                      .fileExtension("ext")
                                      .newExtension("ext")
                                      .fileAbsolutePath("somePath") // This might not match the actual file path
                                      .fsCreationDate(null)
                                      .fsModificationDate(null)
                                      .metadata(null)
                                      .build();

        // Assert throws with a specific message (optional)
        assertThrows(IllegalArgumentException.class,
                     () -> FileInformationUtils.getFileAbsolutePathWithoutName(fileInfo,
                                                                               FileInformationUtils::getFileFullName),
                     "File absolute path does not match original file path");
    }
}