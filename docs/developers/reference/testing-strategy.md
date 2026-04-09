---
title: "Testing Strategy"
description: "Test framework, conventions, test data infrastructure, generation scripts, and coverage reporting for the Renamer App"
audience: "developers"
last_validated: "2026-04-09"
last_commit: "3c570e2"
related_modules:
  - "app/core"
  - "app/backend"
  - "app/metadata"
---

# Testing Strategy

This document describes the test framework, conventions, and test data infrastructure for the Renamer App.

## 1. Test Framework Stack

The project uses a lightweight testing stack optimized for fast feedback and deterministic assertions:

| Component             | Version | Purpose                                                           |
|-----------------------|---------|-------------------------------------------------------------------|
| JUnit Platform BOM    | 1.10.3  | Test execution engine and lifecycle                               |
| JUnit Jupiter         | 5.10.3  | JUnit 5 annotations, `@Test`, `@BeforeEach`, `@ParameterizedTest` |
| AssertJ               | 3.27.7  | Fluent, readable assertions                                       |
| Mockito               | 5.23.0  | Mocking and verification                                          |
| mockito-junit-jupiter | 5.23.0  | `@Mock`, `@ExtendWith(MockitoExtension.class)` integration        |
| Maven Surefire        | 3.5.5   | Test discovery and execution                                      |
| JaCoCo                | 0.8.14  | Coverage reporting (no enforced minimum threshold)                |

The project does NOT use Spring Test, Testcontainers, or any web test framework — this is a pure JavaFX desktop app.

### Common Assertion Styles

Each library contributes specific assertion patterns:

```java
// AssertJ — fluent, readable assertions (preferred for most cases)
assertThat(result.getNewName()).isEqualTo("prefix_document");

assertThat(results)
    .hasSize(3)
    .extracting(PreparedFileModel::getNewName)
    .containsExactly("a","b","c");

assertThat(result.getCreationDate())
    .isPresent()
    .hasValueSatisfying(dt ->
        assertThat(dt.getYear()).isEqualTo(2025));

// AssertJ — exception assertion
assertThatThrownBy(() -> method(null))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("must not be null");

// JUnit 5 — exception assertion (when exact type and stacktrace matter)
assertThrows(NullPointerException.class, () ->
    transformer.transform(null, config));

// Mockito — verification
var mock = mock(FilesOperations.class);
verify(mock, times(1)).move(any(), any());
```

---

## 2. Unit Test Conventions

### Naming Convention

Use the pattern `methodOrBehavior_stateOrInput_expectedOutcome`:

```
testTransform_whenFileNameIsEmpty_shouldReturnErrorResult
testExecute_withDuplicateNames_shouldAppendSuffixes
testProcessItem_withNullExtension_shouldPreserveEmptyExtension
```

### AAA Structure (Arrange-Act-Assert)

All tests follow an implicit Arrange-Act-Assert structure. When the logical sections are not immediately obvious from the code, include `// Given`, `// When`, `// Then` comment headers:

```java
@Test
void testAddTextTransformer_whenPrefixAndEmptyName_shouldOnlyAddPrefix() {
    // Given
    FileModel file = FileModel.builder()
            .withName("")
            .withExtension("txt")
            .build();

    AddTextConfig config = AddTextConfig.builder()
            .withTextToAdd("prefix_")
            .withPosition(ItemPosition.BEGIN)
            .build();

    // When
    PreparedFileModel result = transformer.transform(file, config);

    // Then
    assertThat(result.getNewName()).isEqualTo("prefix_");
}
```

### Mock vs. Instantiate Decision

