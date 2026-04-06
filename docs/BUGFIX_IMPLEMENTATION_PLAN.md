# Bugfix & UI Consistency — Implementation Plan

## Overview

Six issues addressed across four modules. Two data-integrity bugs (`app/api`, `app/backend`), two pipeline correctness bugs (`app/core`), and two UI polish issues (`app/ui`). All are independent — each can be implemented and tested in isolation.

## Issue Dependency Map

```
Issue 1 (Trailing Dot)     — independent, app/api + app/backend
Issue 2 (Duplicate Items)  — independent, app/backend
Issue 3 (False Duplicates) — independent, app/core
Issue 4 (Race Condition)   — independent, app/core
Issue 5 (Reload Button)    — independent, app/ui
Issue 6 (Dialog Styling)   — independent, app/ui
```

No compile-time dependency between issues.

## Recommended Execution Order

1. **Issue 3** — single `groupingBy` change, no API impact
2. **Issue 4** — sort + stream change in orchestrator, no API impact
3. **Issue 2** — additive filter in `RenameSession.addFiles()`
4. **Issue 1** — fix `PreparedFileModel.getOldFullName()` symmetry
5. **Issue 5** — two-line UI change
6. **Issue 6** — purely additive CSS application to dialog

---

## Issue 1: Trailing Dot Forces Pending Status

### Root Cause

`FilenameUtils.getBaseName("my_folder.")` → `"my_folder"`, `getExtension("my_folder.")` → `""`. The trailing dot is permanently lost at ingestion in `ThreadAwareFileMapper`. Both name-reconstruction methods diverge:

- `PreparedFileModel.getOldFullName()` (line 51–53) unconditionally concatenates a dot:
  `name + "." + extension` → `"my_folder."` (has the dot)
- `PreparedFileModel.getNewFullName()` conditionally skips the dot when extension is empty:
  `extension.isEmpty() ? name : name + "." + extension` → `"my_folder"` (no dot)

These two methods are asymmetric. `needsRename()` calls both and finds they differ → item marked Pending with no transform applied.

### Affected Files

| File | Location | What Changes |
|------|----------|--------------|
| `app/api/src/main/java/ua/renamer/app/api/model/PreparedFileModel.java` | lines 51–53 | `getOldFullName()` — make symmetric with `getNewFullName()` |

### Implementation Steps

1. In `PreparedFileModel.getOldFullName()`, replace the unconditional dot with a conditional:
   ```java
   public String getOldFullName() {
       String ext = originalFile.getExtension();
       return (ext == null || ext.isEmpty())
               ? originalFile.getName()
               : originalFile.getName() + "." + ext;
   }
   ```
   `getNewFullName()` already uses this pattern — now both methods are symmetric. No change needed in `RenameSessionConverter.toPlaceholderPreview()` — its conditional logic already matches the corrected `getOldFullName()`.

### Edge Cases

- Normal file `"file.txt"`: ext=`"txt"` → `getOldFullName()` = `"file.txt"`. Unchanged.
- File with no extension `"Makefile"`: ext=`""` → `"Makefile"`. Unchanged.
- Hidden file `".bashrc"`: ext=`""`, name=`".bashrc"` → `".bashrc"`. Unchanged.
- Folder `"my_folder."`: ext=`""`, name=`"my_folder"` (dot lost at `FilenameUtils` level). After fix, `getOldFullName()` = `"my_folder"` = `getNewFullName()` → `needsRename()` = false → not Pending. Correct.

### Tests

**File:** `app/api/src/test/java/ua/renamer/app/api/model/PreparedFileModelTest.java`

- `getOldFullName_withExtension_returnsNameDotExtension()`
- `getOldFullName_withEmptyExtension_returnsNameOnly()`
- `getOldFullName_andGetNewFullName_areSymmetric_whenNoTransformApplied()` — when newName=name and newExtension=extension (both empty), both methods return identical strings and `needsRename()` returns false
- `needsRename_returnsFalse_forItemWithNoExtension_andNoTransform()` — regression for trailing-dot scenario

### Verification

