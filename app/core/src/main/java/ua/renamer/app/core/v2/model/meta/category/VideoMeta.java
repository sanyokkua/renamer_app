package ua.renamer.app.core.v2.model.meta.category;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Optional;

@Value
@Builder(setterPrefix = "with")
public class VideoMeta {
    LocalDateTime contentCreationDate;
    Integer width;
    Integer height;
    Integer duration;

    public Optional<LocalDateTime> getContentCreationDate() {
        return Optional.ofNullable(contentCreationDate);
    }

    public Optional<Integer> getWidth() {
        return Optional.ofNullable(width);
    }

    public Optional<Integer> getHeight() {
        return Optional.ofNullable(height);
    }

    public Optional<Integer> getDuration() {
        return Optional.ofNullable(duration);
    }

}
