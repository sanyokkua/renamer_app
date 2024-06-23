package ua.renamer.app.core.service.command.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.mapper.DataMapper;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapFileToFileInformationCommandTest {

    @Mock
    DataMapper<File, FileInformation> dataMapper;

    @Test
    void testFileToFileInformationMapper() {
        var mockedFileInformation1 = mock(FileInformation.class);
        var mockedFileInformation2 = mock(FileInformation.class);
        var mockedFileInformation3 = mock(FileInformation.class);
        var mockedFileInformation4 = mock(FileInformation.class);

        var mockedFile1 = mock(File.class);
        var mockedFile2 = mock(File.class);
        var mockedFile3 = mock(File.class);
        var mockedFile4 = mock(File.class);

        var listOfFiles = List.of(mockedFile1, mockedFile2, mockedFile3, mockedFile4);

        when(dataMapper.map(any(File.class))).thenReturn(mockedFileInformation1)
                                             .thenReturn(mockedFileInformation2)
                                             .thenReturn(mockedFileInformation3)
                                             .thenReturn(mockedFileInformation4);

        var mapper = new MapFileToFileInformationCommand(dataMapper);
        var result = mapper.execute(listOfFiles, null);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains(mockedFileInformation1));
        assertTrue(result.contains(mockedFileInformation2));
        assertTrue(result.contains(mockedFileInformation3));
        assertTrue(result.contains(mockedFileInformation4));
        verify(dataMapper, times(1)).map(mockedFile1);
        verify(dataMapper, times(1)).map(mockedFile2);
        verify(dataMapper, times(1)).map(mockedFile3);
        verify(dataMapper, times(1)).map(mockedFile4);
    }

}