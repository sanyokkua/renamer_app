module ua.renamer.app.ui {
    requires static lombok;
    requires ch.qos.logback.core;
    requires org.slf4j;

    requires com.google.common;
    requires com.google.guice;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires ua.renamer.app.core;

    exports ua.renamer.app.ui.config;
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
    exports ua.renamer.app;

    opens fxml;
    opens langs;
    opens ua.renamer.app.ui.controller to javafx.fxml;
    opens ua.renamer.app.ui.controller.mode to javafx.fxml;
    opens ua.renamer.app.ui.controller.mode.impl to javafx.fxml;
    opens ua.renamer.app.ui.converter to javafx.fxml;
    opens ua.renamer.app.ui.service to javafx.fxml;
    opens ua.renamer.app.ui.widget to javafx.fxml;
    opens ua.renamer.app.ui.widget.impl to javafx.fxml;
}