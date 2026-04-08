# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

JavaFX 25 desktop app for batch file renaming with metadata extraction. Multi-module Maven: `app/api` (interfaces/enums/models), `app/core` (business logic), `app/backend` (session/service layer), `app/metadata` (metadata extractors), `app/ui` (JavaFX frontend), `app/utils` (standalone — **not imported by other modules**). Java 25 with JPMS (`module-info.java`) — always export new packages.

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

## After Any Code Change — REQUIRED

**MUST run `../scripts/ai-build.sh` after every code change before finishing.** This is not optional.

The script runs the full pipeline in order: compile → Checkstyle → PMD → SpotBugs → tests. All three linting tools (Checkstyle, PMD with `targetJdk=25`, SpotBugs) are active. Violations are informational (`failOnViolation=false`) but must be reviewed and reported to the user.

## Architecture

Strategy + Pipeline: `FileModel → PreparedFileModel → RenameResult`.

Pipeline phases (virtual threads via `Executors.newVirtualThreadPerTaskExecutor()`):
1. Metadata Extraction (parallel) — `File` → `FileModel`
2. Transformation (parallel, sequential for ADD_SEQUENCE) — `FileModel` → `PreparedFileModel`
3. Duplicate Resolution (sequential, appends `_1`, `_2`)
4. Physical Rename (parallel) — `PreparedFileModel` → `RenameResult`

Pipeline **never throws** — errors captured in `hasError`/`RenameStatus` fields.

Module responsibilities:
- `app/api` — Shared interfaces, enums (`ua.renamer.app.api.enums`), models (`ua.renamer.app.api.model`), session contracts
- `app/core` — Transformers (`ua.renamer.app.core.service.transformation`), orchestrator (`ua.renamer.app.core.service.impl`), file mapper (`ua.renamer.app.core.mapper`), DI module (`DIV2ServiceModule`)
- `app/backend` — Session implementations, DI module (`DIBackendModule`)
- `app/metadata` — File metadata extractors, DI module (`DIMetadataModule`)

## Critical Patterns

**DI**: Google Guice 7 (no Spring). Constructor injection only:
```java
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MyService { private final Dep dep; }
```
Modules: `DIAppModule`, `DICoreModule`, `DIUIModule` in `app/ui/.../config/`; `DIV2ServiceModule` in `app/core/.../config/`; `DIBackendModule` in `app/backend/.../config/`; `DIMetadataModule` in `app/metadata/.../config/`. `DICoreModule` installs `DIMetadataModule` and `DIV2ServiceModule`.

**V2 model builders** — non-default prefix, critical:
```java
PreparedFileModel.builder().withOriginalFile(file).withNewName(name).withHasError(false).build();
// NOT: .originalFile(file)  ← compile error
```

**UI disambiguation**: `InjectQualifiers` holds 30 `@jakarta.inject.Qualifier` annotations (10 each for FXMLLoader, Parent, ModeControllerApi). Required when adding new modes.

**JPMS**: Internal packages (interfaces, exceptions) that are implementation details are intentionally NOT exported from their respective modules. Always check the module's `module-info.java` before adding cross-module calls.

## Agents (invoke with `@"agent-name (agent)"`)

Six specialized agents cover the full development lifecycle. Each is tuned to
this project's Java/Maven/JavaFX stack. Invoke them in pipeline order for
planned work, or individually for reactive tasks.

| Agent | Model | Role | When to invoke |
|-------|-------|------|----------------|
| `investigator` | haiku | Read-only codebase cartographer | Before any new task — maps the code, traces data flow, identifies modification scope |
| `architect` | sonnet | Technical designer | After investigation — designs solution, evaluates trade-offs, writes `PLAN.md` |
| `coder` | sonnet | Step-by-step implementer | After human approves `PLAN.md` — implements exactly one plan step at a time |
| `tester` | sonnet | JUnit 5 QA engineer | After each coder step — writes tests, finds edge cases, verifies the no-throw contract |
| `debugger` | sonnet | Root cause analyst | When any Maven build, test, or runtime failure occurs |
| `docs-writer` | haiku | Technical writer | After implementation — writes/updates README, ADRs, ARCHITECTURE.md, Javadoc, CHANGELOG |

**Standard pipeline for planned work:**
```
investigator → architect → 🧑 review PLAN.md → coder (step N) → tester → repeat
```

**Reactive invocation (no pipeline needed):**
```
Build/test failure       → debugger → tester (add regression test)
New architecture choice  → architect (write ADR) → docs-writer
Documentation drift      → docs-writer
```

**Invocation examples:**
```bash
# Map the codebase before starting a new feature
@"investigator (agent)" trace the data flow from file selection to physical rename

# Design a solution based on investigation findings
@"architect (agent)" design a new REPLACE_TEXT transformation mode

# Implement one step from the approved plan
@"coder (agent)" implement Step 1 of PLAN.md

# Test what was just implemented
@"tester (agent)" test the changes from Step 1 of PLAN.md

# Debug a failing build
@"debugger (agent)" [paste full Maven error output here]

# Document a completed feature
@"docs-writer (agent)" document the new REPLACE_TEXT mode added in PLAN.md
```

## Skills (invoke with `/skill-name`)

| Skill | When to use |
|-------|-------------|
| `/java-developer` | Writing any Java code — auto-loads logging, Javadoc, and dependency rules |
| `/write-junit5-tests` | Writing unit or integration tests |
| `/javafx` | Writing JavaFX controllers, FXML, or CSS in `app/ui/` |
| `/javafx-ui-designer` | Designing, styling, or theming JavaFX UI — colors, CSS tokens, layout, typography, accessibility |
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

macOS builds are stable. Linux/Windows have known environment-specific issues — see [`docs/cross-platform-issues.md`](../docs/developers/temporary/cross-platform-issues.md).
