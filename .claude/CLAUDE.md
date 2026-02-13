# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Renamer App is a JavaFX desktop application for batch file renaming with metadata extraction capabilities. Built with
Java 25, it uses a multi-module
Maven structure separating core business logic from UI presentation.

## Build & Development Commands

### Prerequisites

- Java 25+ (tested with Amazon Corretto 25)
- Maven 3.9.6+

### Essential Commands

```bash
# Clean build and install all modules
mvn clean install

# Run the application (from project root)
cd app/ui
mvn javafx:run

# Run all tests
mvn test

# Run tests for specific module
cd app/core && mvn test
cd app/ui && mvn test

# Run a single test class
mvn test -Dtest=ThreadAwareFileMapperIntegrationTest

# Run a single test method
mvn test -Dtest=ThreadAwareFileMapperIntegrationTest#testSpecificMethod

# Generate coverage report (if configured)
mvn clean test jacoco:report
```

### Module Structure

```
app/
├── pom.xml          # Parent POM (Java 25, Guice 7, JavaFX 25)
├── core/            # Business logic (no UI dependencies)
│   └── pom.xml
└── ui/              # JavaFX frontend (depends on core)
    └── pom.xml
```

## Architecture Overview

The project has **two coexisting architectures**:

- **V1 (Legacy)**: Command Pattern with FileInformation → RenameModel flow. Maintained for backward compatibility.
- **V2 (Production)**: Strategy Pattern + Pipeline Orchestration with FileModel → PreparedFileModel → RenameResult flow.
  Recommended for new features.

### Core Design Patterns (V1 - Legacy)

**Command Pattern (Primary)**: All file operations are encapsulated as commands inheriting from `Command<I,O>`. Commands
use `parallelStream()` for
performance and include synchronized progress tracking.

```
Command<I,O>
  └── ListProcessingCommand<I,O>
      ├── FileInformationCommand (modify FileInformation.newName)
      │   ├── AddTextPrepareInformationCommand
      │   ├── ReplaceTextPrepareInformationCommand
      │   ├── SequencePrepareInformationCommand
      │   └── ... (other preparation commands)
      └── MapFileToFileInformationCommand
      └── MapFileInformationToRenameModelCommand
      └── RenameCommand (executes Files.move)
```

**Chain of Responsibility**: Metadata extraction uses chained mappers per file format (JPEG, PNG, MP3, MP4, etc.). Each
mapper checks `canHandle()`
via MIME type, extracts metadata, or delegates to next mapper.

**Dependency Injection**: Google Guice with four modules:

- `DIAppModule`: Application config (ExecutorService, ResourceBundle, i18n)
- `DICoreModule`: V1 business logic (commands, mappers, FilesOperations)
- `DIV2ServiceModule`: V2 services (transformers, orchestrator, mappers)
- `DIUIModule`: UI components (controllers, FXML loaders, ObservableList)

### File Renaming Workflow (V1 - Legacy)

```
User selects files
    ↓
[1] MapFileToFileInformationCommand
    - Extracts: path, size, dates, extension via FilesOperations
    - FileToMetadataMapper chain extracts EXIF/ID3/etc.
    - Result: FileInformation with metadata
    ↓
[2] FileInformationCommand (user-selected mode)
    - Modifies FileInformation.newName/newExtension
    - FixEqualNamesCommand handles duplicates
    ↓
[3] MapFileInformationToRenameModelCommand
    - Determines if renaming needed (oldName != newName)
    - Result: RenameModel with execution metadata
    ↓
[4] RenameCommand
    - Calls FilesOperations.renameFile() → Files.move()
    - Sets RenameResult (SUCCESS/ERROR/NOT_NEEDED)
```

### Key Models (V1 - Legacy)

**FileInformation**: Central mutable model representing file attributes and metadata

- Basic: path, name, extension, size, filesystem dates
- Metadata: creation date, image dimensions, audio tags
- Mutable: `newName`, `newExtension` (set by preparation commands)

