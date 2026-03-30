---
name: javafx
description: JavaFX threading, FXML patterns, CSS rules, and Guice DI integration for the Renamer App UI. Use when writing or reviewing any JavaFX controller, FXML file, or CSS stylesheet in app/ui/.
paths: app/ui/**/*.java, app/ui/**/*.fxml, app/ui/**/*.css
allowed-tools: Read, Grep, Glob
---

# JavaFX Desktop Development Standards — Renamer App

## Critical Rules

- MUST execute all scene graph reads and writes exclusively on the FX Application Thread — marshal from background threads via `Platform.runLater()` or `Task` callbacks (`setOnSucceeded`, `setOnFailed`)
- MUST NOT use the `fx:controller` attribute in FXML files — controllers are loaded via Guice's `DIUIModule`, never via FXML's built-in controller factory
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

**FXML loading with Guice** (existing pattern — use `DIUIModule` as reference):

```java
// Controllers are @Provides @Singleton beans in DIUIModule
// FXMLLoader sets no fx:controller — controller is injected by Guice
FXMLLoader loader = injector.getInstance(Key.get(FXMLLoader.class, MyModeFxmlLoader.class));
loader.setLocation(getClass().getResource("/fxml/mode_my_mode.fxml"));
Parent parent = loader.load();
// Controller is retrieved from the loader after load()
```

**Adding a new UI mode requires:**
1. Controller class extending `ModeBaseController` with `@RequiredArgsConstructor(onConstructor_ = {@Inject})`
2. Three qualifier annotations in `InjectQualifiers` (FxmlLoader, Parent, Controller)
3. Three `@Provides @Singleton` methods in `DIUIModule`
4. Entry in `ViewNames` enum
5. Mapping in `MainViewControllerHelper`

---

## FXML Rules

- MUST place all FXML files under `app/ui/src/main/resources/fxml/`
- MUST NOT include `fx:controller` attribute in any FXML file — breaks Guice injection
- FXML files reference `@FXML` fields/methods; these are injected after `loader.load()` returns
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

- MUST flag any FXML file containing `fx:controller`
- MUST flag any `new Thread()` without `setDaemon(true)`
- MUST flag any import from `javax.swing.*` or `org.eclipse.swt.*`
- MUST flag any CSS property without `-fx-` prefix
- MUST flag blocking operations called directly on the FX Application Thread
