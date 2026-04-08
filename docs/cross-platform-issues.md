# Cross-Platform Build Issues

macOS builds (Intel and Apple Silicon) are stable — all tests pass and native packaging works.
Linux and Windows (tested on ARM64 VMs) have three known environment-specific issues documented here.
These are not application bugs; they are build/test environment differences to resolve per platform.

---

## Issue 1 — `-Xdock:name=Renamer` breaks non-macOS Maven builds

**File:** `app/.mvn/jvm.config`

`-Xdock:name=Renamer` is a macOS-only JVM argument that sets the app name in the Dock.
The JVM on Linux and Windows does not recognise it, so Maven fails at startup on those platforms.

**Workaround:** Remove the flag from `jvm.config` before building on Linux/Windows.

**Likely fix:** Move the flag into a Maven profile activated only on macOS:

```xml
<profile>
  <id>macos</id>
  <activation><os><family>mac</family></os></activation>
  <properties>
    <jvm.extra.args>-Xdock:name=Renamer</jvm.extra.args>
  </properties>
</profile>
```

Alternatively, pass it only via `cd app/ui && mvn javafx:run` where it is actually needed,
rather than globally in `jvm.config`.

---

## Issue 2 — `DuplicateResolutionIntegrationTest` fails on Linux

**Test:** `DuplicateResolutionIntegrationTest.testDuplicates_SameNameDifferentExtensions`
**Error:** `File should exist: 002.pdf — expected: <true> but was: <false>`

The test creates `file.doc`, `file.pdf`, `file.txt`, sorts them by `SortSource.FILE_NAME`,
and assigns sequence numbers. The assertions expect reverse-alphabetical order
(`txt → 001`, `pdf → 002`, `doc → 003`), but the test comment says "sorted alphabetically" —
an internal inconsistency that hints the sort behaviour is not platform-neutral.

On macOS (APFS, `en_US` locale) the observed ordering matches the assertions.
On Linux (ext4, potentially different locale/collation) the ordering differs,
so `pdf` gets a different sequence number and the assertion fails.

**Things to investigate on Linux before fixing:**

1. Run `locale` — collation (`LC_COLLATE`) affects `String.compareTo` and thus any
   comparator built on it.
2. Check whether `SortSource.FILE_NAME` compares the full filename (including extension)
   or the stem only — the `file.*` group all share the same stem.
3. Check whether `getAllFilesInTempDir()` pre-sorts results before they reach the
   orchestrator sort, and whether that pre-sort order differs across filesystems.

**Likely fix:** Make the sort in `SortSource.FILE_NAME` explicitly locale-independent
(`Comparator.comparing(f -> f.getName(), Comparator.naturalOrder())` with a fixed
`Locale.ROOT` collator), and correct the test comment to match the actual expected order.

---

## Issue 3 — `scripts/package-linux.sh` fails with `--linux-shortcut` / `--linux-menu-group`

**Symptom:** `jpackage` reports an error on `--linux-shortcut` and/or
`--linux-menu-group "Utility"` when building the `.deb` package.

**Workaround:** Remove both flags to build a raw app-image; `.deb` packaging is skipped.

**Likely causes (investigate in order):**

1. **Missing system dependencies** — `jpackage` on Linux requires `fakeroot` and `dpkg`
   to produce `.deb` packages. Without them the tool may fail before processing flags.
   Install with: `sudo apt-get install fakeroot dpkg`.

2. **JDK version compatibility** — some `jpackage` versions between JDK 21–24 changed
   or removed certain Linux-specific flags. Run `jpackage --help` on the target machine
   and confirm `--linux-shortcut` and `--linux-menu-group` are listed.

3. **XDG category spelling** — `--linux-menu-group "Utility"` must match a valid XDG
   application category exactly. The canonical name is `Utility` (singular), but some
   desktop environments expect `Utilities`. Check the installed JDK's behaviour.

4. **ARM64 support** — `jpackage` `.deb` generation on Linux ARM64 may have limitations
   in older JDK builds. Verify the JDK version with `java -version` and check if a newer
   build resolves the issue.
