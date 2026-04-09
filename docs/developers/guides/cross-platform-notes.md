# Cross-Platform Notes

This document covers platform-specific JDK requirements, filesystem differences, code patterns that exist because of
platform quirks, packaging prerequisites, and test annotations required for cross-platform correctness.

**Testing scope:** macOS (Intel and Apple Silicon) is the primary development platform — all tests pass and packaging is
stable. Linux (ARM64) and Windows (x86_64) were verified as working with multiple JDK vendors. Windows ARM64 is not
supported — OpenJFX does not publish win-aarch64 Maven artifacts.

---

## 1. JDK Requirements by Platform

| Platform                      | Verified JDK                                                         | Notes                                                                                                                                                                                  |
|-------------------------------|----------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| macOS (Intel + Apple Silicon) | Any standard JDK 25 (Corretto, Temurin, Liberica, etc.)              | Primary dev platform; JavaFX native libraries are bundled in platform-specific JARs on the classpath — no special JDK needed                                                           |
| Linux ARM64                   | Any JDK 25 distribution (Corretto, Temurin, BellSoft, OpenJDK, etc.) | jpackage resolves JavaFX from the modular JARs in `target/libs/` — no special JDK needed, same mechanism as macOS. Verified with four vendors.                                         |
| Windows x86_64                | Any x64 JDK 25 (Temurin, Liberica, Zulu, Corretto, etc.)             | Maven resolves JavaFX as platform-specific JARs — same mechanism as macOS/Linux. No special JDK needed. Verified with Liberica, Temurin, and Zulu. ARM64 Windows is **not supported**. |

**Why any JDK works on macOS, Linux, and Windows x64:** jpackage resolves JavaFX from the modular JARs already present
in `app/ui/target/libs/` (pulled in by the OpenJFX Maven dependency) — no `.jmod` files from the JDK itself are needed.
All three platforms share this mechanism.

**Why Windows ARM64 is not supported:** OpenJFX does not publish Maven artifacts for the `win-aarch64` classifier.
Maven dependency resolution fails for all JavaFX dependencies before any code is compiled.

**Verify on Linux or Windows:**

```bash
java -version   # any JDK 25 vendor; confirm arch is x64 (amd64) on Windows
```

---

## 2. Filesystem Behavior Differences

| Behavior                              | macOS (APFS/HFS+)                                           | Linux (ext4)                                    | Windows (NTFS)                                                |
|---------------------------------------|-------------------------------------------------------------|-------------------------------------------------|---------------------------------------------------------------|
| Case sensitivity                      | Case-insensitive by default                                 | Case-sensitive                                  | Case-insensitive                                              |
| Case-only rename via `Files.move()`   | No-op — treated as same file                                | Works correctly                                 | No-op — treated as same file                                  |
| Case-only rename solution             | `File.renameTo()` fallback                                  | N/A                                             | `File.renameTo()` fallback                                    |
| Hidden files                          | Dot-prefix names (`.hidden`) detected by `Files.isHidden()` | Dot-prefix names detected by `Files.isHidden()` | HIDDEN file attribute — dot-prefix names are **not** hidden   |
| Path separator                        | `/`                                                         | `/`                                             | `\`                                                           |
| Illegal filename characters           | None specific to the OS                                     | None specific to the OS                         | `:  < > " / \                                                 | ? *` |
| Reserved filenames                    | None                                                        | None                                            | `CON`, `PRN`, `AUX`, `NUL`, `COM1`–`COM9`, `LPT1`–`LPT9`      |
| Path length limit                     | None practical                                              | None practical                                  | 260 characters (MAX_PATH) unless long-path support is enabled |
| Filesystem ordering of `Files.list()` | Non-deterministic (APFS)                                    | Non-deterministic (ext4/tmpfs)                  | Non-deterministic                                             |

---

## 3. Known Platform Quirks

These were each discovered as bugs and fixed. The explanations below describe why the code is structured the way it is.

