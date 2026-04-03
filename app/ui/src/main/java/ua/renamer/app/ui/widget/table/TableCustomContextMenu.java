package ua.renamer.app.ui.widget.table;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.TableView;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TableCustomContextMenu extends ContextMenu {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public TableCustomContextMenu(TableView<?> filesTableView) {
        var rawTable = (TableView) filesTableView;
        for (var column : rawTable.getColumns()) {
            CheckBox checkBox = new CheckBox(((javafx.scene.control.TableColumn<?, ?>) column).getText());
            checkBox.setSelected(true);
            checkBox.setOnAction(event -> {
                if (checkBox.isSelected()) {
                    if (!rawTable.getColumns().contains(column)) {
                        rawTable.getColumns().add(column);
                    }
                } else {
                    rawTable.getColumns().remove(column);
                }
            });
            CustomMenuItem customMenuItem = new CustomMenuItem(checkBox);
            customMenuItem.setHideOnClick(false);
            this.getItems().add(customMenuItem);
        }
    }
}
