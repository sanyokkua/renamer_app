module ua.renamer.app.core {
    requires ua.renamer.app.api;
    requires static lombok;
    requires org.slf4j; // Required by lombok
    requires com.google.guice;
    requires jakarta.annotation;
    requires jakarta.inject; // Required by Guice
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.jspecify;


    exports ua.renamer.app.core.config;
    exports ua.renamer.app.core.service;
    exports ua.renamer.app.core.service.validator;
    exports ua.renamer.app.core.service.validator.impl;

    // v2 packages
    exports ua.renamer.app.core.service.impl;
    exports ua.renamer.app.core.service.transformation;
    exports ua.renamer.app.core.mapper;
    exports ua.renamer.app.core.util;

    opens ua.renamer.app.core.config;
    opens ua.renamer.app.core.service;
    opens ua.renamer.app.core.service.validator;
    opens ua.renamer.app.core.service.validator.impl;

    // v2 packages
    opens ua.renamer.app.core.service.impl;
    opens ua.renamer.app.core.service.transformation;
    opens ua.renamer.app.core.mapper;
    opens ua.renamer.app.core.util;
}
