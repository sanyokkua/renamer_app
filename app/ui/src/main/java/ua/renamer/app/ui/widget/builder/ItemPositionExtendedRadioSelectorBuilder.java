package ua.renamer.app.ui.widget.builder;

import com.google.inject.Inject;
import javafx.util.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.ui.widget.impl.ItemPositionExtendedRadioSelector;

@Data
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ItemPositionExtendedRadioSelectorBuilder implements Builder<ItemPositionExtendedRadioSelector> {

    private final ItemPositionExtendedRadioSelector itemPositionExtendedRadioSelector;
    private String labelValue;
    private String id;

    @Override
    public ItemPositionExtendedRadioSelector build() {
        itemPositionExtendedRadioSelector.setLabelValue(labelValue);
        itemPositionExtendedRadioSelector.setId(id);
        return itemPositionExtendedRadioSelector;
    }

}
