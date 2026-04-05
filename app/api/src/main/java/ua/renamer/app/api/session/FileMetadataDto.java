package ua.renamer.app.api.session;

import java.time.LocalDateTime;

/**
 * FX-safe snapshot of a file's extracted metadata.
 * All fields are nullable — callers must handle absent values.
 *
 * <p>Produced by {@link SessionApi#getFileMetadata(String)} and safe to read
 * from any thread after it is returned.
 *
 * @param mimeType            detected MIME type; {@code null} if unknown
 * @param fileSize            file size in bytes
 * @param category            file category name: {@code "IMAGE"}, {@code "VIDEO"},
 *                            {@code "AUDIO"}, or {@code "GENERIC"}
 * @param contentCreationDate EXIF/embedded content creation date; {@code null} if unavailable
 * @param widthPx             image/video width in pixels; {@code null} if not applicable
 * @param heightPx            image/video height in pixels; {@code null} if not applicable
 * @param audioArtist         audio artist name; {@code null} if not audio
 * @param audioAlbum          audio album name; {@code null} if not audio
 * @param audioTitle          audio track title; {@code null} if not audio
 * @param audioYear           audio release year; {@code null} if not audio
 */
public record FileMetadataDto(
        String mimeType,
        long fileSize,
        String category,
        LocalDateTime contentCreationDate,
        Integer widthPx,
        Integer heightPx,
        String audioArtist,
        String audioAlbum,
        String audioTitle,
        Integer audioYear
) {
}