**RenameModel**: UI-ready wrapper with execution state

- Flags: `isNeedRename`, `isRenamed`, `hasRenamingError`
- Display: `oldName`, `newName`, `renameResult`

**FileInformationMetadata**: Extracted metadata container

- Images/Video: width, height, creation date from EXIF
- Audio: artist, album, song name, year from ID3

### V2 Models (Production Architecture)

**FileModel** - Input representation for v2 pipeline

- Basic attributes: file, name, extension, absolutePath, fileSize
- Filesystem dates: creationDate, modificationDate
- Detection: detectedMimeType, isFile flag
- Extracted metadata: FileMeta object (EXIF, ID3, dimensions, GPS)
- Immutable using Lombok @Value with @Builder
- Used as input to Phase 2 (Transformation)

**PreparedFileModel** - Transformation output

- originalFile: Reference to source FileModel
- newName, newExtension: Calculated target names
- hasError, errorMessage: Error state tracking
- transformationMeta: TransformationMetadata for audit trail
- Helper methods: needsRename(), getOldPath(), getNewPath()
- Used as input to Phase 2.5 (Duplicate Resolution) and Phase 3 (Execution)

**RenameResult** - Final execution outcome

- preparedFile: Reference to PreparedFileModel
- status: RenameStatus enum (SUCCESS, SKIPPED, ERROR_EXTRACTION, ERROR_RENAME)
- errorMessage: Error details if failed
- executedAt: Timestamp of execution
- Helper method: isSuccess()
- Returned by Phase 3 (Physical Rename)

**TransformationMetadata** - Audit trail

- appliedMode: TransformationMode that was applied
- appliedAt: Timestamp of transformation
- configurationUsed: Map<String, String> of config key-value pairs
- Enables tracking of what transformations were applied and when

**V2 vs V1 Model Comparison:**

- V1: FileInformation (mutable with newName/newExtension fields) → RenameModel (UI wrapper)
- V2: FileModel (immutable source) → PreparedFileModel (immutable transformation result) → RenameResult (execution
  outcome)
- V2 provides clearer separation of concerns and better immutability guarantees

### Threading Model

- **UI Thread**: JavaFX event handlers, TableView updates, progress bar binding
- **Background Thread**: Single-threaded ExecutorService (daemon) executes Tasks
- **Parallel Processing**: Commands use `parallelStream()` within background thread
- **Progress**: Synchronized callbacks update JavaFX progress bar

### Dependency Injection Patterns

**Constructor Injection with Lombok**:

```java

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MyService {
    private final Dependency1 dep1;
    private final Dependency2 dep2;
}
```

**Provider Methods for Complex Setup**:

```java

@Provides
@Singleton
FileToMetadataMapper provideFileToMetadataMapper(
        AviMapper aviMapper, BmpMapper bmpMapper, ...
) {
    // Manually chain: nullMapper → aviMapper → bmpMapper → ...
    return nullMapper;
}
```

**TypeLiterals for Generics**:

```java
TypeLiteral<DataMapper<File, FileInformation>> mapperLiteral
```

**V2 Service Module (DIV2ServiceModule)**:

- Binds all 10 transformers as singletons
- Provides transformer registry Map<TransformationMode, FileTransformationService<?>>
- Configures orchestration services (FileRenameOrchestrator, DuplicateNameResolver, RenameExecutionService)
- Wires mappers (ThreadAwareFileMapper, ThreadAwareFileMetadataMapper)

## Extending the Application

### Adding a New Transformation Mode (V2 - Recommended)

The v2 architecture makes adding transformation modes straightforward using the Strategy Pattern.

**1. Create Config Model** in `app/core/.../v2/model/config/`:

```java
@Value
@Builder(setterPrefix = "with")
public class MyModeConfig {
    String parameter1;
    int parameter2;
    ItemPosition position;
}
```

