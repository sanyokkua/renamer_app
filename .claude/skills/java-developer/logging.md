# Java Developer — Logging Standards

SLF4J + Logback logging rules for the Renamer App. See [SKILL.md](SKILL.md) for general Java rules.

---

## Critical Rules

- Log exclusively through SLF4J 2.x via Lombok `@Slf4j` — MUST NOT import `ch.qos.logback`, `org.apache.logging.log4j`, or `java.util.logging` in app code
- MUST NOT use `System.out.println()`, `System.err.println()`, or `e.printStackTrace()`
- MUST use SLF4J `{}` parameterized placeholders — MUST NOT use string concatenation in log arguments
- Exactly one action per caught exception: log it OR rethrow it — MUST NOT do both; MUST NOT silently swallow
- `ERROR` is reserved for unexpected failures only — MUST NOT use for expected business outcomes (user validation, cancellations)
- NEVER log passwords, tokens, API keys, or authentication secrets at any level

---

## Log Level Guide

| Level | When to use | Example |
|-------|-------------|---------|
| `ERROR` | Unexpected failure the app cannot self-correct; requires investigation | Unhandled exception, data corruption |
| `WARN` | Unexpected condition handled gracefully; may indicate latent problem | Missing optional config, deprecated format auto-converted |
| `INFO` | Significant event completed normally | File saved, export completed, app started |
| `DEBUG` | Developer diagnostics: variable state, decision branches | Parsed file structure, selected code path |
| `TRACE` | Fine-grained execution flow: per-element processing | Each row processed in a loop |

Default: `INFO` in release, `DEBUG` during development.

---

## Logger Declaration

```java
@Slf4j
public class MyTransformer {
    public void process(Path file) {
        log.info("Processing file: {}", file.getFileName());
    }
}
```

MUST NOT declare loggers manually when Lombok is available.

---

## Message Construction

```java
// CORRECT — parameterized placeholders
log.info("File loaded: {} ({} bytes)", fileName, fileSize);

// WRONG — string concatenation
log.debug("Processing file " + fileName + " at " + path);
```

Guard expensive computations:

```java
if (log.isDebugEnabled()) {
    log.debug("Document structure: {}", document.toTreeString());
}
```

---

## Exception Logging

Pass `Throwable` as the **last** argument — never inside `{}`:

```java
log.error("Failed to save file: {}", filePath, e);   // CORRECT
log.error("Failed: {}", e.getMessage());              // WRONG — loses stack trace
```

One action per catch block:

```java
// CORRECT — log and handle
catch (IOException e) {
    log.error("Failed to save document: {}", filePath, e);
    showErrorDialog("Could not save the file.");
}

// CORRECT — rethrow for caller to handle
catch (IOException e) {
    throw new DocumentSaveException("Write failed for " + filePath, e);
}

// WRONG — log AND rethrow
catch (IOException e) {
    log.error("Write failed", e);
    throw e;   // duplicate log entries up the stack
}
```

---

## Events to Log

### MUST log
- Application lifecycle: start, shutdown, stage shown
- Unhandled exceptions (uncaught exception handler)
- File operation results: opened, saved, exported, imported
- Critical operation failures: write failed unexpectedly, required resource missing

### PREFER logging
- User-facing error recovery (`WARN`): invalid file format handled with fallback
- Background task lifecycle (`INFO`): Task started/completed with duration
- Business logic decisions (`DEBUG`): file format version detected

### MUST NOT log
- Every method entry/exit at `INFO` (use `DEBUG`/`TRACE` only when troubleshooting)
- Routine UI events (button clicks, mouse moves) unless debugging UI
- Successful no-op operations at `INFO` or above

---

## JavaFX Thread Considerations

Install global uncaught exception handler in `Application.start()`:

```java
Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
    log.error("Uncaught exception on thread {}", thread.getName(), throwable)
);
```

Log Task failures in `onFailed` handler:

```java
task.setOnFailed(event ->
    log.error("Background task failed: {}", task.getTitle(), task.getException())
);
```

MUST NOT log expensive `toString()` calls or large data structures on the FX Application Thread — defer to background thread or guard with level check.

---

## Hot Path Restrictions

MUST NOT log at `INFO` or above inside tight loops. Emit a single summary after:

```java
for (FileModel file : files) {
    if (log.isTraceEnabled()) {
        log.trace("Processing: {}", file.getName());
    }
    process(file);
}
log.info("Processed {} files", files.size());
```

MUST NOT log inside JavaFX layout cycles (`layoutChildren()`, `updateItem()`, property listeners on frequently-changing values).

---

## Logback Configuration

Console pattern (include in `logback.xml`):
```
%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

Suppress framework noise:
```xml
<logger name="javafx" level="WARN"/>
<logger name="com.sun" level="WARN"/>
<logger name="jdk" level="WARN"/>
```
