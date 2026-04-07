package ua.renamer.app.backend.settings;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import ua.renamer.app.api.settings.AppSettings;
import ua.renamer.app.api.settings.LogLevel;
import ua.renamer.app.api.settings.SettingsService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configure the Logback runtime based on the user's {@link AppSettings}.
 *
 * <p>Invoked eagerly by Guice during application startup via
 * {@link #configure()}, and again whenever the user changes logging
 * preferences via {@link #reconfigure(AppSettings)}.
 *
 * <p>File logging is written to a rolling log under
 * {@code <app-config-dir>/logs/renamer.log} with a 1 GB per-file cap,
 * a 2-file history, and a 2 GB total size cap. Console logging is
 * controlled exclusively by {@code logback.xml} and is not modified here.
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class LoggingConfigService {

    private final SettingsService settingsService;

    /**
     * Apply logging configuration from the current {@link AppSettings}.
     * Called automatically by Guice when the singleton is first created.
     */
    @Inject
    public void configure() {
        AppSettings settings = settingsService.getCurrent();
        applyLogLevel(settings.getLogLevel());
        if (settings.isLoggingEnabled()) {
            enableFileLogging(settingsService.getSettingsFilePath().getParent(), settings.getLogLevel());
        }
    }

    /**
     * Reapply logging configuration after a settings change.
     *
     * <p>Removes any existing FILE appender, then re-adds it if
     * {@link AppSettings#isLoggingEnabled()} is {@code true}.
     *
     * @param settings the new settings snapshot to apply; must not be null
     */
    public void reconfigure(final AppSettings settings) {
        applyLogLevel(settings.getLogLevel());
        removeFileAppender();
        if (settings.isLoggingEnabled()) {
            enableFileLogging(settingsService.getSettingsFilePath().getParent(), settings.getLogLevel());
        }
    }

    private void removeFileAppender() {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext ctx)) {
            return;
        }
        ctx.getLogger(Logger.ROOT_LOGGER_NAME).detachAppender("FILE");
    }

    private void applyLogLevel(final LogLevel level) {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext ctx)) {
            return;
        }
        ctx.getLogger("ua.renamer.app").setLevel(Level.toLevel(level.name(), Level.INFO));
    }

    private void enableFileLogging(final Path appDir, final LogLevel level) {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext ctx)) {
            log.warn("Cannot configure file logging: LoggerFactory is not a LoggerContext");
            return;
        }

        Path logFile = appDir.resolve("logs").resolve("renamer.log");
        try {
            Files.createDirectories(logFile.getParent());
        } catch (IOException e) {
            log.warn("Cannot create log directory: {}", logFile.getParent(), e);
            return;
        }

        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(ctx);
        appender.setName("FILE");
        appender.setFile(logFile.toString());

        SizeAndTimeBasedRollingPolicy<ILoggingEvent> policy = new SizeAndTimeBasedRollingPolicy<>();
        policy.setContext(ctx);
        policy.setParent(appender);
        policy.setFileNamePattern(
                appDir.resolve("logs").resolve("renamer.%d{yyyy-MM-dd}.%i.log").toString());
        policy.setMaxFileSize(FileSize.valueOf("1GB"));
        policy.setMaxHistory(2);
        policy.setTotalSizeCap(FileSize.valueOf("2GB"));
        policy.start();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(ctx);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{60} -- %msg%n");
        encoder.start();

        appender.setRollingPolicy(policy);
        appender.setEncoder(encoder);
        appender.start();

        ctx.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(appender);
        log.info("File logging enabled: {}", logFile);
    }
}
