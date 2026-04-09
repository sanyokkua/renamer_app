package ua.renamer.app.ui.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.service.TextExtractorByKey;
import ua.renamer.app.core.service.validator.impl.NameValidator;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;

/**
 * Dependency Injection module configuration for the application. Includes only core module dependencies.
 */
@Slf4j
@Getter
public class DICoreModule extends AbstractModule {

    @Override
    protected void configure() {
        bindApplicationServices();
    }

    private void bindApplicationServices() {
        bind(NameValidator.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public TextExtractorByKey provideTextExtractorByKey(LanguageTextRetrieverApi languageTextRetrieverApi) {
        return s -> languageTextRetrieverApi.getString(s, "");
    }
}

