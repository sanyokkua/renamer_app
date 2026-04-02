module ua.renamer.app.backend {
    requires ua.renamer.app.api;
    requires ua.renamer.app.core;
    requires com.google.guice;
    requires jakarta.inject;
    requires jakarta.annotation;
    requires static lombok;
    requires org.slf4j;
    // IMPORTANT: NO require javafx.* — JPMS enforces FX-free backend

    exports ua.renamer.app.backend.service;
    exports ua.renamer.app.backend.session;
    exports ua.renamer.app.backend.config;

    opens ua.renamer.app.backend.service;
    opens ua.renamer.app.backend.session;
    opens ua.renamer.app.backend.config;
}