| What                                             | How                  | Why                                                  |
|--------------------------------------------------|----------------------|------------------------------------------------------|
| `FilesOperations`                                | Mock                 | Performs real I/O; side effects                      |
| System clock / time providers                    | Mock                 | Non-deterministic                                    |
| External services with side effects              | Mock                 | Isolation from external state                        |
| `FileModel`, `PreparedFileModel`, `RenameResult` | Instantiate directly | Immutable value objects; use builders                |
| `FileInformation`, `RenameModel`                 | Instantiate directly | Mutable domain objects; straightforward construction |
| All transformers (`AddTextTransformer`, etc.)    | Instantiate directly | Pure logic; no external dependencies                 |
| `String`, `LocalDateTime`, collections           | Instantiate directly | Standard library types                               |

**Never mock transformers or value objects** — mocking them obscures the actual behavior under test. Instead, instantiate them and test their methods directly.

### V2 Builder Syntax (Critical)

V2 model builders require the `with` prefix for all setter methods. Omitting the prefix causes a compile error.

```java
// CORRECT — all V2 builders use .withFieldName()
FileModel model = FileModel.builder()
                .withFile(new File("/path/file.txt"))
                .withIsFile(true)
                .withFileSize(1024L)
                .withName("file")
                .withExtension("txt")
                .withAbsolutePath("/path/file.txt")
                .build();

PreparedFileModel result = PreparedFileModel.builder()
        .withOriginalFile(model)
        .withNewName("renamed")
        .withHasError(false)
        .build();

// Apply to all config objects
AddTextConfig config = AddTextConfig.builder()
        .withTextToAdd("prefix_")
        .withPosition(ItemPosition.BEGIN)
        .build();

// WRONG — compile error, "with" prefix is required
FileModel.builder()
    .file(f)
    .name("x")
    .build();  // ❌ Compile error
```

### @TempDir for Filesystem Tests

Annotate a `Path` or `File` field with `@TempDir` for tests that create real files. JUnit 5 creates the directory before each test and cleans it up automatically:

```java
class FileRenameTest {

    @TempDir
    Path tempDir;

    @Test
    void testRename_createsNewFile() throws IOException {
        // Create a test file
        Path file = tempDir.resolve("original.txt");
        Files.writeString(file, "content");

        // Perform rename
        Path renamed = tempDir.resolve("renamed.txt");
        Files.move(file, renamed);

        // Verify
        assertThat(Files.exists(file)).isFalse();
        assertThat(Files.exists(renamed)).isTrue();
    }
}
```

### Quality Rules

- **No `Thread.sleep()`** — Use Awaitility or polling loops for async assertions
- **No control flow in test methods** — Use `@ParameterizedTest` with `@MethodSource` or `@CsvSource` instead of `if`/`for`/`while`
- **One logical concept per test method** — Multiple assertions on the same output are OK; multiple independent scenarios are not
- **Hard-coded expected values only** — Never re-derive the expected value using production logic (defeats the purpose of the test)
- **`@Disabled` must include a reason** — `@Disabled("JIRA-123: description")`

---

## 3. Integration Tests

Integration tests exercise multiple components together with real files and I/O.

### DIBackendModuleTest

**Location:** `app/backend/src/test/java/ua/renamer/app/backend/config/DIBackendModuleTest.java`

Verifies that all Guice bindings in `DIBackendModule` resolve at injector-creation time and that `@Singleton` bindings return the same instance on repeated calls. Uses a real injector — not mocks.

The test supplements `DIBackendModule` with a stub binding for `StatePublisher`, which is normally provided by `DIUIModule` (the JavaFX layer):

```java
@BeforeEach
void setUp() {
    injector = Guice.createInjector(
            new DIBackendModule(),
            binder -> binder.bind(StatePublisher.class)
                    .toInstance(mock(StatePublisher.class))
    );
}

@Test
void sessionApiBinding_whenRequested_resolvesNonNull() {
    SessionApi instance = injector.getInstance(SessionApi.class);
    assertThat(instance).isNotNull();
}

@Test
void sessionApiBinding_whenRequestedTwice_returnsSameInstance() {
    SessionApi first = injector.getInstance(SessionApi.class);
    SessionApi second = injector.getInstance(SessionApi.class);
    assertThat(first).isSameAs(second);  // Singleton verification
}
```

