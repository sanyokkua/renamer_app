module ua.renamer.app.ui {
    requires static lombok;

    requires ua.renamer.app.core;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.slf4j;
    requires ch.qos.logback.core;

    exports ua.renamer.app;
    exports ua.renamer.app.ui;
    exports ua.renamer.app.ui.constants;
    exports ua.renamer.app.ui.widgets.controllers;
    exports ua.renamer.app.ui.widgets.controllers.modes;
    exports ua.renamer.app.ui.abstracts;

    opens ua.renamer.app.ui.widgets.view to javafx.fxml;
    opens ua.renamer.app.ui.widgets.controllers to javafx.fxml;
    opens ua.renamer.app.ui.widgets.controllers.modes to javafx.fxml;
    opens ua.renamer.app.ui.converters to javafx.fxml;
    opens ua.renamer.app.ui.abstracts to javafx.fxml;

    opens fxml;
}