# Java Developer — Code Examples

Reference examples for patterns used in the Renamer App. See [SKILL.md](SKILL.md) for rules.

---

## V2 Transformer Implementation

Full template — use this when implementing a new `FileTransformationService<Config>`:

```java
@Slf4j
@RequiredArgsConstructor
public class MyModeTransformer implements FileTransformationService<MyModeConfig> {

    @Override
    public PreparedFileModel transform(FileModel file, MyModeConfig config) {
        String newName = applyLogic(file.getName(), config);
        return PreparedFileModel.builder()
            .withOriginalFile(file)
            .withNewName(newName)
            .withNewExtension(file.getExtension())
            .withHasError(false)
            .withTransformationMeta(createMetadata(TransformationMode.MY_MODE, config))
            .build();
    }

    @Override
    public boolean requiresSequentialExecution() {
        return false; // true only for ADD_SEQUENCE
    }

    @Override
    public List<PreparedFileModel> transformBatch(List<FileModel> files, MyModeConfig config) {
        return files.stream().map(f -> transform(f, config)).toList();
    }
}
```

---

## Error Path in Transformer

When the transformation cannot proceed, capture in model fields — never throw:

```java
@Override
public PreparedFileModel transform(FileModel file, MyModeConfig config) {
    try {
        String newName = applyLogic(file.getName(), config);
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
```

---

## JavaFX Background Task

Long-running operations must run in a `Task<V>` on a daemon thread, never on the FX Application Thread:

```java
Task<List<RenameResult>> task = new Task<>() {
    @Override
    protected List<RenameResult> call() {
        return orchestrator.orchestrate(files, config);
    }
};
task.setOnSucceeded(e -> Platform.runLater(() -> tableView.getItems().setAll(task.getValue())));
task.setOnFailed(e -> Platform.runLater(() -> showError(task.getException())));
executor.execute(task);
```

---

## DI Provider with Dependencies

When a transformer requires injected collaborators:

```java
// In DIV2ServiceModule:
@Provides
@Singleton
DateTimeTransformer provideDateTimeTransformer(FileMetadataService metadataService) {
    return new DateTimeTransformer(metadataService);
}
```

No-dependency transformers can be added directly to the registry constructor without a `@Provides` method:

```java
@Provides
@Singleton
Map<TransformationMode, FileTransformationService<?>> provideTransformerRegistry(
        AddTextTransformer addText,
        DateTimeTransformer dateTime,
        MyModeTransformer myMode   // injected by Guice
) {
    return Map.of(
        TransformationMode.ADD_TEXT, addText,
        TransformationMode.DATE_TIME, dateTime,
        TransformationMode.MY_MODE, myMode
    );
}
```
