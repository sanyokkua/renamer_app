module ua.renamer.app.core {
    requires static lombok;
    requires org.slf4j; // Required by lombok
    requires metadata.extractor;
    requires com.google.guice;
    requires jakarta.annotation;
    requires jakarta.inject; // Required by Guice
    requires org.apache.tika.core;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.apache.tika.parser.image;
    requires org.jspecify;
    requires jaudiotagger;
    requires com.google.common; // Audio metadata extraction


    exports ua.renamer.app.core.config;
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

    // v2 packages
    exports ua.renamer.app.core.v2.model;
    exports ua.renamer.app.core.v2.model.config;
    exports ua.renamer.app.core.v2.model.meta;
    exports ua.renamer.app.core.v2.model.meta.category;
    exports ua.renamer.app.core.v2.service;
    exports ua.renamer.app.core.v2.service.impl;
    exports ua.renamer.app.core.v2.service.transformation;
    exports ua.renamer.app.core.v2.mapper;
    exports ua.renamer.app.core.v2.mapper.strategy;
    exports ua.renamer.app.core.v2.mapper.strategy.format;
    exports ua.renamer.app.core.v2.util;
    exports ua.renamer.app.core.v2.enums;

    opens ua.renamer.app.core.config;
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

    // v2 packages
    opens ua.renamer.app.core.v2.model;
    opens ua.renamer.app.core.v2.model.config;
    opens ua.renamer.app.core.v2.model.meta;
    opens ua.renamer.app.core.v2.model.meta.category;
    opens ua.renamer.app.core.v2.service;
    opens ua.renamer.app.core.v2.service.impl;
    opens ua.renamer.app.core.v2.service.transformation;
    opens ua.renamer.app.core.v2.mapper;
    opens ua.renamer.app.core.v2.mapper.strategy;
    opens ua.renamer.app.core.v2.mapper.strategy.format;
    opens ua.renamer.app.core.v2.util;
    opens ua.renamer.app.core.v2.enums;
}