### macOS Gatekeeper — unsigned app blocked on first launch

**Affected platform:** macOS  
**Root cause:** Apps downloaded from the internet receive a `com.apple.quarantine` extended attribute set by the browser
or
the OS. macOS Gatekeeper checks this attribute and blocks launch for unsigned apps, displaying _"cannot be opened
because
it is from an unidentified developer."_

**How to allow it (three options):**

- **Right-click → Open** — right-click the app icon in Finder, choose **Open**, then click **Open** in the dialog. This
  is a one-time bypass and is the quickest option.
- **Remove the quarantine attribute** in Terminal:
  ```bash
  xattr -rd com.apple.quarantine /Applications/Renamer.app
  ```
  The `-rd` flags remove (`d`) the specific quarantine key recursively (`r`) without touching other extended attributes.
- **System Settings → Privacy & Security** — if the app was already blocked once, scroll to the bottom of the Privacy &
  Security pane; a _"Renamer was blocked"_ entry appears with an **Open Anyway** button.

Code signing is not currently implemented. This is an end-user distribution concern, not a code defect.

---

### Case-only rename on macOS and Windows

**Affected platforms:** macOS, Windows  
**Root cause:** `Files.move(oldPath, newPath)` treats a rename from `photo.jpg` to `Photo.jpg` as a no-op on
case-insensitive filesystems — both paths resolve to the same inode and the operation succeeds silently without changing
anything.

**How the code handles it** (`RenameExecutionServiceImpl`, line 89):

```java
var isCaseChange = oldPath.toAbsolutePath().toString()
        .equalsIgnoreCase(newPath.toAbsolutePath().toString());
```

When `isCaseChange` is `true`, the implementation falls back to `File.renameTo()`, which correctly performs case-only
renames on both macOS and Windows:

```java
if(isCaseChange){
boolean success = oldFile.renameTo(newPath.toFile());
// ...
}else{
        Files.

move(oldPath, newPath);
}
```

---

### Colon characters in filenames on Windows

**Affected platform:** Windows  
**Root cause:** Windows does not allow `:` in filenames. When a transformation produces a name like
`2024:01:01_photo.jpg`, calling `preparedFile.getNewPath()` (which calls `Path.resolveSibling()`) throws
`InvalidPathException` before the name validator can run — causing the wrong error code (`ERROR_EXECUTION` instead of
`ERROR_TRANSFORMATION`).

**How the code handles it** (`RenameExecutionServiceImpl`, lines 61–69):

```java
// Validate BEFORE calling getNewPath() — getNewPath() throws on Windows for names
// containing ':', which would mask the real error as ERROR_EXECUTION.
String finalName = preparedFile.getNewFullName();
if(!nameValidator.

isValid(finalName)){
        return RenameResult.

builder()
            .

withStatus(RenameStatus.ERROR_TRANSFORMATION)
            .

build();
}
// Only reach getNewPath() after the name is confirmed valid.
Path newPath = preparedFile.getNewPath();
```

`NameValidator` rejects Windows-illegal characters, reserved names (`CON`, `PRN`, etc.), and trailing dots/spaces.

---

### Sort ordering non-determinism on Linux

**Affected platform:** Linux (ext4, tmpfs)  
**Root cause:** `SequenceTransformer.sortBySource()` originally sorted by `FileModel::getName` (stem without extension)
only. Files with the same stem (`file.doc`, `file.pdf`, `file.txt`) compared equal, so `List.sort()` preserved the
insertion order returned by `Files.list()` — which is non-deterministic on ext4/tmpfs. macOS (APFS) happened to return
files in an order that matched the test assertions; Linux did not.

**How the code handles it:** All sort cases in `SequenceTransformer` now use multi-field comparators with deterministic
tiebreakers:

```java
case FILE_NAME ->sorted.

sort(Comparator.comparing(FileModel::getName)
        .

thenComparing(FileModel::getExtension)
        .

thenComparing(FileModel::getAbsolutePath));
```

