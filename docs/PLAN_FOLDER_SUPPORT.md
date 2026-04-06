# Folder Support — Full Implementation Plan

**Feature:** First-class folder support — rename folders, expand folder contents, drop dialog.
**Branch:** `feature/v2-redesign` (or create `feature/folder-support` from it)
**Modules touched:** `app/api`, `app/core`, `app/backend`, `app/ui`

---

## Background & Root-Cause Analysis

When a folder is dropped onto the file list, the app shows **"× Error / File extraction failed"**
for every folder entry. The screenshot at `docs/Screenshot 2026-04-06 at 14.42.18.png` shows
this clearly: the folder "Ava." appears in the table with "Folder" in the Type column but
"× Error" in the File Status column.

**The bug is NOT in metadata extraction.** `ThreadAwareFileMapper.mapFrom()` already handles
directories correctly:
- Sets `FileModel.isFile = false` (from `attributes.isRegularFile()`)
- Sets `mimeType = "application/x-directory"`
- Sets `category = Category.GENERIC`
- Populates `creationDate` and `modificationDate` from filesystem attributes ✓

**The bug is in all 10 transformer classes.** Each one has this identical early-return guard:
```java
if (!input.isFile()) {
    return buildErrorResult(input, "File extraction failed");
}
```
This block was written to propagate a metadata-extraction failure signal, but it conflates
"this entry is a directory" with "metadata extraction failed" — both happen to set
`isFile = false`. The fix is to make transformers handle directories explicitly, not treat
them as extraction errors.

**Physical rename already works for directories:**
- `Files.move(oldPath, newPath)` — works on both files AND directories (Java NIO)
- `File.renameTo(newFile)` — works on both files AND directories (Java IO)
No changes are needed in `RenameExecutionServiceImpl`.

---

## Transformer Compatibility Matrix

| Mode | Transformer | Folder treatment | Rationale |
|---|---|---|---|
| ADD_TEXT | AddTextTransformer | **Process normally** | Pure text op on the name string |
| REMOVE_TEXT | RemoveTextTransformer | **Process normally** | Pure text op on the name string |
| REPLACE_TEXT | ReplaceTextTransformer | **Process normally** | Pure text op on the name string |
| CHANGE_CASE | CaseChangeTransformer | **Process normally** | Pure text op on the name string |
| TRUNCATE_FILE_NAME | TruncateTransformer | **Process normally** | Pure text op on the name string |
| USE_PARENT_FOLDER_NAME | ParentFolderTransformer | **Process normally** | Parent dir name is available |
| ADD_SEQUENCE | SequenceTransformer | **Process normally** | Sequence number on name (uses FILE_SIZE / dates which are available for dirs) |
| CHANGE_EXTENSION | ExtensionChangeTransformer | **Skip (pass-through)** | Directories have no semantic extension; silently skip |
| USE_IMAGE_DIMENSIONS | ImageDimensionsTransformer | **Skip (pass-through)** | No width/height for directories |
| USE_DATETIME | DateTimeTransformer | **Partial** — FILE_CREATION, FILE_MODIFICATION, CURRENT, CUSTOM → process; CONTENT_CREATION_DATE with no fallback → pass-through | Filesystem dates are populated; no media content date |

**Pass-through** = `hasError=false`, original name unchanged →
`needsRename()` returns `false` → execution phase returns `RenameStatus.SKIPPED` (shows
"⚠ Skipped" badge, never "× Error").

---

## Recommended Implementation Order

```
Task 1  → Core fix: transformer pass-through       (unblocks everything; stand-alone)
Task 2  → Type column folder badge                 (visual polish; stand-alone)
Task 3  → File info preview for folders            (visual polish; stand-alone)
Task 4  → FolderDropOptions model                  (prerequisite for Tasks 5 & 6)
Task 5  → FolderExpansionService                   (prerequisite for Task 6)
Task 6  → Wire expansion into DIBackendModule       (prerequisite for Task 7)
Task 7  → Folder drop dialog (FXML + controller)   (requires Tasks 4 & 5)
Task 8  → Integrate dialog into main controller    (requires Tasks 4, 5, 6, 7)
Task 9  → i18n strings for dialog                  (parallel with Task 7)
```

---

## Task 1 — Core Fix: Remove folder error guards from transformers (Done)

### Why
All 10 transformers currently treat `!input.isFile()` (which is true for directories) as a
metadata-extraction failure. The intended behavior was to propagate extraction errors, but
this conflation causes every valid folder to show as "× Error." This task separates the
two concerns: compatible modes process folders normally; incompatible modes return a
pass-through result that yields "⚠ Skipped" in the UI.

### Files to modify (all in `app/core/src/main/java/ua/renamer/app/core/service/transformation/`)

#### 1a. `AddTextTransformer.java`
Delete lines 25–29 (the `!input.isFile()` guard). Folders have a `name` field and this
transformer only operates on the name string — no file-type assumption.

Before (lines 25–29):
```java
// Check if file extraction failed - propagate as extraction error
if (!input.isFile()) {
    log.debug("Propagating extraction error for: {}", input.getAbsolutePath());
    return buildErrorResult(input, "File extraction failed");
}
```
After: **Delete these 5 lines entirely.**

No other changes needed in this file.

