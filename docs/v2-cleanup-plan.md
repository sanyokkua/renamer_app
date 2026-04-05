# V2 Cleanup Plan

**Prepared:** 2026-04-05  
**Status:** Ready for implementation  
**Branch:** `feature/v2-cleanup` (recommended — create per task or all together)

---

## Executive Summary

Four housekeeping tasks remain after V1 was removed from the codebase. None changes user-visible behaviour — every
change is a structural refactor. Each task is independent and can be executed on a separate branch and merged
separately.

| # | Task                                                            | Risk   | Files changed |
|---|-----------------------------------------------------------------|--------|---------------|
| 1 | Consolidate `MimeTypes` into `AppMimeTypes`                     | Low    | 3             |
| 2 | Remove `StringUtilsV2` from `core`, move dispatcher to `utils`  | Medium | 6             |
| 3 | Fix UI module's forbidden dependencies on `core` and `metadata` | Medium | 6             |
| 4 | Remove stale comments and migration-era documentation           | Low    | 6             |

---

## Execution Order

All four tasks are **independent**. Recommended order by risk:

```
Task 1 (MimeTypes)        — no dependencies, lowest risk
Task 2 (StringUtilsV2)    — no dependencies, parallel with Task 1
Task 3 (UI architecture)  — no dependencies, parallel with Tasks 1–2
Task 4 (comments/docs)    — no dependencies, do last (cosmetic)
```

---

## Task 1 — Consolidate `MimeTypes` into `AppMimeTypes` (Done)

### Objective

`MimeTypes` (18 constants, with file-extension mappings) is a strict subset of `AppMimeTypes` (52 constants, no
extensions). Its only consumer is `ThreadAwareFileMapper.findExtensions()`. Extend `AppMimeTypes` with extension data,
migrate `ThreadAwareFileMapper`, delete `MimeTypes`.

### Affected Files

| File                                                                           | Change                                                              |
|--------------------------------------------------------------------------------|---------------------------------------------------------------------|
| `app/api/src/main/java/ua/renamer/app/api/enums/AppMimeTypes.java`             | Add extension field + constructor + methods + populate 18 constants |
| `app/core/src/main/java/ua/renamer/app/core/mapper/ThreadAwareFileMapper.java` | Swap import; simplify `findExtensions`                              |
| `app/api/src/main/java/ua/renamer/app/api/enums/MimeTypes.java`                | **Delete**                                                          |

### Step-by-Step Implementation

#### Step 1.1 — Extend `AppMimeTypes.java`

**Add imports** after `import lombok.Getter;`:

```java
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
```

**Replace the field block** (currently `private final String mime;`) with:

```java
private final String mime;
private final Set<String> extensions;
```

**Replace the constructor** (currently `AppMimeTypes(String mime)`) with:

```java
AppMimeTypes(String mime, String... extensions) {
    this.mime = mime;
    this.extensions = Arrays.stream(extensions).collect(Collectors.toUnmodifiableSet());
}
```

**Add `getExtensions()` method** (write explicitly; do not rely on Lombok picking up the new field):

```java
public Set<String> getExtensions() {
    return extensions;
}
```

**Add `getExtensionsByMimeString()` static method** at the end of the enum body, before closing `}`:

```java
/**
 * Returns all file extensions associated with the given MIME type string.
 *
 * @param mimeString the MIME type string to look up; may be null or empty
 * @return the set of extensions, or an empty set if not found
 */
public static Set<String> getExtensionsByMimeString(String mimeString) {
    if (Objects.isNull(mimeString) || mimeString.isEmpty()) {
        return Set.of();
    }
    return Arrays.stream(AppMimeTypes.values())
            .filter(mime -> mime.getMime().equalsIgnoreCase(mimeString))
            .findFirst()
            .map(AppMimeTypes::getExtensions)
            .orElse(Set.of());
}
```

**Update 18 constants with extension data** (the remaining 34 keep their single-argument form and will automatically
receive an empty set):

