package ua.renamer.app.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Builder
@AllArgsConstructor
public class FileInformationMetadata {

    private final LocalDateTime creationDate;
    private final Integer imgVidWidth;
    private final Integer imgVidHeight;
    private final String audioArtistName;
    private final String audioAlbumName;
    private final String audioSongName;
    private final Integer audioYear;

    @Getter
    @Builder.Default
    private final Map<String, String> otherFoundTagValues = new HashMap<>();

    public Optional<LocalDateTime> getCreationDate() {
        return Optional.ofNullable(creationDate);
    }

    public Optional<Integer> getImgVidWidth() {
        return Optional.ofNullable(imgVidWidth);
    }

    public Optional<Integer> getImgVidHeight() {
        return Optional.ofNullable(imgVidHeight);
    }

    public Optional<String> getAudioArtistName() {
        return Optional.ofNullable(audioArtistName);
    }

    public Optional<String> getAudioAlbumName() {
        return Optional.ofNullable(audioAlbumName);
    }

    public Optional<String> getAudioSongName() {
        return Optional.ofNullable(audioSongName);
    }

    public Optional<Integer> getAudioYear() {
        return Optional.ofNullable(audioYear);
    }

}
