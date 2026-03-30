---
name: add-transformation-mode
description: End-to-end procedure for adding a new V2 file-renaming transformation mode — config model, transformer, enum entry, DI registration, tests, and UI integration.
argument-hint: [ModeName]
disable-model-invocation: true
allowed-tools: Bash(mvn *), Read, Grep, Glob, Edit, Write
---

# Add a V2 Transformation Mode — Renamer App

V2 is the production architecture. Use this skill — not the V1 legacy pattern — for all new modes.

For complete code templates, see [templates.md](templates.md).

---

## Overview of Steps

1. Create Config model (`app/core`)
2. Create Transformer service (`app/core`)
3. Add enum value (`app/core`)
4. Register in DIV2ServiceModule (`app/core`)
5. Add tests
6. Integrate UI (`app/ui`) — optional, do when core is stable

---

## Step 1: Create Config Model

Location: `app/core/src/main/java/ua/renamer/app/core/v2/model/config/`

```java
@Value
@Builder(setterPrefix = "with")
public class MyModeConfig {
    String parameter1;
    int parameter2;
    ItemPosition position;   // Use existing enums where possible
}
```

Rules:
- `@Value` — immutable, no setters
- `@Builder(setterPrefix = "with")` — **required**, default prefix doesn't work in this project
- Simple data holder, no logic

---

## Step 2: Create Transformer

Location: `app/core/src/main/java/ua/renamer/app/core/v2/service/transformation/`

Implements `FileTransformationService<MyModeConfig>`. See [templates.md](templates.md) for the full class template.

Key rules:
- **Never throw to callers** — capture errors in `PreparedFileModel.hasError + errorMessage`
- Stateless and thread-safe (called from parallel virtual threads)
- `@RequiredArgsConstructor` — Guice injects via constructor
- Use `@Slf4j` for logging

---

## Step 3: Add Enum Value

Location: `app/core/src/main/java/ua/renamer/app/core/v2/model/TransformationMode.java`

```java
public enum TransformationMode {
    ADD_TEXT,
    REMOVE_TEXT,
    // ...
    MY_MODE,   // Add here
}
```

---

## Step 4: Register in DIV2ServiceModule

Location: `app/core/src/main/java/ua/renamer/app/core/config/DIV2ServiceModule.java`

Two changes required:
1. Add a `@Provides @Singleton` method for the transformer
2. Add the transformer to the `provideTransformerRegistry()` map parameter list and `Map.of(...)` body

See [templates.md](templates.md) for the full registration code.

Note: `DateTimeTransformer` is the example of a transformer that needs a `@Provides` method with dependencies.

---

## Step 5: Add Tests

Locations:
- Unit: `app/core/src/test/java/.../v2/service/transformation/MyModeTransformerTest.java`
- Integration: `app/core/src/test/java/.../v2/service/integration/MyModeTransformationIntegrationTest.java`

See [templates.md](templates.md) for test scaffolds.

---

## Step 6: UI Integration (when ready)

Six sub-steps required:
1. **FXML view** — `app/ui/src/main/resources/fxml/mode_my_mode.fxml` (no `fx:controller`)
2. **Controller** — extends `ModeBaseController`, `@RequiredArgsConstructor(onConstructor_ = {@Inject})`
3. **Qualifiers** — add 3 annotations to `InjectQualifiers.java` (FxmlLoader, Parent, Controller)
4. **DIUIModule** — add 3 `@Provides @Singleton` methods
5. **ViewNames enum** — add new entry
6. **MainViewControllerHelper** — map mode to controller and view

See [templates.md](templates.md) for full code for each sub-step.

---

## Common Mistakes to Avoid

| Mistake | Consequence |
|---------|-------------|
| Forgetting `setterPrefix = "with"` in `@Builder` | Compile errors on builder usage |
| Throwing exceptions from `transform()` | Crashes pipeline, propagates to UI |
| Using mutable state in the transformer | Breaks parallel virtual thread execution |
| Skipping the transformer registry entry in `DIV2ServiceModule` | Mode silently ignored at runtime |
| Using `fx:controller` in FXML | Controller not injected by Guice, NPE on `@FXML` fields |
| Forgetting qualifier annotations in `InjectQualifiers` | Ambiguous binding exception at startup |
