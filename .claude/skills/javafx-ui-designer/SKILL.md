---
name: javafx-ui-designer
description: >
  JavaFX UI/UX design skill — visual design, layout composition, color tokens, typography,
  spacing systems, CSS styling, control theming, accessibility, and usability improvements
  for the Renamer App. Use whenever the task involves styling, beautifying, or improving
  visual appearance — choosing colors, fonts, layouts, spacing, CSS stylesheets, control
  variants, responsive sizing, dark/light themes, or any UI/UX polish. Trigger on phrases
  like "make it look better", "improve the UI", "redesign the layout", "style the controls",
  "create a theme", "CSS for JavaFX", or any visual/aesthetic concern. Complements the
  separate /javafx skill that handles technical/functional concerns (threading, DI, FXML
  loading). When both design and functional changes are needed, use this skill for design
  aspects and /javafx for wiring concerns.
paths: app/ui/**/*.css, app/ui/**/*.fxml, app/ui/**/*.java
allowed-tools: Read, Grep, Glob, Edit, Write
---

# JavaFX UI Designer — Renamer App

> **Purpose**: Guide the design of visually polished, usable, and consistent JavaFX interfaces
> for the Renamer App. Covers layout composition, color palettes, typography, spacing systems,
> CSS styling, control theming, accessibility, and responsive design.
>
> **Supporting files in this directory:**
> - `css-control-patterns.md` — CSS property reference + full control styling patterns (sections 7–8)
> - `theming-and-motion.md` — theming architecture, responsive design, CSS transitions (sections 9, 11–12)

---

## 1. Project Context

```yaml
project_name:       "Renamer App"
javafx_version:     25
java_version:       25
build_tool:         maven
css_location:       app/ui/src/main/resources/styles/     # create this dir; no CSS files exist yet
fxml_location:      app/ui/src/main/resources/fxml/
target_platform:    desktop
design_style:       modern-flat
color_scheme:       light                                  # dark/auto as future option
primary_color:      "#2196F3"                              # placeholder — replace when theme is chosen
secondary_color:    "#FF9800"
base_font_family:   "System"                               # platform default; replace with Inter/Segoe later
base_font_size:     14px
spacing_unit:       8px
min_window_width:   900
min_window_height:  600
scene_builder_used: false                                  # Guice controllers; no fx:controller attribute
existing_theme:     none                                   # Modena default — no AtlantaFX/JMetro
```

### Critical Project Notes

- **No CSS files exist yet.** All current styling is either inline (`node.setStyle(...)`) or
  stored in `ua.renamer.app.ui.enums.TableStyles`. Creating the first CSS file requires adding
  `scene.getStylesheets().add(...)` in `RenamerApplication.start()` before the stage is shown.
- **`TableStyles` enum** holds hardcoded inline style strings for error/ready row states.
  When introducing a CSS file, migrate these to style classes (`.error`, `.ready`) applied
  programmatically in `ApplicationMainViewController`'s row factory.
- **Stylesheet load point**: `RenamerApplication` — the `Application` subclass at
  `ua.renamer.app.RenamerApplication`. Launcher entry point: `ua.renamer.app.Launcher`.
- **CSS file location**: place all stylesheets under `app/ui/src/main/resources/styles/`.
  Load order matters — see `theming-and-motion.md` for the correct sequence.

---

## 2. Design Philosophy

Apply these principles in order of priority:

**Clarity first** — Every element should communicate its purpose immediately. Avoid decorative
complexity that obscures function. Use whitespace generously to let content breathe.

**Consistency** — Establish a design system (colors, spacing, typography scale) and apply it
uniformly. A consistent interface builds trust and reduces cognitive load. Define tokens once
in CSS looked-up colors on `.root` and reference them everywhere.

**Hierarchy** — Guide the user's eye through size, weight, color contrast, and spatial grouping.
Primary actions should be visually prominent; secondary actions should be visually subdued.

**Feedback** — Every interactive element should respond to user interaction. JavaFX CSS supports
pseudo-classes (`:hover`, `:focused`, `:pressed`, `:disabled`, `:selected`) and CSS transitions
(JavaFX 23+, available in this project on JavaFX 25) for smooth state changes.

