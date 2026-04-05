package ua.renamer.app.ui.widget.factory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.util.Builder;
import javafx.util.BuilderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.ui.widget.builder.ItemPositionExtendedRadioSelectorBuilder;
import ua.renamer.app.ui.widget.builder.ItemPositionRadioSelectorBuilder;
import ua.renamer.app.ui.widget.builder.ItemPositionTruncateRadioSelectorBuilder;
import ua.renamer.app.ui.widget.builder.ItemPositionWithReplacementRadioSelectorBuilder;
import ua.renamer.app.ui.widget.impl.ItemPositionExtendedRadioSelector;
import ua.renamer.app.ui.widget.impl.ItemPositionRadioSelector;
import ua.renamer.app.ui.widget.impl.ItemPositionTruncateRadioSelector;
import ua.renamer.app.ui.widget.impl.ItemPositionWithReplacementRadioSelector;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class RadioSelectorFactory implements BuilderFactory {

    private final JavaFXBuilderFactory defaultBuilderFactory;
    private final Injector injector;

    @Override
    public Builder<?> getBuilder(Class<?> type) {
        if (type.equals(ItemPositionExtendedRadioSelector.class)) {
            return injector.getInstance(ItemPositionExtendedRadioSelectorBuilder.class);
        } else if (type.equals(ItemPositionRadioSelector.class)) {
            return injector.getInstance(ItemPositionRadioSelectorBuilder.class);
        } else if (type.equals(ItemPositionTruncateRadioSelector.class)) {
            return injector.getInstance(ItemPositionTruncateRadioSelectorBuilder.class);
        } else if (type.equals(ItemPositionWithReplacementRadioSelector.class)) {
            return injector.getInstance(ItemPositionWithReplacementRadioSelectorBuilder.class);
        } else {
            return defaultBuilderFactory.getBuilder(type);
        }
    }

}
