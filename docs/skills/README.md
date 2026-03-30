# Skills for Renamer App Development

Project-native Claude Code skills are stored in **`.claude/skills/`** (shared via git, available to all contributors).

Each skill lives in its own directory with a `SKILL.md` entrypoint and optional supporting files.

## Available Skills

| Directory | Invocation | Auto-loads? | Purpose |
|-----------|------------|------------|---------|
| `java-developer/` | `/java-developer` | ✓ when writing Java | Guice/JavaFX patterns, V2 builder syntax, DI rules + logging/Javadoc/dependencies supporting files |
| `write-junit5-tests/` | `/write-junit5-tests` | ✓ when writing tests | JUnit 5/AssertJ/Mockito patterns |
| `javafx/` | `/javafx` | ✓ when editing `app/ui/` | JavaFX threading, FXML rules, CSS, accessibility, crash handling |
| `add-transformation-mode/` | `/add-transformation-mode` | ✗ user-invoked only | End-to-end 6-step procedure with code templates |
| `project-docs/` | `/project-docs` | ✓ when editing docs/ | README structure, ADR format, diagram standards, comment rules |
| `use-exiftool-metadata/` | `/use-exiftool-metadata` | ✗ user-invoked only | datetime/GPS embedding for `test-data/` |
| `use-ffmpeg-cli/` | `/use-ffmpeg-cli` | ✗ user-invoked only | Synthetic media file generation for `test-data/` |
| `create-mermaid-diagrams/` | `/create-mermaid-diagrams` | ✓ when editing diagrams | Syntax rules, diagram types, common errors |

## Skill Structure

```
.claude/skills/
├── java-developer/
│   ├── SKILL.md          ← core Java rules
│   ├── examples.md       ← code templates
│   ├── logging.md        ← SLF4J rules
│   ├── javadoc.md        ← Javadoc standards
│   └── dependencies.md   ← approved/prohibited libraries
├── write-junit5-tests/
│   ├── SKILL.md
│   └── examples.md
├── javafx/
│   └── SKILL.md
├── add-transformation-mode/
│   ├── SKILL.md
│   └── templates.md
├── project-docs/
│   └── SKILL.md
├── use-exiftool-metadata/
│   └── SKILL.md
├── use-ffmpeg-cli/
│   └── SKILL.md
└── create-mermaid-diagrams/
    └── SKILL.md
```

## Usage

Invoke skills in Claude Code with `/skill-name`:
```
/add-transformation-mode MyNewMode
/write-junit5-tests
/javafx
```

Auto-loading skills activate when Claude detects relevant context (e.g., editing a `.java` file, working in `app/ui/`, editing `docs/`).
