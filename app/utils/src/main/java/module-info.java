module ua.renamer.app.utils {
    requires static lombok;
    requires org.slf4j;
    requires org.jspecify;
    requires org.apache.commons.io;

    exports ua.renamer.app.utils;
    exports ua.renamer.app.utils.text;
    exports ua.renamer.app.utils.datetime;
    exports ua.renamer.app.utils.file;
}
