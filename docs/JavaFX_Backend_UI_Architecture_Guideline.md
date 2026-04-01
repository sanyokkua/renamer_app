# JavaFX Backend + UI Architecture Guideline

## Agent-Ready Design for the Batch File Renaming Application

**Purpose:** This document defines the architecture, patterns, and rules for redesigning the backend API and UI layer of the JavaFX batch file renaming application. The goals are: strict separation between backend and UI, type-safe and extensible API, thread safety with virtual threads, and zero-refactoring integration of AI agents in the future.

**Audience:** Developers and AI agents performing the redesign work.

---

## 1. Core Architectural Principle

The backend is the single source of truth. The UI is a reactive client. An AI agent is another reactive client. Neither client contains business logic, domain state, or validation rules.

```
┌──────────────┐     ┌──────────────┐
│  JavaFX UI   │     │  AI Agent    │   ← thin adapter clients
│ (Controllers)│     │  (Adapter)   │
└──────┬───────┘     └──────┬───────┘
       │   Command            │  Command
       ▼                      ▼
   ┌──────────────────────────────┐
   │     SessionApi (Facade)      │   ← the single entry point
   │  execute(cmd) / query(...)   │
   ├──────────────────────────────┤
   │  Validation · Business Logic │
   │  State Machine · Events      │   ← all logic lives here
   ├──────────────────────────────┤
   │  Domain Model (observable)   │
   └──────────────────────────────┘
```

**The test that validates the architecture:** close the UI entirely. Open only the agent. Can the agent do everything the user could? If yes, state is in the right place.

---

## 2. Problem Catalog and Solutions

### 2.1 Problem: Logic Trapped in UI Controllers

**Symptom:** `onButtonClick()` methods contain validation, business logic, and persistence. An agent cannot call a button handler.

**Solution:** Introduce a service layer that speaks in domain actions (commands), not UI events. Both the JavaFX controller and a future agent become thin clients of the same services.

```java
// WRONG — logic in the controller
public class ProjectController {
    @FXML void onRenameClick() {
        if (files.isEmpty()) { showError("No files"); return; }
        for (File f : files) {
            String newName = computeNewName(f); // business logic here
            f.renameTo(new File(newName));       // persistence here
        }
    }
}

// RIGHT — controller is a thin adapter
public class ProjectController {
    @FXML void onRenameClick() {
        // Translate UI event → Command, delegate to backend
        session.execute(); // returns TaskHandle<RenameResult>
    }
}
```

### 2.2 Problem: State Trapped in UI Components

**Symptom:** The list of files the user dragged in only exists inside a `ListView`. Selection only exists inside `TableView.getSelectionModel()`. An agent calling `getWorkingFiles()` gets nothing.

**Solution:** All domain state lives in the backend's `RenameSession`. The UI binds to observable properties published by the backend through an FX-safe mirror.

### 2.3 Problem: Commands Need Data from Multiple Sources

**Symptom:** An action requires the currently selected project (UI state), a file path (user must choose via dialog), and a format (user preference). Assembling all this inside the controller couples everything.

**Solution:** Separate what you know from what you need to ask. Use `ParamResolver` — an interface that the UI implements with dialogs and the agent implements with tool call arguments.

### 2.4 Problem: Long-Running Tasks Freeze the UI

**Symptom:** File renaming blocks the FX Application Thread. No progress feedback. No cancellation.

**Solution:** The backend returns a `TaskHandle<T>` with progress listeners, cancellation, and a `CompletableFuture` result. The UI bridges progress to JavaFX components via `Platform.runLater`. The agent awaits the future.

### 2.5 Problem: Multi-Step Confirmations

**Symptom:** "Are you sure? This will overwrite 12 files." The UI shows a dialog. An agent has no dialog.

**Solution:** Abstract interaction as a protocol (`InteractionHandler` interface), not a dialog. The UI implementation shows `Alert` dialogs. The agent implementation auto-confirms safe operations and escalates destructive ones.

### 2.6 Problem: Dynamic Mode Parameters Break Type Safety

**Symptom:** Using `Map<String, Object>` for mode parameters loses compile-time checking. Field name typos cause runtime errors. Agent receives untyped blobs.

**Solution:** Sealed interface hierarchy for mode parameters. Each mode is a Java `record` with typed `withX()` mutators. `ModeDescriptor<P>` carries field metadata used by both UI (to build forms) and agent (to generate tool schemas).

### 2.7 Problem: Multithreading Corrupts Observable State

**Symptom:** A background thread modifies an `ObservableList` bound to a `ListView`. JavaFX doesn't throw an exception — it silently corrupts.

**Solution:** Thread confinement with explicit handoffs. Backend state thread owns all mutations. FX thread owns a mirror copy of observable state. Virtual worker threads operate on isolated data only. No thread ever touches state it doesn't own.

---

## 3. State Ownership Rules

### 3.1 Decision Flowchart

For every piece of state in the application, apply this test:

1. **Does the concept exist without a screen?** (files to rename, selected mode, unsaved edits, rename preview) → **Backend state.** Observable. UI binds to it. Agent reads/writes it.