**2. Create Transformer** in `app/core/.../v2/service/transformation/`:

```java
@Slf4j
@RequiredArgsConstructor
public class MyModeTransformer implements FileTransformationService<MyModeConfig> {

    private final SomeDependency dependency;

    @Override
    public PreparedFileModel transform(FileModel file, MyModeConfig config) {
        // Implement transformation logic
        String newName = applyMyTransformation(file.getName(), config);

        return PreparedFileModel.builder()
            .withOriginalFile(file)
            .withNewName(newName)
            .withNewExtension(file.getExtension())
            .withHasError(false)
            .withTransformationMeta(createMetadata())
            .build();
    }

    @Override
    public boolean requiresSequentialExecution() {
        return false; // true only if order matters (like ADD_SEQUENCE)
    }

    @Override
    public List<PreparedFileModel> transformBatch(List<FileModel> files, MyModeConfig config) {
        // Implement if requiresSequentialExecution() returns true
        return files.stream()
            .map(file -> transform(file, config))
            .toList();
    }
}
```

**3. Add Mode to Enum** in `app/core/.../v2/model/TransformationMode.java`:

```java
public enum TransformationMode {
    // ... existing modes ...
    MY_NEW_MODE
}
```

**4. Register in DI Module** in `app/core/.../config/DIV2ServiceModule.java`:

```java
@Provides
@Singleton
public MyModeTransformer provideMyModeTransformer(SomeDependency dep) {
    return new MyModeTransformer(dep);
}

@Provides
@Singleton
public Map<TransformationMode, FileTransformationService<?>> provideTransformerRegistry(
    // ... existing transformers ...
    MyModeTransformer myModeTransformer
) {
    Map<TransformationMode, FileTransformationService<?>> registry = new HashMap<>();
    // ... existing registrations ...
    registry.put(TransformationMode.MY_NEW_MODE, myModeTransformer);
    return Collections.unmodifiableMap(registry);
}
```

**5. Add Tests**:

- Unit tests in `app/core/src/test/.../service/transformation/MyModeTransformerTest.java`
- Integration tests in `app/core/src/test/.../service/integration/MyModeTransformationIntegrationTest.java`

**6. UI Integration** (when ready):

- Create FXML view in `app/ui/src/main/resources/fxml/`
- Create controller implementing mode configuration
- Update MainViewController to map mode to UI

### Adding a New Renaming Mode (V1 - Legacy)

For backward compatibility, v1 command-based modes can still be added. Note that **v2 is recommended** for new features.

1. **Core**: Create command in `app/core/.../service/command/impl/preparation/`
   ```java
   public class MyModePrepareInformationCommand extends FileInformationCommand {
       @Override
       public FileInformation processItem(FileInformation item) {
           // Modify item.newName based on logic
           return item;
       }
   }
   ```

2. **Core**: Add enum to `AppModes` enum

3. **UI**: Create FXML view in `app/ui/src/main/resources/fxml/mode_my_mode.fxml`

4. **UI**: Create controller extending `ModeBaseController`
   ```java

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeMyModeController extends ModeBaseController {
@Override
public void updateCommand() {
// Build command with UI values
setCommand(MyModePrepareInformationCommand.builder()...build())
}
}

```

5. **UI**: Register in `DIUIModule`:
    - Add controller singleton binding
    - Add `@Provides` methods for FXMLLoader, Parent, ModeControllerApi
    - Add to ViewNames enum

6. **UI**: Update `MainViewControllerHelper` to map mode to view/controller

### Adding Metadata Support for New File Type (v2 Strategy Pattern)

