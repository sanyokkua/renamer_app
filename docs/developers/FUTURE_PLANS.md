---
title: Future Plans for Renamer App
description: 6 planned features for enhancement — file removal, context menu integration, extension filtering, DateTime configuration, audio metadata transformation, and format naming mode
last_updated: 2026-04-09
---

# Future Plans for Renamer App

This document describes six planned features for the Renamer App, a JavaFX 25 desktop application for batch file renaming with metadata extraction. Each feature is scoped in terms of the modules affected, the real classes and files involved, and an honest assessment of implementation complexity.

## Complexity Scale

Complexity ratings are:

- **Low**: Contained to a single module (UI or backend), no new APIs, leverages existing patterns.
- **Medium**: Touches multiple modules or introduces moderate new logic; existing patterns apply.
- **Medium–High**: Involves new data models or sealed interface updates; requires cross-module coordination.
- **High**: Adds a complete new transformation mode or subsystem; affects api, core, ui, and possibly metadata modules.

---

## Feature Summary

| ID | Feature | Complexity |
|----|---------|------------|
| F1 | Remove individual files from the files list | Medium |
| F2 | Open containing folder from right-click context menu | Low |
| F3 | File type/extension filter in Settings | Medium–High |
| F4 | DateTime mode: configurable fallback source order | Medium |
| F5 | Audio metadata transformation mode | High |
| F6 | Format mode (fixed base name + sequential index) | High |

---

## F1 — Remove Individual Files from the Files List

### Description

Allow users to remove individual files from the loaded files list without clearing the entire list. A remove button in the table row lets users exclude files added by mistake.

### Use Case

A user drops a folder with 50 files. Three files are wrong — currently they must clear everything and re-add the folder. With this feature they remove just the three files without disrupting the rename preview for the remaining 47.

### Implementation Scope

**UI layer:**
- `app/ui/src/main/resources/fxml/ApplicationMainView.fxml` — add a 5th table column (remove button column) alongside the existing file name, new name, status, and size columns.
- `app/ui/src/main/java/ua/renamer/app/ui/controller/ApplicationMainViewController.java` — add button cell factory in the `configureFilesTableViewColumns()` method; implement button click handler `handleRemoveFileFromList(UUID fileId)` that calls the backend service and updates the UI lists.

**State synchronization:**
- `app/ui/src/main/java/ua/renamer/app/ui/state/FxStateMirror.java` — add `removeFile(UUID fileId)` method that atomically updates `previewList` and `filesList`, then calls `publishFilesChanged()` and `publishPreviewChanged()` to notify all observers on the FX thread.

**Backend session service:**
- `app/backend` — extend the `SessionApi` interface and its implementation (`SessionServiceImpl` or equivalent) to expose `removeFile(UUID fileId)` API; ensure the backend session stays consistent with the UI list.

**Session state guard:**
- Removal must be blocked while a rename operation is in progress; check the session status before allowing file removal.

### Complexity

**Medium.** The UI column addition is straightforward. The tricky parts are thread-safe `FxStateMirror` mutation (must coordinate with `Platform.runLater()` or the established `publishFilesChanged()` pattern), ensuring backend and UI state stay in sync, and guarding against removal during active rename operations.

---

## F2 — Open Containing Folder from Right-Click Context Menu

### Description

Add a right-click context menu on table rows that opens the file's parent directory in the OS file manager (Finder on macOS, Explorer on Windows, file manager on Linux).

### Use Case

A user drops a folder tree and sees a file in the results. They want to navigate to the exact folder containing that file — especially useful when the folder has subdirectories and the user needs to verify the file location before rename.

### Implementation Scope

**UI layer:**
- `app/ui/src/main/java/ua/renamer/app/ui/controller/ApplicationMainViewController.java` — the row factory at lines 195–216 (in `configureFilesTableView()`) already sets row-level behavior. Add a `ContextMenu` with an "Open Containing Folder" `MenuItem` here.
- The `candidatesByFileId` map (keyed by `UUID fileId`) gives access to `RenameCandidate.path`; call `.getParent()` to get the containing directory.

**Cross-platform execution:**
- Extract or reuse the existing `openDirectory(Path)` virtual-thread ProcessBuilder pattern already present in `SettingsDialogController` (lines 235–253). The pattern uses:
  - macOS: `open <dir>`
  - Linux: `xdg-open <dir>`
  - Windows: `explorer <dir>`
  - Detected via `System.getProperty("os.name")`

No backend or API changes required.

### Complexity

**Low.** The OS-open pattern is already implemented and proven in `SettingsDialogController`. The row factory and context menu infrastructure already exist in JavaFX. This is purely additive UI work with no state changes.

