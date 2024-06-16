package ua.renamer.app.ui.enums;

import lombok.Getter;

@Getter
public enum TableStyles {

    BLANK("-fx-control-inner-background: #ffffff;"),
    SELECTED("-fx-control-inner-background: #bc40ff;"),
    READY_FOR_RENAMING("-fx-control-inner-background: #5dcff5;"),
    HAS_ERROR("-fx-control-inner-background: #fa7c70;"),
    RENAMED("-fx-control-inner-background: #acfaa7;");

    private static final String STANDARD_STYLE = " -fx-accent: derive(-fx-control-inner-background, -40%); -fx-cell-hover-color: derive(-fx-control-inner-background, -20%);";
    private final String style;

    TableStyles(String style) {
        this.style = style + STANDARD_STYLE;
    }
}