1. **Create extractor** in `app/core/.../v2/mapper/strategy/format/`:
   ```java
   public class MyTypeFileMetadataExtractor extends BaseImageMetadataExtractor {
       public MyTypeFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
           super(dateTimeUtils);
       }

       @Override
       protected Class<? extends Directory> getBaseDirectoryClass() {
           return MyTypeDirectory.class; // From metadata-extractor library
       }

       @Override
       protected Integer getBaseWidthTag() {
           return MyTypeDirectory.TAG_IMAGE_WIDTH;
       }

       @Override
       protected Integer getBaseHeightTag() {
           return MyTypeDirectory.TAG_IMAGE_HEIGHT;
       }
   }
   ```

2. **Add integration tests** in `app/core/src/test/.../v2/mapper/integration/`:
   ```java
   @TestInstance(TestInstance.Lifecycle.PER_CLASS)
   class MyTypeMetadataExtractorIntegrationTest {
       private MyTypeFileMetadataExtractor extractor;

       @BeforeAll
       void setUp() {
           extractor = new MyTypeFileMetadataExtractor(new DateTimeConverter());
       }

       @Test
       void testExtract_BasicMetadata() {
           File testFile = getTestFile("test_mytype_sample.mytype");
           FileMeta result = extractor.extract(testFile, "image/mytype");
           // Add assertions for dimensions, datetime, etc.
       }
   }
   ```

3. **Generate test data** using FFmpeg and ExifTool:
   ```bash
   # Create test file
   ffmpeg -f lavfi -i color=c=blue:s=800x600 -frames:v 1 test_mytype.mytype

   # Add metadata
   exiftool -AllDates="2025:12:11 21:00:35" test_mytype.mytype
   ```

4. **Register in DI** (update `DICoreModule` or relevant module)

### Adding Metadata Support (v1 Chain of Responsibility - Legacy)

For backward compatibility, v1 mappers can still be added:

1. Create mapper extending `FileToMetadataMapper`
2. Register in `DICoreModule` chain via `provideFileToMetadataMapper()`

**Recommendation**: Use v2 Strategy Pattern for new features - it's cleaner and better tested.

## v2 Architecture Overview

The v2 architecture consists of two layers:

- **Metadata Extraction Layer** (Strategy Pattern) - Documented below
- **Services/Transformation Layer** (Pipeline Orchestration) - See "V2 Services Layer" section

### v2 Metadata Extraction (Production Ready)

The v2 metadata extraction system under `app/core/.../v2/mapper/` is **fully implemented and production-ready**. It uses
the Strategy Pattern for cleaner,
more maintainable metadata extraction.

#### Architecture Components

**Strategy Pattern Structure**:

- `FileMetadataExtractor` interface defines the contract
- Category-specific base classes: `BaseImageMetadataExtractor`, `BaseVideoMetadataExtractor`,
  `UnifiedAudioFileMetadataExtractor`
- Format-specific extractors extend base classes (e.g., `JpegFileMetadataExtractor`, `Mp4FileMetadataExtractor`)
- `ThreadAwareFileMetadataMapper` coordinates extraction with thread-safe operations

#### Test Coverage Status (1,236+ total tests passing, 58 test files)

**✅ Metadata Extraction Tests (13 integration test classes):**

- **Images**: JpegMetadataExtractorIntegrationTest (20 tests), PngMetadataExtractorIntegrationTest (11 tests),
  GifMetadataExtractorIntegrationTest (5 tests), BmpMetadataExtractorIntegrationTest (5 tests),
  TiffMetadataExtractorIntegrationTest (6 tests), HeifMetadataExtractorIntegrationTest (8 tests),
  WebPMetadataExtractorIntegrationTest (8 tests)
- **Video**: Mp4MetadataExtractorIntegrationTest (13 tests), AviMetadataExtractorIntegrationTest (10 tests),
  MovMetadataExtractorIntegrationTest (10 tests)
- **Audio**: Mp3MetadataExtractorIntegrationTest (13 tests), AdditionalAudioFormatsIntegrationTest (6 tests)
- **Core Mappers**: ThreadAwareFileMapperIntegrationTest (24 tests), ThreadAwareFileMetadataMapperIntegrationTest (24
  tests)

**✅ V2 Services Tests (13 classes):**

