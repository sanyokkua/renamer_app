package ua.renamer.app.api.session;

import ua.renamer.app.api.enums.*;
import ua.renamer.app.api.model.TransformationMode;

import java.time.LocalDateTime;

/**
 * Parameters for the {@link TransformationMode#ADD_DATETIME} transformation mode.
 * Configures how date and/or time values are extracted from a source and embedded in the filename.
 *
 * @param source                      the origin of the date/time value; must not be null
 * @param dateFormat                  the format pattern for the date portion; required when {@code useDatePart} is true
 * @param timeFormat                  the format pattern for the time portion; required when {@code useTimePart} is true
 * @param position                    where the formatted date/time is inserted or used as replacement; must not be null
 * @param useDatePart                 {@code true} to include the date portion in the output
 * @param useTimePart                 {@code true} to include the time portion in the output
 * @param applyToExtension            {@code true} to also apply the transformation to the file extension
 * @param useFallbackDateTime         {@code true} to use a fallback date/time when the source value is unavailable
 * @param useCustomDateTimeAsFallback {@code true} to use {@code customDateTime} as the fallback value
 * @param customDateTime              the custom date/time to use when {@code source} is {@link DateTimeSource#CUSTOM_DATE}
 *                                    or as fallback; required when source is {@code CUSTOM_DATE}
 * @param useUppercaseForAmPm         {@code true} to render AM/PM markers in uppercase
 * @param dateTimeFormat              how the date and time parts are combined when both are used; may be null
 *                                    if only one of date/time is included
 * @param separator                   string inserted between the formatted datetime and the original name;
 *                                    use empty string for no separator
 */
