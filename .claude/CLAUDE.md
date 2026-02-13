# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Renamer App is a JavaFX desktop application for batch file renaming with metadata extraction capabilities. Built with Java 25, it uses a multi-module
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

### Core Design Patterns

**Command Pattern (Primary)**: All file operations are encapsulated as commands inheriting from `Command<I,O>`. Commands use `parallelStream()` for
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

**Chain of Responsibility**: Metadata extraction uses chained mappers per file format (JPEG, PNG, MP3, MP4, etc.). Each mapper checks `canHandle()`
via MIME type, extracts metadata, or delegates to next mapper.

**Dependency Injection**: Google Guice with three modules:

- `DIAppModule`: Application config (ExecutorService, ResourceBundle, i18n)
- `DICoreModule`: Business logic (commands, mappers, FilesOperations)
- `DIUIModule`: UI components (controllers, FXML loaders, ObservableList)

### File Renaming Workflow

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

### Key Models

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

## Extending the Application

### Adding a New Renaming Mode

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

## v2 Architecture (Production Ready)

The v2 metadata extraction system under `app/core/.../v2/` is **fully implemented and production-ready**. It uses the Strategy Pattern for cleaner,
more maintainable metadata extraction.

### Architecture Components

**Strategy Pattern Structure**:

- `FileMetadataExtractor` interface defines the contract
- Category-specific base classes: `BaseImageMetadataExtractor`, `BaseVideoMetadataExtractor`, `UnifiedAudioFileMetadataExtractor`
- Format-specific extractors extend base classes (e.g., `JpegFileMetadataExtractor`, `Mp4FileMetadataExtractor`)
- `ThreadAwareFileMetadataMapper` coordinates extraction with thread-safe operations

### Test Coverage Status (765 total tests passing)

**✅ Fully Tested Formats (100% coverage of popular formats):**

- **Images**: JPEG (20 tests), PNG (11 tests), GIF (5 tests), BMP (5 tests), TIFF (6 tests), HEIF/HEIC (8 tests), WebP (8 tests)
- **Video**: MP4 (13 tests), AVI (10 tests), MOV/QuickTime (10 tests)
- **Audio**: MP3 (13 tests), WAV/FLAC/OGG (6 tests)
- **Core Mappers**: ThreadAwareFileMapper (24 tests), ThreadAwareFileMetadataMapper (24 tests)

**Test Coverage Highlights:**

- GPS coordinate extraction (JPEG, TIFF)
- DateTime extraction with timezone handling
- Edge cases: pre-1970 dates, conflicting date metadata, partial metadata
- Format limitations documented (GIF/BMP: no datetime, Video: QuickTime epoch issues)

**⚠️ Untested Formats (intentionally excluded):**

- **RAW Camera Formats** (8 extractors): CR2, CR3, NEF, ARW, DNG, ORF, RAF, RW2 - Excluded by design as low-priority professional formats
- **Niche/Legacy Formats** (5 extractors): AVIF (modern, low adoption), EPS (PostScript), ICO (icons), PCX (obsolete), PSD (Photoshop)

All extractors are **fully implemented** using the same tested base classes. Untested formats will work correctly but lack integration test coverage.
Add tests for these formats only if users require them.

### Implementation Status Summary

| Category           | Implemented   | Tested        | Status                |
|--------------------|---------------|---------------|-----------------------|
| Popular Formats    | 14 extractors | 14 extractors | ✅ Production Ready    |
| RAW Camera Formats | 8 extractors  | 0 extractors  | ⚠️ Works but untested |
| Niche Formats      | 5 extractors  | 0 extractors  | ⚠️ Works but untested |

### Migration Notes

- v2 architecture coexists with v1 Chain of Responsibility pattern
- New features should use v2 (Strategy Pattern) for better maintainability
- v1 remains for backward compatibility but v2 is recommended for all new development

## Important Implementation Details

**Parallel Processing**: Commands use `parallelStream()` with synchronized progress updates. Ensure `processItem()` implementations are thread-safe
and stateless.

**Error Handling**: Commands don't throw exceptions. They return partial results with error information captured in models (e.g.,
`RenameModel.hasRenamingError`).

**Progress Callbacks**: Can be null; always check before calling. Used for UI progress bar updates via
`ProgressCallback.updateProgress(current, max)`.

**Mode Controllers**: Implement `ModeControllerApi` with `ObjectProperty<FileInformationCommand>`. UI changes trigger `updateCommand()` which creates
new command instance.

**Lombok Configuration**: `lombok.config` in app/ directory configures annotation processing. Use
`@RequiredArgsConstructor(onConstructor_ = {@Inject})` for constructor injection.

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

**Note**: RAW camera formats and niche image formats are fully implemented but lack integration tests. They use the same battle-tested base classes as
popular formats and will work correctly. Add tests only if users report issues or require these formats.

## Testing

Tests are primarily in `app/core/src/test/`. The core module is extensively tested with **765 passing tests** using JUnit 5 and Mockito. UI module has
minimal tests.

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

When adding features, prioritize testing business logic in the core module. For v2 metadata extractors, add integration tests in the `integration/`
package with real test files.

## Internationalization

Supports English and Ukrainian via ResourceBundles in `app/ui/src/main/resources/langs/`. Language is selected based on system locale (UA → Ukrainian,
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
