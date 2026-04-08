# Linux Build, Test & Packaging Requirements

Verified on: **Ubuntu 22.04.5 LTS (Jammy Jellyfish)**, architecture **aarch64 (ARM64)**.

---

## 1. JDK — critical: must be a full distribution with JavaFX

Standard OpenJDK distributions for Linux ARM64 ship **without JavaFX**. The project requires a
JDK that bundles JavaFX natively.

**Required:** BellSoft Liberica JDK 25 Full Edition

| Property | Value |
|----------|-------|
| Vendor | BellSoft |
| Distribution name | Liberica JDK Full |
| Version | 25.0.2 (or later 25.x) |
| Architecture | aarch64 |
| Runtime string | `OpenJDK Runtime Environment (build 25.0.2+12-LTS)` |
| `java -version` vendor | `BellSoft` |
| Install path on this machine | `/usr/lib/jvm/bellsoft-java25-full-aarch64` |

> **Why Liberica Full, not standard OpenJDK?**
> The `javafx.*` modules (`javafx.controls`, `javafx.fxml`, etc.) required by `app/ui` are not
> bundled in the official OpenJDK ARM64 builds. Liberica Full Edition includes a pre-built
> JavaFX for aarch64. Other distributions that include JavaFX on ARM64 (e.g. Azul Zulu with FX)
> would also work, but have not been tested.

Install via BellSoft APT repository:
```bash
wget -q -O - https://download.bell-sw.com/pki/GPG-KEY-bellsoft | sudo apt-key add -
echo "deb [arch=arm64] https://apt.bell-sw.com/ stable main" | sudo tee /etc/apt/sources.list.d/bellsoft.list
sudo apt-get update
sudo apt-get install -y bellsoft-java25-full
```

---

## 2. Maven

| Property | Value |
|----------|-------|
| Version | Apache Maven 3.9.14 |
| Install path on this machine | `/home/ubuntu/Tools/apache-maven-3.9` |
| Minimum version | 3.9.x (for Maven 4 compatibility flags) |

Maven is **not** provided by the Maven Wrapper in this project — the `mvnw` script is not present.
Use a system or manually installed Maven 3.9+.

Install:
```bash
# Download and extract, then add to PATH
wget https://archive.apache.org/dist/maven/maven-3/3.9.14/binaries/apache-maven-3.9.14-bin.tar.gz
tar -xzf apache-maven-3.9.14-bin.tar.gz -C ~/Tools/
export PATH="$HOME/Tools/apache-maven-3.9.14/bin:$PATH"
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
> `app/.mvn/jvm.config` sets PMD log level; the macOS Dock flag is excluded on Linux
> via the `macos-dock` Maven profile (activated only on `<family>mac</family>`).

---

## 4. Packaging dependencies

Required only for `scripts/package-linux.sh` (not needed for compile/test):

| Tool | Package | Required for |
|------|---------|-------------|
| `fakeroot` | `fakeroot` | Building `.deb` packages |
| `dpkg` | `dpkg` (pre-installed on Ubuntu) | `.deb` packaging internals |
| `jpackage` | Bundled with JDK 25 | Both `app-image` and `.deb` |

```bash
sudo apt-get install -y fakeroot
```

If `fakeroot` is absent, `scripts/package-linux.sh` skips the `.deb` step and builds the
`app-image` only (exits cleanly with a warning).

---

## 5. GitHub Actions CI — packaging step

The current CI workflow (`build-and-release.yml`) runs the Linux packaging job on
`ubuntu-latest` (x86_64). To support ARM64 packaging in CI, the matrix entry must use an
ARM64 runner:

```yaml
# Current (x86_64 only):
- runner: ubuntu-latest
  arch: x86_64

# To add ARM64:
- runner: ubuntu-24.04-arm   # GitHub hosted ARM64 runner
  arch: arm64
```

The `package-linux` job also uses `distribution: 'temurin'` for the JDK setup step.
**Temurin does not bundle JavaFX** — this works for packaging because the `.jar` files are
already built by the `build-and-test` step (which must use a JavaFX-capable JDK) and only
`jpackage` runs on the packaging runner. If the packaging step is ever changed to also compile
code, the distribution must be switched to `liberica` with variant `full`:

```yaml
- name: Set up JDK
  uses: actions/setup-java@v4
  with:
    java-version: '25'
    distribution: 'liberica'
    java-package: 'jdk+fx'   # or variant: 'full' depending on setup-java version
```

---

## 6. `dist/` folder structure after packaging

After running `scripts/package-linux.sh` from the project root:

```
dist/
├── Renamer/                        ← app-image (raw executable, no install needed)
│   ├── bin/
│   │   └── Renamer                 ← native launcher binary (ELF, aarch64)
│   └── lib/
│       ├── Renamer.png             ← app icon
│       ├── libapplauncher.so       ← JPackage native launcher support library
│       ├── app/                    ← all JAR files (app + all runtime dependencies)
│       │   ├── Renamer.cfg         ← jpackage launcher config (JVM args, main class)
│       │   ├── ua.renamer.app.ui-2.0.0.jar
│       │   └── *.jar               ← ~100 dependency JARs
│       └── runtime/                ← stripped, embedded JRE (produced by jlink)
│           ├── bin/                ← java, jar, jarsigner, jshell, …
│           ├── conf/
│           ├── legal/
│           └── lib/                ← JVM native libs, JavaFX modules, …
└── renamer_2.0.0_arm64.deb         ← Debian installer (~221 MB)
```

**app-image size:** ~395 MB (embedded JRE + app JARs + JavaFX natives)
**`.deb` size:** ~221 MB

### What the `.deb` installs

| Path | Contents |
|------|----------|
| `/opt/renamer/bin/Renamer` | Native launcher |
| `/opt/renamer/lib/app/*.jar` | App and dependency JARs |
| `/opt/renamer/lib/runtime/` | Embedded JRE |
| `/opt/renamer/lib/Renamer.png` | App icon |
| `/opt/renamer/lib/renamer-Renamer.desktop` | `.desktop` entry (adds to application menu) |

The `.desktop` entry is placed at `/opt/renamer/lib/renamer-Renamer.desktop` by jpackage.
To integrate into the system application menu after manual extraction (app-image), copy it to
`~/.local/share/applications/` or `/usr/share/applications/`.

### Packaging the app-image for distribution

The CI pipeline tars the app-image for upload:

```bash
cd dist && tar -czf "Renamer-2.0.0-linux-arm64.tar.gz" Renamer/
```

CI artifact names follow the pattern: `Renamer-{version}-linux-{arch}.tar.gz` and
`renamer_{version}_{arch}.deb`.

---

## 7. Runtime dependencies

The app-image and `.deb` bundle their own JRE — **no system Java is required to run the
installed application**. No other runtime dependencies are needed on the end-user machine.

---

## 8. Locale

Build and tests were verified with `LC_COLLATE=C.UTF-8`. The sort tiebreakers added to
`SequenceTransformer` make the test suite locale-independent — no special locale configuration
is required.
