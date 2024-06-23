package ua.renamer.app.ui.widget.impl;

import com.google.inject.Inject;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.ui.converter.ItemPositionConverter;
import ua.renamer.app.ui.widget.RadioSelector;

public class ItemPositionRadioSelector extends RadioSelector<ItemPosition> {

    @Inject
    public ItemPositionRadioSelector(ItemPositionConverter converter) {
        this("", converter);
    }

    public ItemPositionRadioSelector(String labelText, ItemPositionConverter converter) {
        super(labelText, ItemPosition.class, converter);
    }

}