| Constant (current line)               | New argument list                                    |
|---------------------------------------|------------------------------------------------------|
| `APPLICATION_POSTSCRIPT` (line 10)    | `"application/postscript", ".eps", ".epsf", ".epsi"` |
| `AUDIO_MP4` (line 11)                 | `"audio/mp4", ".m4a", ".m4b", ".m4p", ".m4r"`        |
| `AUDIO_MPEG` (line 12)                | `"audio/mpeg", ".mp3"`                               |
| `AUDIO_WAV` (line 13)                 | `"audio/wav", ".wav", ".wave"`                       |
| `IMAGE_BMP` (line 30)                 | `"image/bmp", ".bmp"`                                |
| `IMAGE_GIF` (line 31)                 | `"image/gif", ".gif"`                                |
| `IMAGE_HEIC` (line 32)                | `"image/heic", ".heic"`                              |
| `IMAGE_HEIF` (line 33)                | `"image/heif", ".heif"`                              |
| `IMAGE_JPEG` (line 34)                | `"image/jpeg", ".jpg", ".jpeg", ".jpe"`              |
| `IMAGE_PNG` (line 35)                 | `"image/png", ".png"`                                |
| `IMAGE_TIFF` (line 36)                | `"image/tiff", ".tiff", ".tif"`                      |
| `IMAGE_VND_ADOBE_PHOTOSHOP` (line 37) | `"image/vnd.adobe.photoshop", ".psd"`                |
| `IMAGE_WEBP` (line 38)                | `"image/webp", ".webp"`                              |
| `IMAGE_X_ICON` (line 39)              | `"image/x-icon", ".ico"`                             |
| `IMAGE_X_PCX` (line 40)               | `"image/x-pcx", ".pcx"`                              |
| `VIDEO_MP4` (line 50)                 | `"video/mp4", ".mp4", ".m4v"`                        |
| `VIDEO_QUICKTIME` (line 51)           | `"video/quicktime", ".mov", ".qt"`                   |
| `VIDEO_X_MS_VIDEO` (line 52)          | `"video/x-msvideo", ".avi"`                          |

All other constants (lines 14–29, 41–49) are unchanged — they receive zero extension args and will have `Set.of()`.

#### Step 1.2 — Migrate `ThreadAwareFileMapper.java`

**Line 7** — replace:

```java
import ua.renamer.app.api.enums.MimeTypes;
```

with:

```java
import ua.renamer.app.api.enums.AppMimeTypes;
```

**Lines 92–98 (`findExtensions` method)** — replace the entire body with a delegation to the new static helper:

```java
private Set<String> findExtensions(String mimeType) {
    return AppMimeTypes.getExtensionsByMimeString(mimeType);
}
```

The static helper uses `equalsIgnoreCase`, preserving the original comparison semantics.

#### Step 1.3 — Delete `MimeTypes.java`

Delete `app/api/src/main/java/ua/renamer/app/api/enums/MimeTypes.java`.  
No other file references this class (only `ThreadAwareFileMapper` imported it).

### Risk Assessment

**Low.** The change is additive: new fields and methods on `AppMimeTypes`, and a one-line body swap in
`ThreadAwareFileMapper`. The 34 constants without extension data receive an empty `Set`, matching the existing
`orElse(Collections.emptySet())` fallback. The MIME extension cache in `ThreadAwareFileMapper` (`MIME_EXTENSIONS_CACHE`)
is unaffected — key and value types are unchanged.

The naming discrepancy (`VIDEO_X_MSVIDEO` in old `MimeTypes` vs `VIDEO_X_MS_VIDEO` in `AppMimeTypes`) is immaterial
because the lookup operates on the MIME string value `"video/x-msvideo"`, not the constant name.

### Verification Checklist

- [ ] `AppMimeTypes.java` compiles without errors
- [ ] `ThreadAwareFileMapper.java` compiles; zero references to `MimeTypes` remain
- [ ] `grep -rn "import.*MimeTypes" app/` returns zero results after deletion
- [ ] `AppMimeTypes.getExtensionsByMimeString("image/jpeg")` returns `{".jpg", ".jpeg", ".jpe"}`
- [ ] `AppMimeTypes.getExtensionsByMimeString("audio/flac")` returns `Set.of()` (no extensions defined)
- [ ] `AppMimeTypes.getExtensionsByMimeString(null)` returns `Set.of()` without throwing
- [ ] `mvn verify -pl app/api,app/core -am` passes

---

## Task 2 — Remove `StringUtilsV2`

### Objective

`StringUtilsV2` in `app/core/util/` is an acknowledged V2-specific copy of string utilities. Its only consumer is
`CaseChangeTransformer`, which calls the single method `toProvidedCase(String, TextCaseOptions)`. `CaseUtils` in
`app/utils` already has all individual case-conversion methods but is missing the dispatcher. Add `toProvidedCase` to
`CaseUtils` (requiring the `api` module be added as a dependency of `utils`), migrate `CaseChangeTransformer`, delete
`StringUtilsV2`.

