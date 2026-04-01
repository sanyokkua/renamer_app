module ua.renamer.app.backend {
    requires ua.renamer.app.api;
    requires ua.renamer.app.core;
    requires com.google.guice;
    requires jakarta.inject;
    requires jakarta.annotation;
    requires static lombok;
    requires org.slf4j;
    // IMPORTANT: NO require javafx.* — JPMS enforces FX-free backend

    // exports below are commented out until real types exist in each package (javac 25 rejects exports of package-info-only packages)
    // Uncomment each export in TASK-2.2/2.3/2.4 as classes are added:
    //   exports ua.renamer.app.backend.config;     // DIBackendModule
    //   exports ua.renamer.app.backend.service;    // RenameSessionService, BackendExecutor
    //   exports ua.renamer.app.backend.session;    // RenameSession, SessionApiImpl
    // Internal packages are NOT exported
}
