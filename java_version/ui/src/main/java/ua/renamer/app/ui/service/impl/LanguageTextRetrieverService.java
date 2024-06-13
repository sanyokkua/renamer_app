package ua.renamer.app.ui.service.impl;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class LanguageTextRetrieverService implements LanguageTextRetrieverApi {

    private final ResourceBundle resourceBundle;

    @Override
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    @Override
    public String getString(String key) {
        return getString(key, "");
    }

    @Override
    public String getString(TextKeys key) {
        return getString(key, "");
    }

    @Override
    public String getString(TextKeys key, String defaultValue) {
        log.debug("Getting string for TextKey {}", key);
        return getString(key.getKeyString(), defaultValue);
    }

    @Override
    public String getString(String key, String defaultValue) {
        try {
            log.debug("Getting string for key {}", key);
            var value = getResourceBundle().getString(key);
            log.debug("KeyValue mapping result: {} -> {}", key, value);
            return value;
        } catch (MissingResourceException ex) {
            log.warn(ex.getMessage(), ex);
            log.debug("Returning default value {} for key {}", defaultValue, key);
            return defaultValue;
        }
    }

}
