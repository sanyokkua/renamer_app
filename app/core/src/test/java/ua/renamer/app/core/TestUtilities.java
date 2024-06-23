package ua.renamer.app.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestUtilities {

    public static final int TEST_YEAR = 2024;
    public static final int TEST_MONTH = 6;
    public static final int TEST_DAY_OF_MONTH = 20;
    public static final int TEST_HOUR = 17;
    public static final int TEST_MINUTE = 0;
    public static final int TEST_SECOND = 59;
    public static final LocalDateTime TEST_DEFAULT_TIME = LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY_OF_MONTH, TEST_HOUR, TEST_MINUTE, TEST_SECOND);
    public static final int TEST_IMG_VID_WIDTH = 1920;
    public static final int TEST_IMG_VID_HEIGHT = 1080;
    public static final String TEST_ARTIST_NAME = "ArtistName";
    public static final String TEST_ALBUM_NAME = "AlbumName";
    public static final String TEST_SONG_NAME = "SongName";
    public static final int TEST_AUDIO_YEAR = 1999;
    public static final String TEST_DEFAULT_FILE_PATH = "/";
    public static final String TEST_ABSOLUTE_PATH = "/file/path/Image_1.jpg";
    public static final boolean TEST_IS_FILE = true;
    public static final String TEST_FILE_NAME = "Image_1";
    public static final String TEST_FILE_EXTENSION = ".jpg";
    public static final int TEST_FILE_SIZE = 1000;

    public static final LocalDateTime TEST_FC_TIME_2005_10_12_12_00_05 = LocalDateTime.of(2005, 10, 12, 12, 0, 5);
    public static final LocalDateTime TEST_FM_TIME_2010_09_12_09_00_00 = LocalDateTime.of(2010, 9, 12, 9, 0, 0);
    public static final LocalDateTime TEST_CC_TIME_2005_09_12_12_00_00 = LocalDateTime.of(2005, 9, 12, 12, 0, 0);

    public static LocaleAmPm getLocaleAmPm() {
        var morning = LocalDateTime.of(2000, 12, 12, 8, 0, 0);
        var afternoon = LocalDateTime.of(2000, 12, 12, 13, 0, 0);
        var formatter = DateTimeFormatter.ofPattern("YYYYMMDD_HHMMSS a");
        var formattedMorning = morning.format(formatter);
        var formattedAfternoon = afternoon.format(formatter);
        var indexMorning = formattedMorning.indexOf(" ");
        var indexAfternoon = formattedMorning.indexOf(" ");
        var amLocaleSymbols = formattedMorning.substring(indexMorning + 1);
        var pmLocaleSymbols = formattedAfternoon.substring(indexAfternoon + 1);

        return new LocaleAmPm(amLocaleSymbols, pmLocaleSymbols);
    }

    public record LocaleAmPm(String AM, String PM) {

    }

}
