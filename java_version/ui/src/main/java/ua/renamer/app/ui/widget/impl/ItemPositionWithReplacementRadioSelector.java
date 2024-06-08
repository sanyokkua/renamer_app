package ua.renamer.app.ui.widget.impl;

import com.google.inject.Inject;
import ua.renamer.app.core.enums.ItemPositionWithReplacement;
import ua.renamer.app.ui.converter.ItemPositionWithReplacementConverter;
import ua.renamer.app.ui.widget.RadioSelector;

public class ItemPositionWithReplacementRadioSelector extends RadioSelector<ItemPositionWithReplacement> {

    @Inject
    public ItemPositionWithReplacementRadioSelector(ItemPositionWithReplacementConverter converter) {
        this("", converter);
    }

    public ItemPositionWithReplacementRadioSelector(String labelText, ItemPositionWithReplacementConverter converter) {
        super(labelText, ItemPositionWithReplacement.class, converter);
    }

}