#### 1b. `RemoveTextTransformer.java`
Apply the same deletion as 1a: remove the `!input.isFile()` guard block.

#### 1c. `ReplaceTextTransformer.java`
Apply the same deletion as 1a: remove the `!input.isFile()` guard block.

#### 1d. `CaseChangeTransformer.java`
Apply the same deletion as 1a: remove the `!input.isFile()` guard block.

#### 1e. `TruncateTransformer.java`
Apply the same deletion as 1a: remove the `!input.isFile()` guard block.

#### 1f. `ParentFolderTransformer.java`
Apply the same deletion as 1a: remove the `!input.isFile()` guard block.
`ParentFolderTransformer` reads `input.getFile().getParentFile()` — this works for
directories identically to regular files.

#### 1g. `ExtensionChangeTransformer.java`
Replace the `buildErrorResult` in the `!input.isFile()` guard with a new
`buildPassThroughResult` call.

Add a new private helper method at the bottom of the class (above or below
`buildErrorResult`):
```java
private PreparedFileModel buildPassThroughResult(FileModel input) {
    return PreparedFileModel.builder()
            .withOriginalFile(input)
            .withNewName(input.getName())
            .withNewExtension(input.getExtension())
            .withHasError(false)
            .withErrorMessage(null)
            .withTransformationMeta(null)
            .build();
}
```

Change the guard block (lines 25–29) from:
```java
if (!input.isFile()) {
    log.debug("Propagating extraction error for: {}", input.getAbsolutePath());
    return buildErrorResult(input, "File extraction failed");
}
```
To:
```java
if (!input.isFile()) {
    log.debug("Skipping extension change for directory: {}", input.getAbsolutePath());
    return buildPassThroughResult(input);
}
```

#### 1h. `ImageDimensionsTransformer.java`
Same pattern as 1g — add `buildPassThroughResult` helper, change the guard to call it:
```java
if (!input.isFile()) {
    log.debug("Skipping image dimensions for directory: {}", input.getAbsolutePath());
    return buildPassThroughResult(input);
}
```

#### 1i. `DateTimeTransformer.java`
This transformer is more complex. The `isFile` check is at line 53. Remove it, but add
folder-specific null-dateTime handling.

**Step 1:** Delete the `!input.isFile()` guard block (lines 52–56).

**Step 2:** Modify the `extractDateTime` method to handle the folder + CONTENT_CREATION_DATE
case. The existing `extractContentCreationDate` method will return `null` for folders
(no media metadata), which flows into the `if (dateTime == null)` check at line 62. For
*files*, a null dateTime is an error; for *folders*, it should be a pass-through.

Change the null-dateTime check inside `transform()` (currently around line 62):

Before:
```java
if (dateTime == null) {
    return buildErrorResult(input,
            "No datetime available for source: " + config.getSource());
}
```
After:
```java
if (dateTime == null) {
    if (!input.isFile()) {
        log.debug("Skipping datetime for directory (source unavailable): {}", input.getAbsolutePath());
        return buildPassThroughResult(input);
    }
    return buildErrorResult(input, "No datetime available for source: " + config.getSource());
}
```

Add the `buildPassThroughResult` helper method to this class as well.

#### 1j. `SequenceTransformer.java`
`SequenceTransformer` uses `transformBatch()` (not `transform()`). The guard is inside the
batch loop at lines 50–53:
```java
if (!input.isFile()) {
    log.debug("Propagating extraction error for: {}", input.getAbsolutePath());
    results.add(buildErrorResult(input, "File extraction failed"));
}
```
Change to:
```java
if (!input.isFile()) {
    log.debug("Including directory in sequence batch: {}", input.getAbsolutePath());
    validFiles.add(input);  // treat directory as a valid sequenceable item
}
```
This lets folders participate in the sequence along with files. The `sortBySource` method
uses `getName()`, `getAbsolutePath()`, `getFileSize()`, `getCreationDate()`,
`getModificationDate()` — all of which are populated for directories by
`ThreadAwareFileMapper`. The IMAGE_WIDTH / IMAGE_HEIGHT sort sources will use `orElse(0)`
for directories (no image metadata), so they'll sort to the front, which is acceptable.

### Completion Criteria for Task 1
1. `mvn compile -q -ff` from `app/` succeeds with no errors.
2. `mvn test -q -ff -Dai=true` from `app/` — all existing tests pass (none should fail
   from this change since the previous behavior was to error on dirs, not test it).
3. Drop a folder onto the app → it appears in the table with "⚠ Skipped" badge (not Error)
   when ADD_TEXT mode is selected but renamed normally when mode is text-based.
4. Drop a folder with ADD_TEXT mode → the folder appears with a renamed name (e.g., if name
   is "Ava" and mode adds "2026_" at the beginning, the new name shows "2026_Ava").
5. Drop a folder with USE_IMAGE_DIMENSIONS mode → folder shows "⚠ Skipped" (not Error).

---

## Task 2 — Type Column: Folder Badge (Done)

### Why
Currently (lines 294–323 of `ApplicationMainViewController.java`), the `itemTypeColumn`
CellFactory shows the file extension as a chip for files. For folders, `isFile=false` causes
`setGraphic(null)` — the Type cell is blank. We want to show a "folder" chip to make folders
visually distinguishable.

