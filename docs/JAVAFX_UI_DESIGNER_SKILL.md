---
name: javafx-ui-designer
description: >
  JavaFX UI/UX design skill focused on visual design, layout composition, color theory, typography,
  spacing, CSS styling, and usability improvements for JavaFX desktop applications. Use this skill
  whenever the user asks to design, style, beautify, theme, or improve the visual appearance of a
  JavaFX application — including choosing colors, fonts, layouts, spacing, CSS stylesheets, control
  styling, responsive design, accessibility, dark/light themes, or any UI/UX polish. Also trigger
  when the user mentions "make it look better", "improve the UI", "redesign the layout",
  "style the controls", "create a theme", "CSS for JavaFX", or any visual/aesthetic concern
  in a JavaFX context. This skill is design-focused and complements a separate JavaFX developer
  skill that handles technical/functional concerns. When both design and functional changes are
  needed, use this skill for the design aspects.
---

# JavaFX UI Designer Skill

> **Purpose**: Guide the design of visually polished, usable, and consistent JavaFX application
> interfaces. This skill covers layout composition, color palettes, typography, spacing systems,
> CSS styling, control theming, accessibility, and responsive design — all within the JavaFX
> framework's capabilities.

---

## Table of Contents

1. [Project Context](#1-project-context)
2. [Design Philosophy](#2-design-philosophy)
3. [Layout System & Composition](#3-layout-system--composition)
4. [Color System](#4-color-system)
5. [Typography](#5-typography)
6. [Spacing & Sizing System](#6-spacing--sizing-system)
7. [JavaFX CSS Styling Reference](#7-javafx-css-styling-reference)
8. [Control Styling Patterns](#8-control-styling-patterns)
9. [Theming Architecture](#9-theming-architecture)
10. [Accessibility & Usability](#10-accessibility--usability)
11. [Responsive & Adaptive Design](#11-responsive--adaptive-design)
12. [CSS Transitions & Micro-Interactions](#12-css-transitions--micro-interactions)
13. [Design Audit Checklist](#13-design-audit-checklist)
14. [Common Patterns & Anti-Patterns](#14-common-patterns--anti-patterns)

---

## 1. Project Context

<!-- TEMPLATE: Replace these placeholders with your project's actual values -->

```yaml
project_name: "{{PROJECT_NAME}}"
javafx_version: "{{JAVAFX_VERSION}}"            # e.g., 21, 22, 23, 26
java_version: "{{JAVA_VERSION}}"                # e.g., 21, 22, 23
build_tool: "{{BUILD_TOOL}}"                    # maven | gradle
css_location: "{{CSS_PATH}}"                    # e.g., src/main/resources/styles/
fxml_location: "{{FXML_PATH}}"                  # e.g., src/main/resources/fxml/
target_platform: "{{TARGET_PLATFORM}}"          # desktop | embedded | cross-platform
design_style: "{{DESIGN_STYLE}}"               # modern-flat | material | native | custom
color_scheme: "{{COLOR_SCHEME}}"               # light | dark | auto | both
primary_color: "{{PRIMARY_COLOR}}"             # e.g., #2196F3
secondary_color: "{{SECONDARY_COLOR}}"         # e.g., #FF9800
base_font_family: "{{FONT_FAMILY}}"            # e.g., "Segoe UI", "Inter", system
base_font_size: "{{BASE_FONT_SIZE}}"           # e.g., 14px
spacing_unit: "{{SPACING_UNIT}}"               # e.g., 8px (base unit for spacing scale)
min_window_width: "{{MIN_WIDTH}}"              # e.g., 800
min_window_height: "{{MIN_HEIGHT}}"            # e.g., 600
scene_builder_used: "{{SCENE_BUILDER}}"        # true | false
existing_theme: "{{EXISTING_THEME}}"           # none | jmetro | atlantafx | materialfx | custom
```

---

## 2. Design Philosophy

When designing JavaFX interfaces, follow these principles in order of priority:

**Clarity first** — Every element should communicate its purpose immediately. Avoid decorative
complexity that obscures function. Use whitespace generously to let content breathe.

**Consistency** — Establish a design system (colors, spacing, typography scale) and apply it
uniformly. A consistent interface builds trust and reduces cognitive load. Define your tokens
once in CSS variables (via the `.root` selector's looked-up colors) and reference them everywhere.

**Hierarchy** — Guide the user's eye through size, weight, color contrast, and spatial grouping.
Primary actions should be visually prominent; secondary actions should be visually subdued.

**Feedback** — Every interactive element should respond to user interaction. JavaFX CSS supports
pseudo-classes (`:hover`, `:focused`, `:pressed`, `:disabled`, `:selected`) and, since JavaFX 23+,
CSS transitions for smooth state changes.

**Platform respect** — Desktop applications have different expectations than web or mobile.
Users expect keyboard navigation, focus indicators, tooltips, context menus, and window resizing.
Design for these affordances.

---

## 3. Layout System & Composition

JavaFX provides layout panes that each serve a specific composition purpose. Choosing the right
pane is fundamental to good UI structure. Here is when to use each:

### Layout Pane Selection Guide

**VBox** — Stack children vertically with uniform spacing. Use for forms, lists of controls,
sidebar menus, or any top-to-bottom flow. Set `-fx-spacing` for consistent gaps and
`-fx-alignment` for horizontal positioning of children.

**HBox** — Stack children horizontally. Use for toolbars, button rows, inline form fields, or
horizontal groupings. Same spacing and alignment properties as VBox.

**BorderPane** — Classic 5-region layout (top, bottom, left, right, center). Use as the root
layout for application windows. The center region grows to fill available space — put your
primary content there. Toolbars go in top, status bars in bottom, navigation in left.

**GridPane** — Two-dimensional grid for structured forms and data entry layouts. Assign children
to specific row/column positions. Use `GridPane.columnConstraints` and `GridPane.rowConstraints`
to control sizing behavior (fixed, percentage, or computed widths/heights). Use `GridPane.hgap`
and `GridPane.vgap` for consistent gutters.

**StackPane** — Layer children on top of each other. Use for overlays, loading indicators over
content, centered content, or combining a background with foreground elements. Set
`-fx-alignment` to position the stacked children.

**FlowPane** — Wrapping flow layout. Children wrap to the next row (or column) when the pane runs
out of space. Use for tag clouds, icon grids, or chip/badge collections.

**TilePane** — Similar to FlowPane but all tiles are uniform size. Good for dashboards,
image galleries, or card grids.

**AnchorPane** — Pin children to edges with specific pixel offsets. Use sparingly — it creates
rigid layouts that don't adapt well to resizing. Prefer constraint-based layouts (VBox/HBox with
`HBox.hgrow` / `VBox.vgrow` priorities) for responsive designs.

### Layout Composition Rules

Nest layouts purposefully. A typical application structure looks like this:

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

Set grow priorities to control which children expand when the window resizes:

```java
HBox.setHgrow(contentArea, Priority.ALWAYS);
VBox.setVgrow(scrollPane, Priority.ALWAYS);
```

In FXML, achieve the same with static property attributes:

```xml
<HBox>
    <Label text="Fixed" />
    <Region HBox.hgrow="ALWAYS" />  <!-- spacer -->
    <Button text="Action" />
</HBox>
```

---

## 4. Color System

### Defining Colors with Looked-Up Colors

JavaFX CSS supports "looked-up colors" — a powerful mechanism for building a color token system.
Define all your colors on the `.root` style class, then reference them anywhere in your stylesheet.
This is the foundation of themeable design.

```css
.root {
    /* ── Primary palette ── */
    -fx-primary:          {{PRIMARY_COLOR}};
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

JavaFX CSS provides two powerful color functions:

**`derive(color, percentage)`** — Brightens (positive %) or darkens (negative %) a color.
Use this to create hover/pressed states from a single base color without manually picking shades.

**`ladder(color, stop1, stop2, ...)`** — Chooses a color from a gradient based on the brightness
of the first argument. This is extremely useful for making text automatically adapt to its
background brightness:

```css
.label {
    -fx-text-fill: ladder(-fx-background,
        -fx-text-on-primary 49%,
        -fx-text-primary     50%
    );
}
```

### Color Contrast Guidelines

For readable text, maintain sufficient contrast between text and background:
- Body text: aim for at least 4.5:1 contrast ratio (WCAG AA)
- Large text (18px+ or 14px+ bold): at least 3:1 contrast ratio
- Use `derive()` to generate accessible hover and pressed states from your base colors
- Test your palette in both light and dark contexts if supporting both themes

### Anti-Patterns

- Avoid hardcoding hex colors directly on controls — always reference looked-up color tokens
- Avoid pure black (`#000000`) text on pure white (`#FFFFFF`) backgrounds — use slightly tinted
  neutrals for a softer, more comfortable appearance
- Avoid using more than 3-4 distinct hues in your palette — rely on shades/tints of a few base
  colors

---

## 5. Typography

### Font Loading

JavaFX supports loading custom fonts at runtime or via CSS `@font-face`:

```java
// In code — load before scene creation
Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Regular.ttf"), 14);
Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Bold.ttf"), 14);
```

```css
/* Via CSS @font-face */
@font-face {
    font-family: 'Inter';
    src: url('/fonts/Inter-Regular.ttf');
}
```

### Type Scale

Define a consistent type scale on `.root` and apply via style classes. Use a modular scale
(e.g., 1.25 ratio — "Major Third") for harmonious sizing:

```css
.root {
    -fx-font-family:      "{{FONT_FAMILY}}";
    -fx-font-size:        {{BASE_FONT_SIZE}};

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

/* ── Heading classes ── */
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

- Set `-fx-font-size` on `.root` to establish the base — all relative sizes (em) will scale from it
- Use no more than 2 font families (one for UI, one for code/data)
- Limit font weights to regular (400) and bold (700); occasionally semi-bold (600) for subheadings
- JavaFX CSS properties for text: `-fx-font-family`, `-fx-font-size`, `-fx-font-weight`,
  `-fx-font-style`, `-fx-text-fill`, `-fx-text-alignment`, `-fx-line-spacing`

---

## 6. Spacing & Sizing System

### Spacing Scale

Use a base unit (typically 4px or 8px) and build a scale. Define as looked-up values on `.root`,
then apply consistently:

```css
.root {
    /* ── Spacing scale ({{SPACING_UNIT}} base) ── */
    -fx-spacing-xs:   4px;
    -fx-spacing-sm:   8px;
    -fx-spacing-md:   12px;
    -fx-spacing-lg:   16px;
    -fx-spacing-xl:   24px;
    -fx-spacing-2xl:  32px;
    -fx-spacing-3xl:  48px;
}
```

Apply these via style classes or directly:

```css
.card {
    -fx-padding: -fx-spacing-lg;
    -fx-spacing: -fx-spacing-md;
}

.section-gap {
    -fx-padding: -fx-spacing-2xl 0 0 0;
}
```

### Sizing Controls

JavaFX controls have three sizing constraints: `minWidth/Height`, `prefWidth/Height`,
`maxWidth/Height`. For responsive design:

- Set `prefWidth` / `prefHeight` for the ideal size
- Set `maxWidth` to `Double.MAX_VALUE` (or `Infinity` in FXML / `MAX_VALUE` in CSS) to allow
  the control to stretch
- Use `Region.USE_COMPUTED_SIZE` (-1 in CSS) to let JavaFX compute sizes automatically
- Avoid hardcoding pixel sizes unless the element truly must be fixed-size

```css
/* Let buttons stretch to fill their container */
.stretch-button {
    -fx-max-width: Infinity;
}

/* Fixed-width sidebar */
.sidebar {
    -fx-pref-width: 240px;
    -fx-min-width: 200px;
    -fx-max-width: 280px;
}
```

### The Box Model in JavaFX

Every `Region` in JavaFX follows this layering model (painted bottom to top):

1. **Background fills** — one or more colored rectangles (`-fx-background-color`)
2. **Background images** — images painted over fills (`-fx-background-image`)
3. **Border strokes** — stroke lines around the region (`-fx-border-color`)
4. **Border images** — images used as borders (`-fx-border-image-source`)
5. **Content area** — where children are laid out

The relationship between background, border, and padding:

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

Multiple backgrounds and borders can be layered with comma-separated values. This is a common
technique for creating visual depth effects (shadows, inner glows) purely in CSS:

```css
.card-elevated {
    -fx-background-color:
        derive(-fx-border-default, -5%),   /* outer shadow layer */
        -fx-border-default,                 /* border layer */
        -fx-bg-elevated;                    /* content background */
    -fx-background-insets:
        -1,                                 /* shadow extends 1px beyond */
        0,                                  /* border at edge */
        1;                                  /* content inset by 1px (border width) */
    -fx-background-radius:
        9,                                  /* shadow radius */
        8,                                  /* border radius */
        7;                                  /* content radius (slightly less) */
}
```

---

## 7. JavaFX CSS Styling Reference

### CSS Property Naming Convention

JavaFX CSS properties use the `-fx-` vendor prefix. The naming convention converts camelCase Java
property names to hyphenated lowercase CSS names:

- Java `blendMode` → CSS `-fx-blend-mode`
- Java `backgroundColor` → CSS `-fx-background-color`

### Key CSS Properties by Node Type

**Node (all nodes inherit these)**:
`-fx-opacity`, `-fx-cursor`, `-fx-blend-mode`, `-fx-effect`, `-fx-rotate`,
`-fx-scale-x`, `-fx-scale-y`, `-fx-translate-x`, `-fx-translate-y`

**Region (all layout panes and controls)**:
`-fx-background-color`, `-fx-background-radius`, `-fx-background-insets`,
`-fx-background-image`, `-fx-background-position`, `-fx-background-repeat`,
`-fx-background-size`, `-fx-border-color`, `-fx-border-width`, `-fx-border-radius`,
`-fx-border-insets`, `-fx-border-style`, `-fx-padding`, `-fx-min-width`, `-fx-pref-width`,
`-fx-max-width`, `-fx-min-height`, `-fx-pref-height`, `-fx-max-height`, `-fx-shape`,
`-fx-snap-to-pixel`

**Labeled (Label, Button, CheckBox, RadioButton, etc.)**:
`-fx-text-fill`, `-fx-font`, `-fx-font-family`, `-fx-font-size`, `-fx-font-weight`,
`-fx-font-style`, `-fx-alignment`, `-fx-text-alignment`, `-fx-text-overrun`,
`-fx-wrap-text`, `-fx-graphic-text-gap`, `-fx-content-display`, `-fx-label-padding`,
`-fx-ellipsis-string`

**TextInputControl (TextField, TextArea)**:
`-fx-font`, `-fx-text-fill`, `-fx-prompt-text-fill`, `-fx-highlight-fill`,
`-fx-highlight-text-fill`, `-fx-display-caret`

**Layout-specific**:
- HBox/VBox: `-fx-spacing`, `-fx-alignment`, `-fx-fill-height` (HBox) / `-fx-fill-width` (VBox)
- GridPane: `-fx-hgap`, `-fx-vgap`, `-fx-alignment`, `-fx-grid-lines-visible`
- FlowPane: `-fx-hgap`, `-fx-vgap`, `-fx-alignment`, `-fx-orientation`,
  `-fx-column-halignment`, `-fx-row-valignment`
- TilePane: `-fx-hgap`, `-fx-vgap`, `-fx-alignment`, `-fx-orientation`,
  `-fx-pref-rows`, `-fx-pref-columns`, `-fx-pref-tile-width`, `-fx-pref-tile-height`,
  `-fx-tile-alignment`

### Pseudo-Classes for Interactive States

JavaFX CSS supports these pseudo-classes (vary by control):

- `:hover` — mouse is over the node
- `:focused` — node has keyboard focus
- `:pressed` — mouse button is down on the node
- `:disabled` — node is disabled
- `:selected` — for toggleable controls (CheckBox, RadioButton, ToggleButton)
- `:checked` — alias for `:selected` on some controls
- `:armed` — button is armed (about to fire)
- `:visited` — Hyperlink has been visited
- `:empty` — for cells with no content
- `:filled` — for cells with content
- `:showing` — for controls that show popups (ComboBox, MenuButton)
- `:top`, `:bottom`, `:left`, `:right` — for TabPane side
- `:horizontal`, `:vertical` — for orientation-aware controls

### Selectors

JavaFX CSS uses standard CSS selector syntax:

```css
/* Type selector — matches Java class simple name */
Button { }

/* Style class — assigned via styleClass */
.primary-button { }

/* ID selector — matches fx:id in FXML or setId() in code */
#submit-button { }

/* Descendant selector */
.sidebar .menu-item { }

/* Direct child selector */
.toolbar > .button { }

/* Pseudo-class */
.button:hover { }

/* Compound */
.button.primary:hover { }
```

### Loading Stylesheets

```java
// On Scene
scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

// On any Parent (scoped styles)
myPane.getStylesheets().add(getClass().getResource("/styles/sidebar.css").toExternalForm());
```

```xml
<!-- In FXML -->
<VBox xmlns:fx="http://javafx.com/fxml"
      stylesheets="@../styles/main.css">
```

---

## 8. Control Styling Patterns

### Buttons

```css
/* ── Base button reset ── */
.button {
    -fx-background-color: -fx-bg-surface;
    -fx-border-color: -fx-border-default;
    -fx-border-width: 1px;
    -fx-border-radius: 6px;
    -fx-background-radius: 6px;
    -fx-padding: 8px 16px;
    -fx-font-size: -fx-font-size-base;
    -fx-text-fill: -fx-text-primary;
    -fx-cursor: hand;
}
.button:hover {
    -fx-background-color: derive(-fx-bg-surface, -5%);
    -fx-border-color: derive(-fx-border-default, -10%);
}
.button:pressed {
    -fx-background-color: derive(-fx-bg-surface, -10%);
}
.button:focused {
    -fx-border-color: -fx-primary;
    -fx-border-width: 2px;
    -fx-background-insets: 0;
    -fx-border-insets: -1;
}
.button:disabled {
    -fx-opacity: 0.5;
}

/* ── Primary button variant ── */
.button.primary {
    -fx-background-color: -fx-primary;
    -fx-text-fill: -fx-text-on-primary;
    -fx-border-color: transparent;
}
.button.primary:hover {
    -fx-background-color: -fx-primary-hover;
}
.button.primary:pressed {
    -fx-background-color: -fx-primary-pressed;
}

/* ── Danger button variant ── */
.button.danger {
    -fx-background-color: -fx-danger;
    -fx-text-fill: white;
    -fx-border-color: transparent;
}

/* ── Ghost / text-only button ── */
.button.ghost {
    -fx-background-color: transparent;
    -fx-border-color: transparent;
    -fx-text-fill: -fx-primary;
}
.button.ghost:hover {
    -fx-background-color: -fx-primary-subtle;
}
```

### Text Fields

```css
.text-field {
    -fx-background-color: -fx-bg-base;
    -fx-border-color: -fx-border-default;
    -fx-border-width: 1px;
    -fx-border-radius: 6px;
    -fx-background-radius: 6px;
    -fx-padding: 8px 12px;
    -fx-font-size: -fx-font-size-base;
    -fx-text-fill: -fx-text-primary;
    -fx-prompt-text-fill: -fx-text-tertiary;
}
.text-field:focused {
    -fx-border-color: -fx-primary;
    -fx-border-width: 2px;
    -fx-border-insets: -1;
}
.text-field:disabled {
    -fx-background-color: -fx-bg-surface;
    -fx-opacity: 0.6;
}

/* ── Error state (add "error" style class programmatically) ── */
.text-field.error {
    -fx-border-color: -fx-danger;
}
.text-field.error:focused {
    -fx-border-color: -fx-danger;
}
```

### Tables

```css
.table-view {
    -fx-background-color: -fx-bg-base;
    -fx-border-color: -fx-border-subtle;
    -fx-border-width: 1px;
    -fx-border-radius: 8px;
    -fx-background-radius: 8px;
}

.table-view .column-header {
    -fx-background-color: -fx-bg-surface;
    -fx-border-color: transparent transparent -fx-border-default transparent;
    -fx-padding: 8px 12px;
    -fx-font-weight: 600;
    -fx-font-size: -fx-font-size-sm;
    -fx-text-fill: -fx-text-secondary;
}

.table-row-cell {
    -fx-background-color: -fx-bg-base;
    -fx-border-color: transparent transparent -fx-border-subtle transparent;
    -fx-padding: 6px 0;
}
.table-row-cell:hover {
    -fx-background-color: -fx-primary-subtle;
}
.table-row-cell:selected {
    -fx-background-color: derive(-fx-primary, 80%);
}

.table-cell {
    -fx-padding: 4px 12px;
    -fx-text-fill: -fx-text-primary;
    -fx-alignment: CENTER-LEFT;
}
```

### Scroll Panes / ScrollBars

```css
.scroll-pane {
    -fx-background-color: transparent;
    -fx-padding: 0;
}
.scroll-pane > .viewport {
    -fx-background-color: transparent;
}

/* Thin, subtle scrollbar */
.scroll-bar {
    -fx-background-color: transparent;
    -fx-padding: 2px;
}
.scroll-bar .thumb {
    -fx-background-color: derive(-fx-text-tertiary, 30%);
    -fx-background-radius: 100px;
    -fx-background-insets: 0;
}
.scroll-bar .thumb:hover {
    -fx-background-color: -fx-text-tertiary;
}
.scroll-bar .increment-button,
.scroll-bar .decrement-button {
    -fx-padding: 0;
    -fx-pref-height: 0;
    -fx-pref-width: 0;
}
.scroll-bar .increment-arrow,
.scroll-bar .decrement-arrow {
    -fx-shape: "";
    -fx-padding: 0;
}
```

### Cards / Panels

```css
.card {
    -fx-background-color: -fx-bg-elevated;
    -fx-background-radius: 12px;
    -fx-border-color: -fx-border-subtle;
    -fx-border-radius: 12px;
    -fx-border-width: 1px;
    -fx-padding: -fx-spacing-lg;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);
}

.card-flat {
    -fx-background-color: -fx-bg-surface;
    -fx-background-radius: 8px;
    -fx-padding: -fx-spacing-lg;
    -fx-effect: none;
}
```

---

## 9. Theming Architecture

### Light / Dark Theme Structure

Create separate CSS files or use the `.root` selector to switch themes.
The recommended approach is to define a `theme-light.css` and `theme-dark.css` that only
override the color tokens:

**theme-light.css**:
```css
.root {
    -fx-bg-base:        #FFFFFF;
    -fx-bg-surface:     #F8F9FA;
    -fx-bg-elevated:    #FFFFFF;
    -fx-border-default: #DEE2E6;
    -fx-border-subtle:  #E9ECEF;
    -fx-text-primary:   #212529;
    -fx-text-secondary: #6C757D;
    -fx-text-tertiary:  #ADB5BD;
    -fx-base:           #FFFFFF;
    -fx-background:     #F8F9FA;
}
```

**theme-dark.css**:
```css
.root {
    -fx-bg-base:        #1E1E1E;
    -fx-bg-surface:     #252526;
    -fx-bg-elevated:    #2D2D30;
    -fx-border-default: #3E3E42;
    -fx-border-subtle:  #333337;
    -fx-text-primary:   #E0E0E0;
    -fx-text-secondary: #A0A0A0;
    -fx-text-tertiary:  #6B6B6B;
    -fx-base:           #1E1E1E;
    -fx-background:     #252526;
}
```

**Switching at runtime**:
```java
public void setTheme(Scene scene, boolean dark) {
    scene.getStylesheets().removeIf(s -> s.contains("theme-"));
    String theme = dark ? "/styles/theme-dark.css" : "/styles/theme-light.css";
    scene.getStylesheets().add(getClass().getResource(theme).toExternalForm());
}
```

### Using `@media prefers-color-scheme` (JavaFX 26+)

JavaFX 26 introduces CSS `@media` queries including `prefers-color-scheme`:

```css
@media (prefers-color-scheme: dark) {
    .root {
        -fx-bg-base:      #1E1E1E;
        -fx-bg-surface:   #252526;
        -fx-text-primary: #E0E0E0;
        /* ... override all color tokens */
    }
}
```

This allows the application to automatically follow the operating system's theme preference
without any runtime Java code.

### Stylesheet Ordering

Load stylesheets in this order (later sheets override earlier):

1. **Base / reset styles** (`base.css`) — foundational tokens, resets, typography scale
2. **Component styles** (`components.css`) — button, field, table, card styles
3. **Layout styles** (`layout.css`) — page-specific layout rules
4. **Theme overrides** (`theme-dark.css` or `theme-light.css`) — color token overrides only
5. **View-specific styles** (optional, loaded per-view on `Parent.getStylesheets()`)

---

## 10. Accessibility & Usability

### Focus Indicators

Never remove focus indicators entirely. Instead, style them to match your design:

```css
.button:focused {
    -fx-border-color: -fx-primary;
    -fx-border-width: 2px;
}
```

The built-in `-fx-focus-color` and `-fx-faint-focus-color` looked-up colors control the default
focus ring. Override them on `.root` to change the global focus appearance:

```css
.root {
    -fx-focus-color: derive(-fx-primary, 20%);
    -fx-faint-focus-color: transparent;  /* removes the outer glow */
}
```

### Keyboard Navigation

- All interactive controls should be reachable via Tab key
- Use `focusTraversable="true"` on custom interactive nodes
- Group related controls so Tab order is logical (top-to-bottom, left-to-right)
- Provide mnemonics on labels and buttons: `_Save` creates Alt+S shortcut
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

### Minimum Target Sizes

Interactive elements should have a minimum clickable area of 32×32 pixels (ideally 44×44 for
touch-friendly interfaces). This is controlled through `-fx-padding` and `-fx-min-width` /
`-fx-min-height`:

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
- Ensure error states have both color change AND an icon/text indicator

---

## 11. Responsive & Adaptive Design

### Size Constraints

Set minimum window sizes to prevent layout breakage:

```java
stage.setMinWidth({{MIN_WIDTH}});
stage.setMinHeight({{MIN_HEIGHT}});
```

### Using Grow Priorities

```java
// Let the center content grow, keep sidebar fixed
HBox.setHgrow(sidebar, Priority.NEVER);
HBox.setHgrow(content, Priority.ALWAYS);
```

In CSS, use `-fx-max-width: Infinity` to allow a control to grow.

### Using `@media` Queries for Window Size (JavaFX 26+)

JavaFX 26 introduces CSS media queries for scene dimensions:

```css
/* Default: single-column layout */
.content-area {
    -fx-pref-columns: 1;
}

/* When window is wide enough, switch to two columns */
@media (min-width: 1200px) {
    .content-area {
        -fx-pref-columns: 2;
    }
}

/* Compact mode for narrow windows */
@media (max-width: 600px) {
    .sidebar {
        -fx-pref-width: 0;
        -fx-max-width: 0;
        -fx-min-width: 0;
    }
}
```

### Programmatic Responsive Layout

For JavaFX versions before 26 (without media queries), use listeners:

```java
scene.widthProperty().addListener((obs, oldW, newW) -> {
    if (newW.doubleValue() < 800) {
        sidebar.setVisible(false);
        sidebar.setManaged(false);
    } else {
        sidebar.setVisible(true);
        sidebar.setManaged(true);
    }
});
```

---

## 12. CSS Transitions & Micro-Interactions

### CSS Transitions (JavaFX 23+)

JavaFX 23 introduced implicit CSS transitions. These allow smooth property changes when CSS
pseudo-class states change:

```css
.button {
    -fx-opacity: 0.9;
    -fx-background-color: -fx-bg-surface;

    transition-property: -fx-opacity, -fx-background-color;
    transition-duration: 0.15s;
    transition-timing-function: ease;
}

.button:hover {
    -fx-opacity: 1.0;
    -fx-background-color: derive(-fx-bg-surface, -5%);
}
```

Shorthand syntax:

```css
.sidebar-item {
    -fx-background-color: transparent;
    -fx-text-fill: -fx-text-secondary;

    transition: -fx-background-color 0.2s ease, -fx-text-fill 0.2s ease;
}

.sidebar-item:hover {
    -fx-background-color: -fx-primary-subtle;
    -fx-text-fill: -fx-primary;
}
```

### Transition Timing Functions

JavaFX CSS supports these easing functions:

- `ease` — slow start, fast middle, slow end (default, good for most UI)
- `ease-in` — slow start (good for elements leaving the screen)
- `ease-out` — slow end (good for elements entering the screen)
- `ease-in-out` — slow start and end
- `linear` — constant speed (good for progress indicators)

### Design Guidance for Animations

- Keep transitions short (100–300ms) for UI state changes
- Use `ease-out` for hover/focus enters, `ease-in` for exits
- Don't animate too many properties simultaneously — it looks noisy
- Respect `prefers-reduced-motion` media query (JavaFX 26+):

```css
@media (prefers-reduced-motion: reduce) {
    * {
        transition-duration: 0s !important;
    }
}
```

---

## 13. Design Audit Checklist

When reviewing a JavaFX UI for design quality, check each item:

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
- [ ] Clickable items use `hand` cursor (`-fx-cursor: hand`)
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
colors/properties on `.root`. This makes themes trivial and ensures consistency.

**Layered backgrounds for depth** — Use multiple `-fx-background-color` values with
`-fx-background-insets` to create border and shadow effects without `-fx-effect`. This performs
better than drop shadows.

**Style class composition** — Give controls multiple style classes that combine:
```xml
<Button styleClass="button, primary, large" text="Submit" />
```
```css
.large { -fx-padding: 12px 24px; -fx-font-size: -fx-font-size-lg; }
```

**CSS-only states** — Use pseudo-classes and looked-up colors to express all states in CSS rather
than manipulating styles from Java code. Add/remove style classes for application states
(`.error`, `.success`, `.loading`).

**Consistent border radius** — Pick 2-3 border radius values and use them everywhere:
```css
.root {
    -fx-radius-sm:  4px;
    -fx-radius-md:  8px;
    -fx-radius-lg:  12px;
    -fx-radius-full: 100px;  /* for pills and circles */
}
```

### Anti-Patterns (Avoid This)

**Inline styles in Java** — Calling `node.setStyle("-fx-background-color: red")` creates
unmaintainable, unthemeable UI. Use style classes instead.

**Overriding `-fx-base` per control** — Changing `-fx-base` on individual controls cascades
unpredictably through the Modena theme's derived colors. Use specific properties instead.

**Using `-fx-effect: dropshadow(...)` everywhere** — Drop shadow effects are expensive to render.
Use background-color layering for card borders and reserve actual effects for elevated elements
like dialogs and popovers.

**Ignoring the Modena defaults** — Fighting the default stylesheet leads to inconsistencies.
Either commit to a full custom theme that resets all controls, or work within Modena's structure.

**AnchorPane for everything** — AnchorPane creates rigid, non-responsive layouts. Prefer
VBox/HBox with grow priorities, BorderPane for page structure, and GridPane for forms.

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
