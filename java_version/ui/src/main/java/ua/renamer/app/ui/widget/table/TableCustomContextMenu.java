package ua.renamer.app.ui.widget.table;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.TableView;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.RenameModel;

@Slf4j
public class TableCustomContextMenu extends ContextMenu {

    public TableCustomContextMenu(TableView<RenameModel> filesTableView) {
        for (var column : filesTableView.getColumns()) {
            CheckBox checkBox = new CheckBox(column.getText());
            checkBox.setSelected(true);
            checkBox.setOnAction(event -> {
                if (checkBox.isSelected()) {
                    if (!filesTableView.getColumns().contains(column)) {
                        filesTableView.getColumns().add(column);
                    }
                } else {
                    filesTableView.getColumns().remove(column);
                }
            });
            CustomMenuItem customMenuItem = new CustomMenuItem(checkBox);
            customMenuItem.setHideOnClick(false);
            this.getItems().add(customMenuItem);
        }
    }
}
