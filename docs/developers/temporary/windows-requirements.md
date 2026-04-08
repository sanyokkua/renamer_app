# Windows Build, Test & Packaging Requirements

Verified on: **Windows 11 Pro**, architecture **x86_64 (amd64)**.

---

## 1. JDK — critical: must be a Full distribution with JavaFX

Standard JDK distributions (Temurin, standard Liberica, etc.) do **not** bundle JavaFX jmods.
`jpackage` uses `jlink` to build a self-contained JRE — without JavaFX jmods, the bundled runtime
will be missing the `javafx.*` modules required by the app.

**Required:** BellSoft Liberica JDK 25 Full Edition (Windows x64)

| Property | Value |
|----------|-------|
| Vendor | BellSoft |
| Distribution name | Liberica JDK Full |
| Version | 25.0.2 (or later 25.x) |
| Architecture | x86_64 |
| Runtime string | `OpenJDK Runtime Environment (build 25.0.2+12-LTS)` |
| `java -version` vendor | `BellSoft` |
| Typical install path | `C:\Program Files\BellSoft\LibericaJDK-25-Full` |

> **Why Liberica Full, not standard Liberica or Temurin?**
> The standard Liberica JDK (non-Full) and Temurin do not include `javafx.*.jmod` files.
> `jpackage` needs these jmods to link a bundled JRE that includes JavaFX. Liberica Full Edition
> integrates JavaFX jmods directly into the JDK's `jmods/` directory — no separate download or
> `--module-path` configuration needed.

Install from the BellSoft download page — select **Full JDK**, **Windows**, **x86_64 (amd64)**:
```
https://bell-sw.com/pages/downloads/#jdk-25
```

Verify the installation:
```bat
java -version
REM Expected output includes: OpenJDK ... BellSoft

jmod list javafx.controls
REM Must succeed (exit code 0). If this fails, the JDK is not the Full edition.
```

---

## 2. Maven

| Property | Value |
|----------|-------|
| Version | Apache Maven 3.9.14 |
| Minimum version | 3.9.x |

Maven is **not** provided by a Maven Wrapper — `mvnw` is not present. Install and add to PATH manually.

Download and extract, then add to the Windows PATH:
```bat
REM Add to System PATH (example — adjust path as needed):
set PATH=C:\Tools\apache-maven-3.9.14\bin;%PATH%
```

Or set permanently via System Properties → Environment Variables → PATH.

---

## 3. Build commands (run from `app/` directory)

```bat
cd app

mvn compile -q -ff                          :: Compile only
mvn test -q -ff -Dai=true                   :: All tests (quiet, stops on failure)
mvn test -q -ff -Dai=true -Dtest=ClassName  :: Single test class
mvn clean install -q                        :: Build all modules, install to local repo
mvn verify -Pcode-quality -q                :: Full quality check (Checkstyle, PMD, SpotBugs)
```

> `app/.mvn/maven.config` applies `-B --no-transfer-progress` globally.
> `app/.mvn/jvm.config` sets PMD log level; the macOS Dock flag is excluded on Windows
> via the `macos-dock` Maven profile (activated only on `<family>mac</family>`).

**Running `ai-build.sh`:** This script requires Git Bash or WSL. From Git Bash:
```bash
cd /c/path/to/renamer_app
../scripts/ai-build.sh
```

---

## 4. Packaging dependencies

Required only for `scripts/package-windows.bat` (not needed for compile/test):

| Tool | Required for | Install |
|------|-------------|---------|
| `jpackage` | Both app-image and .msi | Bundled with JDK 25 |
| Liberica JDK 25 Full Edition | Both app-image and .msi | See Section 1 |
| WiX Toolset 3.x | `.msi` installer only | See below |

### WiX Toolset

WiX is required only to produce the `.msi` installer. If WiX is absent, `scripts/package-windows.bat`
will skip the `.msi` step and build only the app-image.

Download WiX Toolset 3.x from GitHub:
```
https://github.com/wixtoolset/wix3/releases
```

Install and confirm that `candle` is on PATH:
```bat
where candle
REM Expected: C:\Program Files (x86)\WiX Toolset v3.x\bin\candle.exe
```

