---
name: add-transformation-mode
description: End-to-end procedure for adding a new V2 file-renaming transformation mode — enum, config, params, transformer, orchestrator dispatch, DI, FXML, controller, tests.
argument-hint: [ModeName]
disable-model-invocation: true
allowed-tools: Bash(mvn *), Read, Grep, Glob, Edit, Write
---

# Add a V2 Transformation Mode — Renamer App

Follow all 11 steps in order. Each step produces output consumed by the next. For full
code templates, see [templates.md](templates.md).

---

## Quick Checklist

- [ ] **Step 1** — Add enum value to `TransformationMode` (`app/api`)
- [ ] **Step 2** — Create config class + update `TransformationConfig` permits (`app/api`)
- [ ] **Step 3** — Create params record + update `ModeParameters` permits + update converter (`app/api` + `app/backend`)
- [ ] **Step 4** — Create transformer class (`app/core`)
- [ ] **Step 5** — Bind transformer in `DIV2ServiceModule` (`app/core`)
- [ ] **Step 6** — Add field + switch arm to `FileRenameOrchestratorImpl` (`app/core`)
- [ ] **Step 7** — Create FXML view file (`app/ui`)
- [ ] **Step 8** — Create controller class (`app/ui`)
- [ ] **Step 9** — Add `ViewNames` entry + register in `DIUIModule` (`app/ui`)
- [ ] **Step 10** — Write unit tests (`app/core` test)
- [ ] **Step 11** — Run `../scripts/ai-build.sh` and verify

---

## Step 1: Add Enum Value

**File:** `app/api/src/main/java/ua/renamer/app/api/model/TransformationMode.java`

Add your constant at the end of the list. This makes the switch statements in Steps 3 and 6
non-exhaustive — the compiler tells you exactly which arms to add.

```java
public enum TransformationMode {
    ADD_TEXT, REMOVE_TEXT, REPLACE_TEXT, CHANGE_CASE, ADD_DATETIME,
    ADD_DIMENSIONS, NUMBER_FILES, ADD_FOLDER_NAME, TRIM_NAME, CHANGE_EXTENSION,
    MY_MODE,   // add here
}
```

---

## Step 2: Create Config Class

**File:** `app/api/src/main/java/ua/renamer/app/api/model/config/MyModeConfig.java`

Immutable value object; implements the sealed `TransformationConfig` interface:

```java
@Value
@Builder(setterPrefix = "with")
public class MyModeConfig implements TransformationConfig {
    String myParameter;
    ItemPosition position;
}
```

Rules:
- `@Value` — immutable, no setters
- `@Builder(setterPrefix = "with")` — **required**
- `implements TransformationConfig` — required for sealed type hierarchy
- No business logic — pure data holder

**Also add** `MyModeConfig` to the `permits` clause of:

**File:** `app/api/src/main/java/ua/renamer/app/api/model/config/TransformationConfig.java`

---

## Step 3: Create Params Record

The params record is the UI-layer representation of mode settings (distinct from the config, which is assembled once before pipeline execution).

**File:** `app/api/src/main/java/ua/renamer/app/api/session/MyModeParams.java`

```java
public record MyModeParams(String myParameter, ItemPosition position) implements ModeParameters {

    @Override
    public TransformationMode mode() { return TransformationMode.MY_MODE; }

    @Override
    public ValidationResult validate() {
        if (position == null) return ValidationResult.fieldError("position", "must not be null");
        if (myParameter == null) return ValidationResult.fieldError("myParameter", "must not be null");
        return ValidationResult.valid();
    }

    public MyModeParams withMyParameter(String v) { return new MyModeParams(v, this.position); }
    public MyModeParams withPosition(ItemPosition v) { return new MyModeParams(this.myParameter, v); }
}
```

**Also add** `MyModeParams` to the `permits` clause of:

**File:** `app/api/src/main/java/ua/renamer/app/api/session/ModeParameters.java`

**Also add** a `case` arm to `ModeParametersConverter.toConfig()`:

**File:** `app/backend/src/main/java/ua/renamer/app/backend/session/ModeParametersConverter.java`

```java
case MyModeParams p -> MyModeConfig.builder()
        .withMyParameter(p.myParameter())
        .withPosition(p.position())
        .build();
```

---

## Step 4: Create Transformer

**File:** `app/core/src/main/java/ua/renamer/app/core/service/transformation/MyModeTransformer.java`

Implements `FileTransformationService<MyModeConfig>`. See [templates.md](templates.md) for
the full class template.

Key rules:
- **Never throw** — wrap all logic in `try/catch`, return `hasError = true` on error
- Check `file.isFile()` first — propagate upstream extraction errors
- Stateless and thread-safe (called from parallel virtual threads)
- `@RequiredArgsConstructor` — Guice injects via constructor

