# V2 Stabilization Plan (Phase 0)

**Document Type:** Implementation Plan
**Status:** Ready for Implementation
**Last Updated:** March 2026
**Prerequisite for:** Phase 1 of `V2_API_DESIGN_APPROACHES.md` (Structural Foundation)
**Audience:** Developers implementing V2 fixes before the backend API migration

---

## Purpose

Before the new backend API (`SessionApi`, `ModeApi<P>`, `FxStateMirror`) is built on top of V2, the V2 pipeline must be **feature-complete relative to V1**, **null-safe**, and **bug-free**. Building the API facade on top of a broken foundation would propagate defects upward and make them harder to fix later.

This document is the authoritative checklist for Phase 0. It must be completed and all acceptance criteria met before Phase 1 begins.

**Decision:** V2's stricter validation behavior (errors on empty truncation result, errors on empty extension, errors when no parent folder exists) is **intentional and correct** — it is not a bug. These are improvements over V1's silent-skip behavior and are preserved as-is.

---

## Scope: What Changes, What Doesn't

**Changes in Phase 0:**
- Bug fixes in V2 transformer and config classes
- Feature additions to V2 configs and transformers (restoring V1 capabilities)
- General null-safety hardening across all transformers
- Config validation in constructors/compact constructors
- NameValidator integration in execution service

**Does NOT change in Phase 0:**
- V1 command classes (`FileInformationCommand` implementations) — left untouched
- UI controllers, FXML files, DI wiring — left untouched
- The 4-phase pipeline orchestration in `FileRenameOrchestratorImpl` — left untouched
- Public API signatures of `FileRenameOrchestratorImpl.execute()` — left untouched
- Maven module structure — no new modules added yet (that is Phase 1)

---

## Fix Inventory

### BUG-1: ImageDimensionsTransformer — hardcoded `nameSeparator`

**Severity:** High — feature regression, user-visible behavior locked to hardcoded value

**Problem:**
`ImageDimensionsTransformer` hardcodes a space `" "` between the dimension block and the filename when `position` is `BEGIN` or `END`. V1 had a configurable `nameSeparator` field (passed as a constructor parameter to `ImageDimensionsPrepareInformationCommand`). V2 dropped this field entirely from `ImageDimensionsConfig`.

Example of broken behavior:
```
// User wants: "1920x1080_photo.jpg"  (separator = "_")
// V2 produces: "1920x1080 photo.jpg" (hardcoded space — wrong)
```

**Affected Files:**
1. `app/api/src/main/java/ua/renamer/app/api/model/config/ImageDimensionsConfig.java`
2. `app/core/src/main/java/ua/renamer/app/core/v2/service/transformation/ImageDimensionsTransformer.java`

**Changes Required:**

`ImageDimensionsConfig.java`:
- Add field: `String nameSeparator` — the separator inserted between the dimension block and the filename
- Default value for the builder: `""` (empty string — no separator, closest to the current hardcoded space behavior but explicit and configurable)
- The field must be non-null; use `""` as the neutral/default value

`ImageDimensionsTransformer.java`:
- In the `BEGIN` case: replace `dimensionStr + " " + input.getName()` with `dimensionStr + config.getNameSeparator() + input.getName()`
- In the `END` case: replace `input.getName() + " " + dimensionStr` with `input.getName() + config.getNameSeparator() + dimensionStr`
- The `REPLACE` case is unaffected (no name involved)

**Acceptance Criteria (JUnit 5):**
- `givenNameSeparatorUnderscore_whenPositionBegin_thenDimensionAndNameJoinedWithUnderscore()`
- `givenNameSeparatorEmpty_whenPositionEnd_thenDimensionAndNameConcatenatedDirectly()`
- `givenNameSeparatorSpace_whenPositionBegin_thenSpaceInserted()` — verifies old behavior is achievable with explicit config
- `givenNameSeparatorNull_whenTransformerCalled_thenNullPointerNotThrown()` — null-safety check

**Complexity:** Small (2 files, ~10 lines of change)

---

### BUG-2: SequenceTransformer — negative or zero `padding` causes format exception

