# Integration Test Suite — Implementation Plan

## Overview

This plan describes a comprehensive integration test suite that tests the **real backend pipeline** (no UI, no mocks) against **real files with real embedded metadata** for every transformation mode.

**Key gap filled:** The existing `BaseTransformationIntegrationTest` in `app/core/src/test/` hard-codes a mock metadata mapper (`FileMeta.empty()`), so every existing integration test gets `null` for EXIF dates, image dimensions, and audio tags. Modes like `ADD_DATETIME` (content creation source), `ADD_DIMENSIONS`, and `NUMBER_FILES` (sort by image width/height or content date) have never been tested against real metadata. The new suite closes that gap.

**Total scope:** 1 data generation task + 1 infrastructure task + 10 mode-specific tasks + 1 finalization task = **13 tasks**.

---

## Architecture

| Decision | Choice | Rationale |
|---|---|---|
| Test module | `app/backend` | Only module with `requires ua.renamer.app.metadata` in JPMS — enforces real extractor access |
| Pipeline entry | `FileRenameOrchestrator.execute()` | Synchronous, no session state machine complexity |
| Guice bootstrap | `DIBackendModule` + `mock(StatePublisher.class)` | Proven in `DIBackendModuleTest`; `StatePublisher` is the only unbound runtime dep |
| Test data location | `app/backend/src/test/resources/integration-test-data/` | Self-contained, not sharing `app/core` test resources |
| `@TempDir` scope | Per test **method** (instance field) | Each test gets a fresh copy; parallel execution safe |
| Guice injector scope | Per test **class** (`@BeforeAll` static `GuiceTestHelper`) | Avoids re-initializing Tika/metadata-extractor per method — shared static singleton |
| Manifest | `manifest.json` committed to git | ExifTool run once during data generation; tests read the manifest, never run ExifTool |

### Confirmed API

```java
// FileRenameOrchestrator (synchronous)
List<RenameResult> execute(List<File> files, TransformationMode mode, Object config, ProgressCallback cb);

// RenameResult fields (confirmed from source)
result.getStatus()           // RenameStatus: SUCCESS | SKIPPED | ERROR_EXTRACTION | ERROR_TRANSFORMATION | ERROR_EXECUTION
result.isSuccess()           // shortcut: status == SUCCESS
result.getNewFileName()      // calls preparedFile.getNewFullName() — includes extension
result.getOriginalFileName() // calls preparedFile.getOldFullName()

// V2 builder syntax (non-default prefix — CRITICAL)
AddTextConfig config = AddTextConfig.builder()
    .withTextToAdd("x")
    .withPosition(ItemPosition.BEGIN)
    .build();
```

---

## Test Data

Curated set (~20 files) generated once by `tools/generate_integration_test_data.py` and committed to git.

### Directory Structure

```
app/backend/src/test/resources/integration-test-data/
├── manifest.json
├── flat/
│   ├── document.txt          plain text; no metadata
│   ├── report_final.md
│   ├── data_export.csv
│   ├── config_dev.json
│   └── no_extension          file with no extension
├── media/
│   ├── photo_1920x1080.jpg   JPEG 1920×1080; EXIF DateTimeOriginal=2025-06-15T08:22:15
│   ├── photo_no_exif.jpg     JPEG 800×600; all EXIF stripped
│   ├── image_800x600.png     PNG 800×600; no EXIF
│   ├── song_with_tags.mp3    MP3 1s; ID3: artist=TestArtist, title=TestSong, year=2020
│   └── audio_no_tags.wav     WAV 1s; no tags
├── nested/
│   ├── level1_a.txt
│   ├── level1_b.txt
│   └── sublevel/
│       ├── level2_a.txt
│       └── deep/
│           └── level3_a.txt
└── multi_folder/
    ├── folder_a/
    │   ├── file_x.txt
    │   └── file_y.txt
    └── folder_b/
        ├── file_p.txt
        └── file_q.txt
```

### manifest.json Key Entries

```json
{
  "generated_at": "2026-04-08T12:00:00Z",
  "platform_note": "fs_creation_date and fs_modification_date are NOT reliable after file copy. Only content_creation_date (EXIF-embedded) is stable.",
  "files": {
    "media/photo_1920x1080.jpg": {
      "file_name": "photo_1920x1080", "extension": "jpg",
      "width": 1920, "height": 1080,
      "content_creation_date": "2025-06-15T08:22:15",
      "audio_artist": null, "audio_title": null, "audio_year": null
    },
    "media/photo_no_exif.jpg": {
      "file_name": "photo_no_exif", "extension": "jpg",
      "width": 800, "height": 600, "content_creation_date": null,
      "audio_artist": null, "audio_title": null, "audio_year": null
    },
    "media/image_800x600.png": {
      "file_name": "image_800x600", "extension": "png",
      "width": 800, "height": 600, "content_creation_date": null,
      "audio_artist": null, "audio_title": null, "audio_year": null
    },
    "media/song_with_tags.mp3": {
      "file_name": "song_with_tags", "extension": "mp3",
      "width": null, "height": null, "content_creation_date": null,
      "audio_artist": "TestArtist", "audio_title": "TestSong", "audio_year": 2020
    },
    "media/audio_no_tags.wav": {
      "file_name": "audio_no_tags", "extension": "wav",
      "width": null, "height": null, "content_creation_date": null,
      "audio_artist": null, "audio_title": null, "audio_year": null
    }
  }
}
```

---

## Cross-Cutting Concerns

### Filesystem Dates Are Unreliable After Copy

`Files.copy()` into `@TempDir` resets or preserves FS dates depending on OS and filesystem — behavior is undefined. Tests MUST NEVER assert specific `fs_creation_date` or `fs_modification_date` values from the manifest against copied files.

Strategy per `ADD_DATETIME` source:
- `CONTENT_CREATION_DATE`: assert exact date string derived from `manifest.json` (stable — EXIF is embedded in file bytes)
- `FILE_CREATION_DATE` / `FILE_MODIFICATION_DATE`: assert only `status == SUCCESS` + filename matches a date regex (e.g., `\\d{8}.*`)
- `CURRENT_DATE`: capture `LocalDate.now()` before `execute()`, assert filename contains that date formatted correctly
- `CUSTOM_DATE`: fully deterministic — assert exact expected string

### Case-Insensitive Filesystems (CHANGE_CASE)

