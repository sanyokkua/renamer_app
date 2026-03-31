package ua.renamer.app.metadata.model.meta;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.metadata.model.meta.category.AudioMeta;
import ua.renamer.app.metadata.model.meta.category.ImageMeta;
import ua.renamer.app.metadata.model.meta.category.VideoMeta;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Value
@Builder(setterPrefix = "with")
public class FileMeta {
    private static final FileMeta EMPTY = FileMeta.builder().build();

    ImageMeta image;
    VideoMeta video;
    AudioMeta audio;

    @Builder.Default
    Map<String, String> metaInfo = Map.of(); // DirectoryName.TagName -> Value)
    @Builder.Default
    List<String> errors = List.of();

    public static FileMeta empty() {
        return EMPTY;
    }

    public static FileMeta withError(Exception ex) {
        return FileMeta.builder().withErrors(List.of(ex.getMessage())).build();
    }

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