**Severity:** Medium — crashes the pipeline for invalid user input instead of returning a graceful error

**Problem:**
`SequenceTransformer.formatSequenceNumber(int number, int padding)` calls:
```java
String.format("%0" + padding + "d", number)
```
If `padding` is negative (e.g., user enters `-1` in a spinner), `String.format` throws `MissingFormatWidthException`. The outer try-catch in `transformBatch()` catches this and returns an error result, but the error message is opaque. The root cause should be caught at config validation time.

**Affected Files:**
1. `app/api/src/main/java/ua/renamer/app/api/model/config/SequenceConfig.java`
2. `app/core/src/main/java/ua/renamer/app/core/v2/service/transformation/SequenceTransformer.java`

**Changes Required:**

`SequenceConfig.java`:
- Add compact constructor (or `@Builder.Default` + static factory) that validates: `if (padding < 0) throw new IllegalArgumentException("padding must be >= 0, got: " + padding)`
- This makes invalid configs fail fast at construction, not at transformation time

`SequenceTransformer.java` — defensive guard in `formatSequenceNumber`:
```java
private String formatSequenceNumber(int number, int padding) {
    if (padding <= 0) {
        return String.valueOf(number); // No padding: use raw number
    }
    return String.format("%0" + padding + "d", number);
}
```
This guard ensures the transformer never crashes even if a config with `padding < 0` somehow bypasses constructor validation.

**Acceptance Criteria (JUnit 5):**
- `givenNegativePadding_whenConfigConstructed_thenIllegalArgumentExceptionThrown()`
- `givenZeroPadding_whenFormatSequenceNumber_thenRawNumberReturned()`
- `givenPositivePadding_whenFormatSequenceNumber_thenZeroPaddedStringReturned()`
- `givenNegativeStartNumber_whenFormatSequenceNumber_thenNegativeNumberReturnedWithoutCrash()`

**Complexity:** Small (2 files, ~15 lines of change)

---

### BUG-3: ThreadAwareFileMapper — no null-safety after metadata extraction

**Severity:** Medium — potential NullPointerException for files whose type is not handled by the metadata chain

**Problem:**
`ThreadAwareFileMapper.map(File file)` calls `fileMetadataMapper.extract(file, category, mimeType)`. The chain-of-responsibility returns `null` if no mapper in the chain handles the file type. The result is stored directly as `metadata` in `FileModel` without a null check. Downstream transformers that call `input.getMetadata()` expect an `Optional<FileMeta>` — if the Optional itself wraps correctly, this is safe; but if the contract is violated and `null` is returned as the Optional value rather than `Optional.empty()`, NPEs propagate silently.

**Affected Files:**
1. `app/core/src/main/java/ua/renamer/app/core/v2/service/impl/ThreadAwareFileMapper.java`

**Changes Required:**
- After calling `fileMetadataMapper.extract(...)`, wrap the result:
```java
FileMeta rawMeta = fileMetadataMapper.extract(file, category, mimeType);
Optional<FileMeta> metadata = Optional.ofNullable(rawMeta);
```
- Ensure `FileModel` is built with this `Optional.ofNullable()` to guarantee the Optional is never null itself
- Review `FileModel.getMetadata()` signature: confirm it returns `Optional<FileMeta>` (not nullable `FileMeta`)

**Acceptance Criteria (JUnit 5):**
- `givenUnknownFileType_whenMapped_thenMetadataIsEmptyOptional()` — not null, not NPE
- `givenNullFromMetadataChain_whenMapped_thenFileModelMetadataIsEmptyOptional()`
- `givenKnownFileType_whenMapped_thenMetadataIsPresentOptional()`

**Complexity:** Small (1 file, ~5 lines of change)

---

### BUG-4: RenameExecutionServiceImpl — disk conflict causes immediate error without suffix retry

**Severity:** Medium — poor user experience; a conflict with a pre-existing file fails the rename instead of finding a safe alternative name

