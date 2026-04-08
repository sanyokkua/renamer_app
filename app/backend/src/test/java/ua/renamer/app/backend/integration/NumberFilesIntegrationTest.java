package ua.renamer.app.backend.integration;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.SortSource;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.SequenceConfig;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NumberFilesIntegrationTest extends BaseRealMetadataIntegrationTest {

    @Test
    void numberFiles_byName_threePlainFiles_paddedSequence() throws Exception {
        File apple = createPlainFile("apple.txt", "a");
        File banana = createPlainFile("banana.txt", "b");
        File cherry = createPlainFile("cherry.txt", "c");
        SequenceConfig config = SequenceConfig.builder()
                .withSortSource(SortSource.FILE_NAME)
                .withStartNumber(1).withStepValue(1).withPadding(3)
                .withPerFolderCounting(false)
                .build();

        List<RenameResult> results = orchestrator.execute(List.of(apple, banana, cherry),
                TransformationMode.NUMBER_FILES, config, noOpCallback());

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(r -> r.getStatus() == RenameStatus.SUCCESS);
        // Sorted by name: apple→001, banana→002, cherry→003
        assertThat(results.stream().map(RenameResult::getNewFileName).toList())
                .containsExactlyInAnyOrder("001.txt", "002.txt", "003.txt");
        assertDiskStateForBatch(results);
    }

    @Test
    void numberFiles_bySize_threeDifferentSizeFiles_sortsBySize() throws Exception {
        File small = createPlainFile("small.txt", "a".repeat(100));
        File medium = createPlainFile("medium.txt", "b".repeat(200));
        File large = createPlainFile("large.txt", "c".repeat(300));
        SequenceConfig config = SequenceConfig.builder()
                .withSortSource(SortSource.FILE_SIZE)
                .withStartNumber(1).withStepValue(1).withPadding(3)
                .withPerFolderCounting(false)
                .build();

        List<RenameResult> results = orchestrator.execute(List.of(small, medium, large),
                TransformationMode.NUMBER_FILES, config, noOpCallback());

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(r -> r.getStatus() == RenameStatus.SUCCESS);
        assertThat(results.stream().map(RenameResult::getNewFileName).toList())
                .containsExactlyInAnyOrder("001.txt", "002.txt", "003.txt");
        assertDiskStateForBatch(results);
    }

    @Test
    void numberFiles_byContentDate_noExifSortsFirst() throws Exception {
        // photo_no_exif.jpg has no content creation date → sorts to LocalDateTime.MIN → numbered 001
        // photo_1920x1080.jpg has EXIF DateTimeOriginal 2025-06-15 → numbered 002
        File noExif = copyTestResource("media/photo_no_exif.jpg");
        File withExif = copyTestResource("media/photo_1920x1080.jpg");
        SequenceConfig config = SequenceConfig.builder()
                .withSortSource(SortSource.FILE_CONTENT_CREATION_DATETIME)
                .withStartNumber(1).withStepValue(1).withPadding(3)
                .withPerFolderCounting(false)
                .build();

        List<RenameResult> results = orchestrator.execute(List.of(noExif, withExif),
                TransformationMode.NUMBER_FILES, config, noOpCallback());

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getStatus() == RenameStatus.SUCCESS);

        RenameResult noExifResult = results.stream()
                .filter(r -> r.getOriginalFileName().equals("photo_no_exif.jpg"))
                .findFirst().orElseThrow();
        RenameResult withExifResult = results.stream()
                .filter(r -> r.getOriginalFileName().equals("photo_1920x1080.jpg"))
                .findFirst().orElseThrow();

        assertThat(noExifResult.getNewFileName()).isEqualTo("001.jpg");
        assertThat(withExifResult.getNewFileName()).isEqualTo("002.jpg");
        assertDiskStateForBatch(results);
    }

    @Test
    void numberFiles_byImageWidth_sortsByPixelWidth() throws Exception {
        // image_800x600.png is 800px wide → numbered 001
        // photo_1920x1080.jpg is 1920px wide → numbered 002
        File png = copyTestResource("media/image_800x600.png");
        File jpeg = copyTestResource("media/photo_1920x1080.jpg");
        SequenceConfig config = SequenceConfig.builder()
                .withSortSource(SortSource.IMAGE_WIDTH)
                .withStartNumber(1).withStepValue(1).withPadding(3)
                .withPerFolderCounting(false)
                .build();

        List<RenameResult> results = orchestrator.execute(List.of(png, jpeg),
                TransformationMode.NUMBER_FILES, config, noOpCallback());

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getStatus() == RenameStatus.SUCCESS);

        RenameResult pngResult = results.stream()
                .filter(r -> r.getOriginalFileName().equals("image_800x600.png"))
                .findFirst().orElseThrow();
        RenameResult jpegResult = results.stream()
                .filter(r -> r.getOriginalFileName().equals("photo_1920x1080.jpg"))
                .findFirst().orElseThrow();

        assertThat(pngResult.getNewFileName()).isEqualTo("001.png");
        assertThat(jpegResult.getNewFileName()).isEqualTo("002.jpg");
        assertDiskStateForBatch(results);
    }

    @Test
    void numberFiles_byImageHeight_sortsByPixelHeight() throws Exception {
        // image_800x600.png is 600px tall → numbered 001
        // photo_1920x1080.jpg is 1080px tall → numbered 002
        File png = copyTestResource("media/image_800x600.png");
        File jpeg = copyTestResource("media/photo_1920x1080.jpg");
        SequenceConfig config = SequenceConfig.builder()
                .withSortSource(SortSource.IMAGE_HEIGHT)
                .withStartNumber(1).withStepValue(1).withPadding(3)
                .withPerFolderCounting(false)
                .build();

        List<RenameResult> results = orchestrator.execute(List.of(png, jpeg),
                TransformationMode.NUMBER_FILES, config, noOpCallback());

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getStatus() == RenameStatus.SUCCESS);

        RenameResult pngResult = results.stream()
                .filter(r -> r.getOriginalFileName().equals("image_800x600.png"))
                .findFirst().orElseThrow();
        RenameResult jpegResult = results.stream()
                .filter(r -> r.getOriginalFileName().equals("photo_1920x1080.jpg"))
                .findFirst().orElseThrow();

        assertThat(pngResult.getNewFileName()).isEqualTo("001.png");
        assertThat(jpegResult.getNewFileName()).isEqualTo("002.jpg");
        assertDiskStateForBatch(results);
    }

    @Test
    void numberFiles_byCreationDatetime_threeFiles_allSucceed() throws Exception {
        File a = createPlainFile("a.txt", "1");
        File b = createPlainFile("b.txt", "2");
        File c = createPlainFile("c.txt", "3");
        SequenceConfig config = SequenceConfig.builder()
                .withSortSource(SortSource.FILE_CREATION_DATETIME)
                .withStartNumber(1).withStepValue(1).withPadding(3)
                .withPerFolderCounting(false)
                .build();

        List<RenameResult> results = orchestrator.execute(List.of(a, b, c),
                TransformationMode.NUMBER_FILES, config, noOpCallback());

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(r -> r.getStatus() == RenameStatus.SUCCESS);
        assertThat(results.stream().map(RenameResult::getNewFileName).toList())
                .containsExactlyInAnyOrder("001.txt", "002.txt", "003.txt");
        assertDiskStateForBatch(results);
    }

    @Test
    void numberFiles_perFolderCounting_true_eachFolderIndependent() throws Exception {
        File fx = copyTestResourceTo("multi_folder/folder_a/file_x.txt", "folder_a");
        File fy = copyTestResourceTo("multi_folder/folder_a/file_y.txt", "folder_a");
        File fp = copyTestResourceTo("multi_folder/folder_b/file_p.txt", "folder_b");
        File fq = copyTestResourceTo("multi_folder/folder_b/file_q.txt", "folder_b");
        SequenceConfig config = SequenceConfig.builder()
                .withSortSource(SortSource.FILE_NAME)
                .withStartNumber(1).withStepValue(1).withPadding(3)
                .withPerFolderCounting(true)
                .build();

        List<RenameResult> results = orchestrator.execute(List.of(fx, fy, fp, fq),
                TransformationMode.NUMBER_FILES, config, noOpCallback());

        assertThat(results).hasSize(4);
        assertThat(results).allMatch(r -> r.getStatus() == RenameStatus.SUCCESS);
        // Each folder independently numbered 001, 002
        assertThat(results.stream().map(RenameResult::getNewFileName).toList())
                .containsExactlyInAnyOrder("001.txt", "002.txt", "001.txt", "002.txt");
        assertDiskStateForBatch(results);
    }

    @Test
    void numberFiles_perFolderCounting_false_globalCounter() throws Exception {
        File fx = copyTestResourceTo("multi_folder/folder_a/file_x.txt", "folder_a");
        File fy = copyTestResourceTo("multi_folder/folder_a/file_y.txt", "folder_a");
        File fp = copyTestResourceTo("multi_folder/folder_b/file_p.txt", "folder_b");
        File fq = copyTestResourceTo("multi_folder/folder_b/file_q.txt", "folder_b");
        SequenceConfig config = SequenceConfig.builder()
                .withSortSource(SortSource.FILE_NAME)
                .withStartNumber(1).withStepValue(1).withPadding(3)
                .withPerFolderCounting(false)
                .build();

        List<RenameResult> results = orchestrator.execute(List.of(fx, fy, fp, fq),
                TransformationMode.NUMBER_FILES, config, noOpCallback());

        assertThat(results).hasSize(4);
        assertThat(results).allMatch(r -> r.getStatus() == RenameStatus.SUCCESS);
        // Global counter: 001–004
        assertThat(results.stream().map(RenameResult::getNewFileName).toList())
                .containsExactlyInAnyOrder("001.txt", "002.txt", "003.txt", "004.txt");
        assertDiskStateForBatch(results);
    }

    @Test
    void numberFiles_customStartAndStep_nonDefaultValues() throws Exception {
        File a = createPlainFile("a.txt", "1");
        File b = createPlainFile("b.txt", "2");
        File c = createPlainFile("c.txt", "3");
        SequenceConfig config = SequenceConfig.builder()
                .withSortSource(SortSource.FILE_NAME)
                .withStartNumber(10).withStepValue(2).withPadding(0)
                .withPerFolderCounting(false)
                .build();

        List<RenameResult> results = orchestrator.execute(List.of(a, b, c),
                TransformationMode.NUMBER_FILES, config, noOpCallback());

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(r -> r.getStatus() == RenameStatus.SUCCESS);
        assertThat(results.stream().map(RenameResult::getNewFileName).toList())
                .containsExactlyInAnyOrder("10.txt", "12.txt", "14.txt");
        assertDiskStateForBatch(results);
    }

    @Test
    void numberFiles_paddingFour_zeropaddsToFourDigits() throws Exception {
        File a = createPlainFile("a.txt", "1");
        File b = createPlainFile("b.txt", "2");
        File c = createPlainFile("c.txt", "3");
        SequenceConfig config = SequenceConfig.builder()
                .withSortSource(SortSource.FILE_NAME)
                .withStartNumber(1).withStepValue(1).withPadding(4)
                .withPerFolderCounting(false)
                .build();

        List<RenameResult> results = orchestrator.execute(List.of(a, b, c),
                TransformationMode.NUMBER_FILES, config, noOpCallback());

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(r -> r.getStatus() == RenameStatus.SUCCESS);
        assertThat(results.stream().map(RenameResult::getNewFileName).toList())
                .containsExactlyInAnyOrder("0001.txt", "0002.txt", "0003.txt");
        assertDiskStateForBatch(results);
    }

    @Test
    void numberFiles_fullTree_byFileName_globalCounter_allFilesRenamed() throws Exception {
        List<File> allItems = copyAndExpandFullTree();
        assertThat(allItems).isNotEmpty();

        SequenceConfig config = SequenceConfig.builder()
                .withSortSource(SortSource.FILE_NAME)
                .withStartNumber(1).withStepValue(1).withPadding(0)
                .withPerFolderCounting(false)
                .build();

        List<RenameResult> results = orchestrator.execute(allItems, TransformationMode.NUMBER_FILES, config,
                noOpCallback());

        assertThat(results).hasSameSizeAs(allItems);
        assertDiskStateForBatch(results);
    }
}
