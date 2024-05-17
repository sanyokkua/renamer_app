package ua.renamer.app.ui.widgets.view;

import ua.renamer.app.core.enums.ItemPositionWithReplacement;
import ua.renamer.app.ui.converters.ItemPositionWithReplacementConverter;

public class ItemPositionWithReplacementRadioSelector extends RadioSelector<ItemPositionWithReplacement> {

    public ItemPositionWithReplacementRadioSelector() {
        this("");
    }

    public ItemPositionWithReplacementRadioSelector(String labelText) {
        super(labelText, ItemPositionWithReplacement.class, new ItemPositionWithReplacementConverter());
    }
}
