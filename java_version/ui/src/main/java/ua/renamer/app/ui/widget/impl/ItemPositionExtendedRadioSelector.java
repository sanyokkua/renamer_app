package ua.renamer.app.ui.widget.impl;

import com.google.inject.Inject;
import ua.renamer.app.core.enums.ItemPositionExtended;
import ua.renamer.app.ui.converter.ItemPositionExtendedConverter;
import ua.renamer.app.ui.widget.RadioSelector;

public class ItemPositionExtendedRadioSelector extends RadioSelector<ItemPositionExtended> {

    @Inject
    public ItemPositionExtendedRadioSelector(ItemPositionExtendedConverter converter) {
        this(converter, "");
    }

    public ItemPositionExtendedRadioSelector(ItemPositionExtendedConverter converter, String labelText) {
        super(labelText, ItemPositionExtended.class, converter);
    }

}