---

## F3 — File Type/Extension Filter in Settings

### Description

A new Settings section where users configure an allowlist of file extensions. When set, only files matching the configured extensions are added to the files list during drag-drop or folder traversal; all others are silently skipped.

### Use Case

A photographer drops a folder containing RAW, JPG, XMP, and various sidecar files. They want only `.jpg` and `.png` to be processed. Currently they must manually remove unwanted files; with this feature the filter handles it automatically.

### Implementation Scope

**API module:**
- `app/api/src/main/java/ua/renamer/app/api/settings/AppSettings.java` — add `Set<String> allowedExtensions` field (empty set = no filter, accept all); update `AppSettings.defaults()` to return empty set; bump `SETTINGS_VERSION` in `AppDefaults` to trigger migration on first read.
- `app/api/src/main/java/ua/renamer/app/api/settings/AppDefaults.java` — increment `SETTINGS_VERSION`.

**Backend settings layer:**
- `app/backend/src/main/java/ua/renamer/app/backend/settings/SettingsServiceImpl.java` — implement JSON schema migration handling for the new field; provide graceful fallback to empty set when reading old settings files.

**UI Settings dialog:**
- `app/ui/src/main/resources/fxml/SettingsDialog.fxml` — add a new "File Filters" section (consider sidebar tab or new section in General).
- `app/ui/src/main/java/ua/renamer/app/ui/controller/SettingsDialogController.java` — load and save `allowedExtensions`; UI input can be a tag-style TextField (comma-separated extensions, normalized to lowercase without dots).

**File ingestion path:**
- Trace `handleFilesTableViewFilesDroppedEvent()` in `ApplicationMainViewController` through to the backend session service that walks directories. Apply the filter at the point files are collected/accepted. Extension matching must be case-insensitive.

### Complexity

**Medium–High.** The settings model and JSON persistence changes are well-understood (same pattern as existing fields like `loggingEnabled`). The migration handling adds moderate work. The hardest part is locating and cleanly modifying the file ingestion/directory-walk path to apply the filter, and designing a usable multi-value extension input in the settings dialog.

---

## F4 — DateTime Mode: Configurable Fallback Source Order

### Description

Extend the Add Date & Time mode to let users define an ordered list of datetime sources to try as fallbacks, rather than the current binary on/off fallback flag. The transformer tries each source in the user-defined order and uses the first one that yields a valid datetime.

### Use Case

A user has photos from an old camera where `CONTENT_CREATION_DATE` is often missing. They want to fall back to `FILE_CREATION_DATE` first, then `FILE_MODIFICATION_DATE`, and only use a custom date as a last resort — currently this ordering is hardcoded and not configurable.

### Implementation Scope

**API module:**
- New enum `FallbackDateTimeSource` in `app/api/src/main/java/ua/renamer/app/api/enums/` — values: `FILE_CREATION_DATE`, `FILE_MODIFICATION_DATE`, `CONTENT_CREATION_DATE`, `CUSTOM_DATE`.
- `app/api/src/main/java/ua/renamer/app/api/model/config/DateTimeConfig.java` — add `List<FallbackDateTimeSource> fallbackOrder` field. The existing `useFallbackDateTime` boolean becomes derived (non-empty list = fallback enabled); consider deprecating `useCustomDateTimeAsFallback` in favour of `CUSTOM_DATE` appearing in the list.
- Update sealed interface `TransformationConfig` if necessary (already permits `DateTimeConfig`).

**Core transformation:**
- `app/core/src/main/java/ua/renamer/app/core/service/transformation/DateTimeTransformer.java` — update `extractDateTime()` method to iterate `fallbackOrder`, try each source using the existing per-source extraction logic, return the first non-null result.

**UI mode controller:**
- Update the Add Date & Time mode controller — replace the simple fallback checkbox with an ordered list control; up/down buttons per item are simpler to implement than drag-to-reorder. Show/hide the custom datetime picker when `CUSTOM_DATE` is in the list.

### Complexity

**Medium.** The config and transformer changes are clean and well-isolated. The UI ordered-list control is the most complex element — there is no existing pattern for reordering controls in the app, requiring new UI logic. Overall scope is contained but the UI work is non-trivial.

---

## F5 — Audio Metadata Transformation Mode

### Description

A new transformation mode that builds a filename fragment from audio metadata fields (artist, album, song title, year, track length). Users choose which fields to include (in up to 5 positional slots), define a separator, and specify where to insert the fragment (beginning, end, or replace the whole name). An "Ignore Non-Existing" option prevents files with missing metadata from being marked as errors.