### Files to modify

#### 2a. `app/ui/src/main/resources/styles/components.css`
Add a new CSS class after the existing `.type-chip` block (after line 204):
```css
/* ── Folder type chip (Type column) ── */
.type-chip-folder {
    -fx-background-color: #EAF0FB;
    -fx-text-fill: #2C6FBF;
    -fx-border-color: #A8C5E8;
    -fx-border-width: 0.5;
    -fx-background-radius: 10;
    -fx-border-radius: 10;
    -fx-padding: 2 6 2 6;
    -fx-font-size: 9px;
    -fx-font-weight: bold;
}
```
The slightly more prominent styling (bolder, deeper border) visually separates folders from
file extension chips.

#### 2b. `app/ui/src/main/java/ua/renamer/app/ui/controller/ApplicationMainViewController.java`
In the `itemTypeColumn` CellFactory (lines 294–323), change the `else` branch that currently
calls `setGraphic(null)` for non-file entries.

Current code (lines 309–322):
```java
boolean isFile = candidate == null || !java.nio.file.Files.isDirectory(candidate.path());
if (isFile) {
    String name = preview.originalName() != null ? preview.originalName() : "";
    int dot = name.lastIndexOf('.');
    String ext = (dot >= 0 && dot < name.length() - 1) ? name.substring(dot + 1) : "";
    if (!ext.isEmpty()) {
        var chip = new javafx.scene.control.Label(ext.toLowerCase());
        chip.getStyleClass().add("type-chip");
        setGraphic(chip);
        return;
    }
}
setGraphic(null);
```
Replace with:
```java
boolean isDir = candidate != null && java.nio.file.Files.isDirectory(candidate.path());
if (isDir) {
    var chip = new javafx.scene.control.Label("folder");
    chip.getStyleClass().add("type-chip-folder");
    setGraphic(chip);
    return;
}
// File: show extension chip
String name = preview.originalName() != null ? preview.originalName() : "";
int dot = name.lastIndexOf('.');
String ext = (dot >= 0 && dot < name.length() - 1) ? name.substring(dot + 1) : "";
if (!ext.isEmpty()) {
    var chip = new javafx.scene.control.Label(ext.toLowerCase());
    chip.getStyleClass().add("type-chip");
    setGraphic(chip);
    return;
}
setGraphic(null);
```

### Completion Criteria for Task 2
1. Compile succeeds.
2. Folders in the table show a visually distinct "folder" chip in the Type column.
3. Files still show their extension chip (`.jpg`, `.png`, etc.) unchanged.
4. Files without an extension still show blank (unchanged behavior).

---

## Task 3 — File Info Preview Panel for Folders (Done)

### Why
The file info panel (`updateFileInfoPanel`, lines 497–629 of `ApplicationMainViewController.java`)
currently shows:
- File Size (in bytes/KB/MB) — for folders this is meaningless (shows 160 B = inode size)
- Media metadata section (image/video width/height, content creation date, audio tags) — never
  present for folders but the panel still tries to look it up and shows "N/A"

We want folders to show **Item Count** (how many direct children the folder has) instead of
File Size, and suppress the media metadata section entirely for directories.

### Files to modify

#### 3a. `app/ui/src/main/java/ua/renamer/app/ui/controller/ApplicationMainViewController.java`
Inside `updateFileInfoPanel()`, find the File Size block (lines 552–557):
```java
try {
    long bytes = Files.size(candidate.path());
    addRow.accept(leftCol, new String[]{
            languageTextRetriever.getString(TextKeys.FILE_SIZE), formatFileSize(bytes)});
} catch (IOException ignored) {
}
```
Replace with:
```java
boolean isDir = Files.isDirectory(candidate.path());
if (isDir) {
    try (var stream = Files.list(candidate.path())) {
        long itemCount = stream.count();
        addRow.accept(leftCol, new String[]{
                languageTextRetriever.getString(TextKeys.FILE_ITEM_COUNT),
                itemCount + " items"});
    } catch (IOException ignored) {
    }
} else {
    try {
        long bytes = Files.size(candidate.path());
        addRow.accept(leftCol, new String[]{
                languageTextRetriever.getString(TextKeys.FILE_SIZE), formatFileSize(bytes)});
    } catch (IOException ignored) {
    }
}
```

For the media metadata section (lines 578–610), wrap the entire `metaOpt.ifPresent` block
so that it is skipped for directories. The `mimeType` row should still show for both files
and folders (it shows "application/x-directory" which is informative). Only suppress the
IMAGE/VIDEO/AUDIO media rows:

Find the existing block inside `metaOpt.ifPresent(dto -> { ... })`:
```java
if ("IMAGE".equals(dto.category()) || "VIDEO".equals(dto.category())) {
    // ... content creation date, width, height rows
}
if ("AUDIO".equals(dto.category())) {
    // ... audio rows
}
```
Add a guard before each block:
```java
if (!isDir && ("IMAGE".equals(dto.category()) || "VIDEO".equals(dto.category()))) {
    // ... unchanged
}
if (!isDir && "AUDIO".equals(dto.category())) {
    // ... unchanged
}
```
Note: `isDir` was computed just above in the candidate block; ensure it is effectively final
or recompute it inside the lambda using `candidate != null && Files.isDirectory(candidate.path())`.

