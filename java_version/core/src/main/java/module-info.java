module ua.renamer.app.core {
    requires static lombok;
    requires org.slf4j;
    requires ch.qos.logback.core;
    requires metadata.extractor;
    requires java.desktop;
    requires com.google.guice;
    requires jakarta.annotation;

    exports ua.renamer.app.core.enums;
    exports ua.renamer.app.core.model;
    exports ua.renamer.app.core.util;
    exports ua.renamer.app.core.service;
    exports ua.renamer.app.core.service.command;
    exports ua.renamer.app.core.service.command.impl;
    exports ua.renamer.app.core.service.command.impl.preparation;
    exports ua.renamer.app.core.service.mapper;
    exports ua.renamer.app.core.service.mapper.impl;
    exports ua.renamer.app.core.service.mapper.impl.metadata;
    exports ua.renamer.app.core.service.mapper.impl.metadata.audio;
    exports ua.renamer.app.core.service.mapper.impl.metadata.video;
    exports ua.renamer.app.core.service.mapper.impl.metadata.images;
    exports ua.renamer.app.core.service.helper;
    exports ua.renamer.app.core.service.file;
    exports ua.renamer.app.core.service.file.impl;
    exports ua.renamer.app.core.service.validator;
    exports ua.renamer.app.core.service.validator.impl;

    opens ua.renamer.app.core.model;
    opens ua.renamer.app.core.util;
    opens ua.renamer.app.core.service;
    opens ua.renamer.app.core.service.command;
    opens ua.renamer.app.core.service.command.impl;
    opens ua.renamer.app.core.service.command.impl.preparation;
    opens ua.renamer.app.core.service.mapper;
    opens ua.renamer.app.core.service.mapper.impl;
    opens ua.renamer.app.core.service.mapper.impl.metadata;
    opens ua.renamer.app.core.service.mapper.impl.metadata.audio;
    opens ua.renamer.app.core.service.mapper.impl.metadata.video;
    opens ua.renamer.app.core.service.mapper.impl.metadata.images;
    opens ua.renamer.app.core.service.helper;
    opens ua.renamer.app.core.service.file;
    opens ua.renamer.app.core.service.file.impl;
    opens ua.renamer.app.core.service.validator;
    opens ua.renamer.app.core.service.validator.impl;
    opens ua.renamer.app.core.enums;
}