- [ ] Folder with trailing dot added → status NOT "Pending", Changed Name column matches original
- [ ] File with no extension added → status NOT "Pending"
- [ ] Applying an actual rename rule to such an item still marks it Pending correctly
- [ ] `mvn test -q -ff -Dai=true -Dtest=PreparedFileModelTest` passes

---

## Issue 2: Duplicate Items Added

### Root Cause

`RenameSession.addFiles()` (lines 39–41):
```java
files.addAll(newFiles);
```
`ArrayList.addAll()` appends blindly. The same absolute path can be inserted multiple times. `removeFiles()` already uses `absolutePath` as a key — deduplication on removal exists, but addition has no guard.

### Affected Files

| File | Location | What Changes |
|------|----------|--------------|
| `app/backend/src/main/java/ua/renamer/app/backend/session/RenameSession.java` | lines 39–47 | `addFiles()` — filter by absolute path before `addAll` |

### Implementation Steps

1. Replace the body of `addFiles()`:
   ```java
   public void addFiles(List<FileModel> newFiles) {
       Set<String> existingPaths = files.stream()
               .map(FileModel::getAbsolutePath)
               .collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new));

       List<FileModel> deduped = newFiles.stream()
               .filter(fm -> existingPaths.add(fm.getAbsolutePath()))
               .toList();

       files.addAll(deduped);
       lastPreview = List.of();
       if (activeMode != null && currentParams != null) {
           status = SessionStatus.MODE_CONFIGURED;
       } else {
           status = SessionStatus.FILES_LOADED;
       }
   }
   ```
   `Set.add()` returns `false` if the path is already present — this deduplicates both within the incoming batch and against the existing session state in a single pass.

### Edge Cases

- All incoming paths already loaded: `deduped` is empty, `files` unchanged, `lastPreview` cleared, status advanced. Harmless.
- Duplicates within the same batch (overlapping folder expansions): `existingPaths.add()` rejects the second occurrence.
- `absolutePath` nullability: `ThreadAwareFileMapper` always constructs it via `file.getAbsolutePath()`. Never null.

### Tests

**File:** `app/backend/src/test/java/ua/renamer/app/backend/session/RenameSessionTest.java`

- `addFiles_withDuplicatePaths_doesNotAddDuplicates()` — add FILE_A twice, assert `files.size() == 1`
- `addFiles_withDuplicateWithinBatch_addsOnlyOne()` — `addFiles(List.of(FILE_A, FILE_A))`, assert size == 1
- `addFiles_withOverlappingBatches_deduplicatesAcrossCalls()` — first batch A+B, second batch B+C, assert final size == 3
- `addFiles_allDuplicates_doesNotChangeSizeButClearsPreview()` — add A, set preview, add A again, assert preview empty and size unchanged

### Verification

- [ ] Dropping the same file twice → one row in table
- [ ] Dropping a child of an already-loaded folder individually → no new row
- [ ] `mvn test -q -ff -Dai=true -Dtest=RenameSessionTest` passes

---

## Issue 3: False Duplicate Name Detection Across Directories

### Root Cause

`DuplicateNameResolverImpl.resolve()` (lines 19–21) groups by `getNewFullName()` alone:
```java
.collect(Collectors.groupingBy(PreparedFileModel::getNewFullName));
```
`getNewFullName()` returns only the filename (e.g., `"FileA.jpg"`), with no directory component. Files in different directories with the same target name land in the same group → one gets a suffix appended incorrectly.

### Affected Files

| File | Location | What Changes |
|------|----------|--------------|
| `app/core/src/main/java/ua/renamer/app/core/service/impl/DuplicateNameResolverImpl.java` | lines 19–21, `usedNames` Set, and do-while loop | groupBy key → composite `(parentDir, newFullName)` |

### Implementation Steps

1. Add a private record inside `DuplicateNameResolverImpl`:
   ```java
   private record NameKey(java.nio.file.Path parentDir, String newFullName) {}
   ```

2. Change the `groupingBy` classifier:
   ```java
   Map<NameKey, List<PreparedFileModel>> nameGroups = models.stream()
           .collect(Collectors.groupingBy(model -> new NameKey(
                   model.getOriginalFile().getFile().toPath().getParent(),
                   model.getNewFullName()
           )));
   ```

