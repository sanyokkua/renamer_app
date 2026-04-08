# API Decoupling Verification Report

**Date:** 2026-04-08  
**Status:** ✅ PASS — Fully Decoupled  
**Scope:** Complete codebase audit — backend, core, metadata, utils, and API modules

---

## Executive Summary

This report verifies that the backend API is fully decoupled from JavaFX. The backend operates as a pure Java service with no UI framework dependencies, enabling two critical use cases:

1. **AI Agent Integration** — An external agent can operate the app through the API without requiring a JavaFX runtime
2. **Headless Integration Testing** — Backend tests run in CI/CD without JavaFX initialization or platform setup

**Result:** All checks pass. Zero JavaFX imports, zero UI imports, zero JPMS violations in non-UI modules. The API surface is complete and agent-ready today.

---

## Overall Verdict: PASS

| Check | Result |
|---|---|
| JavaFX imports in non-UI modules | ✅ 0 violations |
| UI module imports in non-UI modules | ✅ 0 violations |
| `requires javafx.*` outside `app/ui` | ✅ 0 violations |
| JavaFX in non-UI Maven POMs | ✅ 0 violations |
| `Platform.runLater` in backend/core | ✅ 0 violations |
| API types using JavaFX Observable types | ✅ 0 violations |
| UI actions with no API method | ✅ 0 gaps |
| Tests requiring JavaFX runtime | ✅ 0 |

---

## Section 1: Hard Dependency Violations

**Result: NONE**

The following grep commands were executed across all non-UI modules and returned zero results:

```bash
grep -rn "import javafx\|import com.sun.javafx" \
  app/api/src app/core/src app/backend/src app/metadata/src app/utils/src \
  --include="*.java"
# Result: (no output)

grep -rn "import ua.renamer.app.ui" \
  app/api/src app/core/src app/backend/src app/metadata/src app/utils/src \
  --include="*.java"
# Result: (no output)

grep -rn "Platform\.runLater\|ObservableList\|SimpleStringProperty\|Property<\|ReadOnlyProperty" \
  app/backend/src app/core/src app/metadata/src app/api/src \
  --include="*.java"
# Result: (no output)
```

**Conclusion:** No module outside `app/ui` has a compile-time or runtime dependency on JavaFX.

---

## Section 2: JPMS Module Graph

**Verification:** Actual module dependencies match intended architecture.

```
app/api         requires: lombok, jspecify, jakarta.annotation
app/utils       requires: api, lombok, slf4j, jspecify, commons-io
app/metadata    requires: api, utils, lombok, slf4j, guice, tika, metadata-extractor, jaudiotagger, guava
app/core        requires: api, utils, lombok, slf4j, guice, jakarta, commons-io, commons-lang3, jspecify
app/backend     requires: api, core, metadata, guice, jakarta, lombok, slf4j, logback, jackson
                # Note: NO requires javafx.* — JPMS enforces FX-free backend at compile time
app/ui          requires: api, backend, core, javafx.base, javafx.controls, javafx.fxml, guice, jakarta, lombok, slf4j, java.desktop
```

**Key Property:** No non-UI module declares `requires javafx.*`. The Java Platform Module System enforces this at compile time — any future JavaFX import in a non-UI module would cause an immediate compilation failure.

---

## Section 3: API Surface Map

### SessionApi

Located: `app/api/src/main/java/ua/renamer/app/api/session/SessionApi.java`

| Method | Parameters | Returns | Thread Safety |
|---|---|---|---|
| `addFiles` | `List<Path> paths` | `CompletableFuture<CommandResult>` | Any thread |
| `removeFiles` | `List<String> fileIds` | `CompletableFuture<CommandResult>` | Any thread |
| `clearFiles` | — | `CompletableFuture<CommandResult>` | Any thread |
| `selectMode` | `TransformationMode mode` | `CompletableFuture<ModeApi<P>>` | Any thread |
| `execute` | — | `TaskHandle<List<RenameSessionResult>>` | Any thread |
| `canExecute` | — | `boolean` | Any thread (synchronized) |
| `snapshot` | — | `SessionSnapshot` | Any thread (AtomicReference) |
| `availableActions` | — | `List<AvailableAction>` | Any thread |
| `getFileMetadata` | `String fileId` | `Optional<FileMetadataDto>` | Any thread (ConcurrentHashMap) |

### ModeApi\<P extends ModeParameters\>

Located: `app/api/src/main/java/ua/renamer/app/api/session/ModeApi.java`