**Problem:**
When `Files.exists(newPath)` returns true for a pre-existing file (one not in the current batch), `RenameExecutionServiceImpl` immediately returns `ERROR_EXECUTION` with message "Target file already exists". This is inconsistent with how batch conflicts are handled in Phase 2.5 (DuplicateNameResolverImpl), which automatically appends ` (01)`, ` (02)` etc.

The user sees a cryptic error when the solution is straightforward: try `name (01).ext`, then `name (02).ext`, etc.

**Affected Files:**
1. `app/core/src/main/java/ua/renamer/app/core/v2/service/impl/RenameExecutionServiceImpl.java`

**Changes Required:**

When the target path already exists (and it is NOT the source file itself — case-change rename on case-insensitive filesystem), attempt suffix resolution:

```java
// Pseudo-code for the fix:
private Path resolveConflictWithDisk(Path targetPath) {
    if (!Files.exists(targetPath)) return targetPath; // No conflict

    String baseName = getNameWithoutExtension(targetPath);
    String ext = getExtension(targetPath);
    Path parent = targetPath.getParent();

    for (int i = 1; i <= MAX_SUFFIX_ATTEMPTS; i++) {
        String suffix = " (" + String.format("%0" + digitCount(MAX_SUFFIX_ATTEMPTS) + "d", i) + ")";
        Path candidate = parent.resolve(baseName + suffix + ext);
        if (!Files.exists(candidate)) return candidate;
    }
    return null; // All attempts exhausted — caller returns ERROR_EXECUTION
}
private static final int MAX_SUFFIX_ATTEMPTS = 999;
```

- If `resolveConflictWithDisk` returns a non-null path, use it for the rename and record `SUCCESS` with a note that the name was adjusted
- If it returns null (999 candidates all exist — essentially impossible), return `ERROR_EXECUTION`
- The `RenameResult` model should capture the final used name (it already does via `PreparedFileModel.newName` which may need to be updated)

**Note:** This means `RenameResult` must carry the *actual* final name used, not just the planned name. Verify `RenameResult` has a field for this.

**Acceptance Criteria (JUnit 5):**
- `givenTargetFileExistsOnDisk_whenExecute_thenSuffixAppliedAndFileRenamed()` — integration test with real temp files
- `givenTargetAndSuffix01ExistOnDisk_whenExecute_thenSuffix02Applied()`
- `givenCaseChangeRename_whenTargetAppearsToExist_thenRenameSucceedsWithoutSuffix()` — case-insensitive filesystem edge case
- `givenNormalRename_whenNoConflict_thenNoSuffixAdded()`

**Complexity:** Medium (1 file, ~40 lines of logic, requires integration test with temp filesystem)

---

### FEATURE-1: DateTimeConfig/DateTimeTransformer — restore `useFallbackDateTime`

**Severity:** Feature regression — V1 users relying on fallback will see errors instead of best-available-date behavior

**Problem:**
V1's `DateTimeRenamePrepareInformationCommand` has `useFallbackDateTime: boolean`. When the selected datetime source returns null (e.g., file has no EXIF data and `CONTENT_CREATION_DATE` was selected), and `useFallbackDateTime = true`, V1 finds the earliest (minimum) of all available dates: `fsCreationDate`, `fsModificationDate`, `contentCreationDate`. V2 immediately returns `ERROR_TRANSFORMATION` with no fallback.

**Affected Files:**
1. `app/api/src/main/java/ua/renamer/app/api/model/config/DateTimeConfig.java`
2. `app/core/src/main/java/ua/renamer/app/core/v2/service/transformation/DateTimeTransformer.java`

**Changes Required:**

`DateTimeConfig.java`:
- Add field: `boolean useFallbackDateTime` (default: `false` — preserves current V2 behavior when not set)

`DateTimeTransformer.java`:
- In `extractDateTime()`, after the primary source extraction returns null:
```java
if (dateTime == null && config.isUseFallbackDateTime()) {
    // Collect all available dates and pick the earliest non-null one
    LocalDateTime creation = input.getCreationDate().orElse(null);
    LocalDateTime modification = input.getModificationDate().orElse(null);
    LocalDateTime contentCreation = extractContentCreationDate(input); // existing helper
    dateTime = Stream.of(creation, modification, contentCreation)
                     .filter(Objects::nonNull)
                     .min(Comparator.naturalOrder())
                     .orElse(null);
}
```
- If after fallback `dateTime` is still null, return `ERROR_TRANSFORMATION` as before