3. Change `usedNames` from `Set<String>` to `Set<NameKey>`:
   ```java
   Set<NameKey> usedNames = new HashSet<>(nameGroups.keySet());
   ```

4. Inside the do-while loop, compute `parentDir` once per model (before the loop) and use composite key in the condition and add:
   ```java
   java.nio.file.Path parentDir = model.getOriginalFile().getFile().toPath().getParent();
   // in the while condition:
   } while (usedNames.contains(new NameKey(parentDir, uniqueFullName)));
   usedNames.add(new NameKey(parentDir, uniqueFullName));
   ```

5. Update the debug log:
   ```java
   log.debug("Found {} files with duplicate target name '{}' in '{}'",
           group.size(), entry.getKey().newFullName(), entry.getKey().parentDir());
   ```

### Edge Cases

- Same directory, same target name: `NameKey` includes parent → still detected as conflict → suffix appended. Existing behavior preserved.
- Different directory, same target name: different `NameKey` → not a conflict → no suffix. Bug fixed.
- `toPath().getParent()` returns null (root-level path): guard with `parentDir != null ? parentDir : java.nio.file.Path.of("")`. Document with a comment: _"null parent means filesystem root; use empty path as sentinel."_
- Folders and files: the fix applies equally to both — the parent directory is always well-defined.

### Tests

**File:** `app/core/src/test/java/ua/renamer/app/core/service/impl/DuplicateNameResolverImplTest.java`

- `resolve_filesInDifferentDirectories_noSuffixAdded()` — two `PreparedFileModel` with same `newFullName` but different parent dirs → neither gets suffix
- `resolve_filesInSameDirectory_suffixAddedToSecond()` — regression: same dir, same target name → second gets suffix
- `resolve_mixedDirectories_onlySameDirConflictsResolved()` — A+B in dir1 (conflict), C in dir2 (same name as A, no conflict) → only B gets suffix
- `resolve_nullParentDirectory_doesNotThrow()` — file with no parent → handled gracefully

### Verification

- [ ] `folder1/photo.jpg` and `folder2/photo.jpg` with no transform → both show `photo.jpg`, no suffix on either
- [ ] `folder1/photo.jpg` and `folder1/pic.jpg` where `pic.jpg` transforms to `photo.jpg` → conflict detected, suffix applied
- [ ] `mvn test -q -ff -Dai=true -Dtest=DuplicateNameResolverImplTest` passes

---

## Issue 4: Folder-Before-File Rename Race Condition

### Root Cause

`FileRenameOrchestratorImpl.executeRenamesParallel()` (lines ~257–268) uses `parallelStream()` with no ordering:
```java
return prepared.parallelStream().map(preparedFile -> CompletableFuture.supplyAsync(() -> {
    RenameResult result = renameExecutor.execute(preparedFile);
    ...
}, executor)).map(CompletableFuture::join).toList();
```
Virtual threads schedule tasks non-deterministically. If a parent folder is renamed before its children, the children's stored `oldPath` (set at ingestion via `PreparedFileModel.getOldPath()` → `originalFile.getFile().toPath()`) is stale → `Files.move()` throws `NoSuchFileException`, captured as an error result by the no-throw pipeline.

### Affected Files

| File | Location | What Changes |
|------|----------|--------------|
| `app/core/src/main/java/ua/renamer/app/core/service/impl/FileRenameOrchestratorImpl.java` | lines ~257–268 | Sort depth-descending + switch to sequential stream; rename method to `executeRenamesOrdered` |

### Implementation Steps

1. Sort `prepared` by path depth descending (deepest = most components = children first):
   ```java
   List<PreparedFileModel> ordered = prepared.stream()
           .sorted(Comparator.comparingInt(
                   (PreparedFileModel p) -> p.getOldPath().getNameCount()
           ).reversed())
           .toList();
   ```