Bindings verified:

- `SessionApi` resolves to `RenameSessionService`
- `BackendExecutor` resolves to a non-null instance
- Both are `@Singleton`: repeated calls return the same instance

### FullPipelineIntegrationTest

**Location:** `app/core/src/test/java/ua/renamer/app/core/v2/service/integration/FullPipelineIntegrationTest.java`

Exercises the complete four-phase V2 pipeline with real temporary files and the full orchestrator:

1. **Metadata Extraction** (parallel) — `File` → `FileModel` via `ThreadAwareFileMapper`
2. **Transformation** (parallel; sequential for `NUMBER_FILES`) — `FileModel` → `PreparedFileModel` via transformers
3. **Duplicate Resolution** (sequential) — `DuplicateNameResolver` appends `_1`, `_2` on name collisions
4. **Physical Rename** (parallel) — `PreparedFileModel` → `RenameResult` on disk

#### Base Class

Extends `BaseTransformationIntegrationTest`, which provides:

- `@TempDir Path tempDir` — temporary directory cleaned up after each test
- `FileRenameOrchestrator orchestrator` — manually assembled (avoids complex Guice setup for tests)
- Helper methods:
    - `createTestFile(String filename)` — single file
    - `createTestFiles(String baseName, String ext, int count)` — `file_1.ext`, `file_2.ext`, ...
    - `createTestFilesWithNames(String... filenames)` — custom names
    - `createTestFileWithSize(String filename, long sizeInBytes)` — file with specific size
    - `assertFileExists(String filename)` — verify file exists in tempDir
    - `assertFileNotExists(String filename)` — verify file does not exist
    - `createTrackingCallback()` — returns a `ProgressCallback` that updates `progressCurrent` and `progressMax` atomics
    - `logTestSummary(String testName, int created, int renamed)` — log test results

#### Transformation Modes Tested

| Mode               | Key Verifications                                               |
|--------------------|-----------------------------------------------------------------|
| `ADD_TEXT`         | Prefix and suffix variants; null callback handling              |
| `REMOVE_TEXT`      | From begin and end positions; text removed correctly            |
| `REPLACE_TEXT`     | At `EVERYWHERE` and `BEGIN` positions; all occurrences replaced |
| `CHANGE_CASE`      | Uppercase and lowercase transformations                         |
| `NUMBER_FILES`     | Basic (1-step-1) and custom (start=100, step=5) sequences       |
| `CHANGE_EXTENSION` | File extension changed; original files absent after rename      |
| `ADD_FOLDER_NAME`  | Nested folder; parent name prepended as prefix                  |
| `TRIM_NAME`        | Name truncated to specified symbol count                        |

#### Additional Scenarios

- **Async execution** — `orchestrator.executeAsync()` returns `CompletableFuture<List<RenameResult>>`; verified with `future.get()`
- **Progress callback** — `progressMax.get() > 0` confirms callback was invoked during execution
- **Null callback** — pipeline handles `null` callback without throwing `NullPointerException`
- **Mixed results** — 2 valid files + 1 non-existent file → 2 successes + 1 error; both counted correctly
- **Physical verification** — asserts new files exist on disk and old paths are gone
- **Large batch** — 100 files sequentially numbered; all 100 renamed and verified
- **Result metadata** — each `RenameResult` carries `getPreparedFile()`, `getStatus()`, `getExecutedAt()`, `getOriginalFileName()`, `getNewFileName()`; verified non-null for each result

---

## 4. Test Data

The project maintains two separate test data directories for unit and integration tests.

### Unit Test Data — `app/core`

**Location:** `app/core/src/test/resources/test-data/`

93 real media files across 15 formats. Used by metadata extractor tests in `app/core` and `app/metadata` modules.

**Distribution:**