All other sort cases (`FILE_SIZE`, `FILE_CREATION_DATETIME`, etc.) similarly append
`.thenComparing(FileModel::getName).thenComparing(FileModel::getAbsolutePath)`.

---

### `-Xdock:name=Renamer` JVM flag on Linux and Windows

**Affected platforms:** Linux, Windows  
**Root cause:** `-Xdock:name=Renamer` is a macOS-only JVM argument that sets the Dock app name. Linux and Windows JVMs
reject it with `Unrecognized option: -Xdock:name=Renamer`, causing Maven to fail immediately at startup.

**How the code handles it:** The flag is moved to a `macos-dock` Maven profile that activates automatically only on
macOS:

```xml

<profile>
    <id>macos-dock</id>
    <activation>
        <os>
            <family>mac</family>
        </os>
    </activation>
    <!-- sets javafx.jvm.args = -Xdock:name=Renamer -->
</profile>
```

On Linux and Windows, `javafx.jvm.args` defaults to empty. No action needed when running on non-macOS.

---

### `--linux-shortcut` flag invalid for `app-image` type

**Affected platform:** Linux  
**Root cause:** `--linux-shortcut` and `--linux-menu-group` are Linux installer flags valid only for `--type deb` and
`--type rpm`. Placing them in args shared with the `app-image` step caused jpackage to fail immediately.

**How the code handles it** (`scripts/package-linux.sh`): Args are split into `BASE_ARGS` (valid for all types) and
`DEB_ARGS` (installer-only). The `app-image` step uses `BASE_ARGS` only; the `deb` step uses both.

---

### Logback file lock on Windows

**Affected platform:** Windows  
**Root cause:** `LoggingConfigService.removeFileAppender()` called `detachAppender("FILE")` without first stopping the
appender. On Windows, Logback holds an OS-level file lock on `logs/renamer.log`. JUnit's `@TempDir` cleanup then fails
because the file handle is still open.

**How the code handles it:** `LoggingConfigService.removeFileAppender()` now calls `appender.stop()` before
`detachAppender()` to release the file handle on all platforms. Tests also call `stop()` in `@AfterEach` as a guard.

---

### Dot-prefix hidden files on Windows

**Affected platform:** Windows  
**Root cause:** `Files.isHidden()` on Windows checks the HIDDEN file attribute (set via file properties), not the
dot-prefix convention used on Unix. A file named `.hidden_file` created with `Files.createFile()` is visible on
Windows — no HIDDEN attribute is set.

**How the code handles it:** The `FolderExpansionServiceImpl` hidden-file filtering test is annotated
`@EnabledOnOs({OS.LINUX, OS.MAC})`. On Windows, the filtering logic still runs correctly for files that have the HIDDEN
attribute set — the test simply does not cover the dot-prefix variant on that platform.

---

### Unix path separators in cross-platform test assertions

**Affected platform:** Windows  
**Root cause:** `Path.of("/home/testuser").toString()` returns `\home\testuser` on Windows (backslash separators). Test
assertions that use `startsWith("/home/")` or `contains("/.config/")` fail on Windows even though the path logic itself
is correct.

**How the code handles it:** Tests that assert on Unix-specific paths are annotated `@EnabledOnOs(OS.LINUX)` or
`@EnabledOnOs(OS.MAC)`. The corresponding Windows branch test is annotated `@EnabledOnOs(OS.WINDOWS)`.

---

## 4. Packaging Prerequisites

| Platform | Tool                     | Purpose                                                       | Install                            |
|----------|--------------------------|---------------------------------------------------------------|------------------------------------|
| macOS    | Xcode Command Line Tools | Required by jpackage to build `.app` bundle and `.dmg`        | `xcode-select --install`           |
| Linux    | `fakeroot`               | Required by jpackage to build `.deb` package                  | `sudo apt-get install -y fakeroot` |
| Linux    | `dpkg`                   | Used internally by jpackage for `.deb`                        | Pre-installed on Ubuntu/Debian     |
| Windows  | WiX Toolset 3.x          | Would be needed for `.msi` installer — **not currently used** | —                                  |