macOS HFS+ and Windows NTFS are case-insensitive. Renaming `lowercase.txt` to `LOWERCASE.txt` is a no-op at the OS level on these systems. To write cross-platform tests:
- Use inputs whose transformation produces a structurally different name (e.g., `hello_world.txt` → `helloWorldTest.txt` for CAMEL_CASE — different characters, not just different case)
- For modes where input already matches the output case (e.g., SNAKE_CASE on a snake_case input), assert `SKIPPED` — this is expected and platform-agnostic

### NUMBER_FILES Ordering Determinism

- `FILE_NAME` sort: use files named `apple.txt`, `banana.txt`, `cherry.txt` — unambiguous lexicographic order on all platforms
- `FILE_CONTENT_CREATION_DATETIME` sort: null dates sort to `LocalDateTime.MIN` (come first); dated JPEG sorts after — the relative order is deterministic even if copy time changes
- `FILE_CREATION_DATE` / `FILE_MODIFICATION_DATE` sort: copy time is nearly simultaneous → order undefined → assert only count, not specific assignments
- `IMAGE_WIDTH` / `IMAGE_HEIGHT` sort: `image_800x600.png` (800 or 600) < `photo_1920x1080.jpg` (1920 or 1080) — deterministic

### NUMBER_FILES Transformer Internals

`SequenceTransformer.transform()` throws `UnsupportedOperationException` by design. The orchestrator always calls `transformBatch()` for `NUMBER_FILES` mode. Tests exercise this only via `orchestrator.execute()`, never the transformer directly.

### REPLACE_TEXT Literal Matching

`textToReplace="file.v2.0"` must be treated as a literal string, not a regex. Verify the transformer uses `String.replace()` not `replaceAll()`. Include a test with special-regex-char text to confirm.

---

## Task Dependency Order

```
Task 0  (generate test data + manifest.json)
    └── Task 1  (base infrastructure: GuiceTestHelper, TestManifest, BaseRealMetadataIntegrationTest, SmokeIntegrationTest)
            └── Tasks 2–11  (mode tests — independent of each other, require Task 1)
                    └── Task 12  (finalization — requires all mode tests complete)
```

Tasks 2–11 can be executed in any order after Task 1 completes.

---

## Task 0: Generate Test Data & Manifest

### Objective
Create `tools/generate_integration_test_data.py` that produces all test binary files with embedded metadata and writes `manifest.json`. Run once, commit output to git. CI never runs this script.

### Files to Create
- `tools/generate_integration_test_data.py` (new script)
- `app/backend/src/test/resources/integration-test-data/**` (generated + committed)

### Script Requirements
- **Run with `uv`:** `uv run tools/generate_integration_test_data.py`
- **Never use bare `python`** — UV handles dependencies via inline script metadata
- Script is idempotent: re-running produces identical output (no timestamps in content)
- Script prints verification summary at the end

### Generation Steps

```python
# Inline script metadata block at top of file:
# /// script
# dependencies = ["mutagen", "pillow"]
# ///

# 1. Generate JPEG 1920x1080 (blue solid)
#    ffmpeg -f lavfi -i color=c=blue:s=1920x1080:d=1 -frames:v 1 photo_1920x1080.jpg
# 2. Embed fixed EXIF date (NOT current time — must be stable)
#    exiftool -DateTimeOriginal="2025:06:15 08:22:15" -CreateDate="2025:06:15 08:22:15" -overwrite_original photo_1920x1080.jpg
# 3. Generate JPEG 800x600 (red solid)
#    ffmpeg -f lavfi -i color=c=red:s=800x600:d=1 -frames:v 1 photo_no_exif.jpg
# 4. Strip all EXIF from no-exif JPEG
#    exiftool -all= -overwrite_original photo_no_exif.jpg
# 5. Generate PNG 800x600 (green solid)
#    ffmpeg -f lavfi -i color=c=green:s=800x600:d=1 -frames:v 1 image_800x600.png
# 6. Generate MP3 1s sine wave
#    ffmpeg -f lavfi -i sine=frequency=440:duration=1 -c:a libmp3lame song_with_tags.mp3
# 7. Embed ID3 tags using mutagen (not exiftool — more reliable for MP3)
#    from mutagen.id3 import ID3, TIT2, TPE1, TDRC
# 8. Generate WAV 1s sine wave (no tags needed)
#    ffmpeg -f lavfi -i sine=frequency=440:duration=1 audio_no_tags.wav
# 9. Create flat/, nested/, multi_folder/ text files (plain Python)
# 10. Write manifest.json using json.dumps()
```

### Manifest Generation
The script writes `manifest.json` with the fixed values shown in the Test Data section above. Values are hard-coded constants in the script, not read back from the files (to avoid ExifTool re-run dependency).

### Verification Steps
```bash
uv run tools/generate_integration_test_data.py
exiftool app/backend/src/test/resources/integration-test-data/media/photo_1920x1080.jpg | grep DateTimeOriginal
# Expected: DateTimeOriginal: 2025:06:15 08:22:15
exiftool app/backend/src/test/resources/integration-test-data/media/photo_no_exif.jpg | grep -c Date
# Expected: 0
python3 -c "
from mutagen.id3 import ID3
tags = ID3('app/backend/src/test/resources/integration-test-data/media/song_with_tags.mp3')
print(tags['TPE1'])  # TestArtist
print(tags['TIT2'])  # TestSong
"
```

**Checklist:**
- [ ] All files in directory tree exist and are readable
- [ ] `photo_1920x1080.jpg` EXIF date confirmed with exiftool
- [ ] `photo_no_exif.jpg` has no date tags
- [ ] MP3 artist/title verified
- [ ] `manifest.json` is valid JSON
- [ ] Total test data size < 10 MB
- [ ] Files committed to git

---

## Task 1: Test Infrastructure

### Objective
Create the shared base class, Guice helper, manifest reader, and a smoke test that confirms the full stack initializes.

### Files to Create

| Path | Class | Purpose |
|------|-------|---------|
| `app/backend/src/test/java/ua/renamer/app/backend/integration/support/GuiceTestHelper.java` | `GuiceTestHelper` | Static singleton Guice injector |
| `app/backend/src/test/java/ua/renamer/app/backend/integration/support/ManifestEntry.java` | `ManifestEntry` | Jackson record for one file's manifest data |
| `app/backend/src/test/java/ua/renamer/app/backend/integration/support/TestManifest.java` | `TestManifest` | Loads and queries `manifest.json` |
| `app/backend/src/test/java/ua/renamer/app/backend/integration/BaseRealMetadataIntegrationTest.java` | `BaseRealMetadataIntegrationTest` | Abstract base class for all mode tests |
| `app/backend/src/test/java/ua/renamer/app/backend/integration/SmokeIntegrationTest.java` | `SmokeIntegrationTest` | Verifies the Guice injector and pipeline start correctly |