| Method | Parameters | Returns | Purpose |
|---|---|---|---|
| `mode()` | — | `TransformationMode` | Which mode this API manages |
| `currentParameters()` | — | `P` | Immutable current params record |
| `addParameterListener` | `ParameterListener<P>` | `void` | Subscribe to param changes |
| `removeParameterListener` | `ParameterListener<P>` | `void` | Unsubscribe |
| `updateParameters` | `ParamMutator<P> mutator` | `CompletableFuture<ValidationResult>` | Apply wither mutation, validate, notify |
| `resetParameters` | — | `CompletableFuture<Void>` | Reset to defaults, notify |
| `previewSingleFile` | `String exampleName, String exampleExtension` | `Optional<String>` | Synchronous single-name preview |

### FileRenameOrchestrator

Located: `app/api/src/main/java/ua/renamer/app/api/service/FileRenameOrchestrator.java`

| Method | Parameters | Returns | Purpose |
|---|---|---|---|
| `execute` | `List<File>, TransformationMode, Object config, ProgressCallback` | `List<RenameResult>` | Full pipeline (sync) |
| `executeAsync` | same | `CompletableFuture<List<RenameResult>>` | Full pipeline (async) |
| `extractMetadata` | `List<File>, ProgressCallback` | `List<FileModel>` | Phase 1 only |
| `computePreview` | `List<FileModel>, TransformationMode, Object config, ProgressCallback` | `List<PreparedFileModel>` | Phases 2–2.5 (no disk I/O) |

### SettingsService

Located: `app/api/src/main/java/ua/renamer/app/api/settings/SettingsService.java`

| Method | Parameters | Returns |
|---|---|---|
| `getCurrent()` | — | `AppSettings` |
| `load()` | — | `AppSettings` |
| `save` | `AppSettings` | `void` (throws IOException) |
| `getSettingsFilePath()` | — | `Path` |

---

## Section 4: UI Action → API Method Map

All UI actions in `ApplicationMainViewController.java` and 13 mode controllers were traced to their backend API equivalents:

| UI Action | Controller | Handler Method | Backend API Call |
|---|---|---|---|
| Drag-drop files onto table | ApplicationMainViewController | `handleFilesTableViewFilesDroppedEvent()` | `sessionApi.addFiles(List<Path>)` |
| Click Clear button | ApplicationMainViewController | `handleBtnClickedClear()` | `sessionApi.clearFiles()` |
| Click Reload button | ApplicationMainViewController | `handleBtnClickedReload()` | `sessionApi.clearFiles()` + `sessionApi.addFiles()` |
| Mode menu selection | ApplicationMainViewController | `handleModeChanged(TransformationMode)` | `sessionApi.selectMode(mode)` |
| Table row selection | ApplicationMainViewController | `handleFileInTableSelectedEvent()` | `sessionApi.getFileMetadata(fileId)` |
| Click Rename button | ApplicationMainViewController | `handleBtnClickedRename()` | `sessionApi.execute()` |
| Mode parameter change (all 10 modes) | ModeAdd/Remove/Replace/etc. Controller | `initialize()` listeners | `modeApi.updateParameters(mutator)` |
| Mode parameter reset | all mode controllers | (on mode switch) | `modeApi.resetParameters()` |
| Settings save | SettingsDialogController | Result handler | `settingsService.save(updated)` |
| Folder drop expand | FolderDropDialogController | Static `show()` | `folderExpansionService.expand()` (via backend) |

**Conclusion:** Every UI action has a corresponding API method. No controller reads or writes backend state directly — all access is mediated through the API.

---

## Section 5: Data Type Portability

All API models are pure Java with no JavaFX types:

| Model | Type | JavaFX Types? | Contents |
|---|---|---|---|
| `FileModel` | Lombok `@Value` | No | File, boolean, long, String, LocalDateTime, Set, enums |
| `PreparedFileModel` | Lombok `@Value` | No | FileModel, String, boolean |
| `RenameResult` | Lombok `@Value` | No | PreparedFileModel, RenameStatus, String, LocalDateTime |
| `RenameCandidate` | Java record | No | String, Path (java.nio) |
| `RenamePreview` | Java record | No | String fields only |
| `SessionSnapshot` | Java record | No | List<RenameCandidate>, enums (defensive-copied) |
| `FileMetadataDto` | Java record | No | String, long, LocalDateTime, Integer |
| All `*Params` records (10 modes) | sealed Java records | No | primitives, String, enums, Path |
| `CommandResult` | Java record | No | boolean, String |
| `ValidationResult` | Java record | No | boolean, String, String |
| `TaskHandle` | interface | No | uses CompletableFuture |