2. **Could an agent need to read or change this?** (current selection, active filter, sort order) → **Backend state.** Even if it "feels like" UI state.

3. **Is it purely about rendering?** (scroll position, panel width, animation state, divider position, tooltip visibility) → **UI state.** Lives in the controller. Backend never sees it.

### 3.2 Concrete Classification for the Renaming App

**Backend State (owned by `RenameSession`):**

- File list from drag-and-drop or agent file paths
- Which file(s) are selected / focused
- Active rename mode (`ADD_TEXT`, `FIND_REPLACE`, etc.)
- Mode-specific parameters (text to add, regex pattern, etc.)
- Computed rename preview (original name → new name)
- Pending unsaved edits / draft state
- Validation results and error states
- Sort order and filter criteria (if they affect what the agent sees)
- Session status (`EMPTY`, `FILES_LOADED`, `MODE_CONFIGURED`, `READY`, `EXECUTING`)
- Undo/redo stacks

**UI State (owned by controllers):**

- Scroll position within file list
- Which accordion pane is expanded
- Window dimensions and divider positions
- Drag visual feedback (ghost images)
- Tooltip visibility
- Context menu open/closed
- Animation progress

### 3.3 The Drag-and-Drop Flow

This is the canonical example of correct state ownership:

```java
// Step 1: UI catches the OS event → immediately delegates to backend
dropZone.setOnDragDropped(event -> {
    List<File> osFiles = event.getDragboard().getFiles();
    session.addFiles(
        osFiles.stream().map(File::toPath).toList()
    );
    event.setDropCompleted(true);
    event.consume();
});

// Step 2: Backend processes, updates its state, recomputes preview
// (inside RenameSessionService on the backend state thread)

// Step 3: UI reacts — ListView was bound to backend's observable list
fileList.setItems(fxMirror.files()); // one-time binding in initialize()

// Step 4 (future): Agent does the same thing
session.addFiles(List.of(Path.of("/data/report.csv")));
// Same backend state updates. Same preview recomputes.
// If UI is also running, ListView updates automatically.
```

### 3.4 Intermediate / Unsaved State

