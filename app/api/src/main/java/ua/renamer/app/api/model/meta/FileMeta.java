package ua.renamer.app.api.model.meta;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.api.model.meta.category.AudioMeta;
import ua.renamer.app.api.model.meta.category.ImageMeta;
import ua.renamer.app.api.model.meta.category.VideoMeta;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable container for all metadata extracted from a file.
 * Category-specific metadata (image/video/audio) may be absent depending on file type.
 */
@Value
@Builder(setterPrefix = "with")
public class FileMeta {
    private static final FileMeta EMPTY = FileMeta.builder().build();

    ImageMeta image;
    VideoMeta video;
    AudioMeta audio;

    @Builder.Default
    Map<String, String> metaInfo = Map.of();
    @Builder.Default
    List<String> errors = List.of();

    /**
     * Returns an empty {@code FileMeta} instance with no metadata and no errors.
     *
     * @return the shared empty instance; never null
     */
    public static FileMeta empty() {
        return EMPTY;
    }

    /**
     * Returns a {@code FileMeta} instance containing the given exception message as an error.
     *
     * @param ex the exception whose message to record; must not be null
     * @return a FileMeta with the error recorded; never null
     */
    public static FileMeta withError(Exception ex) {
        return FileMeta.builder().withErrors(List.of(ex.getMessage())).build();
    }

    /**
     * Returns a {@code FileMeta} instance containing the given error message.
     *
     * @param errMsg the error message to record; must not be null
     * @return a FileMeta with the error recorded; never null
     */
    public static FileMeta withError(String errMsg) {
        return FileMeta.builder().withErrors(List.of(errMsg)).build();
    }

    public Optional<ImageMeta> getImageMeta() {
        return Optional.ofNullable(image);
    }

    public Optional<VideoMeta> getVideoMeta() {
        return Optional.ofNullable(video);
    }

    public Optional<AudioMeta> getAudioMeta() {
        return Optional.ofNullable(audio);
    }
}