`package-linux.sh` checks for `fakeroot` at startup and, if absent, skips the `.deb` step with a warning while still
building the app-image. An MSI installer for Windows is not currently scripted — `package-windows.bat` produces an
app-image directory only. WiX would be required if MSI packaging is added in the future.

---

## 5. Runtime Folder Structure

All packages bundle their own JRE. End users do not need Java installed.

**macOS:**

```
dist/
├── Renamer.app/
│   └── Contents/
│       ├── Info.plist
│       ├── MacOS/Renamer          (native Mach-O launcher)
│       ├── Resources/icon.icns
│       ├── app/                   (JARs + config)
│       └── runtime/               (embedded JRE, ~280–300 MB)
└── Renamer-<version>.dmg          (~200–220 MB distributable)
```

**Linux ARM64:**

```
dist/
├── Renamer/
│   ├── bin/Renamer                (ELF binary)
│   └── lib/
│       ├── app/                   (JARs + config)
│       └── runtime/               (embedded JRE)
└── renamer_<version>_arm64.deb    (~221 MB distributable)
```

**Windows (x86_64):**

```
dist\
└── Renamer\
    ├── Renamer.exe                (native launcher)
    ├── Renamer.ico
    ├── app\                       (JARs + config)
    └── runtime\                   (embedded JRE)
```

Windows currently has no installer — distribute `dist\Renamer\` as a zip archive.

---

## 6. Testing Considerations

### Sort order in NUMBER_FILES mode tests

Never assume `Files.list()` returns files in any particular order — the order differs between APFS, ext4, and tmpfs.
Tests that use `NUMBER_FILES` (sequence) mode must either:

- Sort the input list explicitly before passing to the orchestrator, or
- Assert on the set of output names, not their order

The `SequenceTransformer` itself is now deterministic for a given sorted input — the sort tiebreakers guarantee
consistent numbering — but the test must control what order files are submitted.

### Hidden file tests

Use `@EnabledOnOs({OS.LINUX, OS.MAC})` on any test that relies on dot-prefix name → `Files.isHidden()` returning `true`.
This behaviour does not hold on Windows.

### Path separator assertions

Avoid `String.contains("/")` or `startsWith("/...")` on `Path.toString()` results — use `@EnabledOnOs` to restrict
platform-specific path tests, or compare `Path` objects directly with `Path.equals()`.

### Windows colon test

The test `givenFilenameWithColonOnWindows_whenExecute_thenErrorTransformationReturned` is annotated
`@EnabledOnOs(OS.WINDOWS)` and does not run on the CI `ubuntu-latest` build job. Run it locally on a Windows environment
to verify colon-handling behaviour.

### Remaining known warnings (informational only)

These appear during builds but do not indicate defects:

| Warning                                                  | Where                  | Status                                                                                           |
|----------------------------------------------------------|------------------------|--------------------------------------------------------------------------------------------------|
| `sun.misc.Unsafe` Lombok warning                         | All modules, test runs | Lombok uses deprecated internals; will resolve when Lombok releases a Java 25–compatible version |
| `java.lang.System::load` JavaFX warning                  | UI tests               | JavaFX native library loader; suppressed with `--enable-native-access=javafx.graphics` if needed |
| PMD `NonSerializableClass` on `DateFormat`/`TimeFormat`  | `api` module           | These enums are not serialised in practice                                                       |
| PMD `CyclomaticComplexity` on orchestrator/extractors    | `core`, `metadata`     | Pre-existing; refactoring tracked separately                                                     |
| Checkstyle `MethodName` on test methods with underscores | Test classes           | `given_when_then` naming style with underscores                                                  |
