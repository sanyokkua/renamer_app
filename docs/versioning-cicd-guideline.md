# Versioning & CI/CD Guideline for JavaFX Maven Projects with GitHub Actions

## Purpose

This document describes how to implement automated version management and CI/CD pipelines for a JavaFX desktop application built with Maven, packaged with `jpackage`, and released via GitHub Actions. It is intended as a reference for Claude Code (or any developer) to adapt to a specific project's build scripts and requirements.

---

## Core Principle: Git Tags Are the Single Source of Truth

The version of the application is **derived from Git tags**, not manually maintained in `pom.xml`. During development, the pom carries a placeholder SNAPSHOT version. At release time, a Git tag (e.g., `v1.2.0`) triggers the CI pipeline, which injects the tag-derived version into Maven via `mvn versions:set`, builds the app, packages it with `jpackage`, and creates a GitHub Release with the resulting artifacts.

The pom is **never committed with the release version**. The tag is the permanent, immutable record of what version was released. This avoids CI-generated version-bump commits and keeps the Git history clean.

---

## Version Format

Use **Semantic Versioning**: `MAJOR.MINOR.PATCH` (e.g., `1.2.0`).

Git tags use a `v` prefix: `v1.2.0`. The pipeline strips the prefix before passing the version to Maven and `jpackage`.

`jpackage` requires `--app-version` to be a purely numeric semver-like string (`X.Y.Z` or `X.Y.Z.N`). No leading `v`, no text suffixes. The pipeline must validate this.

---

## pom.xml Setup

Keep a static placeholder version in `pom.xml`. This version is used only for local development builds and feature-branch CI. It is overridden by the pipeline for release builds.

```xml
<project>
  <groupId>com.example</groupId>
  <artifactId>my-javafx-app</artifactId>
  <!-- Placeholder: overridden by CI for releases -->
  <version>0.0.0-SNAPSHOT</version>
  ...
</project>
```

### Making the version available at runtime

If the application needs to display its version at runtime (e.g., in an About dialog), use Maven resource filtering or a properties file:

**Option A: Filtered properties file**

Create `src/main/resources/app.properties`:

```properties
app.version=${project.version}
```

Enable filtering in `pom.xml`:

```xml
<build>
  <resources>
    <resource>
      <directory>src/main/resources</directory>
      <filtering>true</filtering>
      <includes>
        <include>app.properties</include>
      </includes>
    </resource>
    <resource>
      <directory>src/main/resources</directory>
      <filtering>false</filtering>
      <excludes>
        <exclude>app.properties</exclude>
      </excludes>
    </resource>
  </resources>
</build>
```

Read it in Java:

```java
Properties props = new Properties();
props.load(getClass().getResourceAsStream("/app.properties"));
String version = props.getProperty("app.version");
```

When CI runs `mvn versions:set -DnewVersion=1.2.0`, subsequent `mvn package` will filter `${project.version}` to `1.2.0` in the output jar.

**Option B: `Implementation-Version` in MANIFEST.MF**

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-jar-plugin</artifactId>
  <configuration>
    <archive>
      <manifest>
        <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
      </manifest>
    </archive>
  </configuration>
