---
name: Dependency Injection Reference Documentation
description: Complete technical reference for Google Guice 7 dependency injection in the Renamer App, including module hierarchy, constructor injection patterns, UI mode registration, and recipes for adding new bindings
type: project
---

# Dependency Injection Reference Documentation

**Created:** 2026-04-09  
**Location:** `docs/developers/architecture/dependency-injection.md`

## What was documented

Complete technical reference for the Renamer App's Google Guice 7 dependency injection system.

### Content delivered

1. **Why Guice** — Lightweight, explicit wiring, constructor injection enforced
2. **Module Wiring Graph** — Mermaid diagram showing actual injector composition with DIAppModule, DICoreModule, DIUIModule, DIBackendModule, DIV2ServiceModule, and DIMetadataModule
3. **Per-Module Responsibility Table** — 6 modules, Maven artifacts, key bindings, and scopes
4. **Constructor Injection Pattern** — Standard `@RequiredArgsConstructor(onConstructor_ = {@Inject})` with code examples
5. **UI Mode Registration** — Registry pattern (not `@Named` qualifiers) for wiring 10 mode controllers to FXML views
6. **Adding New Bindings** — 4 code recipes with file locations:
   - New singleton service (interface → impl)
   - New transformer (DIV2ServiceModule + dispatcher)
   - New metadata extractor (DIMetadataModule + category dispatcher)
   - New UI mode (DIUIModule: 2 changes to bindViewControllers + provideModeViewRegistry)
7. **Testing Guice Modules** — DIBackendModuleTest pattern with callout box
8. **Cross-References** — Links to pipeline-architecture.md and add-transformation-mode.md guides
9. **Troubleshooting Table** — 5 common Guice errors with causes and solutions

### Key technical decisions captured

- **Composition root is DIUIModule** — Pulls in all other modules via install() calls; allows DIBackendModule to be tested independently with mock StatePublisher
- **Registry pattern for modes** — No @Named or @Qualifier annotations needed; ModeViewRegistry maps TransformationMode → (Parent supplier, ModeControllerV2Api)
- **StatePublisher intentionally unbound in DIBackendModule** — DIUIModule provides the JavaFX-aware implementation in production
- **All bindings are singletons** — Safe because transformers, extractors, and mappers are stateless and thread-safe
- **Virtual threads for concurrency** — Handled at service layer, not in DI configuration

### Validation performed

- ✅ All module class names and package paths verified against source via Grep
- ✅ All bindings verified in actual module implementations
- ✅ Injector creation pattern verified in RenamerApplication.main()
- ✅ Test pattern verified in DIBackendModuleTest.java
- ✅ Mermaid syntax validated per create-mermaid-diagrams standards
- ✅ Constructor injection pattern verified in 10+ services
- ✅ UI mode registration pattern verified in DIUIModule.provideModeViewRegistry()

### Documentation style

- Technical reference format (tables, code blocks, recipes)
- Imperative mood: "Bind the service", "Create a real Guice injector"
- Javadoc summary style in code examples
- No emojis, no filler
- First-use definitions: Guice, DI, singleton, constructor injection, registry pattern
- Cross-references to related documentation

### Related documentation

This doc supports and complements:
- `docs/developers/architecture/pipeline-architecture.md` — How FileRenameOrchestrator (bound in DIV2ServiceModule) drives the pipeline
- `/add-transformation-mode` skill — Includes DI binding steps
- CLAUDE.md — Lists module locations and composition root pattern
