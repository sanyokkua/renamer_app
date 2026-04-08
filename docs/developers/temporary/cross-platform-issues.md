# Cross-Platform Build Issues

macOS builds (Intel and Apple Silicon) are stable — all tests pass and native packaging works.
Linux (ARM64), Windows (ARM64 VMs), and Windows (x86_64) had environment-specific issues
documented here. All five are now **fixed** as of the `feature/v2-redesign` branch.

For full Linux environment requirements (JDK, Maven, packaging tools, CI notes, `dist/` layout),
see [`docs/linux-requirements.md`](linux-requirements.md).

---

## Issue 1 — `-Xdock:name=Renamer` breaks non-macOS Maven builds

**Status: FIXED**

**File:** `app/.mvn/jvm.config`

`-Xdock:name=Renamer` is a macOS-only JVM argument that sets the app name in the Dock.
The JVM on Linux and Windows does not recognise it, so Maven fails immediately at startup:

```
Unrecognized option: -Xdock:name=Renamer
Error: Could not create the Java Virtual Machine.
```

**Fix applied:**
- Removed `-Xdock:name=Renamer` from `app/.mvn/jvm.config`
- Added a `macos-dock` profile to `app/pom.xml` that activates automatically on macOS via `<os><family>mac</family></os>` and sets `<javafx.jvm.args>-Xdock:name=Renamer</javafx.jvm.args>`
- Wired `${javafx.jvm.args}` into the `<options>` element of `javafx-maven-plugin` in `app/ui/pom.xml`
- On Linux/Windows, `javafx.jvm.args` defaults to empty; on macOS, the flag is injected automatically

**CI/GitHub Actions note:** No change needed — CI already runs on Linux for the build step and macOS runners for packaging. Both will pick up the correct behaviour from the profile.

---

## Issue 2 — `DuplicateResolutionIntegrationTest` fails on Linux

**Status: FIXED**

**Test:** `DuplicateResolutionIntegrationTest.testDuplicates_SameNameDifferentExtensions`
**Error:** `File should exist: 002.pdf — expected: <true> but was: <false>`

**Root cause (confirmed on Ubuntu 22, ARM64, ext4/tmpfs, `LC_COLLATE=C.UTF-8`):**

`SequenceTransformer.sortBySource()` sorted by `FileModel::getName` only. `getName()` returns the file stem without extension, so `file.doc`, `file.pdf`, and `file.txt` all compare as equal. Java's `List.sort()` is stable, meaning the output order equalled the input order from `Files.list(tempDir)`, which is not guaranteed to be consistent across filesystems:
- macOS (APFS) returned files in an order that happened to match the original test assertions
- Linux (ext4/tmpfs) returned files in a different order, producing different sequence numbers

**Fix applied:**
- `SequenceTransformer.sortBySource()` now adds tiebreakers to all sort cases to guarantee platform-independent deterministic ordering. For `FILE_NAME`:

  ```java
  case FILE_NAME -> sorted.sort(Comparator.comparing(FileModel::getName)
          .thenComparing(FileModel::getExtension)
          .thenComparing(FileModel::getAbsolutePath));
  ```

  All other sort cases (`FILE_SIZE`, `FILE_CREATION_DATETIME`, etc.) also received `.thenComparing(FileModel::getName).thenComparing(FileModel::getAbsolutePath)` tiebreakers.

- Updated test assertions to match the now-deterministic alphabetical-by-extension order: `001.doc`, `002.pdf`, `003.txt`

**CI/GitHub Actions note:** No change needed. The sort is now deterministic on all platforms.

---

## Issue 3 — `scripts/package-linux.sh` fails with `--linux-shortcut` / `--linux-menu-group`

**Status: FIXED**

**Symptoms (confirmed on Ubuntu 22, ARM64, JDK 25):**

1. `jpackage` errors immediately on the app-image step:
   ```
   Error: Option [--linux-shortcut] is not valid with type [app-image]
   ```
