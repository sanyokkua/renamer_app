# Java Developer — Approved Libraries

Dependency rules for the Renamer App. See [SKILL.md](SKILL.md) for general Java rules.

---

## Critical Rules

- MUST NOT add Spring, JPA, Jackson, MapStruct, Resilience4j, Caffeine, Testcontainers, or any web/persistence framework
- MUST use `java.time` for all date/time — MUST NOT use `java.util.Date` or `java.util.Calendar`
- MUST use `java.nio.file.Path` / `java.nio.file.Files` — MUST NOT use `java.io.File` in new code
- MUST use `java.util.concurrent` utilities — MUST NOT use `synchronized` in new code
- Tech Lead approval required before adding any new third-party dependency

---

## Approved Libraries (all in `app/pom.xml`)

### Core Framework

| Library | Version | Role |
|---------|---------|------|
| **JavaFX** (OpenJFX) | 25.0.1 | UI framework (controls, fxml, base) |
| **Google Guice** | 7.0.0 | Dependency injection |
| **Lombok** | 1.18.42 | Code generation (`@Value`, `@Builder`, `@Slf4j`, `@RequiredArgsConstructor`) |
| **Jakarta Inject API** | 2.0.1 | `@Inject`, `@Singleton`, `@Qualifier` |
| **Jakarta Annotation API** | 3.0.0 | `@Nullable`, `@NonNull` |

### Metadata & File Processing

| Library | Version | Role |
|---------|---------|------|
| **Apache Tika** (core + parsers) | 3.2.3 | MIME type detection; file format identification |
| **metadata-extractor** (drewnoakes) | 2.19.0 | EXIF/IPTC/XMP reading from images/videos |
| **jaudiotagger** (RouHim fork) | 2.0.10 | ID3/Vorbis tag reading from audio files |
| **Apache Commons IO** | 2.18.0 | `FileUtils`, `FilenameUtils`, `IOUtils` |

### Utilities

| Library | Version | Role |
|---------|---------|------|
| **Google Guava** | 33.5.0-jre | Collections utilities (`ImmutableList`, `Preconditions`) |
| **JSpecify** | 1.0.0 | Null-safety annotations (`@Nullable`, `@NonNull`) |
| **SLF4J API** | 2.0.17 | Logging facade |
| **Logback Classic** | 1.5.21 | SLF4J implementation |

### Testing

| Library | Version | Role |
|---------|---------|------|
| **JUnit 5 (Jupiter)** | 6.0.1 | Test framework |
| **Mockito** | 5.20.0 | Mocking |
| **AssertJ** | (via JUnit BOM) | Fluent assertions |

---

## Usage Rules Per Library

### Lombok
- V2 models: `@Value @Builder(setterPrefix = "with")` — builders use `.withFieldName()` (non-default, required)
- Services: `@Slf4j` for logger, `@RequiredArgsConstructor(onConstructor_ = {@Inject})` for injection
- MUST NOT use `@Data` on V2 models — use `@Value` for immutable, explicit `@Getter @Setter` for mutable
- `lombok.config` at `app/` root: `addNullAnnotations=jakarta`, `addLombokGeneratedAnnotation=true`

### Guice
- Four modules: `DIAppModule`, `DICoreModule`, `DIUIModule` (in `app/ui`), `DIV2ServiceModule` (in `app/core`)
- `@Provides @Singleton` for complex wiring; `@RequiredArgsConstructor` for simple construction
- `InjectQualifiers` holds 30 `@jakarta.inject.Qualifier` annotations disambiguating same-type bindings
- Use `TypeLiteral<>` for generic type bindings

### Apache Tika
- Used only in `FilesOperations` (V1) and `ThreadAwareFileMapper` (V2) for MIME type detection
- Inject as a singleton — MUST NOT create `new Tika()` in multiple places

### metadata-extractor
- Used in V2 extractor strategies under `app/core/.../v2/mapper/strategy/format/`
- Read-only — never write EXIF via this library (use ExifTool CLI for test data, see `/use-exiftool-metadata`)

### Apache Commons IO
- `FilenameUtils.getExtension()` for file extension extraction
- `FileUtils.deleteDirectory()` for recursive deletion (tests only)
- MUST NOT use for MIME detection — use Tika instead

### Guava
- MAY use for collection utilities (`ImmutableList.of()`, `Preconditions.checkNotNull()`)
- MUST NOT use Guava I/O (`com.google.common.io`) — use `java.nio.file` instead
- MUST NOT use Guava Cache — no caching layer in this app

---

## Prohibited Libraries

| Pattern | Reason |
|---------|--------|
| `spring-*` | No Spring in any form |
| `hibernate-*`, `jakarta.persistence` | No JPA/ORM — no database |
| `jackson-*`, `gson`, `org.json` | No JSON — app has no JSON data |
| `mapstruct` | No MapStruct |
| `resilience4j-*` | No circuit breakers |
| `caffeine` | No cache layer |
| `testcontainers-*` | No DB containers — no database |
| `javax.swing.*`, `org.eclipse.swt.*` | No Swing/SWT — JavaFX only |

---

## Adding a New Dependency

Before adding any new library:
1. Confirm no JDK 25 native API covers the need
2. Check if an existing approved library can be extended
3. Verify: maintained within 12 months, no critical CVEs, Apache 2.0 / MIT / BSD license
4. Get Tech Lead approval; document in PR description
