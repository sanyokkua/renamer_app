# Migration Guide: jDeploy → jlink + jpackage

## For Claude Code — Step-by-step instructions to migrate Renamer App

---

## Context

- **Project**: Java 25 + JavaFX 25 multi-module Maven desktop app
- **Current packaging**: jDeploy (thin launcher + on-demand JVM download)
- **Target packaging**: jpackage (self-contained bundle with embedded JVM)
- **Entry point**: `ua.renamer.app.Launcher` (in `app/ui` module)
- **Main app class**: `ua.renamer.app.RenamerApplication` (extends `Application`)
- **Icons**: `icon.icns` (macOS), `icon.ico` (Windows), `icon.png` (Linux) — in project root

## Architecture of the Build

The production-proven pattern for multi-module JavaFX + jpackage:

```
mvn clean install          →  Builds all modules, produces JARs
                              UI module collects all dependency JARs into target/libs/
                              ↓
scripts/package.sh (or .bat) →  Calls jpackage with:
                                --input target/libs (all JARs)
                                --main-jar renamer-app-ui-*.jar
                                --main-class ua.renamer.app.Launcher
                                --icon <platform-specific icon>
                                --type <platform-specific: dmg/msi/deb/app-image>
                              ↓
                              Output: platform installer + app-image (raw executable)
```

**Why shell scripts instead of Maven plugin**: jpackage Maven plugins (e.g., `org.panteleyev:jpackage-maven-plugin`) are fragile with multi-module projects and mixed modular/non-modular dependencies. Shell scripts are transparent, debuggable, and the approach used by the reference `JPackageScriptFX` project (the de facto community template for this pattern). This is the most reliable approach.

---

## Phase 1: Modify UI Module POM — Collect Dependencies

**File**: `app/ui/pom.xml`

Add the `maven-dependency-plugin` to copy all runtime dependency JARs into `target/libs/`. This must run during the `package` phase, AFTER the UI module's own JAR is built.

Add inside `<build><plugins>`:

```xml
<!-- Collect all runtime dependencies for jpackage -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>${maven-dependency-plugin.version}</version>
    <executions>
        <execution>
            <id>copy-dependencies</id>
            <phase>package</phase>
            <goals>
                <goal>copy-dependencies</goal>
            </goals>
            <configuration>
                <outputDirectory>${project.build.directory}/libs</outputDirectory>
                <includeScope>runtime</includeScope>
                <excludeScope>test</excludeScope>
                <overWriteReleases>false</overWriteReleases>
                <overWriteSnapshots>true</overWriteSnapshots>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Also add a step to copy the UI module's own JAR into the same `target/libs/` directory:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>${maven-jar-plugin.version}</version>
    <configuration>
        <archive>
            <manifest>
                <mainClass>ua.renamer.app.Launcher</mainClass>
                <addClasspath>true</addClasspath>
            </manifest>
        </archive>
        <outputDirectory>${project.build.directory}/libs</outputDirectory>
    </configuration>
</plugin>
```

**IMPORTANT**: If the `maven-jar-plugin` `outputDirectory` change causes issues with inter-module resolution, an alternative is to add a `maven-resources-plugin` or `maven-antrun-plugin` execution that copies the built JAR into `target/libs/` after packaging. The simplest fallback:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-antrun-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <id>copy-app-jar</id>
            <phase>package</phase>
            <goals><goal>run</goal></goals>
            <configuration>
                <target>
                    <copy todir="${project.build.directory}/libs">
                        <fileset dir="${project.build.directory}" includes="*.jar" excludes="*-sources.jar,*-javadoc.jar"/>
                    </copy>
                </target>
            </configuration>
        </execution>
    </executions>
