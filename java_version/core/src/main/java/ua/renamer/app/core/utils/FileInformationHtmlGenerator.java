package ua.renamer.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;

import java.time.LocalDateTime;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileInformationHtmlGenerator {

    public static final String DATA_IS_NOT_FOUND = "Data Is Not Found";

    public static String generateHtml(FileInformation fileInfo) {
        StringBuilder html = new StringBuilder();

        html.append("<html><body>");
        html.append("<h2>Absolute Path: ").append(fileInfo.getFileAbsolutePath()).append("</h2>");
        html.append("<p>File Extension: ").append(getFieldValue(fileInfo.getFileExtension())).append("</p>");

        long fileSize = fileInfo.getFileSize();
        html.append("<p>File Size: ").append(fileSize).append(" Bytes, ")
            .append(toKilobytes(fileSize)).append(" KB, ")
            .append(toMegabytes(fileSize)).append(" MB</p>");

        html.append("<p>Creation Time: ").append(formatTimestamp(fileInfo.getFsCreationDate())).append("</p>");
        html.append("<p>Modification Time: ").append(formatTimestamp(fileInfo.getFsModificationDate())).append("</p>");

        Optional<FileInformationMetadata> metadata = fileInfo.getMetadata();
        if (metadata.isPresent()) {
            FileInformationMetadata meta = metadata.get();
            html.append("<p>Content Creation Time: ").append(formatTimestamp(meta.getCreationDate())).append("</p>");
            html.append("<p>Image/Video Width: ").append(getFieldValue(meta.getImgVidWidth())).append("</p>");
            html.append("<p>Image/Video Height: ").append(getFieldValue(meta.getImgVidHeight())).append("</p>");
            html.append("<p>Audio Artist Name: ").append(getFieldValue(meta.getAudioArtistName())).append("</p>");
            html.append("<p>Audio Album Name: ").append(getFieldValue(meta.getAudioAlbumName())).append("</p>");
            html.append("<p>Audio Song Name: ").append(getFieldValue(meta.getAudioSongName())).append("</p>");
            html.append("<p>Audio Year: ").append(getFieldValue(meta.getAudioYear())).append("</p>");
        } else {
            html.append("<p>Content Creation Time: Data Is Not Found</p>");
            html.append("<p>Image/Video Width: Data Is Not Found</p>");
            html.append("<p>Image/Video Height: Data Is Not Found</p>");
            html.append("<p>Audio Artist Name: Data Is Not Found</p>");
            html.append("<p>Audio Album Name: Data Is Not Found</p>");
            html.append("<p>Audio Song Name: Data Is Not Found</p>");
            html.append("<p>Audio Year: Data Is Not Found</p>");
        }

        html.append("</body></html>");

        return html.toString();
    }

    private static String getFieldValue(Optional<?> optional) {
        return optional.map(Object::toString).orElse(DATA_IS_NOT_FOUND);
    }

    private static String getFieldValue(String value) {
        return value != null ? value : DATA_IS_NOT_FOUND;
    }

    private static String formatTimestamp(Optional<LocalDateTime> localDateTime) {
        return localDateTime.map(DateTimeUtils::formatLocalDateTime).orElse(DATA_IS_NOT_FOUND);
    }

    private static long toKilobytes(long bytes) {
        return bytes / 1024;
    }

    private static long toMegabytes(long bytes) {
        return toKilobytes(bytes) / 1024;
    }

}