### Affected Files

| File                                                                                           | Change                                                                                  |
|------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| `app/utils/pom.xml`                                                                            | Add `ua.renamer.app.api` dependency                                                     |
| `app/utils/src/main/java/module-info.java`                                                     | Add `requires ua.renamer.app.api`                                                       |
| `app/utils/src/main/java/ua/renamer/app/utils/text/CaseUtils.java`                             | Add `toProvidedCase` method + `TextCaseOptions` import                                  |
| `app/core/src/main/java/ua/renamer/app/core/service/transformation/CaseChangeTransformer.java` | Swap import + call site                                                                 |
| `app/core/src/main/java/ua/renamer/app/core/util/StringUtilsV2.java`                           | **Delete**                                                                              |
| `app/core/src/main/java/module-info.java`                                                      | Conditionally remove `exports`/`opens ua.renamer.app.core.util` if package is now empty |

### Step-by-Step Implementation

#### Step 2.1 — Add `api` dependency to the `utils` module

This is architecturally valid: the intended chain is `api → utils → …`, so `utils` is explicitly allowed to depend on
`api`.

**`app/utils/pom.xml`** — inside `<dependencies>`, add after the existing `lombok` dependency:

```xml

<dependency>
    <groupId>ua.renamer.app</groupId>
    <artifactId>ua.renamer.app.api</artifactId>
    <version>${project.version}</version>
</dependency>
```

**`app/utils/src/main/java/module-info.java`** — add after the existing `requires` lines:

```
requires ua.renamer.app.api;
```

#### Step 2.2 — Add `toProvidedCase` to `CaseUtils.java`

**Add import** at the top of the file (after existing imports):

```java
import ua.renamer.app.api.enums.TextCaseOptions;
```

**Add method** at the end of the class body, after `toTitleCase`:

```java
/**
 * Converts a string to the case specified by {@link TextCaseOptions}.
 *
 * @param inputString     the string to convert
 * @param textCaseOptions the target case
 * @return the converted string, or the original value if it is null or blank
 */
public static String toProvidedCase(final String inputString, TextCaseOptions textCaseOptions) {
    if (TextUtils.isEmpty(inputString)) {
        return inputString;
    }
    return switch (textCaseOptions) {
        case CAMEL_CASE -> toCamelCase(inputString);
        case PASCAL_CASE -> toPascalCase(inputString);
        case SNAKE_CASE -> toSnakeCase(inputString);
        case SNAKE_CASE_SCREAMING -> toScreamingSnakeCase(inputString);
        case KEBAB_CASE -> toKebabCase(inputString);
        case UPPERCASE -> toUppercase(inputString);
        case LOWERCASE -> toLowercase(inputString);
        case TITLE_CASE -> toTitleCase(inputString);
    };
}
```

This is a direct port of `StringUtilsV2.toProvidedCase`; the switch arms are identical. The null/blank check delegates
to `TextUtils.isEmpty`, matching the pattern used by all other `CaseUtils` methods.

#### Step 2.3 — Migrate `CaseChangeTransformer.java`

**Line 10** — replace:

```java
import ua.renamer.app.core.util.StringUtilsV2;
```

with:

```java
import ua.renamer.app.utils.text.CaseUtils;
```

**Line 34** — replace:

```java
String newName = StringUtilsV2.toProvidedCase(input.getName(), config.getCaseOption());
```

with:

```java
String newName = CaseUtils.toProvidedCase(input.getName(), config.getCaseOption());
```

No other changes to this file.

#### Step 2.4 — Delete `StringUtilsV2.java`

Delete `app/core/src/main/java/ua/renamer/app/core/util/StringUtilsV2.java`.

#### Step 2.5 — Clean up `core/module-info.java` if package is now empty

Run: `ls app/core/src/main/java/ua/renamer/app/core/util/`

If the directory is empty (no files remain), remove the following two lines from
`app/core/src/main/java/module-info.java`:

```
exports ua.renamer.app.core.util;
```

```
opens ua.renamer.app.core.util;
```

If other classes still exist in the package, leave those lines in place.

### Risk Assessment

**Medium.** Behavioural risk is low — `toProvidedCase` logic is copied verbatim and `TextCaseOptions` has exactly the 8
cases handled in both switch statements. The structural risk is the new `api` dependency added to `utils`. While
architecturally valid (`api → utils`), it changes the module graph; any ArchUnit tests or IDE inspection rules that
assert `utils` has no external module deps should be updated accordingly.

