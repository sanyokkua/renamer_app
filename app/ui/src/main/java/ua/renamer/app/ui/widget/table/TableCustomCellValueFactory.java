package ua.renamer.app.ui.widget.table;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.RenameModel;

import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class TableCustomCellValueFactory implements Callback<TableColumn.CellDataFeatures<RenameModel, String>, ObservableValue<String>> {

    @NonNull
    private final Function<RenameModel, String> converter;

    @Override
    public ObservableValue<String> call(TableColumn.CellDataFeatures<RenameModel, String> param) {
        RenameModel model = param.getValue();
        String apply = converter.apply(model);
        return new SimpleStringProperty(apply);
    }
}
