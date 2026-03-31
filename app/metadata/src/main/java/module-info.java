module ua.renamer.app.metadata {
    requires static lombok;
    requires org.slf4j; // Required by lombok
    requires metadata.extractor;
    requires com.google.guice;
    requires jakarta.annotation;
    requires jakarta.inject; // Required by Guice
    requires org.apache.tika.core;
    requires org.apache.tika.parser.image;
    requires ua.renamer.app.utils;
    requires ua.renamer.app.api;
    requires org.jspecify;
    requires jaudiotagger;
    requires com.google.common; // Audio metadata extraction

    exports ua.renamer.app.metadata.config;
    exports ua.renamer.app.metadata.extractor;
    exports ua.renamer.app.metadata.extractor.strategy;
    exports ua.renamer.app.metadata.extractor.strategy.format;
    exports ua.renamer.app.metadata.extractor.strategy.format.audio;
    exports ua.renamer.app.metadata.extractor.strategy.format.image;
    exports ua.renamer.app.metadata.extractor.strategy.format.video;
    exports ua.renamer.app.metadata.util;

    // Guice needs reflective access
    opens ua.renamer.app.metadata.config;
    opens ua.renamer.app.metadata.extractor;
    opens ua.renamer.app.metadata.extractor.strategy;
    opens ua.renamer.app.metadata.extractor.strategy.format;
    opens ua.renamer.app.metadata.extractor.strategy.format.audio;
    opens ua.renamer.app.metadata.extractor.strategy.format.image;
    opens ua.renamer.app.metadata.extractor.strategy.format.video;
    opens ua.renamer.app.metadata.util;
}
