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
| `/gitnexus-exploring` | Understand architecture and execution flows |
| `/gitnexus-impact-analysis` | Blast radius / safety check before editing |
| `/gitnexus-debugging` | Trace bugs and errors through the call graph |
| `/gitnexus-refactoring` | Safe rename, extract, or split operations |
| `/gitnexus-guide` | GitNexus tools and resources reference |
| `/gitnexus-cli` | Manage the GitNexus index (analyze, status, clean) |

## Test Data

Real test files in `app/core/src/test/resources/test-data/`. Generate with `tools/generate_test_data.py` or manually with FFmpeg + ExifTool (see skills above).

## MCP Server

`py-search-helper` provides web search: `search_web_ddg(query)`, `open_page(url)`. Use for library API docs (Tika, Guice, JavaFX, metadata-extractor).

## Deployment

jdeploy (`package.json`) builds native installers. CI/CD (`.github/workflows/maven.yml`) triggers on `*-release-*` / `*-snapshot-*` branches. Entry point: `ua.renamer.app.Launcher`.

macOS builds are stable. Linux/Windows have known environment-specific issues — see [`docs/cross-platform-issues.md`](../docs/cross-platform-issues.md).

<!-- gitnexus:start -->
# GitNexus — Code Intelligence

This project is indexed by GitNexus as **renamer_app** (4655 symbols, 17057 relationships, 196 execution flows). Use the GitNexus MCP tools to understand code, assess impact, and navigate safely.

> If any GitNexus tool warns the index is stale, run `npx gitnexus analyze` in terminal first.

## Always Do

- **MUST run impact analysis before editing any symbol.** Before modifying a function, class, or method, run `gitnexus_impact({target: "symbolName", direction: "upstream"})` and report the blast radius (direct callers, affected processes, risk level) to the user.
- **MUST run `gitnexus_detect_changes()` before committing** to verify your changes only affect expected symbols and execution flows.
- **MUST warn the user** if impact analysis returns HIGH or CRITICAL risk before proceeding with edits.
- When exploring unfamiliar code, use `gitnexus_query({query: "concept"})` to find execution flows instead of grepping. It returns process-grouped results ranked by relevance.
- When you need full context on a specific symbol — callers, callees, which execution flows it participates in — use `gitnexus_context({name: "symbolName"})`.

## When Debugging

1. `gitnexus_query({query: "<error or symptom>"})` — find execution flows related to the issue
2. `gitnexus_context({name: "<suspect function>"})` — see all callers, callees, and process participation
3. `READ gitnexus://repo/renamer_app/process/{processName}` — trace the full execution flow step by step
4. For regressions: `gitnexus_detect_changes({scope: "compare", base_ref: "main"})` — see what your branch changed

## When Refactoring

- **Renaming**: MUST use `gitnexus_rename({symbol_name: "old", new_name: "new", dry_run: true})` first. Review the preview — graph edits are safe, text_search edits need manual review. Then run with `dry_run: false`.
- **Extracting/Splitting**: MUST run `gitnexus_context({name: "target"})` to see all incoming/outgoing refs, then `gitnexus_impact({target: "target", direction: "upstream"})` to find all external callers before moving code.
- After any refactor: run `gitnexus_detect_changes({scope: "all"})` to verify only expected files changed.

## Never Do

- NEVER edit a function, class, or method without first running `gitnexus_impact` on it.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis.
- NEVER rename symbols with find-and-replace — use `gitnexus_rename` which understands the call graph.
- NEVER commit changes without running `gitnexus_detect_changes()` to check affected scope.

## Tools Quick Reference

| Tool | When to use | Command |
|------|-------------|---------|
| `query` | Find code by concept | `gitnexus_query({query: "auth validation"})` |
| `context` | 360-degree view of one symbol | `gitnexus_context({name: "validateUser"})` |
| `impact` | Blast radius before editing | `gitnexus_impact({target: "X", direction: "upstream"})` |
| `detect_changes` | Pre-commit scope check | `gitnexus_detect_changes({scope: "staged"})` |
| `rename` | Safe multi-file rename | `gitnexus_rename({symbol_name: "old", new_name: "new", dry_run: true})` |
| `cypher` | Custom graph queries | `gitnexus_cypher({query: "MATCH ..."})` |

## Impact Risk Levels

| Depth | Meaning | Action |
|-------|---------|--------|
| d=1 | WILL BREAK — direct callers/importers | MUST update these |
| d=2 | LIKELY AFFECTED — indirect deps | Should test |
| d=3 | MAY NEED TESTING — transitive | Test if critical path |

## Resources

| Resource | Use for |
|----------|---------|
| `gitnexus://repo/renamer_app/context` | Codebase overview, check index freshness |
| `gitnexus://repo/renamer_app/clusters` | All functional areas |
| `gitnexus://repo/renamer_app/processes` | All execution flows |
| `gitnexus://repo/renamer_app/process/{name}` | Step-by-step execution trace |

## Self-Check Before Finishing

Before completing any code modification task, verify:
1. `../scripts/ai-build.sh` was run and output reviewed (compile → Checkstyle → PMD → SpotBugs → tests)
2. `gitnexus_impact` was run for all modified symbols
3. No HIGH/CRITICAL risk warnings were ignored
4. `gitnexus_detect_changes()` confirms changes match expected scope
5. All d=1 (WILL BREAK) dependents were updated

## Keeping the Index Fresh

After committing code changes, the GitNexus index becomes stale. Re-run analyze to update it:

```bash
npx gitnexus analyze
```

If the index previously included embeddings, preserve them by adding `--embeddings`:

```bash
npx gitnexus analyze --embeddings
```

To check whether embeddings exist, inspect `.gitnexus/meta.json` — the `stats.embeddings` field shows the count (0 means no embeddings). **Running analyze without `--embeddings` will delete any previously generated embeddings.**

> Claude Code users: A PostToolUse hook handles this automatically after `git commit` and `git merge`.

## CLI

| Task | Skill |
|------|-------|
| Understand architecture / "How does X work?" | `/gitnexus-exploring` |
| Blast radius / "What breaks if I change X?" | `/gitnexus-impact-analysis` |
| Trace bugs / "Why is X failing?" | `/gitnexus-debugging` |
| Rename / extract / split / refactor | `/gitnexus-refactoring` |
| Tools, resources, schema reference | `/gitnexus-guide` |
| Index, status, clean, wiki CLI commands | `/gitnexus-cli` |

<!-- gitnexus:end -->