2. Switch from `parallelStream()` to sequential `stream()`. `parallelStream()` does not guarantee execution order even with a pre-sorted list — virtual threads schedule tasks independently:
   ```java
   return ordered.stream().map(preparedFile -> {
       RenameResult result = renameExecutor.execute(preparedFile);
       int current = completed.incrementAndGet();
       updateProgress(current, total, progressCallback);
       return result;
   }).toList();
   ```

3. Rename the method from `executeRenamesParallel` to `executeRenamesOrdered` and update all call sites.

### Edge Cases

- Flat list (no parent-child relationships): sorting has no correctness impact. Sequential execution through depth order. No regression.
- Siblings at same depth: order among siblings is unspecified but irrelevant — siblings are independent of each other.
- Performance: phase 3 loses parallelism. For typical sessions on a single filesystem, I/O is disk-bound not CPU-bound — negligible impact. Re-parallelization with path-ancestry grouping can be done later if profiling shows a need.
- Very deep nesting: `getNameCount()` is O(1). Sorting is O(n log n). Not a concern for typical session sizes.

### Tests

**File:** `app/core/src/test/java/ua/renamer/app/core/service/impl/FileRenameOrchestratorImplTest.java`

- `execute_withParentAndChildInList_childRenamedBeforeParent()` — use `@TempDir`, create `parent/child.txt`, add both to rename list, verify all results succeed (no `NoSuchFileException`)
- `execute_withDeeplyNestedFolders_allRenameSucceed()` — create `a/b/c/file.txt` where `a`, `a/b`, `a/b/c`, and `a/b/c/file.txt` are all in the rename list → all results succeed

### Verification

- [ ] Renaming a folder that contains loaded child files produces no errors for the children
- [ ] Method `executeRenamesParallel` renamed to `executeRenamesOrdered` at definition and all call sites
- [ ] `mvn test -q -ff -Dai=true -Dtest=FileRenameOrchestratorImplTest` passes

---

## Issue 5: Reload Button Always Visible But Disabled

### Root Cause

`configureControlWidgetsState()` (line ~392):
```java
reloadBtn.setVisible(areFilesRenamed);
```
`setVisible(false)` makes the node invisible **and** removes it from layout — the surrounding HBox reflows when the button appears/disappears. Fix: use `setDisable()`. The button occupies its space at all times.

Line ~400:
```java
renameBtn.setDisable(reloadBtn.isVisible());
```
This uses `isVisible()` as a proxy for `areFilesRenamed`. After the visibility fix, `isVisible()` always returns true → `renameBtn` would always be disabled. Must use the field directly.

### Affected Files

| File | Location | What Changes |
|------|----------|--------------|
| `app/ui/src/main/java/ua/renamer/app/ui/controller/ApplicationMainViewController.java` | ~line 392 | `setVisible(areFilesRenamed)` → `setDisable(!areFilesRenamed)` |
| same | ~line 400 | `reloadBtn.isVisible()` → `areFilesRenamed` |

### Implementation Steps

1. Line ~392: `reloadBtn.setVisible(areFilesRenamed)` → `reloadBtn.setDisable(!areFilesRenamed)`
2. Line ~400: `renameBtn.setDisable(reloadBtn.isVisible())` → `renameBtn.setDisable(areFilesRenamed)`

No CSS changes required — `buttons.css` already has `.btn-secondary:disabled { -fx-opacity: 0.5; }` for visual feedback.

### Edge Cases

- Initial state (`areFilesRenamed = false`): reload disabled, rename enabled (if files present). Correct.
- After rename (`areFilesRenamed = true`): reload enabled, rename disabled. Correct.
- After reload (`areFilesRenamed` reset to false): reload disabled again, rename enabled. Correct.
- After clear: `areFilesRenamed` reset to false → reload disabled. Correct.

### Verification

- [ ] App start: all 3 buttons visible; reload is grayed out (disabled), rename is enabled
- [ ] After rename: reload enabled, rename disabled — no layout shift
- [ ] After reload: reload grayed out again
- [ ] After clear: reload grayed out
- [ ] `mvn compile -q -ff` passes

---

## Issue 6: Confirmation Dialog Styling

### Root Cause

`showConfirmationDialog()` (lines ~708–720) creates `new Alert(CONFIRMATION)` with no CSS attached. JavaFX renders it with Modena defaults — doesn't match the app's design language.