2. If the app-image step is skipped, the deb step fails with:
   ```
   Bundler DEB Bundle skipped because of a configuration problem:
   Can not find fakeroot. Reason: Cannot run program "fakeroot"
   ```

**Root cause 1:** `--linux-shortcut` and `--linux-menu-group` are Linux installer flags valid only for `--type deb` (and `--type rpm`). The original script placed them in `COMMON_ARGS` shared by both the `app-image` and `deb` build invocations, so `jpackage` rejected them on the `app-image` step.

**Root cause 2:** `fakeroot` is not pre-installed on stock Ubuntu and is required by `jpackage` to build `.deb` packages. The script had no check for this and failed with a cryptic error.

**Fix applied:**
- Split `COMMON_ARGS` into `BASE_ARGS` (valid for all types) and `DEB_ARGS` (installer-only flags: `--linux-shortcut`, `--linux-menu-group`, `--linux-deb-maintainer`, `--linux-app-category`)
- `app-image` build uses `BASE_ARGS` only
- `deb` build uses `BASE_ARGS + DEB_ARGS`
- Added a `fakeroot` pre-flight check: if absent, the deb step is skipped with a clear warning and install instructions; the app-image still builds successfully

**CI/GitHub Actions note:** The `.github/workflows/build-and-release.yml` `package-linux` job already installs `fakeroot` via `apt-get`. No change needed to the workflow. For local development on a fresh machine, install with:

```bash
sudo apt-get install fakeroot
```

---

## Issue 4 — Windows colon test returns ERROR_EXECUTION instead of ERROR_TRANSFORMATION

**Status: FIXED**

**Test:** `RenameExecutionServiceImplTest.givenFilenameWithColonOnWindows_whenExecute_thenErrorTransformationReturned`
**Error:** `expected: <ERROR_TRANSFORMATION> but was: <ERROR_EXECUTION>`

**Root cause (confirmed on Windows 11 x86_64):**

`RenameExecutionServiceImpl.execute()` called `preparedFile.getNewPath()` before running
`nameValidator.isValid()`. `PreparedFileModel.getNewPath()` calls `resolveSibling(getNewFullName())`
on the Java NIO Path API. On Windows, filenames containing `:` (e.g. `"2024:01:01"`) cause
`resolveSibling` to throw `InvalidPathException` immediately — `:` is an illegal character in
Windows path components. The exception is caught by `catch (Exception e)`, which returns
`ERROR_EXECUTION`. The `nameValidator.isValid()` check (which would have returned `false` →
`ERROR_TRANSFORMATION`) was never reached.

Contrast: the slash test (`bad/name.txt`) passes on all platforms because
`resolveSibling("bad/name.txt")` does not throw on Windows, so the validator is reached.

**Fix applied:**
- Moved the `nameValidator.isValid(finalName)` block from inside the `try` block (after
  `getNewPath()`) to **before** the `try` block (immediately after the `needsRename()` guard).
  `getNewFullName()` is pure string concatenation and never throws. By the time `getNewPath()`
  is called, the filename is already verified to be valid on the current OS.

**Files changed:**
- `app/core/src/main/java/ua/renamer/app/core/service/impl/RenameExecutionServiceImpl.java`

**CI/GitHub Actions note:** The test is annotated `@EnabledOnOs(OS.WINDOWS)` and only runs on
Windows. The `build-and-test` job runs on `ubuntu-latest` and skips this test. No CI change is
needed for the fix itself.

---

## Issue 5 — Windows backend test failures: Logback file lock, dot-prefix hidden files, OS-specific path assertions

**Status: FIXED**

**Tests (confirmed on Windows 11 x86_64):**

1. `LoggingConfigServiceTest` — multiple tests fail with `JUnit: Failed to close extension context`
2. `FolderExpansionServiceImplTest.expand_hiddenFilesExcluded` — `AssertionError`: `.hidden_file` appears in results
3. `SettingsServiceResolveAppDirTest$LinuxBranchWithXdg`, `$LinuxBranchWithoutXdg`, `$MacOsBranch` — path assertions fail

