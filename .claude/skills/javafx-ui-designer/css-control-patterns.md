# CSS Styling Reference & Control Patterns

Supporting file for the `javafx-ui-designer` skill.
Covers sections 7 (CSS reference) and 8 (control styling patterns).

---

## 7. JavaFX CSS Styling Reference

### CSS Property Naming Convention

JavaFX CSS properties use the `-fx-` vendor prefix. camelCase Java property names become
hyphenated lowercase CSS names:

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

| Pseudo-class | When it applies |
|---|---|
| `:hover` | Mouse is over the node |
| `:focused` | Node has keyboard focus |
| `:pressed` | Mouse button is down on the node |
| `:disabled` | Node is disabled |
| `:selected` | Toggleable controls (CheckBox, RadioButton, ToggleButton) |
| `:checked` | Alias for `:selected` on some controls |
| `:armed` | Button is armed (about to fire) |
| `:visited` | Hyperlink has been visited |
| `:empty` | Cell with no content |
| `:filled` | Cell with content |
| `:showing` | Controls that show popups (ComboBox, MenuButton) |
| `:top`, `:bottom`, `:left`, `:right` | TabPane side |
| `:horizontal`, `:vertical` | Orientation-aware controls |

### Selectors

JavaFX CSS uses standard CSS selector syntax:

```css
/* Type selector — matches Java class simple name */
Button { }

/* Style class — assigned via styleClass list or FXML styleClass attribute */
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
// On Scene — preferred; applies to all nodes in the scene
scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

// On any Parent — scoped styles for a subtree only
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
.button.danger:hover {
    -fx-background-color: derive(-fx-danger, -10%);
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

**Applying variants in FXML:**

```xml
<Button text="Save" styleClass="button, primary" />
<Button text="Delete" styleClass="button, danger" />
<Button text="Cancel" styleClass="button, ghost" />
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

/* ── Error state — add "error" style class programmatically ── */
.text-field.error {
    -fx-border-color: -fx-danger;
}
.text-field.error:focused {
    -fx-border-color: -fx-danger;
}
```

**Adding/removing error state in controller:**

```java
// Add error
textField.getStyleClass().add("error");

// Remove error
textField.getStyleClass().remove("error");
```

### Tables

> **Project note**: `TableStyles` enum holds row-level error/ready styles as inline strings.
> Migrate to CSS style classes (`.error`, `.ready`) added/removed on `TableRow` in
> `ApplicationMainViewController`'s row factory. This removes hardcoded strings from the enum.

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

/* Application-specific row state classes */
.table-row-cell.error {
    -fx-background-color: derive(-fx-danger, 85%);
}
.table-row-cell.ready {
    -fx-background-color: derive(-fx-success, 85%);
}

.table-cell {
    -fx-padding: 4px 12px;
    -fx-text-fill: -fx-text-primary;
    -fx-alignment: CENTER-LEFT;
}
```

**Migrated row factory (replaces `TableStyles` enum inline strings):**

```java
filesTableView.setRowFactory(tv -> new TableRow<RenamePreview>() {
    @Override
    protected void updateItem(RenamePreview preview, boolean empty) {
        super.updateItem(preview, empty);
        getStyleClass().removeAll("error", "ready");
        if (preview != null && !empty) {
            if (preview.hasError()) {
                getStyleClass().add("error");
            } else if (preview.newName() != null
                       && !preview.newName().equals(preview.originalName())) {
                getStyleClass().add("ready");
            }
        }
    }
});
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