The `FolderDropDialogController` (lines 60–84) shows the correct pattern: attach 3 stylesheets to `getDialogPane()` and style buttons via `lookupButton()` inside `setOnShowing`.

### Affected Files

| File | Location | What Changes |
|------|----------|--------------|
| `app/ui/src/main/java/ua/renamer/app/ui/controller/ApplicationMainViewController.java` | lines ~708–720 (`showConfirmationDialog()`) | Add CSS stylesheets + button styling via `setOnShowing` |

### Reference Implementation

`app/ui/src/main/java/ua/renamer/app/ui/controller/FolderDropDialogController.java` lines 60–84:
```java
dialog.getDialogPane().getStylesheets().addAll(
    FolderDropDialogController.class.getResource("/styles/base.css").toExternalForm(),
    FolderDropDialogController.class.getResource("/styles/buttons.css").toExternalForm(),
    FolderDropDialogController.class.getResource("/styles/components.css").toExternalForm()
);
dialog.setOnShowing(e -> {
    var bar = dialog.getDialogPane().lookup(".button-bar");
    if (bar instanceof ButtonBar buttonBar) {
        buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_NONE);
    }
    var nodeContents = dialog.getDialogPane().lookupButton(btnContents);
    if (nodeContents != null) nodeContents.getStyleClass().add("btn-primary");
    // ...
});
```

### Implementation Steps

1. After `alert.getButtonTypes().setAll(confirmButton, cancelButton)`, attach stylesheets:
   ```java
   alert.getDialogPane().getStylesheets().addAll(
           ApplicationMainViewController.class.getResource("/styles/base.css").toExternalForm(),
           ApplicationMainViewController.class.getResource("/styles/buttons.css").toExternalForm(),
           ApplicationMainViewController.class.getResource("/styles/components.css").toExternalForm()
   );
   ```

2. Add `setOnShowing` handler for button styling and order control:
   ```java
   alert.setOnShowing(e -> {
       var bar = alert.getDialogPane().lookup(".button-bar");
       if (bar instanceof ButtonBar buttonBar) {
           buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_NONE);
       }
       var btnOk = alert.getDialogPane().lookupButton(confirmButton);
       var btnCancel = alert.getDialogPane().lookupButton(cancelButton);
       if (btnOk != null) btnOk.getStyleClass().add("btn-primary");
       if (btnCancel != null) btnCancel.getStyleClass().add("btn-ghost");
   });
   ```

3. Optionally suppress the default question-mark graphic: `alert.setGraphic(null);`

### Edge Cases

- `lookupButton()` returns null: guarded with `if (btnOk != null)`. Safe.
- CSS resource paths: identical to those used by `FolderDropDialogController` — confirmed to work at runtime.
- Platform button reordering: `BUTTON_ORDER_NONE` ensures stable order across OS.

### Verification

- [ ] Confirmation dialog renders with app theme (blue primary confirm button, ghost cancel button)
- [ ] Button order is stable — not platform-reordered
- [ ] Visual appearance matches FolderDropDialog
- [ ] `mvn compile -q -ff` passes

---

## Risk Assessment

| Issue | Risk | Notes |
|-------|------|-------|
| 1 (Trailing Dot) | Medium | `getOldFullName()` feeds `needsRename()`, `toPreview()`, and transformers. Test `PreparedFileModelTest` symmetry contract before merging. |
| 2 (Duplicate Items) | Low | Additive filter; normal usage unaffected. |
| 3 (False Duplicates) | Low | Narrower grouping key; existing same-directory behavior preserved. |
| 4 (Race Condition) | Medium | Removes parallelism from phase 3. Check for additional callers of `executeRenamesParallel`. Performance impact negligible for typical sessions. |
| 5 (Reload Button) | Low | Two-line change; verify `isVisible()` proxy replacement. |
| 6 (Dialog Styling) | Very Low | Purely additive; no logic changes. |

**Riskiest change:** Issue 1 — `getOldFullName()` is load-bearing in the preview pipeline. Ensure `PreparedFileModelTest` covers the symmetry contract before merging.
