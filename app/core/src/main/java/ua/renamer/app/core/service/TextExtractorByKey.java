package ua.renamer.app.core.service;

import java.util.function.Function;

/**
 * Functional interface for extracting a text value by a given key from metadata.
 */
@FunctionalInterface
public interface TextExtractorByKey extends Function<String, String> {

}
