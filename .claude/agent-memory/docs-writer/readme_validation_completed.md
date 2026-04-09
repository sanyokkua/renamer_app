---
name: README.md Validation Completed
description: Comprehensive fact-check and restructure of README.md to 6-section standard with YAML frontmatter
type: project
---

## Validation Completed — 2026-04-09

### Facts Verified
- **10 transformation modes** — verified against `TransformationMode.java` enum: ADD_TEXT, REMOVE_TEXT, REPLACE_TEXT, CHANGE_CASE, ADD_DATETIME, ADD_DIMENSIONS, NUMBER_FILES, ADD_FOLDER_NAME, TRIM_NAME, CHANGE_EXTENSION
- **Java 25** — verified in `app/pom.xml` (maven.compiler.release=25)
- **JavaFX 25.0.2** — verified in `app/pom.xml`
- **Guice 7.0.0** — verified in `app/pom.xml`
- **Lombok 1.18.44** — verified in `app/pom.xml`
- **JUnit 5 (6.0.3)** — verified in `app/pom.xml`
- **metadata-extractor 2.19.0** — verified in `app/pom.xml`
- **Apache Tika 3.3.0** — verified in `app/pom.xml`
- **6 modules** — verified: api, core, backend, metadata, ui, utils
- **CI badge** — references `.github/workflows/build.yml` (verified correct)
- **Platform support** — docs claim macOS, Linux, Windows x64; ARM64 Windows not supported (matches CLAUDE.md)
- **v1 screenshots** — 13 PNG files exist in `docs/screens/v1/`
- **v2 screenshots** — referenced but do not exist yet (acceptable placeholder per project instructions)
- **Documentation links** — all referenced `.md` files verified to exist:
  - `docs/users/user-guide.md` ✅
  - `docs/users/mode-reference-card.md` ✅
  - `docs/developers/architecture/project-overview.md` ✅
  - `docs/developers/architecture/pipeline-architecture.md` ✅
  - `docs/developers/architecture/transformation-modes.md` ✅
  - `docs/developers/architecture/data-models.md` ✅
  - `docs/developers/architecture/dependency-injection.md` ✅
  - `docs/developers/architecture/metadata-extraction.md` ✅
  - `docs/developers/reference/ui-architecture.md` ✅
  - `docs/developers/reference/settings-system.md` ✅
  - `docs/developers/reference/testing-strategy.md` ✅
  - `docs/developers/reference/ai-agent-setup.md` ✅
  - `docs/developers/guides/build-and-package.md` ✅
  - `docs/developers/guides/icon-creation.md` ✅
  - `docs/developers/guides/add-transformation-mode.md` ✅
  - `docs/developers/guides/cross-platform-notes.md` ✅

### Changes Made
1. **Added YAML frontmatter** with title, description, audience, validation timestamp, commit hash, and module list
2. **Restructured to 6-section standard**:
   - **Purpose** — merged tagline, features, screenshots into cohesive opening
   - **Prerequisites** — moved tools/versions; separated pre-built vs source paths
   - **Setup** — consolidated download instructions (macOS/Windows/Linux) + build from source
   - **Usage** — build commands, project structure, documentation index, tech stack table
   - **Contributing** — agents, pipeline, links to guide
   - **License** — unchanged
3. **Removed all TODOs and meta-commentary** — original had none
4. **Reorganized content** — moved tech stack table from top-level section into Usage; moved modules explanation into Usage as context for developers
5. **Expanded documentation index** — added all developer doc links with descriptions
6. **Improved clarity** — separated "download and run" from "build from source" with clearer headings

### No Breaking Changes
- All original content preserved
- All links valid
- No factual claims corrected (all were accurate)
- v2 screenshot placeholders kept as-is (acceptable per project instructions)
