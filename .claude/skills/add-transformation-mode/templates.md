# Add Transformation Mode — Code Templates

Full code templates for each step. See [SKILL.md](SKILL.md) for the procedure and rules.

---

## Step 2: Transformer Class

```java
@Slf4j
@RequiredArgsConstructor
public class MyModeTransformer implements FileTransformationService<MyModeConfig> {

    @Override
    public PreparedFileModel transform(FileModel file, MyModeConfig config) {
        try {
            String newName = applyMyTransformation(file.getName(), config);
            return PreparedFileModel.builder()
                .withOriginalFile(file)
                .withNewName(newName)
                .withNewExtension(file.getExtension())
                .withHasError(false)
                .withErrorMessage(null)
                .withTransformationMeta(buildMeta(config))
                .build();
        } catch (Exception e) {
            log.error("Failed to transform file: {}", file.getName(), e);
            return PreparedFileModel.builder()
                .withOriginalFile(file)
                .withNewName(file.getName())
                .withNewExtension(file.getExtension())
                .withHasError(true)
                .withErrorMessage(e.getMessage())
                .withTransformationMeta(buildMeta(config))
                .build();
        }
    }

    @Override
    public boolean requiresSequentialExecution() {
        // Return true ONLY if files must be processed in order (like ADD_SEQUENCE)
        return false;
    }

    @Override
    public List<PreparedFileModel> transformBatch(List<FileModel> files, MyModeConfig config) {
        return files.stream().map(file -> transform(file, config)).toList();
    }

    private TransformationMetadata buildMeta(MyModeConfig config) {
        return TransformationMetadata.builder()
            .withAppliedMode(TransformationMode.MY_MODE)
            .withAppliedAt(LocalDateTime.now())
            .withConfigurationUsed(Map.of(
                "parameter1", String.valueOf(config.getParameter1()),
                "parameter2", String.valueOf(config.getParameter2())
            ))
            .build();
    }

    private String applyMyTransformation(String name, MyModeConfig config) {
        // Your transformation logic here
        return name;
    }
}
```

---

## Step 4: DI Registration

**4a. Provider method** — add to `DIV2ServiceModule`:

```java
@Provides
@Singleton
MyModeTransformer provideMyModeTransformer() {
    return new MyModeTransformer();
    // If the transformer has dependencies, add them as parameters — Guice injects them:
    // MyModeTransformer provideMyModeTransformer(SomeDep dep) { ... }
}
```

**4b. Registry entry** — add parameter and map entry to `provideTransformerRegistry()`:

```java
@Provides
@Singleton
Map<TransformationMode, FileTransformationService<?>> provideTransformerRegistry(
        AddTextTransformer addText,
        // ... other existing transformers ...
        MyModeTransformer myMode    // add parameter
) {
    return Map.of(
        TransformationMode.ADD_TEXT, addText,
        // ... other existing entries ...
        TransformationMode.MY_MODE, myMode   // add entry
    );
}
```

---

## Step 5: Test Scaffolds

### Unit test

```java
class MyModeTransformerTest {

    private final MyModeTransformer transformer = new MyModeTransformer();

    @ParameterizedTest
    @CsvSource({
        "photo, param1, expected_result",
        "empty, param2, expected_result2",
    })
    void transform_withVariousInputs_shouldProduceExpectedOutput(
            String input, String param1, String expected) {
        FileModel file = FileModel.builder()
            .withName(input).withExtension("jpg").build();
        MyModeConfig config = MyModeConfig.builder()
            .withParameter1(param1).build();

        PreparedFileModel result = transformer.transform(file, config);

        assertThat(result.getNewName()).isEqualTo(expected);
        assertThat(result.isHasError()).isFalse();
    }

    @Test
    void transform_whenInputCausesError_shouldReturnErrorResult() {
        // Test error handling path
    }
}
```

### Integration test

```java
class MyModeTransformationIntegrationTest {

    // Tests the transformer within the full orchestration context
    // Use FileRenameOrchestratorImpl with real transformers, mocked FilesOperations

    @Test
    void orchestrate_withMyModeConfig_shouldProduceExpectedNames(@TempDir Path tempDir) {
        // Arrange: create real test files, build config
        // Act: orchestrator.orchestrate(files, config)
        // Assert: verify RenameResult list
    }
}
```

---

## Step 6: UI Templates

### 6a. FXML view

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.control.TextField?>
<VBox xmlns:fx="http://javafx.com/fxml" spacing="8">
    <TextField fx:id="parameterField" />
</VBox>
```

**No `fx:controller` attribute** — controllers are loaded by Guice via `DIUIModule`.

### 6b. Controller

```java
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeMyModeController extends ModeBaseController implements Initializable {

    @FXML
    private TextField parameterField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parameterField.textProperty().addListener((obs, old, newVal) -> updateCommand());
    }

    @Override
    public void updateCommand() {
        var config = MyModeConfig.builder()
            .withParameter1(parameterField.getText())
            .build();
        // NOTE: V2 UI integration not fully wired yet — see existing ModeControllers for pattern
        // setCommand(commandFromV2Config(config));
    }
}
```

### 6c. Qualifier annotations (add to `InjectQualifiers.java`)

```java
@Qualifier @Retention(RUNTIME) @interface MyModeFxmlLoader {}
@Qualifier @Retention(RUNTIME) @interface MyModeParent {}
@Qualifier @Retention(RUNTIME) @interface MyModeController {}
```

### 6d. DIUIModule — three provider methods

Follow the pattern of existing mode registrations in `DIUIModule`. Add:
1. `@Provides @Singleton @MyModeFxmlLoader FXMLLoader ...`
2. `@Provides @Singleton @MyModeParent Parent ...`
3. `@Provides @Singleton @MyModeController ModeMyModeController ...`