- **Orchestration**: FileRenameOrchestratorImplTest (22 tests), DuplicateNameResolverImplTest (24 tests),
  RenameExecutionServiceImplTest (23 tests)
- **Transformers** (10 unit test classes): AddTextTransformerTest (22 tests), RemoveTextTransformerTest (32 tests),
  ReplaceTextTransformerTest (44 tests), CaseChangeTransformerTest (31 tests), DateTimeTransformerTest (32 tests),
  ExtensionChangeTransformerTest (28 tests), ImageDimensionsTransformerTest (30 tests), ParentFolderTransformerTest (28
  tests), SequenceTransformerTest (27 tests), TruncateTransformerTest (32 tests)

**✅ V2 Integration Tests (7 classes):**

- FullPipelineIntegrationTest (22 tests) - End-to-end workflow validation
- DuplicateResolutionIntegrationTest (13 tests)
- AddTextTransformationIntegrationTest (16 tests)
- SequenceTransformationIntegrationTest (20 tests)
- ErrorHandlingIntegrationTest (13 tests)
- PerformanceIntegrationTest (10 tests) - Performance benchmarking
- BaseTransformationIntegrationTest (base class)

**✅ V2 Utility Tests (2 classes):**

- CommonFileUtilsTest (27 tests)
- DateTimeConverterTest (114 tests) - Comprehensive datetime parsing and formatting

**✅ V1 Legacy Tests (~200 tests):**

- Command tests, file operations, helpers, utilities
- Maintained for backward compatibility

**Test Coverage Highlights:**

- GPS coordinate extraction (JPEG, TIFF)
- DateTime extraction with timezone handling
- Edge cases: pre-1970 dates, conflicting date metadata, partial metadata
- Format limitations documented (GIF/BMP: no datetime, Video: QuickTime epoch issues)
- Full pipeline integration testing with error handling and performance benchmarks

**⚠️ Untested Formats (intentionally excluded):**

- **RAW Camera Formats** (8 extractors): CR2, CR3, NEF, ARW, DNG, ORF, RAF, RW2 - Excluded by design as low-priority
  professional formats
- **Niche/Legacy Formats** (5 extractors): AVIF (modern, low adoption), EPS (PostScript), ICO (icons), PCX (obsolete),
  PSD (Photoshop)

All extractors are **fully implemented** using the same tested base classes. Untested formats will work correctly but
lack integration test coverage.
Add tests for these formats only if users require them.

#### Implementation Status Summary

| Category           | Implemented   | Tested        | Status                |
|--------------------|---------------|---------------|-----------------------|
| Popular Formats    | 14 extractors | 14 extractors | ✅ Production Ready    |
| RAW Camera Formats | 8 extractors  | 0 extractors  | ⚠️ Works but untested |
| Niche Formats      | 5 extractors  | 0 extractors  | ⚠️ Works but untested |

#### Migration Notes

- v2 architecture coexists with v1 Chain of Responsibility pattern
- New features should use v2 (Strategy Pattern) for better maintainability
- v1 remains for backward compatibility but v2 is recommended for all new development

### V2 Services Layer - Transformation Pipeline

The v2 services layer implements a **4-phase transformation pipeline** that orchestrates the complete file renaming
workflow from file selection to physical rename execution.

#### Architecture Overview

**FileRenameOrchestratorImpl** - Main conductor coordinating the entire pipeline:

- Uses virtual threads (Executors.newVirtualThreadPerTaskExecutor()) for scalable I/O operations
- Implements 4 distinct phases with conditional parallelization
- Provides synchronous and asynchronous execution modes
- Graceful error handling with partial result recovery

#### 4-Phase Pipeline

**Phase 1: Metadata Extraction (Always Parallel)**

- Uses ThreadAwareFileMapper to extract file metadata
- Processes files in parallel using virtual threads
- Extracts: basic attributes (size, dates), MIME type, format-specific metadata (EXIF, ID3, etc.)
- Output: List<FileModel> with complete metadata

