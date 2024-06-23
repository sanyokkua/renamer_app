package ua.renamer.app.ui.widget.builder;

import com.google.inject.Inject;
import javafx.util.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.ui.widget.impl.ItemPositionTruncateRadioSelector;

@Data
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ItemPositionTruncateRadioSelectorBuilder implements Builder<ItemPositionTruncateRadioSelector> {

    private final ItemPositionTruncateRadioSelector itemPositionTruncateRadioSelector;
    private String labelValue;
    private String id;

    @Override
    public ItemPositionTruncateRadioSelector build() {
        itemPositionTruncateRadioSelector.setLabelValue(labelValue);
        itemPositionTruncateRadioSelector.setId(id);
        return itemPositionTruncateRadioSelector;
    }

}
