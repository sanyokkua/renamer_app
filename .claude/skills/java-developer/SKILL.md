---
name: java-developer
description: Java coding standards for the Renamer App — V2 immutable models with @Builder(setterPrefix = "with"), Guice DI, JPMS exports, virtual threads, and project conventions. Use when writing or reviewing any Java code.
allowed-tools: Read, Grep, Glob
---

# Java Developer — Renamer App

## This Project's Stack

- **Java 25** with JPMS (`module-info.java`) — always export new packages
- **Google Guice 7** — constructor injection via `@RequiredArgsConstructor(onConstructor_ = {@Inject})`
- **Lombok 1.18.42** — `lombok.config` sets `addNullAnnotations=jakarta`, `addLombokGeneratedAnnotation=true`
- **V2 models**: `@Value @Builder(setterPrefix = "with")` — builders use `.withFieldName()` not `.fieldName()`
- No Spring, no JPA, no JSON — this is a pure JavaFX desktop app

Supporting files:

- [examples.md](examples.md) — complete code templates
- [logging.md](logging.md) — SLF4J log levels, placeholders, exception logging, hot path rules
- [javadoc.md](javadoc.md) — Javadoc on all public/protected, tag ordering, {@link}, prohibited practices
- [dependencies.md](dependencies.md) — approved libraries with versions, per-library rules, prohibited list

---

## Core Coding Rules

**Be clear, not clever.** Code is read far more than written.

**Immutability by default:**

- V2 models: always `@Value @Builder(setterPrefix = "with")` — never mutate after construction
- V1 models: `FileInformation` is intentionally mutable (preparation commands modify it)
- Fields `final` unless mutation is explicitly required

**Constructor injection only:**

```java
// CORRECT for this project
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MyService {
    private final Dependency1 dep1;
    private final Dependency2 dep2;
}

// NEVER use field injection
@Inject private Dependency1 dep1; // WRONG
```

**V2 model builder pattern (critical — non-default prefix):**

```java
// CORRECT
PreparedFileModel result = PreparedFileModel.builder()
    .withOriginalFile(file)      // withFieldName, not fieldName
    .withNewName(newName)
    .withHasError(false)
    .build();

// WRONG — default prefix doesn't work here
PreparedFileModel.builder().originalFile(file).build(); // compile error
```

---

## Java Type System

- **Prefer `record`** for immutable data carriers — MUST NOT use Lombok `@Data`/`@Value` for new pure data classes
- **Use `sealed`** for closed type hierarchies with a known, fixed set of subtypes
- **Pattern matching**: use `instanceof` patterns (JDK 16+) and `switch` patterns (JDK 21+) instead of manual casting
- **`Optional`**: return type only — MUST NOT use as field, parameter, collection element, or record component
- MUST NOT call `Optional.get()` without a preceding `isPresent()` check — use `orElse`, `orElseThrow`, or `map`
- **Streams**: max 5 chained intermediate operations; extract complex pipelines to named methods
- **Nesting depth**: max 3 levels — use guard clauses (early returns) to flatten
- **No checked exceptions** in app code — wrap library checked exceptions at the adapter layer

---

## Module Rules

- `app/core`: business logic only — no JavaFX imports allowed
- `app/ui`: JavaFX + controllers — depends on `app/core`
- `app/utils`: standalone library — **never add it as a dependency in core or ui**
- Every new package must be added to the module's `module-info.java`
- `ua.renamer.app.core.v2.interfaces` and `ua.renamer.app.core.v2.exception` are intentionally NOT exported

---

## Package Naming

Base: `ua.renamer.app`

| Purpose                   | Package                                         |
|---------------------------|-------------------------------------------------|
| V2 transformation configs | `ua.renamer.app.core.v2.model.config`           |
| V2 transformers           | `ua.renamer.app.core.service.transformation`    |
| V2 metadata extractors    | `ua.renamer.app.core.v2.mapper.strategy.format` |
| UI controllers            | `ua.renamer.app.ui.controller.mode.impl`        |
| DI modules (core)         | `ua.renamer.app.core.config`                    |
| DI modules (UI)           | `ua.renamer.app.ui.config`                      |

