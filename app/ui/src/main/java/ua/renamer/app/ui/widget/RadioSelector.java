package ua.renamer.app.ui.widget;

import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * An abstract class representing a custom JavaFX widget for selecting values from an enum using radio buttons.
 *
 * @param <T> The type of enum values to be selected.
 */
@Slf4j
@Getter
public abstract class RadioSelector<T extends Enum<T>> extends VBox {

    private final Class<T> enumClass;
    private final StringConverter<T> converter;
    private final Label labelWidget;
    private final List<ValueRadioBtn<T>> buttons;
    private final ToggleGroup toggleGroup;
    private String labelValue;
    private EventHandler<RadioSelectorEvent<?>> valueSelectedHandler;

    /**
     * Constructs a RadioSelector object with the specified label value, enum class, and converter.
     *
     * @param labelValue The label value displayed alongside the radio buttons.
     * @param enumClass  The class of the enum type from which values will be selected.
     * @param converter  The converter for converting enum values to string representations.
     */
    protected RadioSelector(String labelValue, Class<T> enumClass, StringConverter<T> converter) {
        super();
        log.debug("Created new RadioSelector, with labelValue: {}, enumClass: {}, converter: {}",
                labelValue,
                enumClass.getName(),
                converter.getClass().getName());
        this.labelValue = labelValue;
        this.enumClass = enumClass;
        this.converter = converter;

        this.labelWidget = new Label();
        this.buttons = new ArrayList<>();
        this.toggleGroup = new ToggleGroup();

        this.initWidgets();
    }

    private void initWidgets() {
        for (T enumConstant : enumClass.getEnumConstants()) {
            var btnLabel = converter.toString(enumConstant);
            var btn = new ValueRadioBtn<>(enumConstant);

            btn.setText(btnLabel);
            btn.setToggleGroup(toggleGroup);
            btn.setTooltip(new Tooltip(btnLabel));

            HBox.setMargin(btn, new Insets(2, 0, 2, 0));
            buttons.add(btn);
        }

        buttons.stream().findFirst().ifPresent(toggleGroup::selectToggle);

        labelWidget.setStyle("-fx-text-fill: #35506A; -fx-font-size: 11px; -fx-font-weight: bold;");
        getChildren().add(labelWidget);
        HBox.setMargin(labelWidget, new Insets(0, 5, 0, 0));
        getChildren().addAll(buttons);

        toggleGroup.selectedToggleProperty().addListener(this::toggleChangeListener);
    }

    // observable and oldValue are required by the ChangeListener functional interface but not used here
    @SuppressWarnings({"unchecked", "PMD.UnusedFormalParameter"})
    private void toggleChangeListener(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
        if (Objects.nonNull(newValue)) {
            ValueRadioBtn<T> selectedRadio = (ValueRadioBtn<T>) newValue;
            T selectedValue = selectedRadio.getValue();
            fireEvent(new RadioSelectorEvent<>(selectedValue));
        }
    }

    /**
     * Sets the label value displayed alongside the radio buttons.
     *
     * @param labelValue The new label value.
     */
    public void setLabelValue(String labelValue) {
        log.debug("Set labelValue: {}", labelValue);
        this.labelValue = labelValue;
        labelWidget.setText(labelValue);
        labelWidget.setTooltip(new Tooltip(labelValue));
        labelWidget.setStyle("-fx-text-fill: #35506A; -fx-font-size: 11px; -fx-font-weight: bold;");
    }

    /**
     * Gets the currently selected enum value.
     *
     * @return The currently selected enum value.
     * @throws IllegalStateException If no radio button is selected.
     */
    public T getSelectedValue() {
        Optional<ValueRadioBtn<T>> foundBtn = buttons.stream().filter(ToggleButton::isSelected).findFirst();
        if (foundBtn.isPresent()) {
            return foundBtn.get().getValue();
        }
        throw new IllegalStateException("Widget doesn't have any selected buttons");
    }

    /**
     * Adds a callback function to handle selection change events.
     *
     * @param callback The callback function to be added.
     */
    @SuppressWarnings("unchecked")
    public void addValueSelectedHandler(Consumer<T> callback) {
        this.addEventHandler(RadioSelector.RadioSelectorEvent.RADIO_BUTTON_SELECTED_EVENT_EVENT_TYPE, event -> {
            T selectedValue = (T) event.getSelectedValue();
            callback.accept(selectedValue);
        });
    }

    /**
     * Sets (replaces) the callback for selection-change events.
     * Any previously registered callback is removed before the new one is registered,
     * so repeated calls never accumulate handlers.
     *
     * @param callback the new callback; must not be null
     */
    @SuppressWarnings("unchecked")
    public void setValueSelectedHandler(Consumer<T> callback) {
        if (valueSelectedHandler != null) {
            this.removeEventHandler(RadioSelectorEvent.RADIO_BUTTON_SELECTED_EVENT_EVENT_TYPE, valueSelectedHandler);
        }
        valueSelectedHandler = event -> callback.accept((T) event.getSelectedValue());
        this.addEventHandler(RadioSelectorEvent.RADIO_BUTTON_SELECTED_EVENT_EVENT_TYPE, valueSelectedHandler);
    }

    /**
     * Represents a radio button associated with a specific enum value.
     *
     * @param <T> The type of enum value associated with the radio button.
     */
    @Getter
    public static class ValueRadioBtn<T> extends RadioButton {

        private final T value;

        /**
         * Constructs a ValueRadioBtn object with the specified enum value.
         *
         * @param value The enum value associated with the radio button.
         */
        public ValueRadioBtn(T value) {
            this.value = value;
        }

    }

    /**
     * Represents an event triggered when a radio button selection changes.
     *
     * @param <T> The type of enum value associated with the event.
     */
    @Getter
    public static class RadioSelectorEvent<T> extends Event {

        /**
         * The event type for radio button selection change events.
         */
        public static final EventType<RadioSelectorEvent<?>> RADIO_BUTTON_SELECTED_EVENT_EVENT_TYPE = new EventType<>(
                Event.ANY,
                "RADIO BUTTON VALUE CHANGED");
        @Serial
        private static final long serialVersionUID = 1L;
        private final transient T selectedValue;

        /**
         * Constructs a RadioSelectorEvent object with the specified selected enum value.
         *
         * @param selectedValue The selected enum value associated with the event.
         */
        public RadioSelectorEvent(T selectedValue) {
            super(RADIO_BUTTON_SELECTED_EVENT_EVENT_TYPE);
            this.selectedValue = selectedValue;
        }

    }

}