#### 3b. `app/ui/src/main/java/ua/renamer/app/ui/enums/TextKeys.java`
Add a new text key after `FILE_SIZE`:
```java
FILE_ITEM_COUNT("file_item_count"),
```

#### 3c. `app/ui/src/main/resources/langs/lang.properties`
Add after the `file_size` entry:
```properties
file_item_count=Item Count
```

#### 3d. `app/ui/src/main/resources/langs/lang_uk_UA.properties`
Add the Ukrainian translation (or duplicate the English as placeholder):
```properties
file_item_count=Кількість елементів
```

### Completion Criteria for Task 3
1. Compile succeeds.
2. Selecting a folder row shows "Item Count: N items" instead of "File Size: 160 B".
3. No Width / Height / Content Creation Date rows appear for folder selections.
4. Filesystem dates (Modification Time, Creation Time) still display for folders.
5. MIME type row ("application/x-directory") still displays.

---

## Task 4 — FolderDropOptions Model (Done)

### Why
The folder drop dialog (Task 7) needs to communicate the user's choice back to the drop
handler. This task creates the data record that carries the choice. It lives in `app/api`
so both the UI and backend (if needed) can use it without circular dependencies.

### Files to create

#### 4a. `app/api/src/main/java/ua/renamer/app/api/model/FolderDropOptions.java`
```java
package ua.renamer.app.api.model;

/**
 * Carries the user's choice from the folder drop dialog.
 *
 * <p>{@link Action#CANCEL} — discard the entire drop operation.
 * <p>{@link Action#USE_AS_ITEM} — add the folder itself as a single renamable entry.
 * <p>{@link Action#USE_CONTENTS} — expand the folder; options control depth and
 * whether sub-folders are added as items.
 *
 * @param action             the chosen action; never null
 * @param recursive          only meaningful when action == USE_CONTENTS; true means
 *                           traverse all descendant directories, false means immediate
 *                           children only
 * @param includeFoldersAsItems only meaningful when action == USE_CONTENTS; true means
 *                           sub-directories encountered during traversal are added as
 *                           renamable items as well as their file contents
 */
public record FolderDropOptions(Action action, boolean recursive, boolean includeFoldersAsItems) {

    public enum Action {
        CANCEL,
        USE_AS_ITEM,
        USE_CONTENTS
    }

    /** Convenience factory — cancel with no options. */
    public static FolderDropOptions cancel() {
        return new FolderDropOptions(Action.CANCEL, false, false);
    }

    /** Convenience factory — use folder itself as a single item. */
    public static FolderDropOptions useAsItem() {
        return new FolderDropOptions(Action.USE_AS_ITEM, false, false);
    }
}
```

### Files to modify

#### 4b. `app/api/src/main/java/module-info.java`
The `ua.renamer.app.api.model` package is already exported (line 9). No change needed — the
new record is in that package and will be exported automatically.

### Completion Criteria for Task 4
1. `mvn compile -q -ff` succeeds.
2. `FolderDropOptions` is importable from `app/ui` (since `app/ui` requires `ua.renamer.app.api`).

---

## Task 5 — FolderExpansionService: Interface + Implementation (Done)

### Why
When the user chooses "Use folder contents" in the drop dialog, the app needs to traverse a
directory and return a flat list of `Path` objects to add to the session. This service
encapsulates that traversal logic — making it injectable, testable, and independent of the
JavaFX UI layer.

The service lives in `app/backend` because: (a) it is I/O-bound work that may run on a
virtual thread, (b) it has no JavaFX dependencies, and (c) it fits the backend's role as
the service layer that mediates between the UI and the core pipeline.

### Files to create

#### 5a. `app/backend/src/main/java/ua/renamer/app/backend/service/FolderExpansionService.java`
```java
package ua.renamer.app.backend.service;

import ua.renamer.app.api.model.FolderDropOptions;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Expands a directory into a flat list of {@link Path} objects according to
 * the supplied {@link FolderDropOptions}.
 *
 * <p>This service handles:
 * <ul>
 *   <li>Non-recursive expansion (immediate children files only)</li>
 *   <li>Recursive expansion (all descendant files)</li>
 *   <li>Optional inclusion of sub-directories as renamable items</li>
 *   <li>Skipping hidden files and inaccessible paths (logged, not thrown)</li>
 * </ul>
 *
 * <p>Never throws; returns a partial result if some entries cannot be accessed.
 *
 * @param folder  the directory to expand; must not be null; must be a directory
 * @param options the expansion options derived from the drop dialog
 * @return ordered list of paths to add to the session; never null; may be empty
 */
public interface FolderExpansionService {
    List<Path> expand(Path folder, FolderDropOptions options);
}
```