**Acceptance Criteria (JUnit 5):**
- `givenContentCreationDateMissingAndFallbackEnabled_whenExtract_thenEarliestAvailableDateUsed()`
- `givenAllDatesNullAndFallbackEnabled_whenExtract_thenErrorTransformation()`
- `givenFallbackDisabled_whenPrimarySourceNull_thenErrorTransformation()` — existing behavior unchanged
- `givenFallbackEnabled_whenPrimarySourcePresent_thenPrimaryUsedNotFallback()`

**Complexity:** Small (2 files, ~20 lines of change)

---

### FEATURE-2: DateTimeConfig/DateTimeTransformer — restore `useCustomDateTimeAsFallback`

**Severity:** Medium — V1 power-user feature for guaranteed date availability

**Problem:**
V1 allows the user to specify a `customDateTime` that serves as a guaranteed fallback when all natural date sources return null and `useFallbackDateTime = true`. Instead of finding the minimum of available dates, the system uses the user-provided custom date. This is useful for batch-processing files with no metadata where the user knows the approximate capture date.

**Affected Files:**
1. `app/api/src/main/java/ua/renamer/app/api/model/config/DateTimeConfig.java`
2. `app/core/src/main/java/ua/renamer/app/core/v2/service/transformation/DateTimeTransformer.java`

**Changes Required:**

`DateTimeConfig.java`:
- Add field: `boolean useCustomDateTimeAsFallback` (default: `false`)
- This field is only meaningful when `useFallbackDateTime = true`

`DateTimeTransformer.java`:
- Extend fallback logic from FEATURE-1:
```java
if (dateTime == null && config.isUseFallbackDateTime()) {
    if (config.isUseCustomDateTimeAsFallback() && config.getCustomDateTime().isPresent()) {
        dateTime = config.getCustomDateTime().get(); // Use custom date as fallback
    } else {
        // Find minimum of available natural dates (FEATURE-1 logic)
        dateTime = Stream.of(creation, modification, contentCreation)
                         .filter(Objects::nonNull)
                         .min(Comparator.naturalOrder())
                         .orElse(null);
    }
}
```

**Acceptance Criteria (JUnit 5):**
- `givenAllNaturalDatesNullAndCustomFallbackEnabled_whenExtract_thenCustomDateUsed()`
- `givenCustomFallbackEnabledButCustomDateNull_whenExtract_thenFallsBackToMinOfNaturalDates()`
- `givenCustomFallbackEnabledAndPrimarySourcePresent_thenPrimaryUsedNotCustom()`
- `givenCustomFallbackDisabledAndFallbackEnabled_thenMinNaturalDateUsed()`

**Complexity:** Small (2 files, ~10 lines added to FEATURE-1 changes)

---

### FEATURE-3: DateTimeConfig/DateTimeTransformer — restore `useUppercaseForAmPm`

**Severity:** Low — cosmetic formatting difference but user-visible in filenames

**Problem:**
V1's `DateTimeRenamePrepareInformationCommand` has `useUppercaseForAmPm: boolean`. When the selected `TimeFormat` includes AM/PM notation (Java format character `a`), V1 can uppercase (AM, PM) or lowercase (am, pm) the result. V2 uses whatever the JVM locale returns from `DateTimeFormatter` with the `a` pattern — typically uppercase AM/PM in English, but locale-dependent.

**Affected Files:**
1. `app/api/src/main/java/ua/renamer/app/api/model/config/DateTimeConfig.java`
2. `app/core/src/main/java/ua/renamer/app/core/v2/service/transformation/DateTimeTransformer.java`

**Changes Required:**

`DateTimeConfig.java`:
- Add field: `boolean useUppercaseForAmPm` (default: `true` — uppercase AM/PM is conventional in filenames)

