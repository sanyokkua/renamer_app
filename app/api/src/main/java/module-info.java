module ua.renamer.app.api {
    requires static lombok;
    requires org.jspecify;
    requires jakarta.annotation;

    exports ua.renamer.app.api.enums;
    exports ua.renamer.app.api.exception;
    exports ua.renamer.app.api.interfaces;
    exports ua.renamer.app.api.model;
    exports ua.renamer.app.api.model.config;
    exports ua.renamer.app.api.model.meta;
    exports ua.renamer.app.api.model.meta.category;
    exports ua.renamer.app.api.service;

    opens ua.renamer.app.api.enums;
    opens ua.renamer.app.api.model;
    opens ua.renamer.app.api.model.config;
    opens ua.renamer.app.api.model.meta;
    opens ua.renamer.app.api.model.meta.category;
}
