# Renamer App - Technical Documentation & Architecture

## Table of Contents
1. [System Overview](#1-system-overview)
2. [Technology Stack](#2-technology-stack)
3. [Project Structure](#3-project-structure)
4. [Architecture Overview](#4-architecture-overview)
5. [Core Domain Models](#5-core-domain-models)
6. [Design Patterns](#6-design-patterns)
7. [Component Details](#7-component-details)
8. [Application Flows](#8-application-flows)
9. [Threading Model](#9-threading-model)
10. [Dependency Injection](#10-dependency-injection)
11. [Extending the Application](#11-extending-the-application)

---

## 1. System Overview

**Renamer App** is a desktop application designed for batch renaming of files. It provides various modes for renaming, including:
- Adding/removing custom text
- Changing text case
- Using metadata (EXIF, creation dates, image dimensions)
- Sequencing files
- Truncating filenames
- Changing file extensions

The application is built using **Java 25** with **JavaFX** for the UI, following clean architecture principles with strict separation between the Core business logic and UI presentation layer.

---

## 2. Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | 25 (Preview features enabled) |
| **UI Framework** | JavaFX | 25.0.1 |
| **Dependency Injection** | Google Guice | 7.0.0 |
| **Build Tool** | Maven | Multi-module project |
| **Boilerplate Reduction** | Lombok | 1.18.42 |
| **Logging** | SLF4J + Logback | 2.0.17 / 1.5.21 |
| **File Analysis** | Apache Tika | 3.2.3 |
| **Metadata Extraction** | Metadata Extractor | 2.19.0 |
| **Testing** | JUnit 5 + Mockito | 6.0.1 / 5.20.0 |

---

## 3. Project Structure

### 3.1. Module Organization

```
renamer_app/
├── app/
│   ├── pom.xml                    # Parent POM (dependency management)
│   ├── lombok.config              # Lombok configuration
│   ├── core/                      # Core business logic module
│   │   ├── pom.xml
│   │   └── src/main/java/
│   │       └── ua/renamer/app/core/
│   │           ├── enums/         # Application enums
│   │           ├── model/         # Domain models
│   │           └── service/       # Business logic services
│   │               ├── command/   # Command pattern implementations
│   │               ├── file/      # File operations
│   │               ├── helper/    # Helper utilities
│   │               ├── mapper/    # Data mappers
│   │               └── validator/ # Validation logic
│   └── ui/                        # UI module
│       ├── pom.xml
│       └── src/main/
│           ├── java/
│           │   └── ua/renamer/app/
│           │       ├── Launcher.java          # Entry point
│           │       ├── RenamerApplication.java # JavaFX Application
│           │       └── ui/
│           │           ├── config/            # DI configuration
│           │           ├── controller/        # FXML controllers
│           │           ├── converter/         # String converters
│           │           ├── enums/             # UI enums
│           │           ├── models/            # UI models
│           │           ├── service/           # UI services
│           │           └── widget/            # Custom widgets
│           └── resources/
│               ├── fxml/                     # FXML view files
│               ├── images/                   # Application icons
│               └── langs/                    # i18n resource bundles
├── package.json                             # jDeploy config
├── icon.png, icon.ico, icon.icns           # Platform icons
└── docs/                                    # Documentation
```

### 3.2. Module Dependencies

```mermaid
graph TD
    A[ui module] -->|depends on| B[core module]
    A -->|uses| C[JavaFX 25.0.1]
    B -->|uses| D[Apache Tika]
    B -->|uses| E[Metadata Extractor]
    A -->|uses| F[Google Guice]
    B -->|uses| F
    A -->|packaged with| G[jDeploy]
```

**Core Module**: Pure business logic, no UI dependencies.
**UI Module**: Depends on Core, contains all UI-related code and JavaFX controllers.

---

## 4. Architecture Overview

### 4.1. High-Level Architecture Diagram

```mermaid
graph TB
    subgraph "UI Layer"
        A[Launcher] --> B[RenamerApplication]
        B --> C[ApplicationMainViewController]
        C --> D[Mode Controllers]
        D --> E[FXML Views]
    end

    subgraph "Service Layer"
        F[CoreFunctionalityHelper]
        G[ViewLoaderService]
        H[MainViewControllerHelper]
    end

    subgraph "Core Domain Layer"
        I[Commands]
        J[Mappers]
        K[File Operations]
        L[Validators]
    end

    subgraph "DI Container"
        M[DIAppModule]
        N[DICoreModule]
        O[DIUIModule]
    end

    C --> F
    C --> H
    F --> I
    F --> J
    I --> K
    J --> K

    M -.provides.-> G
    N -.provides.-> I
    N -.provides.-> J
    O -.provides.-> C
    O -.provides.-> D
```

### 4.2. Layer Responsibilities

| Layer | Responsibility | Key Components |
|-------|----------------|----------------|
| **UI Layer** | User interaction, view rendering | Controllers, FXML, Widgets |
| **Service Layer** | Bridge between UI and Core | CoreFunctionalityHelper, ViewLoader |
| **Core Domain** | Business logic, file processing | Commands, Mappers, FilesOperations |
| **DI Container** | Dependency management | Guice Modules |

---

## 5. Core Domain Models

### 5.1. Data Model Hierarchy

```mermaid
classDiagram
    class FileInformation {
        -File originalFile
        -String fileAbsolutePath
        -boolean isFile
        -String fileName
        -String fileExtension
        -Set~String~ detectedExtension
        -String detectedMimeType
        -long fileSize
        -LocalDateTime fsCreationDate
        -LocalDateTime fsModificationDate
        -FileInformationMetadata metadata
        -String newName
        -String newExtension
        +getFsCreationDate() Optional~LocalDateTime~
        +getFsModificationDate() Optional~LocalDateTime~
        +getMetadata() Optional~FileInformationMetadata~
    }

    class FileInformationMetadata {
        -LocalDateTime creationDate
        -Integer imgVidWidth
        -Integer imgVidHeight
        -String audioArtistName
        -String audioAlbumName
        -String audioSongName
        -Integer audioYear
        +getCreationDate() Optional~LocalDateTime~
        +getImgVidWidth() Optional~Integer~
        +getImgVidHeight() Optional~Integer~
    }

    class RenameModel {
        -FileInformation fileInformation
        -boolean isNeedRename
        -String oldName
        -String newName
        -String absolutePathWithoutName
        -boolean hasRenamingError
        -String renamingErrorMessage
        -boolean isRenamed
        -RenameResult renameResult
    }

    FileInformation "1" *-- "0..1" FileInformationMetadata
    RenameModel "1" *-- "1" FileInformation
```

### 5.2. Model Descriptions

#### **FileInformation**
Represents comprehensive information about a file or directory:
- **File system attributes**: path, name, extension, size
- **Metadata**: Optional embedded metadata (EXIF, ID3, etc.)
- **Detection results**: MIME type, detected extensions
- **Mutable fields**: `newName`, `newExtension` (set during preparation phase)

#### **FileInformationMetadata**
Contains extracted metadata from various file types:
- **Image/Video metadata**: dimensions (width, height), creation date
- **Audio metadata**: artist, album, song name, year
- All fields are Optional to handle missing metadata gracefully

#### **RenameModel**
UI-friendly wrapper around `FileInformation`:
- **State tracking**: `isNeedRename`, `isRenamed`, `hasRenamingError`
- **Display data**: `oldName`, `newName` (used by TableView)
- **Status information**: `renameResult`, `renamingErrorMessage`

---

## 6. Design Patterns

### 6.1. Command Pattern

The **Command Pattern** is central to the renaming logic. Each operation (mapping, preparation, renaming) is encapsulated in a command.

```mermaid
classDiagram
    class Command~I,O~ {
        <<interface>>
        +execute(List~I~ input, ProgressCallback callback) List~O~
    }

    class ListProcessingCommand~I,O~ {
        <<abstract>>
        +execute(List~I~ input, ProgressCallback callback) List~O~
        #preprocessInput(List~I~ input) List~I~
        #processItem(I item) O*
        #updateProgress(int current, int max, ProgressCallback)
    }

    class FileInformationCommand {
        <<abstract>>
        +processItem(FileInformation) FileInformation
    }

    class AddTextPrepareInformationCommand {
        -ItemPosition position
        -String text
        +processItem(FileInformation) FileInformation
    }

    class RenameCommand {
        -FilesOperations filesOperations
        +processItem(RenameModel) RenameModel
    }

    class MapFileToFileInformationCommand {
        +processItem(File) FileInformation
    }

    Command <|.. ListProcessingCommand
    ListProcessingCommand <|-- FileInformationCommand
    FileInformationCommand <|-- AddTextPrepareInformationCommand
    ListProcessingCommand <|-- RenameCommand
    ListProcessingCommand <|-- MapFileToFileInformationCommand
```

**Key Features:**
- **Parallel Processing**: `ListProcessingCommand` uses `parallelStream()` for performance
- **Progress Tracking**: Updates UI progress bar via `ProgressCallback`
- **Preprocessing Hook**: `preprocessInput()` allows batch operations (e.g., `FixEqualNamesCommand`)

### 6.2. Chain of Responsibility Pattern

Used for **metadata extraction** from different file formats:

```mermaid
graph LR
    A[FileToMetadataMapper Chain] --> B[NullMapper]
    B --> C[AviMapper]
    C --> D[BmpMapper]
    D --> E[EpsMapper]
    E --> F[GifMapper]
    F --> G[HeifMapper]
    G --> H[IcoMapper]
    H --> I[JpegMapper]
    I --> J[Mp3Mapper]
    J --> K[Mp4Mapper]
    K --> L[PcxMapper]
    L --> M[PngMapper]
    M --> N[PsdMapper]
    N --> O[QuickTimeMapper]
    O --> P[TiffMapper]
    P --> Q[WavMapper]
    Q --> R[WebPMapper]
    R --> S[LastReserveMapper]
```

**How it works:**
1. Each mapper checks if the file matches its supported extensions
2. If yes, it extracts metadata and returns `FileInformationMetadata`
3. If no or extraction fails, it delegates to the next mapper in the chain
4. `LastReserveMapper` is a fallback that tries all mappers sequentially

### 6.3. Model-View-Controller (MVC)

JavaFX's FXML-based MVC pattern:

```mermaid
graph TB
    A[FXML View] -->|binds to| B[Controller]
    B -->|updates| C[Model - ObservableList~RenameModel~]
    C -->|notifies| A
    B -->|calls| D[CoreFunctionalityHelper]
    D -->|executes| E[Commands]
    E -->|returns| F[List~RenameModel~]
    F -->|updates| C
```

**Components:**
- **View**: FXML files (e.g., `app_main_view.fxml`, `mode_add_custom_text.fxml`)
- **Controller**: Java classes (e.g., `ApplicationMainViewController`, `ModeAddCustomTextController`)
- **Model**: `ObservableList<RenameModel>` shared across the application

### 6.4. Dependency Injection (Guice)

```mermaid
graph TB
    A[Guice Injector] -->|creates| B[DIAppModule]
    A -->|creates| C[DICoreModule]
    A -->|creates| D[DIUIModule]

    B -->|provides| E[ExecutorService]
    B -->|provides| F[ResourceBundle]
    B -->|provides| G[LanguageTextRetrieverApi]

    C -->|provides| H[Commands]
    C -->|provides| I[Mappers]
    C -->|provides| J[FilesOperations]

    D -->|provides| K[Controllers]
    D -->|provides| L[FXMLLoaders]
    D -->|provides| M[ObservableList~RenameModel~]

    K -->|injects| E
    K -->|injects| H
    K -->|injects| I
```

---

## 7. Component Details

### 7.1. Core Layer Components

#### **FilesOperations**
Central service for file I/O operations:
- **File attribute extraction**: `getFileAbsolutePath()`, `getFileNameWithoutExtension()`, `getFileExtension()`
- **Metadata reading**: `getFileCreationTime()`, `getFileModificationTime()`, `getFileSize()`
- **MIME detection**: `getMimeType()` using Apache Tika
- **Renaming**: `renameFile(RenameModel)` - performs the actual `Files.move()` operation
- **Path utilities**: `getParentFolders()` for extracting parent directory names

#### **Commands**

| Command | Input | Output | Purpose |
|---------|-------|--------|---------|
| `MapFileToFileInformationCommand` | `List<File>` | `List<FileInformation>` | Extracts file attributes and metadata |
| `MapFileInformationToRenameModelCommand` | `List<FileInformation>` | `List<RenameModel>` | Converts domain models to UI models |
| `RenameCommand` | `List<RenameModel>` | `List<RenameModel>` | Performs file renaming |
| `ResetRenameModelsCommand` | `List<RenameModel>` | `List<RenameModel>` | Resets `newName` to original `fileName` |
| `FixEqualNamesCommand` | `List<FileInformation>` | `List<FileInformation>` | Appends suffix to duplicate names |
| **Preparation Commands** | `List<FileInformation>` | `List<FileInformation>` | Apply renaming logic |
| - `AddTextPrepareInformationCommand` | | | Adds text at begin/end |
| - `ChangeCasePreparePrepareInformationCommand` | | | Changes case (UPPER, lower, Title) |
| - `DateTimeRenamePrepareInformationCommand` | | | Uses datetime from metadata/filesystem |
| - `ExtensionChangePrepareInformationCommand` | | | Changes file extension |
| - `ImageDimensionsPrepareInformationCommand` | | | Uses image dimensions |
| - `ParentFoldersPrepareInformationCommand` | | | Uses parent folder names |
| - `RemoveTextPrepareInformationCommand` | | | Removes text from begin/end |
| - `ReplaceTextPrepareInformationCommand` | | | Replaces text occurrences |
| - `SequencePrepareInformationCommand` | | | Adds sequential numbers |
| - `TruncateNamePrepareInformationCommand` | | | Truncates filename |

#### **Mappers**

```mermaid
graph TB
    A[DataMapper Interface] -->|implemented by| B[FileToFileInformationMapper]
    A -->|implemented by| C[FileInformationToRenameModelMapper]
    A -->|implemented by| D[RenameModelToHtmlMapper]

    E[FileToMetadataMapper Abstract] -->|extended by| F[AviMapper]
    E -->|extended by| G[JpegMapper]
    E -->|extended by| H[Mp3Mapper]
    E -->|extended by| I[...]
```

**Key Mappers:**
- **FileToFileInformationMapper**: Uses `FilesOperations` and `FileToMetadataMapper` chain
- **FileInformationToRenameModelMapper**: Creates UI models with `isNeedRename` logic
- **RenameModelToHtmlMapper**: Generates HTML for the file info display panel

### 7.2. UI Layer Components

#### **ApplicationMainViewController**
The main controller managing the entire UI:

**Key responsibilities:**
1. **File table management**: Drag & drop, selection, display
2. **Mode selection**: Dynamically loads mode-specific views
3. **Button handlers**: Preview, Rename, Clear, Reload
4. **Progress tracking**: Binds progress bar to background tasks
5. **Table styling**: Applies CSS classes based on `RenameModel` state

**Important fields:**
- `ObservableList<RenameModel> loadedAppFilesList`: Shared model
- `filesTableView`: Displays all loaded files
- `appModeContainer`: StackPane that hosts mode-specific views
- `appModeChoiceBox`: ChoiceBox for selecting renaming mode

#### **Mode Controllers**
Each renaming mode has its own controller extending `ModeBaseController`:

```mermaid
classDiagram
    class ModeControllerApi {
        <<interface>>
        +getCommand() FileInformationCommand
        +setCommand(FileInformationCommand)
        +commandProperty() ObjectProperty~FileInformationCommand~
    }

    class ModeBaseController {
        <<abstract>>
        -CommandModel commandModel
        +getCommand() FileInformationCommand
        +setCommand(FileInformationCommand)
        +commandProperty() ObjectProperty~FileInformationCommand~
        #updateCommand()*
    }

    class ModeAddCustomTextController {
        -TextField textField
        -ItemPositionRadioSelector itemPositionRadioSelector
        +initialize()
        +handlePositionChanged(ItemPosition)
        +handleTextChanged(String)
        +updateCommand()
    }

    ModeControllerApi <|.. ModeBaseController
    ModeBaseController <|-- ModeAddCustomTextController
    ModeBaseController <|-- ModeChangeCaseController
    ModeBaseController <|-- ModeUseDatetimeController
```

**Controller responsibilities:**
- Initialize UI widgets (TextFields, RadioButtons, etc.)
- Listen to UI value changes
- Build corresponding `FileInformationCommand` when values change
- Notify listeners via `commandProperty()` (used by `ApplicationMainViewController`)

#### **CoreFunctionalityHelper**
Bridge between UI and Core logic:

```java
// Maps files to RenameModel (used when files are dropped)
void mapFileToRenameModel(List<File>, ProgressBar, ListCallback<RenameModel>)

// Resets models and applies new command (used when mode changes)
void resetModels(List<RenameModel>, FileInformationCommand, ProgressBar, ListCallback<RenameModel>)

// Prepares files for preview (used when settings change)
void prepareFiles(List<RenameModel>, FileInformationCommand, ProgressBar, ListCallback<RenameModel>)

// Executes renaming (used when Rename button clicked)
void renameFiles(List<RenameModel>, ProgressBar, ListCallback<RenameModel>)

// Reloads files after renaming (used when Reload button clicked)
void reloadFiles(List<RenameModel>, ProgressBar, ListCallback<RenameModel>)
```

All methods:
1. Create a `javafx.concurrent.Task`
2. Execute commands in the background thread
3. Update progress bar
4. Invoke callback on JavaFX Application Thread when complete

---

## 8. Application Flows

### 8.1. Application Startup Flow

```mermaid
sequenceDiagram
    participant User
    participant Launcher
    participant RenamerApplication
    participant Guice
    participant ViewLoaderApi
    participant Stage

    User->>Launcher: Run application
    Launcher->>RenamerApplication: main(args)
    RenamerApplication->>Guice: createInjector(DIAppModule, DICoreModule, DIUIModule)
    Guice-->>RenamerApplication: Injector
    RenamerApplication->>RenamerApplication: launch()
    RenamerApplication->>RenamerApplication: start(Stage)
    RenamerApplication->>Guice: getInstance(ViewLoaderApi)
    Guice-->>RenamerApplication: ViewLoaderService
    RenamerApplication->>ViewLoaderApi: loadFXML(APP_MAIN_VIEW)
    ViewLoaderApi-->>RenamerApplication: Parent node
    RenamerApplication->>Stage: setScene(Scene)
    RenamerApplication->>Stage: show()
    Stage-->>User: Display main window
```

### 8.2. File Loading Flow (Drag & Drop)

```mermaid
sequenceDiagram
    participant User
    participant TableView
    participant MainController
    participant CoreHelper
    participant ExecutorService
    participant Task
    participant Commands
    participant FilesOperations

    User->>TableView: Drag & Drop files
    TableView->>MainController: handleFilesDroppedEvent(DragEvent)
    MainController->>CoreHelper: mapFileToRenameModel(List<File>, ProgressBar, callback)
    CoreHelper->>Task: buildTaskWithCallbackOnUIThread()
    CoreHelper->>ExecutorService: execute(Task)

    ExecutorService->>Task: Background thread execution
    Task->>Commands: MapFileToFileInformationCommand.execute()
    Commands->>FilesOperations: getFileAbsolutePath(), getFileName(), etc.
    FilesOperations-->>Commands: File attributes
    Commands->>Commands: FileToMetadataMapper (Chain of Responsibility)
    Commands-->>Task: List<FileInformation>
    Task->>Commands: MapFileInformationToRenameModelCommand.execute()
    Commands-->>Task: List<RenameModel>

    Task->>MainController: onSucceeded callback (JavaFX Thread)
    MainController->>MainController: loadedAppFilesList.addAll(result)
    MainController->>TableView: Refresh table
    TableView-->>User: Display loaded files
```

### 8.3. Preview Flow (Settings Changed)

```mermaid
sequenceDiagram
    participant User
    participant ModeController
    participant MainController
    participant CoreHelper
    participant Task
    participant Commands

    User->>ModeController: Change setting (e.g., text input)
    ModeController->>ModeController: handleTextChanged()
    ModeController->>ModeController: updateCommand()
    ModeController->>ModeController: setCommand(newCommand)
    ModeController->>MainController: commandProperty changed (listener)
    MainController->>MainController: handleModeControllerCommandChanged()

    alt Auto-preview enabled
        MainController->>CoreHelper: prepareFiles(loadedAppFilesList, command, progressBar, callback)
        CoreHelper->>Task: Build background task
        Task->>Commands: ResetRenameModelsCommand.execute()
        Task->>Commands: PrepareCommand.execute() [e.g., AddTextPrepareInformationCommand]
        Task->>Commands: FixEqualNamesCommand.execute()
        Task-->>MainController: onSucceeded callback
        MainController->>MainController: Update loadedAppFilesList
        MainController-->>User: Update table with new names
    end
```

### 8.4. Rename Flow

```mermaid
sequenceDiagram
    participant User
    participant MainController
    participant Dialog
    participant CoreHelper
    participant Task
    participant RenameCommand
    participant FilesOperations
    participant FileSystem

    User->>MainController: Click Rename button
    MainController->>Dialog: showConfirmationDialog()
    Dialog-->>User: Confirm renaming?
    User->>Dialog: Confirm
    Dialog-->>MainController: true

    MainController->>CoreHelper: renameFiles(loadedAppFilesList, progressBar, callback)
    CoreHelper->>Task: Build background task
    Task->>RenameCommand: execute(List<RenameModel>, progressCallback)

    loop For each RenameModel (parallel stream)
        RenameCommand->>FilesOperations: renameFile(renameModel)
        FilesOperations->>FileSystem: Files.move(oldPath, newPath)

        alt Rename successful
            FileSystem-->>FilesOperations: Success
            FilesOperations->>FilesOperations: Set isRenamed=true, renameResult=SUCCESS
        else Rename failed
            FileSystem-->>FilesOperations: IOException
            FilesOperations->>FilesOperations: Set hasRenamingError=true, errorMessage
        end

        FilesOperations-->>RenameCommand: Updated RenameModel
        RenameCommand->>Task: updateProgress()
    end

    RenameCommand-->>Task: List<RenameModel>
    Task-->>MainController: onSucceeded callback
    MainController->>MainController: Update loadedAppFilesList
    MainController->>MainController: Set areFilesRenamed=true
    MainController->>MainController: Show Reload button, disable Rename button
    MainController-->>User: Table shows renamed files with status
```

### 8.5. Mode Change Flow

```mermaid
sequenceDiagram
    participant User
    participant ChoiceBox
    participant MainController
    participant MainHelper
    participant CoreHelper
    participant StackPane

    User->>ChoiceBox: Select new mode (e.g., CHANGE_CASE)
    ChoiceBox->>MainController: handleModeChanged()
    MainController->>MainHelper: getViewForAppMode(CHANGE_CASE)
    MainHelper-->>MainController: Parent (FXML view)
    MainController->>StackPane: Clear children
    MainController->>StackPane: Add new view

    MainController->>MainHelper: getControllerForAppMode(CHANGE_CASE)
    MainHelper-->>MainController: ModeChangeCaseController
    MainController->>MainController: Get command from controller

    alt Files not yet renamed
        MainController->>CoreHelper: resetModels(loadedAppFilesList, command, progressBar, callback)
        CoreHelper-->>MainController: Reset and apply new command logic
        MainController-->>User: Table updated with new preview
    end
```

---

## 9. Threading Model

### 9.1. Thread Architecture

```mermaid
graph TB
    subgraph "JavaFX Application Thread"
        A[UI Event Handlers]
        B[TableView Updates]
        C[Progress Bar Updates]
        D[Task Success Callbacks]
    end

    subgraph "Background Thread Pool"
        E[ExecutorService - Single Thread]
        F[Task 1: mapFileToRenameModel]
        G[Task 2: prepareFiles]
        H[Task 3: renameFiles]
    end

    subgraph "Commands Parallel Processing"
        I[ParallelStream Worker 1]
        J[ParallelStream Worker 2]
        K[ParallelStream Worker N]
    end

    A -->|submits| E
    E -->|executes| F
    E -->|executes| G
    E -->|executes| H

    F -->|spawns| I
    F -->|spawns| J
    F -->|spawns| K

    I -->|progress updates| C
    J -->|progress updates| C
    K -->|progress updates| C

    F -->|onSucceeded| D
    G -->|onSucceeded| D
    H -->|onSucceeded| D

    D -->|updates| B
```

### 9.2. Threading Strategy

**ExecutorService Configuration:**
```java
// Defined in DIAppModule
ExecutorService provideExecutorService() {
    return Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);  // Daemon thread - won't prevent JVM shutdown
        return thread;
    });
}
```

**Task Pattern:**
```java
Task<List<T>> task = new Task<>() {
    @Override
    protected List<T> call() {
        updateProgress(0, 0);  // Indeterminate
        List<T> result = executeCommand(progressCallback);
        return result;
    }
};

// Bind progress bar to task progress
progressBar.progressProperty().bind(task.progressProperty());

// Execute on background thread
executorService.execute(task);

// Callback runs on JavaFX Application Thread
task.setOnSucceeded(event -> {
    callback.accept(task.getValue());
});
```

**Parallel Processing in Commands:**
- `ListProcessingCommand.execute()` uses `parallelStream()` for parallel processing of file list
- Progress tracking is synchronized to avoid race conditions
- Each file is processed independently, allowing multi-core utilization

---

## 10. Dependency Injection

### 10.1. Guice Module Structure

```mermaid
graph TB
    subgraph "DIAppModule"
        A1[LanguageTextRetrieverApi → LanguageTextRetrieverService]
        A2[ResourceBundle Provider]
        A3[ExecutorService Provider]
    end

    subgraph "DICoreModule"
        B1[Tika Singleton]
        B2[FilesOperations]
        B3[Commands]
        B4[Mappers]
        B5[FileToMetadataMapper Chain Provider]
        B6[TextExtractorByKey Provider]
    end

    subgraph "DIUIModule"
        C1[ViewLoaderApi → ViewLoaderService]
        C2[CoreFunctionalityHelper]
        C3[MainViewControllerHelper]
        C4[Mode Controllers]
        C5[FXMLLoader Providers x 10]
        C6[Parent Providers x 10]
        C7[ModeControllerApi Providers x 10]
        C8[ObservableList RenameModel Provider]
    end

    A1 -.-> C2
    A2 -.-> A1
    A3 -.-> C2

    B3 -.-> C2
    B4 -.-> C2
    B2 -.-> B3
    B1 -.-> B2

    C1 -.-> C5
    C2 -.-> C4
    C8 -.-> C4
```

### 10.2. Key Bindings

**Singleton Services:**
```java
// DIAppModule
bind(LanguageTextRetrieverApi.class).to(LanguageTextRetrieverService.class).in(Singleton.class);

// DICoreModule
bind(Tika.class).in(Singleton.class);
bind(FilesOperations.class).in(Singleton.class);
bind(NameValidator.class).in(Singleton.class);

// DIUIModule
bind(ViewLoaderApi.class).to(ViewLoaderService.class).in(Singleton.class);
bind(ApplicationMainViewController.class).in(Singleton.class);
bind(CoreFunctionalityHelper.class).in(Singleton.class);
```

**Qualified Bindings:**
```java
// Custom qualifiers for each mode's FXML loader, Parent, and Controller
@AddCustomTextFxmlLoader
@ChangeCaseFxmlLoader
@UseDatetimeFxmlLoader
// ... and so on
```

**Provider Methods:**
```java
@Provides
@Singleton
public FileToMetadataMapper provideFileToMetadataMapper(...) {
    // Manually construct chain of responsibility
    nullMapper.setNext(aviMapper);
    aviMapper.setNext(bmpMapper);
    // ... full chain setup
    return nullMapper; // Head of the chain
}

@Provides
@Singleton
public ObservableList<RenameModel> provideAppGlobalRenameModelList() {
    return FXCollections.observableArrayList();
}
```

### 10.3. Injection Points

**Controller Injection:**
```java
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApplicationMainViewController implements Initializable {
    private final CoreFunctionalityHelper coreHelper;
    private final MainViewControllerHelper mainControllerHelper;
    private final AppModesConverter appModesConverter;
    private final ObservableList<RenameModel> loadedAppFilesList;
    // ...
}
```

**Service Injection:**
```java
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class CoreFunctionalityHelper {
    private final ExecutorService executorService;
    private final LanguageTextRetrieverApi languageTextRetriever;
    private final RenameModelToHtmlMapper renameModelToHtmlMapper;
    private final MapFileToFileInformationCommand mapFileToFileInformationCommand;
    private final MapFileInformationToRenameModelCommand mapFileInformationToRenameModelCommand;
    private final RenameCommand renameCommand;
    private final FixEqualNamesCommand fixEqualNamesCommand;
    private final ResetRenameModelsCommand resetRenameModelsCommand;
    // ...
}
```

---

## 11. Extending the Application

### 11.1. Adding a New Renaming Mode

**Step 1: Core Layer** - Create the command:
```java
// app/core/src/main/java/.../service/command/impl/preparation/
public class MyNewPrepareInformationCommand extends FileInformationCommand {

    private final String myParameter;

    @Override
    public FileInformation processItem(FileInformation item) {
        // Your renaming logic
        String newName = applyMyLogic(item.getFileName(), myParameter);
        item.setNewName(newName);
        return item;
    }
}
```

**Step 2: Core Layer** - Add enum:
```java
// app/core/src/main/java/.../enums/AppModes.java
public enum AppModes {
    // ... existing modes
    MY_NEW_MODE,
}
```

**Step 3: UI Layer** - Create FXML view:
```xml
<!-- app/ui/src/main/resources/fxml/mode_my_new_mode.fxml -->
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.control.TextField?>
<VBox xmlns:fx="http://javafx.com/fxml">
    <TextField fx:id="myParameterField" promptText="Enter parameter"/>
</VBox>
```

**Step 4: UI Layer** - Create controller:
```java
// app/ui/src/main/java/.../controller/mode/impl/
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeMyNewModeController extends ModeBaseController {

    @FXML
    private TextField myParameterField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        myParameterField.textProperty().addListener((obs, old, newVal) -> updateCommand());
    }

    @Override
    public void updateCommand() {
        String param = myParameterField.getText();
        var cmd = MyNewPrepareInformationCommand.builder()
            .myParameter(param)
            .build();
        setCommand(cmd);
    }
}
```

**Step 5: UI Layer** - Register in DI:
```java
// DIUIModule.java
private void bindViewControllers() {
    // ... existing bindings
    bind(ModeMyNewModeController.class).in(Singleton.class);
}

@Provides
@Singleton
@MyNewModeFxmlLoader
public FXMLLoader provideMyNewModeFxmlLoader(ViewLoaderApi viewLoaderApi) {
    return createFXMLLoader(viewLoaderApi, ViewNames.MODE_MY_NEW_MODE);
}

@Provides
@Singleton
@MyNewModeParent
public Parent provideMyNewModeParent(@MyNewModeFxmlLoader FXMLLoader loader) throws IOException {
    return loader.load();
}

@Provides
@Singleton
@MyNewModeController
public ModeControllerApi provideMyNewModeController(@MyNewModeFxmlLoader FXMLLoader loader,
                                                     @MyNewModeParent Parent parent) {
    return loader.getController();
}
```

**Step 6: UI Layer** - Add to ViewNames enum:
```java
public enum ViewNames {
    // ... existing
    MODE_MY_NEW_MODE("fxml/mode_my_new_mode.fxml"),
}
```

**Step 7**: Update `MainViewControllerHelper` to map `AppModes.MY_NEW_MODE` to the new controller and view.

### 11.2. Adding a New Metadata Mapper

**Create mapper:**
```java
public class MyFileTypeMapper extends FileToMetadataMapper {

    @Override
    public List<String> getSupportedExtensions() {
        return List.of(".myext");
    }

    @Override
    public FileInformationMetadata process(File input) {
        // Extract metadata using Metadata Extractor or custom logic
        return FileInformationMetadata.builder()
            .creationDate(extractedDate)
            .build();
    }
}
```

**Register in DICoreModule:**
```java
bind(MyFileTypeMapper.class).in(Singleton.class);

// Update provideFileToMetadataMapper to include in chain
webPmapper.setNext(myFileTypeMapper);
myFileTypeMapper.setNext(reserveMapper);
```

---

## Appendix: Renaming Modes Reference

| Mode | Enum | Command | Purpose |
|------|------|---------|---------|
| **Add Custom Text** | `ADD_CUSTOM_TEXT` | `AddTextPrepareInformationCommand` | Prepend or append text |
| **Change Case** | `CHANGE_CASE` | `ChangeCasePreparePrepareInformationCommand` | UPPER, lower, Title Case |
| **Use DateTime** | `USE_DATETIME` | `DateTimeRenamePrepareInformationCommand` | Use file/metadata dates |
| **Use Image Dimensions** | `USE_IMAGE_DIMENSIONS` | `ImageDimensionsPrepareInformationCommand` | Add WIDTHxHEIGHT |
| **Use Parent Folder Name** | `USE_PARENT_FOLDER_NAME` | `ParentFoldersPrepareInformationCommand` | Use folder names |
| **Remove Custom Text** | `REMOVE_CUSTOM_TEXT` | `RemoveTextPrepareInformationCommand` | Remove N chars from begin/end |
| **Replace Custom Text** | `REPLACE_CUSTOM_TEXT` | `ReplaceTextPrepareInformationCommand` | Find & replace text |
| **Add Sequence** | `ADD_SEQUENCE` | `SequencePrepareInformationCommand` | Add sequential numbers |
| **Truncate File Name** | `TRUNCATE_FILE_NAME` | `TruncateNamePrepareInformationCommand` | Limit filename length |
| **Change Extension** | `CHANGE_EXTENSION` | `ExtensionChangePrepareInformationCommand` | Change file extension |

---

## Summary

This document provides a comprehensive architectural overview of the Renamer App, covering:
- **Modular structure** with clean separation between Core and UI
- **Design patterns** (Command, Chain of Responsibility, MVC, DI)
- **Threading model** for responsive UI with background processing
- **Complete data flow** from file loading to renaming
- **Extension points** for adding new features

The application demonstrates solid software engineering practices, making it maintainable and extensible for future development.