### Verification Checklist

- [ ] `CaseUtils.java` compiles; `toProvidedCase` is accessible from `core`
- [ ] `CaseChangeTransformer.java` compiles; zero references to `StringUtilsV2` remain
- [ ] `grep -rn "StringUtilsV2" app/` returns zero results
- [ ] `CaseUtils.toProvidedCase("hello world", TextCaseOptions.CAMEL_CASE)` returns `"helloWorld"`
- [ ] `CaseUtils.toProvidedCase("hello world", TextCaseOptions.PASCAL_CASE)` returns `"HelloWorld"`
- [ ] `CaseUtils.toProvidedCase(null, TextCaseOptions.UPPERCASE)` returns `null` without throwing
- [ ] If `ua.renamer.app.core.util` is now empty, exports/opens directives are removed from `core/module-info.java`
- [ ] New unit tests added for `toProvidedCase` covering all 8 `TextCaseOptions` branches
- [ ] `mvn verify -pl app/utils,app/core -am` passes

---

## Task 3 — Fix UI Module Architecture Violations

### Objective

The `ui` module directly depends on `core` and `metadata`, violating the intended architecture where `ui` should only
see `api` and `backend`. The root cause is `DICoreModule` in the `ui` config package, which installs `DIMetadataModule`
and `DIV2ServiceModule` — responsibilities that belong in `DIBackendModule`. Move those installs to `backend`, add
`metadata` as a `backend` dependency, then strip `metadata` from `ui`.

### Architecture Context

**Intended dependency chain:** `api → utils → metadata → core → backend → ui`

**Current (incorrect) DI wiring:**

```
ui: DICoreModule.configure()
  ├── install(new DIMetadataModule())    ← belongs in backend
  ├── install(new DIV2ServiceModule())   ← belongs in backend
  ├── bind(NameValidator)
  └── @Provides TextExtractorByKey
```

**Target (correct) wiring:**

```
backend: DIBackendModule.configure()
  ├── install(new DIMetadataModule())    ← moved here
  ├── install(new DIV2ServiceModule())   ← moved here
  ├── bind(BackendExecutor)
  └── bind(SessionApi → RenameSessionService)

ui: DICoreModule.configure()
  ├── bind(NameValidator)                ← stays (UI-layer validation)
  └── @Provides TextExtractorByKey      ← stays (bridges UI i18n into core interface)
```

**Note on the remaining `core` dependency in `ui`:** After this task, `ui` will still require `ua.renamer.app.core`
because `DICoreModule` imports `NameValidator` (from `core.service.validator.impl`) and `TextExtractorByKey` (from
`core.service`). The `TextExtractorByKey` `@Provides` method has a UI-specific body (
`languageTextRetrieverApi.getString(…)`) and cannot move to `backend` without creating a circular dependency. Full
resolution requires promoting `TextExtractorByKey` from `core` to `api` — document as a follow-up task (see below). This
task eliminates the `metadata` violation entirely and does not worsen the `core` situation.

### Affected Files

| File                                                                           | Change                                                                       |
|--------------------------------------------------------------------------------|------------------------------------------------------------------------------|
| `app/backend/src/main/java/ua/renamer/app/backend/config/DIBackendModule.java` | Add `install(new DIMetadataModule())` and `install(new DIV2ServiceModule())` |
| `app/backend/pom.xml`                                                          | Add `ua.renamer.app.metadata` dependency                                     |
| `app/backend/src/main/java/module-info.java`                                   | Add `requires ua.renamer.app.metadata`                                       |
| `app/ui/src/main/java/ua/renamer/app/ui/config/DICoreModule.java`              | Remove the two install calls and their cross-module imports                  |
| `app/ui/pom.xml`                                                               | Remove `ua.renamer.app.metadata` dependency block                            |
| `app/ui/src/main/java/module-info.java`                                        | Remove `requires ua.renamer.app.metadata`                                    |

### Step-by-Step Implementation

#### Step 3.1 — Extend `DIBackendModule.java`

**Add imports** at the top of `DIBackendModule.java` (after existing imports):

```java
import ua.renamer.app.core.config.DIV2ServiceModule;
import ua.renamer.app.metadata.config.DIMetadataModule;
```

**Prepend two install calls** inside `configure()`, before the existing `bind` calls:

```java
install(new DIMetadataModule());

install(new DIV2ServiceModule());
```

Update the class-level Javadoc to note that this module now also installs the metadata and core-service sub-modules.

#### Step 3.2 — Add `metadata` dependency to `backend`

