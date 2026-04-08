# Cross-Platform Build Issues

macOS builds (Intel and Apple Silicon) are stable — all tests pass and native packaging works.
Linux and Windows (tested on ARM64 VMs) had three environment-specific issues documented here.
All three are now **fixed** as of the `feature/v2-redesign` branch.

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

## Remaining known warnings (informational only, not build failures)

| Warning | Where | Notes |
|---------|-------|-------|
| `sun.misc.Unsafe` Lombok warning | All modules, test runs | Lombok uses deprecated internals; will resolve when Lombok releases a Java 25–compatible version |
| `java.lang.System::load` JavaFX warning | UI tests | JavaFX native library loader; suppressed with `--enable-native-access=javafx.graphics` if needed |
| PMD `NonSerializableClass` on `DateFormat`/`TimeFormat` | `api` module | Enums are not serialised in practice; exclusion can be added to `pmd-rules.xml` if desired |
| PMD `CyclomaticComplexity` on orchestrator/extractors | `core`, `metadata` | Pre-existing; refactoring is a separate concern |
| Checkstyle `MethodName` on test methods with underscores | `api` test classes | Pre-existing; test naming convention uses `given_when_then` style with underscores |