</plugin>
```

Read it in Java:

```java
String version = getClass().getPackage().getImplementationVersion();
```

Both options work. Choose one and be consistent.

---

## Pipeline Architecture

Three workflows, triggered by different events:

```
feature/* branches ──► ci.yml ──► build + test
main branch push    ──► build.yml ──► build + test + upload artifacts (snapshot)
v* tag on main      ──► release.yml ──► verify main CI passed ──► build artifacts ──► GitHub Release
```

### Pipeline 1: Feature Branch CI

**Trigger:** Push to any non-main branch, or pull request targeting main.

**Purpose:** Validate that the code compiles and tests pass. No packaging, no artifacts.

**File:** `.github/workflows/ci.yml`

```yaml
name: CI

on:
  push:
    branches-ignore:
      - main
  pull_request:
    branches:
      - main

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '21'          # adjust to project requirements
          distribution: 'temurin'
          cache: maven

      - name: Build and test
        run: mvn -B verify
```

**Notes:**

- The pom's `0.0.0-SNAPSHOT` version is used as-is. This is fine; the version is meaningless for feature branches.
- `-B` (batch mode) suppresses interactive Maven output.
- `verify` runs compile, test, and integration-test phases.
- No artifacts are uploaded. The sole purpose is validation.

### Pipeline 2: Main Branch Build

**Trigger:** Push to `main` (direct commit or merged PR). Excludes tag pushes so it doesn't double-fire with the release workflow.

**Purpose:** Build, test, and optionally upload snapshot artifacts. This confirms that `main` is always in a releasable state.

**File:** `.github/workflows/build.yml`

```yaml
name: Build

on:
  push:
    branches:
      - main
    tags-ignore:
      - '**'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Build and test
        run: mvn -B verify

      - name: Upload build artifacts (snapshot)
        uses: actions/upload-artifact@v4
        with:
          name: snapshot-build
          path: target/*.jar
          retention-days: 5
```

**Notes:**

- `tags-ignore: '**'` prevents this workflow from running when a tag is pushed (the release workflow handles that).
- Uploading the jar as a GitHub Actions artifact (not a Release asset) gives you a downloadable snapshot for internal testing. This is optional — remove the upload step if not needed.
- The snapshot artifact uses the pom's SNAPSHOT version. This is fine because it is not a release.

### Pipeline 3: Release (Tag-Triggered)

**Trigger:** A tag matching `v*.*.*` is pushed.

**Purpose:** Derive the version from the tag, inject it into Maven, build, run jpackage on each target OS, and create a GitHub Release with the installer artifacts.

**File:** `.github/workflows/release.yml`

```yaml
name: Release

on:
  push:
    tags:
      - 'v[0-9]+.[0-9]+.[0-9]+'       # matches v1.2.3
      - 'v[0-9]+.[0-9]+.[0-9]+-*'     # matches v1.2.3-rc1, v1.2.3-beta.1

permissions:
  contents: write                       # required to create GitHub Releases

jobs:
  # ──────────────────────────────────────────────
  # Step 1: Validate that main CI passed for this commit
  # ──────────────────────────────────────────────
  verify:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Verify tag is on main branch
        run: |
          git fetch origin main
          if ! git merge-base --is-ancestor ${{ github.sha }} origin/main; then
            echo "::error::Tag must point to a commit on the main branch"
            exit 1
          fi

      # Optional: check that the Build workflow succeeded for this commit.
      # This uses the GitHub CLI to query workflow run status.
      - name: Verify main build passed
        env:
          GH_TOKEN: ${{ github.token }}
        run: |
          # Find the most recent Build workflow run for this commit
          RUN_STATUS=$(gh run list \
            --workflow=build.yml \
            --commit ${{ github.sha }} \
            --json status,conclusion \
            --jq '.[0].conclusion // "not_found"')

          if [ "$RUN_STATUS" != "success" ]; then
            echo "::error::Build workflow has not succeeded for commit ${{ github.sha }} (status: $RUN_STATUS)"
            echo "Push to main and wait for the build to pass before tagging."
            exit 1
          fi

  # ──────────────────────────────────────────────
  # Step 2: Build platform-specific installers
  # ──────────────────────────────────────────────
  package:
    needs: verify
    runs-on: ${{ matrix.os }}
    strategy:
      fail-fast: false
      matrix:
        include:
          - os: ubuntu-latest
            jpackage-type: deb
            artifact-ext: deb
          - os: windows-latest
            jpackage-type: msi
            artifact-ext: msi
          - os: macos-latest
            jpackage-type: dmg
            artifact-ext: dmg
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      # ── Derive version from Git tag ──
      - name: Extract version from tag
        id: version
        shell: bash
        run: |
          TAG="${GITHUB_REF_NAME}"
          VERSION="${TAG#v}"

          # Validate: must be X.Y.Z (jpackage requirement)
          if [[ ! "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
            echo "::error::Version '$VERSION' is not valid for jpackage (must be X.Y.Z)"
            exit 1
          fi

          echo "version=$VERSION" >> "$GITHUB_OUTPUT"
          echo "tag=$TAG" >> "$GITHUB_OUTPUT"
          echo "Derived version: $VERSION from tag: $TAG"

      # ── Set Maven project version ──
      - name: Set Maven version
        run: >
          mvn versions:set
          -DnewVersion=${{ steps.version.outputs.version }}
          -DgenerateBackupFiles=false
          --batch-mode

      # ── Build ──
      - name: Build with Maven
        run: mvn -B package -DskipTests

      # ── Package with jpackage ──
      # ADAPT THIS STEP to your project's specifics:
      #   --input: directory containing the built jars
      #   --main-jar: name of the main jar file
      #   --main-class: fully qualified main class (if not in MANIFEST)
      #   --name: application name
      #   Add --icon, --vendor, --description, etc. as needed.
      - name: Create installer with jpackage
        shell: bash
        run: |
          jpackage \
            --input target/lib \
            --main-jar my-app-${{ steps.version.outputs.version }}.jar \
            --name "MyApp" \
            --app-version ${{ steps.version.outputs.version }} \
            --vendor "My Company" \
            --description "My JavaFX Application" \
            --type ${{ matrix.jpackage-type }} \
            --dest target/installer

      - name: Upload installer artifact
        uses: actions/upload-artifact@v4
        with:
          name: installer-${{ matrix.os }}
          path: target/installer/*.${{ matrix.artifact-ext }}
          if-no-files-found: error

  # ──────────────────────────────────────────────
  # Step 3: Create GitHub Release
  # ──────────────────────────────────────────────
  release:
    needs: package
    runs-on: ubuntu-latest
    steps:
      - name: Download all installer artifacts
        uses: actions/download-artifact@v4
        with:
          path: installers
          pattern: installer-*
          merge-multiple: true

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          tag_name: ${{ github.ref_name }}
          name: "Release ${{ github.ref_name }}"
          draft: false
          prerelease: ${{ contains(github.ref_name, '-') }}
          generate_release_notes: true
          files: |
            installers/*
```

---

## Key Mechanisms Explained

### Version injection with `mvn versions:set`

```bash
mvn versions:set -DnewVersion=1.2.0 -DgenerateBackupFiles=false --batch-mode
```

This modifies `pom.xml` in the CI workspace only (the change is never committed). All subsequent Maven commands (`mvn package`, etc.) use version `1.2.0`. Resource filtering picks up the new version. The built jar will be named `my-app-1.2.0.jar`.

`-DgenerateBackupFiles=false` prevents creation of `pom.xml.versionsBackup` files.

### Verifying the tag is on main

The release workflow explicitly checks that the tagged commit exists on `main`. This prevents accidental releases from feature branches.

```bash
git fetch origin main
git merge-base --is-ancestor <commit-sha> origin/main
```

### Verifying prior CI success

Before building release artifacts, the pipeline checks that the Build workflow (pipeline 2) already ran successfully for the tagged commit. This avoids re-running the full test suite in the release pipeline (though you can choose to re-run tests if you prefer safety over speed).

The check uses `gh run list` to query the GitHub API. If the build hasn't passed, the release aborts.

### Pre-release detection

Tags containing a hyphen (e.g., `v1.2.0-rc1`, `v1.2.0-beta.1`) are automatically marked as pre-releases on GitHub. The expression `contains(github.ref_name, '-')` handles this. Note that jpackage version validation above rejects non-numeric suffixes — if you need pre-release tags with jpackage, strip the suffix for `--app-version` while keeping it for the GitHub Release name.

To support pre-release tags with jpackage, adjust the version extraction:

```bash
# Full version for GitHub Release naming
FULL_VERSION="${TAG#v}"
# Numeric-only version for jpackage (strip everything after first hyphen)
JPACKAGE_VERSION="${FULL_VERSION%%-*}"

echo "full-version=$FULL_VERSION" >> "$GITHUB_OUTPUT"
echo "jpackage-version=$JPACKAGE_VERSION" >> "$GITHUB_OUTPUT"
```

---

## Developer Workflow

### Day-to-day development

1. Work on a feature branch. Push triggers pipeline 1 (build + test).
2. Open a PR to main. Pipeline 1 runs on the PR.
3. Merge to main. Pipeline 2 runs (build + test + optional snapshot artifacts).

### Creating a release

```bash
# Ensure main is up to date and CI has passed
git checkout main
git pull

# Tag the release
git tag v1.2.0
git push origin v1.2.0
```

That's it. Pipeline 3 runs automatically.

Alternatively, use the **GitHub UI**: go to Releases → Draft a new release → Create a new tag on publish. This creates the tag and can trigger the release workflow. If using this approach, change the trigger to:

```yaml
on:
  release:
    types: [created]
```

And extract the version from `github.event.release.tag_name` instead of `GITHUB_REF_NAME`.

### Hotfix workflow

For urgent fixes:

1. Fix on main (or merge a hotfix branch).
2. Wait for pipeline 2 to pass.
3. Tag with incremented patch version: `git tag v1.2.1 && git push origin v1.2.1`.

---

## Adaptation Checklist

When applying this guideline to a specific project, the following items need to be customized:

| Item | What to change |
|------|----------------|
| **Java version** | `java-version` in `setup-java` — match the project's target JDK |
| **jpackage `--input`** | Path to the directory containing built jars and dependencies |
| **jpackage `--main-jar`** | Name of the main application jar (includes version in name) |
| **jpackage `--main-class`** | Fully qualified main class, if not specified in jar MANIFEST |
| **jpackage `--type`** | Target installer types per OS (deb/rpm for Linux, msi/exe for Windows, dmg/pkg for macOS) |
| **jpackage extras** | `--icon`, `--vendor`, `--copyright`, `--license-file`, `--file-associations`, `--java-options` |
| **OS matrix** | Which platforms to build for — remove rows from the matrix if not needed |
| **Maven profiles** | If the project uses profiles for packaging (e.g., `-P release`), add them to the `mvn package` command |
| **Module path** | If the project is modular (JPMS), use `--module-path` and `--module` instead of `--input` and `--main-jar` |
| **Multi-module pom** | If the project has sub-modules, `mvn versions:set` propagates to children. Verify with `mvn help:evaluate -Dexpression=project.version` |
| **Dependencies dir** | If using `maven-dependency-plugin` to copy deps to `target/lib`, ensure `--input` points there |
| **Code signing** | macOS notarization and Windows code signing require additional steps (secrets, `codesign`/`signtool` calls) |
| **Test scope** | Adjust `mvn verify` vs `mvn test` based on whether integration tests exist |
| **Snapshot artifacts** | Decide whether pipeline 2 should upload artifacts or just validate |

---

## Alternatives Considered

### maven-git-versioning-extension

A Maven extension (`me.qoomon:maven-git-versioning-extension`) that automatically sets the project version in memory based on the current Git branch or tag, without modifying pom files. It is CI-aware (reads `GITHUB_REF` in GitHub Actions). This is a powerful zero-configuration approach for library projects, but adds a build-time dependency and a configuration file (`.mvn/maven-git-versioning-extension.xml`). For desktop apps, the explicit `versions:set` approach is simpler and more transparent.

### Maven Release Plugin

The traditional Maven release plugin (`maven-release-plugin`) automates version bumps, tagging, and deployment. It creates two commits (release version, then next SNAPSHOT) and a tag. This is heavyweight, assumes a deploy-to-Maven-repo workflow, and produces noisy commit history. It is unnecessary for desktop app distribution.

### Conventional Commits + Auto-Semver

GitHub Actions like `semantic-versioning-maven` can parse conventional commit messages (`feat:`, `fix:`, `feat!:`) and auto-increment the version. This is useful for teams that want fully automated versioning but adds process overhead (enforcing commit message format) and can be surprising when a `feat!:` accidentally bumps the major version. Best suited for library projects with many contributors.

---

## Quick Reference: Version Flow

```
Developer pushes tag v1.2.0
        │
        ▼
GitHub Actions triggers release.yml
        │
        ▼
Extract version: GITHUB_REF_NAME="v1.2.0" → VERSION="1.2.0"
        │
        ▼
mvn versions:set -DnewVersion=1.2.0    (pom.xml updated in CI workspace only)
        │
        ▼
mvn -B package                          (jar is my-app-1.2.0.jar)
        │                               (app.properties contains app.version=1.2.0)
        ▼
jpackage --app-version 1.2.0           (installer shows version 1.2.0)
        │
        ▼
GitHub Release "Release v1.2.0"         (assets: .deb, .msi, .dmg)
```

The version string `1.2.0` flows from the single derivation point (the Git tag) through every layer of the build, into the packaged application, and onto the release page. No manual synchronization required.
