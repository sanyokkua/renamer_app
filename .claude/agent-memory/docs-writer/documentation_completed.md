---
name: UI-Backend Architecture Document Completed
description: Comprehensive technical document for redesign planning created March 2026
type: project
---

Created `/Users/ok/Development/GitHub/renamer_app/docs/UI_BACKEND_ARCHITECTURE.md` — a 500+ line technical reference document detailing the current UI-backend connection.

**Key artifacts documented:**
- Complete DI module stack (DIAppModule, DICoreModule, DIUIModule) with V2 module wiring verified
- InjectQualifiers pattern for 31 qualifiers supporting 10 UI modes
- ModeControllerApi contract and all 10 mode controller implementations
- ApplicationMainViewController orchestration flows
- V1 Command Pattern pipeline (14 command classes documented)
- V2 pipeline architecture (wired but unused) — FileRenameOrchestratorImpl, 10 transformers
- Threading model comparison (single-threaded V1 vs. virtual thread V2)
- All data flows: file loading, transformation preview, rename execution
- **10 pain points identified for redesign** (mutable models, no V2 bridge, DI friction, etc.)
- Full class reference appendix (40+ UI/V1/V2 classes)

**Mermaid diagrams embedded:**
- File loading flow (sequential + parallel metadata extraction)
- Transformation preview flow (UI → CoreFunctionalityHelper → background task → FX thread)
- Rename execution flow (physical file operations → RenameModel state updates)

**Verified against source:**
- FileInformationCommand interface (abstract class, actually)
- FileModel location (in api module, not core)
- FileRenameOrchestratorImpl 4-phase pipeline
- CoreFunctionalityHelper threading pattern (Task<V> + ExecutorService)
- DIUIModule 10 mode controller bindings
- All 10 mode controller command mappings

**Why:** Document serves as input for architecture redesign decisions: V2 adoption, V1 immutability refactor, gradual migration strategy, or infrastructure extraction.
