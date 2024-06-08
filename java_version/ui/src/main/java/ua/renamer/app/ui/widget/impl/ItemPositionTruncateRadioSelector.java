package ua.renamer.app.ui.widget.impl;

import com.google.inject.Inject;
import ua.renamer.app.core.enums.TruncateOptions;
import ua.renamer.app.ui.converter.TruncateOptionsConverter;
import ua.renamer.app.ui.widget.RadioSelector;

public class ItemPositionTruncateRadioSelector extends RadioSelector<TruncateOptions> {

    @Inject
    public ItemPositionTruncateRadioSelector(TruncateOptionsConverter converter) {
        this("", converter);
    }

    public ItemPositionTruncateRadioSelector(String labelText, TruncateOptionsConverter converter) {
        super(labelText, TruncateOptions.class, converter);
    }

}
