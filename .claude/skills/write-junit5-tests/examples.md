# JUnit 5 Tests — Code Examples

Complete test class templates for the Renamer App. See [SKILL.md](SKILL.md) for rules.

---

## Unit Test with Mock (Arrange-Act-Assert)

```java
@ExtendWith(MockitoExtension.class)
class AddTextTransformerTest {

    @InjectMocks
    private AddTextTransformer transformer;

    @Test
    void transform_whenAppendText_shouldAppendToFileName() {
        // Arrange
        FileModel file = FileModel.builder()
            .withName("photo")
            .withExtension("jpg")
            .withAbsolutePath("/test/photo.jpg")
            .build();
        AddTextConfig config = AddTextConfig.builder()
            .withText("_backup")
            .withPosition(ItemPosition.END)
            .build();

        // Act
        PreparedFileModel result = transformer.transform(file, config);

        // Assert
        assertThat(result.getNewName()).isEqualTo("photo_backup");
        assertThat(result.isHasError()).isFalse();
        assertThat(result.getOriginalFile()).isEqualTo(file);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void transform_whenTextIsBlankOrNull_shouldReturnOriginalName(String text) {
        FileModel file = FileModel.builder().withName("photo").withExtension("jpg").build();
        AddTextConfig config = AddTextConfig.builder()
            .withText(text)
            .withPosition(ItemPosition.END)
            .build();

        PreparedFileModel result = transformer.transform(file, config);

        assertThat(result.getNewName()).isEqualTo("photo");
    }
}
```

---

## Integration Test (real files)

```java
class FileMetadataExtractorIntegrationTest {

    private static final Path TEST_RESOURCES =
        Path.of("src/test/resources/test-data");

    @Test
    void extract_fromJpegWithExif_shouldReturnCreationDate(@TempDir Path tempDir) throws Exception {
        Path sourceFile = TEST_RESOURCES.resolve("test_jpeg_with_date.jpg");
        assumeTrue(Files.exists(sourceFile), "Test file must exist");

        JpegMetadataExtractor extractor = new JpegMetadataExtractor();
        FileMeta result = extractor.extract(sourceFile.toFile());

        assertThat(result.getCreationDate()).isPresent();
        assertThat(result.getCreationDate().get().getYear()).isEqualTo(2025);
    }
}
```

---

## V2 Transformer Test (@CsvSource)

Test transformers independently — one transformer per test class:

```java
class ChangeCaseTransformerTest {

    private final ChangeCaseTransformer transformer = new ChangeCaseTransformer();

    @ParameterizedTest
    @CsvSource({
        "hello world, UPPER_CASE, HELLO WORLD",
        "HELLO WORLD, LOWER_CASE, hello world",
        "hello world, TITLE_CASE, Hello World",
    })
    void transform_shouldChangeCaseCorrectly(String input, String mode, String expected) {
        FileModel file = FileModel.builder().withName(input).withExtension("txt").build();
        CaseChangeConfig config = CaseChangeConfig.builder()
            .withCaseType(CaseType.valueOf(mode))
            .build();

        PreparedFileModel result = transformer.transform(file, config);

        assertThat(result.getNewName()).isEqualTo(expected);
        assertThat(result.isHasError()).isFalse();
    }
}
```

---

## V2 Orchestrator Test (mocked pipeline)

```java
@ExtendWith(MockitoExtension.class)
class FileRenameOrchestratorImplTest {

    @Mock private ThreadAwareFileMapper fileMapper;
    @Mock private DuplicateNameResolver duplicateResolver;
    @Mock private RenameExecutionService renameService;
    @InjectMocks private FileRenameOrchestratorImpl orchestrator;

    // Test full pipeline phases using mocked collaborators
}
```

---

## V1 Command Test

Use real `FileInformation`, not mocked:

```java
class AddTextPrepareInformationCommandTest {

    @Test
    void execute_shouldPrependTextToAllFiles() {
        FileInformation file = FileInformation.builder()
            .fileName("photo")
            .fileExtension("jpg")
            .build();
        var command = AddTextPrepareInformationCommand.builder()
            .text("prefix_")
            .position(ItemPosition.BEGINNING)
            .build();

        List<FileInformation> result = command.execute(List.of(file), null); // null progress OK

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNewName()).isEqualTo("prefix_photo");
    }
}
```

---

## File System Test (@TempDir)

Always use `@TempDir` — never hardcoded paths:

```java
@Test
void renameFile_shouldMoveFileToNewName(@TempDir Path tempDir) throws Exception {
    Path source = tempDir.resolve("old.txt");
    Files.writeString(source, "content");

    FilesOperations ops = new FilesOperations(tika);
    // ...

    assertThat(tempDir.resolve("new.txt")).exists();
    assertThat(source).doesNotExist();
}
```

---

## Error Path Test

```java
@Test
void transform_whenFileIsDirectory_shouldReturnErrorResult() {
    FileModel dir = FileModel.builder()
        .withName("photos")
        .withIsFile(false)
        .build();

    PreparedFileModel result = transformer.transform(dir, config);

    assertThat(result.isHasError()).isTrue();
    assertThat(result.getErrorMessage()).isNotBlank();
}
```

Use `assertThatThrownBy` **only** when the method contract explicitly throws:

```java
assertThatThrownBy(() -> service.doSomething(null))
    .isInstanceOf(NullPointerException.class);
```