| Category | Formats                                                           | Count             |
|----------|-------------------------------------------------------------------|-------------------|
| Audio    | FLAC, MP3, OGG, WAV                                               | 6 each (24 total) |
| Image    | BMP (3), GIF (3), HEIC (7), JPG (10), PNG (7), TIFF (4), WebP (7) | 41 total          |
| Video    | AVI, MKV, MOV, MP4                                                | 7 each (28 total) |

**Metadata Scenarios**

Each format has files covering 7 metadata states, named by convention:

| Suffix                        | Meaning                                    | Example                                                     |
|-------------------------------|--------------------------------------------|-------------------------------------------------------------|
| `_clean`                      | All metadata stripped                      | `test_jpg_clean.jpg`                                        |
| `_std`                        | Current creation date in EXIF/QuickTime    | `test_mp4_std_2025-12-11_21-00-35.mp4`                      |
| `_past_2000-01-01_…`          | Date set to 2000-01-01                     | `test_flac_past_2000-01-01_12-00-00.flac`                   |
| `_future_2050-01-01_…`        | Date set to 2050-01-01                     | `test_webp_future_2050-01-01_12-00-00.webp`                 |
| `_std_tz_…p02-00`             | Date with `+02:00` timezone offset         | `test_mp3_std_tz_2025-12-11_21-00-35p02-00.mp3`             |
| `_std_no_tz_…`                | Date without timezone                      | `test_png_std_no_tz_2025-12-11_21-00-35.png`                |
| `_gps_…_lat48.8566_lon2.3522` | Date + Paris GPS coordinates (images only) | `test_jpg_gps_2025-12-11_21-00-35_lat48.8566_lon2.3522.jpg` |

### Integration Test Data — `app/backend`

**Location:** `app/backend/src/test/resources/integration-test-data/`

18 semantically named files + `manifest.json` (committed to git). Used by `DIBackendModuleTest` and full pipeline integration tests that need predictable, reproducible file content and metadata.

**Directory Layout:**

```
integration-test-data/
├── manifest.json                    # Metadata index for all files
├── flat/                            # 5 text files
│   ├── document.txt
│   ├── report_final.md
│   ├── data_export.csv
│   ├── config_dev.json
│   └── no_extension
├── media/                           # 5 media files
│   ├── photo_1920x1080.jpg
│   ├── photo_no_exif.jpg
│   ├── image_800x600.png
│   ├── song_with_tags.mp3
│   └── audio_no_tags.wav
├── nested/                          # Hierarchical text files
│   ├── level1_a.txt
│   ├── level1_b.txt
│   ├── sublevel/
│   │   ├── level2_a.txt
│   │   └── deep/
│   │       └── level3_a.txt
└── multi_folder/                    # Files in multiple folders
    ├── folder_a/
    │   ├── file_x.txt
    │   └── file_y.txt
    └── folder_b/
        ├── file_p.txt
        └── file_q.txt
```

**manifest.json Structure**

Each file entry is keyed by relative path and contains static metadata:

```json
{
  "generated_at": "2026-04-08T12:00:00Z",
  "platform_note": "fs_creation_date and fs_modification_date are NOT reliable after file copy. Only content_creation_date (EXIF-embedded) is stable.",
  "files": {
    "media/photo_1920x1080.jpg": {
      "file_name": "photo_1920x1080",
      "extension": "jpg",
      "width": 1920,
      "height": 1080,
      "content_creation_date": "2025-06-15T08:22:15",
      "audio_artist": null,
      "audio_title": null,
      "audio_year": null
    },
    "media/song_with_tags.mp3": {
      "file_name": "song_with_tags",
      "extension": "mp3",
      "width": null,
      "height": null,
      "content_creation_date": null,
      "audio_artist": "TestArtist",
      "audio_title": "TestSong",
      "audio_year": 2020
    }
  }
}
```

**Critical Note**

`content_creation_date` is the only reliable metadata field — EXIF-embedded values survive file copies and file system operations. `fs_creation_date` and `fs_modification_date` are unreliable and MUST NOT be used in assertions. These filesystem-level timestamps change when files are copied or moved.