#### 5b. `app/backend/src/main/java/ua/renamer/app/backend/service/impl/FolderExpansionServiceImpl.java`
```java
package ua.renamer.app.backend.service.impl;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.FolderDropOptions;
import ua.renamer.app.backend.service.FolderExpansionService;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Default implementation of {@link FolderExpansionService}.
 *
 * <p>Uses {@link Files#list} for non-recursive traversal and {@link Files#walk}
 * for recursive traversal. Hidden files are skipped. Permission errors and
 * symlink cycles are caught per-entry and logged.
 */
@Slf4j
@Singleton
public class FolderExpansionServiceImpl implements FolderExpansionService {

    @Override
    public List<Path> expand(Path folder, FolderDropOptions options) {
        List<Path> result = new ArrayList<>();
        try {
            if (options.recursive()) {
                collectRecursive(folder, options.includeFoldersAsItems(), result);
            } else {
                collectFlat(folder, options.includeFoldersAsItems(), result);
            }
        } catch (IOException e) {
            log.warn("Failed to list contents of folder: {}", folder, e);
        }
        return result;
    }

    private void collectFlat(Path folder, boolean includeFolders, List<Path> result)
            throws IOException {
        try (Stream<Path> stream = Files.list(folder)) {
            stream.forEach(path -> {
                try {
                    if (Files.isHidden(path)) return;
                    boolean isDir = Files.isDirectory(path);
                    if (!isDir) {
                        result.add(path);
                    } else if (includeFolders) {
                        result.add(path);
                    }
                } catch (IOException e) {
                    log.warn("Skipping inaccessible path: {}", path, e);
                }
            });
        }
    }

    private void collectRecursive(Path folder, boolean includeFolders, List<Path> result)
            throws IOException {
        try (Stream<Path> stream = Files.walk(folder, FileVisitOption.FOLLOW_LINKS)) {
            stream.skip(1) // skip the root folder itself
                  .forEach(path -> {
                      try {
                          if (Files.isHidden(path)) return;
                          boolean isDir = Files.isDirectory(path);
                          if (!isDir) {
                              result.add(path);
                          } else if (includeFolders) {
                              result.add(path);
                          }
                      } catch (IOException e) {
                          log.warn("Skipping inaccessible path: {}", path, e);
                      }
                  });
        } catch (FileSystemLoopException e) {
            log.warn("Symlink cycle detected during recursive expansion of: {}", folder, e);
            // Return whatever we have collected so far
        }
    }
}
```

### Files to modify

#### 5c. `app/backend/src/main/java/module-info.java`
Add the new package to the exports:
```
exports ua.renamer.app.backend.service.impl;
```
Also add `opens` if Guice reflection is needed (it typically is for Guice to inject):
```
opens ua.renamer.app.backend.service.impl;
```

Current `module-info.java` for `app/backend`:
```java
module ua.renamer.app.backend {
    requires ua.renamer.app.api;
    ...
    exports ua.renamer.app.backend.service;
    exports ua.renamer.app.backend.session;
    exports ua.renamer.app.backend.config;
    opens ua.renamer.app.backend.service;
    opens ua.renamer.app.backend.session;
    opens ua.renamer.app.backend.config;
}
```
Add after the existing exports:
```
exports ua.renamer.app.backend.service.impl;
opens ua.renamer.app.backend.service.impl;
```

### Completion Criteria for Task 5
1. `mvn compile -q -ff` succeeds.
2. Unit tests for `FolderExpansionServiceImpl`:
   - Non-recursive: returns only direct-child files, not subdirectory entries.
   - Non-recursive + includeFolders: returns direct-child files AND direct-child subdirs.
   - Recursive: returns all descendant files.
   - Recursive + includeFolders: returns all descendant files AND all descendant subdirs.
   - Empty folder: returns empty list.
   - Hidden files (e.g. `.DS_Store`): excluded from result.
   - The root folder itself is NOT in the result.

---

## Task 6 — Wire FolderExpansionService into DI

### Why
Guice needs to know that `FolderExpansionService` interface should be resolved to
`FolderExpansionServiceImpl`. Without this binding, `@Inject` of `FolderExpansionService`
in the main controller (Task 8) will fail at runtime.

### Files to modify

#### 6a. `app/backend/src/main/java/ua/renamer/app/backend/config/DIBackendModule.java`
Add one binding inside `configure()`:
```java
bind(FolderExpansionService.class).to(FolderExpansionServiceImpl.class).in(Scopes.SINGLETON);
```
Add the required imports:
```java
import ua.renamer.app.backend.service.FolderExpansionService;
import ua.renamer.app.backend.service.impl.FolderExpansionServiceImpl;
```

### Completion Criteria for Task 6
1. `mvn compile -q -ff` succeeds.
2. No `CreationException` from Guice for `FolderExpansionService` when the app is started.

---

## Task 7 — Folder Drop Dialog (FXML + Controller)

### Why
When folders are detected in a drop event, the user must choose how to handle them before
anything enters the session. The dialog presents three mutually exclusive choices — Cancel,
Use as item, Use folder contents — and when "Use folder contents" is selected, two optional
checkboxes for traversal depth and sub-folder handling.

The dialog is implemented as a JavaFX `Dialog<FolderDropOptions>` backed by a programmatic
approach (no FXML file needed for this simple dialog to keep complexity low and stay
consistent with how `showConfirmationDialog()` works in `ApplicationMainViewController`).

### Files to create

#### 7a. `app/ui/src/main/java/ua/renamer/app/ui/controller/FolderDropDialogController.java`

This class is a self-contained dialog builder (not an FXML controller). It builds the dialog
programmatically using JavaFX controls and returns a `Dialog<FolderDropOptions>`.