> **CI note:** WiX 3.x is pre-installed on GitHub Actions `windows-latest` runners. MSI
> creation works in CI even if WiX is absent locally.

---

## 5. Packaging workflow

From the project root (after `cd app && mvn clean package -DskipTests`):

```bat
scripts\package-windows.bat
```

The script performs two pre-flight checks before running:
1. **JavaFX jmods** — runs `jmod list javafx.controls`. Exits with an error if jmods are missing,
   before any jpackage call.
2. **WiX Toolset** — checks for `candle` in PATH. If absent, prints a note and skips the `.msi`
   step; the app-image is still built.

---

## 6. GitHub Actions CI — packaging step

The `package-windows` job in `.github/workflows/build-and-release.yml` uses Liberica Full:

```yaml
- name: Set up JDK
  uses: actions/setup-java@v4
  with:
    java-version: '25'
    distribution: 'liberica'
    java-package: 'jdk+fx'    # Full edition with JavaFX jmods
```

> **Why not Temurin for packaging?** Temurin is used for `build-and-test` (runs on Ubuntu) and
> works there because Maven downloads JavaFX JARs from Maven Central for the compile/test phases.
> Packaging requires JavaFX jmods (for jlink to bundle them into the JRE), which Temurin does not
> provide. Using `liberica` + `java-package: 'jdk+fx'` ensures the packaging runner has the
> same jmod-capable JDK used for local packaging.

The `build-and-test` job (Linux, Temurin) is unchanged — it does not run jpackage.

---

## 7. `dist/` folder structure after packaging

After running `scripts/package-windows.bat` from the project root:

```
dist/
├── Renamer/                          <- app-image (raw executable, no install needed)
│   ├── Renamer.exe                   <- native launcher
│   ├── Renamer.ico                   <- app icon
│   └── app/                         <- all JAR files + launcher config
│       ├── Renamer.cfg               <- jpackage launcher config (JVM args, main class)
│       ├── ua.renamer.app.ui-2.0.0.jar
│       └── *.jar                     <- ~100 dependency JARs (including JavaFX natives)
│   └── runtime/                     <- stripped, embedded JRE (produced by jlink)
│       ├── bin/                     <- java.exe, ...
│       ├── conf/
│       └── lib/                     <- JVM native DLLs, JavaFX modules, ...
└── Renamer-2.0.0.msi                <- Windows installer (only if WiX is available)
```

**App-image size:** ~400 MB (embedded JRE + app JARs + JavaFX natives)
**`.msi` size:** ~220 MB

### What the `.msi` installs

| Path | Contents |
|------|----------|
| `C:\Program Files\Renamer\` | App-image layout |
| `C:\Program Files\Renamer\Renamer.exe` | Native launcher |
| Start Menu → Renamer | Shortcut (via `--win-menu`) |
| Desktop | Shortcut (via `--win-shortcut`) |

### Packaging the app-image for distribution

The CI pipeline zips the app-image for upload:

```powershell
Compress-Archive -Path dist\Renamer -DestinationPath "dist\Renamer-2.0.0-windows-x86_64.zip"
```

CI artifact names follow the pattern: `Renamer-{version}-windows-x86_64.zip` and
`Renamer-{version}.msi`.

---

## 8. Runtime dependencies

The app-image and `.msi` bundle their own JRE — **no system Java is required to run the
installed application**. No other runtime dependencies are needed on the end-user machine.

---

## 9. Known Windows-specific behaviour

- **Filenames with `:` (colon)** — Colons are illegal in Windows filename components. The rename
  pipeline detects this before attempting any file operation and returns `ERROR_TRANSFORMATION`
  with message `"Generated filename contains invalid characters"`. See Issue 4 in
  [`docs/cross-platform-issues.md`](cross-platform-issues.md).

- **Case-only renames** (e.g. `file.txt` → `File.txt`) — NTFS is case-insensitive by default, so
  `Files.move()` is a no-op for case-only changes. The pipeline detects this via
  `equalsIgnoreCase()` and uses `File.renameTo()` instead, which works on NTFS.

- **Windows reserved names** — `CON`, `PRN`, `AUX`, `NUL`, `COM1`–`COM9`, `LPT1`–`LPT9` are
  rejected by `NameValidator` before any rename attempt.
