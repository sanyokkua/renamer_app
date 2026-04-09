# How to Add a New Transformation Mode

This guide walks through every file you must create or edit to add a fully functional transformation mode — from the
enum value through the UI panel. Follow the steps in order; each step produces output consumed by the next.

## Prerequisites

Before starting, read:

- **[Transformation Modes](../architecture/transformation-modes.md)** — understand what a mode is and how the pipeline
  uses it
- **[Data Models Reference](../architecture/data-models.md)** — understand `FileModel`, `PreparedFileModel`, and the
  `@Builder(setterPrefix = "with")` pattern

You also need the project building cleanly:

```bash
cd app/
../scripts/ai-build.sh
```

---

## Quick Checklist

Track your progress through these 11 items:

- [ ] **Step 1** — Add enum value to `TransformationMode` (api)
- [ ] **Step 2** — Create config class + update `TransformationConfig` permits (api)
- [ ] **Step 3** — Create params record + update `ModeParameters` permits + update converter (api + backend)
- [ ] **Step 4** — Create transformer class (core)
- [ ] **Step 5** — Bind transformer in `DIV2ServiceModule` (core)
- [ ] **Step 6** — Add field + switch arm to `FileRenameOrchestratorImpl` (core)
- [ ] **Step 7** — Create FXML view file (ui)
- [ ] **Step 8** — Create controller class (ui)
- [ ] **Step 9** — Add `ViewNames` entry + register in `DIUIModule` (ui)
- [ ] **Step 10** — Write unit tests (core test)
- [ ] **Step 11** — Run `../scripts/ai-build.sh` and verify

---

## Step-by-Step Guide

The examples below use `MY_MODE` and `MyMode` as placeholders. Replace them with your actual mode name throughout.

---

### Step 1 — Add the enum value

**File:** `app/api/src/main/java/ua/renamer/app/api/model/TransformationMode.java`

Add your new constant to the end of the list:

```java
public enum TransformationMode {
    ADD_TEXT,
    REMOVE_TEXT,
    REPLACE_TEXT,
    CHANGE_CASE,
    ADD_DATETIME,
    ADD_DIMENSIONS,
    NUMBER_FILES,
    ADD_FOLDER_NAME,
    TRIM_NAME,
    CHANGE_EXTENSION,
    MY_MODE,   // add here
}
```

`TransformationMode` drives compile-time exhaustiveness checking across the codebase. Adding the enum value makes the
switch statements in Steps 3 and 6 non-exhaustive — the compiler tells you exactly which arms to add.

---

### Step 2 — Create the config class

**File:** `app/api/src/main/java/ua/renamer/app/api/model/config/MyModeConfig.java`

The config is an immutable value object that carries all pipeline-time parameters for your mode:

```java
package ua.renamer.app.api.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.api.enums.ItemPosition;

@Value
@Builder(setterPrefix = "with")
public class MyModeConfig implements TransformationConfig {

    String myParameter;
    ItemPosition position;

    // Override the generated build() to validate invariants.
    // Throw IllegalArgumentException — ModeParametersConverter catches it.
    public static class MyModeConfigBuilder {
        public MyModeConfig build() {
            if (myParameter == null) {
                throw new IllegalArgumentException("myParameter must not be null");
            }
            if (position == null) {
                throw new IllegalArgumentException("position must not be null");
            }
            return new MyModeConfig(myParameter, position);
        }
    }
}
```

**Critical rules:**

- `@Builder(setterPrefix = "with")` — **required**. Without it, every builder call site fails to compile (
  `withMyParameter(...)` → compile error if prefix is wrong).
- `implements TransformationConfig` — required for the sealed type hierarchy.
- No business logic in the config — it is a pure data holder.

**Also update** `TransformationConfig` to add `MyModeConfig` to its `permits` clause:

**File:** `app/api/src/main/java/ua/renamer/app/api/model/config/TransformationConfig.java`

```java
public sealed interface TransformationConfig
        permits AddTextConfig, RemoveTextConfig, ReplaceTextConfig,
        CaseChangeConfig, DateTimeConfig, ImageDimensionsConfig,
        SequenceConfig, ParentFolderConfig, TruncateConfig,
        ExtensionChangeConfig,
        MyModeConfig {   // add here
}
```

---

### Step 3 — Create the params record

