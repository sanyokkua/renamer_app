# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

JavaFX 25 desktop app for batch file renaming with metadata extraction. Multi-module Maven: `app/core` (business logic), `app/ui` (JavaFX frontend), `app/utils` (standalone — **not imported by core or ui**). Java 25 with JPMS (`module-info.java`) — always export new packages.

## Build Commands

Run from `app/` directory. `.mvn/maven.config` applies `-B --no-transfer-progress` globally.

```bash
mvn compile -q -ff                                         # Compile only (fast feedback)
mvn test -q -ff -Dai=true                                  # All tests, quiet output
mvn test -q -ff -Dai=true -Dtest=ClassName                # Single test class
mvn test -q -ff -Dai=true -Dtest=ClassName#methodName     # Single test method
mvn clean test jacoco:report -Dai=true                     # Coverage report
mvn verify -Pcode-quality -q                               # Checkstyle + PMD + SpotBugs
../scripts/ai-build.sh                                     # Sequential: compile → lint → test
mvn clean install -q                                       # Build all modules
cd app/ui && mvn javafx:run                                # Run the app
```

## Architecture: Two Coexisting Generations

**V1 (Legacy)** — Command Pattern: `FileInformation → RenameModel`. Maintained for compatibility.
**V2 (Production)** — Strategy + Pipeline: `FileModel → PreparedFileModel → RenameResult`. Use for all new features.

V2 pipeline phases (virtual threads via `Executors.newVirtualThreadPerTaskExecutor()`):
1. Metadata Extraction (parallel) — `File` → `FileModel`
2. Transformation (parallel, sequential for ADD_SEQUENCE) — `FileModel` → `PreparedFileModel`
3. Duplicate Resolution (sequential, appends `_1`, `_2`)
4. Physical Rename (parallel) — `PreparedFileModel` → `RenameResult`

Pipeline **never throws** — errors captured in `hasError`/`RenameStatus` fields.

## Critical Patterns

**DI**: Google Guice 7 (no Spring). Constructor injection only:
```java
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MyService { private final Dep dep; }
```
Modules: `DIAppModule`, `DICoreModule`, `DIUIModule` in `app/ui/.../config/`; `DIV2ServiceModule` in `app/core/.../config/`. `DICoreModule` installs `DIV2ServiceModule`.

**V2 model builders** — non-default prefix, critical:
```java
PreparedFileModel.builder().withOriginalFile(file).withNewName(name).withHasError(false).build();
// NOT: .originalFile(file)  ← compile error
```

**UI disambiguation**: `InjectQualifiers` holds 30 `@jakarta.inject.Qualifier` annotations (10 each for FXMLLoader, Parent, ModeControllerApi). Required when adding new modes.

**JPMS**: `ua.renamer.app.core.v2.interfaces` and `ua.renamer.app.core.v2.exception` are intentionally NOT exported.

## Skills (invoke with `/skill-name`)

| Skill | When to use |
|-------|-------------|
| `/java-developer` | Writing any Java code — auto-loads logging, Javadoc, and dependency rules |
| `/write-junit5-tests` | Writing unit or integration tests |
| `/javafx` | Writing JavaFX controllers, FXML, or CSS in `app/ui/` |
| `/add-transformation-mode` | Adding a new V2 transformation mode end-to-end |
| `/project-docs` | Writing or updating README, ADRs, or architecture docs |
| `/use-exiftool-metadata` | Embedding datetime/GPS into test files |
| `/use-ffmpeg-cli` | Generating base test media files |
| `/create-mermaid-diagrams` | Creating architecture or flow diagrams |

## Test Data

Real test files in `app/core/src/test/resources/test-data/`. Generate with `tools/generate_test_data.py` or manually with FFmpeg + ExifTool (see skills above).

## MCP Server

`py-search-helper` provides web search: `search_web_ddg(query)`, `open_page(url)`. Use for library API docs (Tika, Guice, JavaFX, metadata-extractor).

## Deployment

jdeploy (`package.json`) builds native installers. CI/CD (`.github/workflows/maven.yml`) triggers on `*-release-*` / `*-snapshot-*` branches. Entry point: `ua.renamer.app.Launcher`.