Test file naming: `*Test.java` = unit tests, `*IT.java` = integration tests (real files).

---

## DI Patterns

**Module locations:**

- `app/ui/.../config/`: `DIAppModule`, `DICoreModule`, `DIUIModule`, `InjectQualifiers`
- `app/core/.../config/`: `DIV2ServiceModule` only

**DI startup chain:**

```
Guice.createInjector(DIAppModule, DICoreModule, DIUIModule)
  DICoreModule installs DIV2ServiceModule
```

**Adding a new UI mode requires a new qualifier in `InjectQualifiers.java`** — 10 FXMLLoaders, 10 Parents, 10
ModeControllerApis are registered with different `@jakarta.inject.Qualifier` annotations to disambiguate.

**Provider methods for complex wiring:**

```java
@Provides
@Singleton
MyTransformer provideMyTransformer(Dep1 dep1, Dep2 dep2) {
    return new MyTransformer(dep1, dep2);
}
```

---

## Threading

- **UI Thread**: All scene graph reads/writes. Use `Platform.runLater()` from background threads.
- **Background**: Single-threaded `ExecutorService` (daemon) runs `javafx.concurrent.Task<V>`.
- **V2 parallel**: `Executors.newVirtualThreadPerTaskExecutor()` for metadata extraction and rename phases.
- **V1 parallel**: `parallelStream()` inside commands — `processItem()` must be stateless and thread-safe.
- **Progress callbacks**: Always null-check — `if (callback != null) callback.updateProgress(current, max)`.

---

## Error Handling

- Never throw from `processItem()` / `transform()` to callers
- Capture errors in model fields: `RenameModel.hasRenamingError`, `PreparedFileModel.hasError`, `RenameResult.status`
- Use SLF4J `@Slf4j` — never `System.out.println()`
- Never log AND rethrow — pick one

---

## Naming & Style

- Classes: `UpperCamelCase`, nouns
- Methods: `lowerCamelCase`, verbs; boolean accessors: `is`/`has`/`can` prefix
- Constants (`static final` deeply immutable): `UPPER_SNAKE_CASE`
- Loggers (`static final Logger`): `lowerCamelCase`
- No magic numbers — extract to named constants
- Methods ≤ 30 lines; refactor if > 50 lines
- No empty catch blocks

---

## What NOT to Do

- Never add Spring, JPA, Jackson, MapStruct, Resilience4j — wrong stack entirely
- Never use `java.util.Date`, `java.util.Calendar` — use `java.time`
- Never use `java.io.File` for new path operations — use `java.nio.file.Path`
- Never use `@Data` on Lombok models — use `@Value` (immutable) or explicit `@Getter @Setter`
- Never use `synchronized` in new code — use `java.util.concurrent` utilities
- Never call `UUID.randomUUID()` in production logic without an abstraction
- Never use field injection (`@Inject private field`)
- Never return `null` from methods — return `Optional` or empty collections

---

## Javadoc Standards

Document public API: what it does (not how), parameters (with constraints), return (success + failure), exceptions (
trigger conditions).

```java
/**
 * Transform a file's name according to the provided configuration.
 *
 * @param file   the source file model; must not be null
 * @param config the transformation configuration; must not be null
 * @return the transformation result; never null; hasError is true if transformation failed
 */
```

Never use marketing language ("powerful", "robust", "blazing fast"). Never document implementation details in Javadoc.

---

## Checklist Before Finishing

- [ ] Uses constructor injection (`@RequiredArgsConstructor(onConstructor_ = {@Inject})`)
- [ ] V2 model builders use `.withFieldName()` prefix
- [ ] No exceptions thrown to callers from pipeline methods
- [ ] New packages added to `module-info.java`
- [ ] `processItem()` / `transform()` is stateless and thread-safe
- [ ] Progress callbacks null-checked
- [ ] No Spring/JPA/Jackson imports
- [ ] SLF4J `@Slf4j` used for logging
