package ua.renamer.app.ui.widget.builder;

import com.google.inject.Inject;
import javafx.util.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.ui.widget.impl.ItemPositionWithReplacementRadioSelector;

@Data
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ItemPositionWithReplacementRadioSelectorBuilder implements Builder<ItemPositionWithReplacementRadioSelector> {

    private final ItemPositionWithReplacementRadioSelector itemPositionWithReplacementRadioSelector;
    private String labelValue;
    private String id;

    @Override
    public ItemPositionWithReplacementRadioSelector build() {
        itemPositionWithReplacementRadioSelector.setLabelValue(labelValue);
        itemPositionWithReplacementRadioSelector.setId(id);
        return itemPositionWithReplacementRadioSelector;
    }

}
