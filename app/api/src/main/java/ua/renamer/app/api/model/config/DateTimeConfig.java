package ua.renamer.app.api.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.api.enums.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration for adding datetime to filenames.
 */
@Value
@Builder(setterPrefix = "with")
public class DateTimeConfig implements TransformationConfig {
    /**
     * Source of the datetime (FILE_CREATION, CONTENT_CREATION, etc.).
     */
    DateTimeSource source;

    /**
     * Date format to use.
     */
    DateFormat dateFormat;

    /**
     * Time format to use.
     */
    TimeFormat timeFormat;

    /**
     * Combined date-time format to use; optional, may be null.
     */
    DateTimeFormat dateTimeFormat;

    /**
     * Position where to add datetime (BEGIN, END, or REPLACE).
     */
    ItemPositionWithReplacement position;

    /**
     * Custom datetime value (when source is CUSTOM_DATE).
     */
    LocalDateTime customDateTime;

    /**
     * Separator between datetime and filename; optional, may be null.
     */
    String separator;

    /**
     * When {@code true} and the primary datetime source returns null,
     * falls back to the earliest of creation, modification, and content-creation dates.
     * Defaults to {@code false} to preserve existing V2 behavior.
     */
    @Builder.Default
    boolean useFallbackDateTime = false;

    /**
     * When {@code true}, the AM/PM designator in 12-hour time formats is uppercased (e.g. {@code AM}, {@code PM}).
     * When {@code false}, it is lowercased (e.g. {@code am}, {@code pm}).
     * Only applied when the configured {@link ua.renamer.app.api.enums.TimeFormat} is a 12-hour AM/PM format.
     * Defaults to {@code true} (uppercase is the conventional filename style).
     */
    @Builder.Default
    boolean useUppercaseForAmPm = true;

    /**
     * When {@code true} and {@link #useFallbackDateTime} is also {@code true},
     * uses the user-provided {@link #customDateTime} as the ultimate fallback when
     * all natural dates (creation, modification, content-creation) are null.
     * Defaults to {@code false}.
     */
    @Builder.Default
    boolean useCustomDateTimeAsFallback = false;

    /**
     * Returns the custom datetime value wrapped in an {@link Optional}.
     *
     * @return the custom datetime if set, or empty if not set
     */
    public Optional<LocalDateTime> getCustomDateTime() {
        return Optional.ofNullable(customDateTime);
    }

    // Partial Lombok builder — Lombok adds with* methods; we override build() for validation
    public static class DateTimeConfigBuilder {

        /**
         * Builds the {@link DateTimeConfig}, validating that required fields are non-null and that
         * customDateTime is provided when source is {@link DateTimeSource#CUSTOM_DATE}.
         *
         * @return a new {@link DateTimeConfig} instance
         * @throws NullPointerException     if source, dateFormat, timeFormat, or position is null
         * @throws IllegalArgumentException if source is CUSTOM_DATE and customDateTime is not set
         */
        public DateTimeConfig build() {
            Objects.requireNonNull(source, "source must not be null");
            Objects.requireNonNull(dateFormat, "dateFormat must not be null");
            Objects.requireNonNull(timeFormat, "timeFormat must not be null");
            Objects.requireNonNull(position, "position must not be null");
            if (source == DateTimeSource.CUSTOM_DATE && customDateTime == null) {
                throw new IllegalArgumentException(
                        "customDateTime must be set when source is CUSTOM_DATE");
            }
            return new DateTimeConfig(source, dateFormat, timeFormat, dateTimeFormat, position, customDateTime,
                    separator,
                    useFallbackDateTime$set && useFallbackDateTime$value,
                    !useUppercaseForAmPm$set || useUppercaseForAmPm$value,
                    useCustomDateTimeAsFallback$set && useCustomDateTimeAsFallback$value);
        }
    }
}
