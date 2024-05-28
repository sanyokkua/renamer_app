package ua.renamer.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    public static LocalDateTime findMinOrNull(LocalDateTime... values) {
        if (values == null || values.length == 0) {
            return null;
        }

        return Stream.of(values)
                     .filter(Objects::nonNull)
                     .min(LocalDateTime::compareTo)
                     .orElse(null);
    }

}
