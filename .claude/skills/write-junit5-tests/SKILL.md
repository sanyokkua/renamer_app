---
name: write-junit5-tests
description: JUnit 5 / AssertJ / Mockito 5 testing patterns for the Renamer App. Use when writing or reviewing tests — what to mock vs instantiate directly, V2 builder syntax, @TempDir, AAA structure, and coverage rules.
allowed-tools: Read, Grep, Glob
---

# JUnit 5 Tests — Renamer App

## Test Stack

- **JUnit 6.0.1** (Jupiter) + **Mockito 5.20.0** + **AssertJ 3.x**
- No Spring, no Testcontainers — pure logic + file system tests
- 1,236+ existing tests across 58+ files — check existing patterns before writing new ones

See [examples.md](examples.md) for complete test class templates.

---

## What to Mock vs. Instantiate Directly

### Mock these (external boundaries / non-deterministic):
- `FilesOperations` — file I/O operations
- System clock / time providers
- External dependencies with side effects

### Never mock these (instantiate directly):
- `FileInformation`, `RenameModel` — mutable V1 domain objects, use real instances
- `FileModel`, `PreparedFileModel`, `RenameResult` — V2 immutable models, use builders
- `FileInformationMetadata`, `FileMeta` — metadata containers
- Collections, `String`, `LocalDateTime`, other standard types
- Pure text transformation functions

---

## Test Naming Pattern

```
methodOrBehavior_stateOrInput_expectedOutcome
```

```java
// CORRECT
void transform_whenFileNameIsEmpty_shouldReturnErrorResult()
void processItem_withNullExtension_shouldPreserveEmptyExtension()
void execute_withDuplicateNames_shouldAppendSuffixes()

// WRONG
void testTransform()
void test1()
```

---

## Test Organization (mirrors production structure)

```
app/core/src/test/java/.../
├── v2/
│   ├── mapper/integration/     # Real file tests per format (JpegExtractorIT)
│   └── service/
│       ├── transformation/     # Transformer unit tests (*Test.java)
│       └── integration/        # End-to-end pipeline tests (*IT.java)
└── service/command/impl/       # V1 command tests
```

Integration tests use suffix `*IT.java`, unit tests use `*Test.java`.

---

## Coverage & Quality Rules

- ≥ 80% branch coverage enforced via JaCoCo
- `@Disabled` MUST have a reason: `@Disabled("JIRA-XXX: reason")`
- No `Thread.sleep()` — use `Awaitility` for async assertions
- No control flow in tests (`if`/`for`/`while`) — use `@ParameterizedTest` instead
- One logical concept per test method
- Hard-coded expected values — never recalculate expected value with same logic as production

---

## Quick Reference: AssertJ Patterns

```java
// Basic
assertThat(result).isEqualTo(expected);
assertThat(result).isNotNull();
assertThat(result.isHasError()).isFalse();

// Collections
assertThat(results).hasSize(3)
    .extracting(PreparedFileModel::getNewName)
    .containsExactly("a", "b", "c");

// Strings
assertThat(result.getNewName()).startsWith("2025").endsWith(".jpg");

// Optionals
assertThat(result.getCreationDate()).isPresent()
    .hasValueSatisfying(dt -> assertThat(dt.getYear()).isEqualTo(2025));

// Exceptions
assertThatThrownBy(() -> method(null))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("must not be null");
```