### Use Case

A music library manager has thousands of audio files with embedded ID3/Vorbis tags but inconsistent filenames. This mode lets them rename files to a consistent pattern like `ArtistName - AlbumName - SongTitle` in one operation.

### Implementation Scope

**API module:**
- New enum `AudioMetaField` in `app/api/src/main/java/ua/renamer/app/api/enums/` — values: `ARTIST_NAME`, `ALBUM_NAME`, `SONG_NAME`, `YEAR`, `LENGTH`, `DO_NOT_USE`.
- New `AudioMetaConfig` in `app/api/src/main/java/ua/renamer/app/api/model/config/` — fields: `AudioMetaField slot1` through `slot5`, `String separator`, `ItemPositionWithReplacement position`, `boolean ignoreNonExisting`; annotated `@Value @Builder(setterPrefix = "with")`; implements `TransformationConfig` sealed interface. Custom `build()` validation: at least one slot must not be `DO_NOT_USE`.
- `app/api/src/main/java/ua/renamer/app/api/model/TransformationMode.java` — add `ADD_AUDIO_METADATA` enum value.
- Update sealed interface `app/api/src/main/java/ua/renamer/app/api/model/config/TransformationConfig.java` — add `permits AudioMetaConfig`.

**Core transformation:**
- New `AudioMetaTransformer` in `app/core/src/main/java/ua/renamer/app/core/service/transformation/` — implements `FileTransformationService<AudioMetaConfig>`; reads `fileModel.getMetadata().getAudio()` (the `AudioMeta` model already has all 5 fields as `Optional` — `artistName`, `albumName`, `songName`, `year`, `length`). Iterates slots in order, collects non-`DO_NOT_USE` present values, joins with separator, applies position; if any configured field is absent and `ignoreNonExisting=false`, returns error result.
- `app/core/src/main/java/ua/renamer/app/core/config/DIV2ServiceModule.java` — bind `AudioMetaTransformer.class` as `@Singleton`.
- `app/core/src/main/java/ua/renamer/app/core/service/impl/FileRenameOrchestratorImpl.java` — inject `AudioMetaTransformer`; add `case ADD_AUDIO_METADATA` in the `applyTransformation()` switch statement.

**UI module:**
- New mode controller class in `app/ui/src/main/java/ua/renamer/app/ui/controller/mode/` — follow the pattern of existing mode controllers.
- New FXML file in `app/ui/src/main/resources/fxml/mode/` — controls: 5 `ComboBox<AudioMetaField>` (slot1–slot5), `TextField` for separator, `ComboBox<ItemPositionWithReplacement>` for position, `CheckBox` for ignoreNonExisting.
- Register in `InjectQualifiers` with 3 qualifiers (FXMLLoader, Parent, ModeControllerApi) and bind in `DIUIModule`.

**Metadata layer:**
- Audio metadata extraction is already complete: `app/metadata/src/main/java/ua/renamer/app/metadata/strategy/AudioFileMetadataExtractor.java` populates `AudioMeta` for MP3, FLAC, OGG, WAV, AIFF, and a dozen other formats. No changes needed.

### Complexity

**High.** This is a full new transformation mode touching all four modules (api, core, ui, metadata). The metadata extraction layer is already complete, which reduces scope. The main work is the config/transformer implementation, sealed interface wiring across modules, and the multi-slot UI. Expect similar effort to adding any previous transformation mode, but with more UI controls than most existing modes.

---

## F6 — Format Mode (Fixed Base Name + Sequential Index)

### Description

A new sequential transformation mode where the user supplies a fixed base name and index parameters. All files are renamed to `<baseName><sep><index>` (or `<index><sep><baseName>`), where the index increments per file. The user controls index format (plain number, `(n)`, or `[n]`), padding, start value, step, and separator.

### Use Case

A user wants to rename a batch of downloaded images to `wallpaper_001`, `wallpaper_002`, etc. The existing Number Files (Sequence) mode only adds an index to the existing name; this mode replaces the entire name with a user-chosen base and a clean index.

### Implementation Scope

