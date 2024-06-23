package ua.renamer.app.ui.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;
import ua.renamer.app.ui.service.impl.LanguageTextRetrieverService;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Dependency Injection module configuration for the application.
 */
@Slf4j
@Getter
public class DIAppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(LanguageTextRetrieverApi.class).to(LanguageTextRetrieverService.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public ResourceBundle provideResourceBundle() {
        Locale locale = Locale.getDefault();
        return ResourceBundle.getBundle("langs/lang", locale);
    }

    @Provides
    @Singleton
    public ExecutorService provideExecutorService() {
        return Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
    }

}
