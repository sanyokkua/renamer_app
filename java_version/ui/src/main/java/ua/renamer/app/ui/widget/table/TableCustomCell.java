package ua.renamer.app.ui.widget.table;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import ua.renamer.app.core.model.RenameModel;

public class TableCustomCell extends TableCell<RenameModel, String> {

    private final TableColumn<RenameModel, String> tableColumn;

    public TableCustomCell(TableColumn<RenameModel, String> tableColumn) {
        super();
        this.tableColumn = tableColumn;
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setTooltip(null);
        } else {
            setText(item);
            Tooltip tooltip = new Tooltip(item);
            setTooltip(tooltip);
            RenameModel model = getTableView().getItems().get(getIndex());
            if (model.isNeedRename()) {
                setTextFill(Color.valueOf("#ffffff"));
            } else if (model.isHasRenamingError()) {
                setTextFill(Color.valueOf("#ffffff"));
            } else if (model.isRenamed()) {
                setTextFill(Color.valueOf("#000000"));
            } else {
                setTextFill(Color.BLACK);
            }
        }
    }
}
