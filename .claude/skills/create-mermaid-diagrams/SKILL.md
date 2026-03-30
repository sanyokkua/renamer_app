---
name: create-mermaid-diagrams
description: Mermaid diagram syntax rules and patterns for architecture docs. Use when creating or updating any Mermaid diagram in docs/ARCHITECTURE.md or other doc files.
allowed-tools: Read, Write, Edit
---

# Create Mermaid Diagrams

Invoke when creating or updating Mermaid diagrams — architecture overviews, sequence diagrams, class hierarchies, state machines, pipeline flows. Most useful for `docs/ARCHITECTURE.md` updates.

---

## Golden Rules

1. **Never put comments inline** — `%%` comments must be on their own line
2. **IDs are alphanumeric only** — no spaces, hyphens, or special chars in IDs
3. **Quote labels with special chars** — `["Step 1: Init"]` not `[Step 1: Init]`
4. **Never use reserved words as IDs** — `end`, `class`, `subgraph`, `graph`, `default`
5. **IDs never start with numbers** — use `step1` not `1stStep`

```
✅ CORRECT:
%% This is a comment
A["Step 1: Init"] --> B["Step 2: Process"]

❌ WRONG (inline comment causes parse error):
A --> B %% This breaks rendering
```

---

## Diagram Type Quick Pick

| Goal | Use |
|------|-----|
| Process flow / pipeline | `flowchart TD` or `flowchart LR` |
| Class hierarchy / interfaces | `classDiagram` |
| Method call sequence | `sequenceDiagram` |
| State machine / lifecycle | `stateDiagram-v2` |
| Project timeline | `gantt` |
| Git branching | `gitgraph` |

---

## Flowchart (Most Common)

```mermaid
flowchart TD
    %% Node shapes
    A[Rectangle]
    B(Rounded)
    C{Diamond decision}
    D[(Database)]
    E((Circle))

    %% Connections
    A --> B
    B --> C
    C -->|Yes| D
    C -->|No| E

    %% Subgraph grouping
    subgraph phase1["Phase 1: Input"]
        A
        B
    end
```

---

## Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    actor U as User
    participant UI as JavaFX UI
    participant Orc as Orchestrator
    participant FS as FileSystem

    U->>UI: Select files
    UI->>+Orc: orchestrate(files, config)
    Orc->>+FS: extract metadata
    FS-->>-Orc: FileModel list
    Orc-->>-UI: RenameResult list
    UI-->>U: Show results
```

---

## Class Diagram

```mermaid
classDiagram
    class FileTransformationService {
        <<interface>>
        +transform(FileModel, Config) PreparedFileModel
        +transformBatch(List, Config) List
        +requiresSequentialExecution() bool
    }

    class AddTextTransformer {
        +transform(FileModel, AddTextConfig) PreparedFileModel
    }

    FileTransformationService <|.. AddTextTransformer : implements
```

---

## State Diagram

```mermaid
stateDiagram-v2
    direction LR
    [*] --> Idle
    Idle --> Processing: start
    Processing --> Complete: success
    Processing --> Error: failure
    Complete --> [*]
    Error --> Idle: retry
```

---

## Special Characters in Labels

```mermaid
flowchart LR
    A["Function#40;param#41;"]
    B["Array#91;0#93;"]
    C["Map#123;key#125;"]
```

| Character | Entity |
|-----------|--------|
| `(` `)` | `#40;` `#41;` |
| `[` `]` | `#91;` `#93;` |
| `{` `}` | `#123;` `#125;` |

---

## Validation Checklist

- [ ] No inline `%%` comments (all on own lines)
- [ ] All IDs alphanumeric, no reserved words
- [ ] Labels with `:`, `-`, `(`, etc. are quoted
- [ ] `subgraph` blocks have matching `end`
- [ ] Direction declared (`TD`, `LR`, etc.) for flowcharts

---

## Common Errors

| Error | Cause | Fix |
|-------|-------|-----|
| Parse error after `-->` | Inline comment | Move `%%` to its own line |
| Diagram doesn't render | Reserved word as ID | `end` → `endNode`, `class` → `classNode` |
| Syntax error on `(` | Unquoted parens in label | `["text(x)"]` or `["text#40;x#41;"]` |
| "end" breaks diagram | Reserved word | Use `["end"]` in label instead |
