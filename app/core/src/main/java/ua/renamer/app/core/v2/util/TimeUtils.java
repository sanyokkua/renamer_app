package ua.renamer.app.core.v2.util;

import lombok.experimental.UtilityClass;

import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;

@UtilityClass
public class TimeUtils {
    private static final LocalDateTime MINIMAL = LocalDateTime.of(1900, 1, 1, 0, 0);

    public static ZoneId getZoneId() {
        return ZoneId.systemDefault();
    }

    public static LocalDateTime toLocalDateTime(FileTime fileTime) {
        if (fileTime == null) {
            return null;
        }

        return LocalDateTime.ofInstant(fileTime.toInstant(), TimeUtils.getZoneId());
    }
}
