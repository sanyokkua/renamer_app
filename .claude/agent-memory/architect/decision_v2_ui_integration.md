---
name: V2 UI Integration Architectural Decision
description: Approach 3 (Pragmatic Facade) selected for connecting V2 pipeline to JavaFX UI — March 2026
type: project
---

**Decision (2026-03-31):** Selected Approach 3 — Pragmatic Facade — for integrating the V2 pipeline with the JavaFX UI layer. Documented in `docs/V2_API_DESIGN_APPROACHES.md`.

**Why:** Four approaches were evaluated. Approach 1 (Thin Adapter Shim) perpetuates V1 technical debt and blocks agent integration. Approach 2 (Full SessionApi with ModeDescriptor/FieldDescriptor/ModeViewFactory) achieves all goals but replaces 10 working FXML layouts with auto-generated forms — high risk and high scope for a marginal Phase 1 gain. Approach 4 (Observable Service Bus) forecloses agent integration by coupling JavaFX types into the backend. Approach 3 achieves the three core guideline goals (agent-ready API, no JavaFX in backend, typed sealed ModeParameters) while retaining the existing FXML investment and allowing one-controller-at-a-time migration.

**How to apply:** When designing any new implementation plan for the V2-to-UI connection, design toward the Approach 3 structure:
- `app-api` module: `SessionApi`, `ModeApi<P>`, sealed `ModeParameters`, `TaskHandle<T>`, `CommandResult`, `ValidationResult`
- `app-backend` module: `RenameSession`, `RenameSessionService`, `BackendExecutor` (no javafx.* imports)
- `FxStateMirror` lives in `app-ui`; only publish methods called from backend via `Platform.runLater`
- Mode controllers keep their FXML; refactor to push typed `ModeParameters` records through `ModeApi<P>`
- `ModeDescriptor`/`FieldDescriptor`/`ModeViewFactory` deferred to Phase 3 (agent schema auto-generation)

**Three-phase migration plan:**
- Phase 1: New modules + abstractions (no V1 code deleted; app stays on V1)
- Phase 2: Migrate controllers one at a time (dual-path via `ModeControllerV2Api` instanceof check)
- Phase 3: Agent adapter, remove V1 artifacts, add undo/redo

**Key constraint discovered:** `FileRenameOrchestratorImpl.execute()` takes `Object config` at runtime and pattern-matches via `instanceof` for type safety. Converting sealed `ModeParameters` records to the appropriate V2 `*Config` objects is centralized in `RenameSessionService` — one conversion per mode, not per controller.
