module ua.renamer.app.core {
    requires static lombok;
    requires org.slf4j;
    requires ch.qos.logback.core;
    requires metadata.extractor;
    requires java.desktop;

    exports ua.renamer.app.core.lang;
    exports ua.renamer.app.core.enums;
    exports ua.renamer.app.core.model;
    exports ua.renamer.app.core.abstracts;
    exports ua.renamer.app.core.utils;
    exports ua.renamer.app.core.commands;
    exports ua.renamer.app.core.commands.preparation;
    exports ua.renamer.app.core.mappers.audio;
    exports ua.renamer.app.core.mappers.video;
    exports ua.renamer.app.core.mappers.images;

    opens langs;
}