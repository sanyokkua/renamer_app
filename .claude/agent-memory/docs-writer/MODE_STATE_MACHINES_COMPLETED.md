---
name: Mode State Machines Document Completed
description: Comprehensive 1,200+ line technical reference for all 10 transformation modes, investigation output for Approach 3 implementation
type: project
---

## Completion Status

**Document:** `docs/MODE_STATE_MACHINES.md`
**Date Completed:** 2026-03-31
**Lines:** 2,281
**Mermaid Diagrams:** 13 (one per mode + conflict resolution)

## Content Summary

### Coverage
- All 10 transformation modes documented with:
  - Mermaid `stateDiagram-v2` state machines
  - Parameter specifications (V1 vs V2 comparison)
  - Algorithms with pseudocode
  - Validation rules tables
  - Edge cases with behavioral differences
  - V1/V2 compatibility notes
  - Design implications for ModeParameters records

- Cross-cutting concerns:
  - Conflict resolution with cascading safety example
  - Mode ordering and cumulative effects
  - Idempotence analysis
  - Shared infrastructure (enums, validation, no-throw contract)

### Design Decisions Documented

**Critical Differences Found:**
1. Mode 3 (Replace Text): V1 uses regex; V2 uses literal matching — **SECURITY ISSUE**
2. Mode 6 (Truncate): V1 allows empty results; V2 errors — **BEHAVIORAL DIFFERENCE**
3. Mode 7 (Extension): V1 allows empty extension; V2 errors — **BEHAVIORAL DIFFERENCE**
4. Mode 8 (Datetime): V2 missing 3 fallback features — **FEATURE GAP**
5. Mode 9 (Image Dimensions): V2 hardcodes space separator — **BUG IN V2**
6. Mode 10 (Parent Folder): V1 allows root-level; V2 errors — **BEHAVIORAL DIFFERENCE**

**Recommendations Made:**
- Use V2 literal matching for Replace Text (security improvement)
- Restore missing Datetime fallback features for V1 compatibility
- Fix Image Dimensions separator bug (add `nameSeparator` back to parameters)
- Standardize empty result handling (recommend V2 error approach)
- Implement sealed `ModeParameters` interface for type-safe parameter passing

### Key Sections

| Section | Content | Purpose |
|---|---|---|
| 1. Shared Infrastructure | Enums, validation, no-throw contract | Foundation for all modes |
| 2. State Machine Notation | State categories, transition triggers | Reference for reading diagrams |
| 3-12. Mode Specifications | 10 modes × (machine + algorithm + validation) | Implementation guide |
| 13. Conflict Resolution | DuplicateNameResolverImpl + Phase 4 disk check | Critical for preview/execution |
| 14. Cross-Mode Concerns | Ordering, dependencies, idempotence | Advanced usage patterns |
| 15. Design Implications | ModeParameters records + package structure | Implementation specification |
| Appendix | Mode summary table | Quick reference |

### For Next Step: Approach 3 Implementation

This document serves as the authoritative specification for implementing:
1. Sealed `ModeParameters` interface with 10 concrete records
2. V1/V2 compatibility shim (if choosing backward-compatible path)
3. Unit tests for parameter validation
4. Integration tests for each transformer
5. Conflict resolution tests with cascading scenarios

**Recommendation:** Use section 15 (Design Implications) as the implementation checklist.

## Technical Accuracy

- ✅ All 10 `TransformationMode` enum values mapped to modes
- ✅ All position enums (`ItemPosition`, `ItemPositionExtended`, `ItemPositionWithReplacement`) documented
- ✅ All validation rules cross-checked against NameValidator contract
- ✅ V1/V2 differences verified against existing code comments
- ✅ All Mermaid diagrams follow `stateDiagram-v2` syntax rules (no inline comments, alphanumeric IDs only)
- ✅ Example pseudocode in algorithms formatted consistently
- ✅ Edge cases include V1/V2 behavioral comparison tables

## Not Included (Out of Scope)

- Actual Java record implementations (left for coder agent)
- FXML controller refactoring (left for JavaFX skill)
- DI configuration changes (left for architect)
- Test code (left for tester agent)
- Changes to existing source code (this is investigation output only)

## Design Decisions for Team Consensus

Before implementation, team should decide:

1. **Replace Text regex security:** Drop V1 regex support or add explicit regex mode?
2. **Datetime fallback:** Restore V1 fallback flags or keep hard-error approach?
3. **Empty result handling:** Enforce V2 error behavior or allow V1 empty results?
4. **Image Dimensions separator:** Fix hardcoded space bug in V2?
5. **Sequential execution:** Check `requiresSequentialExecution()` flag or hardcode Sequence mode only?

## Related Documents

- `docs/V2_API_DESIGN_APPROACHES.md` — Approach 3 overview (Pragmatic Facade)
- `docs/ARCHITECTURE.md` — System overview with V1/V2 pipeline diagrams
- `docs/JavaFX_Backend_UI_Architecture_Guideline.md` — Full Approach 2 reference (for comparison)
- `docs/UI_BACKEND_ARCHITECTURE.md` — UI-backend interaction patterns

---

**This is an investigation output document.** No code was changed. All findings are backed by analysis of existing source and research findings.