Intermediate edits (user typed a new prefix but hasn't executed the rename yet) also live in the backend as the current `ModeParameters`. The UI pushes changes via `updateParameters()`, debouncing rapid keystrokes at the UI boundary.

```java
// Debounce in the controller — UI-level optimization
PauseTransition debounce = new PauseTransition(Duration.millis(300));
nameField.textProperty().addListener((obs, old, val) -> {
    debounce.setOnFinished(e ->
        modeApi.updateParameters(p -> p.withText(val))
    );
    debounce.playFromStart();
});
```

---

## 4. Backend API Design

### 4.1 Interface Architecture

Three focused interfaces connected by generics. No fat interface.

```
SessionApi          — files, lifecycle, execution (mode-agnostic)
ModeApi<P>          — typed parameter operations for a specific mode
ModeRegistry        — discovery: what modes exist, their schemas
```

### 4.2 SessionApi — The Main Entry Point

```java
public interface SessionApi {

    // ── File management ──
    CompletableFuture<CommandResult> addFiles(List<Path> paths);
    CompletableFuture<CommandResult> removeFiles(List<String> fileIds);
    CompletableFuture<CommandResult> clearFiles();

    // ── Mode selection (returns a typed API for the mode) ──
    <P extends ModeParameters> CompletableFuture<ModeApi<P>>
        selectMode(ModeDescriptor<P> mode);
    ModeApi<?> currentMode();

    // ── Execution ──
    TaskHandle<RenameResult> execute();
    boolean canExecute();

    // ── Observable state for UI binding (FX-thread safe) ──
    ReadOnlyListProperty<RenameCandidate> files();
    ReadOnlyListProperty<RenamePreview> preview();
    ReadOnlyObjectProperty<SessionStatus> status();

    // ── Discovery ──
    ModeRegistry modeRegistry();
    List<ActionDescriptor> availableActions();

    // ── Session state snapshot (for agent or serialization) ──
    SessionSnapshot snapshot();
}
```

**Key design decisions:**

- All mutating methods return `CompletableFuture`. The UI attaches callbacks with `Platform::runLater`. The agent can `.join()` on a virtual thread. Nobody blocks the FX thread.
- Observable properties (`files()`, `preview()`, etc.) return FX-mirror copies that are safe to bind directly to JavaFX UI components.
- `availableActions()` drives both UI (enabled/disabled buttons) and agent (tool list). Same source of truth.

### 4.3 ModeApi\<P\> — Typed Per-Mode Operations

```java
public interface ModeApi<P extends ModeParameters> {

    ModeDescriptor<P> descriptor();

    // Current parameters — observable, UI can bind
    ReadOnlyObjectProperty<P> parameters();

    // Type-safe update via mutator function
    CompletableFuture<ValidationResult> updateParameters(ParamMutator<P> mutator);

    // Reset to defaults
    void resetParameters();
}

@FunctionalInterface
public interface ParamMutator<P extends ModeParameters> {
    P apply(P current);
}
```

**Usage from UI (fully type-safe, compile-time checked):**

```java
modeApi.updateParameters(p -> p.withText("2024_"));
modeApi.updateParameters(p -> p.withPosition(InsertPosition.PREFIX));
```

**Usage from agent (enters untyped, recovers type through descriptor):**

```java
private <P extends ModeParameters> String updateTyped(
        ModeApi<P> modeApi, Map<String, Object> args) {
    P current = modeApi.parameters().get();
    P updated = modeApi.descriptor().applyChanges(current, args);
    ValidationResult result = modeApi.updateParameters(p -> updated);
    return serialize(result);
}
```

### 4.4 ModeRegistry — Discovery

```java
public interface ModeRegistry {
    List<ModeDescriptor<?>> availableModes();
    <P extends ModeParameters> ModeDescriptor<P> getMode(RenameMode mode);
}
```

---

## 5. Mode Parameters — Type-Safe Sealed Hierarchy

### 5.1 The Sealed Interface

```java
public sealed interface ModeParameters
    permits AddTextParams, FindReplaceParams,
            DateTimeParams, SequenceParams, RegexParams {

    RenameMode mode();
    ValidationResult validate();
}
```

Sealed means every `switch` over `ModeParameters` is checked at compile time. Adding a new mode without handling it everywhere is a compiler error.

### 5.2 Parameter Records with Typed Withers

Each mode's parameters are an immutable Java `record` with `withX()` methods for type-safe mutation.

```java
public record AddTextParams(
    String text,
    InsertPosition position,
    int index,
    boolean applyToExtension
) implements ModeParameters {

    public AddTextParams {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(position, "position");
        if (position != InsertPosition.AT_INDEX) index = 0;
    }

    @Override public RenameMode mode() { return RenameMode.ADD_TEXT; }

    @Override
    public ValidationResult validate() {
        var errors = new ArrayList<FieldError>();
        if (text.isBlank())
            errors.add(FieldError.of("text", "Cannot be empty"));
        if (position == InsertPosition.AT_INDEX && index < 0)
            errors.add(FieldError.of("index", "Must be non-negative"));
        return ValidationResult.of(errors);
    }

    // Typed withers — UI calls these directly
    public AddTextParams withText(String text) {
        return new AddTextParams(text, position, index, applyToExtension);
    }
    public AddTextParams withPosition(InsertPosition position) {
        return new AddTextParams(text, position, index, applyToExtension);
    }
    public AddTextParams withIndex(int index) {
        return new AddTextParams(text, position, index, applyToExtension);
    }
    public AddTextParams withApplyToExtension(boolean apply) {
        return new AddTextParams(text, position, index, apply);
    }
}
```

```java
public record FindReplaceParams(
    String find,
    String replaceWith,
    boolean caseSensitive,
    boolean firstOnly
) implements ModeParameters {

    public FindReplaceParams {
        Objects.requireNonNull(find, "find");
        Objects.requireNonNull(replaceWith, "replaceWith");
    }

    @Override public RenameMode mode() { return RenameMode.FIND_REPLACE; }

    @Override
    public ValidationResult validate() {
        if (find.isBlank())
            return ValidationResult.error("find", "Search text required");
        return ValidationResult.ok();
    }

    // Typed withers
    public FindReplaceParams withFind(String find) {
        return new FindReplaceParams(find, replaceWith, caseSensitive, firstOnly);
    }
    public FindReplaceParams withReplaceWith(String replaceWith) {
        return new FindReplaceParams(find, replaceWith, caseSensitive, firstOnly);
    }
    public FindReplaceParams withCaseSensitive(boolean caseSensitive) {
        return new FindReplaceParams(find, replaceWith, caseSensitive, firstOnly);
    }
    public FindReplaceParams withFirstOnly(boolean firstOnly) {
        return new FindReplaceParams(find, replaceWith, caseSensitive, firstOnly);
    }
}
```

### 5.3 Validation Is in the Parameters, Not the UI

`ModeParameters.validate()` runs in the backend. The UI shows red borders based on `ValidationResult`. The agent gets the same errors as structured data and can fix them. Validation logic is never duplicated.

---

## 6. ModeDescriptor — Single Source of Truth Per Mode

Each mode descriptor is defined once and registered. It carries everything that both the UI and agent need to understand the mode: display metadata, field descriptors, default factory, schema generation, and deserialization.

### 6.1 FieldDescriptor — Typed Field Metadata

```java
public sealed interface FieldDescriptor {

    String name();
    String label();
    String description();
    boolean required();
    Object defaultValue();
    Optional<VisibilityCondition> visibleWhen();

    record StringField(
        String name, String label, String description,
        boolean required, String defaultValue,
        Optional<String> pattern,
        Optional<Integer> maxLength,
        Optional<VisibilityCondition> visibleWhen
    ) implements FieldDescriptor { }

    record EnumField<E extends Enum<E>>(
        String name, String label, String description,
        boolean required, E defaultValue,
        Class<E> enumType,
        Optional<VisibilityCondition> visibleWhen
    ) implements FieldDescriptor {
        public E[] options() { return enumType.getEnumConstants(); }
    }

    record IntegerField(
        String name, String label, String description,
        boolean required, int defaultValue,
        OptionalInt min, OptionalInt max,
        Optional<VisibilityCondition> visibleWhen
    ) implements FieldDescriptor { }

    record BooleanField(
        String name, String label, String description,
        boolean required, boolean defaultValue,
        Optional<VisibilityCondition> visibleWhen
    ) implements FieldDescriptor { }
}

public record VisibilityCondition(String fieldName, Object expectedValue) { }
```

**Why sealed?** The UI can do an exhaustive `switch` over field types to pick the right widget (TextField, ComboBox, Spinner, CheckBox). Adding a new field type forces handling it everywhere.

### 6.2 ModeDescriptor\<P\>

```java
public final class ModeDescriptor<P extends ModeParameters> {

    private final RenameMode mode;
    private final String displayName;
    private final String description;
    private final Class<P> parameterType;
    private final Supplier<P> defaultFactory;
    private final List<FieldDescriptor> fieldDescriptors;

    // For UI: field metadata for building forms
    public List<FieldDescriptor> fields() { ... }

    // For Agent: JSON schema generation
    public String toJsonSchema() { ... }

    // For Agent: deserialize from untyped map
    public P fromMap(Map<String, Object> raw) { ... }

    // For Agent: apply partial changes from untyped map to existing params
    public P applyChanges(P current, Map<String, Object> changes) { ... }

    // Factory for default parameters
    public P createDefaults() { return defaultFactory.get(); }

    // Identity
    public RenameMode mode() { return mode; }
    public Class<P> parameterType() { return parameterType; }
}
```

### 6.3 Mode Registration

All modes are defined as constants and registered in one place.

```java
public class RenameModes {

    public static final ModeDescriptor<AddTextParams> ADD_TEXT =
        ModeDescriptor.<AddTextParams>builder()
            .mode(RenameMode.ADD_TEXT)
            .displayName("Add Text")
            .description("Insert text at a specific position in filenames")
            .parameterType(AddTextParams.class)
            .defaults(() -> new AddTextParams("", InsertPosition.PREFIX, 0, false))
            .field(new FieldDescriptor.StringField(
                "text", "Text to Add", "The text to insert",
                true, "", Optional.empty(), Optional.of(200),
                Optional.empty()))
            .field(new FieldDescriptor.EnumField<>(
                "position", "Insert Position", "Where to place the text",
                true, InsertPosition.PREFIX, InsertPosition.class,
                Optional.empty()))
            .field(new FieldDescriptor.IntegerField(
                "index", "Character Index", "Position to insert at",
                false, 0, OptionalInt.of(0), OptionalInt.empty(),
                Optional.of(new VisibilityCondition(
                    "position", InsertPosition.AT_INDEX))))
            .field(new FieldDescriptor.BooleanField(
                "applyToExtension", "Include Extension",
                "Also modify the file extension",
                false, false, Optional.empty()))
            .build();

    public static final ModeDescriptor<FindReplaceParams> FIND_REPLACE = ...;
    public static final ModeDescriptor<DateTimeParams> DATETIME = ...;
    public static final ModeDescriptor<SequenceParams> SEQUENCE = ...;
    public static final ModeDescriptor<RegexParams> REGEX = ...;

    public static ModeRegistry createRegistry() {
        return ModeRegistry.of(ADD_TEXT, FIND_REPLACE, DATETIME, SEQUENCE, REGEX);
    }
}
```

### 6.4 Adding a New Mode — The Checklist

Adding a new mode touches exactly three things:

1. **Define the parameter record** implementing `ModeParameters`.
2. **Define the descriptor constant** in `RenameModes` with field metadata.
3. **Add to the registry** — one new line in `createRegistry()`.

The UI's generic `ModeViewFactory` automatically builds the correct form from field descriptors. The agent's tool definitions automatically include the new mode and its parameter schema. The `SessionApi` and `ModeApi` interfaces don't change — they're generic over `P`. Only the `RenameEngine` needs a new case to perform the actual rename logic.

---

## 7. Session Service — Command Processing

### 7.1 The RenameSession (Backend State Object)

```java
public class RenameSession {

    private final String sessionId;
    private final List<RenameCandidate> files = new ArrayList<>();
    private RenameMode activeMode;
    private ModeParameters modeParameters;
    private final List<RenamePreview> preview = new ArrayList<>();
    private SessionStatus status = SessionStatus.EMPTY;
}
```

Note: this is a plain Java object with plain collections — NOT `ObservableList`. It is only accessed from the backend state thread. The FX thread has its own observable mirror.

### 7.2 RenameSessionService — All Mutations Go Through Here

```java
public class RenameSessionService {

    private final RenameSession session;
    private final RenameEngine engine;

    public CommandResult addFiles(List<Path> paths) {
        // validate, create candidates, add to session, recompute preview
    }

    public CommandResult removeFiles(List<String> fileIds) { ... }

    public <P extends ModeParameters> ModeApi<P> selectMode(
            ModeDescriptor<P> descriptor) {
        P defaults = descriptor.createDefaults();
        session.setActiveMode(descriptor.mode());
        session.setModeParameters(defaults);
        recomputePreview();
        return new ThreadSafeModeApi<>(descriptor, session, ...);
    }

    public <P extends ModeParameters> ValidationResult updateParameters(
            P updated) {
        ValidationResult v = updated.validate();
        if (!v.isValid()) return v;
        session.setModeParameters(updated);
        recomputePreview();
        return v;
    }

    public TaskHandle<RenameResult> execute() { ... }

    public List<ActionDescriptor> availableActions() {
        // Returns what actions are possible given current state.
        // Drives enabled/disabled buttons in UI and tool list for agent.
    }

    private void recomputePreview() {
        // Called after every state mutation.
        // Produces list of (originalName → newName) with conflict detection.
    }
}
```

**Key invariant:** `recomputePreview()` is called after every mutation. The preview is always consistent with the current state.

---

## 8. Interaction Handling — Confirmations and User Input

### 8.1 The InteractionHandler Contract

```java
public interface InteractionHandler {
    CompletableFuture<Boolean> confirm(ConfirmationRequest request);
    CompletableFuture<String> choose(ChoiceRequest request);
}

public record ConfirmationRequest(
    String title,
    String message,
    Severity severity,    // INFO, WARNING, DESTRUCTIVE
    List<String> details  // "12 files will be overwritten"
) { }
```

### 8.2 UI Implementation

```java
public class UIInteractionHandler implements InteractionHandler {
    @Override
    public CompletableFuture<Boolean> confirm(ConfirmationRequest req) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            Alert alert = new Alert(req.severity() == Severity.DESTRUCTIVE
                ? Alert.AlertType.WARNING : Alert.AlertType.CONFIRMATION);
            alert.setTitle(req.title());
            alert.setContentText(req.message());
            alert.showAndWait()
                .ifPresentOrElse(
                    btn -> future.complete(btn == ButtonType.OK),
                    () -> future.complete(false));
        });
        return future;
    }
}
```

### 8.3 Agent Implementation

```java
public class AgentInteractionHandler implements InteractionHandler {
    @Override
    public CompletableFuture<Boolean> confirm(ConfirmationRequest req) {
        if (req.severity() == Severity.DESTRUCTIVE) {
            return askLlmForConfirmation(req); // or escalate to user
        }
        return CompletableFuture.completedFuture(true); // auto-confirm safe ops
    }
}
```

### 8.4 Parameter Resolution

For commands that need data from multiple sources (UI state, user dialogs, settings), use a `ParamResolver` interface.

```java
public interface ParamResolver {
    CompletableFuture<ResolvedParams> resolve(List<ParamRequest> requests);
}
```

The UI implementation shows FileChoosers and dialogs. The agent implementation reads from LLM tool call arguments. The backend declares what it needs via `ParamRequest` objects — it never calls `showDialog()` itself.

---

## 9. Long-Running Tasks

### 9.1 TaskHandle Contract

```java
public interface TaskHandle<T> {
    String taskId();
    CompletableFuture<T> result();
    void requestCancellation();
    boolean isCancellationRequested();
    void addProgressListener(ProgressListener listener);
}

@FunctionalInterface
public interface ProgressListener {
    void onProgress(double workDone, double totalWork, String message);
}
```

### 9.2 UI Consumption

```java
TaskHandle<RenameResult> handle = session.execute();

handle.addProgressListener((done, total, msg) -> {
    Platform.runLater(() -> {
        progressBar.setProgress(done / total);
        statusLabel.setText(msg);
    });
});

cancelButton.setOnAction(e -> handle.requestCancellation());

handle.result().whenCompleteAsync((result, error) -> {
    // update UI with final result
}, Platform::runLater); // ← ensures FX thread
```

### 9.3 Agent Consumption

```java
TaskHandle<RenameResult> handle = session.execute();

handle.addProgressListener((done, total, msg) ->
    log.debug("Progress: {}/{} - {}", done, total, msg));

return handle.result()
    .thenApply(result -> serialize(result)); // agent awaits future
```

---

## 10. Threading Model

### 10.1 Three Thread Roles

| Thread | Owns | Never Touches |
|--------|------|---------------|
| FX Application Thread | UI nodes, `FxStateMirror` observable collections | `RenameSession` internals |
| Backend State Thread (single) | `RenameSession`, all state mutations, preview computation | JavaFX UI nodes |
| Virtual Worker Threads (many) | Isolated per-file I/O operations | `RenameSession`, UI nodes |

### 10.2 BackendExecutor

```java
public class BackendExecutor {

    // Single-threaded: all state mutations serialized here
    private final ExecutorService stateThread =
        Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "backend-state");
            t.setDaemon(true);
            return t;
        });

    // Virtual threads for I/O-bound work
    private final ExecutorService workerPool =
        Executors.newVirtualThreadPerTaskExecutor();

    public <T> CompletableFuture<T> submitStateChange(Callable<T> mutation) {
        return CompletableFuture.supplyAsync(() -> {
            try { return mutation.call(); }
            catch (Exception e) { throw new CompletionException(e); }
        }, stateThread);
    }

    public <T> CompletableFuture<T> submitWork(Callable<T> work) {
        return CompletableFuture.supplyAsync(() -> {
            try { return work.call(); }
            catch (Exception e) { throw new CompletionException(e); }
        }, workerPool);
    }
}
```

### 10.3 FxStateMirror — Safe Bridge Between Threads

The FX thread must never read the backend session's mutable state directly. Instead, the backend publishes immutable snapshots to a mirror object whose observable collections are only ever mutated on the FX thread.

```java
public class FxStateMirror {

    // Observable state that UI components bind to
    private final ObservableList<RenameCandidate> files =
        FXCollections.observableArrayList();
    private final ObservableList<RenamePreview> preview =
        FXCollections.observableArrayList();
    private final SimpleObjectProperty<SessionStatus> status =
        new SimpleObjectProperty<>(SessionStatus.EMPTY);
    private final SimpleObjectProperty<RenameMode> activeMode =
        new SimpleObjectProperty<>();
    private final SimpleObjectProperty<ModeParameters> modeParameters =
        new SimpleObjectProperty<>();

    // Read-only access for UI binding
    public ReadOnlyListProperty<RenameCandidate> files() { ... }
    public ReadOnlyListProperty<RenamePreview> preview() { ... }
    public ReadOnlyObjectProperty<SessionStatus> status() { ... }

    // Publish methods — called from backend thread, execute on FX thread
    void publishFilesChanged(FilesChangedEvent event) {
        Platform.runLater(() -> {
            files.setAll(event.files());       // atomic replace
            preview.setAll(event.preview());
        });
    }

    void publishParametersChanged(
            ModeParameters params, List<RenamePreview> newPreview) {
        Platform.runLater(() -> {
            modeParameters.set(params);
            preview.setAll(newPreview);
        });
    }
}
```

**Why a mirror instead of just `Platform.runLater` on the backend's lists?** If you mutate the backend's `ObservableList` and then call `Platform.runLater` to "notify" the UI, the FX thread reads the list while the backend thread may still be modifying it. The mirror means the FX thread has its own copy that is only ever touched from the FX thread. No races possible.

### 10.4 The Update Round-Trip

When the user types in a text field, the change flows: FX Thread → Backend State Thread → FX Thread.

```
FX Thread               Backend State Thread      Virtual Threads
─────────               ────────────────────      ───────────────
User types "2024_"
    │
debounce (300ms)
    │
controller calls
modeApi.updateParameters()
    │
    ├─── submits to ────►  receives mutator
    │    stateThread         │
    │                     applies mutator
    │                     validates
    │                     recomputes preview
    │                     creates snapshot
    │                        │
    │    ◄── Platform  ──────┤
    │       .runLater()
    ▼
FxMirror updates
ObservableList
    │
TableView re-renders
preview
```

### 10.5 The Echo Problem

When the UI pushes a change to the backend, the backend updates the FX mirror, which fires a change listener back in the UI. To prevent an infinite loop, use a guard flag or a `TwoWayBridge`:

```java
public class TwoWayBridge<T> {
    private boolean updating = false;

    public void bindBidirectional(
            Property<T> uiProperty,
            String fieldName,
            ModeApi<?> modeApi,
            Function<ModeParameters, T> extractor) {

        // UI → Backend
        uiProperty.addListener((obs, old, val) -> {
            if (updating) return;
            modeApi.updateParameters(p ->
                FieldUpdater.withField(p, fieldName, val));
        });

        // Backend → UI
        modeApi.parameters().addListener((obs, old, params) -> {
            if (params == null) return;
            updating = true;
            try { uiProperty.setValue(extractor.apply(params)); }
            finally { updating = false; }
        });
    }
}
```

### 10.6 Virtual Threads for Batch Rename

File renaming is I/O-bound. Virtual threads let you rename thousands of files concurrently. Use `StructuredTaskScope` for coordination.

```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    List<Subtask<SingleRenameResult>> tasks = plan.stream()
        .map(preview -> scope.fork(() -> renameSingleFile(preview)))
        .toList();
    scope.join();
    // collect results on the backend state thread
}
```

**Rule:** Virtual threads are for leaf I/O, not coordination. They never touch the session.

### 10.7 Threading Invariants (Enforce These)

1. **Thread confinement by role.** The backend state thread is the only thread that reads or writes `RenameSession`. The FX thread is the only thread that reads or writes `FxStateMirror`. Virtual workers operate on isolated data only.

2. **All cross-thread communication is explicit.** `executor.submitStateChange()` → backend thread. `Platform.runLater()` → FX thread. `executor.submitWork()` → virtual threads. If you don't see one of these calls, the code is wrong.

3. **Snapshots cross boundaries, references don't.** Data sent from the backend thread to the FX thread is always an immutable snapshot (`List.copyOf`, new record), never a reference to live mutable state.

4. **The API returns `CompletableFuture`, never blocks.** The UI uses `.whenCompleteAsync(..., Platform::runLater)`. The agent can `.join()` on a virtual thread. Nobody blocks the FX thread.

5. **Virtual threads are for leaf I/O, not coordination.** Fan out with `StructuredTaskScope`. Aggregate results on the backend state thread.

---

## 11. UI Design Rules

### 11.1 Controllers Are Thin Adapters

A JavaFX controller does exactly three things:

1. Translates UI events into commands / mutator calls.
2. Binds UI components to FX-mirror observable properties.
3. Bridges progress/completion callbacks to UI updates via `Platform.runLater`.

A controller never contains business logic, validation, or direct file system operations.

### 11.2 Generic Form Building from FieldDescriptors

The UI can generate mode-specific forms automatically from `ModeDescriptor.fields()`:

```java
public class ModeViewFactory {
    public static <P extends ModeParameters> Node create(ModeApi<P> modeApi) {
        VBox container = new VBox(8);
        for (FieldDescriptor field : modeApi.descriptor().fields()) {
            Node widget = switch (field) {
                case FieldDescriptor.StringField sf  -> createTextField(sf, modeApi);
                case FieldDescriptor.EnumField<?> ef -> createComboBox(ef, modeApi);
                case FieldDescriptor.IntegerField nf -> createSpinner(nf, modeApi);
                case FieldDescriptor.BooleanField bf -> createCheckBox(bf, modeApi);
            };
            container.getChildren().add(widget);
        }
        return container;
    }
}
```

This means adding a new mode with new parameters automatically gets a working UI without writing any new FXML or controller code (unless custom layout is desired).

### 11.3 Mode Switching via Backend

```java
public class MainController {
    void onModeSelected(RenameMode selectedMode) {
        switch (selectedMode) {
            case ADD_TEXT ->
                showModeView(session.selectMode(RenameModes.ADD_TEXT));
            case FIND_REPLACE ->
                showModeView(session.selectMode(RenameModes.FIND_REPLACE));
            // ... exhaustive switch, compiler-checked
        }
    }

    private <P extends ModeParameters> void showModeView(ModeApi<P> modeApi) {
        Node view = ModeViewFactory.create(modeApi);
        modeContainer.getChildren().setAll(view);
    }
}
```

### 11.4 No `javafx.*` Imports in the Backend

Enforce at the Java module level. If the backend module's `module-info.java` does not `require javafx.*`, the compiler prevents accidental coupling.

```java
// module-info.java for the backend module
module app.backend {
    exports com.app.api;
    exports com.app.model;
    exports com.app.service;
    // NO: requires javafx.base;
    // NO: requires javafx.controls;
}
```

---

## 12. Agent Integration (Future)

### 12.1 Agent Adapter

The agent adapter is a thin translation layer that maps LLM tool calls to `SessionApi` / `ModeApi` methods.

```java
public class AgentToolHandler {

    private final SessionApi session;
    private final ModeRegistry registry;

    public String handle(ToolCall call) {
        return switch (call.name()) {
            case "addFiles" -> serialize(
                session.addFiles(call.argList("paths", Path::of)).join());
            case "selectMode" -> {
                RenameMode mode = RenameMode.valueOf(call.arg("mode"));
                ModeDescriptor<?> desc = registry.getMode(mode);
                session.selectMode(desc).join();
                yield serialize(Map.of("mode", mode,
                    "schema", desc.toJsonSchema()));
            }
            case "updateParameters" ->
                updateFromAgent(call.argMap("changes"));
            case "getPreview" ->
                serialize(session.snapshot().preview());
            case "execute" ->
                serialize(session.execute().result().join());
            default -> error("Unknown action: " + call.name());
        };
    }

    // Tool definitions auto-generated from ModeDescriptors
    public List<ToolDefinition> generateToolDefinitions() {
        // Uses registry.availableModes() and descriptor.toJsonSchema()
    }
}
```

### 12.2 Agent Interaction Flow

```
Agent: addFiles(paths: ["/docs/report1.pdf", "/docs/report2.pdf"])
 → "Added 2 files"

Agent: selectMode(mode: "ADD_TEXT")
 → "Mode set to ADD_TEXT" + parameter schema

Agent: updateParameters(changes: {text: "2024_", position: "PREFIX"})
 → "Parameters updated" + preview

Agent: getPreview()
 → [{original: "report1.pdf", newName: "2024_report1.pdf", conflict: false}, ...]

Agent: execute()
 → "Renamed 2 files successfully"
```

### 12.3 Why Zero Backend Changes Are Needed

The agent adapter implements three interfaces: `ParamResolver` (reads from tool call args), `InteractionHandler` (auto-confirms or escalates), `ProgressListener` (logs or ignores). The `SessionApi`, `ModeApi`, `ModeDescriptor`, and all business logic remain unchanged.

---

## 13. CommandResult and Error Handling

### 13.1 Structured Results

```java
public record CommandResult(
    boolean success,
    String message,
    Object payload,
    List<String> suggestedNextActions,
    ValidationResult validation
) {
    public static CommandResult success(String message) { ... }
    public static CommandResult success(String message, Object payload) { ... }
    public static CommandResult error(String message) { ... }
    public static CommandResult validationError(ValidationResult v) { ... }
    public static CommandResult cancelled() { ... }
}
```

### 13.2 ValidationResult

```java
public record ValidationResult(List<FieldError> errors) {
    public boolean isValid() { return errors.isEmpty(); }
    public static ValidationResult ok() { return new ValidationResult(List.of()); }
    public static ValidationResult error(String field, String message) { ... }
}

public record FieldError(String fieldName, String message) { }
```

The UI maps `FieldError` to red borders on the corresponding widget. The agent receives the same structured errors and can retry with corrected values.

---

## 14. Anti-Patterns to Avoid

| Anti-Pattern | Why It's Harmful | Correct Approach |
|---|---|---|
| Business logic in FXML controllers | Agent can't reach it | Service layer with commands |
| State only in UI components (`ListView`, `TextField`) | Agent is blind to it | Backend `RenameSession` as source of truth |
| Returning `Node`, `Alert`, or FX types from services | Agent can't render them | Return `CommandResult`, `ValidationResult` |
| `Map<String, Object>` for parameters | No type safety, runtime errors | Sealed `ModeParameters` records with typed withers |
| Mutating `ObservableList` from background thread | Silent UI corruption | `FxStateMirror` with `Platform.runLater` |
| `CompletableFuture.join()` on FX thread | UI freeze | `.whenCompleteAsync(..., Platform::runLater)` |
| Passing `ProgressBar` to service methods | Backend imports `javafx.*` | `TaskHandle` with `ProgressListener` |
| Designing actions around UI gestures | "handleDragDrop" vs domain intent | Name commands after domain actions: "addFiles" |
| Sharing mutable state between threads | Race conditions | Snapshots cross boundaries, references don't |
| `Platform.runLater` in the backend | Implicit FX dependency | Backend emits events; FX mirror translates |

---

## 15. Module Structure

```
app/
├── app-api/                    # Interfaces and DTOs only
│   ├── SessionApi.java
│   ├── ModeApi.java
│   ├── ModeRegistry.java
│   ├── ModeDescriptor.java
│   ├── FieldDescriptor.java
│   ├── ModeParameters.java     # sealed interface
│   ├── AddTextParams.java
│   ├── FindReplaceParams.java
│   ├── ...Params.java
│   ├── TaskHandle.java
│   ├── CommandResult.java
│   ├── ValidationResult.java
│   ├── InteractionHandler.java
│   └── ParamResolver.java
│
├── app-backend/                # Implementation, no javafx imports
│   ├── RenameSession.java
│   ├── RenameSessionService.java
│   ├── RenameEngine.java
│   ├── BackendExecutor.java
│   ├── RenameModes.java        # mode descriptors registry
│   ├── ThreadSafeSessionApi.java
│   ├── ThreadSafeModeApi.java
│   └── FxStateMirror.java      # only FX dependency: Platform.runLater
│
├── app-ui/                     # JavaFX controllers and views
│   ├── MainController.java
│   ├── FileListController.java
│   ├── ModeViewFactory.java
│   ├── UIInteractionHandler.java
│   ├── UIParamResolver.java
│   └── fxml/
│
└── app-agent/                  # Future: AI agent adapter
    ├── AgentToolHandler.java
    ├── AgentInteractionHandler.java
    └── AgentParamResolver.java
```

**Dependency rules:**

- `app-api` depends on nothing (except `javafx.base` for observable properties).
- `app-backend` depends on `app-api`.
- `app-ui` depends on `app-api` and `app-backend`.
- `app-agent` depends on `app-api` and `app-backend`. Does NOT depend on `app-ui`.
- `app-ui` and `app-agent` never depend on each other.

---

## 16. Summary Checklist for the Redesign

When implementing each feature, verify:

- [ ] All domain state is in `RenameSession`, not in controllers.
- [ ] Every mutation goes through `RenameSessionService` and returns `CommandResult`.
- [ ] Mode parameters are typed records with `withX()` methods and `validate()`.
- [ ] `ModeDescriptor` carries all metadata: field descriptors, defaults, schema generation.
- [ ] UI controllers only translate events → commands and bind to FX-mirror properties.
- [ ] No `javafx.*` imports in the backend module (enforced by `module-info.java`).
- [ ] All mutating API methods return `CompletableFuture`.
- [ ] State mutations happen only on the backend state thread.
- [ ] FX-mirror properties are updated only via `Platform.runLater`.
- [ ] `TaskHandle` is used for long-running operations with progress and cancellation.
- [ ] `InteractionHandler` is used for confirmations, not direct `Alert` calls from services.
- [ ] `availableActions()` is implemented and reflects current session state.
- [ ] `recomputePreview()` is called after every state mutation.
- [ ] Validation errors are structured (`ValidationResult`) and returned to callers.
- [ ] Adding a new mode requires only: new record, new descriptor, registry entry.
