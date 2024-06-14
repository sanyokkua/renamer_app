package ua.renamer.app.ui.widget.table;

import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import ua.renamer.app.core.model.RenameModel;

public class TableCustomRow extends TableRow<RenameModel> {

    private final TableView<RenameModel> tableView;

    public TableCustomRow(TableView<RenameModel> tableView) {
        super();
        this.tableView = tableView;
    }

    @Override
    protected void updateItem(RenameModel item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setStyle("");
        } else {
            if (item.isNeedRename()) {
                setStyle("-fx-background-color: #6195ab;");
            } else if (item.isHasRenamingError()) {
                setStyle("-fx-background-color: #ef0b0b;");
            } else if (item.isRenamed()) {
                setStyle("-fx-background-color: #67ff67;");
            } else {
                setStyle("");
            }
        }
    }
}
