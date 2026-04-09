package ua.renamer.app.backend.integration;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.AddTextConfig;
import ua.renamer.app.backend.integration.support.GuiceTestHelper;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class SmokeIntegrationTest extends BaseRealMetadataIntegrationTest {

    @Test
    void guiceInjector_startsWithoutException() {
        assertThat(GuiceTestHelper.getInjector()).isNotNull();
    }

    @Test
    void pipeline_plainTextFile_addTextMode_renamesSuccessfully() throws Exception {
        File file = createPlainFile("smoke_test.txt", "hello");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("OK_")
                .withPosition(ItemPosition.BEGIN)
                .build();
        RenameResult result = executeSingle(file, TransformationMode.ADD_TEXT, config);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getNewFileName()).isEqualTo("OK_smoke_test.txt");
        assertRenamed("smoke_test.txt", "OK_smoke_test.txt");
    }

    @Test
    void pipeline_realJpegFile_metadataExtraction_doesNotProduceError() throws Exception {
        File file = copyTestResource("media/photo_1920x1080.jpg");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("x")
                .withPosition(ItemPosition.BEGIN)
                .build();
        RenameResult result = executeSingle(file, TransformationMode.ADD_TEXT, config);
        assertThat(result.getStatus()).isNotEqualTo(RenameStatus.ERROR_EXTRACTION);
    }
}
