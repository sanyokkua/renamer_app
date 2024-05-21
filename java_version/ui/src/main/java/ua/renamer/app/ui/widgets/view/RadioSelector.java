package ua.renamer.app.ui.widgets.view;

import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Getter
public abstract class RadioSelector<T extends Enum<T>> extends HBox {

    private final Class<T> enumClass;
    private final StringConverter<T> convertor;
    private final Label labelWidget;
    private final List<ValueRadioBtn<T>> buttons;
    private final ToggleGroup toggleGroup;
    private String labelValue;

    protected RadioSelector(String labelValue, Class<T> enumClass, StringConverter<T> convertor) {
        super();
        log.debug("Created new RadioSelector, with labelValue: {}, enumClass: {}, convertor: {}",
                  labelValue,
                  enumClass.getName(),
                  convertor.getClass().getName()
                 );
        this.labelValue = labelValue;
        this.enumClass = enumClass;
        this.convertor = convertor;

        this.labelWidget = new Label();
        this.buttons = new ArrayList<>();
        this.toggleGroup = new ToggleGroup();

        this.initWidgets();
    }

    private void initWidgets() {
        for (T enumConstant : enumClass.getEnumConstants()) {
            var btnLabel = convertor.toString(enumConstant);
            var btn = new ValueRadioBtn<T>(enumConstant);

            btn.setText(btnLabel);
            btn.setToggleGroup(toggleGroup);

            buttons.add(btn);
        }

        buttons.stream().findFirst().ifPresent(toggleGroup::selectToggle);

        getChildren().add(labelWidget);
        getChildren().addAll(buttons);

        toggleGroup.selectedToggleProperty().addListener(this::toggleChangeListener);
    }

    private void toggleChangeListener(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
        if (Objects.nonNull(newValue)) {
            ValueRadioBtn<T> selectedRadio = (ValueRadioBtn<T>) newValue;
            T selectedValue = selectedRadio.getValue();
            fireEvent(new RadioSelectorEvent<>(selectedValue));
        }
    }

    public void setLabelValue(String labelValue) {
        log.debug("Set labelValue: {}", labelValue);
        this.labelValue = labelValue;
        labelWidget.setText(labelValue);
    }

    public T getSelectedValue() {
        Optional<ValueRadioBtn<T>> foundBtn = buttons.stream().filter(ToggleButton::isSelected).findFirst();
        if (foundBtn.isPresent()) {
            return foundBtn.get().getValue();
        }
        throw new IllegalStateException("Widget doesn't have any selected buttons");
    }

    public void addValueSelectedHandler(Consumer<T> callback) {
        this.addEventHandler(RadioSelector.RadioSelectorEvent.RADIO_BUTTON_SELECTED_EVENT_EVENT_TYPE, event -> {
            T selectedValue = (T) event.getSelectedValue();
            callback.accept(selectedValue);
        });
    }

    public static class ValueRadioBtn<T> extends RadioButton {

        private final T value;

        public ValueRadioBtn(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

    }

    public static class RadioSelectorEvent<T> extends Event {

        public static final EventType<RadioSelectorEvent<?>> RADIO_BUTTON_SELECTED_EVENT_EVENT_TYPE = new EventType<>(
                Event.ANY,
                "RADIO BUTTON VALUE CHANGED"
        );

        private final transient T selectedValue;

        public RadioSelectorEvent(T selectedValue) {
            super(RADIO_BUTTON_SELECTED_EVENT_EVENT_TYPE);
            this.selectedValue = selectedValue;
        }

        public T getSelectedValue() {
            return selectedValue;
        }

    }

}