```java
package ua.renamer.app.ui.controller;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ua.renamer.app.api.model.FolderDropOptions;

/**
 * Builds and shows the modal dialog displayed when one or more folders are
 * dropped onto the file table.
 *
 * <p>This is a static utility class — no instantiation or Guice injection needed.
 * It is called from {@link ApplicationMainViewController} on the JavaFX Application Thread.
 */
public final class FolderDropDialogController {

    private FolderDropDialogController() {}

    /**
     * Shows the folder drop dialog and blocks until the user dismisses it.
     *
     * @param folderCount number of folders that were dropped (used to customise the header)
     * @return the user's choice; never null; returns {@link FolderDropOptions#cancel()} if
     *         the user closes the dialog without choosing
     */
    public static FolderDropOptions show(int folderCount) {
        // ── Button types ──
        var btnCancel     = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        var btnAsItem     = new ButtonType("Use as item", ButtonBar.ButtonData.LEFT);
        var btnContents   = new ButtonType("Use folder contents", ButtonBar.ButtonData.OK_DONE);

        var dialog = new Dialog<FolderDropOptions>();
        dialog.setTitle("Folder dropped");
        dialog.setHeaderText(folderCount == 1
                ? "A folder was dropped. How should it be handled?"
                : folderCount + " folders were dropped. How should they be handled?");
        dialog.getDialogPane().getButtonTypes().addAll(btnCancel, btnAsItem, btnContents);

        // ── Checkboxes (only relevant for "Use folder contents") ──
        var cbRecursive = new CheckBox("Include files from subfolders (recursive)");
        cbRecursive.setSelected(false);

        var cbIncludeFolders = new CheckBox("Include subfolders as items");
        cbIncludeFolders.setSelected(false);

        // Disable checkboxes until user clicks "Use folder contents"
        cbRecursive.setDisable(true);
        cbIncludeFolders.setDisable(true);

        // Enable checkboxes when the "Use folder contents" button is hovered or the dialog
        // is focused — simplest approach: always show them, enable them by default.
        // Actually, enable them always so the user can pre-configure before clicking.
        cbRecursive.setDisable(false);
        cbIncludeFolders.setDisable(false);

        var content = new VBox(10,
                new Label("Options for \"Use folder contents\":"),
                cbRecursive,
                cbIncludeFolders);
        content.setPadding(new Insets(10, 0, 0, 0));

        dialog.getDialogPane().setContent(content);

        // ── Result converter ──
        dialog.setResultConverter(buttonType -> {
            if (buttonType == btnAsItem)   return FolderDropOptions.useAsItem();
            if (buttonType == btnContents) return new FolderDropOptions(
                    FolderDropOptions.Action.USE_CONTENTS,
                    cbRecursive.isSelected(),
                    cbIncludeFolders.isSelected());
            return FolderDropOptions.cancel();
        });

        // Apply app stylesheet so the dialog inherits the existing theme
        dialog.getDialogPane().getStylesheets().addAll(
                FolderDropDialogController.class.getResource("/styles/base.css").toExternalForm(),
                FolderDropDialogController.class.getResource("/styles/components.css").toExternalForm()
        );

        return dialog.showAndWait().orElse(FolderDropOptions.cancel());
    }
}
```

### Files to modify

#### 7b. `app/ui/src/main/java/module-info.java`
The new class is in the existing `ua.renamer.app.ui.controller` package which is already
exported and opened to `javafx.fxml`. No module-info change is needed.

### Completion Criteria for Task 7
1. `mvn compile -q -ff` succeeds.
2. `FolderDropDialogController.show(1)` can be called on the FX thread and returns a non-null
   `FolderDropOptions` value for all three button choices.
3. The dialog is styled consistently with the rest of the app (uses `base.css` and
   `components.css`).
4. Closing the dialog window (X button) returns `FolderDropOptions.cancel()`.

---

## Task 8 — Integrate Dialog into Main Controller (Drop Handler)

### Why
This is where all previous tasks connect. The drop handler in `ApplicationMainViewController`
currently calls `sessionApi.addFiles(paths)` unconditionally. We change it to:
1. Detect if any dropped paths are directories.
2. If yes, show the dialog (Task 7) on the FX thread.
3. Based on the dialog result, build the final list of paths and call `sessionApi.addFiles`.

### Files to modify

#### 8a. `app/ui/src/main/java/ua/renamer/app/ui/controller/ApplicationMainViewController.java`

**Step 1:** Add `FolderExpansionService` as an injected dependency.

Find the class-level field declarations. The class uses
`@RequiredArgsConstructor(onConstructor_ = {@Inject})` (check the actual class header for
the Lombok annotation). If the class uses constructor injection via Lombok, add the field:
```java
private final FolderExpansionService folderExpansionService;
```
Add the import:
```java
import ua.renamer.app.backend.service.FolderExpansionService;
```

**Step 2:** Replace `handleFilesTableViewFilesDroppedEvent` (lines 447–467).