`DateTimeTransformer.java`:
- After formatting datetime to string, if the `timeFormat` enum value is one of the AM/PM formats (those whose format pattern contains `a`):
```java
// Helper: does the selected TimeFormat use AM/PM?
private boolean isAmPmFormat(TimeFormat timeFormat) {
    return timeFormat.getPattern().contains("a"); // 'a' is the Java AM/PM designator
}

// After formatting:
if (isAmPmFormat(config.getTimeFormat())) {
    formattedDatetime = config.isUseUppercaseForAmPm()
        ? formattedDatetime.toUpperCase()
        : formattedDatetime.toLowerCase();
}
```

**Note:** `toUpperCase()` / `toLowerCase()` on the full string is safe because the date portion uses digits and separators (unaffected by case conversion). Only the AM/PM portion contains letters in an AM/PM formatted string.

**Acceptance Criteria (JUnit 5):**
- `givenAmPmFormatAndUppercaseTrue_whenFormat_thenResultContainsUppercaseAMPM()`
- `givenAmPmFormatAndUppercaseFalse_whenFormat_thenResultContainsLowercaseAmPm()`
- `givenNonAmPmFormat_whenFormat_thenUppercaseFlagHasNoEffect()`
- `givenAmPmFormatAndUppercaseTrue_whenDatePortionHasLetters_thenOnlyAmPmAffected()` — edge case for locales

**Complexity:** Small (2 files, ~15 lines of change)

---

### FEATURE-4: ImageDimensionsConfig — restore configurable `nameSeparator`

This is the same as **BUG-1**. Already fully specified above. Listed here for completeness to confirm it is both a bug fix AND a V1 feature restoration.

---

### STABILITY-1: All transformer `transform()` methods must be null-safe

**Severity:** Medium — prevents unexpected NullPointerExceptions in production

**Problem:**
All 10 transformer implementations receive a `config` parameter. If a caller passes null (possible during refactoring, incorrect DI wiring, or test setup), the transformers will throw NullPointerException from within the method body rather than returning a structured error result.

**Affected Files (all 10 transformers):**
```
app/core/src/main/java/ua/renamer/app/core/v2/service/transformation/
  AddTextTransformer.java
  RemoveTextTransformer.java
  ReplaceTextTransformer.java
  ChangeCaseTransformer.java
  SequenceTransformer.java
  TruncateTransformer.java
  ExtensionChangeTransformer.java
  DateTimeTransformer.java
  ImageDimensionsTransformer.java
  ParentFolderTransformer.java
```

**Changes Required (same pattern for all 10):**

In each `transform(FileModel input, XxxConfig config)` method, add a guard at the top (before the existing `isFile()` check):
```java
if (config == null) {
    return buildErrorResult(input, "Transformer configuration must not be null");
}
if (input == null) {
    // Cannot build a proper error result without input — throw as programming error
    throw new IllegalArgumentException("input FileModel must not be null");
}
```

Additionally, for string fields in configs that are used without null checks (e.g., `config.getTextToAdd()`, `config.getSeparator()`), add null-to-empty coercion where `null` should behave the same as empty:
```java
String text = config.getTextToAdd() != null ? config.getTextToAdd() : "";
```

**Acceptance Criteria (JUnit 5) — one test per transformer:**
- `givenNullConfig_whenTransform_thenErrorResultReturned()` — not NPE
- `givenConfigWithNullStringField_whenTransform_thenTreatedAsEmpty()` — for fields where null = empty

**Complexity:** Small per transformer, Medium total (10 files, ~5 lines each)

---

### STABILITY-2: Config compact constructors must validate invariants

**Severity:** Medium — fails fast at construction rather than at runtime during pipeline execution

**Problem:**
All V2 config classes use `@Value @Builder(setterPrefix = "with")` (Lombok). Lombok `@Value` generates a constructor but no validation. Invalid values (negative padding, null required fields, contradictory options) are caught only when the transformer tries to use them, deep inside the pipeline, producing confusing error messages.

**Affected Files:**

Each config class needs validation. Priority (highest risk first):