</plugin>
```

After `mvn package`, `app/ui/target/libs/` should contain ALL JARs needed to run the app (the UI jar + all transitive dependencies including JavaFX platform JARs).

---

## Phase 2: Create Packaging Scripts

Create a `scripts/` directory structure:

```
scripts/
├── package-macos.sh
├── package-linux.sh
├── package-windows.bat
└── jpackage-common.sh     (shared variables)
```

### scripts/jpackage-common.sh

Shared configuration sourced by platform scripts:

```bash
#!/usr/bin/env bash
# Common jpackage configuration
# Source this file from platform-specific scripts

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
UI_TARGET="$PROJECT_ROOT/app/ui/target"
INPUT_DIR="$UI_TARGET/libs"
OUTPUT_DIR="$PROJECT_ROOT/dist"

# App metadata — update these for each release
APP_NAME="Renamer"
APP_VERSION="${APP_VERSION:-1.1.0}"
APP_VENDOR="Renamer App"
APP_DESCRIPTION="Batch file renaming application"
APP_COPYRIGHT="Copyright (c) Renamer App"
MAIN_JAR="$(ls "$INPUT_DIR"/renamer-app-ui-*.jar 2>/dev/null | head -1 | xargs basename)"
MAIN_CLASS="ua.renamer.app.Launcher"

# JavaFX modules your app uses
JAVA_FX_MODULES="javafx.controls,javafx.fxml,javafx.web,javafx.base"

# JDK modules to include (jpackage runs jlink internally)
# Start minimal, add more if you get runtime errors about missing modules
ADD_MODULES="java.base,java.desktop,java.logging,java.management,java.naming,java.net.http,java.prefs,java.scripting,java.sql,java.xml,jdk.unsupported"

# Verify prerequisites
if [ ! -d "$INPUT_DIR" ]; then
    echo "ERROR: $INPUT_DIR not found. Run 'cd app && mvn clean package -DskipTests' first."
    exit 1
fi

if [ -z "$MAIN_JAR" ]; then
    echo "ERROR: Could not find renamer-app-ui-*.jar in $INPUT_DIR"
    exit 1
fi

mkdir -p "$OUTPUT_DIR"

echo "App: $APP_NAME v$APP_VERSION"
echo "Main JAR: $MAIN_JAR"
echo "Input dir: $INPUT_DIR"
echo "Output dir: $OUTPUT_DIR"
```

### scripts/package-macos.sh

```bash
#!/usr/bin/env bash
# Build macOS .app bundle and .dmg installer
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/jpackage-common.sh"

ICON="$PROJECT_ROOT/icon.icns"

COMMON_ARGS=(
    --input "$INPUT_DIR"
    --dest "$OUTPUT_DIR"
    --name "$APP_NAME"
    --main-jar "$MAIN_JAR"
    --main-class "$MAIN_CLASS"
    --app-version "$APP_VERSION"
    --vendor "$APP_VENDOR"
    --description "$APP_DESCRIPTION"
    --copyright "$APP_COPYRIGHT"
    --icon "$ICON"
    --java-options "--enable-preview"
    --java-options "-Xmx512m"
)

# Build app-image (raw .app bundle — the executable)
echo "=== Building macOS app-image ==="
rm -rf "$OUTPUT_DIR/$APP_NAME.app"
jpackage "${COMMON_ARGS[@]}" \
    --type app-image

echo "App image created: $OUTPUT_DIR/$APP_NAME.app"

# Build .dmg installer
echo "=== Building macOS .dmg ==="
rm -f "$OUTPUT_DIR/${APP_NAME}-${APP_VERSION}.dmg"
jpackage "${COMMON_ARGS[@]}" \
    --type dmg

echo "DMG created in: $OUTPUT_DIR/"
echo "=== macOS packaging complete ==="
```

### scripts/package-linux.sh

```bash
#!/usr/bin/env bash
# Build Linux executable and .deb installer
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/jpackage-common.sh"

ICON="$PROJECT_ROOT/icon.png"

COMMON_ARGS=(
    --input "$INPUT_DIR"
    --dest "$OUTPUT_DIR"
    --name "$APP_NAME"
    --main-jar "$MAIN_JAR"
    --main-class "$MAIN_CLASS"
    --app-version "$APP_VERSION"
    --vendor "$APP_VENDOR"
    --description "$APP_DESCRIPTION"
    --copyright "$APP_COPYRIGHT"
    --icon "$ICON"
    --java-options "--enable-preview"
    --java-options "-Xmx512m"
    --linux-shortcut
    --linux-menu-group "Utility"
)