**`app/backend/pom.xml`** — add inside `<dependencies>` after the existing `core` dependency:

```xml

<dependency>
    <groupId>ua.renamer.app</groupId>
    <artifactId>ua.renamer.app.metadata</artifactId>
    <version>${project.version}</version>
</dependency>
```

**`app/backend/src/main/java/module-info.java`** — add after `requires ua.renamer.app.core;`:

```
requires ua.renamer.app.metadata;
```

This is architecturally valid: `backend` is downstream of `metadata` in the intended chain.

#### Step 3.3 — Simplify `DICoreModule.java` in UI

**Remove the two cross-module imports** (lines 8 and 11 in the current file):

```java
// DELETE:

import ua.renamer.app.core.config.DIV2ServiceModule;
import ua.renamer.app.metadata.config.DIMetadataModule;
```

**Remove the install calls** from the `configure()` method (along with their comment):

```java
// DELETE these two lines (and any associated comment like "// Install v2 services..."):
install(new DIMetadataModule());

install(new DIV2ServiceModule());
```

The imports for `TextExtractorByKey` (line 9) and `NameValidator` (line 10) remain — they are needed for the retained
`@Provides` method and `bind` call.

#### Step 3.4 — Remove `metadata` from `ui/pom.xml`

Delete the entire `ua.renamer.app.metadata` dependency block (approximately lines 30–33):

```xml
<!-- DELETE: -->
<dependency>
    <groupId>ua.renamer.app</groupId>
    <artifactId>ua.renamer.app.metadata</artifactId>
    <version>${project.version}</version>
</dependency>
```

The `ua.renamer.app.core` dependency block is retained.

#### Step 3.5 — Remove `metadata` from `ui/module-info.java`

Delete the line (approximately line 14):

```
requires ua.renamer.app.metadata;
```

The `requires ua.renamer.app.core;` line is retained.

#### Follow-Up Task (Out of Scope — Document as ADR)

To fully eliminate `ui`'s dependency on `core`, promote `TextExtractorByKey` from `ua.renamer.app.core.service` to
`ua.renamer.app.api` (or a new `ua.renamer.app.api.service` package). This is a larger change requiring an interface
extraction and module re-wiring — track separately.

### Risk Assessment

**Medium.** The DI graph must remain consistent at runtime. Moving `install(new DIMetadataModule())` and
`install(new DIV2ServiceModule())` from `DICoreModule` to `DIBackendModule` is safe only if both modules are not
installed twice in the same `Injector.createInjector(…)` call. Before implementing, verify the UI bootstrap (
`RenamerApplication.java` or similar) constructs the injector with a single call that includes both `DIBackendModule`
and `DICoreModule` — the installs will then happen exactly once through `DIBackendModule`.

If any integration test constructs an injector with only `DICoreModule` (and relied on the `install` calls that are
being removed), those tests will break and need to be updated to install `DIBackendModule` instead.

### Verification Checklist

- [ ] `DIBackendModule.java` compiles with new imports; `mvn compile -pl app/backend -am` passes
- [ ] `DICoreModule.java` compiles; no imports from `ua.renamer.app.core.config` or `ua.renamer.app.metadata`
- [ ] `ui/module-info.java` no longer contains `requires ua.renamer.app.metadata`
- [ ] `ui/pom.xml` no longer contains `ua.renamer.app.metadata` as a dependency
- [ ] Application starts and the full rename workflow executes end-to-end
- [ ] Metadata extraction works for image, audio, and video files
- [ ] `mvn verify` passes for all modules

---

## Task 4 — Remove Stale Comments and Migration-Era Docs

### Objective

Remove inline comments that reference V1 and simplify a misleading comment in the launcher. Delete four documentation
files that describe a dual V1/V2 architecture that no longer exists — V1 was removed in commit `3f7e71f`.

### Affected Files — Inline Comments

| File                                                                                         | Location   | Issue                                                     | Fix                                            |
|----------------------------------------------------------------------------------------------|------------|-----------------------------------------------------------|------------------------------------------------|
| `app/core/src/main/java/ua/renamer/app/core/service/transformation/SequenceTransformer.java` | Line 59    | `// Step 2: Sort valid files by criteria (just like v1)`  | Remove the `(just like v1)` suffix             |
| `app/ui/src/main/java/ua/renamer/app/Launcher.java`                                          | Lines 7–11 | Comment refers to "SuperMain" and "only a temporary name" | Rewrite comment to remove historical artefacts |