The params record is the UI-layer representation of mode settings. It is distinct from the config because the UI updates
individual fields interactively; the config is assembled once before pipeline execution.

**File:** `app/api/src/main/java/ua/renamer/app/api/session/MyModeParams.java`

```java
package ua.renamer.app.api.session;

import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.TransformationMode;

/**
 * Parameters for the {@link TransformationMode#MY_MODE} transformation mode.
 *
 * @param myParameter the value for my parameter; must not be null
 * @param position    the position; must not be null
 */
public record MyModeParams(String myParameter, ItemPosition position) implements ModeParameters {

    @Override
    public TransformationMode mode() {
        return TransformationMode.MY_MODE;
    }

    @Override
    public ValidationResult validate() {
        if (position == null) {
            return ValidationResult.fieldError("position", "must not be null");
        }
        if (myParameter == null) {
            return ValidationResult.fieldError("myParameter", "must not be null");
        }
        return ValidationResult.valid();
    }

    /** Return a copy with the given parameter value. */
    public MyModeParams withMyParameter(String myParameter) {
        return new MyModeParams(myParameter, this.position);
    }

    /** Return a copy with the given position. */
    public MyModeParams withPosition(ItemPosition position) {
        return new MyModeParams(this.myParameter, position);
    }
}
```

**Also update** the sealed `ModeParameters` interface to add `MyModeParams` to its `permits` clause:

**File:** `app/api/src/main/java/ua/renamer/app/api/session/ModeParameters.java`

```java
public sealed interface ModeParameters
        permits AddTextParams, RemoveTextParams, ReplaceTextParams,
        ChangeCaseParams, SequenceParams, TruncateParams,
        ExtensionChangeParams, DateTimeParams,
        ImageDimensionsParams, ParentFolderParams,
        MyModeParams {   // add here
    // ...
}
```

**Also update** `ModeParametersConverter` to convert your params to the config. Because `ModeParameters` is sealed, the
compiler will flag the missing arm immediately:

**File:** `app/backend/src/main/java/ua/renamer/app/backend/session/ModeParametersConverter.java`

Add the new case to `toConfig()`:

```java
case MyModeParams p ->MyModeConfig.

builder()
        .

withMyParameter(p.myParameter())
        .

withPosition(p.position())
        .

build();
```

---

### Step 4 — Create the transformer

**File:** `app/core/src/main/java/ua/renamer/app/core/service/transformation/MyModeTransformer.java`

The transformer is stateless, receives one `FileModel` and its config, and always returns a `PreparedFileModel` — never
throws:

```java
package ua.renamer.app.core.service.transformation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.TransformationMetadata;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.MyModeConfig;
import ua.renamer.app.core.service.FileTransformationService;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class MyModeTransformer implements FileTransformationService<MyModeConfig> {

    @Override
    public PreparedFileModel transform(FileModel file, MyModeConfig config) {
        if (!file.isFile()) {
            // Propagate upstream extraction error — do not attempt transformation.
            return PreparedFileModel.builder()
                    .withOriginalFile(file)
                    .withNewName(file.getName())
                    .withNewExtension(file.getExtension())
                    .withHasError(true)
                    .withErrorMessage("File metadata extraction failed")
                    .withTransformationMeta(buildMeta(config))
                    .build();
        }
        try {
            String newName = applyTransformation(file.getName(), config);
            return PreparedFileModel.builder()
                    .withOriginalFile(file)
                    .withNewName(newName)
                    .withNewExtension(file.getExtension())
                    .withHasError(false)
                    .withErrorMessage(null)
                    .withTransformationMeta(buildMeta(config))
                    .build();
        } catch (Exception e) {
            log.error("MyModeTransformer failed for file: {}", file.getName(), e);
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

    private String applyTransformation(String name, MyModeConfig config) {
        // Your transformation logic here.
        return name;
    }

    private TransformationMetadata buildMeta(MyModeConfig config) {
        return TransformationMetadata.builder()
                .withAppliedMode(TransformationMode.MY_MODE)
                .withAppliedAt(LocalDateTime.now())
                .withConfigurationUsed(Map.of(
                        "myParameter", String.valueOf(config.getMyParameter()),
                        "position", String.valueOf(config.getPosition())
                ))
                .build();
    }
}
```

**Contract rules:**