### GuiceTestHelper

```java
public final class GuiceTestHelper {
    private static volatile Injector injector;

    private GuiceTestHelper() {}

    public static synchronized Injector getInjector() {
        if (injector == null) {
            injector = Guice.createInjector(
                new DIBackendModule(),
                b -> b.bind(StatePublisher.class).toInstance(mock(StatePublisher.class))
            );
        }
        return injector;
    }

    public static FileRenameOrchestrator getOrchestrator() {
        return getInjector().getInstance(FileRenameOrchestrator.class);
    }
}
```

### ManifestEntry

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public record ManifestEntry(
    @JsonProperty("file_name")             String fileName,
    @JsonProperty("extension")             String extension,
    @JsonProperty("width")                 Integer width,
    @JsonProperty("height")                Integer height,
    @JsonProperty("content_creation_date") String contentCreationDate,  // ISO-8601 or null
    @JsonProperty("audio_artist")          String audioArtist,
    @JsonProperty("audio_title")           String audioTitle,
    @JsonProperty("audio_year")            Integer audioYear
) {}
```

### TestManifest

```java
public class TestManifest {
    private static TestManifest INSTANCE;
    private final Map<String, ManifestEntry> files;

    private TestManifest() {
        // Load from classpath: /integration-test-data/manifest.json
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getResourceAsStream("/integration-test-data/manifest.json");
        // parse "files" map into Map<String, ManifestEntry>
    }

    public static synchronized TestManifest load() { ... }

    /** Key is the relative path from integration-test-data root, e.g. "media/photo_1920x1080.jpg" */
    public ManifestEntry get(String relativePath) { ... }

    public Optional<LocalDateTime> getContentCreationDate(String relativePath) {
        // parse contentCreationDate string into LocalDateTime, or empty if null
    }
}
```

### BaseRealMetadataIntegrationTest

Annotated `@Tag("integration")` at the class level — all subclasses inherit the tag automatically; no per-class annotation needed.

```java
@Slf4j
@Tag("integration")
public abstract class BaseRealMetadataIntegrationTest {

    /** Explicit enumeration of all test resource paths (relative to integration-test-data root).
     *  Used by copyAndExpandFullTree() to reconstruct the full directory structure. */
    protected static final List<String> ALL_TEST_RESOURCES = List.of(
            "flat/document.txt", "flat/report_final.md", "flat/data_export.csv",
            "flat/config_dev.json", "flat/no_extension",
            "nested/level1_a.txt", "nested/level1_b.txt",
            "nested/sublevel/level2_a.txt", "nested/sublevel/deep/level3_a.txt",
            "multi_folder/folder_a/file_x.txt", "multi_folder/folder_a/file_y.txt",
            "multi_folder/folder_b/file_p.txt", "multi_folder/folder_b/file_q.txt",
            "media/photo_1920x1080.jpg", "media/photo_no_exif.jpg",
            "media/image_800x600.png", "media/song_with_tags.mp3", "media/audio_no_tags.wav"
    );

    @TempDir
    protected Path tempDir;  // per-method, JUnit creates a fresh directory per test

    protected static FileRenameOrchestrator orchestrator;
    protected static TestManifest manifest;
    protected static FolderExpansionService folderExpansionService;

    @BeforeAll
    static void setUpClass() {
        orchestrator = GuiceTestHelper.getOrchestrator();
        manifest = TestManifest.load();
        folderExpansionService = GuiceTestHelper.getInjector().getInstance(FolderExpansionService.class);
    }

    /** Copies into tempDir flat (filename only, no subdirectory structure). */
    protected File copyTestResource(String resourceRelativePath) throws IOException { ... }

    /** Copies into tempDir/{subDir}/{filename}. Creates subDir if absent. */
    protected File copyTestResourceTo(String resourceRelativePath, String subDir) throws IOException { ... }

    /**
     * Copies preserving full relative path structure.
     * E.g. "nested/sublevel/level2_a.txt" → tempDir/nested/sublevel/level2_a.txt
     */
    protected File copyTestResourcePreservingPath(String resourceRelativePath) throws IOException { ... }

    /**
     * Copies ALL_TEST_RESOURCES into tempDir (preserving structure) then expands via
     * FolderExpansionService(USE_CONTENTS, recursive=true, includeFoldersAsItems=false).
     *
     * Returns all files from every subdirectory — the working list for mode tests.
     *
     * includeFoldersAsItems=false intentional: batching parent dirs together with their
     * own children in a single rename pass produces unpredictable results because renaming
     * the parent moves all children, invalidating the captured child paths. Mode tests
     * assert "every file across all subdirectories is renamed"; they do not need to rename
     * the directories themselves.
     */
    protected List<File> copyAndExpandFullTree() throws IOException { ... }

    /** Creates a plain text file in tempDir with the given content. */
    protected File createPlainFile(String name, String content) throws IOException { ... }

    protected void assertFileExists(String name) { assertThat(tempDir.resolve(name)).exists(); }

    protected void assertFileNotExists(String name) { assertThat(tempDir.resolve(name)).doesNotExist(); }

    /**
     * Asserts the file was physically renamed on disk: old name is gone, new name exists.
     * Both paths resolved relative to tempDir. MUST be called after every SUCCESS result.
     */
    protected void assertRenamed(String oldName, String newName) {
        assertFileNotExists(oldName);
        assertFileExists(newName);
    }

    /**
     * Verifies disk state for every result in a batch:
     *   ERROR_* status   → assertion failure (no unexpected errors allowed)
     *   SUCCESS          → new path exists on disk; original path is gone
     *   SKIPPED          → original path still exists on disk
     *
     * Uses result.getPreparedFile().getNewPath() / getOldPath() for full-path resolution,
     * so it works for files in any subdirectory.
     */
    protected void assertDiskStateForBatch(List<RenameResult> results) { ... }

    protected ProgressCallback noOpCallback() { return (current, max) -> {}; }