**Platform respect** — Desktop applications have different expectations than web or mobile.
Users expect keyboard navigation, focus indicators, tooltips, context menus, and window resizing.
Design for these affordances.

---

## 3. Layout System & Composition

### Layout Pane Selection Guide

**VBox** — Stack children vertically with uniform spacing. Use for forms, lists of controls,
sidebar menus, or any top-to-bottom flow. Set `-fx-spacing` for gaps and `-fx-alignment`
for horizontal positioning.

**HBox** — Stack children horizontally. Use for toolbars, button rows, inline form fields.

**BorderPane** — Classic 5-region layout (top, bottom, left, right, center). Use as the root
layout for application windows. The center region grows to fill available space. Toolbars go in
top, status bars in bottom, navigation in left.

**GridPane** — Two-dimensional grid for structured forms and data entry layouts. Use
`columnConstraints` and `rowConstraints` for sizing control. Use `hgap`/`vgap` for gutters.

**StackPane** — Layer children on top of each other. Use for overlays, loading indicators,
or combining a background with foreground elements.

**FlowPane** — Wrapping flow layout. Children wrap to the next row when space runs out.
Use for tag clouds, icon grids, or chip/badge collections.

**TilePane** — Like FlowPane but all tiles are uniform size. Good for dashboards, image
galleries, or card grids.

**AnchorPane** — Pin children to edges with pixel offsets. Use sparingly — creates rigid
layouts that don't adapt to resizing.

### Layout Composition Rules

Nest layouts purposefully:

```
BorderPane (root)
├── top: VBox
│   ├── MenuBar
│   └── HBox (toolbar)
├── left: VBox (sidebar/navigation)
├── center: StackPane
│   └── ScrollPane
│       └── VBox (content)
├── bottom: HBox (status bar)
```

Set grow priorities to control which children expand on resize:

```java
HBox.setHgrow(contentArea, Priority.ALWAYS);
VBox.setVgrow(scrollPane, Priority.ALWAYS);
```

In FXML:

```xml
<HBox>
    <Label text="Fixed" />
    <Region HBox.hgrow="ALWAYS" />  <!-- spacer -->
    <Button text="Action" />
</HBox>
```

> **In this project**: `ApplicationMainView.fxml` uses `BorderPane` as root. Mode views use
> `VBox/HBox/GridPane`. Prefer constraint-based growth over `AnchorPane`. The `FxStateMirror`
> drives `TableView` data — do not replace the `ObservableList` binding on the table.

---

## 4. Color System

### Defining Colors with Looked-Up Colors

JavaFX CSS supports "looked-up colors" — define all tokens on `.root`, reference them everywhere.
This is the foundation of themeable design.

```css
.root {
    /* ── Primary palette ── */
    -fx-primary:          #2196F3;
    -fx-primary-hover:    derive(-fx-primary, -10%);
    -fx-primary-pressed:  derive(-fx-primary, -20%);
    -fx-primary-subtle:   derive(-fx-primary, 85%);

    /* ── Neutral palette ── */
    -fx-bg-base:          #FFFFFF;
    -fx-bg-surface:       #F8F9FA;
    -fx-bg-elevated:      #FFFFFF;
    -fx-border-default:   #DEE2E6;
    -fx-border-subtle:    #E9ECEF;

    /* ── Text palette ── */
    -fx-text-primary:     #212529;
    -fx-text-secondary:   #6C757D;
    -fx-text-tertiary:    #ADB5BD;
    -fx-text-on-primary:  #FFFFFF;

    /* ── Semantic palette ── */
    -fx-success:          #28A745;
    -fx-warning:          #FFC107;
    -fx-danger:           #DC3545;
    -fx-info:             #17A2B8;

    /* ── Assign to built-in looked-up colors ── */
    -fx-base:             -fx-bg-base;
    -fx-background:       -fx-bg-surface;
    -fx-accent:           -fx-primary;
    -fx-focus-color:      derive(-fx-primary, 40%);
    -fx-faint-focus-color: transparent;
}
```

