# Add Transformation Mode — Code Templates

Full code templates for each step. See [SKILL.md](SKILL.md) for the procedure and rules.

---

## Step 4: Transformer Class

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

---

## Step 5: DI Registration in DIV2ServiceModule

Add one line in `configure()` alongside the other transformer bindings:

```java
bind(MyModeTransformer.class).in(Singleton.class);
```

---

## Step 6: Orchestrator Dispatch

**6a.** Add transformer field to `FileRenameOrchestratorImpl` (Lombok `@RequiredArgsConstructor` picks it up):

```java
private final MyModeTransformer myModeTransformer;
```

**6b.** Add `case` arm to `applyTransformation()` switch:

```java
case MY_MODE -> {
    if (!(config instanceof MyModeConfig typedConfig)) {
        throw new IllegalArgumentException("MY_MODE requires MyModeConfig, got: " + configClassName);
    }
    yield applyTransformationParallel(fileModels, myModeTransformer, typedConfig, executor, progressCallback);
}
```

---

## Step 8: Controller Class

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

**Key rules:**
- `implements ModeControllerV2Api<MyModeParams>` — **not** `ModeBaseController`
- `bind()` is the entry point — **not** `updateCommand()`
- Always remove old listeners at the top of `bind()` (panel is reused on mode switches)

---

## Step 9: DIUIModule Registration

**9b.** In `bindViewControllers()`:
```java
bind(ModeMyModeController.class).in(Singleton.class);
```

**9c.** In `provideModeViewRegistry(...)` — add parameter and registration:
```java
// Add to parameter list:
ModeMyModeController myMode,
// Add to method body:
loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_MY_MODE, myMode);
```

---

## Step 10: Test Scaffold

```java
class MyModeTransformerTest {

    private final MyModeTransformer transformer = new MyModeTransformer();

    private FileModel fileModel(String name, String extension) {
        return FileModel.builder()
                .withFile(new java.io.File(name + "." + extension))
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
        assertThat(result.getNewName()).isEqualTo("photo_value");  // adjust to actual logic
        assertThat(result.getNewExtension()).isEqualTo("jpg");
    }

    @Test
    void transform_whenFileHasExtractionError_shouldReturnErrorResult() {
        FileModel errorFile = FileModel.builder()
                .withFile(new java.io.File("missing.jpg"))
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
        assertThat(result.getNewName()).isEqualTo("missing");  // original name preserved
    }

    @Test
    void transform_withNullConfig_shouldReturnErrorResult() {
        assertThatCode(() -> transformer.transform(fileModel("photo", "jpg"), null))
                .doesNotThrowAnyException();
        // result must have hasError=true — never a thrown exception
    }
}
```