public record DateTimeParams(
        DateTimeSource source,
        DateFormat dateFormat,
        TimeFormat timeFormat,
        ItemPositionWithReplacement position,
        boolean useDatePart,
        boolean useTimePart,
        boolean applyToExtension,
        boolean useFallbackDateTime,
        boolean useCustomDateTimeAsFallback,
        LocalDateTime customDateTime,
        boolean useUppercaseForAmPm,
        DateTimeFormat dateTimeFormat,
        String separator
) implements ModeParameters {

    @Override
    public TransformationMode mode() {
        return TransformationMode.ADD_DATETIME;
    }

    @Override
    public ValidationResult validate() {
        if (source == null) {
            return ValidationResult.fieldError("source", "must not be null");
        }
        if (!useDatePart && !useTimePart) {
            return ValidationResult.fieldError("useDatePart",
                    "at least one of useDatePart or useTimePart must be true");
        }
        if (useDatePart && dateFormat == null) {
            return ValidationResult.fieldError("dateFormat",
                    "must not be null when useDatePart is true");
        }
        if (useTimePart && timeFormat == null) {
            return ValidationResult.fieldError("timeFormat",
                    "must not be null when useTimePart is true");
        }
        if (source == DateTimeSource.CUSTOM_DATE && customDateTime == null) {
            return ValidationResult.fieldError("customDateTime",
                    "must not be null when source is CUSTOM_DATE");
        }
        return ValidationResult.valid();
    }

    /**
     * Returns a copy of this record with the given source.
     *
     * @param source the new date/time source; must not be null for valid configuration
     * @return a new {@link DateTimeParams} with the updated field
     */
    public DateTimeParams withSource(DateTimeSource source) {
        return new DateTimeParams(source, dateFormat, timeFormat, position, useDatePart,
                useTimePart, applyToExtension, useFallbackDateTime,
                useCustomDateTimeAsFallback, customDateTime, useUppercaseForAmPm,
                dateTimeFormat, separator);
    }

    /**
     * Returns a copy of this record with the given date format.
     *
     * @param dateFormat the new date format; required when useDatePart is true
     * @return a new {@link DateTimeParams} with the updated field
     */
    public DateTimeParams withDateFormat(DateFormat dateFormat) {
        return new DateTimeParams(source, dateFormat, timeFormat, position, useDatePart,
                useTimePart, applyToExtension, useFallbackDateTime,
                useCustomDateTimeAsFallback, customDateTime, useUppercaseForAmPm,
                dateTimeFormat, separator);
    }

    /**
     * Returns a copy of this record with the given time format.
     *
     * @param timeFormat the new time format; required when useTimePart is true
     * @return a new {@link DateTimeParams} with the updated field
     */
    public DateTimeParams withTimeFormat(TimeFormat timeFormat) {
        return new DateTimeParams(source, dateFormat, timeFormat, position, useDatePart,
                useTimePart, applyToExtension, useFallbackDateTime,
                useCustomDateTimeAsFallback, customDateTime, useUppercaseForAmPm,
                dateTimeFormat, separator);
    }

    /**
     * Returns a copy of this record with the given position.
     *
     * @param position the new insertion/replacement position; must not be null for valid configuration
     * @return a new {@link DateTimeParams} with the updated field
     */
    public DateTimeParams withPosition(ItemPositionWithReplacement position) {
        return new DateTimeParams(source, dateFormat, timeFormat, position, useDatePart,
                useTimePart, applyToExtension, useFallbackDateTime,
                useCustomDateTimeAsFallback, customDateTime, useUppercaseForAmPm,
                dateTimeFormat, separator);
    }

    /**
     * Returns a copy of this record with the given useDatePart flag.
     *
     * @param useDatePart {@code true} to include the date portion in output
     * @return a new {@link DateTimeParams} with the updated field
     */
    public DateTimeParams withUseDatePart(boolean useDatePart) {
        return new DateTimeParams(source, dateFormat, timeFormat, position, useDatePart,
                useTimePart, applyToExtension, useFallbackDateTime,
                useCustomDateTimeAsFallback, customDateTime, useUppercaseForAmPm,
                dateTimeFormat, separator);
    }

    /**
     * Returns a copy of this record with the given useTimePart flag.
     *
     * @param useTimePart {@code true} to include the time portion in output
     * @return a new {@link DateTimeParams} with the updated field
     */
    public DateTimeParams withUseTimePart(boolean useTimePart) {
        return new DateTimeParams(source, dateFormat, timeFormat, position, useDatePart,
                useTimePart, applyToExtension, useFallbackDateTime,
                useCustomDateTimeAsFallback, customDateTime, useUppercaseForAmPm,
                dateTimeFormat, separator);
    }

    /**
     * Returns a copy of this record with the given applyToExtension flag.
     *
     * @param applyToExtension {@code true} to transform the file extension as well
     * @return a new {@link DateTimeParams} with the updated field
     */
    public DateTimeParams withApplyToExtension(boolean applyToExtension) {
        return new DateTimeParams(source, dateFormat, timeFormat, position, useDatePart,
                useTimePart, applyToExtension, useFallbackDateTime,
                useCustomDateTimeAsFallback, customDateTime, useUppercaseForAmPm,
                dateTimeFormat, separator);
    }

    /**
     * Returns a copy of this record with the given useFallbackDateTime flag.
     *
     * @param useFallbackDateTime {@code true} to fall back to an alternative date/time when source is unavailable
     * @return a new {@link DateTimeParams} with the updated field
     */
    public DateTimeParams withUseFallbackDateTime(boolean useFallbackDateTime) {
        return new DateTimeParams(source, dateFormat, timeFormat, position, useDatePart,
                useTimePart, applyToExtension, useFallbackDateTime,
                useCustomDateTimeAsFallback, customDateTime, useUppercaseForAmPm,
                dateTimeFormat, separator);
    }

    /**
     * Returns a copy of this record with the given useCustomDateTimeAsFallback flag.
     *
     * @param useCustomDateTimeAsFallback {@code true} to use {@code customDateTime} as the fallback value
     * @return a new {@link DateTimeParams} with the updated field
     */
    public DateTimeParams withUseCustomDateTimeAsFallback(boolean useCustomDateTimeAsFallback) {
        return new DateTimeParams(source, dateFormat, timeFormat, position, useDatePart,
                useTimePart, applyToExtension, useFallbackDateTime,
                useCustomDateTimeAsFallback, customDateTime, useUppercaseForAmPm,
                dateTimeFormat, separator);
    }

    /**
     * Returns a copy of this record with the given custom date/time.
     *
     * @param customDateTime the custom date/time value; required when source is {@link DateTimeSource#CUSTOM_DATE}
     * @return a new {@link DateTimeParams} with the updated field
     */
    public DateTimeParams withCustomDateTime(LocalDateTime customDateTime) {
        return new DateTimeParams(source, dateFormat, timeFormat, position, useDatePart,
                useTimePart, applyToExtension, useFallbackDateTime,
                useCustomDateTimeAsFallback, customDateTime, useUppercaseForAmPm,
                dateTimeFormat, separator);
    }

    /**
     * Returns a copy of this record with the given useUppercaseForAmPm flag.
     *
     * @param useUppercaseForAmPm {@code true} to render AM/PM markers in uppercase
     * @return a new {@link DateTimeParams} with the updated field
     */
    public DateTimeParams withUseUppercaseForAmPm(boolean useUppercaseForAmPm) {
        return new DateTimeParams(source, dateFormat, timeFormat, position, useDatePart,
                useTimePart, applyToExtension, useFallbackDateTime,
                useCustomDateTimeAsFallback, customDateTime, useUppercaseForAmPm,
                dateTimeFormat, separator);
    }

    /**
     * Returns a copy of this record with the given dateTimeFormat.
     *
     * @param dateTimeFormat how the date and time parts are combined; may be null when only one part is used
     * @return a new {@link DateTimeParams} with the updated field
     */
    public DateTimeParams withDateTimeFormat(DateTimeFormat dateTimeFormat) {
        return new DateTimeParams(source, dateFormat, timeFormat, position, useDatePart,
                useTimePart, applyToExtension, useFallbackDateTime,
                useCustomDateTimeAsFallback, customDateTime, useUppercaseForAmPm,
                dateTimeFormat, separator);
    }

    /**
     * Returns a copy of this record with the given separator.
     *
     * @param separator string inserted between the datetime and the original filename; use empty string for none
     * @return a new {@link DateTimeParams} with the updated field
     */
    public DateTimeParams withSeparator(String separator) {
        return new DateTimeParams(source, dateFormat, timeFormat, position, useDatePart,
                useTimePart, applyToExtension, useFallbackDateTime,
                useCustomDateTimeAsFallback, customDateTime, useUppercaseForAmPm,
                dateTimeFormat, separator);
    }
}