All enums (`TransformationMode`, `SessionStatus`, `RenameStatus`, `Category`, `DateFormat`, etc.) are pure Java with no JavaFX dependency.

**Conclusion:** All types crossing the API boundary are serializable and framework-agnostic. No type carries JavaFX Observable semantics.

---

## Section 6: Notification Mechanism

**Pattern: Interface-based inversion of control**

The backend notifies the UI of state changes through pure Java interfaces that are implemented by UI-aware wrappers:

```
app/api:      StatePublisher interface (pure Java, no FX imports)
              ↓ implemented by
app/ui:       FxStateMirror (wraps every call in Platform.runLater())
              ↓ bound by
app/ui:       DIUIModule.provideStatePublisher() → returns FxStateMirror instance
              ↓ injected into
app/backend:  RenameSessionService (calls interface methods from state thread)
```

**Key Implementation Details:**

- **StatePublisher** (`app/api/src/main/java/ua/renamer/app/api/session/StatePublisher.java`) — interface with 5 publish methods, zero JavaFX imports. Methods:
  - `publishSessionSnapshot(SessionSnapshot)`
  - `publishFileMetadata(String fileId, FileMetadataDto)`
  - `publishModeParameters(TransformationMode, ModeParameters)`
  - `publishValidationResult(ValidationResult)`
  - `publishTaskProgress(TaskHandle.ProgressEvent)`

- **FxStateMirror** (`app/ui/src/main/java/ua/renamer/app/ui/state/FxStateMirror.java`) — implementation wraps each call in `Platform.runLater()` to marshal state updates to the JavaFX thread

- **DIBackendModule binding** — explicitly does NOT bind `StatePublisher` in the backend (comment in source: "UI provides state publishing; backend is decoupled")

**Progress Reporting:**
- `ProgressCallback` (functional interface in `app/api`) — pure Java, zero JavaFX
- Used by pipeline phases to report extraction/transformation/rename progress

**Mode Parameter Changes:**
- `ModeApi.ParameterListener<P>` (pure Java functional interface) — notifies UI when mode parameters change
- Callbacks are all in `app/api`, no JavaFX

**Task Progress:**
- `TaskHandle.ProgressListener` (pure Java functional interface) — allows any thread to monitor rename progress
- Independent of UI threading model

---

## Section 7: Session State

Session state is accessed through immutable snapshots and explicit API queries:

**Snapshot Access:**
```java
SessionSnapshot snapshot = sessionApi.snapshot();
// Returns:
//   files: List<RenameCandidate> — all files in session
//   activeMode: TransformationMode — current mode (nullable)
//   currentParameters: ModeParameters — current mode params (nullable)
//   preview: List<RenamePreview> — computed preview (may be empty)
//   status: SessionStatus — current session lifecycle status
```

**File Metadata:**
```java
Optional<FileMetadataDto> metadata = sessionApi.getFileMetadata(fileId);
// Returns extracted image/video/audio metadata or empty if not yet extracted
```

**Key Property:** All state mutations require explicit API method calls — there is no shared mutable state exposed directly. The `FxStateMirror` wraps backend-supplied immutable lists in Observable collections, but this is UI-only and one-directional (UI observes backend, not vice versa).

---

## Section 8: Agent-Readiness Matrix

| Capability | API Available? | JavaFX-Free? | Agent-Ready? | Notes |
|---|---|---|---|---|
| Add files to session | YES | YES | ✅ | `sessionApi.addFiles(List<Path>)` |
| Remove specific files | YES | YES | ✅ | `sessionApi.removeFiles(List<String>)` |
| Clear all files | YES | YES | ✅ | `sessionApi.clearFiles()` |
| Set transformation mode | YES | YES | ✅ | `sessionApi.selectMode(mode)` |
| Configure mode parameters | YES | YES | ✅ | `modeApi.updateParameters(mutator)` using wither pattern |
| Reset mode parameters | YES | YES | ✅ | `modeApi.resetParameters()` |
| Preview single filename | YES | YES | ✅ | `modeApi.previewSingleFile(name, ext)` synchronous |
| Get session snapshot | YES | YES | ✅ | `sessionApi.snapshot()` — immutable view of all state |
| Check available actions | YES | YES | ✅ | `sessionApi.availableActions()` — which operations are legal now |
| Get file metadata | YES | YES | ✅ | `sessionApi.getFileMetadata(fileId)` — image/video/audio metadata |
| Execute rename | YES | YES | ✅ | `sessionApi.execute()` returns TaskHandle |
| Monitor rename progress | YES | YES | ✅ | `TaskHandle.addProgressListener(ProgressListener)` — callback-based |
| Cancel rename in progress | YES | YES | ✅ | `TaskHandle.cancel()` — safe cancellation token |
| Load settings | YES | YES | ✅ | `settingsService.load()` — no file I/O on critical path |
| Save settings | YES | YES | ✅ | `settingsService.save(AppSettings)` — throws IOException only |
| Expand folder contents | YES | YES | ✅ | `folderExpansionService.expand(folder, options)` backend service |
| Subscribe to state changes | YES | YES | ✅ | Implement `StatePublisher` interface — pure Java |
| Run pipeline directly | YES | YES | ✅ | `fileRenameOrchestrator.execute(...)` or `computePreview(...)` |

