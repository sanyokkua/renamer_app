package ua.renamer.app.core.lang;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A class for managing language resources and providing localized strings.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LanguageManager {

    @Getter
    private static ResourceBundle resourceBundle;

    /**
     * Sets the locale for the language manager.
     *
     * @param locale the locale to set.
     */
    public static void setLocale(Locale locale) {
        log.debug("Locale set to {}", locale);
        resourceBundle = ResourceBundle.getBundle("langs/lang", locale);
    }

    /**
     * Gets the localized string for the given key.
     *
     * @param key the key for the localized string.
     * @return the localized string if found, or an empty string otherwise.
     */
    public static String getString(String key) {
        return getString(key, "");
    }

    /**
     * Gets the localized string for the given TextKeys enum.
     *
     * @param key the TextKeys enum for the localized string.
     * @return the localized string if found, or an empty string otherwise.
     */
    public static String getString(TextKeys key) {
        return getString(key, "");
    }

    /**
     * Gets the localized string for the given key with a default value.
     *
     * @param key          the key for the localized string.
     * @param defaultValue the default value to return if the key is not found.
     * @return the localized string if found, or the default value otherwise.
     */
    public static String getString(String key, String defaultValue) {
        try {
            log.debug("Getting key {}", key);
            return resourceBundle.getString(key);
        } catch (MissingResourceException ex) {
            log.warn(ex.getMessage(), ex);
            log.debug("Returning default value {}", defaultValue);
            return defaultValue;
        }
    }

    /**
     * Gets the localized string for the given TextKeys enum with a default value.
     *
     * @param key          the TextKeys enum for the localized string.
     * @param defaultValue the default value to return if the key is not found.
     * @return the localized string if found, or the default value otherwise.
     */
    public static String getString(TextKeys key, String defaultValue) {
        log.debug("Getting key {}", key);
        return getString(key.getKeyString(), defaultValue);
    }

}
