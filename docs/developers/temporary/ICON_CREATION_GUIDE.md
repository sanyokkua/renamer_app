# App Icon Creation Guide

How to create correctly sized and formatted app icons for macOS, Windows, and Linux so they render without artifacts in installers, the Dock, taskbar, and desktop environments.

---

## The One Rule That Prevents All Problems

> **Fill the entire canvas. Let the OS apply shape and rounding.**

Every platform clips or masks your icon at render time. If you pre-apply rounded corners (leaving transparent pixels in the corners), the OS places a white or gray background behind your icon, making the transparent areas visible as dead space.

Correct source icon:

```
┌──────────────┐   1024×1024, fully opaque, no transparency anywhere.
│▓▓▓▓▓▓▓▓▓▓▓▓│   Artwork bleeds to every edge.
│▓▓▓▓▓▓▓▓▓▓▓▓│   OS clips corners and adds shadow at render time.
│▓▓▓▓▓▓▓▓▓▓▓▓│
└──────────────┘
```

Wrong source icon (what causes the white-gap problem):

```
┌──────────────┐   Transparent corners (shown as ░).
│░░╭────────╮░░│   The OS fills those transparent areas with its
│░░│▓▓▓▓▓▓▓│░░│   background color, creating a double-rounded-rect
│░░╰────────╯░░│   / white-box artifact.
└──────────────┘
```

---

## macOS — `.icns`

### Canvas rules

| Rule | Detail |
|------|--------|
| Source size | 1024×1024 px minimum |
| Background | Fully opaque — zero transparent pixels |
| Rounded corners | Do **not** pre-apply. macOS adds them (and a shadow) automatically |
| Color space | sRGB |
| Format | PNG → compiled to `.icns` via `iconutil` |

macOS uses a squircle (superellipse) mask. The effective corner radius is approximately **22.5% of the icon width** (≈230 px on a 1024 px canvas). Your artwork may extend into those corners — macOS will clip them cleanly.

### Required sizes

The `.iconset` folder must contain exactly these files:

| Filename | Pixel dimensions | Usage |
|----------|-----------------|-------|
| `icon_16x16.png` | 16×16 | Finder list view |
| `icon_16x16@2x.png` | 32×32 | Retina Finder list view |
| `icon_32x32.png` | 32×32 | Finder small icon view |
| `icon_32x32@2x.png` | 64×64 | Retina small icon |
| `icon_128x128.png` | 128×128 | Finder icon view |
| `icon_128x128@2x.png` | 256×256 | Retina Finder |
| `icon_256x256.png` | 256×256 | Finder large icon |
| `icon_256x256@2x.png` | 512×512 | Retina large icon |
| `icon_512x512.png` | 512×512 | Preview, sharing |
| `icon_512x512@2x.png` | 1024×1024 | Retina preview, App Store |

### Building the `.icns`

```bash
# 1. Create the iconset folder (the name must end in .iconset)
mkdir Renamer.iconset

# 2. Export all required sizes from your design tool into Renamer.iconset/
#    using the filenames above.

# 3. Compile
iconutil --convert icns Renamer.iconset -o icon.icns
```

`iconutil` is built into macOS — no installation required.

### Verifying the result

```bash
# Extract back to PNG to inspect
iconutil --convert iconset icon.icns -o verify.iconset
open verify.iconset/icon_512x512@2x.png
```

The extracted PNG must show artwork filling corner-to-corner with no dark/transparent areas.

---

## Windows — `.ico`

### Canvas rules

| Rule | Detail |
|------|--------|
| Background | Transparent is fine — Windows renders on its own background |
| Rounded corners | Do **not** pre-apply. Windows does not add them automatically, but the taskbar/Explorer will composite against its background |
| Format | `.ico` (multi-resolution container) |

### Required sizes

A Windows `.ico` file is a multi-image container. Include all of these:

| Size | Priority | Used for |
|------|----------|---------|
| 16×16 | Required | Explorer list view, taskbar small |
| 24×24 | Recommended | Taskbar medium (older Windows) |
| 32×32 | Required | Explorer medium icons |
| 48×48 | Required | Explorer large icons |
| 64×64 | Recommended | Explorer extra large |
| 128×128 | Recommended | High-DPI screens |
| 256×256 | Required | Explorer jumbo icons, modern Windows |

The 256×256 entry is stored as PNG inside the `.ico` container on modern Windows — this is normal and expected.

### Creating the `.ico`

**Option A — GIMP (free, cross-platform):**
1. Open your 1024×1024 source PNG in GIMP.
2. **Image → Scale Image** to 256×256.
3. **File → Export As** → name it `icon.ico` → click Export.
4. In the ICO export dialog, check all desired sizes — GIMP will downsample from your image.

