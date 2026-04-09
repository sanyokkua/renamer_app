# How to Build & Package

This guide covers every workflow a developer needs: compiling, testing, linting, running locally, and building native
installers with jpackage.

---

## 1. Prerequisites

### JDK

| Platform      | JDK Required                                                         | Why                                                                                                                                                                                                      |
|---------------|----------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| macOS         | Any JDK 25 distribution (Corretto, Temurin, Liberica)                | JavaFX native libraries are embedded in platform-specific JARs on the classpath — jpackage resolves them via jlink without needing Full JDK                                                              |
| Linux (ARM64) | Any JDK 25 distribution (Corretto, Temurin, BellSoft, OpenJDK, etc.) | jpackage resolves JavaFX from the modular JARs in `target/libs/` — no special JDK needed, same mechanism as macOS. Verified with BellSoft, Corretto, Temurin, OpenJDK.                                   |
| Windows (x64) | Any x64 JDK 25 (Temurin, Liberica, Zulu, Corretto, etc.)             | Maven resolves JavaFX as platform-specific JARs — same mechanism as macOS/Linux. ARM64 Windows is not supported (OpenJFX has no win-aarch64 Maven artifacts). Verified with Liberica, Temurin, and Zulu. |

**Verify on Linux:**

```bash
java -version   # any JDK 25 vendor works
```

**Verify on Windows:**

```bash
java -version   # any JDK 25 vendor works; confirm arch shows amd64 (not aarch64)
```

**Installing JDK 25 on Linux (ARM64) — any vendor works:**

```bash
# Ubuntu/Debian — OpenJDK
sudo apt-get install openjdk-25-jdk

# Eclipse Temurin (via SDKMAN)
sdk install java 25-tem

# Amazon Corretto
sudo apt-get install java-25-amazon-corretto-jdk

# BellSoft Liberica (standard edition also works)
sudo apt-get install bellsoft-java25
```

**Installing on macOS (example — Corretto via direct download):**

Download from the AWS Corretto 25 page (select macOS, the appropriate architecture). Install the `.pkg` file.

### Maven

All platforms: Apache Maven 3.9+. No Maven Wrapper (`mvnw`) is provided — install Maven and add it to `PATH`.

**Verify:**

```bash
mvn --version   # must show 3.9.x or higher
```

### Platform-Specific Packaging Tools