- `transform()` must never throw — wrap every error path in `try/catch` and return a `hasError = true` result.
- The transformer is called from virtual threads in parallel. Do not use mutable instance state.
- Preserve the original name and extension on the error path so the file flows correctly through Phase 3.

---

### Step 5 — Bind the transformer

**File:** `app/core/src/main/java/ua/renamer/app/core/config/DIV2ServiceModule.java`

Add one line to `configure()`:

```java
bind(MyModeTransformer .class).

in(Singleton .class);
```

Place it alongside the other transformer bindings:

```java
// Transformers (stateless, can be singletons)
bind(AddTextTransformer .class).

in(Singleton .class);

// ... other existing transformers ...
bind(MyModeTransformer .class).

in(Singleton .class);   // add here
```

---

### Step 6 — Add the orchestrator dispatch case

**File:** `app/core/src/main/java/ua/renamer/app/core/service/impl/FileRenameOrchestratorImpl.java`

**6a.** Add a new `final` field alongside the existing transformer fields:

```java
// Individual transformers — no registry needed with pattern matching
private final AddTextTransformer addTextTransformer;
// ... existing fields ...
private final MyModeTransformer myModeTransformer;   // add here
```

**6b.** Add a `case` arm to the `applyTransformation()` switch. The switch is over `TransformationMode`, so the compiler
enforces exhaustiveness — the missing arm will be a compile error after Step 1:

```java
case MY_MODE ->{
        if(!(config instanceof
MyModeConfig typedConfig)){
        throw new

IllegalArgumentException(
                "MY_MODE requires MyModeConfig, got: "+configClassName);
    }

yield applyTransformationParallel(
        fileModels, myModeTransformer, typedConfig, executor, progressCallback);
}
```

Use `applyTransformationParallel()` for all modes except `NUMBER_FILES`-style modes that require a strict ordering
across files. If your mode assigns per-file sequential values (like counters), use
`sequenceTransformer.transformBatch()` instead and document why.

---

### Step 7 — Create the FXML view

**File:** `app/ui/src/main/resources/fxml/ModeMyMode.fxml`

Use `VBox` as the root. Set `fx:controller` to your controller's fully qualified class name — Guice instantiates the
controller and JavaFX's `FXMLLoader` links the `@FXML` fields:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.control.TextField?>
<?import javafx.scene.layout.VBox?>
<VBox xmlns:fx="http://javafx.com/fxml"
      xmlns="http://javafx.com/javafx"
      fx:controller="ua.renamer.app.ui.controller.mode.impl.ModeMyModeController"
      prefHeight="400.0" prefWidth="600.0" spacing="12">
    <padding>
        <Insets top="12" right="12" bottom="12" left="12"/>
    </padding>

    <VBox spacing="8">
        <Label text="%mode_my_mode_label_parameter" styleClass="label-section"/>
        <TextField fx:id="myParameterField" maxWidth="Infinity"/>
    </VBox>

</VBox>
```

Use `%key` references for all user-visible strings. Add the corresponding key to
`app/ui/src/main/resources/langs/lang.properties` (and translations).

---

### Step 8 — Create the controller

**File:** `app/ui/src/main/java/ua/renamer/app/ui/controller/mode/impl/ModeMyModeController.java`

The controller wires FXML controls to the `ModeApi` parameter update mechanism. Guice injects it; Lombok generates the
`@Inject` constructor:

```java
package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.MyModeParams;
import ua.renamer.app.ui.controller.mode.ModeControllerV2Api;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeMyModeController implements ModeControllerV2Api<MyModeParams>, Initializable {

    @FXML
    private TextField myParameterField;

    private ChangeListener<String> myParameterListener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("initialize()");
    }

    @Override
    public TransformationMode supportedMode() {
        return TransformationMode.MY_MODE;
    }

    @Override
    public void bind(ModeApi<MyModeParams> modeApi) {
        var params = modeApi.currentParameters();

        // Remove old listener before re-binding (mode switches re-call bind()).
        if (myParameterListener != null) {
            myParameterField.textProperty().removeListener(myParameterListener);
        }

        // Initialise controls from current params.
        myParameterField.setText(params.myParameter() != null ? params.myParameter() : "");

        // Wire change listener.
        myParameterListener = (obs, oldVal, newVal) -> {
            log.debug("bind: myParameter changed → {}", newVal);
            modeApi.updateParameters(p -> p.withMyParameter(newVal));
        };
        myParameterField.textProperty().addListener(myParameterListener);
    }
}
```

**Rules:**

- Always remove the old listener at the top of `bind()`. The mode panel is reused across mode switches — a stale
  listener from a previous `bind()` call would fire against the wrong `ModeApi`.
- `bind()` is called on the JavaFX Application Thread. Do not block it.
- If your controller has no Guice-injected dependencies, `@RequiredArgsConstructor` still works — Lombok generates an
  empty constructor with `@Inject`.

---

### Step 9 — Register the view and controller

Three edits, all in the UI module.

**9a. Add to `ViewNames`**

**File:** `app/ui/src/main/java/ua/renamer/app/ui/enums/ViewNames.java`

```java
MODE_MY_MODE("ModeMyMode.fxml"),
```

The enum value name must match the `TransformationMode` name by convention (`MODE_` prefix + the mode name), and the
string must match the FXML filename created in Step 7 exactly.

**9b. Bind the controller class**

**File:** `app/ui/src/main/java/ua/renamer/app/ui/config/DIUIModule.java`

In `bindViewControllers()`, add:

```java
bind(ModeMyModeController .class).

