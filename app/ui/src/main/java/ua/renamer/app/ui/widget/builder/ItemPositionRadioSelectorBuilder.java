package ua.renamer.app.ui.widget.builder;

import com.google.inject.Inject;
import javafx.util.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.ui.widget.impl.ItemPositionRadioSelector;

@Data
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ItemPositionRadioSelectorBuilder implements Builder<ItemPositionRadioSelector> {

    private final ItemPositionRadioSelector itemPositionRadioSelector;
    private String labelValue;
    private String id;

    @Override
    public ItemPositionRadioSelector build() {
        itemPositionRadioSelector.setLabelValue(labelValue);
        itemPositionRadioSelector.setId(id);
        return itemPositionRadioSelector;
    }

}
