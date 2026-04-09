module ua.renamer.app.backend {
    requires ua.renamer.app.api;
    requires ua.renamer.app.core;
    requires ua.renamer.app.metadata;
    requires com.google.guice;
    requires jakarta.inject;
    requires jakarta.annotation;
    requires static lombok;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires com.fasterxml.jackson.databind;
    // IMPORTANT: NO require javafx.* — JPMS enforces FX-free backend

    exports ua.renamer.app.backend.service;
    exports ua.renamer.app.backend.settings;
    exports ua.renamer.app.backend.service.impl;
    exports ua.renamer.app.backend.session;
    exports ua.renamer.app.backend.config;

    opens ua.renamer.app.backend.service;
    opens ua.renamer.app.backend.service.impl;
    opens ua.renamer.app.backend.session;
    opens ua.renamer.app.backend.config;
    opens ua.renamer.app.backend.settings;
}
