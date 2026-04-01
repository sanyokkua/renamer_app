---
name: NameValidator toRealPath bug
description: isValid() rejected every valid rename-target name because toRealPath() throws NoSuchFileException for non-existent files
type: project
---

**Date fixed:** 2026-04-01

`NameValidator.isValid()` called `path.toRealPath()` to perform OS-level path validation. `toRealPath()` resolves
symlinks and verifies the path exists on disk — it throws `NoSuchFileException` (a subtype of `IOException`) for any
path that does not yet exist. Since rename-target filenames never exist yet, every valid name returned `false`, causing
all pipeline renames to fail with `ERROR_TRANSFORMATION`.

**Fix:** Remove `toRealPath()` entirely. `FileSystems.getDefault().getPath(fileName)` already throws
`InvalidPathException` for OS-invalid characters — that is the correct and sufficient validation. Removed
`java.io.IOException` from the catch clause and from the imports.

**File:** `app/core/src/main/java/ua/renamer/app/core/service/validator/impl/NameValidator.java` — `isValid()`, lines
73-78 after fix.

**Why:** Validation intent was "reject OS-illegal characters", but `toRealPath()` also enforces "file must exist" — a
completely separate and wrong requirement for this use case.

**How to apply:** When debugging "valid names rejected as invalid" in the rename pipeline, check `NameValidator` first.
Any future change to this class must NOT reintroduce filesystem-existence checks in the validation path.

**Regression test class:** `NameValidatorTest` (if added) or `FullPipelineIntegrationTest`.