**All 18 tracked capabilities are agent-ready. No gaps found.**

---

## Section 9: Integration Test Feasibility

**Verdict:** ✅ Can write integration tests today. Nothing blocks it.

### Existing Test Infrastructure

Test patterns already in use across the backend:

```java
// From DIBackendModuleTest.java — typical pattern:
Injector injector = Guice.createInjector(
    new DIBackendModule(),          // installs core + metadata transitively
    binder -> binder.bind(StatePublisher.class)
                    .toInstance(mock(StatePublisher.class))
);
SessionApi sessionApi = injector.getInstance(SessionApi.class);
// ✅ Full backend initialized, zero JavaFX imports
```

### Existing Headless Tests

All tests below run without JavaFX initialization:

| Module | Test Classes | Coverage |
|---|---|---|
| `app/backend/src/test/` | 12 test classes | Session API, executor, settings, DI module |
| `app/core/src/test/` | 17 test classes | All 10 transformers, orchestrator, duplicate resolver |
| `app/metadata/src/test/` | 64+ test classes | All media format extractors (JPEG, PNG, MP4, WAV, etc.) |
| **Total** | **93+ test classes** | **Full backend pipeline, zero UI dependencies** |

### Zero JavaFX Test Harness Usage

Verification across all non-UI test files:

```bash
grep -rn "TestFX\|FxToolkit\|JFXPanel\|Platform.startup\|javafx.application" \
  app/api/src/test app/core/src/test app/backend/src/test app/metadata/src/test \
  --include="*.java"
# Result: (no output)
```

No test file imports JavaFX test utilities.

### Running Headless Tests in CI

To run all backend+core+metadata tests in a headless CI environment:

```bash
cd app && mvn test -q -ff -Dai=true -pl app/backend,app/core,app/metadata
```

Optional hardening for air-gapped CI:

```bash
cd app && mvn test -q -ff -Dai=true -Djava.awt.headless=true \
  -pl app/backend,app/core,app/metadata
```

---

## Section 10: Recommended Actions

### Blocking — None

The backend is fully decoupled and testable today. Zero blocking issues.

### Suggestions (Non-Blocking, Low Priority)

**1. CI headless flag** — Add `-Djava.awt.headless=true` to backend/core/metadata test runs in `.github/workflows/maven.yml` to make the isolation explicit and environment-independent.

**2. StatePublisher test utility** — Create a `RecordingStatePublisher` in `app/backend/src/test/` that captures all published events for assertion:

```java
// Pattern for agent/integration tests:
RecordingStatePublisher recorder = new RecordingStatePublisher();
Injector injector = Guice.createInjector(
    new DIBackendModule(),
    binder -> binder.bind(StatePublisher.class).toInstance(recorder)
);
SessionApi sessionApi = injector.getInstance(SessionApi.class);
// Use API...
List<SessionSnapshot> snapshots = recorder.getCapturedSnapshots();
// Assert on state evolution
```

This reusable mock would replace ad-hoc `mock(StatePublisher.class)` across test classes.

**3. Agent integration test module** — Consider adding an `app/agent-it` Maven module with end-to-end integration tests that drive the full pipeline via `SessionApi` only. This validates the API contract is complete as the agent evolves and provides a template for agent developers.

---

## Verification Date and Sign-Off

| Field | Value |
|---|---|
| Report Date | 2026-04-08 |
| Audit Scope | `app/api`, `app/core`, `app/backend`, `app/metadata`, `app/utils`, `app/ui` (non-UI imports only) |
| Java Version | 25 |
| Maven Modules | 7 (api, core, backend, metadata, utils, ui, parent) |
| Test Classes Audited | 93+ |
| Grep Commands Executed | 3 |
| Hard Violations Found | 0 |
| API Methods Mapped | 18 |
| Integration Test Blockers | 0 |

**Status:** ✅ **PASS — Fully Decoupled and Agent-Ready**
