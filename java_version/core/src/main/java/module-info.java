module ua.renamer.app.core {
    requires static lombok;
    requires org.slf4j;
    requires ch.qos.logback.core;

    exports ua.renamer.app.core.lang;
    exports ua.renamer.app.core.enums;
    exports ua.renamer.app.core.model;
    exports ua.renamer.app.core.abstracts;
    exports ua.renamer.app.core.utils;
    exports ua.renamer.app.core.commands;
    exports ua.renamer.app.core.commands.preparation;

    opens langs;
}