# Build app-image (raw executable directory)
echo "=== Building Linux app-image ==="
rm -rf "$OUTPUT_DIR/$APP_NAME"
jpackage "${COMMON_ARGS[@]}" \
    --type app-image

echo "App image created: $OUTPUT_DIR/$APP_NAME/"

# Build .deb package
echo "=== Building .deb package ==="
rm -f "$OUTPUT_DIR/"*.deb
jpackage "${COMMON_ARGS[@]}" \
    --type deb \
    --linux-deb-maintainer "renamer-app@example.com" \
    --linux-app-category "utils"

echo "=== Linux packaging complete ==="
ls -la "$OUTPUT_DIR/"*.deb 2>/dev/null || true
```

### scripts/package-windows.bat

```batch
@echo off
setlocal enabledelayedexpansion

REM Build Windows executable and .msi installer
REM Requires: JDK 25 with jpackage, WiX Toolset 3.x for MSI

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."
set "UI_TARGET=%PROJECT_ROOT%\app\ui\target"
set "INPUT_DIR=%UI_TARGET%\libs"
set "OUTPUT_DIR=%PROJECT_ROOT%\dist"
set "ICON=%PROJECT_ROOT%\icon.ico"

REM Find the main JAR
for %%F in ("%INPUT_DIR%\renamer-app-ui-*.jar") do set "MAIN_JAR=%%~nxF"

if "%MAIN_JAR%"=="" (
    echo ERROR: Could not find renamer-app-ui-*.jar in %INPUT_DIR%
    exit /b 1
)

if not defined APP_VERSION set "APP_VERSION=1.1.0"

set "APP_NAME=Renamer"

if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

echo App: %APP_NAME% v%APP_VERSION%
echo Main JAR: %MAIN_JAR%

REM Build app-image (raw executable)
echo === Building Windows app-image ===
if exist "%OUTPUT_DIR%\%APP_NAME%" rmdir /s /q "%OUTPUT_DIR%\%APP_NAME%"

jpackage ^
    --input "%INPUT_DIR%" ^
    --dest "%OUTPUT_DIR%" ^
    --name "%APP_NAME%" ^
    --main-jar "%MAIN_JAR%" ^
    --main-class ua.renamer.app.Launcher ^
    --app-version "%APP_VERSION%" ^
    --vendor "Renamer App" ^
    --description "Batch file renaming application" ^
    --icon "%ICON%" ^
    --java-options "--enable-preview" ^
    --java-options "-Xmx512m" ^
    --type app-image

echo App image created: %OUTPUT_DIR%\%APP_NAME%\

REM Build .msi installer (requires WiX Toolset)
echo === Building Windows .msi ===
jpackage ^
    --input "%INPUT_DIR%" ^
    --dest "%OUTPUT_DIR%" ^
    --name "%APP_NAME%" ^
    --main-jar "%MAIN_JAR%" ^
    --main-class ua.renamer.app.Launcher ^
    --app-version "%APP_VERSION%" ^
    --vendor "Renamer App" ^
    --description "Batch file renaming application" ^
    --icon "%ICON%" ^
    --java-options "--enable-preview" ^
    --java-options "-Xmx512m" ^
    --type msi ^
    --win-dir-chooser ^
    --win-menu ^
    --win-menu-group "Renamer" ^
    --win-shortcut

echo === Windows packaging complete ===
dir "%OUTPUT_DIR%\*.msi" 2>nul
```

Make scripts executable:
```bash
chmod +x scripts/package-macos.sh scripts/package-linux.sh scripts/jpackage-common.sh
```

---

## Phase 3: Local Build Workflow

To build locally on any platform:

```bash
# Step 1: Build all modules
cd app
mvn clean package -DskipTests

# Step 2: Package for your current platform
cd ..

# On macOS:
./scripts/package-macos.sh

# On Linux:
./scripts/package-linux.sh

