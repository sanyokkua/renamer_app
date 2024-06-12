package ua.renamer.app.core.service.mapper.impl;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.core.service.TextExtractorByKey;
import ua.renamer.app.core.service.helper.DateTimeOperations;
import ua.renamer.app.core.service.mapper.DataMapper;
import ua.renamer.app.core.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class FileInformationToHtmlMapper implements DataMapper<RenameModel, String> {

    private static final String IS_FOLDER = "is_folder";
    private static final String IS_FILE = "is_file";
    private static final String ABSOLUTE_PATH = "absolute_path";
    private static final String FILE_NAME = "file_name";
    private static final String FILE_EXTENSION = "file_extension";
    private static final String FILE_TYPE = "file_type";
    private static final String FILE_CREATION_TIME = "file_creation_time";
    private static final String FILE_MODIFICATION_TIME = "file_modification_time";
    private static final String FILE_CONTENT_CREATION_TIME = "file_content_creation_time";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String SONG_AUTHOR = "song_author";
    private static final String SONG_NAME = "song_name";
    private static final String SONG_ALBUM = "song_album";
    private static final String SONG_YEAR = "song_year";

    private final DateTimeOperations dateTimeOperations;
    private final TextExtractorByKey textExtractor;

    @Override
    public String map(RenameModel renameModel) {
        // @formatter:off
        var absolutePath = getString(renameModel, model -> model.getFileInformation().getFileAbsolutePath());
        var fileName = getString(renameModel, model -> model.getFileInformation().getFileName());
        var fileExt = getString(renameModel, model -> model.getFileInformation().getFileExtension());
        var fileType = getString(renameModel, this::getType);
        var fcTime = getDateTimeOptional(renameModel, model -> model.getFileInformation().getFsCreationDate());
        var fmTime = getDateTimeOptional(renameModel, model -> model.getFileInformation().getFsModificationDate());
        var ccTime = getDateTimeOptional(renameModel, model -> model.getFileInformation().getMetadata().flatMap(FileInformationMetadata::getCreationDate));
        var width = getIntegerOptional(renameModel, model -> model.getFileInformation().getMetadata().flatMap(FileInformationMetadata::getImgVidWidth));
        var height = getIntegerOptional(renameModel, model -> model.getFileInformation().getMetadata().flatMap(FileInformationMetadata::getImgVidHeight));
        var songArtist = getStringOptional(renameModel, model -> model.getFileInformation().getMetadata().flatMap(FileInformationMetadata::getAudioArtistName));
        var songName = getStringOptional(renameModel, model -> model.getFileInformation().getMetadata().flatMap(FileInformationMetadata::getAudioSongName));
        var songAlbum = getStringOptional(renameModel, model -> model.getFileInformation().getMetadata().flatMap(FileInformationMetadata::getAudioAlbumName));
        var songYear = getIntegerOptional(renameModel, model -> model.getFileInformation().getMetadata().flatMap(FileInformationMetadata::getAudioYear));
        // @formatter:on
        var value = List.of(new TableItem(ABSOLUTE_PATH, absolutePath),
                            new TableItem(FILE_NAME, fileName),
                            new TableItem(FILE_EXTENSION, fileExt),
                            new TableItem(FILE_TYPE, fileType),
                            new TableItem(FILE_CREATION_TIME, fcTime),
                            new TableItem(FILE_MODIFICATION_TIME, fmTime),
                            new TableItem(FILE_CONTENT_CREATION_TIME, ccTime),
                            new TableItem(WIDTH, width),
                            new TableItem(HEIGHT, height),
                            new TableItem(SONG_AUTHOR, songArtist),
                            new TableItem(SONG_NAME, songName),
                            new TableItem(SONG_ALBUM, songAlbum),
                            new TableItem(SONG_YEAR, songYear));

        return generateHtmlWithTableItems(value);
    }

    private String getString(RenameModel renameModel, Function<RenameModel, String> stringExtractor) {
        if (Objects.isNull(renameModel) || Objects.isNull(stringExtractor)) {
            return "";
        }

        try {
            var resultString = stringExtractor.apply(renameModel);

            if (StringUtils.isEmpty(resultString)) {
                return "";
            }

            return resultString;
        } catch (Exception ex) {
            log.warn(ex.getMessage(), ex);
        }

        return "";
    }

    private String getType(RenameModel renameModel) {
        if (Objects.isNull(renameModel)) {
            return "";
        }

        String key;
        if (renameModel.getFileInformation().isFile()) {
            key = IS_FILE;
        } else {
            key = IS_FOLDER;
        }

        return textExtractor.apply(key);
    }

    private String getDateTimeOptional(RenameModel renameModel,
                                       Function<RenameModel, Optional<LocalDateTime>> dateExtractor) {
        if (Objects.isNull(renameModel) || Objects.isNull(dateExtractor)) {
            return "";
        }
        var dateTime = dateExtractor.apply(renameModel);
        return dateTime.map(dateTimeOperations::formatLocalDateTime).orElse("");
    }

    private String getIntegerOptional(RenameModel renameModel, Function<RenameModel, Optional<Integer>> intExtractor) {
        if (Objects.isNull(renameModel) || Objects.isNull(intExtractor)) {
            return "";
        }

        var integerValue = intExtractor.apply(renameModel);
        return integerValue.map(String::valueOf).orElse("");
    }

    private String getStringOptional(RenameModel renameModel, Function<RenameModel, Optional<String>> stringExtractor) {
        if (Objects.isNull(renameModel) || Objects.isNull(stringExtractor)) {
            return "";
        }

        var stringValue = stringExtractor.apply(renameModel);
        return stringValue.orElse("");

    }

    private String generateHtmlWithTableItems(List<TableItem> values) {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html>\n");
        builder.append("<html>\n");
        builder.append("<head>\n");
        builder.append("<meta charset=\"UTF-8\">\n");
        builder.append("<title>File Information</title>\n");
        builder.append("</head>\n");
        builder.append("<body>\n");
        builder.append("<table>");
        builder.append("<tbody>");

        values.stream().filter(value -> !value.value.isBlank()).forEach(value -> {
            String title = textExtractor.apply(value.title);
            builder.append("<tr>");
            builder.append("<td>%s</td>".formatted(title));
            builder.append("<td>%s</td>".formatted(value.value));
            builder.append("</tr>");
        });

        builder.append("</tbody>");
        builder.append("</table>");
        builder.append("</body>\n");
        builder.append("</html>");
        return builder.toString();
    }

    private record TableItem(String title, String value) {

    }

}
