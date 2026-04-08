# macOS Build, Test & Packaging Requirements

Verified on: **macOS 26.3.1 (Sequoia)**, CPU **Apple M1 Pro**, architecture **arm64 (aarch64)**.

---

## 1. JDK — any standard JDK 25 works (no Full edition required)

Unlike Windows and Linux, macOS packaging does **not** require the BellSoft Liberica Full edition
with bundled JavaFX jmods. A standard JDK distribution (Amazon Corretto, Temurin, Liberica, etc.)
is sufficient for both building and packaging.

**Verified locally with:** Amazon Corretto 25.0.2 (arm64), managed via jenv.

| Property | Value |
|----------|-------|
| Vendor | Amazon.com Inc. |
| Distribution name | Amazon Corretto 25 |
| Version | 25.0.2 (or later 25.x) |
| Architecture | arm64 |
| `java -version` string | `openjdk version "25.0.2" 2026-01-20 LTS` |
| Install path | `/Library/Java/JavaVirtualMachines/amazon-corretto-25.jdk/Contents/Home` |
| jenv shim | `/Users/ok/.jenv/shims/java` |

> **Also installed (all arm64):** Corretto 11, 17, 21. jenv selects the active version.
> Any arm64 JDK 25 distribution should work.

Install Corretto from the AWS download page — select **macOS**, **ARM64**:
```
https://aws.amazon.com/corretto/
```

Verify:
```bash
java -version        # Must show 25.x
jpackage --version   # Must succeed (bundled with JDK 25)
```

### Why macOS does not need Liberica Full

JavaFX is distributed on Maven Central as platform-specific JARs. On macOS the native libraries
(`libglass.dylib`, `libprism_mtl.dylib`, etc.) are **embedded inside those JARs as classpath
resources** — no separate `.jmod` files are needed.

The full mechanism (all five steps happen automatically during `mvn package` + `jpackage`):

1. **Classifier auto-resolution:** `app/pom.xml` declares `javafx-controls:25.0.2` with no
   classifier. The OpenJFX parent POM (`org.openjfx:javafx:25.0.2`) activates an OS-aware Maven
   profile at build time:
   - `os.name=mac os x` + `os.arch=aarch64` → resolves `javafx-graphics-25.0.2-mac-aarch64.jar`
   - `os.name=mac os x` + `os.arch=x86_64`  → resolves `javafx-graphics-25.0.2-mac.jar`

2. **Native `.dylib` files inside those JARs:** The platform-specific JAR (e.g.
   `javafx-graphics-25.0.2-mac-aarch64.jar`) contains macOS native libraries embedded as classpath
   resources:
   ```
   com/sun/glass/ui/mac/libglass.dylib
   com/sun/prism/libprism_mtl.dylib
   com/sun/prism/libprism_es2.dylib
   com/sun/prism/libdecora_sse.dylib
   com/sun/javafx/font/libjavafx_font.dylib
   ... (8 total in javafx-graphics alone)
   ```
   The generic `javafx-graphics-25.0.2.jar` contains **no `.dylib` files** — only bytecode.

3. **`maven-dependency-plugin` copies all runtime JARs** (including the platform-specific ones)
   to `app/ui/target/libs/` during the `package` phase.

4. **`jpackage --input` detects modular JARs:** `scripts/package-macos.sh` passes
   `--input app/ui/target/libs/`. jpackage scans each JAR for `module-info.class`. JavaFX JARs
   are modular — jpackage's internal `jlink` step resolves the full `javafx.*` module graph
   **from the JARs themselves**, without any `.jmod` files in the JDK's `jmods/` directory.

5. **Runtime loading:** JavaFX's native-library loader extracts the `.dylib` files from the JARs
   at startup and loads them via JNI.

---

## 2. Maven

| Property | Value |
|----------|-------|
| Version | Apache Maven 3.9.12 |
| Install path | `/Users/ok/.UserTools/apache-maven-3.9` |
| Minimum version | 3.9.x |

Maven is **not** provided by a Maven Wrapper — `mvnw` is not present. Install and add to PATH manually.

Install (example):
```bash
wget https://archive.apache.org/dist/maven/maven-3/3.9.x/binaries/apache-maven-3.9.x-bin.tar.gz
tar -xzf apache-maven-3.9.x-bin.tar.gz -C ~/Tools/
export PATH="$HOME/Tools/apache-maven-3.9.x/bin:$PATH"
```

Or via Homebrew:
```bash
brew install maven
```

---

## 3. Build commands (run from `app/` directory)

```bash
cd app/

mvn compile -q -ff                          # Compile only
mvn test -q -ff -Dai=true                   # All tests (quiet, stops on failure)
mvn test -q -ff -Dai=true -Dtest=ClassName  # Single test class
mvn clean install -q                        # Build all modules, install to local repo
mvn verify -Pcode-quality -q                # Full quality check (Checkstyle, PMD, SpotBugs)

../scripts/ai-build.sh                      # Sequential: compile → lint → test
```

> `app/.mvn/maven.config` applies `-B --no-transfer-progress` globally.
>
> The `macos-dock` Maven profile activates automatically on macOS (`<os><family>mac</family></os>`)
> and injects `-Xdock:name=Renamer` as a JVM argument via the `javafx.jvm.args` property.
> This sets the macOS Dock application name during `mvn javafx:run`. The profile is **excluded**
> on Linux and Windows — see [cross-platform-issues.md](cross-platform-issues.md) Issue 1.

---

## 4. Packaging dependencies

Required only for `scripts/package-macos.sh` (not needed for compile/test):

