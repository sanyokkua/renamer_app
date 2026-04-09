module ua.renamer.app.ui {
    requires static lombok;
    requires org.slf4j;
    requires jakarta.annotation;

    requires com.google.guice;

    requires java.desktop;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires ua.renamer.app.core;
    requires ua.renamer.app.backend;
    requires ua.renamer.app.api;
    requires jakarta.inject;

    exports ua.renamer.app.ui.config;
    exports ua.renamer.app.ui.state;
    exports ua.renamer.app.ui.view;
    exports ua.renamer.app.ui.controller.mode.impl;
    exports ua.renamer.app.ui.controller.mode;
    exports ua.renamer.app.ui.controller;
    exports ua.renamer.app.ui.converter;
    exports ua.renamer.app.ui.enums;
    exports ua.renamer.app.ui.service.impl;
    exports ua.renamer.app.ui.service;
    exports ua.renamer.app.ui.widget.builder;
    exports ua.renamer.app.ui.widget.factory;
    exports ua.renamer.app.ui.widget.impl;
    exports ua.renamer.app.ui.widget;
    exports ua.renamer.app.ui.widget.table;
    exports ua.renamer.app;

    opens fxml; // Required by JavaFX
    opens langs; // Required by JavaFX
    opens images; // Required by JavaFX
    opens ua.renamer.app.ui.controller to javafx.fxml;
    opens ua.renamer.app.ui.controller.mode to javafx.fxml;
    opens ua.renamer.app.ui.controller.mode.impl; // open to javafx.fxml for FXML injection and to tests for reflection
    opens ua.renamer.app.ui.converter to javafx.fxml;
    opens ua.renamer.app.ui.service to javafx.fxml;
    opens ua.renamer.app.ui.widget to javafx.fxml;
    opens ua.renamer.app.ui.widget.impl to javafx.fxml;
    opens ua.renamer.app.ui.widget.table to javafx.fxml;
    opens ua.renamer.app.ui.state;
}