    /** Runs pipeline on a single file; asserts result list has exactly 1 entry. */
    protected RenameResult executeSingle(File file, TransformationMode mode, Object config) { ... }
}
```

### SmokeIntegrationTest

Three tests:
1. `guiceInjector_startsWithoutException` — `GuiceTestHelper.getInjector()` is non-null
2. `pipeline_plainTextFile_addTextMode_renamesSuccessfully` — `smoke_test.txt` + ADD_TEXT `BEGIN "OK_"` → `SUCCESS`, `OK_smoke_test.txt` exists; **must call `assertRenamed("smoke_test.txt", "OK_smoke_test.txt")`**
3. `pipeline_realJpegFile_metadataExtraction_doesNotProduceError` — copy `media/photo_1920x1080.jpg`, run ADD_TEXT (metadata-neutral mode), assert `status != ERROR_EXTRACTION`

### Verification

```bash
cd app && mvn test -pl app/backend -Dtest=SmokeIntegrationTest -q -ff -Dai=true
```

---

## Task 1b: Folder Drop Integration Tests

**File:** `…/integration/FolderDropIntegrationTest.java`

**Purpose:** Covers user flow step 1 — "User drops a folder: verify that the correct files/folders are added to the working list based on the chosen drop option." This is the only task that directly tests `FolderExpansionService` expansion behaviour. All other mode tests assume the working list has already been built.

### Drop Option Scenarios

| Scenario | Dropped folder | Options | Expected working-list size | Notes |
|---|---|---|---|---|
| `nonRecursive_includeFolders` | `multi_folder/` | USE_CONTENTS, recursive=false, folders=true | 2 (folder_a, folder_b) | Direct children are both dirs; files-only would yield 0 |
| `recursive_filesOnly` | `nested/` | USE_CONTENTS, recursive=true, folders=false | 4 files | level1_a, level1_b, level2_a, level3_a |
| `recursive_includeFolders` | `nested/` | USE_CONTENTS, recursive=true, folders=true | 6 (4 files + 2 dirs) | sublevel/ and sublevel/deep/ |
| `useAsItem` | `multi_folder/` | (no expand() call — UI adds folder directly) | 1 (the folder itself) | `FolderExpansionService.expand()` is NOT called for USE_AS_ITEM |

### Test Structure (per scenario)

1. Copy relevant subtree into `@TempDir` using `copyTestResourcePreservingPath()`
2. Assert the expansion result (size + file/dir types)
3. Run `orchestrator.execute()` with ADD_TEXT to verify the items can be renamed
4. Call `assertDiskStateForBatch()` to verify disk state

### Important Constraint: Do Not Mix Parent Dirs and Their Children in the Same Batch

When `recursive+includeFolders=true` produces both a directory (`sublevel/`) and its child file (`sublevel/level2_a.txt`), renaming the directory first moves all its contents — the pipeline's stored child path then points to a non-existent location. For this scenario: assert the expansion count (6 items) and rename **only the file items** (`filesOnly = workingList.stream().filter(File::isFile).toList()`).

### Verification

```bash
cd app && mvn test -pl app/backend -Dtest=FolderDropIntegrationTest -q -ff -Dai=true
```

---

## Task 2: ADD_TEXT Mode Tests

**File:** `…/integration/AddTextIntegrationTest.java`

### Config Parameters
| Parameter | Type | Values |
|---|---|---|
| `textToAdd` | String | plain ASCII, unicode, empty |
| `position` | ItemPosition | `BEGIN`, `END` |

### Test Cases (~7)

| # | Position | Text | Input | Expected New Name | Disk assertion |
|---|---|---|---|---|---|
| 1 | `BEGIN` | `"PREFIX_"` | `document.txt` | `PREFIX_document.txt` | `assertRenamed("document.txt", "PREFIX_document.txt")` |
| 2 | `END` | `"_SUFFIX"` | `report_final.md` | `report_final_SUFFIX.md` | `assertRenamed("report_final.md", "report_final_SUFFIX.md")` |
| 3 | `BEGIN` | `"PRE_"` | `no_extension` | `PRE_no_extension` | `assertRenamed("no_extension", "PRE_no_extension")` |
| 4 | `BEGIN` | `""` | `document.txt` | `SKIPPED` (unchanged) | `assertFileExists("document.txt")` |
| 5 | `BEGIN` | `"日本語_"` | `document.txt` | `日本語_document.txt` | `assertRenamed("document.txt", "日本語_document.txt")` |
| 6 | `BEGIN` | `"batch_"` | 5 plain files | all 5 renamed, all `SUCCESS` | `assertRenamed("file_N.txt", "batch_file_N.txt")` for each |
| 7 | `BEGIN` | `"tree_"` | full tree via `copyAndExpandFullTree()` | all files renamed, zero `ERROR_*` | `assertDiskStateForBatch(results)` |

Test #7 is the **full user flow** test: all test resources copied with structure, expanded recursively (files only), renamed, verified on disk across all subdirectories.

### Verification
```bash
cd app && mvn test -pl app/backend -Dtest=AddTextIntegrationTest -q -ff -Dai=true
```

---

## Task 3: REMOVE_TEXT Mode Tests

**File:** `…/integration/RemoveTextIntegrationTest.java`

### Config Parameters
| Parameter | Type | Values |
|---|---|---|
| `textToRemove` | String | found/not-found/partial |
| `position` | ItemPosition | `BEGIN`, `END` |

### Test Cases (~5)
| # | Position | Text | Setup File | Expected |
|---|---|---|---|---|
| 1 | `BEGIN` | `"OLD_"` | `OLD_document.txt` | `document.txt` |
| 2 | `END` | `"_DRAFT"` | `report_DRAFT.md` | `report.md` |
| 3 | `BEGIN` | `"MISSING_"` | `NOTHERE_file.txt` | `SKIPPED` |
| 4 | `BEGIN` | `"OLD_"` | `OLD_OLD_file.txt` | `OLD_file.txt` (only leading occurrence) |
| 5 | `BEGIN` | `"ONLY_"` | `ONLY_.txt` | Error or `SKIPPED` (would produce empty name) |

Note: All setup files created with `createPlainFile()` in `@TempDir` — no copying needed.

**Disk assertions:** Every `SUCCESS` result must call `assertRenamed(old, new)`. Every `SKIPPED` result must call `assertFileExists(old)`.

**Required full-tree test (last test case):** Call `copyAndExpandFullTree()`, run REMOVE_TEXT with a text that appears in some filenames and not others, call `assertDiskStateForBatch(results)`. Verifies files across all subdirectories are processed correctly.

### Verification
```bash
cd app && mvn test -pl app/backend -Dtest=RemoveTextIntegrationTest -q -ff -Dai=true
```

---

## Task 4: REPLACE_TEXT Mode Tests

**File:** `…/integration/ReplaceTextIntegrationTest.java`

### Config Parameters
| Parameter | Type | Values |
|---|---|---|
| `textToReplace` | String | found/not-found/special-regex-chars |
| `replacementText` | String | non-empty/empty |
| `position` | ItemPositionExtended | `BEGIN`, `END`, `EVERYWHERE` |

### Test Cases (~6)
| # | Position | Replace | With | Input | Expected |
|---|---|---|---|---|---|
| 1 | `EVERYWHERE` | `"old"` | `"new"` | `old_name_old.txt` | `new_name_new.txt` |
| 2 | `BEGIN` | `"temp"` | `"final"` | `temp_temp_file.txt` | `final_temp_file.txt` |
| 3 | `END` | `"temp"` | `"final"` | `file_temp_temp.txt` | `file_temp_final.txt` |
| 4 | `EVERYWHERE` | `"_world"` | `""` | `hello_world.txt` | `hello.txt` |
| 5 | `EVERYWHERE` | `"MISSING"` | `"x"` | `document.txt` | `SKIPPED` |
| 6 | `EVERYWHERE` | `".v2"` | `"_v3"` | `file.v2.0.txt` | `file_v3.0.txt` (literal, not regex) |

**Disk assertions:** Every `SUCCESS` result must call `assertRenamed(old, new)`. Every `SKIPPED` result must call `assertFileExists(old)`.

**Required full-tree test (last test case):** `copyAndExpandFullTree()` + REPLACE_TEXT `EVERYWHERE "a"→"@"` (touches many filenames) + `assertDiskStateForBatch(results)`.

### Verification
```bash
cd app && mvn test -pl app/backend -Dtest=ReplaceTextIntegrationTest -q -ff -Dai=true
```

---

## Task 5: CHANGE_CASE Mode Tests

**File:** `…/integration/ChangeCaseIntegrationTest.java`

### Config Parameters
| Parameter | Type | Values |
|---|---|---|
| `caseOption` | TextCaseOptions | 8 values |
| `capitalizeFirstLetter` | boolean | true, false |

### Test Cases (~9)
| # | CaseOption | capitalizeFirst | Input | Expected Stem |
|---|---|---|---|---|
| 1 | `LOWERCASE` | false | `UPPER_CASE.txt` | `upper_case` |
| 2 | `UPPERCASE` | false | `lower_case.txt` | `LOWER_CASE` |
| 3 | `CAMEL_CASE` | false | `hello_world_test.txt` | `helloWorldTest` |
| 4 | `PASCAL_CASE` | false | `hello_world_test.txt` | `HelloWorldTest` |
| 5 | `SNAKE_CASE` | false | `already_snake.txt` | `SKIPPED` |
| 6 | `SNAKE_CASE_SCREAMING` | false | `hello_world.txt` | `HELLO_WORLD` |
| 7 | `KEBAB_CASE` | false | `hello_world_test.txt` | `hello-world-test` |
| 8 | `TITLE_CASE` | false | `hello world test.txt` | `Hello World Test` |
| 9 | `LOWERCASE` | true | `HELLO.txt` | `Hello` |

**Cross-platform safety:** Test cases 1–4, 6–9 all produce structurally different names (different characters, not just case), so they work on case-insensitive filesystems.

**Disk assertions:** Every `SUCCESS` result must call `assertRenamed(old, new)`. Every `SKIPPED` result must call `assertFileExists(old)`.

**Required full-tree test (last test case):** `copyAndExpandFullTree()` + CHANGE_CASE `UPPERCASE` (transforms all filenames) + `assertDiskStateForBatch(results)`.

### Verification
```bash
cd app && mvn test -pl app/backend -Dtest=ChangeCaseIntegrationTest -q -ff -Dai=true
```

---

## Task 6: ADD_DATETIME Mode Tests

**File:** `…/integration/AddDateTimeIntegrationTest.java`

### Config Parameters
| Parameter | Type | Key Values to Test |
|---|---|---|
| `source` | DateTimeSource | `CONTENT_CREATION_DATE`, `FILE_CREATION_DATE`, `FILE_MODIFICATION_DATE`, `CURRENT_DATE`, `CUSTOM_DATE` |
| `position` | ItemPositionWithReplacement | `BEGIN`, `END`, `REPLACE` |
| `dateFormat` | DateFormat | `YYYY_MM_DD_DASHED`, `YYYY_MM_DD_TOGETHER`, at least 3 representative formats |
| `timeFormat` | TimeFormat | `DO_NOT_USE_TIME`, `HH_MM_SS_24_DASHED`, `HH_MM_AM_PM_TOGETHER` |
| `separator` | String | `"_"`, `""`, `" "` |
| `useFallbackDateTime` | boolean | true, false |
| `useUppercaseForAmPm` | boolean | true, false |
| `applyToExtension` | boolean | true, false |
| `customDateTime` | LocalDateTime | `LocalDateTime.of(2024, 3, 15, 10, 30, 0)` |

### Test Cases (~12)

| # | Source | File | Fallback | Expected |
|---|---|---|---|---|
| 1 | `CONTENT_CREATION_DATE` | `photo_1920x1080.jpg` | false | `SUCCESS`; name contains `2025-06-15` (from manifest) |
| 2 | `CONTENT_CREATION_DATE` | `photo_no_exif.jpg` | false | `ERROR_TRANSFORMATION` |
| 3 | `CONTENT_CREATION_DATE` | `photo_no_exif.jpg` | true | `SUCCESS`; some date applied |
| 4 | `CUSTOM_DATE` | `document.txt` | — | `SUCCESS`; name contains `2024-03-15` exactly |
| 5 | `CURRENT_DATE` | `document.txt` | — | `SUCCESS`; name contains `LocalDate.now()` formatted |
| 6 | `FILE_CREATION_DATE` | `document.txt` | — | `SUCCESS`; regex `\\d{8}.*` or `\\d{4}-\\d{2}-\\d{2}.*` matches |
| 7 | `FILE_MODIFICATION_DATE` | `document.txt` | — | `SUCCESS`; date regex matches |
| 8 | `CONTENT_CREATION_DATE` | `photo_1920x1080.jpg` | — | position=`BEGIN`, sep=`_` → name starts with `2025-06-15_` |
| 9 | `CONTENT_CREATION_DATE` | `photo_1920x1080.jpg` | — | position=`END`, sep=`_` → name ends with `_2025-06-15.jpg` |
| 10 | `CUSTOM_DATE` | `document.txt` | — | position=`REPLACE` → name is exactly `2024-03-15` |
| 11 | `CUSTOM_DATE` + AM/PM time format | `document.txt` | — | `useUppercaseForAmPm=true` → name contains `AM` or `PM`, not `am`/`pm` |
| 12 | `CONTENT_CREATION_DATE` | `song_with_tags.mp3` | false | `ERROR_TRANSFORMATION` (MP3 has no `ImageMeta.contentCreationDate`) |
| 13 | `CUSTOM_DATE` | `document.txt` | — | `applyToExtension=true` → extension also transformed |

**Assertion pattern for test #1:**
```java
ManifestEntry entry = manifest.get("media/photo_1920x1080.jpg");
// entry.contentCreationDate() == "2025-06-15T08:22:15"
LocalDateTime expectedDate = LocalDateTime.parse(entry.contentCreationDate());
String expectedDatePart = expectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // "2025-06-15"
assertThat(result.getNewFileName()).startsWith(expectedDatePart + "_");
```

**Disk assertions:** Every `SUCCESS` result must call `assertRenamed(old, new)`. `ERROR_TRANSFORMATION` results do not touch the file — assert `assertFileExists(old)`.

**Required full-tree test (last test case):** `copyAndExpandFullTree()` + ADD_DATETIME `CUSTOM_DATE` (fully deterministic) + `assertDiskStateForBatch(results)`. Plain text files, MP3, and WAV will receive the custom date; JPEG files will also receive it (CUSTOM_DATE ignores metadata). Zero `ERROR_*` expected.

### Verification
```bash
cd app && mvn test -pl app/backend -Dtest=AddDateTimeIntegrationTest -q -ff -Dai=true
```

---

## Task 7: ADD_DIMENSIONS Mode Tests

**File:** `…/integration/AddDimensionsIntegrationTest.java`

This is a **critical new coverage area** — all existing integration tests return `null` for dimensions due to the mocked metadata mapper. These tests verify the real Tika/metadata-extractor pipeline reads actual pixel dimensions from JPEG and PNG files.

### Config Parameters
| Parameter | Type | Values |
|---|---|---|
| `leftSide` | ImageDimensionOptions | `DO_NOT_USE`, `WIDTH`, `HEIGHT` |
| `rightSide` | ImageDimensionOptions | `DO_NOT_USE`, `WIDTH`, `HEIGHT` |
| `separator` | String | `"x"`, `" × "` |
| `position` | ItemPositionWithReplacement | `BEGIN`, `END`, `REPLACE` |
| `nameSeparator` | String | `"_"`, `" "` |

### Test Cases (~8)

| # | Config | File | Expected New Name |
|---|---|---|---|
| 1 | LEFT=WIDTH, RIGHT=HEIGHT, sep=`x`, pos=`BEGIN`, nameSep=`_` | `photo_1920x1080.jpg` | `1920x1080_photo_1920x1080.jpg` |
| 2 | LEFT=WIDTH, RIGHT=HEIGHT, sep=`x`, pos=`END`, nameSep=`_` | `image_800x600.png` | `image_800x600_800x600.png` |
| 3 | LEFT=WIDTH, RIGHT=DO_NOT_USE, pos=`BEGIN`, nameSep=`_` | `photo_1920x1080.jpg` | `1920_photo_1920x1080.jpg` |
| 4 | LEFT=HEIGHT, RIGHT=WIDTH, sep=`x`, pos=`BEGIN`, nameSep=`_` | `photo_1920x1080.jpg` | `1080x1920_photo_1920x1080.jpg` |
| 5 | LEFT=WIDTH, RIGHT=HEIGHT, sep=`x`, pos=`REPLACE` | `photo_1920x1080.jpg` | `1920x1080.jpg` |
| 6 | sep=`" × "` | `photo_1920x1080.jpg` | name contains `1920 × 1080` |
| 7 | any valid config | `photo_no_exif.jpg` | `SUCCESS` with `800x600` — JPEG SOF header has dimensions even without EXIF |
| 8 | any valid config | `song_with_tags.mp3` | `ERROR_TRANSFORMATION` |
| 9 | any valid config | `document.txt` | `ERROR_TRANSFORMATION` |

**Assertion pattern:**
```java
ManifestEntry e = manifest.get("media/photo_1920x1080.jpg");
String expectedDim = e.width() + "x" + e.height(); // "1920x1080"
assertThat(result.getNewFileName()).startsWith(expectedDim + "_");
```

**Disk assertions:** Every `SUCCESS` result must call `assertRenamed(old, new)`. `ERROR_TRANSFORMATION` (non-image files) do not touch the file — assert `assertFileExists(old)`.

**Required full-tree test (last test case):** `copyAndExpandFullTree()` + ADD_DIMENSIONS `BEGIN, WIDTH+HEIGHT, sep="x"` + `assertDiskStateForBatch(results)`. Expect `SUCCESS` for JPEG/PNG files (dimensions in file bytes), `ERROR_TRANSFORMATION` for text/audio, zero `ERROR_EXTRACTION`.

### Verification
```bash
cd app && mvn test -pl app/backend -Dtest=AddDimensionsIntegrationTest -q -ff -Dai=true
```

---

## Task 8: NUMBER_FILES Mode Tests

**File:** `…/integration/NumberFilesIntegrationTest.java`

### Config Parameters
| Parameter | Type | Values |
|---|---|---|
| `sortSource` | SortSource | 8 values: `FILE_NAME`, `FILE_PATH`, `FILE_SIZE`, `FILE_CREATION_DATETIME`, `FILE_MODIFICATION_DATETIME`, `FILE_CONTENT_CREATION_DATETIME`, `IMAGE_WIDTH`, `IMAGE_HEIGHT` |
| `perFolderCounting` | boolean | true, false |
| `startNumber` | int | 1, 10 |
| `stepValue` | int | 1, 2 |
| `padding` | int | 0, 3, 4 |

### Test Cases (~10)

| # | SortSource | Files | Config | Expected |
|---|---|---|---|---|
| 1 | `FILE_NAME` | `apple.txt`, `banana.txt`, `cherry.txt` | start=1, step=1, padding=3 | `001.txt` (apple), `002.txt` (banana), `003.txt` (cherry) |
| 2 | `FILE_SIZE` | 3 files with 1/2/3 KB | start=1 | `001.txt` (smallest), `002.txt`, `003.txt` |
| 3 | `FILE_CONTENT_CREATION_DATETIME` | `photo_no_exif.jpg` + `photo_1920x1080.jpg` | start=1 | no-EXIF → `001.jpg` (MIN date); EXIF → `002.jpg` |
| 4 | `IMAGE_WIDTH` | `image_800x600.png` + `photo_1920x1080.jpg` | start=1 | PNG (800) → `001`; JPEG (1920) → `002` |
| 5 | `IMAGE_HEIGHT` | `image_800x600.png` + `photo_1920x1080.jpg` | start=1 | PNG (600) → `001`; JPEG (1080) → `002` |
| 6 | `FILE_CREATION_DATETIME` | 3 plain files | start=1 | only assert count=3, all `SUCCESS` |
| 7 | `FILE_NAME` + `perFolderCounting=true` | `folder_a/file_x.txt`, `folder_a/file_y.txt`, `folder_b/file_p.txt`, `folder_b/file_q.txt` | start=1 | each folder independently: `001.txt`, `002.txt` in each |
| 8 | `FILE_NAME` + `perFolderCounting=false` | same 4 files | start=1 | global `001.txt`–`004.txt` |
| 9 | `FILE_NAME` | 3 files | start=10, step=2 | `10.txt`, `12.txt`, `14.txt` |
| 10 | `FILE_NAME` | 3 files | start=1, padding=4 | `0001.txt`, `0002.txt`, `0003.txt` |

**Note for tests 3–5:** Copy test resources into `@TempDir` using `copyTestResource()` before running pipeline. The files retain their embedded metadata (EXIF, dimensions) since the metadata is in the file bytes.

**Disk assertions:** Every `SUCCESS` result must call `assertRenamed(old, new)`. Every `SKIPPED` result must call `assertFileExists(old)`.

**Required full-tree test (last test case):** `copyAndExpandFullTree()` + NUMBER_FILES `FILE_NAME` sort + `assertDiskStateForBatch(results)`. Verifies all files across all subdirectories get sequential numbers (global counter). Per-folder counting is tested separately in the focused tests above.

### Verification
```bash
cd app && mvn test -pl app/backend -Dtest=NumberFilesIntegrationTest -q -ff -Dai=true
```

---

## Task 9: ADD_FOLDER_NAME Mode Tests

**File:** `…/integration/AddFolderNameIntegrationTest.java`

### Config Parameters
| Parameter | Type | Values |
|---|---|---|
| `numberOfParentFolders` | int (≥1) | 1, 2 |
| `position` | ItemPosition | `BEGIN`, `END` |
| `separator` | String | `"_"`, `"-"`, `""`, `" "` |

**Important behavior:** `separator` appears BOTH between multiple parent-folder segments AND between the parent block and the filename. `numberOfParentFolders=2`, `separator="_"` on `grandparent/parent/file.txt` → `grandparent_parent_file`.

All tests create the directory structure inside `@TempDir` using `Files.createDirectories()` + `createPlainFile()`.

### Test Cases (~7)

| # | Parents | Position | Separator | File location | Expected new name |
|---|---|---|---|---|---|
| 1 | 1 | `BEGIN` | `"_"` | `tempDir/myFolder/file.txt` | `myFolder_file.txt` |
| 2 | 1 | `END` | `"-"` | `tempDir/myFolder/file.txt` | `file-myFolder.txt` |
| 3 | 2 | `BEGIN` | `"_"` | `tempDir/grand/parent/file.txt` | `grand_parent_file.txt` |
| 4 | 1 | `BEGIN` | `""` | `tempDir/folder/file.txt` | `folderfile.txt` |
| 5 | 1 | `BEGIN` | `"_"` | `tempDir/my folder/file.txt` | `my folder_file.txt` (spaces preserved) |
| 6 | 3 but only 2 levels deep | — | `"_"` | `tempDir/parent/file.txt` | check behavior: error or graceful fallback |
| 7 | 1 | `BEGIN` | `"_"` | `nested/sublevel/deep/level3_a.txt` (copied) | `deep_level3_a.txt` |

**Disk assertions:** Every `SUCCESS` result must call `assertRenamed(old, new)`. Every `SKIPPED` result must call `assertFileExists(old)`.

**Required full-tree test (last test case):** `copyAndExpandFullTree()` + ADD_FOLDER_NAME `1 parent, BEGIN, sep="_"` + `assertDiskStateForBatch(results)`. Every file gets its immediate parent folder name prepended; files in tempDir root (flat/) use the top-level dir name. Zero `ERROR_*` expected.

### Verification
```bash
cd app && mvn test -pl app/backend -Dtest=AddFolderNameIntegrationTest -q -ff -Dai=true
```

---

## Task 10: TRIM_NAME Mode Tests

**File:** `…/integration/TrimNameIntegrationTest.java`

### Config Parameters
| Parameter | Type | Values |
|---|---|---|
| `truncateOption` | TruncateOptions | `REMOVE_SYMBOLS_IN_BEGIN`, `REMOVE_SYMBOLS_FROM_END`, `TRUNCATE_EMPTY_SYMBOLS` |
| `numberOfSymbols` | int (≥0) | 0, partial, exact-name-length, over-name-length |

### Test Cases (~9)

| # | Option | Symbols | Input | Expected Stem |
|---|---|---|---|---|
| 1 | `REMOVE_SYMBOLS_IN_BEGIN` | 3 | `document.txt` (stem=`document`, 8 chars) | `ument` |
| 2 | `REMOVE_SYMBOLS_FROM_END` | 5 | `document.txt` | `doc` |
| 3 | `REMOVE_SYMBOLS_IN_BEGIN` | 8 | `document.txt` | empty name → error or `SKIPPED` |
| 4 | `REMOVE_SYMBOLS_FROM_END` | 10 | `abc.txt` | empty name → error |
| 5 | `TRUNCATE_EMPTY_SYMBOLS` | — | `  spaces  .txt` | `spaces` |
| 6 | `TRUNCATE_EMPTY_SYMBOLS` | — | `clean.txt` | `SKIPPED` (no whitespace) |
| 7 | `REMOVE_SYMBOLS_IN_BEGIN` | 0 | `document.txt` | `SKIPPED` |
| 8 | `REMOVE_SYMBOLS_FROM_END` | 3 | `README` (no extension) | `REA` |
| 9 | `REMOVE_SYMBOLS_FROM_END` | 5 | `photo_1920x1080.jpg` | `photo_1920x1.jpg` (extension unchanged) |

**Disk assertions:** Every `SUCCESS` result must call `assertRenamed(old, new)`. Every `SKIPPED` result must call `assertFileExists(old)`.

**Required full-tree test (last test case):** `copyAndExpandFullTree()` + TRIM_NAME `REMOVE_SYMBOLS_FROM_END, 2` + `assertDiskStateForBatch(results)`. Files with stems ≤ 2 chars may be SKIPPED or error; all others are SUCCESS. Zero `ERROR_*` expected for normal files.

### Verification
```bash
cd app && mvn test -pl app/backend -Dtest=TrimNameIntegrationTest -q -ff -Dai=true
```

---

## Task 11: CHANGE_EXTENSION Mode Tests

**File:** `…/integration/ChangeExtensionIntegrationTest.java`

### Config Parameters
| Parameter | Type | Values |
|---|---|---|
| `newExtension` | String (non-blank) | new ext / empty / same / with-leading-dot |

### Test Cases (~7)

| # | newExtension | Input | Expected |
|---|---|---|---|
| 1 | `"md"` | `document.txt` | `document.md` |
| 2 | `"png"` | `photo_1920x1080.jpg` | `photo_1920x1080.png` (`SUCCESS`; bytes still JPEG — valid test) |
| 3 | `"txt"` | `no_extension` | `no_extension.txt` (adds extension) |
| 4 | `""` | `document.txt` | `document` (removes extension) |
| 5 | `"txt"` | `document.txt` | `SKIPPED` (same extension) |
| 6 | `"json"` | 5 `.csv` files | all 5 → `.json`, all `SUCCESS` |
| 7 | `"TXT"` | `document.txt` | `document.TXT` (case preserved) |
| 8 | `".md"` | `document.txt` | verify normalization: `document.md` (not `document..md`) |

**Disk assertions:** Every `SUCCESS` result must call `assertRenamed(old, new)`. Every `SKIPPED` result (same extension) must call `assertFileExists(old)`.

**Required full-tree test (last test case):** `copyAndExpandFullTree()` + CHANGE_EXTENSION `"bak"` + `assertDiskStateForBatch(results)`. All files get `.bak` extension; files that already have `.bak` are SKIPPED. Zero `ERROR_*` expected.

### Verification
```bash
cd app && mvn test -pl app/backend -Dtest=ChangeExtensionIntegrationTest -q -ff -Dai=true
```

---

## Task 12: Finalization

### Objective
Create the suite runner, confirm the full integration suite passes, and run `ai-build.sh`.

Note: `@Tag("integration")` is already on `BaseRealMetadataIntegrationTest` — all subclasses inherit it automatically. No per-class annotation needed.

### Files to Create
- `app/backend/src/test/java/ua/renamer/app/backend/integration/IntegrationTestSuite.java` (useful for IDE runs)

### Suite Runner
```java
@Suite
@SelectPackages("ua.renamer.app.backend.integration")
@IncludeTags("integration")
class IntegrationTestSuite {}
```

### Run Commands
```bash
# All backend tests (unit + integration)
cd app && mvn test -pl app/backend -q -ff -Dai=true