**Root cause 1 — Logback file lock on Windows:**

`LoggingConfigService.removeFileAppender()` called only `detachAppender("FILE")`, which removes the appender
from the logger chain but does **not** call `appender.stop()`. On Windows, Logback holds an OS-level lock on
the log file (`logs/renamer.log`). When JUnit tries to delete the `@TempDir` after the test, Windows rejects
the deletion because the file handle is still open → `Failed to close extension context`.

**Root cause 2 — Dot-prefix is not hidden on Windows:**

On Unix systems, files whose names begin with `.` are treated as hidden by `Files.isHidden()`. On Windows,
`Files.isHidden()` checks the HIDDEN file attribute (set via Properties → Hidden). A file named `.hidden_file`
created with `Files.createFile()` is NOT hidden on Windows — no attribute is set. The test expected it to be
filtered out, but it was returned as a regular visible file.

**Root cause 3 — Unix path assertions fail on Windows:**

`SettingsServiceResolveAppDirTest$LinuxBranchWithXdg` and `$LinuxBranchWithoutXdg` assert that resolved paths
`startsWith("/home/testuser")` or `contains("/.config/")`. On Windows, `Path.of("/home/testuser").toString()`
returns `\home\testuser` (backslash separators), so both `startsWith` and `contains` assertions fail.
`MacOsBranch` similarly asserts `startsWith("/Users/testuser")`.

**Fix applied:**

1. `LoggingConfigService.removeFileAppender()` now gets the appender and calls `appender.stop()` **before**
   `detachAppender()` to release the file handle on all platforms. A matching `@AfterEach` in
   `LoggingConfigServiceTest` also stops the FILE appender to guard against tests that enable logging but
   never disable it.

2. `FolderExpansionServiceImplTest.expand_hiddenFilesExcluded` annotated with
   `@EnabledOnOs({OS.LINUX, OS.MAC})` — the test covers Unix-specific hidden-file behaviour.

3. `SettingsServiceResolveAppDirTest$LinuxBranchWithXdg` and `$LinuxBranchWithoutXdg` annotated with
   `@EnabledOnOs(OS.LINUX)`; `$MacOsBranch` annotated with `@EnabledOnOs(OS.MAC)` — path assertions are
   platform-specific and must only run where the expected separator is `/`.

**Files changed:**
- `app/backend/src/main/java/ua/renamer/app/backend/settings/LoggingConfigService.java`
- `app/backend/src/test/java/ua/renamer/app/backend/settings/LoggingConfigServiceTest.java`
- `app/backend/src/test/java/ua/renamer/app/backend/service/impl/FolderExpansionServiceImplTest.java`
- `app/backend/src/test/java/ua/renamer/app/backend/settings/SettingsServiceResolveAppDirTest.java`

**CI/GitHub Actions note:** All three failures only manifest on Windows runners. The existing
`build-and-test` job (Ubuntu, Temurin) is unaffected. No CI workflow changes are needed for these fixes.

---

## Remaining known warnings (informational only, not build failures)

| Warning | Where | Notes |
|---------|-------|-------|
| `sun.misc.Unsafe` Lombok warning | All modules, test runs | Lombok uses deprecated internals; will resolve when Lombok releases a Java 25–compatible version |
| `java.lang.System::load` JavaFX warning | UI tests | JavaFX native library loader; suppressed with `--enable-native-access=javafx.graphics` if needed |
| PMD `NonSerializableClass` on `DateFormat`/`TimeFormat` | `api` module | Enums are not serialised in practice; exclusion can be added to `pmd-rules.xml` if desired |
| PMD `CyclomaticComplexity` on orchestrator/extractors | `core`, `metadata` | Pre-existing; refactoring is a separate concern |
| Checkstyle `MethodName` on test methods with underscores | `api` test classes | Pre-existing; test naming convention uses `given_when_then` style with underscores |