**Phase 2: Transformation (Conditional Parallel/Sequential)**

- Applies selected TransformationMode using registered transformer
- Most modes run in parallel, SEQUENCE mode runs sequentially
- Uses FileTransformationService<ConfigType> interface
- Output: List<PreparedFileModel> with calculated new names

**Phase 2.5: Duplicate Resolution (Always Sequential)**

- DuplicateNameResolverImpl handles name collisions
- Appends incremental numbers to duplicates (e.g., "file_1.txt", "file_2.txt")
- Ensures no naming conflicts before execution
- Output: List<PreparedFileModel> with unique names

**Phase 3: Physical Rename (Always Parallel)**

- RenameExecutionServiceImpl executes Files.move() operations
- Processes renames in parallel for performance
- Tracks success/error status per file
- Output: List<RenameResult> with execution outcomes

#### Transformation Modes

The system supports 10 transformation modes via the TransformationMode enum:

| Mode                   | Description                | Config Class          | Example                      |
|------------------------|----------------------------|-----------------------|------------------------------|
| ADD_TEXT               | Prepend/append text        | AddTextConfig         | "prefix_" + name             |
| REMOVE_TEXT            | Remove from start/end      | RemoveTextConfig      | Remove first 5 chars         |
| REPLACE_TEXT           | Find and replace           | ReplaceTextConfig     | Replace "old" with "new"     |
| CHANGE_CASE            | Apply case transformations | CaseChangeConfig      | camelCase, snake_case, UPPER |
| USE_DATETIME           | Embed date/time            | DateTimeConfig        | "2025-12-11_file.jpg"        |
| USE_IMAGE_DIMENSIONS   | Add dimensions             | ImageDimensionsConfig | "photo_1920x1080.jpg"        |
| ADD_SEQUENCE           | Sequential numbering       | SequenceConfig        | "001_file.txt"               |
| USE_PARENT_FOLDER_NAME | Include parent folder      | ParentFolderConfig    | "folder_file.txt"            |
| TRUNCATE_FILE_NAME     | Remove characters          | TruncateConfig        | Truncate to 50 chars         |
| CHANGE_EXTENSION       | Modify extension           | ExtensionChangeConfig | ".jpg" → ".jpeg"             |

Each mode has a corresponding:

- **Transformer** class implementing FileTransformationService<ConfigType>
- **Config** model class defining mode-specific parameters
- **Test** class with 20-40+ unit tests
- **Integration tests** validating end-to-end behavior

## Important Implementation Details

**Parallel Processing**: Commands use `parallelStream()` with synchronized progress updates. Ensure `processItem()`
implementations are thread-safe
and stateless.

**Error Handling**: Commands don't throw exceptions. They return partial results with error information captured in
models (e.g.,
`RenameModel.hasRenamingError`).

**Progress Callbacks**: Can be null; always check before calling. Used for UI progress bar updates via
`ProgressCallback.updateProgress(current, max)`.

**Mode Controllers**: Implement `ModeControllerApi` with `ObjectProperty<FileInformationCommand>`. UI changes trigger
`updateCommand()` which creates
new command instance.

**Lombok Configuration**: `lombok.config` in app/ directory configures annotation processing. Use
`@RequiredArgsConstructor(onConstructor_ = {@Inject})` for constructor injection.

**V2 Pipeline Threading**: FileRenameOrchestratorImpl uses virtual threads for scalable I/O operations. Phase 1 (
metadata extraction) and Phase 3 (rename execution) always run in parallel. Phase 2 (transformation) runs in parallel
except for modes requiring sequential processing (e.g., ADD_SEQUENCE).

**Immutable Models**: All v2 models (FileModel, PreparedFileModel, RenameResult) are immutable using Lombok @Value. This
ensures thread safety and prevents accidental mutations during parallel processing.