# On Windows (from cmd or PowerShell):
scripts\package-windows.bat
```

The results will be in `dist/`:
- **macOS**: `dist/Renamer.app/` (executable) + `dist/Renamer-1.1.0.dmg` (installer)
- **Linux**: `dist/Renamer/` (executable directory) + `dist/renamer_1.1.0-1_amd64.deb`
- **Windows**: `dist/Renamer/` (executable directory with `Renamer.exe`) + `dist/Renamer-1.1.0.msi`

---

## Phase 4: GitHub Actions Workflow

Replace the existing workflow. Create `.github/workflows/build-and-release.yml`:

```yaml
name: Build, Test, and Package

on:
  push:
    tags: [ "*-release-*" ]
    branches: [ "*-release-*", "*-snapshot-*" ]

# Shared env vars for all jobs
env:
  APP_VERSION: "1.1.0"
  JAVA_VERSION: "25"

jobs:
  # ──────────────────────────────────────────────
  # Job 1: Build and test (once, on Linux)
  # ──────────────────────────────────────────────
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      - name: Build and test
        run: |
          cd app
          mvn clean verify

      # Upload the libs directory as artifact for packaging jobs
      - name: Upload build artifacts
        uses: actions/upload-artifact@v4
        with:
          name: app-libs
          path: app/ui/target/libs/
          retention-days: 1

  # ──────────────────────────────────────────────
  # Job 2: Package for macOS (x86_64 + ARM)
  # ──────────────────────────────────────────────
  package-macos:
    needs: build-and-test
    strategy:
      matrix:
        include:
          - runner: macos-13       # Intel
            arch: x86_64
          - runner: macos-14       # Apple Silicon
            arch: aarch64
    runs-on: ${{ matrix.runner }}
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'

      - name: Download build artifacts
        uses: actions/download-artifact@v4
        with:
          name: app-libs
          path: app/ui/target/libs/

      - name: Package macOS app
        env:
          APP_VERSION: ${{ env.APP_VERSION }}
        run: ./scripts/package-macos.sh

      - name: Upload macOS artifacts
        uses: actions/upload-artifact@v4
        with:
          name: macos-${{ matrix.arch }}
          path: |
            dist/*.dmg

      # Tar the .app bundle for upload (preserves symlinks and permissions)
      - name: Tar app bundle
        run: cd dist && tar -czf "Renamer-${{ env.APP_VERSION }}-macos-${{ matrix.arch }}.tar.gz" Renamer.app

      - name: Upload app bundle
        uses: actions/upload-artifact@v4
        with:
          name: macos-app-image-${{ matrix.arch }}
          path: dist/*.tar.gz

  # ──────────────────────────────────────────────
  # Job 3: Package for Windows
  # ──────────────────────────────────────────────
  package-windows:
    needs: build-and-test
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'

      # WiX 3.x is pre-installed on windows-latest GitHub runners
      # If not, uncomment:
      # - name: Install WiX Toolset
      #   run: choco install wixtoolset -y

      - name: Download build artifacts
        uses: actions/download-artifact@v4
        with:
          name: app-libs
          path: app/ui/target/libs/

      - name: Package Windows app
        env:
          APP_VERSION: ${{ env.APP_VERSION }}
        run: scripts\package-windows.bat

      - name: Upload Windows MSI
        uses: actions/upload-artifact@v4
        with:
          name: windows-x86_64-msi
          path: dist/*.msi

      # Zip the app-image for portable exe distribution
      - name: Zip app image
        run: Compress-Archive -Path dist\Renamer -DestinationPath "dist\Renamer-${{ env.APP_VERSION }}-windows-x86_64.zip"

      - name: Upload Windows app image
        uses: actions/upload-artifact@v4
        with:
          name: windows-x86_64-app-image
          path: dist/*.zip

  # ──────────────────────────────────────────────
  # Job 4: Package for Linux
  # ──────────────────────────────────────────────
  package-linux:
    needs: build-and-test
    strategy:
      matrix:
        include:
          - runner: ubuntu-latest    # x86_64
            arch: x86_64
          # Uncomment when you need ARM Linux:
          # - runner: ubuntu-24.04-arm
          #   arch: aarch64
    runs-on: ${{ matrix.runner }}
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'

      # jpackage needs fakeroot for .deb creation
      - name: Install packaging dependencies
        run: sudo apt-get update && sudo apt-get install -y fakeroot

      - name: Download build artifacts
        uses: actions/download-artifact@v4
        with:
          name: app-libs
          path: app/ui/target/libs/

      - name: Package Linux app
        env:
          APP_VERSION: ${{ env.APP_VERSION }}
        run: ./scripts/package-linux.sh

      - name: Upload Linux .deb
        uses: actions/upload-artifact@v4
        with:
          name: linux-${{ matrix.arch }}-deb
          path: dist/*.deb

      # Tar the app-image for portable distribution
      - name: Tar app image
        run: cd dist && tar -czf "Renamer-${{ env.APP_VERSION }}-linux-${{ matrix.arch }}.tar.gz" Renamer/

      - name: Upload Linux app image
        uses: actions/upload-artifact@v4
        with:
          name: linux-${{ matrix.arch }}-app-image
          path: dist/*.tar.gz

  # ──────────────────────────────────────────────
  # Job 5: Create GitHub Release (only on tags)
  # ──────────────────────────────────────────────
  create-release:
    if: startsWith(github.ref, 'refs/tags/')
    needs: [package-macos, package-windows, package-linux]
    runs-on: ubuntu-latest
    permissions:
      contents: write
    steps:
      - name: Download all artifacts
        uses: actions/download-artifact@v4
        with:
          path: release-artifacts/
          merge-multiple: false

      # Flatten all artifacts into one directory
      - name: Collect release files
        run: |
          mkdir -p release-files
          find release-artifacts/ -type f \( -name "*.dmg" -o -name "*.msi" -o -name "*.deb" -o -name "*.tar.gz" -o -name "*.zip" \) -exec cp {} release-files/ \;
          ls -la release-files/

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          files: release-files/*
          generate_release_notes: true
          draft: false
          prerelease: ${{ contains(github.ref, 'snapshot') }}
```

---

## Phase 5: Remove jDeploy Artifacts

After migration is verified working, remove:

1. `package.json` — jDeploy configuration (in project root)
2. `package-lock.json` — npm lock file
3. `installsplash.png` — jDeploy splash (unless you want to keep it for other purposes)
4. The `Set up Node` step and `Publish with jDeploy` step from any remaining workflows
5. Any jDeploy-specific configuration from the old GitHub Actions workflow

**Keep**: `icon.icns`, `icon.ico`, `icon.png` — these are used by the new jpackage scripts.

---

## Phase 6: Add `dist/` to .gitignore

Add to `.gitignore`:

```
dist/
```

---

## Troubleshooting Notes

### "Module not found" errors at runtime

jpackage builds a trimmed JVM using jlink internally. If you get `java.lang.module.FindException` or `ClassNotFoundException` at runtime, you need to add the missing module to the `--add-modules` list in the packaging scripts. Common ones to add:

- `java.sql` — if any dependency uses JDBC
- `jdk.unsupported` — if any dependency uses `sun.misc.Unsafe` (Guice does)
- `java.naming` — if any dependency uses JNDI
- `java.management` — for JMX monitoring beans

### JavaFX "not on module path" warning

If you see: `Unsupported JavaFX configuration: classes were loaded from 'unnamed module'`

This is a warning, not an error. The app will still work. It means JavaFX JARs are on the classpath instead of the module path. jpackage handles this correctly for deployment — the warning only appears during development.

### WiX not found on Windows CI

GitHub `windows-latest` runners include WiX 3.x. If it stops working, add:
```yaml
- name: Install WiX
  run: choco install wixtoolset -y
```

### macOS Gatekeeper / notarization

Without code signing, macOS users will see "app is damaged" warnings for downloaded .dmg files. To fix this properly you need an Apple Developer account ($99/year) and a notarization step. Add this after the macOS package step:

```yaml
# Optional: Sign and notarize (requires Apple Developer account secrets)
- name: Sign and notarize
  if: startsWith(github.ref, 'refs/tags/')
  env:
    APPLE_ID: ${{ secrets.APPLE_ID }}
    APPLE_PASSWORD: ${{ secrets.APPLE_APP_PASSWORD }}
    TEAM_ID: ${{ secrets.APPLE_TEAM_ID }}
    CERT_BASE64: ${{ secrets.MACOS_CERT_BASE64 }}
    CERT_PASSWORD: ${{ secrets.MACOS_CERT_PASSWORD }}
  run: |
    # Import certificate
    echo "$CERT_BASE64" | base64 --decode > cert.p12
    security create-keychain -p "" build.keychain
    security import cert.p12 -k build.keychain -P "$CERT_PASSWORD" -T /usr/bin/codesign
    security set-key-partition-list -S apple-tool:,apple: -s -k "" build.keychain
    security default-keychain -s build.keychain
    # Sign the .app bundle
    codesign --deep --force --options runtime --sign "Developer ID Application: YOUR_NAME (TEAM_ID)" "dist/Renamer.app"
    # Create signed DMG
    jpackage ... --type dmg --mac-sign --mac-signing-key-user-name "Developer ID Application: YOUR_NAME (TEAM_ID)"
    # Notarize
    xcrun notarytool submit dist/*.dmg --apple-id "$APPLE_ID" --password "$APPLE_PASSWORD" --team-id "$TEAM_ID" --wait
    xcrun stapler staple dist/*.dmg
```

For an open-source project where you don't want to pay $99/year, you can instruct users to bypass Gatekeeper:
```bash
xattr -cr /Applications/Renamer.app
```

### Version synchronization

Keep `APP_VERSION` in the packaging scripts synchronized with the `<version>` in `app/pom.xml`. You can automate this by extracting it:

```bash
APP_VERSION=$(mvn -f "$PROJECT_ROOT/app/pom.xml" help:evaluate -Dexpression=project.version -q -DforceStdout)
```

Add this line to `jpackage-common.sh` to replace the hardcoded default.

### --enable-preview flag

Your project uses Java 25 with preview features enabled. The `--java-options "--enable-preview"` in the packaging scripts passes this flag to the JVM at runtime. If you stop using preview features, remove this flag.

### Large installer size

The initial installer will be ~120-180 MB (JVM + JavaFX + your app + all dependencies including Tika). To reduce size:

1. Check if you really need `tika-parsers-standard-package` — if you only use MIME detection, `tika-core` alone is much smaller
2. Add `--strip-debug` to jlink options via `--jlink-options "--strip-debug --no-header-files --no-man-pages"`

Add to the jpackage call:
```bash
--jlink-options "--strip-debug --no-header-files --no-man-pages --compress zip-6"
```

---

## Summary of Files to Create/Modify

| Action | File |
|--------|------|
| **Modify** | `app/ui/pom.xml` — add maven-dependency-plugin + copy JAR to target/libs |
| **Create** | `scripts/jpackage-common.sh` — shared config |
| **Create** | `scripts/package-macos.sh` — macOS packaging |
| **Create** | `scripts/package-linux.sh` — Linux packaging |
| **Create** | `scripts/package-windows.bat` — Windows packaging |
| **Replace** | `.github/workflows/build-and-release.yml` — new matrix build workflow |
| **Modify** | `.gitignore` — add `dist/` |
| **Delete** | `package.json` — jDeploy config (after verification) |
| **Delete** | `package-lock.json` — npm lock file (after verification) |
| **Keep** | `icon.icns`, `icon.ico`, `icon.png` — used by jpackage |

---

## Execution Order for Claude Code

1. First, modify `app/ui/pom.xml` to add the dependency collection plugins
2. Run `cd app && mvn clean package -DskipTests` to verify the libs directory is populated
3. Verify `app/ui/target/libs/` contains all JARs (should be 30+ JARs)
4. Create the packaging scripts in `scripts/`
5. Run the local packaging script for your current platform to test
6. Verify the output in `dist/` launches correctly
7. Create the new GitHub Actions workflow
8. Update `.gitignore`
9. Only after everything works: remove jDeploy files