1. `SequenceConfig.java` — `padding >= 0`, `stepValue != 0` (warning if zero — will cause all-same-number collision), `sortSource != null`
2. `DateTimeConfig.java` — `source != null`, `dateFormat != null`, `timeFormat != null`, `position != null`; if `source == CUSTOM_DATE`, `customDateTime` must be present
3. `ImageDimensionsConfig.java` — `position != null`, at least one of `leftSide` or `rightSide` must not be `DO_NOT_USE`
4. `TruncateConfig.java` — `truncateOption != null`, `numberOfSymbols >= 0`
5. `ExtensionChangeConfig.java` — `newExtension != null`, `newExtension` is not blank after trim
6. `AddTextConfig.java` — `position != null`, `textToAdd != null`
7. `RemoveTextConfig.java` — `position != null`, `textToRemove != null`
8. `ReplaceTextConfig.java` — `position != null`, `textToReplace != null`, `replacementText != null`
9. `CaseChangeConfig.java` (or `ChangeCaseConfig.java`) — `caseOption != null`
10. `ParentFolderConfig.java` — `position != null`, `numberOfParentFolders >= 1`

**Pattern for each (since `@Value` classes use all-args constructor):**
Add a static factory method `of(...)` that validates, or use a `@Builder.ObtainVia` trick to add post-build validation. The simplest approach: replace `@Value` with `@Value` + manual constructor body that validates.

Alternatively, add validation to the Builder's `build()` method using a custom `build()` override.

**Acceptance Criteria (JUnit 5):**
- One test class per config: `XxxConfigValidationTest`
- Test each invariant: `givenNullPosition_whenBuild_thenIllegalArgumentException()`
- Test valid construction: `givenValidParams_whenBuild_thenNoException()`

**Complexity:** Medium (10 files, ~10-20 lines each)

---

### STABILITY-3: NameValidator called before physical rename

**Severity:** Medium — prevents OS-level exceptions from reaching the user as unformatted errors

**Problem:**
After transformation and duplicate resolution, the final filename may contain OS-restricted characters (e.g., `:` on Windows, `*`, `?`, `<`, `>`, `|`). If a transformer produces such a name (e.g., a datetime format like `"2024:01:01"` with colon separators), `Files.move()` throws `InvalidPathException`. This surfaces as `ERROR_EXECUTION` with a confusing OS message.

**Affected Files:**
1. `app/core/src/main/java/ua/renamer/app/core/v2/service/impl/RenameExecutionServiceImpl.java`
2. `app/core/src/main/java/ua/renamer/app/core/service/validator/impl/NameValidator.java` (read-only reference)

**Changes Required:**

In `RenameExecutionServiceImpl`, before attempting `Files.move()`:
```java
String finalName = preparedFile.getNewFullName();
if (!nameValidator.isValid(finalName)) {
    return RenameResult.builder()
        .withPreparedFile(preparedFile)
        .withStatus(RenameStatus.ERROR_TRANSFORMATION)
        .withErrorMessage("Generated filename contains invalid characters: " + finalName)
        .build();
}
```

Use `ERROR_TRANSFORMATION` (not `ERROR_EXECUTION`) because the issue is with the transformation result, not the physical disk operation.

**Note:** `NameValidator.isValid()` performs an OS-level path check (slow). It should be called once per file at execution time, not per-preview.

**Acceptance Criteria (JUnit 5):**
- `givenFilenameWithColonOnWindows_whenExecute_thenErrorTransformationReturned()` — platform-specific, may be skipped on non-Windows CI
- `givenFilenameWithSlash_whenExecute_thenErrorTransformationReturnedOnAllPlatforms()`
- `givenValidFilename_whenExecute_thenNameValidatorPassesAndRenameProceeds()`

**Complexity:** Small (1 file, ~10 lines + DI injection of NameValidator)

---

## Execution Order (dependency-ordered)

The fixes are independent of each other with the following exceptions:
- FEATURE-1 must be implemented before FEATURE-2 (fallback logic depends on it)
- STABILITY-2 (config validation) should be done before STABILITY-1 (transformer null-safety) so that tests can use valid configs

**Recommended implementation order:**