in(Singleton .class);
```

**9c. Register in the mode view registry**

In `provideModeViewRegistry()`, add the controller as a parameter and register it:

```java

@Provides
@Singleton
public ModeViewRegistry provideModeViewRegistry(
        ViewLoaderApi viewLoaderApi,
        ModeAddTextController addText,
        // ... existing parameters ...
        ModeMyModeController myMode)   // add parameter
        throws IOException {

    var registry = new ModeViewRegistry();
    loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_ADD_TEXT, addText);
    // ... existing registrations ...
    loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_MY_MODE, myMode);   // add registration
    return registry;
}
```

`loadAndRegister()` calls `FXMLLoader.load()` once at startup. The loaded `Parent` is reused for every subsequent mode
switch — FXML is not reloaded at runtime.

---

### Step 10 — Write unit tests

**File:** `app/core/src/test/java/ua/renamer/app/core/service/transformation/MyModeTransformerTest.java`

Test the transformer in isolation — no Guice, no Spring, no mocks needed (transformers have no dependencies):

```java
class MyModeTransformerTest {

    private final MyModeTransformer transformer = new MyModeTransformer();

    private FileModel fileModel(String name, String extension) {
        return FileModel.builder()
                .withFile(new File(name + "." + extension))
                .withIsFile(true)
                .withName(name)
                .withExtension(extension)
                .withFileSize(0L)
                .withAbsolutePath("/" + name + "." + extension)
                .withCategory(ua.renamer.app.api.enums.Category.GENERIC)
                .withDetectedMimeType("application/octet-stream")
                .withDetectedExtensions(java.util.Set.of(extension))
                .build();
    }

    @Test
    void transform_withValidInput_shouldProduceExpectedName() {
        FileModel file = fileModel("photo", "jpg");
        MyModeConfig config = MyModeConfig.builder()
                .withMyParameter("value")
                .withPosition(ua.renamer.app.api.enums.ItemPosition.END)
                .build();

        PreparedFileModel result = transformer.transform(file, config);

        assertThat(result.isHasError()).isFalse();
        assertThat(result.getNewName()).isEqualTo("photo_value");   // adjust to actual logic
        assertThat(result.getNewExtension()).isEqualTo("jpg");
    }

    @Test
    void transform_whenFileHasExtractionError_shouldReturnErrorResult() {
        FileModel errorFile = FileModel.builder()
                .withFile(new File("missing.jpg"))
                .withIsFile(false)   // extraction failed
                .withName("missing")
                .withExtension("jpg")
                .withFileSize(0L)
                .withAbsolutePath("/missing.jpg")
                .withCategory(ua.renamer.app.api.enums.Category.GENERIC)
                .withDetectedMimeType("")
                .withDetectedExtensions(java.util.Set.of())
                .build();
        MyModeConfig config = MyModeConfig.builder()
                .withMyParameter("x")
                .withPosition(ua.renamer.app.api.enums.ItemPosition.END)
                .build();

        PreparedFileModel result = transformer.transform(errorFile, config);

        assertThat(result.isHasError()).isTrue();
        assertThat(result.getNewName()).isEqualTo("missing");   // original name preserved
    }