**Error Recovery**: V2 pipeline doesn't throw exceptions from the main flow. Errors are captured in model fields (
hasError, errorMessage, RenameStatus) and returned as part of results. This allows partial success handling.

**Progress Tracking**: All phases support ProgressCallback with synchronized updates. AtomicInteger ensures thread-safe
progress counting across parallel operations.

**Transformer Registry**: DIV2ServiceModule provides a Map<TransformationMode, FileTransformationService<?>> registry.
The orchestrator looks up transformers at runtime, making the system easily extensible.

## Supported File Types

### Metadata Extraction Support

The application supports metadata extraction for **27 file format extractors** across three categories:

**Images (19 formats):**

- **Popular** (fully tested): JPEG, PNG, GIF, BMP, TIFF, HEIF/HEIC, WebP
- **RAW Camera** (implemented, untested): CR2, CR3, NEF, ARW, DNG, ORF, RAF, RW2
- **Niche/Legacy** (implemented, untested): AVIF, EPS, ICO, PCX, PSD

**Video (3 formats, all tested):**

- MP4/M4V/M4A/M4B/M4P/M4R, AVI, MOV/QuickTime

**Audio (5 formats via unified extractor, tested):**

- MP3, WAV, FLAC, OGG, M4A

### Extracted Metadata

- **Images**: EXIF data (datetime, GPS coordinates), dimensions (width/height), camera settings
- **Video**: Dimensions, datetime (with QuickTime epoch limitations documented), duration
- **Audio**: ID3 tags (artist, album, song name, year, genre)

**Note**: RAW camera formats and niche image formats are fully implemented but lack integration tests. They use the same
battle-tested base classes as
popular formats and will work correctly. Add tests only if users report issues or require these formats.

## Testing

Tests are primarily in `app/core/src/test/`. The core module is extensively tested with **1,236+ passing tests across 58
test files** using JUnit 5 and Mockito. UI module has minimal tests.

### Test Organization

**Integration Tests** (`app/core/src/test/java/.../v2/mapper/integration/`):

- Format-specific extractors: JPEG, PNG, GIF, BMP, TIFF, HEIF, WebP, MP4, AVI, MOV, MP3, WAV/FLAC/OGG
- Core mappers: ThreadAwareFileMapper, ThreadAwareFileMetadataMapper
- Test data: Real file samples in `app/core/src/test/resources/test-data/`

**Unit Tests** (`app/core/src/test/java/.../service/`, `.../util/`):

- Commands: File operations, preparation commands, rename logic
- Utilities: String manipulation, date/time parsing, file operations
- Helpers: Business logic helpers, calculations

### Test Data Generation

Integration tests use real files generated with:

- **Images**: FFmpeg (base images) + ExifTool (metadata embedding)
- **Video/Audio**: FFmpeg with explicit metadata flags

When adding features, prioritize testing business logic in the core module. For v2 metadata extractors, add integration
tests in the `integration/`
package with real test files.

## Internationalization

Supports English and Ukrainian via ResourceBundles in `app/ui/src/main/resources/langs/`. Language is selected based on
system locale (UA → Ukrainian,
others → English). Text retrieval uses `LanguageTextRetrieverApi`.

## MCP (Model Context Protocol) Server

This project uses the **py-search-helper** MCP server for web search and content extraction.

**When to use:**

- Looking up API documentation for libraries (Apache Tika, Metadata Extractor, JavaFX, Guice, Mockito, etc.)
- Searching for Java best practices or design patterns
- Finding information about technologies not in Claude's knowledge base
- Getting up-to-date information about library versions or features

**Available tools:**

- `get_engines()` - List available search engines (DuckDuckGo, Wikipedia, PySide6 docs)
- `search_web(engine, query, max_results)` - Search using specific engine
- `search_web_ddg(query, max_results)` - Shortcut for DuckDuckGo search
- `open_page(url, max_chars)` - Extract content from URL as Markdown
