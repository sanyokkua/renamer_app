# Renamer App — V2 Migration Implementation Plan

**Document Type:** Master Implementation Plan
**Status:** Ready for Implementation
**Created:** 2026-04-01
**Approach:** Approach 3 — Pragmatic Facade (SessionApi without Generic UI Generation)
**Source documents:**

- `docs/V2_STABILIZATION_PLAN.md` — Phase 1 bug/feature/stability inventory
- `docs/V2_API_DESIGN_APPROACHES.md` — Architecture decision and phase plan
- `docs/MODE_STATE_MACHINES.md` — Per-mode state machines, validation rules, edge cases
- `docs/JavaFX_Backend_UI_Architecture_Guideline.md` — Target architecture reference

---

## Overview

The Renamer App has two fully implemented backend generations (V1 Command Pattern, V2 Strategy+Pipeline) where only V1
is wired to the UI. This plan migrates the UI to use V2 exclusively, via a new `SessionApi` facade, while keeping all 10
FXML-backed mode controllers intact.

**The plan is divided into four parts:**

| Part       | Focus                      | Goal                                                              |
|------------|----------------------------|-------------------------------------------------------------------|
| **Part 1** | V2 Stabilization           | Fix V2 bugs, restore missing V1 features, harden null-safety      |
| **Part 2** | Structural Foundation      | Create API interfaces, module structure, ModeParameters hierarchy |
| **Part 3** | Backend API Implementation | Implement RenameSessionService, BackendExecutor, SessionApiImpl   |
| **Part 4** | UI Connection              | Implement FxStateMirror, migrate 10 mode controllers, remove V1   |

**Golden rule:** The app must compile and run after every task. No task should leave the build broken.

---

## Current Architecture (Starting Point)

```
app/api/      — V2 config classes (AddTextConfig, etc.), enums, interfaces
app/core/     — V2 pipeline (FileRenameOrchestratorImpl), V1 command classes, transformers
app/ui/       — JavaFX controllers, FXML files; uses ONLY V1 (CoreFunctionalityHelper)
app/metadata/ — Metadata extraction helpers
app/utils/    — Standalone utilities (not imported by core or ui)
```

**V2 pipeline (app/core, never called by UI):**

1. Phase 1: `ThreadAwareFileMapper` — `File → FileModel` (parallel, virtual threads)
2. Phase 2: 10 `FileTransformationService` impls — `FileModel → PreparedFileModel` (parallel; sequential for
   ADD_SEQUENCE)
3. Phase 3: `DuplicateNameResolverImpl` — appends ` (01)`, ` (02)` for in-batch conflicts (sequential)
4. Phase 4: `RenameExecutionServiceImpl` — `PreparedFileModel → RenameResult` (parallel, physical rename)

**V2 never throws** — errors captured in `hasError`/`RenameStatus` fields.

---

## Part 1 — V2 Stabilization

**Goal:** Fix 4 bugs, restore 3 missing V1 features, harden stability across all 10 transformers and configs.
**Completion gate:** `mvn test -q -ff -Dai=true` passes (zero failures) + `mvn verify -Pcode-quality -q` passes.

Tasks must be executed in the order listed (each round has no internal dependencies; rounds are ordered by dependency).

---

### TASK-1.1 — BUG-3: ThreadAwareFileMapper null-safety after metadata extraction

**Priority:** Round 1 (no dependencies)
**Status:** COMPLETED

**Goal:** Prevent a potential `NullPointerException` when the metadata chain-of-responsibility returns null for an
unrecognized file type.

**Context:**
`ThreadAwareFileMapper.map(File)` calls `fileMetadataMapper.extract(file, category, mimeType)`. The
chain-of-responsibility pattern returns `null` if no handler recognizes the file type. This raw `null` propagates into
`FileModel`, which expects `Optional<FileMeta>`, not a bare null. If `Optional<FileMeta>` is stored as `null` (rather
than `Optional.empty()`), downstream transformers that call `input.getMetadata().orElse(null)` will throw
`NullPointerException`.

**Affected files:**

- `app/core/src/main/java/ua/renamer/app/core/v2/mapper/ThreadAwareFileMapper.java`

**Implementation:**

1. Read `ThreadAwareFileMapper.java` to find the line where `fileMetadataMapper.extract(...)` is called.
2. After the call, wrap the result:
   ```java
   FileMeta rawMeta = fileMetadataMapper.extract(file, category, mimeType);
   Optional<FileMeta> metadata = Optional.ofNullable(rawMeta);
   ```
3. Pass `metadata` (the `Optional`) to `FileModel` builder — never pass `rawMeta` directly.
4. Review `FileModel.getMetadata()` return type — confirm it is `Optional<FileMeta>`. If not, note this as a separate
   finding for the coder.

**Acceptance criteria (JUnit 5):**
Location: `app/core/src/test/java/ua/renamer/app/core/v2/mapper/ThreadAwareFileMapperTest.java`

- `givenUnknownFileType_whenMapped_thenMetadataIsEmptyOptional()` — result is `Optional.empty()`, not null, not NPE
- `givenNullFromMetadataChain_whenMapped_thenFileModelMetadataIsEmptyOptional()` — force chain to return null via mock;
  explicitly validates `Optional.ofNullable()` wrapping
- `givenKnownFileType_whenMapped_thenMetadataIsPresentOptional()` — happy path

**Build check:** `mvn test -q -ff -Dai=true -Dtest=ThreadAwareFileMapperTest`

---

### TASK-1.2 — BUG-2: SequenceConfig/SequenceTransformer negative padding

**Priority:** Round 1 (no dependencies)
**Status:** COMPLETED

**Goal:** Fail fast at config construction when `padding < 0`; add a defensive guard in `formatSequenceNumber()` so the
transformer never crashes even if validation is bypassed.

**Context:**
`SequenceTransformer.formatSequenceNumber(int number, int padding)` calls:

```java
String.format("%0"+padding +"d", number)
```

If `padding` is negative (user types `-1` in a spinner), `String.format` throws `MissingFormatWidthException`. Currently
this propagates as `ERROR_TRANSFORMATION` with an opaque message. Root cause should be caught at config construction
time, not pipeline execution time.

**Affected files:**

- `app/api/src/main/java/ua/renamer/app/api/model/config/SequenceConfig.java`
- `app/core/src/main/java/ua/renamer/app/core/v2/service/transformation/SequenceTransformer.java`

**Implementation:**

`SequenceConfig.java`:

- Add a compact constructor (or override `@Builder`'s build method) that validates:
  ```java
  if (padding < 0) {
      throw new IllegalArgumentException("padding must be >= 0, got: " + padding);
  }
  ```
- Note: `SequenceConfig` uses `@Value @Builder(setterPrefix = "with")` (Lombok). Use a static factory or add
  `@Builder.ObtainVia` + validation method. The simplest approach: add a static `validated(...)` factory method and keep
  `@Value`. See STABILITY-2 for the general config validation pattern.

`SequenceTransformer.java`:

- In `formatSequenceNumber(int number, int padding)`, add a guard:
  ```java
  if (padding <= 0) {
      return String.valueOf(number); // No padding: raw number
  }
  return String.format("%0" + padding + "d", number);
  ```
- This makes the transformer safe even if a negative-padding config bypasses construction validation.

**Acceptance criteria (JUnit 5):**
Location: `app/core/src/test/java/ua/renamer/app/core/v2/service/transformation/SequenceTransformerTest.java`
and `app/api/src/test/java/ua/renamer/app/api/model/config/SequenceConfigTest.java`

- `givenNegativePadding_whenConfigConstructed_thenIllegalArgumentExceptionThrown()`
- `givenZeroPadding_whenFormatSequenceNumber_thenRawNumberReturned()`
- `givenPositivePadding_whenFormatSequenceNumber_thenZeroPaddedStringReturned()`
- `givenNegativeStartNumber_whenFormatSequenceNumber_thenNegativeNumberReturnedWithoutCrash()`

---

### TASK-1.3 — STABILITY-2: Constructor/builder validation in all 10 config classes

**Priority:** Round 1 (no dependencies — TASK-1.2 does SequenceConfig, so this covers the other 9)
**Status:** COMPLETED

**Goal:** All V2 config classes must reject invalid state at construction time, not at transformation time.

**Context:**
All 10 V2 config classes use `@Value @Builder(setterPrefix = "with")` (Lombok). Lombok generates an all-args constructor
with no validation. Invalid values are discovered deep inside the pipeline, producing confusing error messages.
Validating at construction provides clear failure points and makes misconfiguration impossible.

**Affected files (one validation per file):**

```
app/api/src/main/java/ua/renamer/app/api/model/config/
  DateTimeConfig.java
  ImageDimensionsConfig.java
  TruncateConfig.java
  ExtensionChangeConfig.java
  AddTextConfig.java
  RemoveTextConfig.java
  ReplaceTextConfig.java
  CaseChangeConfig.java
  ParentFolderConfig.java
```

(SequenceConfig already covered in TASK-1.2)

**Implementation pattern:**
Since `@Value` generates an immutable class with a Lombok-generated constructor, the cleanest approach is to add a
`@Builder.ObtainVia` or a custom builder `build()` method that calls a `validate()` helper. The recommended pattern for
each:

```java
// Option: override builder's build() in a nested static class
@Value
@Builder(setterPrefix = "with", builderMethodName = "builder")
public class AddTextConfig {
    String textToAdd;
    ItemPosition position;

    // Nested class to override build() with validation
    public static class AddTextConfigBuilder {
        public AddTextConfig build() {
            AddTextConfig config = new AddTextConfig(textToAdd, position);
            config.validate();
            return config;
        }
    }

    private void validate() {
        Objects.requireNonNull(position, "position must not be null");
        Objects.requireNonNull(textToAdd, "textToAdd must not be null");
    }
}
```

Alternatively, add validation in a compact constructor if migrating to Java records.

**Validation rules per config:**

| Config                  | Required non-null                                | Numeric constraints          | Business rules                                                  |
|-------------------------|--------------------------------------------------|------------------------------|-----------------------------------------------------------------|
| `DateTimeConfig`        | `source`, `dateFormat`, `timeFormat`, `position` | —                            | If `source == CUSTOM_DATE`, `customDateTime` must be present    |
| `ImageDimensionsConfig` | `position`                                       | —                            | At least one of `leftSide`/`rightSide` must not be `DO_NOT_USE` |
| `TruncateConfig`        | `truncateOption`                                 | `numberOfSymbols >= 0`       | —                                                               |
| `ExtensionChangeConfig` | `newExtension`                                   | —                            | `newExtension` must not be blank after trim                     |
| `AddTextConfig`         | `position`, `textToAdd`                          | —                            | —                                                               |
| `RemoveTextConfig`      | `position`, `textToRemove`                       | —                            | —                                                               |
| `ReplaceTextConfig`     | `position`, `textToReplace`, `replacementText`   | —                            | —                                                               |
| `CaseChangeConfig`      | `caseOption`                                     | —                            | —                                                               |
| `ParentFolderConfig`    | `position`                                       | `numberOfParentFolders >= 1` | —                                                               |

**Acceptance criteria (JUnit 5):**
One test class per config: `XxxConfigValidationTest` in `app/api/src/test/...`

- `givenNullRequiredField_whenBuild_thenNullPointerExceptionOrIllegalArgumentException()`
- `givenInvalidNumericValue_whenBuild_thenIllegalArgumentException()`
- `givenValidParams_whenBuild_thenConfigCreatedSuccessfully()`

**Build check:** `mvn test -q -ff -Dai=true -pl app/api`

---

### TASK-1.4 — BUG-1/FEATURE-4: ImageDimensionsConfig configurable `nameSeparator` ✅ DONE

**Priority:** Round 2 (after TASK-1.3 so config validation pattern is established)

**Goal:** Add a configurable `nameSeparator` field to `ImageDimensionsConfig` and use it in
`ImageDimensionsTransformer`, replacing the hardcoded `" "` (space).

**Context:**
V1's `ImageDimensionsPrepareInformationCommand` accepted a `nameSeparator` constructor parameter. The user could
configure it to `"_"`, `"-"`, or any separator. V2's `ImageDimensionsTransformer` hardcodes `" "` between the dimension
block and the filename, making this a bug (user-visible regression) and a missing feature simultaneously.

Example broken behavior:

```
// User wants: "1920x1080_photo.jpg" (separator = "_")
// V2 produces: "1920x1080 photo.jpg" (hardcoded space — wrong)
```

**Affected files:**

- `app/api/src/main/java/ua/renamer/app/api/model/config/ImageDimensionsConfig.java`
- `app/core/src/main/java/ua/renamer/app/core/v2/service/transformation/ImageDimensionsTransformer.java`

**Implementation:**

`ImageDimensionsConfig.java`:

- Add field: `String nameSeparator`
- Set `@Builder.Default` value to `""` (empty string — explicit no-separator, closest neutral default)
- The field must be non-null; add to validation:
  `Objects.requireNonNull(nameSeparator, "nameSeparator must not be null (use empty string for no separator)")`

`ImageDimensionsTransformer.java`:

- Find the `BEGIN` case where it builds the output name. Replace:
  ```java
  dimensionStr + " " + input.getName()
  ```
  with:
  ```java
  dimensionStr + config.getNameSeparator() + input.getName()
  ```
- Find the `END` case. Replace:
  ```java
  input.getName() + " " + dimensionStr
  ```
  with:
  ```java
  input.getName() + config.getNameSeparator() + dimensionStr
  ```
- The `REPLACE` case (full name replacement with dimension string) is unaffected — no separator needed.

**Acceptance criteria (JUnit 5):**
Location: `app/core/src/test/java/ua/renamer/app/core/v2/service/transformation/ImageDimensionsTransformerTest.java`

- `givenNameSeparatorUnderscore_whenPositionBegin_thenDimensionAndNameJoinedWithUnderscore()`
- `givenNameSeparatorEmpty_whenPositionEnd_thenDimensionAndNameConcatenatedDirectly()`
- `givenNameSeparatorSpace_whenPositionBegin_thenSpaceInserted()` — verifies old space behavior achievable with explicit
  config
- `givenImageDimensionsConfig_whenNameSeparatorNull_thenValidationThrows()` — null rejected at construction

---

### TASK-1.5 — FEATURE-1: DateTimeTransformer — restore `useFallbackDateTime`

**Priority:** Round 2 (after TASK-1.3)
**Status:** COMPLETED

**Goal:** When the primary datetime source returns null, and `useFallbackDateTime = true`, use the earliest of all
available dates (creation, modification, content creation) instead of immediately returning `ERROR_TRANSFORMATION`.

**Context (from V1 behavior):**
V1's `DateTimeRenamePrepareInformationCommand` had `useFallbackDateTime: boolean`. When the user selects
`CONTENT_CREATION_DATE` as source but the file has no EXIF data (e.g., a PNG or plain document), V1 would find the
minimum of `fsCreationDate`, `fsModificationDate`, `contentCreationDate`. V2 immediately returns `ERROR_TRANSFORMATION`
with no fallback. This breaks V1 workflows where users relied on this fallback for mixed media libraries.

**Affected files:**

- `app/api/src/main/java/ua/renamer/app/api/model/config/DateTimeConfig.java`
- `app/core/src/main/java/ua/renamer/app/core/v2/service/transformation/DateTimeTransformer.java`

**Implementation:**

`DateTimeConfig.java`:

- Add field: `boolean useFallbackDateTime` (default: `false` — preserves current V2 behavior)

`DateTimeTransformer.java`:

- In `extractDateTime()` (or equivalent method), after the primary source extraction returns null, add:
  ```java
  if (dateTime == null && config.isUseFallbackDateTime()) {
      LocalDateTime creation    = input.getCreationDate().orElse(null);
      LocalDateTime modification = input.getModificationDate().orElse(null);
      LocalDateTime contentCreation = extractContentCreationDate(input); // existing helper
      dateTime = Stream.of(creation, modification, contentCreation)
                       .filter(Objects::nonNull)
                       .min(Comparator.naturalOrder())
                       .orElse(null);
  }
  ```
- If after fallback `dateTime` is still null, return `ERROR_TRANSFORMATION` as before.
- The fallback must NOT be triggered when the primary source returned a value — only on null primary.

**Acceptance criteria (JUnit 5):**
Location: `app/core/src/test/java/ua/renamer/app/core/v2/service/transformation/DateTimeTransformerFallbackTest.java`

- `givenContentCreationDateMissingAndFallbackEnabled_whenExtract_thenEarliestAvailableDateUsed()`
- `givenAllDatesNullAndFallbackEnabled_whenExtract_thenErrorTransformationReturned()`
- `givenFallbackDisabled_whenPrimarySourceNull_thenErrorTransformationReturned()` — existing V2 behavior preserved
- `givenFallbackEnabled_whenPrimarySourcePresent_thenPrimaryUsedNotFallback()`

---

### TASK-1.6 — FEATURE-3: DateTimeTransformer — restore `useUppercaseForAmPm`

**Priority:** Round 2 (parallel with TASK-1.5, both touch same files)
**Status:** COMPLETED

**Goal:** After datetime formatting, when an AM/PM time format is used, apply `toUpperCase()` or `toLowerCase()` based
on the `useUppercaseForAmPm` flag.

**Context:**
V1 had `useUppercaseForAmPm: boolean`. When the user picks a `TimeFormat` that includes AM/PM (Java format character
`a`), the locale-dependent result (AM/PM vs am/pm) was controlled by this flag. V2 uses whatever `DateTimeFormatter`
returns, which is locale-dependent. The flag ensures predictable, locale-independent behavior in filenames.

**Affected files:**

- `app/api/src/main/java/ua/renamer/app/api/model/config/DateTimeConfig.java`
- `app/core/src/main/java/ua/renamer/app/core/v2/service/transformation/DateTimeTransformer.java`

**Implementation:**

`DateTimeConfig.java`:

- Add field: `boolean useUppercaseForAmPm` (default: `true` — uppercase AM/PM is conventional in filenames)

`DateTimeTransformer.java`:

- After formatting datetime to string, add:
  ```java
  private boolean isAmPmFormat(TimeFormat timeFormat) {
      return timeFormat.getPattern().contains("a"); // 'a' is Java AM/PM designator
  }

  // After formatting:
  if (isAmPmFormat(config.getTimeFormat())) {
      formattedDatetime = config.isUseUppercaseForAmPm()
          ? formattedDatetime.toUpperCase()
          : formattedDatetime.toLowerCase();
  }
  ```
- `toUpperCase()` / `toLowerCase()` on the full string is safe because the date portion uses digits and separators (
  unaffected by case). Only AM/PM contains letters in an AM/PM formatted string.

**Note:** If TASK-1.5 is being implemented in the same sitting, coordinate changes to `DateTimeConfig.java` — one coder
session should handle all three `DateTimeConfig` field additions (TASK-1.5, TASK-1.6, TASK-1.7 together to avoid
conflicts).

**Acceptance criteria (JUnit 5):**
Location: `app/core/src/test/java/ua/renamer/app/core/v2/service/transformation/DateTimeTransformerAmPmTest.java`

- `givenAmPmFormatAndUppercaseTrue_whenFormat_thenResultContainsUppercaseAMPM()`
- `givenAmPmFormatAndUppercaseFalse_whenFormat_thenResultContainsLowercaseAmPm()`
- `givenNonAmPmFormat_whenFormat_thenUppercaseFlagHasNoEffect()`
- `givenAmPmFormatAndUppercaseTrue_whenDatePortionHasLetters_thenOnlyAmPmAffected()` — edge case for locales where
  month/day abbreviations may contain letters; confirms only AM/PM designator changes case

---

### TASK-1.7 — FEATURE-2: DateTimeTransformer — restore `useCustomDateTimeAsFallback`

**Priority:** Round 3 (depends on TASK-1.5 — fallback logic must exist first)
**Status:** COMPLETED

**Goal:** When `useFallbackDateTime = true` and all natural dates are null, use the user-provided `customDateTime` as
the ultimate fallback instead of returning `ERROR_TRANSFORMATION`.

**Context:**
V1 allowed the user to specify a `customDateTime` that serves as a guaranteed fallback. This is useful for
batch-processing files with no metadata (e.g., scanned documents) where the user knows the approximate capture date.
Without this feature, such files always return `ERROR_TRANSFORMATION` even with fallback enabled.

**Affected files:**

- `app/api/src/main/java/ua/renamer/app/api/model/config/DateTimeConfig.java`
- `app/core/src/main/java/ua/renamer/app/core/v2/service/transformation/DateTimeTransformer.java`

**Implementation:**

`DateTimeConfig.java`:

- Add field: `boolean useCustomDateTimeAsFallback` (default: `false`)
- This field is meaningful only when `useFallbackDateTime = true`
- Note: `customDateTime` field should already exist in V2's `DateTimeConfig` (from V2 implementation). Verify this; if
  absent, also add `LocalDateTime customDateTime` (nullable/Optional).

`DateTimeTransformer.java`:

- Extend the fallback logic added in TASK-1.5:
  ```java
  if (dateTime == null && config.isUseFallbackDateTime()) {
      if (config.isUseCustomDateTimeAsFallback() && config.getCustomDateTime() != null) {
          dateTime = config.getCustomDateTime(); // Custom date as guaranteed fallback
      } else {
          // Natural dates minimum (TASK-1.5 logic)
          dateTime = Stream.of(creation, modification, contentCreation)
                           .filter(Objects::nonNull)
                           .min(Comparator.naturalOrder())
                           .orElse(null);
      }
  }
  ```

**Acceptance criteria (JUnit 5):**
Location: `app/core/src/test/java/ua/renamer/app/core/v2/service/transformation/DateTimeTransformerFallbackTest.java`
(extend the test class created in TASK-1.5)

- `givenAllNaturalDatesNullAndCustomFallbackEnabled_whenExtract_thenCustomDateUsed()`
- `givenCustomFallbackEnabledButCustomDateNull_whenExtract_thenFallsBackToMinNaturalDates()`
- `givenCustomFallbackEnabledAndPrimarySourcePresent_thenPrimaryUsedNotCustom()`
- `givenCustomFallbackDisabledAndFallbackEnabled_thenMinNaturalDateUsed()`

---

### TASK-1.8 — STABILITY-1: Null-safe `transform()` in all 10 transformers

**Priority:** Round 4 (after TASK-1.3 so configs have validation, making null-config tests meaningful)
**Status:** COMPLETED

**Goal:** Each transformer's `transform(FileModel, XxxConfig)` method must guard against a null `config` parameter and
return a structured error result instead of throwing NPE.

**Context:**
During refactoring, incorrect DI wiring, or test setup errors, a transformer may receive a null config. Without a guard,
NPE propagates from inside the method body as an unhandled exception, breaking the V2 "never throws" contract. A null
config should produce `ERROR_TRANSFORMATION` with a clear message, the same as any other transformation failure.

**Affected files (one change per transformer):**

```
app/core/src/main/java/ua/renamer/app/core/v2/service/transformation/
  AddTextTransformer.java
  RemoveTextTransformer.java
  ReplaceTextTransformer.java
  CaseChangeTransformer.java
  SequenceTransformer.java
  TruncateTransformer.java
  ExtensionChangeTransformer.java
  DateTimeTransformer.java
  ImageDimensionsTransformer.java
  ParentFolderTransformer.java
```

**Implementation (same pattern for all 10):**
At the top of each `transform(FileModel input, XxxConfig config)` method, before any existing logic:

```java
if(input ==null){
        throw new

IllegalArgumentException("input FileModel must not be null");
}
        if(config ==null){
        return

buildErrorResult(input, "Transformer configuration must not be null");
}
```

Where `buildErrorResult(FileModel, String)` returns a `PreparedFileModel` with `hasError = true` and the given message.
Find the existing error-result builder in each transformer and use the same pattern.

For string fields in configs that transformers use without null checks (e.g., `config.getTextToAdd()`), add
null-to-empty coercion where appropriate:

```java
String text = config.getTextToAdd() != null ? config.getTextToAdd() : "";
```

**Acceptance criteria (JUnit 5):**
One test per transformer (`XxxTransformerTest.java` — extend existing test classes):

- `givenNullConfig_whenTransform_thenErrorResultReturned()` — not NPE, not exception
- `givenNullInput_whenTransform_thenIllegalArgumentExceptionThrown()`

---

### TASK-1.9 — STABILITY-3: NameValidator before physical rename ✅ DONE

**Priority:** Round 4 (parallel with TASK-1.8)

**Goal:** Before calling `Files.move()`, validate the final filename using `NameValidator.isValid()`. Return
`ERROR_TRANSFORMATION` (not `ERROR_EXECUTION`) for invalid names.

**Context:**
After transformation and duplicate resolution, the final filename may contain OS-restricted characters (e.g., `:` on
Windows from datetime format `"2024:01:01"`, `*`, `?`, `<`, `>`, `|`). If `Files.move()` is called with an invalid name,
it throws `InvalidPathException` with an OS-level message that is confusing to users. Validating upfront produces a
clear, structured error.

`NameValidator` already exists at:
`app/core/src/main/java/ua/renamer/app/core/service/validator/impl/NameValidator.java`

**Affected files:**

- `app/core/src/main/java/ua/renamer/app/core/v2/service/impl/RenameExecutionServiceImpl.java`

**Implementation:**

1. Read `RenameExecutionServiceImpl.java` to understand current structure.
2. Inject `NameValidator` via constructor injection (Guice will provide it — it should already be bound in
   `DICoreModule`).
3. In the rename method, before `Files.move()`:
   ```java
   String finalName = preparedFile.getNewFullName(); // or however the full name is obtained
   if (!nameValidator.isValid(finalName)) {
       return RenameResult.builder()
           .withPreparedFile(preparedFile)
           .withStatus(RenameStatus.ERROR_TRANSFORMATION)
           .withErrorMessage("Generated filename contains invalid characters: " + finalName)
           .build();
   }
   ```
4. Use `ERROR_TRANSFORMATION` (not `ERROR_EXECUTION`) — the issue is with the transformation output, not the physical
   disk operation.

**Acceptance criteria (JUnit 5):**
Location: `app/core/src/test/java/ua/renamer/app/core/v2/service/impl/RenameExecutionServiceImplTest.java`

- `givenFilenameWithSlash_whenExecute_thenErrorTransformationReturnedOnAllPlatforms()`
- `givenFilenameWithColonOnWindows_whenExecute_thenErrorTransformationReturned()` — annotate with
  `@EnabledOnOs(OS.WINDOWS)` since `:` is only invalid on Windows; verifies the NameValidator catches OS-specific
  restrictions before `Files.move()`
- `givenValidFilename_whenExecute_thenNameValidatorPassesAndRenameProceeds()`

---

### TASK-1.10 — BUG-4: Disk conflict suffix retry in RenameExecutionServiceImpl ✅ DONE

**Priority:** Round 5 (last — after TASK-1.9 so executor is already being modified)

**Goal:** When the rename target file already exists on disk (not in the current batch), retry with suffix ` (01)`,
` (02)`, ... ` (999)` before returning `ERROR_EXECUTION`.

**Context:**
Currently, when `Files.exists(newPath)` returns true for a pre-existing file (not in the current batch),
`RenameExecutionServiceImpl` immediately returns `ERROR_EXECUTION` with "Target file already exists". This is
inconsistent with `DuplicateNameResolverImpl` (Phase 3), which auto-appends suffixes for in-batch conflicts. Users see a
confusing error when the solution is straightforward: try `name (01).ext`, then `name (02).ext`, etc.

**Affected files:**

- `app/core/src/main/java/ua/renamer/app/core/v2/service/impl/RenameExecutionServiceImpl.java`

**Implementation:**
Add a private helper method:

```java
private static final int MAX_SUFFIX_ATTEMPTS = 999;

private Path resolveConflictWithDisk(Path targetPath) {
    if (!Files.exists(targetPath)) return targetPath; // No conflict

    String fullName = targetPath.getFileName().toString();
    String baseName = getNameWithoutExtension(fullName);   // helper for name without ext
    String ext = getExtensionWithDot(fullName);       // helper for ".ext" or ""
    Path parent = targetPath.getParent();
    int digits = String.valueOf(MAX_SUFFIX_ATTEMPTS).length(); // = 3

    for (int i = 1; i <= MAX_SUFFIX_ATTEMPTS; i++) {
        String suffix = String.format(" (%0" + digits + "d)", i);
        Path candidate = parent.resolve(baseName + suffix + ext);
        if (!Files.exists(candidate)) return candidate;
    }
    return null; // All 999 attempts exhausted — essentially impossible in practice
}
```

In the main rename flow:

- Before calling `Files.move()`, call `resolveConflictWithDisk(newPath)`.
- If it returns a non-null path different from `newPath`, use that path for the rename.
- If it returns null, return `ERROR_EXECUTION` with a message explaining all suffix attempts were exhausted.
- **Edge case:** Case-change rename on a case-insensitive filesystem (e.g., `photo.jpg` → `PHOTO.JPG`). The source and
  target refer to the same file. Detect this: `sourceFile.toPath().equals(targetPath)` or use `Files.isSameFile()`. If
  same file, proceed without suffix.

**Note:** `RenameResult` must carry the *actual* final name used when it differs from the planned name. Verify whether
`RenameResult` has a field for the actual path used; if not, the coder should note this as a minor finding but it is not
a blocker for this task.

**Acceptance criteria (JUnit 5 — integration tests with `@TempDir`):**
Location: `app/core/src/test/java/ua/renamer/app/core/v2/service/impl/RenameExecutionServiceImplDiskConflictTest.java`

- `givenTargetFileExistsOnDisk_whenExecute_thenSuffixAppliedAndFileRenamed()` — uses `@TempDir`, creates real files
- `givenTargetAndSuffix01ExistOnDisk_whenExecute_thenSuffix02Applied()`
- `givenCaseChangeRename_whenSourceAndTargetSameFile_thenRenameSucceedsWithoutSuffix()`
- `givenNormalRename_whenNoConflict_thenNoSuffixAdded()`

**Build check after all Part 1 tasks:**

```bash
cd app && mvn test -q -ff -Dai=true    # zero failures
cd app && mvn verify -Pcode-quality -q  # zero Checkstyle/PMD/SpotBugs violations
```

---

## Part 2 — Structural Foundation (API Interfaces & Module Structure)

**Goal:** Create the `SessionApi`, `ModeApi<P>`, and `ModeParameters` sealed hierarchy as pure-Java interfaces and
records. Extend the existing `app/api` Maven module with new packages. Create a new `app/backend` Maven module with no
JavaFX dependency.

**Key design decision:** Rather than creating a brand new Maven module `app-api`, the session API types will be added to
the **existing `app/api` module** in new sub-packages (`ua.renamer.app.api.session.*`). This avoids Maven module
proliferation while maintaining clean separation. The implementation (`RenameSessionService`, `BackendExecutor`) goes
into a **new `app/backend` module** which has no `require javafx.*` dependency — JPMS enforces the FX-free boundary at
compile time.

---

### TASK-2.1 — Create `app/backend` Maven module skeleton ✅ DONE

**Priority:** First task in Part 2 (others depend on this)

**Goal:** Create the Maven module structure for `app/backend`. At this stage, it is empty except for the module
definition and POM.

**Context:**
`app/backend` will house the implementation of `SessionApi` — specifically `RenameSessionService`, `BackendExecutor`,
`RenameSession`, and the Guice module `DIBackendModule`. It must:

- depend on `app/api` and `app/core`
- NOT depend on `app/ui`
- NOT have `require javafx.*` in `module-info.java` (JPMS enforcement of FX-free backend)

**Files to create:**

1. `app/backend/pom.xml` — Maven POM
2. `app/backend/src/main/java/module-info.java` — JPMS module definition
3. `app/backend/src/main/java/ua/renamer/app/backend/` — empty package placeholder (`.gitkeep` or first class)
4. `app/backend/src/test/java/` — test directory
5. Update `app/pom.xml` — add `backend` to `<modules>` list

**POM content:**

```xml

<parent>
    <groupId>ua.renamer</groupId>
    <artifactId>app</artifactId>
    <version>${project.parent.version}</version>
</parent>
<artifactId>backend</artifactId>
<packaging>jar</packaging>

<dependencies>
<dependency>
    <groupId>ua.renamer</groupId>
    <artifactId>api</artifactId>
</dependency>
<dependency>
    <groupId>ua.renamer</groupId>
    <artifactId>core</artifactId>
</dependency>
<dependency>
    <groupId>com.google.inject</groupId>
    <artifactId>guice</artifactId>
</dependency>
<!-- test dependencies: JUnit 5, Mockito, AssertJ -->
</dependencies>
```

**module-info.java content:**

```java
module ua.renamer.app.backend {
    requires ua.renamer.app.api;
    requires ua.renamer.app.core;
    requires com.google.guice;
    requires jakarta.inject;
    requires static lombok;
    requires org.slf4j;
    // IMPORTANT: NO require javafx.* — JPMS enforces FX-free backend

    exports ua.renamer.app.backend.config;     // DIBackendModule
    exports ua.renamer.app.backend.service;    // RenameSessionService, BackendExecutor
    exports ua.renamer.app.backend.session;    // RenameSession, SessionApiImpl
    // Internal packages are NOT exported
}
```

**Acceptance criteria:**

- `mvn compile -q -ff` passes (empty module compiles)
- `mvn dependency:tree -pl app/backend` shows no JavaFX dependency

---

### TASK-2.2 — Add core API value types to `app/api` ✅ DONE

**Goal:** Define the shared value types that both `app/backend` and `app/ui` will use: `ValidationResult`,
`CommandResult`, `SessionStatus`, `RenameCandidate`, `RenamePreview`, `RenameSessionResult`.

**Context:**
These are pure-Java records/enums with no JavaFX dependency. They live in `app/api` so both the backend implementation
and the UI can use them without circular dependencies.

- `ValidationResult` — result of `ModeParameters.validate()` or `ModeApi.updateParameters()`. Either `ok()` or
  `fieldError(field, message)`.
- `CommandResult` — result of file management operations (`addFiles`, `removeFiles`, `clearFiles`). Either success or
  error with message.
- `SessionStatus` — enum representing the session lifecycle state for UI enable/disable logic.
- `RenameCandidate` — FX-safe snapshot of `FileModel` (name, extension, path; no V2 internal types exposed).
- `RenamePreview` — FX-safe snapshot of `PreparedFileModel` (originalName, newName, hasError, errorMessage).
- `RenameSessionResult` — FX-safe snapshot of `RenameResult` (originalName, finalName, status, errorMessage).

**Files to create:**

```
app/api/src/main/java/ua/renamer/app/api/session/
  ValidationResult.java     — record with valid()/fieldError() factory methods
  CommandResult.java        — record with succeeded()/failure(String) factory methods
  SessionStatus.java        — enum: EMPTY, FILES_LOADED, MODE_CONFIGURED, EXECUTING, COMPLETE, ERROR
  RenameCandidate.java      — record: String fileId, String name, String extension, Path path
  RenamePreview.java        — record: String fileId, String originalName, String newName, boolean hasError, String errorMessage
  RenameSessionResult.java  — record: String fileId, String originalName, String finalName, RenameStatus status, String errorMessage
```

**Implementation details:**

```java
// ValidationResult
public record ValidationResult(boolean ok, String field, String message) {
    public static ValidationResult ok() {
        return new ValidationResult(true, null, null);
    }

    public static ValidationResult fieldError(String field, String message) {
        return new ValidationResult(false, field, message);
    }

    public boolean isError() {
        return !ok;
    }
}

// CommandResult
public record CommandResult(boolean success, String errorMessage) {
    public static CommandResult success() {
        return new CommandResult(true, null);
    }

    public static CommandResult failure(String message) {
        return new CommandResult(false, message);
    }
}

// SessionStatus (enum)
public enum SessionStatus {
    EMPTY,          // No files loaded
    FILES_LOADED,   // Files loaded, no mode selected
    MODE_CONFIGURED,// Mode selected and parameters set, ready to preview/execute
    EXECUTING,      // Rename operation in progress
    COMPLETE,       // Last execution completed (success or partial)
    ERROR           // Session-level error (not file-level)
}
```

**Update `app/api/module-info.java`:**
Add: `exports ua.renamer.app.api.session;` and `opens ua.renamer.app.api.session;`

**API deviation:** Java records cannot have a static method with the same name and zero-arg signature as the
auto-generated component accessor. `ok()` accessor conflicts with `static ok()` factory; same for `success()`. Renamed:
`ok()` → `valid()`, `success()` → `succeeded()`.

**Acceptance criteria:**

- All 6 types compile (83 tests pass)
- `ValidationResult.valid().isError()` is `false`
- `ValidationResult.fieldError("text", "empty").field()` equals `"text"`
- `CommandResult.succeeded().success()` is `true`
- `SessionStatus.values().length` equals `6`

---

### TASK-2.3 — Implement `ModeParameters` sealed hierarchy (10 parameter records) ✅ DONE

**Goal:** Define the sealed `ModeParameters` interface and all 10 concrete record implementations with typed `withX()`
withers and `validate()` methods.

**Context:**
`ModeParameters` is the typed parameter contract between the UI controllers and the backend. Each record is a Java
record (immutable value type) with:

- Fields matching exactly the V2 config class it maps to (see mapping table below)
- A `validate()` method that returns `ValidationResult` — runs validation client-side before sending to backend
- Typed `withX()` withers for each field (creates a new record with one field changed)
- A `mode()` method returning the corresponding `TransformationMode` enum value

**Config → ModeParameters field mapping:**

| ModeParameters Record   | V2 Config Class         | Fields                                                                                                                                                                                                                                                                                                            |
|-------------------------|-------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `AddTextParams`         | `AddTextConfig`         | `String textToAdd`, `ItemPosition position`                                                                                                                                                                                                                                                                       |
| `RemoveTextParams`      | `RemoveTextConfig`      | `String textToRemove`, `ItemPosition position`                                                                                                                                                                                                                                                                    |
| `ReplaceTextParams`     | `ReplaceTextConfig`     | `String textToReplace`, `String replacementText`, `ItemPositionWithReplacement position`                                                                                                                                                                                                                          |
| `ChangeCaseParams`      | `CaseChangeConfig`      | `TextCaseOptions caseOption`, `boolean capitalizeFirstLetter`                                                                                                                                                                                                                                                     |
| `SequenceParams`        | `SequenceConfig`        | `int startNumber`, `int stepValue`, `int paddingDigits`, `SortSource sortSource`                                                                                                                                                                                                                                  |
| `TruncateParams`        | `TruncateConfig`        | `int numberOfSymbols`, `TruncateOptions truncateOption`                                                                                                                                                                                                                                                           |
| `ExtensionChangeParams` | `ExtensionChangeConfig` | `String newExtension`                                                                                                                                                                                                                                                                                             |
| `DateTimeParams`        | `DateTimeConfig`        | `DateTimeSource source`, `DateFormat dateFormat`, `TimeFormat timeFormat`, `ItemPosition position`, `boolean useDatePart`, `boolean useTimePart`, `boolean applyToExtension`, `boolean useFallbackDateTime`, `boolean useCustomDateTimeAsFallback`, `LocalDateTime customDateTime`, `boolean useUppercaseForAmPm` |
| `ImageDimensionsParams` | `ImageDimensionsConfig` | `ImageDimensionOptions leftSide`, `ImageDimensionOptions rightSide`, `ItemPositionWithReplacement position`, `String nameSeparator`                                                                                                                                                                               |
| `ParentFolderParams`    | `ParentFolderConfig`    | `int numberOfParentFolders`, `ItemPosition position`, `String separator`                                                                                                                                                                                                                                          |

**Note on enums:** The existing enums (`ItemPosition`, `ItemPositionExtended`, `ItemPositionWithReplacement`,
`DateTimeSource`, `DateFormat`, `TimeFormat`, `SortSource`, `TextCaseOptions`, `TruncateOptions`,
`ImageDimensionOptions`, `TransformationMode`) already live in `app/api/src/main/java/ua/renamer/app/api/enums/` or
`app/core/src/main/java/ua/renamer/app/core/enums/`. Verify locations; import from there.

**Files to create:**

```
app/api/src/main/java/ua/renamer/app/api/session/
  ModeParameters.java           — sealed interface
  AddTextParams.java            — record implements ModeParameters
  RemoveTextParams.java
  ReplaceTextParams.java
  ChangeCaseParams.java
  SequenceParams.java
  TruncateParams.java
  ExtensionChangeParams.java
  DateTimeParams.java           — most complex: 11 fields
  ImageDimensionsParams.java
  ParentFolderParams.java
```

**Sealed interface:**

```java
package ua.renamer.app.api.session;

public sealed interface ModeParameters permits
        AddTextParams, RemoveTextParams, ReplaceTextParams,
        ChangeCaseParams, SequenceParams, TruncateParams,
        ExtensionChangeParams, DateTimeParams, ImageDimensionsParams,
        ParentFolderParams {

    TransformationMode mode();

    ValidationResult validate();

    // Sequential execution marker (only SequenceParams overrides to true)
    default boolean requiresSequentialExecution() {
        return false;
    }
}
```

**Example — AddTextParams:**

```java
public record AddTextParams(String textToAdd, ItemPosition position)
        implements ModeParameters {

    @Override
    public TransformationMode mode() {
        return TransformationMode.ADD_TEXT;
    }

    @Override
    public ValidationResult validate() {
        if (position == null)
            return ValidationResult.fieldError("position", "Position is required");
        // Empty textToAdd is allowed (no-op transformation)
        return ValidationResult.ok();
    }

    public AddTextParams withTextToAdd(String textToAdd) {
        return new AddTextParams(textToAdd, position);
    }

    public AddTextParams withPosition(ItemPosition position) {
        return new AddTextParams(textToAdd, position);
    }
}
```

**Special case — SequenceParams:**

```java
public record SequenceParams(int startNumber, int stepValue, int paddingDigits, SortSource sortSource)
        implements ModeParameters {

    @Override
    public boolean requiresSequentialExecution() {
        return true;
    } // ADD_SEQUENCE is always sequential

    @Override
    public ValidationResult validate() {
        if (sortSource == null) return ValidationResult.fieldError("sortSource", "Sort source is required");
        if (stepValue <= 0) return ValidationResult.fieldError("stepValue", "Step must be positive");
        if (paddingDigits < 0) return ValidationResult.fieldError("paddingDigits", "Padding cannot be negative");
        return ValidationResult.ok();
    }
    // ... withX() methods
}
```

**Validate() rules from MODE_STATE_MACHINES.md:**

| Mode            | Validate() rules                                                                                                                                         |
|-----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| AddText         | `position != null` (textToAdd may be empty)                                                                                                              |
| RemoveText      | `position != null`, `textToRemove != null`                                                                                                               |
| ReplaceText     | `position != null`, `textToReplace != null`, `replacementText != null`                                                                                   |
| ChangeCase      | `caseOption != null`                                                                                                                                     |
| Sequence        | `sortSource != null`, `stepValue > 0`, `paddingDigits >= 0`                                                                                              |
| Truncate        | `truncateOption != null`, `numberOfSymbols >= 0`                                                                                                         |
| ExtensionChange | `newExtension != null`, `!newExtension.isBlank()`                                                                                                        |
| DateTime        | `source != null`, `dateFormat != null` OR `timeFormat != null`, `useDatePart \|\| useTimePart`; if `source == CUSTOM_DATE` then `customDateTime != null` |
| ImageDimensions | `position != null`; at least one of `leftSide`/`rightSide` != `DO_NOT_USE`                                                                               |
| ParentFolder    | `position != null`, `numberOfParentFolders >= 1`                                                                                                         |

**Acceptance criteria (JUnit 5):**

```
app/api/src/test/java/ua/renamer/app/api/session/ModeParametersValidationTest.java
```

One test class covering all 10 params records:

- `givenAddTextWithNullPosition_whenValidate_thenFieldError()`
- `givenSequenceWithNegativeStep_whenValidate_thenFieldError()`
- `givenDateTimeWithNeitherDateNorTimePart_whenValidate_thenFieldError()`
- `givenValidParams_whenValidate_thenOk()` — one happy-path test per mode

---

### TASK-2.4 — Define `TaskHandle<T>`, `ModeApi<P>`, and `SessionApi` interfaces ✅ DONE

**Goal:** Define the three interface contracts that form the public API surface of the backend.

**Context:**
These are pure-Java interfaces (no JavaFX types). They live in `app/api`. The implementations live in `app/backend`.

- `TaskHandle<T>` — represents a long-running operation; provides `result()` (CompletableFuture), progress listeners,
  and cancellation.
- `ModeApi<P>` — typed per-mode operations: read current parameters, update parameters via mutator function, reset to
  defaults.
- `SessionApi` — single facade entry point: add/remove/clear files, select mode, execute rename, query state.

**Files to create:**

```
app/api/src/main/java/ua/renamer/app/api/session/
  TaskHandle.java
  ModeApi.java
  SessionApi.java
  SessionSnapshot.java    — immutable session state snapshot for agents
  AvailableAction.java    — enum: ADD_FILES, REMOVE_FILES, CLEAR, SELECT_MODE, EXECUTE, CANCEL
  StatePublisher.java     — callback interface: backend → UI state bridge (no JavaFX imports)
```

**TaskHandle\<T\> interface:**

```java
public interface TaskHandle<T> {
    String taskId();

    CompletableFuture<T> result();          // Completes normally with result or exceptionally

    void requestCancellation();

    boolean isCancellationRequested();

    void addProgressListener(ProgressListener listener);

    void removeProgressListener(ProgressListener listener);

    @FunctionalInterface
    interface ProgressListener {
        void onProgress(double workDone, double totalWork, String message);
    }
}
```

**ModeApi\<P extends ModeParameters\> interface:**

```java
public interface ModeApi<P extends ModeParameters> {
    TransformationMode mode();

    P currentParameters();

    void addParameterListener(ParameterListener<P> listener);

    void removeParameterListener(ParameterListener<P> listener);

    CompletableFuture<ValidationResult> updateParameters(ParamMutator<P> mutator);

    CompletableFuture<Void> resetParameters();

    @FunctionalInterface
    interface ParameterListener<P extends ModeParameters> {
        void onParametersChanged(P updated);
    }

    @FunctionalInterface
    interface ParamMutator<P extends ModeParameters> {
        P apply(P current);
    }
}
```

**SessionApi interface:**

```java
public interface SessionApi {
    // File management
    CompletableFuture<CommandResult> addFiles(List<Path> paths);

    CompletableFuture<CommandResult> removeFiles(List<String> fileIds);

    CompletableFuture<CommandResult> clearFiles();

    // Mode selection — returns typed API for selected mode
    <P extends ModeParameters> CompletableFuture<ModeApi<P>> selectMode(TransformationMode mode);

    // Execution
    TaskHandle<List<RenameSessionResult>> execute();

    boolean canExecute();

    // Agent/test support
    SessionSnapshot snapshot();

    List<AvailableAction> availableActions();
}
```

**SessionSnapshot:**

```java
public record SessionSnapshot(
        List<RenameCandidate> files,
        TransformationMode activeMode,
        ModeParameters currentParameters,
        List<RenamePreview> preview,
        SessionStatus status
) {
}
```

**StatePublisher interface:**

```java
package ua.renamer.app.api.session;

/**
 * Callback interface for backend → UI state notifications.
 *
 * <p>Lives in app-api (no JavaFX). Implemented by FxStateMirror in app-ui,
 * which wraps each method body in Platform.runLater(). The backend
 * (RenameSessionService) calls these methods from the state thread;
 * FxStateMirror dispatches to the FX thread transparently.
 *
 * <p>By using this interface, app-backend has zero JavaFX dependency —
 * JPMS enforces this at compile time.
 */
public interface StatePublisher {
    void publishFilesChanged(List<RenameCandidate> files, List<RenamePreview> preview);

    void publishPreviewChanged(List<RenamePreview> preview);

    void publishModeChanged(TransformationMode mode, ModeParameters params);

    void publishRenameComplete(List<RenameSessionResult> results, SessionStatus status);

    void publishStatusChanged(SessionStatus status);
}
```

**Acceptance criteria:**

- All interfaces and records compile
- `SessionApi` has no `javafx.*` imports
- `ModeApi` has no `javafx.*` imports
- `StatePublisher` has no `javafx.*` imports
- JPMS: `ua.renamer.app.api.session` is exported in `module-info.java`

---

## Part 3 — Backend API Implementation

**Goal:** Implement the backend: `RenameSession` (state), `BackendExecutor` (threading), `RenameSessionService` (
business logic), `SessionApiImpl`, `DIBackendModule` (Guice wiring), and the `ModeParameters → XxxConfig` converters.

---

### TASK-3.1 — Implement `RenameSession` (session state holder)

**Status:** COMPLETED

**Goal:** A plain-Java mutable session state object — accessed exclusively from the backend state thread.

**Context:**
`RenameSession` holds all mutable session state: loaded files, active mode, current parameters, last preview results,
session status. It is **not thread-safe** on its own — thread safety is enforced by `BackendExecutor` which gates all
mutations to its single state thread.

**File to create:**

```
app/backend/src/main/java/ua/renamer/app/backend/session/RenameSession.java
```

**Implementation:**

```java

@Getter
public class RenameSession {
    private final List<FileModel> files = new ArrayList<>();
    private final TransformationMode activeMode = null;
    private final ModeParameters currentParams = null;
    private final List<PreparedFileModel> lastPreview = List.of();
    private SessionStatus status = SessionStatus.EMPTY;

    // Mutation methods (called only from BackendExecutor state thread)
    public void addFiles(List<FileModel> newFiles) { ...}

    public void removeFiles(List<String> fileIds) { ...}

    public void clearFiles() { ...}

    public void setActiveMode(TransformationMode mode, ModeParameters defaultParams) { ...}

    public void setParameters(ModeParameters params) { ...}

    public void setLastPreview(List<PreparedFileModel> preview) { ...}

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public SessionSnapshot toSnapshot(List<RenamePreview> previewDtos) {
        return new SessionSnapshot(
                files.stream().map(RenameSessionConverter::toCandidate).toList(),
                activeMode, currentParams,
                previewDtos, status
        );
    }
}
```

**Note:** `FileModel` is V2's internal type from `app/core`. It never leaves the backend module — it is converted to
`RenameCandidate` (for the UI) inside `RenameSession.toSnapshot()`.

**Acceptance criteria:**

- `RenameSession` has no `javafx.*` imports
- `toSnapshot()` returns a `SessionSnapshot` record (not a V2 internal type)

---

### TASK-3.2 — Implement `BackendExecutor` (state thread + virtual pool) ✅ DONE

**Goal:** Provide a single state thread for all state mutations and a virtual thread pool for I/O-bound work.

**Context:**
`BackendExecutor` owns two ExecutorService instances:

1. **State thread** (`Executors.newSingleThreadExecutor()`) — all mutations to `RenameSession` run here. Ensures no
   concurrent modification.
2. **Virtual pool** (`Executors.newVirtualThreadPerTaskExecutor()`) — used by `RenameSessionService` when submitting V2
   pipeline work via `FileRenameOrchestrator`.

`BackendExecutor` does NOT call `FileRenameOrchestrator` directly — it provides the threading primitives;
`RenameSessionService` composes them.

**File to create:**

```
app/backend/src/main/java/ua/renamer/app/backend/service/BackendExecutor.java
```

**Implementation:**

```java

@Singleton
public class BackendExecutor implements Closeable {
    private final ExecutorService stateThread = Executors.newSingleThreadExecutor(
            r -> {
                Thread t = new Thread(r, "backend-state");
                t.setDaemon(true);
                return t;
            });
    private final ExecutorService virtualPool = Executors.newVirtualThreadPerTaskExecutor();

    // Submit a state mutation — runs on state thread, returns CompletableFuture
    public <T> CompletableFuture<T> submitStateChange(Callable<T> mutation) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mutation.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, stateThread);
    }

    // Submit heavy work (V2 pipeline) — runs on virtual thread pool
    public <T> CompletableFuture<T> submitWork(Callable<T> work) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return work.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, virtualPool);
    }

    @Override
    public void close() {
        stateThread.shutdown();
        virtualPool.shutdown();
    }
}
```

**Acceptance criteria:**

- `BackendExecutor` has no `javafx.*` imports
- `submitStateChange()` executes serially when called concurrently (state thread guarantee)
- `close()` shuts down both executors

---

### TASK-3.3 — Implement `ModeParametersConverter` (ModeParameters → V2 Config)

**Status:** COMPLETED

**Goal:** A centralized converter that maps each `ModeParameters` record to the corresponding V2 `XxxConfig` object
consumed by `FileRenameOrchestratorImpl`.

**Context:**
`RenameSessionService` needs to call `FileRenameOrchestrator.execute(files, mode, config, callback)`. The `config`
parameter is a typed V2 `XxxConfig`. The `ModeParameters` record holds the same data in a UI-friendly form. This
converter bridges the two representations and is the single point where parameter mapping is defined.

This is the only class in the codebase that knows about both `ModeParameters` (API layer) and V2 config classes (core
layer).

**File to create:**

```
app/backend/src/main/java/ua/renamer/app/backend/session/ModeParametersConverter.java
```

**Implementation (sealed switch — compiler-checked):**

```java
public final class ModeParametersConverter {
    private ModeParametersConverter() {
    }

    public static TransformationConfig toConfig(ModeParameters params) {
        return switch (params) {
            case AddTextParams p -> AddTextConfig.builder()
                    .withTextToAdd(p.textToAdd())
                    .withPosition(p.position())
                    .build();
            case RemoveTextParams p -> RemoveTextConfig.builder()
                    .withTextToRemove(p.textToRemove())
                    .withPosition(p.position())
                    .build();
            case ReplaceTextParams p -> ReplaceTextConfig.builder()
                    .withTextToReplace(p.textToReplace())
                    .withReplacementText(p.replacementText())
                    .withPosition(p.position())
                    .build();
            case ChangeCaseParams p -> CaseChangeConfig.builder()
                    .withCaseOption(p.caseOption())
                    .withCapitalizeFirstLetter(p.capitalizeFirstLetter())
                    .build();
            case SequenceParams p -> SequenceConfig.builder()
                    .withStartNumber(p.startNumber())
                    .withStepValue(p.stepValue())
                    .withPadding(p.paddingDigits())
                    .withSortSource(p.sortSource())
                    .build();
            case TruncateParams p -> TruncateConfig.builder()
                    .withNumberOfSymbols(p.numberOfSymbols())
                    .withTruncateOption(p.truncateOption())
                    .build();
            case ExtensionChangeParams p -> ExtensionChangeConfig.builder()
                    .withNewExtension(p.newExtension())
                    .build();
            case DateTimeParams p -> DateTimeConfig.builder()
                    .withSource(p.source())
                    .withDateFormat(p.dateFormat())
                    .withTimeFormat(p.timeFormat())
                    .withPosition(p.position())
                    .withUseDatePart(p.useDatePart())
                    .withUseTimePart(p.useTimePart())
                    .withApplyToExtension(p.applyToExtension())
                    .withUseFallbackDateTime(p.useFallbackDateTime())
                    .withUseCustomDateTimeAsFallback(p.useCustomDateTimeAsFallback())
                    .withCustomDateTime(p.customDateTime())
                    .withUseUppercaseForAmPm(p.useUppercaseForAmPm())
                    .build();
            case ImageDimensionsParams p -> ImageDimensionsConfig.builder()
                    .withLeftSide(p.leftSide())
                    .withRightSide(p.rightSide())
                    .withPosition(p.position())
                    .withNameSeparator(p.nameSeparator())
                    .build();
            case ParentFolderParams p -> ParentFolderConfig.builder()
                    .withNumberOfParentFolders(p.numberOfParentFolders())
                    .withPosition(p.position())
                    .withSeparator(p.separator())
                    .build();
        };
    }
}
```

**Note:** The `switch` is exhaustive over the sealed hierarchy — adding a new `ModeParameters` permit without updating
this converter will be a **compile error**. This is the intended safety guarantee.

**Acceptance criteria:**

- `switch` covers all 10 sealed permits — compiler-verified exhaustiveness
- `givenAddTextParams_whenConvert_thenAddTextConfigFieldsMatch()` (JUnit 5)
- `givenDateTimeParams_whenConvert_thenAllFieldsTransferred()` — especially the 3 new fields from TASK-1.5/1.6/1.7
- `givenSequenceParamsWithNegativePadding_whenConvert_thenIllegalArgumentExceptionFromConfig()` — validates integration
  with TASK-1.2 config validation

**Post-verification fix:** Added `applyToExtension` field to `DateTimeConfig`, wired it through
`DateTimeTransformer`, and mapped it in `ModeParametersConverter`. Gap discovered during Part 3
compliance review. All 3 affected modules (`api`, `core`, `backend`) pass tests after the fix.

---

### TASK-3.4 — Implement `RenameSessionService` (core business logic) ✅ DONE

**Goal:** The central class that orchestrates all backend operations: file management, preview computation, and rename
execution.

**Context:**
`RenameSessionService` is the implementation of `SessionApi`. It:

1. Holds a `RenameSession` (state) accessed via `BackendExecutor.submitStateChange()`
2. Calls `FileRenameOrchestratorImpl` for preview (phases 1–3) and execution (all 4 phases)
3. Publishes results to `FxStateMirror` via a `Consumer<FxPublishEvent>` callback (decoupled from JavaFX)
4. Returns `TaskHandle` instances for long-running operations

**File to create:**

```
app/backend/src/main/java/ua/renamer/app/backend/session/RenameSessionService.java
```

**Key design point — FxStateMirror decoupling:**
`RenameSessionService` lives in `app/backend` which has no `require javafx.*`. `FxStateMirror` lives in `app/ui`. The
connection is via a `StatePublisher` interface (defined in `app/api`):

```java
// In app/api/session/
public interface StatePublisher {
    void publishFilesChanged(List<RenameCandidate> files, List<RenamePreview> preview);

    void publishPreviewChanged(List<RenamePreview> preview);

    void publishModeChanged(TransformationMode mode, ModeParameters params);

    void publishRenameComplete(List<RenameSessionResult> results, SessionStatus status);

    void publishStatusChanged(SessionStatus status);
}
```

`FxStateMirror` implements `StatePublisher` (wrapping each call in `Platform.runLater`). `RenameSessionService` receives
`StatePublisher` via constructor injection.

**Implementation sketch:**

```java

@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Singleton
public class RenameSessionService implements SessionApi {
    private final FileRenameOrchestrator orchestrator;
    private final BackendExecutor executor;
    private final StatePublisher publisher;    // FxStateMirror injected here
    private final RenameSession session = new RenameSession();

    @Override
    public CompletableFuture<CommandResult> addFiles(List<Path> paths) {
        return executor.submitStateChange(() -> {
            // 1. Call FileRenameOrchestrator phase 1 to extract metadata → List<FileModel>
            // 2. session.addFiles(fileModels)
            // 3. Compute preview with current params if mode is active
            // 4. publisher.publishFilesChanged(candidates, preview)
            // 5. return CommandResult.success()
        });
    }

    @Override
    public <P extends ModeParameters> CompletableFuture<ModeApi<P>> selectMode(TransformationMode mode) {
        return executor.submitStateChange(() -> {
            // 1. Create default params for mode
            // 2. session.setActiveMode(mode, defaultParams)
            // 3. Compute preview
            // 4. publisher.publishModeChanged(mode, defaultParams)
            // 5. Return new ModeApiImpl<P>(mode, this)
        });
    }

    @Override
    public TaskHandle<List<RenameSessionResult>> execute() {
        // 1. Create TaskHandleImpl
        // 2. executor.submitWork(() -> orchestrator.execute(session.getFiles(), ...))
        // 3. On completion: publisher.publishRenameComplete(results, COMPLETE)
        // 4. Return handle immediately
    }

    // Internal: called by ModeApiImpl when updateParameters() is invoked
    CompletableFuture<ValidationResult> updateParameters(ModeParameters params) {
        return executor.submitStateChange(() -> {
            ValidationResult validation = params.validate();
            if (validation.isError()) return validation;
            session.setParameters(params);
            recomputePreview(); // synchronous within state thread
            return ValidationResult.ok();
        });
    }

    private void recomputePreview() {
        // Call orchestrator phases 1-3 only (no rename)
        // Update session.setLastPreview()
        // publisher.publishPreviewChanged(...)
    }
}
```

**Default parameters per mode (for `selectMode()`):**

| Mode            | Default Parameters                                                                                                                                                    |
|-----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AddText         | `new AddTextParams("", ItemPosition.BEGIN)`                                                                                                                           |
| RemoveText      | `new RemoveTextParams("", ItemPosition.BEGIN)`                                                                                                                        |
| ReplaceText     | `new ReplaceTextParams("", "", ItemPositionWithReplacement.BEGIN)`                                                                                                    |
| ChangeCase      | `new ChangeCaseParams(TextCaseOptions.UPPERCASE, false)`                                                                                                              |
| Sequence        | `new SequenceParams(1, 1, 2, SortSource.FILE_NAME)`                                                                                                                   |
| Truncate        | `new TruncateParams(0, TruncateOptions.REMOVE_FROM_END)`                                                                                                              |
| ExtensionChange | `new ExtensionChangeParams("")`                                                                                                                                       |
| DateTime        | `new DateTimeParams(DateTimeSource.FILE_CREATION_DATETIME, DateFormat.YYYY_MM_DD, TimeFormat.NONE, ItemPosition.BEGIN, true, false, false, false, false, null, true)` |
| ImageDimensions | `new ImageDimensionsParams(ImageDimensionOptions.WIDTH, ImageDimensionOptions.HEIGHT, ItemPositionWithReplacement.BEGIN, " ")`                                        |
| ParentFolder    | `new ParentFolderParams(1, ItemPosition.BEGIN, " ")`                                                                                                                  |

**Acceptance criteria:**

- `givenFilesAdded_whenAddFiles_thenPublisherReceivesFilesChangedEvent()` (mock `StatePublisher`)
- `givenModeSelected_whenSelectMode_thenPreviewComputedAndPublished()`
- `givenValidParams_whenUpdateParameters_thenValidationOkAndPreviewRecomputed()`
- `givenInvalidParams_whenUpdateParameters_thenValidationErrorReturnedAndNoPreviewUpdate()`
- `RenameSessionService` has no `javafx.*` imports

---

### TASK-3.5 — Implement `ModeApiImpl<P>` and `TaskHandleImpl<T>`

**Status:** ✅ DONE

**Goal:** Implement the concrete `ModeApi<P>` and `TaskHandle<T>` interfaces used by UI controllers and returned from
`SessionApi`.

**Files to create:**

```
app/backend/src/main/java/ua/renamer/app/backend/session/ModeApiImpl.java
app/backend/src/main/java/ua/renamer/app/backend/session/TaskHandleImpl.java
```

**ModeApiImpl\<P\>:**

```java
public class ModeApiImpl<P extends ModeParameters> implements ModeApi<P> {
    private final TransformationMode mode;
    private final RenameSessionService service;
    private final AtomicReference<P> currentParams;
    private final CopyOnWriteArrayList<ParameterListener<P>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public CompletableFuture<ValidationResult> updateParameters(ParamMutator<P> mutator) {
        P updated = mutator.apply(currentParams.get());
        return service.updateParameters(updated).thenApply(result -> {
            if (result.ok()) {
                currentParams.set(updated);
                listeners.forEach(l -> l.onParametersChanged(updated));
            }
            return result;
        });
    }
    // ... other methods
}
```

**TaskHandleImpl\<T\>:**

```java
public class TaskHandleImpl<T> implements TaskHandle<T> {
    private final String taskId = UUID.randomUUID().toString();
    private final CompletableFuture<T> future;
    private volatile boolean cancellationRequested = false;
    private final CopyOnWriteArrayList<ProgressListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void requestCancellation() {
        cancellationRequested = true;
        future.cancel(false); // Best-effort
    }
    // ... other methods
}
```

**Acceptance criteria:**

- `givenUpdateParametersCalledConcurrently_whenMultipleUpdates_thenNoRaceCondition()`
- `givenTaskHandleCancel_whenRequested_thenCancellationFlagSet()`

---

### TASK-3.6 — Implement `DIBackendModule` (Guice wiring for backend)

**Status:** ✅ DONE

**Goal:** Guice module that wires all backend components together, including the `StatePublisher` binding to
`FxStateMirror`.

**Context:**
`DIBackendModule` lives in `app/backend`. However, `FxStateMirror` lives in `app/ui` (JavaFX is allowed there). The
binding of `StatePublisher` to `FxStateMirror` must happen in `app/ui`'s `DIUIModule` — not in `DIBackendModule`.
`DIBackendModule` only declares that `StatePublisher` is required; `DIUIModule` provides it.

**Files to create/modify:**

- `app/backend/src/main/java/ua/renamer/app/backend/config/DIBackendModule.java` — new file
- `app/ui/src/main/java/ua/renamer/app/ui/config/DIUIModule.java` — add `install(new DIBackendModule())` and provide
  `StatePublisher` binding

**DIBackendModule.java:**

```java
public class DIBackendModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(BackendExecutor.class).in(Scopes.SINGLETON);
        bind(SessionApi.class).to(RenameSessionService.class).in(Scopes.SINGLETON);
        // StatePublisher is NOT bound here — app/ui provides it
    }
}
```

**DIUIModule.java additions:**

```java
// Install backend module
install(new DIBackendModule());

// Provide FxStateMirror as StatePublisher
@Provides
@Singleton
StatePublisher provideStatePublisher(FxStateMirror mirror) {
    return mirror; // FxStateMirror implements StatePublisher
}

@Provides
@Singleton
FxStateMirror provideFxStateMirror() {
    return new FxStateMirror();
}
```

**Acceptance criteria:**

- `mvn compile -q -ff` passes (Guice wiring verified at compile time via JPMS)
- `DIBackendModule` has no `javafx.*` imports

---

### TASK-3.7 — Add `StatePublisher` interface and update module-info files

**Status:** ✅ DONE

**Goal:** Define `StatePublisher` in `app/api`, add `StatePublisher` to `app/api` exports, add `app/backend` requirement
to `app/ui`, add `app/api` and `app/backend` to parent POM.

**Files to modify:**

- `app/api/src/main/java/ua/renamer/app/api/session/StatePublisher.java` — new file (defined in TASK-3.4)
- `app/api/src/main/java/module-info.java` — confirm `ua.renamer.app.api.session` is exported
- `app/ui/src/main/java/module-info.java` — add `requires ua.renamer.app.backend;`
- `app/backend/src/main/java/module-info.java` — add `requires ua.renamer.app.core;` (for `FileRenameOrchestrator`)
- `app/pom.xml` — confirm `backend` is in `<modules>` list

**Acceptance criteria:**

- `mvn compile -q -ff` from `app/` directory passes with all modules
- `mvn dependency:tree -pl app/backend` shows no JavaFX dependency

---

## Part 4 — Connecting UI to New Backend API

**Goal:** Implement `FxStateMirror`, `ModeViewRegistry`, define `ModeControllerV2Api`, update
`ApplicationMainViewController` for dual-path routing, then migrate all 10 mode controllers one at a time.

**Key principle:** App remains runnable throughout. Each migrated controller is independently testable. The dual-path
routing (V1 fallback for unmigrated controllers) ensures no big-bang transition.

---

### TASK-4.1 — Implement `FxStateMirror` in `app/ui` ✅ done

**Goal:** The FX-thread-safe bridge between backend state and JavaFX observable properties. Implements `StatePublisher`.
Every `publishX()` method wraps its body in `Platform.runLater()`.

**Context:**
`FxStateMirror` is the **only** class in the system that has both backend-side responsibilities (`StatePublisher`
publish methods) and FX-side responsibilities (observable properties for UI binding). It lives in `app/ui` where JavaFX
is allowed. All `publishX()` methods are called from the backend state thread and must dispatch to the FX thread before
mutating observable collections.

**File to create:**

```
app/ui/src/main/java/ua/renamer/app/ui/state/FxStateMirror.java
```

**Implementation:**

```java

@Singleton
public class FxStateMirror implements StatePublisher {
    // Read-only observable properties for UI binding
    private final ObservableList<RenameCandidate> filesList = FXCollections.observableArrayList();
    private final ObservableList<RenamePreview> previewList = FXCollections.observableArrayList();
    private final SimpleObjectProperty<SessionStatus> statusProp = new SimpleObjectProperty<>(SessionStatus.EMPTY);
    private final SimpleObjectProperty<TransformationMode> modeProp = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<ModeParameters> paramsProp = new SimpleObjectProperty<>();

    public ReadOnlyListProperty<RenameCandidate> files() {
        return new ReadOnlyListWrapper<>(filesList).getReadOnlyProperty();
    }

    public ReadOnlyListProperty<RenamePreview> preview() {
        return new ReadOnlyListWrapper<>(previewList).getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<SessionStatus> status() {
        return statusProp;
    }

    public ReadOnlyObjectProperty<TransformationMode> activeMode() {
        return modeProp;
    }

    public ReadOnlyObjectProperty<ModeParameters> currentParameters() {
        return paramsProp;
    }

    @Override
    public void publishFilesChanged(List<RenameCandidate> files, List<RenamePreview> preview) {
        Platform.runLater(() -> {
            filesList.setAll(files);
            previewList.setAll(preview);
        });
    }

    @Override
    public void publishPreviewChanged(List<RenamePreview> preview) {
        Platform.runLater(() -> previewList.setAll(preview));
    }

    @Override
    public void publishModeChanged(TransformationMode mode, ModeParameters params) {
        Platform.runLater(() -> {
            modeProp.set(mode);
            paramsProp.set(params);
        });
    }

    @Override
    public void publishRenameComplete(List<RenameSessionResult> results, SessionStatus status) {
        Platform.runLater(() -> {
            statusProp.set(status);
            // Update previewList with final actual names from results
        });
    }

    @Override
    public void publishStatusChanged(SessionStatus status) {
        Platform.runLater(() -> statusProp.set(status));
    }
}
```

**Acceptance criteria:**

- `FxStateMirror implements StatePublisher` compiles
- `publishFilesChanged()` called from non-FX thread does not throw
- Observable properties change only on FX thread (verified with `Platform.isFxApplicationThread()` assertion)

---

### TASK-4.2 — Define `ModeControllerV2Api` and `ModeViewRegistry` ✅ done

**Goal:** Define the interface that migrated mode controllers implement, and the registry that maps `TransformationMode`
to view factories.

**Context:**
`ModeControllerV2Api<P>` is the marker interface that `ApplicationMainViewController` uses to detect whether a mode
controller has been migrated. During Phase 4 migration, some controllers implement it; others still use the old V1 path.
Once all 10 are migrated, the V1 fallback is removed.

`ModeViewRegistry` replaces the `InjectQualifiers` boilerplate for FXML loading. Instead of 10
`@Provides @Named("ModeXxx") FXMLLoader` methods, a registry maps each `TransformationMode` to a `Supplier<Node>` that
loads the FXML and returns the view.

**Files to create:**

```
app/ui/src/main/java/ua/renamer/app/ui/controller/mode/ModeControllerV2Api.java
app/ui/src/main/java/ua/renamer/app/ui/view/ModeViewRegistry.java
```

**ModeControllerV2Api:**

```java
public interface ModeControllerV2Api<P extends ModeParameters> {
    /**
     * Called by ApplicationMainViewController after mode selection.
     * The controller attaches listeners to its FXML controls that call
     * modeApi.updateParameters(p -> p.withField(value)) on each change.
     */
    void bind(ModeApi<P> modeApi);

    /** The TransformationMode this controller handles. */
    TransformationMode supportedMode();
}
```

**ModeViewRegistry:**

```java

@Singleton
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeViewRegistry {
    // Maps TransformationMode to a Supplier that loads the mode's FXML view
    // Initially empty; each controller registers itself on initialization
    private final Map<TransformationMode, Supplier<Parent>> registry = new EnumMap<>(TransformationMode.class);

    public void register(TransformationMode mode, Supplier<Parent> viewFactory) {
        registry.put(mode, viewFactory);
    }

    public Optional<Parent> getView(TransformationMode mode) {
        return Optional.ofNullable(registry.get(mode)).map(Supplier::get);
    }
}
```

**Acceptance criteria:**

- `ModeControllerV2Api` compiles with generic `P extends ModeParameters`
- `ModeViewRegistry.register()` and `getView()` work for all 10 modes

---

### TASK-4.3 — Update `ApplicationMainViewController` for dual-path routing ✅ done

**Goal:** Inject `SessionApi` into the main controller alongside the existing `CoreFunctionalityHelper`. Add dual-path
routing: if the loaded mode controller implements `ModeControllerV2Api`, route through V2; otherwise fall back to V1.

**Context:**
This task makes the V2 path active for the first time in the UI — but only for controllers that have been migrated (none
yet at this point). The V1 path remains fully functional. This is the enabler for all subsequent controller migration
tasks.

**File to modify:**

```
app/ui/src/main/java/ua/renamer/app/ui/controller/ApplicationMainViewController.java
```

**Changes:**

1. Add `SessionApi sessionApi` as a constructor parameter (inject alongside existing params)
2. Add `FxStateMirror fxStateMirror` as a constructor parameter
3. In `initialize()`, bind FxStateMirror observable properties to TableView columns and status indicators
4. In `onModeSelected(TransformationMode mode)`:
   ```java
   void onModeSelected(TransformationMode mode) {
       // Load the mode view (FXML or registry)
       Object controller = fxmlLoader.getController(); // or however controllers are obtained

       if (controller instanceof ModeControllerV2Api<?> v2ctrl) {
           // V2 path: bind to SessionApi
           sessionApi.selectMode(mode).thenAccept(modeApi ->
               Platform.runLater(() -> v2ctrl.bind((ModeApi) modeApi))
           );
       } else {
           // V1 fallback: existing command-based path (unchanged)
           legacyModeSwitch(mode, controller);
       }
   }
   ```
5. The Rename button: if `sessionApi.canExecute()`, call `sessionApi.execute()` and bind the returned `TaskHandle` to
   the progress bar. Otherwise use legacy path.

**Important:** Do NOT remove `CoreFunctionalityHelper` or any V1 code in this task. The dual path must coexist.

**Acceptance criteria:**

- `mvn compile -q -ff` passes
- App starts and loads successfully
- All 10 modes work exactly as before (all still on V1 path at this point — no controller has been migrated yet)

---

### TASK-4.4 — Migrate `ModeChangeExtensionController` to V2 ✅ done

**Goal:** First controller migration — simplest mode (1 field, minimal validation).

**Mode:** Change Extension (`CHANGE_EXTENSION`)
**ModeParameters:** `ExtensionChangeParams(String newExtension)`
**Validation:** `newExtension != null && !newExtension.isBlank()`
**V2 behavior:** `ERROR_TRANSFORMATION` for blank extension (stricter than V1 which allowed it)

**File to modify:**

```
app/ui/src/main/java/ua/renamer/app/ui/controller/mode/impl/ModeChangeExtensionController.java
```

**Changes:**

1. Add `implements ModeControllerV2Api<ExtensionChangeParams>` to the class declaration.
2. Add `@Override public TransformationMode supportedMode() { return TransformationMode.CHANGE_EXTENSION; }`
3. Replace `updateCommand()` method with `bind(ModeApi<ExtensionChangeParams> modeApi)`:
   ```java
   @Override
   public void bind(ModeApi<ExtensionChangeParams> modeApi) {
       // On text field change, call updateParameters
       newExtensionTextField.textProperty().addListener((obs, oldVal, newVal) ->
           modeApi.updateParameters(p -> p.withNewExtension(newVal))
               .thenAccept(result -> Platform.runLater(() -> {
                   if (result.isError()) {
                       showFieldError(result.field(), result.message());
                   } else {
                       clearFieldError();
                   }
               }))
       );
       // Initialize field from current parameters
       newExtensionTextField.setText(modeApi.currentParameters().newExtension());
   }
   ```
4. Keep the FXML file and `@FXML` field references unchanged.

**Acceptance criteria:**

- `mvn compile -q -ff` passes
- Change Extension mode works in the running app (V2 path active)
- V2 extension validation error message appears in UI when extension is blank
- Other 9 modes still work (V1 fallback)

**Build check:** `mvn test -q -ff -Dai=true`

---

### TASK-4.5 — Migrate `ModeChangeCaseController` to V2 ✅ done

**Mode:** Change Case (`CHANGE_CASE`)
**ModeParameters:** `ChangeCaseParams(TextCaseOptions caseOption, boolean capitalizeFirstLetter)`
**Validation:** `caseOption != null`

**Behavior from MODE_STATE_MACHINES.md:**

- `TextCaseOptions` enum: `CAMEL_CASE`, `PASCAL_CASE`, `SNAKE_CASE`, `SNAKE_CASE_SCREAMING`, `KEBAB_CASE`, `UPPERCASE`,
  `LOWERCASE`, `TITLE_CASE`
- `capitalizeFirstLetter`: additional first-character uppercase after the case change (V1 had a "Capitalize" flag)
- V2 is safe: guards `!newName.isEmpty()` before `substring(1)` — V1 had `IndexOutOfBoundsException` bug on empty names

**File to modify:**
`app/ui/src/main/java/ua/renamer/app/ui/controller/mode/impl/ModeChangeCaseController.java`

**Changes:** Same pattern as TASK-4.4:

1. `implements ModeControllerV2Api<ChangeCaseParams>`
2. `bind(ModeApi<ChangeCaseParams> modeApi)` — attach listener to case option ChoiceBox and capitalize checkbox; call
   `modeApi.updateParameters(p -> p.withCaseOption(val))` on each change.

**Acceptance criteria:** Change Case mode works via V2; 8 remaining modes still on V1.

---

### TASK-4.6 — Migrate `ModeAddCustomTextController` to V2 ✅ done

**Mode:** Add Text (`ADD_TEXT`)
**ModeParameters:** `AddTextParams(String textToAdd, ItemPosition position)`
**Validation:** `position != null` (empty text is allowed — no-op transformation)

**Behavior from MODE_STATE_MACHINES.md:**

- `ItemPosition`: `BEGIN` (prepend), `END` (append)
- Empty `textToAdd` is a no-op (not an error) — this differs from some other modes
- Result: `textToAdd + originalName` for BEGIN; `originalName + textToAdd` for END
- Parallelizable: YES

**File to modify:**
`app/ui/src/main/java/ua/renamer/app/ui/controller/mode/impl/ModeAddCustomTextController.java`

---

### TASK-4.7 — Migrate `ModeRemoveCustomTextController` to V2 ✅ done

**Mode:** Remove Text (`REMOVE_TEXT`)
**ModeParameters:** `RemoveTextParams(String textToRemove, ItemPosition position)`
**Validation:** `position != null`; `textToRemove` can be empty (no-op)

**Behavior from MODE_STATE_MACHINES.md:**

- `ItemPosition`: `BEGIN` (remove if filename starts with text), `END` (remove if filename ends with text)
- If text not found at position: original name returned (not error)
- If removing text leaves empty name: V2 returns `ERROR_TRANSFORMATION`
- Parallelizable: YES

---

### TASK-4.8 — Migrate `ModeReplaceCustomTextController` to V2 ✅ done

**Mode:** Replace Text (`REPLACE_TEXT`)
**ModeParameters:**
`ReplaceTextParams(String textToReplace, String replacementText, ItemPositionWithReplacement position)`
**Validation:** `position != null`; both text fields can be empty (empty replace = remove; empty replacement = delete
match)

**Behavior from MODE_STATE_MACHINES.md:**

- `ItemPositionWithReplacement`: `BEGIN` (replace first occurrence at start), `END` (replace last at end),
  `EVERYWHERE` (replace all occurrences)
- **Critical V2 fix vs V1:** V2 uses **literal** string matching (`String.replace()`, `startsWith()`, `endsWith()`). V1
  used `replaceFirst(regex, replacement)` — which treated user input as a regex pattern. This is a V1 security issue.
  V2's literal matching is correct and intentional. Do NOT revert.
- If text not found: original name returned unchanged (no error)
- Result after replacement must not be empty: `ERROR_TRANSFORMATION` if result is empty
- Parallelizable: YES

---

### TASK-4.9 — Migrate `ModeTruncateFileNameController` to V2 ✅ done

**Mode:** Truncate File Name (`TRUNCATE_FILE_NAME`)
**ModeParameters:** `TruncateParams(int numberOfSymbols, TruncateOptions truncateOption)`
**Validation:** `truncateOption != null`, `numberOfSymbols >= 0`

**Behavior from MODE_STATE_MACHINES.md:**

- `TruncateOptions`: `REMOVE_FROM_BEGIN` (remove first N chars), `REMOVE_FROM_END` (remove last N chars)
- If `numberOfSymbols >= name.length()`: entire name removed → V2 returns `ERROR_TRANSFORMATION` (empty name not
  allowed)
- V2's strict "empty name = error" is intentional and correct (V1 allowed empty names silently)
- Parallelizable: YES

---

### TASK-4.10 — Migrate `ModeUseParentFolderNameController` to V2 ✅ done

**Mode:** Use Parent Folder Name (`USE_PARENT_FOLDER_NAME`)
**ModeParameters:** `ParentFolderParams(int numberOfParentFolders, ItemPosition position, String separator)`
**Validation:** `position != null`, `numberOfParentFolders >= 1`

**Behavior from MODE_STATE_MACHINES.md:**

- `ItemPosition`: `BEGIN` (prepend folder path), `END` (append folder path)
- `numberOfParentFolders`: how many parent directory levels to include (1 = immediate parent, 2 = parent/grandparent,
  etc.)
- `separator`: string inserted between folder path and filename (e.g., `" "`, `"_"`, `"-"`)
- **V2 strict behavior:** If file is at root level (no parent folder): `ERROR_TRANSFORMATION` (V1 returned unchanged
  name silently — V2's behavior is correct)
- Parallelizable: YES

---

### TASK-4.11 — Migrate `ModeUseImageDimensionsController` to V2

**Mode:** Use Image Dimensions (`USE_IMAGE_DIMENSIONS`)
**ModeParameters:**
`ImageDimensionsParams(ImageDimensionOptions leftSide, ImageDimensionOptions rightSide, ItemPositionWithReplacement position, String nameSeparator)`
**Validation:** `position != null`; at least one of `leftSide`/`rightSide` must not be `DO_NOT_USE`

**Behavior from MODE_STATE_MACHINES.md:**

- `ImageDimensionOptions`: `DO_NOT_USE`, `WIDTH`, `HEIGHT`
- The dimension string is built as: `[leftSide value][separator][rightSide value]` — e.g., `"1920x1080"` if separator is
  `"x"`, left=WIDTH, right=HEIGHT
- `ItemPositionWithReplacement`: `BEGIN` (prepend with nameSeparator), `END` (append with nameSeparator), `REPLACE` (
  replace filename entirely)
- For non-image files: metadata extraction returns no dimensions → `ERROR_TRANSFORMATION`
- `nameSeparator` (fixed in TASK-1.4): now configurable, default `""` (no separator between dimension block and
  filename)

**This mode requires the fix from TASK-1.4 (BUG-1) to be complete before migration.**

**Status:** ✅ DONE

---

### TASK-4.12 — Migrate `ModeAddSequenceController` to V2 ✅ DONE

**Mode:** Add Sequence (`ADD_SEQUENCE`)
**ModeParameters:** `SequenceParams(int startNumber, int stepValue, int paddingDigits, SortSource sortSource)`
**Validation:** `sortSource != null`, `stepValue > 0`, `paddingDigits >= 0`

**Behavior from MODE_STATE_MACHINES.md:**

- **SEQUENTIAL ONLY** — cannot be parallelized. `SequenceParams.requiresSequentialExecution()` returns `true`. The V2
  orchestrator checks this and uses `transformBatch()` instead of `transform()`.
- `SortSource` enum: `FILE_NAME`, `FILE_PATH`, `FILE_SIZE`, `FILE_CREATION_DATETIME`, `FILE_MODIFICATION_DATETIME`,
  `FILE_CONTENT_CREATION_DATETIME`, `IMAGE_WIDTH`, `IMAGE_HEIGHT`
- Files are sorted by `sortSource` before sequence number assignment; files with null source values sort first (0 /
  alphabetically first)
- `startNumber`: first sequence number (e.g., 1 for `001`)
- `stepValue`: increment per file (must be > 0; stepValue=0 causes all files to get the same number → all become
  conflicts → high duplicate rate)
- `paddingDigits`: number of zero-padding digits (0 = no padding; validated >= 0 in TASK-1.2)
- Conflict risk: HIGH when stepValue is small; `DuplicateNameResolverImpl` handles in-batch conflicts with suffixes

**This mode's migration requires that TASK-1.2 (BUG-2 negative padding fix) is complete.**

---

### TASK-4.13 — Migrate `ModeUseDatetimeController` to V2

**Mode:** Use Datetime (`USE_DATETIME`)
**ModeParameters:** `DateTimeParams` (11 fields — most complex mode)

**Validation:**

- `source != null`
- At least one of `useDatePart` or `useTimePart` must be `true`
- If `source == CUSTOM_DATE`, `customDateTime` must not be null
- `position != null`

**Behavior from MODE_STATE_MACHINES.md:**

- `DateTimeSource`: `FILE_CREATION_DATETIME`, `FILE_MODIFICATION_DATETIME`, `FILE_CONTENT_CREATION_DATETIME`,
  `CURRENT_DATE`, `CUSTOM_DATE`
- `DateFormat`: multiple date format patterns
- `TimeFormat`: multiple time format patterns (some include AM/PM `a` pattern)
- `position`: `ItemPosition.BEGIN` or `ItemPosition.END`
- `useFallbackDateTime`: when true and primary source returns null, fall back to earliest available date (TASK-1.5)
- `useCustomDateTimeAsFallback`: when true and all natural dates are null, use `customDateTime` (TASK-1.7)
- `useUppercaseForAmPm`: controls AM/PM case in formatted output (TASK-1.6)
- `applyToExtension`: when true, include extension in datetime transformation
- Parallelizable: YES (despite metadata extraction being inherently sequential per file, the orchestrator handles this)

**This mode requires TASK-1.5, TASK-1.6, TASK-1.7 to be complete before migration.**

**Note:** This controller has the most complex FXML — 10+ interdependent controls with conditional visibility. Read the
existing `ModeUseDatetimeController.java` before implementing `bind()` to understand which controls exist. The FXML is
unchanged; only `bind()` replaces `updateCommand()`.

---

### TASK-4.14 — Final cleanup: remove V1 infrastructure

**Goal:** After all 10 mode controllers are migrated to V2, remove the dual-path routing, `CoreFunctionalityHelper`, V1
command wiring, and unused `InjectQualifiers` entries.

**Prerequisite:** ALL of TASK-4.4 through TASK-4.13 are complete. All modes work on the V2 path. The V1 fallback branch
in `ApplicationMainViewController.onModeSelected()` is dead code.

**Files to delete — V1 command classes (app/core):**

- All 10 `*PrepareInformationCommand.java` in `app/core/.../service/command/impl/preparation/`
- `MapFileToFileInformationCommand.java`
- `MapFileInformationToRenameModelCommand.java`
- `FixEqualNamesCommand.java`
- `ResetRenameModelsCommand.java`
- `RenameCommand.java`

**Files to delete — V1 domain model (app/core):**

- `FileInformation.java` — V1 mutable file model; all binding migrated to `FxStateMirror` snapshot types (
  `RenameCandidate`, `RenamePreview`, `RenameSessionResult`)
- `RenameModel.java` — V1 result model; replaced by `RenameCandidate` + `RenamePreview` + `RenameSessionResult`
- `RenameModelToHtmlMapper.java` — WebView HTML display replaced by structured `TableView` columns bound to
  `RenameSessionResult` fields

**Files to delete — V1 UI infrastructure (app/ui):**

- `CoreFunctionalityHelper.java` — replaced by `SessionApi` injection in `ApplicationMainViewController`
- `CommandModel.java` — replaced by `ModeApi<P>.currentParameters()` + `FxStateMirror`
- `ModeControllerApi.java` (old V1 interface) — replaced by `ModeControllerV2Api`
- `MainViewControllerHelper.java` — mode binding responsibility moves to `ModeViewRegistry`

**Qualifier cleanup (app/ui):**

- Remove 20 of 30 `InjectQualifiers` annotations (FXMLLoader and Parent qualifiers — replaced by `ModeViewRegistry`)
- Remove corresponding `@Provides` methods in `DIUIModule` (20 of 30 mode-related methods)
- Remove the `@AppGlobalRenameModelList` qualifier and its `@Provides` singleton binding for
  `ObservableList<RenameModel>` — replaced by `FxStateMirror.files()` and `FxStateMirror.preview()`

**Files to update:**

- `ApplicationMainViewController.java` — remove V1 fallback branch, remove `CoreFunctionalityHelper` injection, bind to
  `FxStateMirror` observable properties instead of `ObservableList<RenameModel>`
- `DIUIModule.java` — remove V1 command bindings, remove `@AppGlobalRenameModelList` binding, clean up unused qualifiers
- `DICoreModule.java` — remove V1 command bindings
- `module-info.java` files — remove exports of now-deleted packages
- `InjectQualifiers.java` — reduce from 31 to 10 annotations (only controller qualifiers remain)

**Post-cleanup build check:**

```bash
cd app && mvn clean install -q      # Full build with all modules
cd app && mvn verify -Pcode-quality -q  # Checkstyle, PMD, SpotBugs — zero violations
```

**Acceptance criteria:**

- All 10 modes work via V2 pipeline exclusively
- No `CoreFunctionalityHelper` class exists
- No `FileInformationCommand` implementations exist in `preparation/` package
- `mvn test -q -ff -Dai=true` — zero failures
- `mvn verify -Pcode-quality -q` — zero violations
- App starts, files can be loaded, all 10 modes produce previews, rename executes and produces correct results

---

## Testing Strategy Summary

Each task requires tests before the next task begins. The following matrix summarizes test locations and required
coverage:

| Part           | Test Type                  | Location Pattern                                | Coverage Gate                   |
|----------------|----------------------------|-------------------------------------------------|---------------------------------|
| Part 1         | Unit (fast, no I/O)        | `app/core/src/test/...`                         | 100% branch on new/changed code |
| Part 1 (BUG-4) | Integration (`@TempDir`)   | `...RenameExecutionServiceImplDiskConflictTest` | Happy + 2 conflict cases        |
| Part 2         | Unit                       | `app/api/src/test/...`                          | All records + validate()        |
| Part 3         | Unit (mock StatePublisher) | `app/backend/src/test/...`                      | Session lifecycle + preview     |
| Part 4         | Manual (JavaFX)            | App runs                                        | Each mode end-to-end            |

**Running tests after each task:**

```bash
# Single test class
cd app && mvn test -q -ff -Dai=true -Dtest=ClassName

# All tests
cd app && mvn test -q -ff -Dai=true

# Full quality check (after completing a part)
cd app && mvn verify -Pcode-quality -q
```

---

## Dependency Graph Between Tasks

```
Part 1:
  1.1 ──────────────────────────────────────────────────────── (independent)
  1.2 ──────────────────────────────────────────────────────── (independent)
  1.3 ──────────────────────────────────────────────────────── (independent)
  1.4 ← 1.3 (validation pattern established)
  1.5 ← 1.3
  1.6 ← 1.3 (can be done same session as 1.5)
  1.7 ← 1.5 (fallback logic must exist)
  1.8 ← 1.3 (configs stable)
  1.9 ← (independent — touches RenameExecutionServiceImpl separately from 1.10)
  1.10 ← 1.9 (both touch same file, coordinate)

Part 2:
  2.1 ← Part 1 complete (V2 stable before building API on top)
  2.2 ← 2.1
  2.3 ← 2.2
  2.4 ← 2.2, 2.3
  (2.1–2.4 can proceed in sequence)

Part 3:
  3.1 ← 2.1 (module exists)
  3.2 ← 3.1
  3.3 ← 2.4 (ModeParametersConverter needs all 10 records)
  3.4 ← 3.2, 3.3 (needs BackendExecutor + converter)
  3.5 ← 3.4
  3.6 ← 3.5
  3.7 ← 3.6

Part 4:
  4.1 ← 3.7
  4.2 ← 4.1
  4.3 ← 4.1, 4.2
  4.4–4.13: each ← 4.3; can proceed in any order (recommended: simplest to most complex)
  4.14 ← ALL of 4.4–4.13
```

---

## Quick Reference: Files Changed Per Part

| Part    | Module                              | Files Created                                                                                                                                               | Files Modified                                                                     |
|---------|-------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| 1       | `app/core`, `app/api`               | test classes                                                                                                                                                | 10 transformers, 10 configs, `ThreadAwareFileMapper`, `RenameExecutionServiceImpl` |
| 2       | `app/api`, `app/backend` (skeleton) | `SessionApi`, `ModeApi`, `ModeParameters` + 10 records, `TaskHandle`, value types                                                                           | `module-info.java`, `pom.xml`                                                      |
| 3       | `app/backend`                       | `RenameSession`, `BackendExecutor`, `ModeParametersConverter`, `RenameSessionService`, `SessionApiImpl`, `DIBackendModule`, `ModeApiImpl`, `TaskHandleImpl` | `DIUIModule`                                                                       |
| 4       | `app/ui`                            | `FxStateMirror`, `ModeViewRegistry`, `ModeControllerV2Api`                                                                                                  | `ApplicationMainViewController`, 10 mode controllers                               |
| Cleanup | `app/core`, `app/ui`                | —                                                                                                                                                           | Delete V1 command classes; reduce `InjectQualifiers`                               |

---

*This document is the authoritative implementation plan. Each task is designed to be implementable by the Coder agent in
a single session. Before starting any task, the Coder agent should read: (1) this task's section, (2) the relevant
sections from `docs/MODE_STATE_MACHINES.md` (for mode-specific tasks), (3) the actual source files listed under "
Affected files".*
