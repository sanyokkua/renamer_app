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
    exports ua.renamer.app.core.enums;
    exports ua.renamer.app.core.service;
    exports ua.renamer.app.core.service.validator;
    exports ua.renamer.app.core.service.validator.impl;

    // v2 packages
    exports ua.renamer.app.core.v2.service;
    exports ua.renamer.app.core.v2.service.impl;
    exports ua.renamer.app.core.v2.service.transformation;
    exports ua.renamer.app.core.v2.mapper;
    exports ua.renamer.app.core.v2.util;

    opens ua.renamer.app.core.config;
    opens ua.renamer.app.core.service;
    opens ua.renamer.app.core.service.validator;
    opens ua.renamer.app.core.service.validator.impl;
    opens ua.renamer.app.core.enums;

    // v2 packages
    opens ua.renamer.app.core.v2.service;
    opens ua.renamer.app.core.v2.service.impl;
    opens ua.renamer.app.core.v2.service.transformation;
    opens ua.renamer.app.core.v2.mapper;
    opens ua.renamer.app.core.v2.util;
}
