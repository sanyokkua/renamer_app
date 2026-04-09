package ua.renamer.app.ui.enums;

import lombok.Getter;

/**
 * Represent CSS style classes applied to table rows based on their rename state.
 *
 * <p>Each constant maps to a CSS class defined in {@code table.css}. These classes are
 * added to and removed from {@code TableRow} style class lists by the row factory in
 * {@code ApplicationMainViewController}, replacing the former inline style strings.
 */
@Getter
public enum TableStyles {

    /**
     * Row has no special state — no style class applied.
     */
    BLANK(""),
    /**
     * Row is selected by the user.
     */
    SELECTED("row-selected"),
    /**
     * Row represents a file ready to be renamed (new name differs from original).
     */
    READY_FOR_RENAMING("row-ready"),
    /**
     * Row represents a file whose transformation produced an error.
     */
    HAS_ERROR("row-error"),
    /**
     * Row represents a file that has already been renamed successfully.
     */
    RENAMED("row-renamed");

    private final String styleClass;

    TableStyles(String styleClass) {
        this.styleClass = styleClass;
    }
}