### The `derive()` and `ladder()` Functions

**`derive(color, percentage)`** — Brightens (positive %) or darkens (negative %) a color.
Use to create hover/pressed states from a single base color without manually picking shades.

**`ladder(color, stop1, stop2, ...)`** — Chooses a color based on the brightness of the first
argument. Extremely useful for making text automatically adapt to its background:

```css
.label {
    -fx-text-fill: ladder(-fx-background,
        -fx-text-on-primary 49%,
        -fx-text-primary     50%
    );
}
```

### Color Contrast Guidelines

- Body text: at least 4.5:1 contrast ratio (WCAG AA)
- Large text (18px+ or 14px+ bold): at least 3:1 contrast ratio
- Use `derive()` to generate accessible hover/pressed states from base colors
- Test palette in both light and dark contexts if supporting both themes

### Anti-Patterns

- Never hardcode hex colors directly on controls — always reference looked-up color tokens
- Avoid pure black (`#000000`) on pure white (`#FFFFFF`) — use slightly tinted neutrals
- Avoid more than 3–4 distinct hues — rely on shades/tints of a few base colors

---

## 5. Typography

### Font Loading

```java
// Load before scene creation
Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Regular.ttf"), 14);
Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Bold.ttf"), 14);
```

```css
@font-face {
    font-family: 'Inter';
    src: url('/fonts/Inter-Regular.ttf');
}
```

### Type Scale

Define on `.root` and apply via style classes. 1.25 ratio (Major Third) for harmonious sizing:

```css
.root {
    -fx-font-family:      "System";
    -fx-font-size:        14px;

    /* ── Type scale (1.25 ratio) ── */
    -fx-font-size-xs:     10px;
    -fx-font-size-sm:     12px;
    -fx-font-size-base:   14px;
    -fx-font-size-md:     16px;
    -fx-font-size-lg:     18px;
    -fx-font-size-xl:     22px;
    -fx-font-size-2xl:    28px;
    -fx-font-size-3xl:    34px;
}

.h1 {
    -fx-font-size: -fx-font-size-3xl;
    -fx-font-weight: bold;
    -fx-text-fill: -fx-text-primary;
}

.h2 {
    -fx-font-size: -fx-font-size-2xl;
    -fx-font-weight: bold;
    -fx-text-fill: -fx-text-primary;
}

.h3 {
    -fx-font-size: -fx-font-size-xl;
    -fx-font-weight: 600;
    -fx-text-fill: -fx-text-primary;
}

.subtitle {
    -fx-font-size: -fx-font-size-md;
    -fx-text-fill: -fx-text-secondary;
}

.caption {
    -fx-font-size: -fx-font-size-xs;
    -fx-text-fill: -fx-text-tertiary;
}

.monospace {
    -fx-font-family: "JetBrains Mono", "Consolas", monospace;
}
```

### Typography Best Practices

- Set `-fx-font-size` on `.root` to establish the base — all em-relative sizes scale from it
- Use no more than 2 font families (one for UI, one for code/data)
- Limit font weights to regular (400) and bold (700); semi-bold (600) for subheadings
- JavaFX text properties: `-fx-font-family`, `-fx-font-size`, `-fx-font-weight`,
  `-fx-font-style`, `-fx-text-fill`, `-fx-text-alignment`, `-fx-line-spacing`

---

## 6. Spacing & Sizing System

### Spacing Scale

Use 8px as base unit. Define as looked-up values on `.root`:

```css
.root {
    /* ── Spacing scale (8px base) ── */
    -fx-spacing-xs:   4px;
    -fx-spacing-sm:   8px;
    -fx-spacing-md:   12px;
    -fx-spacing-lg:   16px;
    -fx-spacing-xl:   24px;
    -fx-spacing-2xl:  32px;
    -fx-spacing-3xl:  48px;
}
```

Apply via style classes or directly:

```css
.card {
    -fx-padding: -fx-spacing-lg;
    -fx-spacing: -fx-spacing-md;
}
```

### Sizing Controls

Three constraints: `minWidth/Height`, `prefWidth/Height`, `maxWidth/Height`.

