---
name: project-docs
description: Documentation standards for the Renamer App — README structure, ADR format, diagram standards, and inline comment rules. Use when writing or updating project documentation, README files, or Architecture Decision Records.
paths: docs/**/*.md, *.md
allowed-tools: Read, Write, Edit
---

# Project Documentation Standards — Renamer App

## Critical Rules

- MUST NOT include commented-out code under any circumstance — use Git history to retrieve deleted code
- MUST update documentation in the same commit as the corresponding code change — never defer
- MUST delete stale documentation immediately upon discovery
- MUST NOT duplicate content from another document — link to the authoritative source instead
- Comments explain **why**, not **what** — never paraphrase what the code literally does

---

## Documentation Philosophy

- Document **why** code exists and **what** its public contract is — never narrate implementation steps
- Before writing a comment, ask: can a rename or refactor make this self-evident?
- Inline comments explain intent, warn about non-obvious edge cases, or reference business rules

```java
// Circuit-breaker threshold chosen to match the p99 latency SLA (200 ms).
// Lowering this value risks false-positive outages during deploy spikes.
CIRCUIT_BREAKER_TIMEOUT_MS = 200
```

---

## README Structure

Every repository MUST contain a `README.md` at its root with these six sections in order:

| # | Section | Content |
|---|---------|---------|
| 1 | **Purpose** | 3–5 sentences: what the project does, who it is for, why it exists |
| 2 | **Prerequisites** | Exhaustive list of required tools/runtimes with **minimum version numbers** |
| 3 | **Setup** | Step-by-step from fresh clone to running local environment; every command copy-pasteable |
| 4 | **Usage** | How to run, test, and build; most common developer workflows |
| 5 | **Contributing** | Branch naming, commit conventions, PR process |
| 6 | **License** | License type or "Proprietary" statement |

---

## Architecture Decision Records (ADRs)

### When to Create an ADR

Create an ADR when:
- A technology, framework, or major library is adopted or replaced
- A system boundary or data-flow topology changes
- A decision constrains future technical choices
- Trade-off analysis is needed that future developers will question

### Format and Storage

- Store in `docs/developers/` with naming `adr-NNNN-short-title.md` (e.g., `adr-0001-use-guice-for-di.md`)
- Zero-padded four-digit sequence number, monotonically increasing

Every ADR MUST contain:

| # | Section | Requirement |
|---|---------|------------|
| 1 | **Title** | Short descriptive title prefixed with sequence number |
| 2 | **Status** | `Proposed` / `Accepted` / `Deprecated` / `Superseded by ADR-NNNN` |
| 3 | **Date** | `YYYY-MM-DD` of the decision |
| 4 | **Context** | The situation that forced a decision; state the problem neutrally |
| 5 | **Decision** | The decision taken, stated clearly and directly |
| 6 | **Consequences** | Both positive and negative anticipated outcomes |
| 7 | **Alternatives Considered** | Other options evaluated and why rejected |

---

## Diagram Standards

- Diagrams MUST be authored using code-based tools: **Mermaid** (preferred) or Structurizr DSL
- Diagram source files MUST be stored in the repository alongside the code they describe
- Store diagrams in `docs/diagrams/`
- Binary image exports (PNG, SVG) MAY be committed alongside source for environments without live rendering
- MUST NOT commit binary-only diagrams without a code-based source

Required diagrams for this project:
1. **System boundary diagram** — the app and its direct dependencies
2. **Data-flow diagram** — how files move through the V2 pipeline for the primary use case

See `/create-mermaid-diagrams` for Mermaid syntax rules and common errors.

---

## Inline Comment Rules

- Inline comments MUST explain reasoning or intent — never restate what the code does
- Use `//` for single-line explanations; `/* */` for multi-line implementation notes inside methods
- Javadoc (`/** */`) is for public API contract only — see `/java-developer` for full Javadoc standards

### TODO / FIXME / HACK Format

```
// TODO(owner): description [TICKET-ID]
// FIXME(owner): description [TICKET-ID]
// HACK(owner): description [TICKET-ID]
```

- `FIXME` MUST NOT exist without a linked ticket
- `HACK` MUST NOT exist without a linked ticket
- `TODO` without a ticket MUST be resolved or ticketed within 30 days

---

## Documentation Lifecycle

- All source documentation (README, ADRs, diagrams) MUST be in the same repository as the code
- When code behavior changes, update corresponding documentation in the **same commit**
- When code is deleted, delete its documentation in the same commit
- Reviewers MUST verify: new/changed public APIs have complete documentation, no orphaned docs remain

---

## Writing Style

- Use imperative mood and active voice: "Return the user ID." ✅ — not "Returns the user ID." ❌
- State numeric thresholds explicitly — never "a few", "some", "reasonable"
- Technical jargon MUST be defined if unavoidable

---

## Anti-Duplication

- MUST NOT copy-paste content from one document into another — link instead
- Every discrete piece of information MUST have exactly one canonical location
- If the same information exists in two places, choose one canonical source and replace the other with a link

---

## Repository Structure

```
repo-root/
├── README.md                               # Required: 6 sections above
├── docs/
│   ├── developers/
│   │   ├── architecture/                   # Data models, pipeline, DI, metadata, modes
│   │   ├── guides/                         # How-to guides (add-transformation-mode.md, build-and-package.md, etc.)
│   │   ├── reference/                      # UI architecture, settings, testing strategy, AI setup
│   │   └── ui_design/                      # JavaFX design system, CSS tokens, component patterns
│   ├── users/                              # User-facing docs (user-guide.md, mode-reference-card.md)
│   ├── diagrams/                           # Code-based Mermaid source files
│   └── screens/                            # App screenshots (v1/, v2/ per OS)
└── .claude/                                # Claude Code infrastructure (skills, memory, CLAUDE.md)
```

ADRs: this project does not have a dedicated `docs/adr/` folder. If creating ADRs, place them in `docs/developers/` with naming `adr-NNNN-short-title.md`.