---

## Step 5: Bind Transformer in DIV2ServiceModule

**File:** `app/core/src/main/java/ua/renamer/app/core/config/DIV2ServiceModule.java`

Add one line in `configure()` alongside the other transformer bindings:

```java
bind(MyModeTransformer.class).in(Singleton.class);
```

---

## Step 6: Add Orchestrator Dispatch

**File:** `app/core/src/main/java/ua/renamer/app/core/service/impl/FileRenameOrchestratorImpl.java`

**6a.** Add a field alongside the existing transformer fields:

```java
private final MyModeTransformer myModeTransformer;
```

**6b.** Add a `case` arm to the `applyTransformation()` switch:

```java
case MY_MODE -> {
    if (!(config instanceof MyModeConfig typedConfig)) {
        throw new IllegalArgumentException("MY_MODE requires MyModeConfig, got: " + configClassName);
    }
    yield applyTransformationParallel(fileModels, myModeTransformer, typedConfig, executor, progressCallback);
}
```

Use `applyTransformationParallel()` for all modes except modes that require strict file ordering (like `NUMBER_FILES`).

---

## Step 7: Create FXML View

**File:** `app/ui/src/main/resources/fxml/ModeMyMode.fxml`

Use `VBox` as root with `fx:controller` pointing to your controller class. All user-visible
strings use `%key` references:

```xml
<VBox xmlns:fx="http://javafx.com/fxml" xmlns="http://javafx.com/javafx"
      fx:controller="ua.renamer.app.ui.controller.mode.impl.ModeMyModeController"
      prefHeight="400.0" prefWidth="600.0" spacing="12">
    <TextField fx:id="myParameterField" maxWidth="Infinity"/>
</VBox>
```

Add string keys to `app/ui/src/main/resources/langs/lang.properties`.

---

## Step 8: Create Controller

**File:** `app/ui/src/main/java/ua/renamer/app/ui/controller/mode/impl/ModeMyModeController.java`

Implements `ModeControllerV2Api<MyModeParams>`. See [templates.md](templates.md) for the
full template.

Key rules:
- `implements ModeControllerV2Api<MyModeParams>` — **not** `ModeBaseController`
- The main method is `bind(ModeApi<MyModeParams> modeApi)` — not `updateCommand()`
- Always remove old listeners at the top of `bind()` (mode panels are reused on mode switches)
- `bind()` is called on the JavaFX Application Thread — do not block it

---

## Step 9: Register View and Controller

Three edits in `app/ui`:

**9a.** `app/ui/src/main/java/ua/renamer/app/ui/enums/ViewNames.java` — add entry:
```java
MODE_MY_MODE("ModeMyMode.fxml"),
```

**9b.** `DIUIModule.bindViewControllers()` — add binding:
```java
bind(ModeMyModeController.class).in(Singleton.class);
```

**9c.** `DIUIModule.provideModeViewRegistry()` — add parameter and registration call:
```java
// Add parameter:
ModeMyModeController myMode,
// Add call:
loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_MY_MODE, myMode);
```

---

## Step 10: Write Unit Tests

**File:** `app/core/src/test/java/ua/renamer/app/core/service/transformation/MyModeTransformerTest.java`

No Guice, no mocks needed — transformers have no dependencies. See [templates.md](templates.md)
for a test scaffold with proper `FileModel` builder usage.

Cover at minimum:
- Happy path (valid input → correct new name)
- `FileModel.isFile == false` → `hasError = true`, original name preserved
- Null config → `hasError = true` (never a thrown exception)
- Edge cases: empty strings, boundary values

---

## Step 11: Verify

```bash
cd app/ && ../scripts/ai-build.sh
```

All four stages must pass: compile → Checkstyle → PMD → SpotBugs → tests.

---

## Common Mistakes

| Mistake | Consequence |
|---------|-------------|
| `@Builder` without `setterPrefix = "with"` | Compile error on every builder call site |
| Missing `MyModeConfig` in `TransformationConfig` permits | Compile error: not a permitted subtype |
| Missing `MyModeParams` in `ModeParameters` permits | Compile error: not a permitted subtype |
| Missing `ModeParametersConverter` case arm | Runtime `MatchException` when converting params to config |
| Missing `DIV2ServiceModule` `bind()` call | Guice `ConfigurationException` at startup |
| Missing orchestrator field or switch arm | Compile error: switch not exhaustive |
| Missing `ViewNames` entry | `IllegalStateException` at startup |
| Omitting `fx:controller` in FXML | `@FXML` fields are never injected — NPE at runtime |
| Mutable state in transformer | Data corruption under parallel virtual threads |
| Throwing from `transform()` | Uncaught exception propagates out of the pipeline |
