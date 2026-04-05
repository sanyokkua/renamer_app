# Theming, Responsive Design & CSS Transitions

Supporting file for the `javafx-ui-designer` skill.
Covers sections 9 (theming architecture), 11 (responsive design), and 12 (CSS transitions).

---

## 9. Theming Architecture

### Light / Dark Theme Structure

Create separate CSS files for each theme that only override the color tokens defined on `.root`.
Component styles reference the tokens and automatically adapt when the theme file is swapped.

**`theme-light.css`**:
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

**`theme-dark.css`**:
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

### Runtime Theme Switching

```java
public void setTheme(Scene scene, boolean dark) {
    scene.getStylesheets().removeIf(s -> s.contains("theme-"));
    String theme = dark ? "/styles/theme-dark.css" : "/styles/theme-light.css";
    scene.getStylesheets().add(getClass().getResource(theme).toExternalForm());
}
```

### Automatic OS Theme Following (JavaFX 26+)

> **Note**: This project runs on **JavaFX 25**. The `@media prefers-color-scheme` query is a
> **JavaFX 26+** feature. Document here for future upgrade readiness; use the runtime switching
> method above for JavaFX 25.

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

### Stylesheet Load Order

Load stylesheets in this sequence (later sheets override earlier ones):

1. **`base.css`** — foundational tokens, resets, typography scale, spacing scale
2. **`components.css`** — button, field, table, card, scrollbar styles
3. **`layout.css`** — page-specific layout rules
4. **`theme-light.css`** or **`theme-dark.css`** — color token overrides only
5. **View-specific styles** (optional, loaded per-view via `Parent.getStylesheets()`)

```java
// In RenamerApplication.start(), before stage.show():
var stylesheets = scene.getStylesheets();
stylesheets.add(getClass().getResource("/styles/base.css").toExternalForm());
stylesheets.add(getClass().getResource("/styles/components.css").toExternalForm());
stylesheets.add(getClass().getResource("/styles/layout.css").toExternalForm());
stylesheets.add(getClass().getResource("/styles/theme-light.css").toExternalForm());
```

---

## 11. Responsive & Adaptive Design

### Minimum Window Size

Set minimum dimensions to prevent layout breakage:

```java
// In RenamerApplication.start():
stage.setMinWidth(900);
stage.setMinHeight(600);
```

### Grow Priority Patterns

```java
// Let the center content grow, keep sidebar fixed
HBox.setHgrow(sidebar, Priority.NEVER);
HBox.setHgrow(content, Priority.ALWAYS);
```

In CSS, allow a control to grow to fill available space:

```css
.stretch-control {
    -fx-max-width: Infinity;
}
```

### `@media` Queries for Window Size (JavaFX 26+)

> **Note**: This project runs on **JavaFX 25**. CSS `@media (min-width/max-width)` queries are
> a **JavaFX 26+** feature. Use the programmatic listener approach below for JavaFX 25.

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

### Programmatic Responsive Layout (JavaFX 25 — current)

Use property listeners to adapt layout based on scene dimensions:

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

> Always set both `setVisible(false)` **and** `setManaged(false)` together. `setVisible(false)`
> alone hides the node but still reserves its space in the layout.

---

## 12. CSS Transitions & Micro-Interactions

### CSS Transitions (JavaFX 23+ — available in this project)

JavaFX 23 introduced implicit CSS transitions for smooth property changes when pseudo-class
states change. These work in this project (JavaFX 25).

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

**Shorthand syntax:**

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

| Function | Behavior | Best for |
|---|---|---|
| `ease` | Slow start, fast middle, slow end | Most UI state changes (default) |
| `ease-in` | Slow start, fast end | Elements leaving the screen |
| `ease-out` | Fast start, slow end | Elements entering the screen |
| `ease-in-out` | Slow start and end | Deliberate, noticeable movements |
| `linear` | Constant speed | Progress indicators, loading animations |

### Design Guidance for Animations

- Keep transitions short: **100–300ms** for UI state changes
- Use `ease-out` for hover/focus **enters**, `ease-in` for **exits**
- Don't animate too many properties simultaneously — it looks noisy and expensive
- Prefer transitioning `opacity` and `background-color` over layout properties

### Respecting Reduced Motion (JavaFX 26+)

> **Note**: This project runs on **JavaFX 25**. The `prefers-reduced-motion` media query is a
> **JavaFX 26+** feature. The CSS below is forward-compatible and will take effect on upgrade.

```css
@media (prefers-reduced-motion: reduce) {
    * {
        transition-duration: 0s !important;
    }
}
```

For JavaFX 25, check the system preference programmatically if needed:

```java
// Check OS accessibility setting (platform-specific, no direct JavaFX API)
// Consider providing a user-facing "Reduce motion" preference toggle in app settings
```