Current implementation:
```java
private void handleFilesTableViewFilesDroppedEvent(DragEvent event) {
    log.debug("handleFilesDroppedEvent");
    var dragboard = event.getDragboard();
    var success = false;
    if (dragboard.hasFiles()) {
        var paths = dragboard.getFiles().stream()
                .map(java.io.File::toPath)
                .toList();
        appProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressLabel.setText("Loading files\u2026");
        sessionApi.addFiles(paths).thenRunAsync(() -> {
            appProgressBar.setProgress(0);
            progressLabel.setText("");
            configureControlWidgetsState();
        }, Platform::runLater);
        success = true;
    }
    event.setDropCompleted(success);
    event.consume();
    configureControlWidgetsState();
}
```

New implementation:
```java
private void handleFilesTableViewFilesDroppedEvent(DragEvent event) {
    log.debug("handleFilesDroppedEvent");
    var dragboard = event.getDragboard();
    var success = false;
    if (dragboard.hasFiles()) {
        var allPaths = dragboard.getFiles().stream()
                .map(java.io.File::toPath)
                .toList();

        var dirs  = allPaths.stream().filter(java.nio.file.Files::isDirectory).toList();
        var files = allPaths.stream().filter(p -> !java.nio.file.Files.isDirectory(p)).toList();

        List<java.nio.file.Path> toAdd = new java.util.ArrayList<>(files);

        if (!dirs.isEmpty()) {
            // Dialog must run on FX thread — we are already on FX thread here
            var opts = FolderDropDialogController.show(dirs.size());

            if (opts.action() == ua.renamer.app.api.model.FolderDropOptions.Action.CANCEL) {
                event.setDropCompleted(false);
                event.consume();
                return;
            } else if (opts.action() == ua.renamer.app.api.model.FolderDropOptions.Action.USE_AS_ITEM) {
                toAdd.addAll(dirs);
            } else { // USE_CONTENTS
                for (var dir : dirs) {
                    var expanded = folderExpansionService.expand(dir, opts);
                    if (expanded.isEmpty()) {
                        log.info("Folder '{}' is empty or yielded no files — skipping", dir);
                    }
                    toAdd.addAll(expanded);
                }
            }
        }

        if (toAdd.isEmpty()) {
            event.setDropCompleted(false);
            event.consume();
            return;
        }

        var pathsToAdd = java.util.List.copyOf(toAdd);
        appProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressLabel.setText("Loading files\u2026");
        sessionApi.addFiles(pathsToAdd).thenRunAsync(() -> {
            appProgressBar.setProgress(0);
            progressLabel.setText("");
            configureControlWidgetsState();
        }, Platform::runLater);
        success = true;
    }
    event.setDropCompleted(success);
    event.consume();
    configureControlWidgetsState();
}
```

Add imports at the top of the file:
```java
import ua.renamer.app.api.model.FolderDropOptions;
import ua.renamer.app.backend.service.FolderExpansionService;
```

**Step 3:** Verify that `ApplicationMainViewController` uses constructor injection.
The class header should look like:
```java
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApplicationMainViewController { ... }
```
If so, adding `private final FolderExpansionService folderExpansionService;` as a field
is sufficient — Lombok generates the constructor parameter automatically.

Guice already knows about `FolderExpansionService` from Task 6 (`DIBackendModule`). Since
`DIUIModule` installs `DIBackendModule`, the binding is available in the UI injector.

#### 8b. Check `DIUIModule.java` — no change needed
`ApplicationMainViewController` is already bound as a singleton in `bindViewControllers()`
(line 95). Guice will automatically inject the new `FolderExpansionService` dependency via
the constructor.

### Completion Criteria for Task 8
1. `mvn compile -q -ff` succeeds.
2. `cd app/ui && mvn javafx:run` starts without `CreationException`.
3. Drop a folder:
   - Dialog appears.
   - Cancel → nothing added, file list unchanged.
   - "Use as item" → folder appears in file list as a single entry.
   - "Use folder contents" (non-recursive, no checkbox) → direct-child files added.
   - "Use folder contents" + "Include files from subfolders" → all descendant files added.
   - "Use folder contents" + "Include subfolders as items" → direct-child files AND direct
     sub-dirs added as separate entries.
4. Mixed drop (files + 1 folder): files added immediately; dialog shown for the folder.
5. Empty folder with "Use folder contents" → nothing added, no crash.
6. Dropping only files (no folders) → NO dialog shown, files added as before.

---

## Task 9 — i18n Strings for Dialog Text

### Why
The dialog in Task 7 currently uses hard-coded English strings. For consistency with the rest
of the app, all user-visible strings should go through `TextKeys` + `languageTextRetriever`.

### Note on timing
This task can be done in parallel with Task 7. However, since `FolderDropDialogController`
is a static utility (not injected), it cannot use `languageTextRetriever`. Two options:
- Pass a string-resolver function into `show()` as a parameter.
- Or keep the dialog in English for now and mark it as a known limitation.

**Recommended approach for this iteration:** Keep the dialog strings hard-coded in English.
Add a TODO comment in `FolderDropDialogController` noting that i18n should be added in a
follow-up. This avoids complexity and the existing confirmation dialog
(`showConfirmationDialog`) uses `languageTextRetriever` from the controller, which is a
different pattern.

**If i18n is required in this iteration:**

