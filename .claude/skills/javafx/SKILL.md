---
name: javafx
description: JavaFX threading, FXML patterns, CSS rules, and Guice DI integration for the Renamer App UI. Use when writing or reviewing any JavaFX controller, FXML file, or CSS stylesheet in app/ui/.
paths: app/ui/**/*.java, app/ui/**/*.fxml, app/ui/**/*.css
allowed-tools: Read, Grep, Glob
---

# JavaFX Desktop Development Standards — Renamer App

## Critical Rules

- MUST execute all scene graph reads and writes exclusively on the FX Application Thread — marshal from background threads via `Platform.runLater()` or `Task` callbacks (`setOnSucceeded`, `setOnFailed`)
- MUST set `fx:controller` to the fully qualified controller class name — `ViewLoaderService` routes it through `injector::getInstance` so Guice handles instantiation
- MUST use the `-fx-` prefix for every CSS property in JavaFX stylesheets — W3C property names are silently ignored
- MUST use `javafx.concurrent.Task<V>` for all operations exceeding 100 ms — always on daemon threads
- MUST install a global `Thread.setDefaultUncaughtExceptionHandler` during startup
- MUST NOT use Swing, SWT, or AWT for UI — MUST NOT import `javax.swing.*` or `org.eclipse.swt.*`

---

## Bootstrap & DI Integration

This project uses **Guice 7** (not Spring). The DI startup chain:

```
Guice.createInjector(DIAppModule, DICoreModule, DIUIModule)
```

The JavaFX `Application` subclass is `ua.renamer.app.RenamerApplication`. Launcher is `ua.renamer.app.Launcher`.

**FXML loading with Guice** — `ViewLoaderService` sets `loader.setControllerFactory(injector::getInstance)`, so
Guice resolves the controller named in `fx:controller`. The service is used internally by `DIUIModule.loadAndRegister()`:

```java
// FXML declares the controller class:
// fx:controller="ua.renamer.app.ui.controller.mode.impl.ModeAddTextController"
//
// ViewLoaderService creates the loader and wires Guice:
// loader.setControllerFactory(injector::getInstance)
// loader.setResources(resourceBundle)
//
// DIUIModule.provideModeViewRegistry() loads each mode view at startup:
// loadAndRegister(registry, viewLoaderApi, ViewNames.MODE_ADD_TEXT, addText);
```

**Adding a new UI mode requires:**
1. Controller class implementing `ModeControllerV2Api<MyModeParams>` with `@RequiredArgsConstructor(onConstructor_ = {@Inject})`
2. FXML file with `fx:controller="...ModeMyModeController"`
3. `bind(ModeMyModeController.class).in(Singleton.class)` in `DIUIModule.bindViewControllers()`
4. Add controller parameter + `loadAndRegister()` call in `DIUIModule.provideModeViewRegistry()`
5. Entry in `ViewNames` enum

---

## FXML Rules

- MUST place all FXML files under `app/ui/src/main/resources/fxml/`
- MUST set `fx:controller` to the fully qualified controller class name — Guice resolves it via `loader.setControllerFactory(injector::getInstance)`
- `@FXML` fields/methods are injected after `loader.load()` returns
- Centralize FXML paths in `ViewNames` enum — MUST NOT hardcode `/fxml/...` strings elsewhere

---

## Threading & Concurrency

- **FX Application Thread**: all scene graph reads/writes, `ObservableList` mutations, `TableView`/`ListView` updates
- **Background thread**: single daemon `ExecutorService` (injected via `DIAppModule`) runs `javafx.concurrent.Task<V>` instances
- **V2 parallel phase**: `Executors.newVirtualThreadPerTaskExecutor()` for metadata extraction and physical rename phases

```java
// Correct: update UI from background thread
Platform.runLater(() -> tableView.getItems().setAll(results));

// Correct: bind progress bar
progressBar.progressProperty().bind(task.progressProperty());

// WRONG: scene graph mutation from non-FX thread
new Thread(() -> tableView.getItems().add(item)).start(); // crashes
```

Task with error handling:

```java
Task<List<RenameResult>> task = new Task<>() {
    @Override
    protected List<RenameResult> call() {
        return orchestrator.orchestrate(files, config);
    }
};
task.setOnSucceeded(e -> updateTable(task.getValue()));
task.setOnFailed(e -> showError(task.getException()));
executor.execute(task);
```

---

## Data Binding

- Use `SimpleStringProperty`, `SimpleBooleanProperty`, `SimpleObjectProperty` for model fields bound to UI
- Use `FXCollections.observableArrayList()` for `TableView`/`ListView` data sources
- Mutate `ObservableList` only on the FX Application Thread

---

## CSS Styling

- MUST use `-fx-` prefix for all CSS properties

| Standard CSS | JavaFX CSS |
|---|---|
| `background-color` | `-fx-background-color` |
| `font-family` | `-fx-font-family` |
| `font-size` | `-fx-font-size` |
| `padding` | `-fx-padding` |
| `color` (text) | `-fx-text-fill` |
| `border-radius` | `-fx-background-radius` / `-fx-border-radius` |
| `cursor` | `-fx-cursor` |
| `opacity` | `-fx-opacity` |

- Place CSS files under `app/ui/src/main/resources/styles/`
- Apply stylesheets at `Scene` level, not on individual nodes
- MUST NOT set `-fx-focus-color: transparent`

---

## Accessibility

- Every interactive control must be reachable via Tab / Shift+Tab
- Set `accessibleText` on image-only buttons and `ImageView` elements
- WCAG 2.1 AA contrast: 4.5:1 for normal text, 3:1 for large text

---

## Crash Handling

```java
// In Application.start():
Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
    log.error("Uncaught exception on thread {}", thread.getName(), throwable);
    Platform.runLater(() -> showErrorDialog(throwable));
});
```

---

## Enforcement

- MUST flag any FXML file missing `fx:controller` (all mode views must declare their controller class)
- MUST flag any `new Thread()` without `setDaemon(true)`
- MUST flag any import from `javax.swing.*` or `org.eclipse.swt.*`
- MUST flag any CSS property without `-fx-` prefix
- MUST flag blocking operations called directly on the FX Application Thread