# Integration tests only
cd app && mvn test -pl app/backend -Dgroups=integration -q -ff -Dai=true

# Unit tests only (skip integration)
cd app && mvn test -pl app/backend -DexcludedGroups=integration -q -ff -Dai=true

# Full pipeline (compile → lint → test)
cd app && ../scripts/ai-build.sh
```

### Final Checklist
- [ ] ~100 test methods pass across 12 test classes (10 mode tests + SmokeIntegrationTest + FolderDropIntegrationTest)
- [ ] Every SUCCESS result has a disk verification assertion — no test asserts only the result object
- [ ] Full-tree test in every mode class passes — zero ERROR_* across all subdirectory files
- [ ] Folder drop test: all 4 scenarios assert correct working-list size and disk state
- [ ] No `ERROR_EXTRACTION` on valid media files — Tika/metadata-extractor wired correctly
- [ ] `ADD_DIMENSIONS` returns `1920x1080` for `photo_1920x1080.jpg` (not `null`)
- [ ] `ADD_DATETIME` with `CONTENT_CREATION_DATE` returns `2025-06-15` for dated JPEG
- [ ] `NUMBER_FILES` with `IMAGE_WIDTH` sort: PNG (800) gets lower number than JPEG (1920)
- [ ] `ai-build.sh` passes clean — Checkstyle + PMD + SpotBugs + all tests

---

## Rules (Repeated for Each Task Executor)

- **Real files only.** No filesystem mocking.
- **JSON manifest is the reference.** Tests read `manifest.json` for expected metadata values. ExifTool is never run during tests.
- **`@TempDir` per test method.** Never share temp directories across tests.
- **Filesystem dates are unreliable after copy.** Never assert `fs_creation_date` or `fs_modification_date` from the manifest against a copied file.
- **Cross-platform paths.** Use `Path.of()` for path operations. Never concatenate `/` or `\\`.
- **V2 builder prefix.** All config classes use `.withFieldName()` — not `.fieldName()`.
- **Test names describe the scenario.** `shouldAddPrefixToBeginning`, not `test1`.
- **Parameterized tests for matrices.** Use `@ParameterizedTest` + `@MethodSource` for parameter combinations.
- **Assert the new name, not just "it changed."** Compute expected name from input + config.
- **Assert disk state after every rename.** `SUCCESS` → call `assertRenamed(old, new)` or `assertDiskStateForBatch()`. `SKIPPED`/`ERROR_*` → call `assertFileExists(old)`. Never assert only the result object.
- **Every mode test class must include a full-tree test.** Use `copyAndExpandFullTree()` (recursive, files only) and call `assertDiskStateForBatch(results)`. This validates all subdirectory files are correctly handled.
- **Do not batch parent directories with their own children.** When `includeFoldersAsItems=true` produces a dir and its children, rename only the files (`filter(File::isFile)`). Renaming a parent first moves all children, invalidating the pipeline's captured child paths.
- **Assert no pipeline errors** unless the test specifically tests an error case.
- **Use Guice DI.** `GuiceTestHelper.getOrchestrator()` — never manually instantiate services.
- **`uv run` for Python scripts.** All Python helpers: `uv run tools/script_name.py`. Never `python` or `pip install`.
- **Run `ai-build.sh` after completing each task.**

---

## When Tests Fail — Stop and Discuss

A failing test means one of two things:
1. **Test logic is wrong** — expected outcome doesn't match how the app actually works → fix the test
2. **Application has a bug** — test expectation is correct but app produces wrong results → this is a real discovery

**Never determine this alone.** Report the failure:
- Which test failed
- Expected vs actual
- Relevant input (file name, mode config, parameters)
- Your assessment: test bug or app bug, and why

Then wait for direction:
- "Fix the test" — adjust the expectation
- "This is a bug" — `@Disabled("Bug: [description]")` and continue
- "Let's investigate" — dig into app code together

**Never weaken a test assertion to make it pass. Never delete a failing test.**

---

## Skills Reference

| Task | Skills |
|---|---|
| Task 0 (test data) | `/use-exiftool-metadata`, `/use-ffmpeg-cli` |
| Tasks 1–11 (tests) | `/write-junit5-tests`, `/java-developer` |
| Understanding pipeline | `/gitnexus-exploring` |
| Impact analysis | `/gitnexus-impact-analysis` |