#### 9a. `TextKeys.java` — add new keys after `DIALOG_CONFIRM_BTN_CANCEL`:
```java
DIALOG_FOLDER_TITLE("dialog_folder_title"),
DIALOG_FOLDER_HEADER_SINGLE("dialog_folder_header_single"),
DIALOG_FOLDER_HEADER_MULTIPLE("dialog_folder_header_multiple"),
DIALOG_FOLDER_BTN_CANCEL("dialog_folder_btn_cancel"),
DIALOG_FOLDER_BTN_AS_ITEM("dialog_folder_btn_as_item"),
DIALOG_FOLDER_BTN_CONTENTS("dialog_folder_btn_contents"),
DIALOG_FOLDER_CB_RECURSIVE("dialog_folder_cb_recursive"),
DIALOG_FOLDER_CB_INCLUDE_FOLDERS("dialog_folder_cb_include_folders"),
DIALOG_FOLDER_OPTIONS_LABEL("dialog_folder_options_label"),
```

#### 9b. `lang.properties`:
```properties
dialog_folder_title=Folder Dropped
dialog_folder_header_single=A folder was dropped. How should it be handled?
dialog_folder_header_multiple={0} folders were dropped. How should they be handled?
dialog_folder_btn_cancel=Cancel
dialog_folder_btn_as_item=Use as item
dialog_folder_btn_contents=Use folder contents
dialog_folder_cb_recursive=Include files from subfolders (recursive)
dialog_folder_cb_include_folders=Include subfolders as items
dialog_folder_options_label=Options for "Use folder contents":
```

#### 9c. `lang_uk_UA.properties`: add Ukrainian equivalents.

**If taking the i18n route**, refactor `FolderDropDialogController.show()` to accept a
`java.util.function.Function<TextKeys, String>` parameter (a resolver lambda) so the strings
can be injected without making the class a Guice-managed bean.

### Completion Criteria for Task 9
If hard-coded: Task is a no-op; add the TODO comment and close.
If i18n: All dialog strings appear translated when `lang_uk_UA` locale is active.

---

## Edge Cases to Verify After All Tasks

| Scenario | Expected Behaviour |
|---|---|
| Drop a single folder → Cancel | Nothing added, table unchanged |
| Drop a single folder → Use as item | Folder in table, compatible modes can rename it |
| Drop a single folder → Use folder contents, non-recursive | Only immediate-child files added |
| Drop a single folder → Use folder contents, recursive | All descendant files added |
| Drop a single folder → Use folder contents + include subfolders | Direct-child files + direct subdirs added |
| Drop multiple folders at once | ONE dialog shown; choice applied to all folders |
| Drop a mix of files and folders | Files added directly; dialog shown for folders |
| Empty folder + "Use folder contents" | Nothing added; no crash; no notification needed |
| Folder with no readable children (permission denied) | Service logs warning; partial result (or empty) returned; no crash |
| ADD_TEXT mode applied to folder | Folder renamed with added text; no error |
| USE_IMAGE_DIMENSIONS mode with folder | Shows "⚠ Skipped" — no error badge |
| USE_DATETIME (CONTENT_CREATION_DATE) with folder | Shows "⚠ Skipped" — no error badge |
| USE_DATETIME (FILE_CREATION_DATE) with folder | Folder renamed with filesystem date |
| ADD_SEQUENCE with mixed files + folders | Both assigned sequence numbers |
| Rename button clicked with folder in list | Folder physically renamed on disk via Files.move() |
| Folder + incompatible mode → Rename clicked | Folder skipped silently (SKIPPED status), no error, file list updates |

---

## Files Changed Summary

| Task | Module | File | Action |
|---|---|---|---|
| 1a–1f | core | AddText, RemoveText, ReplaceText, ChangeCase, Truncate, ParentFolder transformers | Modify — delete `!isFile()` guard |
| 1g | core | ExtensionChangeTransformer | Modify — replace guard with pass-through |
| 1h | core | ImageDimensionsTransformer | Modify — replace guard with pass-through |
| 1i | core | DateTimeTransformer | Modify — remove guard; add folder pass-through at null-dateTime point |
| 1j | core | SequenceTransformer | Modify — include dirs in validFiles instead of erroring |
| 2a | ui | components.css | Modify — add `.type-chip-folder` style |
| 2b | ui | ApplicationMainViewController.java | Modify — update CellFactory for folder chip |
| 3a | ui | ApplicationMainViewController.java | Modify — Item Count vs File Size; suppress media rows for dirs |
| 3b | ui | TextKeys.java | Modify — add `FILE_ITEM_COUNT` |
| 3c | ui | lang.properties | Modify — add `file_item_count` |
| 3d | ui | lang_uk_UA.properties | Modify — add Ukrainian translation |
| 4a | api | FolderDropOptions.java | **Create** |
| 5a | backend | FolderExpansionService.java | **Create** |
| 5b | backend | FolderExpansionServiceImpl.java | **Create** |
| 5c | backend | module-info.java | Modify — export/open `backend.service.impl` |
| 6a | backend | DIBackendModule.java | Modify — bind FolderExpansionService |
| 7a | ui | FolderDropDialogController.java | **Create** |
| 8a | ui | ApplicationMainViewController.java | Modify — new drop handler; inject `FolderExpansionService` |
| 9a–d | ui | TextKeys, lang*.properties | Modify — add dialog i18n keys (optional) |