#### `SequenceTransformer.java` line 59

Replace:

```java
// Step 2: Sort valid files by criteria (just like v1)
```

with:

```java
// Step 2: Sort valid files by criteria
```

#### `Launcher.java` lines 7–11

Replace the current multi-line comment:

```java
// This launcher is needed to run JavaFX app after build it into jar.
// https://stackoverflow.com/questions/57019143/build-executable-jar-with-javafx11-from-maven
// the jar needs to know the actual Main class that does not extend Application,
// so I just created another Main class called SuperMain (it was only a temporary name)
// that calls my original main class, which is Main
```

with:

```java
// Required to launch a JavaFX application from an executable jar.
// The manifest must reference a class that does not extend Application.
// https://stackoverflow.com/questions/57019143/build-executable-jar-with-javafx11-from-maven
```

### Affected Files — Documentation

The following files describe the V1/V2 migration or the dual-architecture that existed during migration. V1 was fully
removed in commit `3f7e71f`. These documents are no longer accurate and offer no value to future contributors.

| File                               | Action |
|------------------------------------|--------|
| `docs/V2_STABILIZATION_PLAN.md`    | Delete |
| `docs/V2_API_DESIGN_APPROACHES.md` | Delete |
| `docs/IMPLEMENTATION_PLAN.md`      | Delete |
| `docs/UI_BACKEND_ARCHITECTURE.md`  | Delete |

All content is preserved in git history. If the team prefers to keep them for reference, create `docs/archive/` and move
them there instead of deleting.

**Do not delete `docs/ARCHITECTURE.md`** — it describes the current architecture. Consider a follow-up editorial pass to
remove any remaining V1 references.

### Risk Assessment

**Low.** Comment changes are not compiled. Documentation deletion has no runtime impact.

### Verification Checklist

- [ ] `SequenceTransformer.java` line 59 no longer contains `(just like v1)`
- [ ] `Launcher.java` comment no longer mentions "SuperMain" or "temporary name"
- [ ] Four migration-era docs are deleted (or archived)
- [ ] `mvn verify` still passes (sanity-check that no build script referenced a deleted doc)

---

## Rollback Strategy

Each task touches a bounded, non-overlapping set of files. To revert any task independently:

**Task 1:**
`git checkout -- app/api/src/main/java/ua/renamer/app/api/enums/AppMimeTypes.java app/core/src/main/java/ua/renamer/app/core/mapper/ThreadAwareFileMapper.java`
and restore `MimeTypes.java` from the commit before it was deleted.

**Task 2:**
`git checkout -- app/utils/pom.xml app/utils/src/main/java/module-info.java app/utils/src/main/java/ua/renamer/app/utils/text/CaseUtils.java app/core/src/main/java/ua/renamer/app/core/service/transformation/CaseChangeTransformer.java app/core/src/main/java/module-info.java`
and restore `StringUtilsV2.java` from the commit before deletion.

**Task 3:**
`git checkout -- app/backend/src/main/java/ua/renamer/app/backend/config/DIBackendModule.java app/backend/pom.xml app/backend/src/main/java/module-info.java app/ui/src/main/java/ua/renamer/app/ui/config/DICoreModule.java app/ui/pom.xml app/ui/src/main/java/module-info.java`

**Task 4:**
`git checkout -- app/core/src/main/java/ua/renamer/app/core/service/transformation/SequenceTransformer.java app/ui/src/main/java/ua/renamer/app/Launcher.java`
and restore deleted docs from the commit before deletion.

---

## Definition of Done

The V2 cleanup is complete when **all** of the following are true:

1. `grep -rn "import.*[^p]MimeTypes" app/` (excluding `AppMimeTypes`) returns zero results.
2. `grep -rn "StringUtilsV2" app/` returns zero results.
3. `ui/pom.xml` has no dependency on `ua.renamer.app.metadata`.
4. `ui/module-info.java` has no `requires ua.renamer.app.metadata`.
5. `DICoreModule.java` has no imports from `ua.renamer.app.core.config` or `ua.renamer.app.metadata`.
6. `DIBackendModule.java` installs both `DIMetadataModule` and `DIV2ServiceModule`.
7. No source file in `app/` contains `(just like v1)` or "SuperMain" in a comment.
8. The four migration-era docs are deleted or moved to `docs/archive/`.
9. Unit tests for `CaseUtils.toProvidedCase` cover all 8 `TextCaseOptions` branches.
10. `mvn verify` passes clean for all modules with no new warnings.
