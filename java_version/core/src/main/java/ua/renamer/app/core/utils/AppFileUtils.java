package ua.renamer.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.model.AppFile;
import ua.renamer.app.core.model.Metadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppFileUtils {

    public static AppFile createAppFile(File file) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

            // Extract file name and extension
            String fileName = file.getName();
            String fileExtension = "";
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                fileExtension = fileName.substring(i + 1);
            }

            return AppFile.builder()
                    .absolutePath(file.getAbsolutePath())
                    .type(file.isDirectory() ? "Folder" : "File")
                    .fileName(fileName)
                    .nextName(fileName)
                    .fileExtension(fileExtension)
                    .fileExtensionNew(fileExtension)
                    .fileSize(file.length())
                    .fsCreationDate(attrs.creationTime().toMillis())
                    .fsModificationDate(attrs.lastModifiedTime().toMillis())
                    .metadata(Metadata.builder().build()) // No metadata can be extracted without additional libraries
                    .build();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generateHTML(AppFile appFile) {
        StringBuilder html = new StringBuilder();
        html.append("<html>");
        html.append("<head><title>").append(appFile.getFileName()).append("</title></head>");
        html.append("<body>");

        // File Information
        html.append("<h2>").append(appFile.getFileName()).append("</h2>");
        appendField(html, "Absolute Path", appFile.getAbsolutePath());
        appendField(html, "File Extension", appFile.getFileExtension());
        appendField(html, "File Size", String.valueOf(appFile.getFileSize()));
        appendField(html, "Creation Time", formatTime(appFile.getFsCreationDate()));
        appendField(html, "Modification Time", formatTime(appFile.getFsModificationDate()));
        appendField(html, "Content Creation Time", formatTime(appFile.getMetadata().getCreationDate()));

        // Metadata
        appendField(html, "Image/Video Width", String.valueOf(appFile.getMetadata().getImgVidWidth()));
        appendField(html, "Image/Video Height", String.valueOf(appFile.getMetadata().getImgVidHeight()));
        appendField(html, "Audio Artist Name", getValueOrDefault(appFile.getMetadata().getAudioArtistName()));
        appendField(html, "Audio Album Name", getValueOrDefault(appFile.getMetadata().getAudioAlbumName()));
        appendField(html, "Audio Song Name", getValueOrDefault(appFile.getMetadata().getAudioSongName()));
        appendField(html, "Audio Year", String.valueOf(appFile.getMetadata().getAudioYear()));
        if (appFile.getMetadata().getOtherFoundTagValues() != null) {
            for (Map.Entry<String, String> entry : appFile.getMetadata().getOtherFoundTagValues().entrySet()) {
                appendField(html, entry.getKey(), getValueOrDefault(entry.getValue()));
            }
        }

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    private static void appendField(StringBuilder html, String fieldName, String value) {
        html.append("<p>").append(fieldName).append(": ").append(getValueOrDefault(value)).append("</p>");
    }

    private static String formatTime(Long timeMillis) {
        if (timeMillis == null) {
            return "Not Available";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timeMillis));
    }

    private static String getValueOrDefault(String value) {
        return value != null && !value.isEmpty() ? value : "Not Available";
    }
}
