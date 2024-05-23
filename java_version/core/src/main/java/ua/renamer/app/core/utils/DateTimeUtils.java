package ua.renamer.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeUtils {

    public static LocalDateTime parseDateTimeString(String dateTimeString) {
        if (Objects.isNull(dateTimeString) || dateTimeString.isEmpty()) {
            return null;
        }

        var formats = List.of("yyyy-MM-dd'T'HH:mm:ss",
                              "EEE, dd MMM yyyy HH:mm:ss Z",
                              "EEE, dd MMM yyyy HH:mm:ss",
                              "yyyy-MM-dd HH:mm:ss Z",
                              "yyyy-MM-dd HH:mm:ss",
                              "yyyy-MM-dd'T'HH:mm:ssZ",
                              "yyyy:MM:dd HH:mm:ss"
                             );
        for (String format : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return LocalDateTime.parse(dateTimeString, formatter);
            } catch (DateTimeParseException e) {
                // Ignore and try next format
            }
        }
        throw new IllegalArgumentException("Date format not supported: " + dateTimeString);
    }

}
