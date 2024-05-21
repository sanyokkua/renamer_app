package ua.renamer.app.ui.widgets.view;

import ua.renamer.app.core.enums.TruncateOptions;
import ua.renamer.app.ui.converters.TruncateOptionsConverter;

public class ItemPositionTruncateRadioSelector extends RadioSelector<TruncateOptions> {

    public ItemPositionTruncateRadioSelector() {
        this("");
    }

    public ItemPositionTruncateRadioSelector(String labelText) {
        super(labelText, TruncateOptions.class, new TruncateOptionsConverter());
    }

}
