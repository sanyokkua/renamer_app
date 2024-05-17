package ua.renamer.app.ui.widgets.view;

import ua.renamer.app.core.enums.ItemPositionExtended;
import ua.renamer.app.ui.converters.ItemPositionExtendedConverter;

public class ItemPositionExtendedRadioSelector extends RadioSelector<ItemPositionExtended> {

    public ItemPositionExtendedRadioSelector() {
        this("");
    }

    public ItemPositionExtendedRadioSelector(String labelText) {
        super(labelText, ItemPositionExtended.class, new ItemPositionExtendedConverter());
    }
}