    @Test
    void transform_withNullConfig_shouldReturnErrorResult() {
        FileModel file = fileModel("photo", "jpg");

        PreparedFileModel result = transformer.transform(file, null);

        assertThat(result.isHasError()).isTrue();
    }
}
```

Cover at minimum:

- Happy path with representative inputs (use `@ParameterizedTest` / `@CsvSource` for value variations)
- Error propagation when `FileModel.isFile == false`
- Null config
- Edge cases specific to your logic (empty strings, max-length values, special characters)

See `AddTextTransformerTest` in `app/core/src/test/` for a reference implementation with 25 test cases.

---

### Step 11 — Verify

Run the full pipeline from the `app/` directory:

```bash
../scripts/ai-build.sh
```

This runs in order: compile → Checkstyle → PMD → SpotBugs → all tests. All four must pass before the mode is considered
done.

If the build fails, check these common causes first (see also the Common Mistakes section below).

---

## Testing Your Mode

### Unit tests — what to cover

| Scenario                    | What to assert                                                                  |
|-----------------------------|---------------------------------------------------------------------------------|
| Each valid input variant    | `result.isHasError() == false`, `result.getNewName()` equals expected           |
| `FileModel.isFile == false` | `result.isHasError() == true`, original name preserved                          |
| Null or invalid config      | `result.isHasError() == true` (never a thrown exception)                        |
| Extension preservation      | `result.getNewExtension()` equals the original extension                        |
| Transformation metadata     | `result.getTransformationMeta().getAppliedMode() == TransformationMode.MY_MODE` |

### Running tests

```bash
# All tests
cd app/ && mvn test -q -ff -Dai=true

# Only your transformer test
cd app/ && mvn test -q -ff -Dai=true -Dtest=MyModeTransformerTest

# Single method
cd app/ && mvn test -q -ff -Dai=true -Dtest=MyModeTransformerTest#transform_withValidInput_shouldProduceExpectedName
```

---

## Common Mistakes

| Mistake                                                  | Symptom                                                                                    | Fix                                                                                         |
|----------------------------------------------------------|--------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| `@Builder` without `setterPrefix = "with"`               | Compile error: `cannot find symbol method withMyParameter()` at every builder call site    | Add `setterPrefix = "with"` to the `@Builder` annotation on the config and params           |
| Missing `MyModeConfig` in `TransformationConfig` permits | Compile error: `MyModeConfig is not a permitted subtype of TransformationConfig`           | Add `MyModeConfig` to the `permits` clause in `TransformationConfig.java`                   |
| Missing `MyModeParams` in `ModeParameters` permits       | Compile error: `MyModeParams is not a permitted subtype of ModeParameters`                 | Add `MyModeParams` to the `permits` clause in `ModeParameters.java`                         |
| Missing `ModeParametersConverter` case arm               | Runtime `MatchException` when converting params to config                                  | Add `case MyModeParams p ->` arm to `ModeParametersConverter.toConfig()`                    |
| Missing `DIV2ServiceModule` binding                      | Guice `ConfigurationException` at startup: `No implementation bound for MyModeTransformer` | Add `bind(MyModeTransformer.class).in(Singleton.class);` to `DIV2ServiceModule.configure()` |
| Missing orchestrator field or switch arm                 | Compile error: switch is not exhaustive (missing `MY_MODE`)                                | Add `private final MyModeTransformer` field and `case MY_MODE ->` arm                       |
| Missing `ViewNames` entry                                | `IllegalStateException: Could not find FXMLLoader for MY_MODE` at startup                  | Add `MODE_MY_MODE("ModeMyMode.fxml")` to `ViewNames` enum                                   |
| Mutable state in transformer                             | Data corruption under parallel virtual threads                                             | Move all state into local variables inside `transform()`                                    |
| Throwing from `transform()`                              | Uncaught exception propagates out of the pipeline                                          | Wrap all logic in `try/catch`, return `hasError = true` result                              |

---

## Cross-References

- **[Transformation Modes](../architecture/transformation-modes.md)** — per-mode documentation for all 10 existing modes
- **[Dependency Injection](../architecture/dependency-injection.md)** — how `DIV2ServiceModule` and `DIUIModule` wiring
  works in detail
- **[Data Models Reference](../architecture/data-models.md)** — `FileModel`, `PreparedFileModel`,
  `@Builder(setterPrefix = "with")` pattern
