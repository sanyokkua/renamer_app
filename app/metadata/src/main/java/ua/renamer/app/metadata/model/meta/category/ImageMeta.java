package ua.renamer.app.metadata.model.meta.category;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Optional;

@Value
@Builder(setterPrefix = "with")
public class ImageMeta {
    LocalDateTime contentCreationDate;
    Integer width;
    Integer height;

    public Optional<LocalDateTime> getContentCreationDate() {
        return Optional.ofNullable(contentCreationDate);
    }

    public Optional<Integer> getWidth() {
        return Optional.ofNullable(width);
    }

    public Optional<Integer> getHeight() {
        return Optional.ofNullable(height);
    }
}