- Set `prefWidth/Height` for ideal size
- Set `maxWidth` to `Infinity` in CSS (or `Double.MAX_VALUE` in Java) to allow stretching
- Use `Region.USE_COMPUTED_SIZE` (-1) to let JavaFX compute automatically
- Avoid hardcoding pixel sizes unless the element truly must be fixed

```css
.stretch-button {
    -fx-max-width: Infinity;
}

.sidebar {
    -fx-pref-width: 240px;
    -fx-min-width: 200px;
    -fx-max-width: 280px;
}
```

### The Box Model in JavaFX

Every `Region` is painted bottom to top:

```
┌─────────────────────────────────────┐
│           background-color          │
│  ┌───────────────────────────────┐  │  ← background-insets
│  │         border-color          │  │
│  │  ┌─────────────────────────┐  │  │  ← border-width + border-insets
│  │  │        padding          │  │  │
│  │  │  ┌───────────────────┐  │  │  │  ← padding
│  │  │  │    CONTENT AREA   │  │  │  │
│  │  │  └───────────────────┘  │  │  │
│  │  └─────────────────────────┘  │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

Multiple backgrounds can be layered with comma-separated values for depth effects:

```css
.card-elevated {
    -fx-background-color:
        derive(-fx-border-default, -5%),   /* outer shadow layer */
        -fx-border-default,                 /* border layer */
        -fx-bg-elevated;                    /* content background */
    -fx-background-insets: -1, 0, 1;
    -fx-background-radius:  9,  8, 7;
}
```

---

## 10. Accessibility & Usability

### Focus Indicators

Never remove focus indicators entirely. Style them to match your design:

```css
.button:focused {
    -fx-border-color: -fx-primary;
    -fx-border-width: 2px;
}
```

Override global focus appearance on `.root`:

```css
.root {
    -fx-focus-color: derive(-fx-primary, 20%);
    -fx-faint-focus-color: transparent;  /* removes outer glow */
}
```

### Keyboard Navigation

- All interactive controls must be reachable via Tab / Shift+Tab
- Use `focusTraversable="true"` on custom interactive nodes
- Group related controls so Tab order is logical (top-to-bottom, left-to-right)
- Provide mnemonics: `_Save` creates Alt+S shortcut
- Use `accelerator` properties for keyboard shortcuts

### Tooltips

Add tooltips to icon-only buttons and controls whose purpose isn't immediately obvious:

```css
.tooltip {
    -fx-background-color: -fx-text-primary;
    -fx-text-fill: -fx-bg-base;
    -fx-font-size: -fx-font-size-sm;
    -fx-background-radius: 4px;
    -fx-padding: 4px 8px;
}
```

> **Existing pattern**: controllers create `Tooltip` in Java via
> `control.setTooltip(new Tooltip(...))`. The CSS `.tooltip { }` rule styles them globally
> without changing any Java code.

### Minimum Target Sizes

Interactive elements should have a minimum clickable area of 32×32 pixels:

```css
.icon-button {
    -fx-min-width: 36px;
    -fx-min-height: 36px;
    -fx-padding: 8px;
}
```

### Color Accessibility

- Don't convey information through color alone — combine with icons, text, or patterns
- Test color palettes for colorblind accessibility (protanopia, deuteranopia, tritanopia)
- Error states must have both color change AND an icon/text indicator

---

## 13. Design Audit Checklist

### Color & Contrast
- [ ] All colors come from looked-up color tokens defined on `.root`
- [ ] No hardcoded hex colors on individual controls
- [ ] Text has sufficient contrast against its background (4.5:1 minimum)
- [ ] Color is not the sole indicator of state (error, success, etc.)
- [ ] Hover and pressed states are visually distinct from default state

### Typography
- [ ] A single base font size is set on `.root`
- [ ] A consistent type scale is used (no arbitrary font sizes)
- [ ] No more than 2 font families in use
- [ ] Text truncation is handled gracefully (`-fx-text-overrun: ellipsis`)
- [ ] Long text wraps or scrolls where appropriate

### Spacing & Layout
- [ ] Consistent spacing scale is applied (not arbitrary padding values)
- [ ] Visual grouping through spacing: related items are closer, groups are separated
- [ ] Content doesn't touch container edges (adequate padding on all regions)
- [ ] Layout adapts when window is resized (no overlap, no hidden content)
- [ ] Minimum window size is set and prevents broken layouts

### Interactivity
- [ ] All interactive elements have `:hover` state
- [ ] Focus indicators are visible and styled appropriately
- [ ] Disabled states are visually distinct (reduced opacity)
- [ ] Clickable items use hand cursor (`-fx-cursor: hand`)
- [ ] Interactive targets are at least 32×32px
- [ ] Tooltips on icon-only buttons

### Consistency
- [ ] All buttons of the same type look identical
- [ ] Form fields have uniform height, border radius, and padding
- [ ] Spacing between form labels and fields is uniform
- [ ] Card/panel styling is consistent across all views
- [ ] One primary action style per view (visual hierarchy is clear)

---

## 14. Common Patterns & Anti-Patterns

### Patterns (Do This)

**Token-based design** — Define all visual values (colors, sizes, radii, spacing) as looked-up
colors on `.root`. Makes themes trivial and ensures consistency.

**Layered backgrounds for depth** — Use multiple `-fx-background-color` values with
`-fx-background-insets` to create border and shadow effects. Performs better than `-fx-effect`.

**Style class composition** — Give controls multiple style classes that combine:

```xml
<Button styleClass="button, primary, large" text="Submit" />
```

```css
.large { -fx-padding: 12px 24px; -fx-font-size: -fx-font-size-lg; }
```

**CSS-only states** — Use pseudo-classes and looked-up colors to express all states in CSS.
Add/remove style classes for application states (`.error`, `.success`, `.loading`).

**Consistent border radius** — Pick 2–3 values and use them everywhere:

```css
.root {
    -fx-radius-sm:   4px;
    -fx-radius-md:   8px;
    -fx-radius-lg:   12px;
    -fx-radius-full: 100px;  /* for pills and circles */
}
```

### Anti-Patterns (Avoid This)

**Inline styles in Java** — `node.setStyle("-fx-background-color: red")` creates unmaintainable,
unthemeable UI. Use style classes instead.

**Overriding `-fx-base` per control** — Changing `-fx-base` on individual controls cascades
unpredictably through Modena's derived colors. Use specific properties instead.

**`-fx-effect: dropshadow(...)` everywhere** — Drop shadow effects are expensive. Use
background-color layering for card borders; reserve actual effects for dialogs and popovers.

**Ignoring Modena defaults** — Fighting the default stylesheet causes inconsistencies. Either
commit to a full custom theme that resets all controls, or work within Modena's structure.

**`AnchorPane` for everything** — Creates rigid, non-responsive layouts. Prefer VBox/HBox with
grow priorities, BorderPane for page structure, and GridPane for forms.

**Pixel-perfect fixed layouts** — JavaFX runs on varying screen sizes and DPI settings. Design
with flexible sizing and test at different window sizes.

---

## Official References

- [JavaFX CSS Reference Guide (v26)](https://openjfx.io/javadoc/26/javafx.graphics/javafx/scene/doc-files/cssref.html)
  — Complete list of all CSS properties, types, and pseudo-classes per control
- [Introduction to FXML (v26)](https://openjfx.io/javadoc/26/javafx.fxml/javafx/fxml/doc-files/introduction_to_fxml.html)
  — Markup language for defining JavaFX scene graphs declaratively
- [JavaFX API Documentation (v26)](https://openjfx.io/javadoc/26/)
  — Full Javadoc for all JavaFX modules
- [JavaFX CSS Reference Guide (v11)](https://openjfx.io/javadoc/11/javafx.graphics/javafx/scene/doc-files/cssref.html)
  — Reference for older LTS version compatibility
- [OpenJFX Home](https://openjfx.io/) — Downloads, community projects, and getting started guides
- [Scene Builder](https://gluonhq.com/products/scene-builder/) — Visual FXML editor for layout design