**API module:**
- New enum `IndexFormat` in `app/api/src/main/java/ua/renamer/app/api/enums/` — values: `PLAIN` (e.g. `001`), `PARENTHESES` (e.g. `(001)`), `BRACKETS` (e.g. `[001]`).
- New `FormatConfig` in `app/api/src/main/java/ua/renamer/app/api/model/config/` — fields: `String baseName`, `int startNumber`, `int stepValue`, `int padding`, `ItemPosition indexPosition` (BEGIN or END relative to `baseName`), `String separator`, `IndexFormat indexFormat`; annotated `@Value @Builder(setterPrefix = "with")`; implements `TransformationConfig`. Add `requiresSequentialExecution() = true` contract. Custom `build()` validation: non-blank baseName, `stepValue >= 1`, `startNumber >= 0`, `padding >= 0`.
- `app/api/src/main/java/ua/renamer/app/api/model/TransformationMode.java` — add `FORMAT_NAME` enum value.
- Update sealed interface `app/api/src/main/java/ua/renamer/app/api/model/config/TransformationConfig.java` — add `permits FormatConfig`.

**Core transformation:**
- New `FormatTransformer` in `app/core/src/main/java/ua/renamer/app/core/service/transformation/` — implements `FileTransformationService<FormatConfig>`; override `requiresSequentialExecution() = true`; override `transformBatch()` using the same sequential counter pattern as `SequenceTransformer` (see lines 40–86 in `SequenceTransformer.java` for reference). For each file: format index with padding and `IndexFormat` wrapping, build the output name from `baseName`, separator, and formatted index according to `indexPosition`.
- `app/core/src/main/java/ua/renamer/app/core/config/DIV2ServiceModule.java` — bind `FormatTransformer.class` as `@Singleton`.
- `app/core/src/main/java/ua/renamer/app/core/service/impl/FileRenameOrchestratorImpl.java` — inject `FormatTransformer`; add `case FORMAT_NAME` in the `applyTransformation()` switch statement.

**UI module:**
- New mode controller class in `app/ui/src/main/java/ua/renamer/app/ui/controller/mode/` — follow the pattern of existing mode controllers.
- New FXML file in `app/ui/src/main/resources/fxml/mode/` — controls: `TextField` for baseName, `Spinner<Integer>` for startNumber / stepValue / padding, `ComboBox<ItemPosition>` for indexPosition, `TextField` for separator, `ComboBox<IndexFormat>` for indexFormat.
- Register in `InjectQualifiers` with 3 qualifiers (FXMLLoader, Parent, ModeControllerApi) and bind in `DIUIModule`.

**Reuse pattern:**
- `SequenceTransformer.transformBatch()` (lines 40–86) is the direct pattern reference. `ItemPosition` (BEGIN/END) already exists. Padding and counter logic from `SequenceTransformer` can be extracted to a shared utility or duplicated inline.

### Complexity

**High.** Same full-mode scope as F5 — all four modules touched, sealed interface updated, new DI bindings, new UI controller and FXML. Slightly simpler than F5 in that the logic is arithmetic rather than metadata-dependent, but the sequential execution contract and batch transform pattern add their own nuance. The transformer implementation can be adapted from `SequenceTransformer` with modest changes.

---

## Implementation Notes

### DI Registration Pattern

When adding new transformers (F5, F6), follow the established pattern in `FileRenameOrchestratorImpl`:

```java
private final YourNewTransformer yourNewTransformer;

@Override
public List<PreparedFileModel> applyTransformation(...) {
    // ...
    case YOUR_MODE -> {
        FileTransformationService<YourConfig> service = yourNewTransformer;
        // ...
    }
    // ...
}
```

Constructor injection via `@RequiredArgsConstructor(onConstructor_ = {@Inject})` wires the transformer automatically.

### Sealed Interface Updates

When adding new config types (F4, F5, F6), the `TransformationConfig` sealed interface must be updated with `permits`. This enforces exhaustiveness checking in switch expressions throughout the codebase — the compiler will flag missing cases.

### Sequential Execution Contract

F6 requires sequential execution (like the existing Sequence mode). Always implement `requiresSequentialExecution() = true` and override `transformBatch()` instead of `transform()`. The orchestrator respects this and processes all files serially through your transformer.

### UI Mode Registration

When adding new modes (F5, F6), you must:
1. Add 3 qualifiers to `InjectQualifiers` (10 existing for each control type).
2. Add 3 `@Provides @Singleton` methods to `DIUIModule`.
3. Add an entry to `ViewNames` enum.
4. Map the mode in `MainViewControllerHelper` (if needed).

See `docs/developers/architecture/MODE_STATE_MACHINES.md` for the complete 10-mode UI architecture.

### Testing Strategy

- Unit tests in `app/core/src/test/java/` for transformers; use `@TempDir` for file operations.
- Integration tests in `app/backend/src/test/java/` with real Guice DI and metadata extraction.
- Use `app/core/src/test/resources/test-data/` real test files (MP3, FLAC, images, etc.). Generate new test files with `tools/generate_test_data.py` if needed.
