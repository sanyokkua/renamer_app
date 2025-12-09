package ua.renamer.app.core.v2.mapper.strategy.format.audio;

import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jspecify.annotations.Nullable;
import ua.renamer.app.core.v2.interfaces.FileMetadataExtractor;
import ua.renamer.app.core.v2.mapper.strategy.format.ExtractionResult;
import ua.renamer.app.core.v2.model.meta.FileMeta;
import ua.renamer.app.core.v2.model.meta.category.AudioMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified audio metadata extractor using jaudiotagger library.
 * Supports MP3, WAV, FLAC, M4A, OGG, WMA, and many other audio formats.
 * Uses a common API regardless of the underlying audio format.
 */
@Slf4j
public class UnifiedAudioFileMetadataExtractor implements FileMetadataExtractor {

    @Override
    public FileMeta extract(File file, String mimeType) {
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            AudioHeader header = audioFile.getAudioHeader();

            // Log detected format
            if (header != null) {
                String format = header.getFormat();
                String encodingType = header.getEncodingType();
                log.debug("Detected audio format: {} ({}), file: {}", format, encodingType, file.getName());
            }

            if (tag == null) {
                log.debug("No tag found in audio file: {}", file.getName());
                return FileMeta.builder().withAudio(AudioMeta.builder().build()).build();
            }

            List<String> errors = new ArrayList<>();

            // Extract metadata with fallbacks
            var artist = extractArtistSafely(tag);
            var album = extractAlbumSafely(tag);
            var title = extractTitleSafely(tag);
            var year = extractYearSafely(tag);
            var duration = extractDurationSafely(header);

            // Collect any extraction errors
            collectError(artist, "extracting artist", errors);
            collectError(album, "extracting album", errors);
            collectError(title, "extracting title", errors);
            collectError(year, "extracting year", errors);
            collectError(duration, "extracting duration", errors);

            // Build audio metadata
            AudioMeta audioMeta = AudioMeta.builder()
                                           .withArtistName(artist.value())
                                           .withAlbumName(album.value())
                                           .withSongName(title.value())
                                           .withYear(year.value())
                                           .withLength(duration.value())
                                           .build();

            return FileMeta.builder().withAudio(audioMeta).withErrors(errors).build();

        } catch (Exception e) {
            log.warn("Failed to extract audio metadata from: {}", file.getName(), e);
            return FileMeta.withError(e);
        }
    }

    private ExtractionResult<String> extractArtistSafely(Tag tag) {
        try {
            String artist = getFirstNonEmpty(tag, FieldKey.ARTIST);
            if (artist == null || artist.trim().isEmpty()) {
                // Fallback to album artist
                artist = getFirstNonEmpty(tag, FieldKey.ALBUM_ARTIST);
                if (artist != null) {
                    log.debug("Using ALBUM_ARTIST as fallback for ARTIST");
                }
            }
            return ExtractionResult.success(artist);
        } catch (Exception e) {
            log.debug("Error extracting artist", e);
            return ExtractionResult.failure("Failed to extract artist: " + e.getMessage());
        }
    }

    private ExtractionResult<String> extractAlbumSafely(Tag tag) {
        try {
            return ExtractionResult.success(getFirstNonEmpty(tag, FieldKey.ALBUM));
        } catch (Exception e) {
            log.debug("Error extracting album", e);
            return ExtractionResult.failure("Failed to extract album: " + e.getMessage());
        }
    }

    private ExtractionResult<String> extractTitleSafely(Tag tag) {
        try {
            return ExtractionResult.success(getFirstNonEmpty(tag, FieldKey.TITLE));
        } catch (Exception e) {
            log.debug("Error extracting title", e);
            return ExtractionResult.failure("Failed to extract title: " + e.getMessage());
        }
    }

    private ExtractionResult<Integer> extractYearSafely(Tag tag) {
        try {
            String yearStr = getFirstNonEmpty(tag, FieldKey.YEAR);
            if (yearStr != null && !yearStr.trim().isEmpty()) {
                try {
                    // Year might be "2023" or "2023-05-15", extract first 4 consecutive digits
                    String digits = yearStr.replaceAll("\\D", "");
                    if (digits.length() >= 4) {
                        int year = Integer.parseInt(digits.substring(0, 4));
                        // Sanity check: year should be reasonable
                        if (year >= 1900 && year <= 2100) {
                            return ExtractionResult.success(year);
                        }
                    }
                } catch (NumberFormatException e) {
                    log.debug("Invalid year format: {}", yearStr);
                }
            }
            return ExtractionResult.success(null);
        } catch (Exception e) {
            log.debug("Error extracting year", e);
            return ExtractionResult.failure("Failed to extract year: " + e.getMessage());
        }
    }

    private ExtractionResult<Integer> extractDurationSafely(AudioHeader header) {
        try {
            if (header != null) {
                int duration = header.getTrackLength();
                return ExtractionResult.success(duration > 0 ? duration : null);
            }
            return ExtractionResult.success(null);
        } catch (Exception e) {
            log.debug("Error extracting duration", e);
            return ExtractionResult.failure("Failed to extract duration: " + e.getMessage());
        }
    }

    @Nullable
    private String getFirstNonEmpty(Tag tag, FieldKey key) {
        try {
            String value = tag.getFirst(key);
            return (value != null && !value.trim().isEmpty()) ? value : null;
        } catch (Exception e) {
            log.debug("Error reading field {}: {}", key, e.getMessage());
            return null;
        }
    }

    private void collectError(ExtractionResult<?> result, String operation, List<String> errors) {
        if (result.hasError()) {
            log.debug("Error when {}: {}", operation, result.errorMessage());
            errors.add(result.errorMessage());
        }
    }
}