---

## 5. Generating Test Data

### generate_test_data.py — Unit Test Media Files

**Location:** `tools/generate_test_data.py`  
**Output:** `tools/test-data/` (not committed to git)

Generates 93 real media files with various metadata states. Output is copied manually to `app/core/src/test/resources/test-data/` to update unit test fixtures.

**Prerequisites:**

- FFmpeg — generates base media files
- ExifTool — embeds EXIF, GPS, and QuickTime metadata
- sips — macOS only, required for HEIC format (converts via intermediate JPG)

**What It Generates**

For each of 12 formats (jpg, png, heic, webp, mp4, mov, mkv, avi, mp3, wav, flac, ogg) × 7 scenarios:

| Scenario    | Metadata Applied                                                |
|-------------|-----------------------------------------------------------------|
| `clean`     | All metadata stripped with `exiftool -all=`                     |
| `std`       | Current datetime in all date fields                             |
| `past`      | `2000:01:01 12:00:00`                                           |
| `future`    | `2050:01:01 12:00:00`                                           |
| `std_tz`    | Current datetime with `+02:00` timezone offset                  |
| `std_no_tz` | Current datetime, no timezone                                   |
| `gps`       | Current datetime + Paris GPS (48.8566°N, 2.3522°E); images only |

**Run from project root:**

```bash
python3 tools/generate_test_data.py
```

### generate_integration_test_data.py — Integration Test Files

**Location:** `tools/generate_integration_test_data.py`  
**Output:** `app/backend/src/test/resources/integration-test-data/` (committed to git)

Generates a fixed set of 18 semantically named files (not a scenario matrix). The script is idempotent: re-running overwrites the output directory with identical content and regenerates `manifest.json`. Total output is kept under 10 MB.

**Prerequisites:**

- FFmpeg
- ExifTool
- Python `mutagen` library — for MP3 ID3 tag writing (installed via uv)

**Run via uv from project root:**

```bash
uv run tools/generate_integration_test_data.py
```

Commit the regenerated output if the script changes.

---

## 6. Running Tests

All commands run from the `app/` directory. `.mvn/maven.config` applies `-B --no-transfer-progress` globally.

| Command                                                 | Purpose                                               |
|---------------------------------------------------------|-------------------------------------------------------|
| `mvn test -q -ff -Dai=true`                             | All tests, quiet output, stop on first module failure |
| `mvn test -q -ff -Dai=true -Dtest=ClassName`            | Single test class by simple name                      |
| `mvn test -q -ff -Dai=true -Dtest=ClassName#methodName` | Single test method                                    |
| `mvn clean test jacoco:report -Dai=true`                | All tests + HTML coverage report                      |

**Flags:**

| Flag            | Meaning                                                                |
|-----------------|------------------------------------------------------------------------|
| `-q`            | Quiet — suppresses Maven INFO lines                                    |
| `-ff`           | Fail fast — stops on first module failure                              |
| `-Dai=true`     | Required for CI/AI environments: activates Surefire fork configuration |
| `jacoco:report` | Generates HTML coverage report after `test` completes                  |

**Running a specific module only:**

```bash
mvn test -q -ff -Dai=true -pl app/core
```

---

## 7. Coverage

JaCoCo `0.8.14` is configured for reporting only — there is no enforced coverage minimum in the build. Coverage reports are generated on demand and are not checked during `mvn verify`.

**Generate the HTML report:**

```bash
cd app
mvn clean test jacoco:report -Dai=true
```

**Report locations:**

After running the command above, reports are available at:

- `app/core/target/site/jacoco/index.html`
- `app/backend/target/site/jacoco/index.html`
- `app/metadata/target/site/jacoco/index.html`

**Note:** The `app/ui` module is excluded from coverage measurement. JavaFX controllers require a running FX toolkit and are tested through manual or integration-level testing rather than JaCoCo instrumentation.
