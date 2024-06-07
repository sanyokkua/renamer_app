package ua.renamer.app.core.commands.preparation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {

    public static final int TEST_YEAR = 2024;
    public static final int TEST_MONTH = 6;
    public static final int TEST_DAY_OF_MONTH = 20;
    public static final int TEST_HOUR = 17;
    public static final int TEST_MINUTE = 0;
    public static final int TEST_SECOND = 59;
    public static final LocalDateTime TEST_DEFAULT_TIME = LocalDateTime.of(TEST_YEAR,
                                                                           TEST_MONTH,
                                                                           TEST_DAY_OF_MONTH,
                                                                           TEST_HOUR,
                                                                           TEST_MINUTE,
                                                                           TEST_SECOND
                                                                          );
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

}
