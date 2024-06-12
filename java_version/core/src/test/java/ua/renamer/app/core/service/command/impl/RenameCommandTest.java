package ua.renamer.app.core.service.command.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.core.service.file.impl.FilesOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RenameCommandTest {

    @Mock
    private FilesOperations filesOperations;

    @Test
    void testCommandExecution() {
        var model1 = mock(RenameModel.class);
        var model2 = mock(RenameModel.class);
        var model3 = mock(RenameModel.class);
        var itemsList = List.of(model1, model2, model3);

        when(filesOperations.renameFile(any(RenameModel.class))).thenReturn(model1)
                                                                .thenReturn(model2)
                                                                .thenReturn(model3);

        var cmd = new RenameCommand(filesOperations);

        var result = cmd.execute(itemsList, null);

        assertNotNull(result);
        assertEquals(itemsList.size(), result.size());
        assertEquals(itemsList.get(0), result.get(0));
        assertEquals(itemsList.get(1), result.get(1));
        assertEquals(itemsList.get(2), result.get(2));
    }

}