**Option B — paint.net with the IcoFX plugin (Windows, free):**
1. Open your PNG.
2. File → Save As → `.ico`.
3. The plugin dialog lets you select which sizes to embed.

**Option C — online converter:**
Tools such as [icoconvert.com](https://icoconvert.com) or [convertico.com](https://convertico.com) accept a PNG and produce a multi-size `.ico`. Upload your 1024×1024 source and select all sizes listed above.

### Verifying the result

On Windows: right-click the `.ico` → Properties → Details. The file should show multiple image resolutions listed.

On macOS you can inspect with:
```bash
file icon.ico        # shows: MS Windows icon resource
sips -g pixelWidth icon.ico
```

---

## Linux — `.png`

### Canvas rules

| Rule | Detail |
|------|--------|
| Background | Transparent is fine — desktop environments composite it |
| Format | PNG |
| Size for jpackage | 512×512 recommended; 256×256 minimum |

Linux desktop environments (GNOME, KDE, XFCE) follow the freedesktop.org Icon Theme Specification. jpackage installs the icon into `/usr/share/icons/hicolor/` at the sizes it finds in the `.png`.

### Required sizes (for manual installation)

If you are distributing via `.deb` or `.rpm` and want proper icon theme integration, provide these sizes as separate files:

```
/usr/share/icons/hicolor/16x16/apps/renamer.png
/usr/share/icons/hicolor/32x32/apps/renamer.png
/usr/share/icons/hicolor/48x48/apps/renamer.png
/usr/share/icons/hicolor/128x128/apps/renamer.png
/usr/share/icons/hicolor/256x256/apps/renamer.png
/usr/share/icons/hicolor/512x512/apps/renamer.png
/usr/share/icons/hicolor/scalable/apps/renamer.svg   ← best option if you have SVG source
```

For jpackage-based `.deb` packaging, passing a single high-resolution PNG (`512×512` or larger) via `--icon` is sufficient — jpackage handles the installation.

---

## Recommended Design Workflow

### 1. Design at 1024×1024

Use any vector or raster tool (Figma, Sketch, Affinity Designer, Adobe Illustrator, Inkscape). Work at 1024×1024 px with:
- A fully opaque background that extends to all four edges.
- No pre-applied rounded corners.
- No drop shadow (OS applies its own).

### 2. Export a single master PNG

Export one `icon_source_1024.png` at 1024×1024. This is your source of truth — keep it in the repository alongside the compiled icon files.

### 3. Generate platform files from the master

**macOS**: Export all 10 iconset sizes (see table above) and run `iconutil`.

**Windows**: Use GIMP, paint.net, or an online converter to produce `icon.ico` from the master.

**Linux**: The 1024×1024 master PNG can be used directly as `icon.png` for jpackage.

---

## Quick Checklist Before Compiling Icons

- [ ] Source PNG is 1024×1024 (or larger)
- [ ] Zero transparent pixels — the entire canvas is opaque
- [ ] Artwork fills all four edges — no pre-applied rounded corners
- [ ] All 10 `.iconset` filenames are correct (no typos, correct `@2x` suffixes)
- [ ] `iconutil` compiled without errors
- [ ] Extracted and visually verified `icon_512x512@2x.png` — no dark/transparent corners

---

## Common Mistakes

| Mistake | Effect | Fix |
|---------|--------|-----|
| Pre-applied rounded corners (transparent corners in source) | White box / double-rounded-rect artifact in DMG window and Finder | Fill canvas edge-to-edge; let OS apply shape |
| Semi-transparent pixels at rounded-rect edges (anti-aliasing) | Faint colored fringe around icon on light backgrounds | Ensure background color is solid under all anti-aliased pixels |
| Wrong `@2x` filename (e.g. `icon_512x512_2x.png`) | `iconutil` silently ignores the file — that resolution missing from `.icns` | Follow the exact naming convention in the table above |
| Missing sizes in `.ico` | Icon looks blurry or pixelated at small sizes on Windows | Always include 16, 32, 48, 256 at minimum |
| Using `.icns` for Linux jpackage | jpackage on Linux does not understand `.icns` | Pass a `.png` to `--icon` on Linux |

---

## References

- [Apple Human Interface Guidelines — App Icons](https://developer.apple.com/design/human-interface-guidelines/app-icons)
- [Apple iconutil man page](https://www.unix.com/man-page/osx/1/iconutil/)
- [freedesktop.org Icon Theme Specification](https://specifications.freedesktop.org/icon-theme-spec/icon-theme-spec-latest.html)
- [Microsoft Windows App Icon Guidelines](https://learn.microsoft.com/en-us/windows/apps/design/style/iconography/app-icon-design)