```
Round 1 — Foundation (no dependencies):
  BUG-3   (ThreadAwareFileMapper null-safety)
  BUG-2   (SequenceTransformer negative padding)
  STABILITY-2 (config validation — all 10 configs)

Round 2 — Config field additions (requires STABILITY-2 to be stable first):
  BUG-1 / FEATURE-4 (ImageDimensionsConfig nameSeparator)
  FEATURE-1          (DateTimeConfig useFallbackDateTime)
  FEATURE-3          (DateTimeConfig useUppercaseForAmPm)

Round 3 — Depends on FEATURE-1:
  FEATURE-2          (DateTimeConfig useCustomDateTimeAsFallback)

Round 4 — Transformer hardening:
  STABILITY-1        (null-safety in all 10 transformers)
  STABILITY-3        (NameValidator in RenameExecutionServiceImpl)

Round 5 — Execution service:
  BUG-4              (disk conflict suffix retry in RenameExecutionServiceImpl)
```

---

## Test Strategy

**Unit tests** (fast, no filesystem I/O):
- One test class per config class: `XxxConfigTest` — covers validation, builder defaults, withX() methods
- One test class per transformer: `XxxTransformerTest` — covers all algorithm branches, all edge cases from `MODE_STATE_MACHINES.md`, null-safety
- One test class per new feature: `DateTimeTransformerFallbackTest`, `DateTimeTransformerAmPmTest`

**Integration tests** (require real files on disk):
- `RenameExecutionServiceImplDiskConflictTest` — uses JUnit 5 `@TempDir` to create real files
- `FileRenameOrchestratorImplPhase0Test` — end-to-end pipeline test with fixed configurations

**Test location:** `app/core/src/test/java/...` (mirrors the main source tree)

**Coverage gate:** All new code must reach 100% branch coverage in unit tests. The `mvn clean test jacoco:report` report confirms this.

---

## Completion Gate

Phase 1 (Structural Foundation) may not begin until ALL of the following are true:

- [ ] All 4 bugs (BUG-1 through BUG-4) are fixed and have passing unit tests
- [ ] All 3 feature restorations (FEATURE-1 through FEATURE-3) are implemented with passing unit tests
- [ ] `ImageDimensionsConfig` has `nameSeparator` field (BUG-1 / FEATURE-4)
- [ ] All 10 config classes have constructor/builder validation (STABILITY-2)
- [ ] All 10 transformers have null-safe `transform()` methods (STABILITY-1)
- [ ] `RenameExecutionServiceImpl` calls `NameValidator` before `Files.move()` (STABILITY-3)
- [ ] `mvn test -q -ff -Dai=true` passes with zero failures
- [ ] `mvn verify -Pcode-quality -q` passes (Checkstyle + PMD + SpotBugs)
- [ ] No V2 transformer produces a NullPointerException for any input described in `MODE_STATE_MACHINES.md` edge case tables

---

## V2 Behavioral Differences from V1 — Intentionally Kept

The following V2 behaviors differ from V1 but are **correct improvements** and must NOT be reverted:

| Mode | V1 Behavior | V2 Behavior | Why V2 is Correct |
|------|-------------|-------------|-------------------|
| Truncate | Empty result (name = "") allowed | `ERROR_TRANSFORMATION` returned | A file with no name cannot be created on any OS |
| Change Extension | Empty extension allowed (removes extension entirely) | `ERROR_TRANSFORMATION` returned | Files with no extension and no name are confusing; empty extension after trim is almost always a user mistake |
| Parent Folder | Root-level file returns unchanged | `ERROR_TRANSFORMATION` returned | Silent no-op is misleading; user should know why the name didn't change |
| Replace Text | Uses `replaceFirst(regex, replacement)` — interprets pattern as regex | Uses `startsWith()` / `endsWith()` / `replace()` — literal matching | V1 is a latent security issue; regex metacharacters in user input cause unexpected behavior |
| Change Case (capitalize) | No bounds check before `substring(1)` — throws `IndexOutOfBoundsException` on empty name | Guards with `!newName.isEmpty()` check | V2 is null-safe; V1 has a bug |
