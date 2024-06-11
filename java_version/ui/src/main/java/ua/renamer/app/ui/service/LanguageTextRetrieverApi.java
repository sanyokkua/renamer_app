package ua.renamer.app.ui.service;

import ua.renamer.app.ui.enums.TextKeys;

import java.util.ResourceBundle;

/**
 * Interface for retrieving localized text strings.
 */
public interface LanguageTextRetrieverApi {

    /**
     * Retrieves the resource bundle containing localized text strings.
     *
     * @return The resource bundle.
     */
    ResourceBundle getResourceBundle();

    /**
     * Retrieves the localized text string corresponding to the provided key.
     *
     * @param key The key for the text string.
     *
     * @return The localized text string, or {@code null} if the key is not found.
     */
    String getString(String key);

    /**
     * Retrieves the localized text string corresponding to the provided key enum.
     *
     * @param key The key enum for the text string.
     *
     * @return The localized text string, or {@code null} if the key is not found.
     */
    String getString(TextKeys key);

    /**
     * Retrieves the localized text string corresponding to the provided key, with a default value fallback.
     *
     * @param key          The key for the text string.
     * @param defaultValue The default value to return if the key is not found.
     *
     * @return The localized text string, or the default value if the key is not found.
     */
    String getString(String key, String defaultValue);

    /**
     * Retrieves the localized text string corresponding to the provided key enum, with a default value fallback.
     *
     * @param key          The key enum for the text string.
     * @param defaultValue The default value to return if the key is not found.
     *
     * @return The localized text string, or the default value if the key is not found.
     */
    String getString(TextKeys key, String defaultValue);

}