| Tool | Required for | Notes |
|------|-------------|-------|
| `jpackage` | Both `.app` bundle and `.dmg` | Bundled with JDK 25 |
| Xcode Command Line Tools | `.app` bundle creation | See below |
| `icon.icns` | App icon | Present at project root |

### Xcode Command Line Tools

`jpackage` calls macOS-native tools to assemble the `.app` bundle and `.dmg`. Xcode CLI tools
must be installed.

**Verified version:** Apple Clang 17.0.0 (`clang-1700.6.4.2`), at `/Library/Developer/CommandLineTools`.

Install or update:
```bash
xcode-select --install
```

Verify:
```bash
xcode-select -p
# Expected: /Library/Developer/CommandLineTools (or an Xcode.app path)

clang --version
# Expected: Apple clang version 17.x.x ...
```

> **No WiX, no fakeroot needed** — unlike Windows (`.msi`) and Linux (`.deb`), macOS produces
> a `.dmg` using built-in system tools.

---

## 5. Packaging workflow

From the project root (after `cd app && mvn clean package -DskipTests`):

```bash
./scripts/package-macos.sh
```

The script sources `scripts/jpackage-common.sh` for shared configuration (app name, version,
main class, jlink options), then runs `jpackage` twice:

1. `--type app-image` → produces `dist/Renamer.app` (raw `.app` bundle, no install needed)
2. `--type dmg` → produces `dist/Renamer-<version>.dmg`

---

## 6. GitHub Actions CI — packaging step

The `package-macos` job uses a matrix strategy to build for both Intel and Apple Silicon:

```yaml
package-macos:
  strategy:
    matrix:
      include:
        - runner: macos-13    # Intel (x86_64)
          arch: x86_64
        - runner: macos-14    # Apple Silicon (aarch64)
          arch: aarch64

  - name: Set up JDK
    uses: actions/setup-java@v4
    with:
      java-version: '25'
      distribution: 'temurin'   # Standard JDK — works because JavaFX JARs embed natives
```

> **Why Temurin and not Liberica Full?** The JavaFX modular JARs in `target/libs/` (built by
> the `build-and-test` job on Ubuntu) already contain macOS-specific `.dylib` files. jpackage
> uses those JARs as its jlink module source, so no JavaFX `.jmod` files are needed from the JDK.
> See Section 1 for the full mechanism.

Artifacts uploaded per run:
- `macos-{arch}-dmg` → `dist/*.dmg`
- `macos-{arch}-app-image` → `dist/Renamer-<version>-macos-<arch>.tar.gz`

---

## 7. `dist/` folder structure after packaging

After running `./scripts/package-macos.sh` from the project root:

```
dist/
├── Renamer.app/                              ← macOS app bundle (drag-to-Applications)
│   └── Contents/
│       ├── Info.plist                        ← bundle metadata (name, version, icon, JVM args)
│       ├── MacOS/
│       │   └── Renamer                       ← native launcher binary (Mach-O, arm64/x86_64)
│       ├── Resources/
│       │   └── icon.icns                     ← app icon
│       ├── app/                              ← all JAR files
│       │   ├── Renamer.cfg                   ← jpackage launcher config (JVM args, main class)
│       │   ├── ua.renamer.app.ui-2.0.0.jar
│       │   └── *.jar                         ← ~100 dependency JARs (incl. JavaFX natives)
│       └── runtime/                          ← stripped, embedded JRE (produced by jlink)
│           ├── bin/                          ← java, jar, jshell, ...
│           ├── conf/
│           ├── legal/
│           └── lib/                          ← JVM native libs, extracted JavaFX .dylibs
└── Renamer-2.0.0.dmg                         ← macOS disk image installer
```

**`.app` size:** ~390–410 MB (embedded JRE + app JARs + JavaFX natives)
**`.dmg` size:** ~200–220 MB

### Packaging the app bundle for distribution

The CI pipeline tars the `.app` bundle for upload:

```bash
cd dist && tar -czf "Renamer-2.0.0-macos-aarch64.tar.gz" Renamer.app
```

CI artifact names follow the pattern: `Renamer-{version}-macos-{arch}.tar.gz` and
`Renamer-{version}.dmg`.

---

## 8. Runtime dependencies

The `.app` bundle and `.dmg` bundle their own JRE — **no system Java is required to run the
installed application**. No other runtime dependencies are needed on the end-user machine.

---

## 9. Known macOS-specific behaviour

- **Gatekeeper (unsigned app warning):** The `.app` bundle and `.dmg` are not code-signed or
  notarized. On first launch macOS may show _"Renamer.app is damaged and can't be opened"_.
  To bypass: right-click → **Open**, or run:
  ```bash
  xattr -cr /Applications/Renamer.app
  ```

- **macOS Dock name (`-Xdock:name`):** The `macos-dock` Maven profile (activated automatically
  on macOS) injects `-Xdock:name=Renamer` so the app appears correctly named in the Dock during
  development (`mvn javafx:run`). This flag is excluded on Linux and Windows — see
  [cross-platform-issues.md](cross-platform-issues.md) Issue 1.

- **Case-insensitive filesystem (APFS default):** APFS is case-insensitive by default. Unlike
  Windows NTFS, macOS correctly handles case-only renames (e.g. `file.txt` → `File.txt`) via
  `File.renameTo()` — no special handling required.

- **Dot-prefix hidden files** (e.g. `.hidden`, `.DS_Store`) are valid filenames on APFS and are
  hidden by default in Finder. The rename pipeline treats them as ordinary files — no filtering
  or special behaviour is applied.

- **Case-sensitive APFS variant:** If the system is formatted with case-sensitive APFS, case-only
  rename behaviour matches Linux. The default (case-insensitive) is assumed for all test
  expectations.
