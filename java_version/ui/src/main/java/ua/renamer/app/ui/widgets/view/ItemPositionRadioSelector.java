package ua.renamer.app.ui.widgets.view;

import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.ui.converters.ItemPositionConverter;

public class ItemPositionRadioSelector extends RadioSelector<ItemPosition> {

    public ItemPositionRadioSelector() {
        this("");
    }

    public ItemPositionRadioSelector(String labelText) {
        super(labelText, ItemPosition.class, new ItemPositionConverter());
    }

}