| Platform | Tool                     | Required for             | Install                                                 |
|----------|--------------------------|--------------------------|---------------------------------------------------------|
| macOS    | Xcode Command Line Tools | `.app` bundle and `.dmg` | `xcode-select --install`                                |
| Linux    | `fakeroot`               | `.deb` package           | `sudo apt-get install -y fakeroot`                      |
| Windows  | WiX Toolset 3.x          | `.msi` installer         | Download from [wixtoolset.org](https://wixtoolset.org/) |

`fakeroot` is optional on Linux — `package-linux.sh` warns and builds only the app-image if it is absent. WiX is not yet
integrated into `package-windows.bat` (see Section 5).

---

## 2. Development Build Commands

All commands run from the `app/` directory. `app/.mvn/maven.config` automatically applies `-B --no-transfer-progress` to
every Maven invocation in this directory — no need to type those flags manually.

| Command                                                 | Purpose                               | Notes                                               |
|---------------------------------------------------------|---------------------------------------|-----------------------------------------------------|
| `mvn compile -q -ff`                                    | Compile all modules                   | Fastest feedback loop                               |
| `mvn test -q -ff -Dai=true`                             | Run all tests                         | `-ff` stops on first module failure                 |
| `mvn test -q -ff -Dai=true -Dtest=ClassName`            | Run one test class                    | Replace `ClassName` with the simple class name      |
| `mvn test -q -ff -Dai=true -Dtest=ClassName#methodName` | Run one test method                   | Use `#` to separate class and method                |
| `mvn clean test jacoco:report -Dai=true`                | Generate coverage report              | HTML report in `target/site/jacoco/` of each module |
| `mvn verify -Pcode-quality -q`                          | Checkstyle + PMD + SpotBugs           | Violations are informational — build does not fail  |
| `../scripts/ai-build.sh`                                | Full pipeline: compile → lint → tests | Recommended before committing                       |
| `mvn clean install -q`                                  | Build and install all modules         | Needed before jpackage (see Section 5)              |

**Flags explained:**

| Flag             | Meaning                                             |
|------------------|-----------------------------------------------------|
| `-q`             | Quiet output — suppresses INFO lines                |
| `-ff`            | Fail fast — stops on first module failure           |
| `-Dai=true`      | Activates test configuration for AI/CI environments |
| `-Pcode-quality` | Activates Checkstyle, PMD, and SpotBugs profiles    |

---

## 3. Running the App Locally

```bash
cd app/ui
mvn javafx:run
```

Run from the `app/ui/` subdirectory, not the `app/` root. The `javafx-maven-plugin` is configured only in the `ui`
module POM.

**macOS dock name:** A `macos-dock` Maven profile auto-activates on macOS and injects `-Xdock:name=Renamer` as a JVM
argument, so the app appears correctly in the Dock. This profile is excluded on Linux and Windows — no action needed.

---

## 4. Full Build Script

`scripts/ai-build.sh` runs the full quality pipeline in a fixed sequence, stopping on compile or test failure but
treating linting as informational:

```
compile → Checkstyle → PMD → SpotBugs → test
```

**Step-by-step:**

| Step           | Command                               | Stops on failure? | Output                           |
|----------------|---------------------------------------|-------------------|----------------------------------|
| 1 — Compile    | `mvn compile`                         | Yes               | —                                |
| 2 — Checkstyle | `mvn checkstyle:check -Pcode-quality` | No                | Violations logged as `[WARN]`    |
| 3 — PMD        | `mvn pmd:check -Pcode-quality`        | No                | Violations logged as `[WARNING]` |
| 4 — SpotBugs   | `mvn spotbugs:check -Pcode-quality`   | No                | Violations logged as `[ERROR]`   |
| 5 — Tests      | `mvn test -Dai=true`                  | Yes               | —                                |

All three linting tools have `failOnViolation=false` in the parent `pom.xml` — they report violations but do not fail
the build. Review the output and address violations before submitting a pull request.

**Run from any directory:**

```bash
# From project root:
./scripts/ai-build.sh

# From app/ directory:
../scripts/ai-build.sh
```

The script's first line (`cd "$(dirname "$0")/../app"`) normalises the working directory regardless of where it is
invoked from.

---

## 5. Packaging with jpackage

jpackage produces self-contained native installers. Each installer bundles a trimmed JRE — end users need no Java
installation.

### Pre-package build (all platforms)

Run this first to assemble the application JARs:

```bash
cd app
mvn clean package -DskipTests
```

This populates `app/ui/target/libs/` with the main JAR and all runtime dependency JARs. The packaging scripts read from
this directory.

### Common jpackage configuration

`scripts/jpackage-common.sh` is sourced by all platform scripts and defines shared settings:

| Setting          | Value                                                             |
|------------------|-------------------------------------------------------------------|
| Input directory  | `app/ui/target/libs/`                                             |
| Output directory | `dist/`                                                           |
| Main class       | `ua.renamer.app.Launcher`                                         |
| JVM flags        | `--enable-preview`, `-Xmx512m`                                    |
| jlink options    | `--strip-debug --no-header-files --no-man-pages --compress zip-6` |
| App version      | Read dynamically from `pom.xml` via `mvn help:evaluate`           |

### macOS

**Script:** `scripts/package-macos.sh`  
**Required tool:** Xcode Command Line Tools  
**Icon:** `icon.icns` (project root)

```bash
./scripts/package-macos.sh
```

The script runs jpackage twice:

1. `--type app-image` → `dist/Renamer.app` (~390–410 MB, raw bundle)
2. `--type dmg` → `dist/Renamer-<version>.dmg` (~200–220 MB, installer)

**`dist/` layout:**

```
dist/
├── Renamer.app/
│   └── Contents/
│       ├── Info.plist
│       ├── MacOS/Renamer          (native launcher)
│       ├── Resources/icon.icns
│       ├── app/                   (JARs + config)
│       └── runtime/               (embedded JRE)
└── Renamer-2.0.0.dmg
```

**Gatekeeper (unsigned app):** The app is not code-signed. macOS Gatekeeper blocks it on first launch. To allow it, use
one of:

- **Right-click → Open** in Finder — accepts a one-time prompt without touching the terminal
- **Remove the quarantine flag** (most reliable after dragging from a DMG):
  ```bash
  xattr -rd com.apple.quarantine /Applications/Renamer.app
  ```
- **System Settings → Privacy & Security** — scroll to the blocked app entry and click **Open Anyway**

### Linux

**Script:** `scripts/package-linux.sh`  
**Required tool:** `fakeroot` (for `.deb` only)  
**Icon:** `icon.png` (project root)

```bash
./scripts/package-linux.sh
```

The script builds:

1. `dist/Renamer/` — app-image (raw executable directory)
2. `dist/renamer_<version>_<arch>.deb` (~221 MB) — only if `fakeroot` is installed; the script warns and skips the
   `.deb` step if `fakeroot` is absent.

**`dist/` layout:**

```
dist/
├── Renamer/
│   ├── bin/Renamer                (ELF binary)
│   └── lib/
│       ├── app/                   (JARs + config)
│       └── runtime/               (embedded JRE)
└── renamer_2.0.0_arm64.deb
```

### Windows

**Script:** `scripts/package-windows.bat`  
**Required JDK:** Any x64 JDK 25 — ARM64 Windows is not supported  
**Icon:** `icon.ico` (project root)

Run from a Command Prompt or PowerShell window:

```bat
scripts\package-windows.bat
```

The script builds `dist\Renamer\` (app-image, ~400 MB). An MSI installer step is not yet implemented in the script; the
app-image can be distributed as a zip archive.

**`dist\` layout:**

```
dist\
└── Renamer\
    ├── Renamer.exe                (native launcher)
    ├── Renamer.ico
    ├── app\                       (JARs + config)
    └── runtime\                   (embedded JRE)
```

> Running `ai-build.sh` on Windows requires Git Bash or WSL. From Git Bash:
> ```bash
> cd /c/path/to/renamer_app
> ./scripts/ai-build.sh
> ```

---

## 6. jdeploy (Historical Note)

The project previously used [jdeploy](https://www.jdeploy.com/) — a tool that packages Java apps as thin launchers that
download the JRE on first run. jdeploy required a `package.json` configuration file at the project root.

The project has migrated to jpackage, which produces fully self-contained installers. The `package.json` file and
jdeploy configuration have been removed. All packaging now uses the `scripts/` directory described in Section 5.

---

## 7. Quick Reference Cheat Sheet

| Purpose                            | Command                                             | Directory    |
|------------------------------------|-----------------------------------------------------|--------------|
| Compile                            | `mvn compile -q -ff`                                | `app/`       |
| All tests                          | `mvn test -q -ff -Dai=true`                         | `app/`       |
| One test class                     | `mvn test -q -ff -Dai=true -Dtest=MyClass`          | `app/`       |
| One test method                    | `mvn test -q -ff -Dai=true -Dtest=MyClass#myMethod` | `app/`       |
| Coverage report                    | `mvn clean test jacoco:report -Dai=true`            | `app/`       |
| Lint (Checkstyle + PMD + SpotBugs) | `mvn verify -Pcode-quality -q`                      | `app/`       |
| Full pipeline                      | `../scripts/ai-build.sh`                            | `app/`       |
| Build all modules                  | `mvn clean install -q`                              | `app/`       |
| Run the app                        | `mvn javafx:run`                                    | `app/ui/`    |
| Prepare for packaging              | `mvn clean package -DskipTests`                     | `app/`       |
| Package — macOS                    | `./scripts/package-macos.sh`                        | project root |
| Package — Linux                    | `./scripts/package-linux.sh`                        | project root |
| Package — Windows                  | `scripts\package-windows.bat`                       | project root |
