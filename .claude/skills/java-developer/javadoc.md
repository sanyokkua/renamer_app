# Java Developer — Javadoc Standards

Javadoc rules for the Renamer App. See [SKILL.md](SKILL.md) for general Java rules.

---

## Critical Rules

- MUST provide complete Javadoc on every `public` and `protected` class, interface, method, field, and constructor
- Javadoc (`/** */`) = contract documentation only (what, inputs, outputs, errors) — MUST NOT describe implementation details
- Implementation comments (`//`, `/* */`) = internal logic explanations
- MUST NOT write noise: no restating what code expresses, no empty tag descriptions, no `@param`/`@return` repeating the parameter name
- MUST NOT use `@author` tags — Git history is authoritative
- Every package MUST have a `package-info.java` with Javadoc
- MUST use `{@link}` instead of plain-text class or method names in Javadoc
- Delete stale documentation in the same commit as behavioral changes

---

## Block Structure

Order in every Javadoc block:
1. Summary fragment (third-person verb phrase: "Calculate the tax…")
2. Detailed description (if needed)
3. Block tags

Summary fragment rules:
- MUST NOT begin with "This method…", "This class…", "A…", or "The…"
- MUST be capitalized, end with a period
- MUST NOT begin with `@return` alone

---

## Tag Ordering

When block tags are present, use this order:
1. `@param` (in parameter declaration order)
2. `@return`
3. `@throws` / `@exception` (by likelihood)
4. `@see`
5. `@since` (library projects only — MUST NOT use in app code)
6. `@deprecated`

---

## Essential Tag Rules

### `@param`
- MUST provide for every parameter; include constraints (nullability, range, valid values)
- MUST NOT leave empty or restate the parameter name

```java
/**
 * @param file   the source file; must not be null
 * @param config the transformation configuration; must not be null
 */
```

### `@return`
- Every non-`void` method MUST have `@return`; describe both success AND failure/empty states
- `Optional<T>`: "the X if found, or empty if not found"
- Collection: "list of X, never null; empty if none found"
- Boolean: "`{@code true}` if X, `{@code false}` otherwise"

### `@throws`
- MUST document all checked exceptions and significant runtime exceptions (`IllegalArgumentException`, `NullPointerException`)
- Every `@throws` MUST specify the **condition** that causes the exception

```java
/**
 * @throws IllegalArgumentException if amount is negative or zero
 * @throws IllegalStateException    if the service is not initialized
 */
```

---

## Documentation Obligation Levels

| Component | Level |
|-----------|-------|
| Public classes, interfaces | MUST — full Javadoc |
| Public methods | MUST — all applicable tags |
| Public fields / constants | MUST — business meaning |
| Protected members | MUST — part of extension API |
| Private methods | Only if complex (>10 lines) — use `//` comments |
| Standard getters/setters | MUST NOT document unless they contain logic |
| Overriding methods | MUST NOT duplicate parent Javadoc — use `{@inheritDoc}` or omit |

---

## Component-Specific Rules

### Interfaces
- Document with highest priority; include thread-safety requirements and expectations for implementers

### Records
- Document record components using `@param` tags in class-level Javadoc

```java
/**
 * Represent a renamed file result.
 *
 * @param originalFile the source file; never null
 * @param newName      the target name after transformation; never null or empty
 * @param status       the outcome of the rename operation; never null
 */
public record RenameResult(FileModel originalFile, String newName, RenameStatus status) { }
```

### Enums
- Class-level Javadoc on the enum type; each constant MUST document its business meaning

```java
/**
 * Represent the outcome of a file rename operation.
 */
public enum RenameStatus {
    /** File was successfully renamed to the new name. */
    SUCCESS,
    /** Rename was skipped because the new name equals the original name. */
    SKIPPED,
    /** Rename failed due to a file system error. See {@link RenameResult#errorMessage()}. */
    ERROR
}
```

### Constants
- Every public constant MUST explain its business meaning; if the value is not self-evident, explain why that value

### `module-info.java`
- PREFER a Javadoc block documenting the module's purpose and exported packages

---

## Linking and Formatting

```java
// Class reference
{@link FileTransformationService}

// Method in same class
{@link #transform(FileModel, Config)}

// Method in other class
{@link RenameOrchestrator#orchestrate(List, Config)}

// Inline code (literals, types, expressions)
{@code true}, {@code null}, {@code List<String>}

// Multi-line code example
<pre>{@code
PreparedFileModel result = transformer.transform(file, config);
assertThat(result.isHasError()).isFalse();
}</pre>
```

---

## Prohibited Practices

- MUST NOT restate what the code expresses through name, type, or structure
- MUST NOT leave `@param`, `@return`, `@throws` descriptions empty
- MUST NOT use `@author` tags
- MUST NOT use `@since` in application code
- MUST NOT have `@Deprecated` without a `@deprecated` tag explaining the migration path
- MUST NOT leave stale documentation (deleted parameters, removed exceptions, changed return types)

---

## TODO / FIXME / HACK Format

```java
// TODO(owner): description [TICKET-ID]
// FIXME(owner): description [TICKET-ID]
// HACK(owner): description [TICKET-ID]
```

- `FIXME` and `HACK` MUST NOT exist without a linked ticket
- `TODO` without a ticket MUST be resolved or ticketed within 30 days
