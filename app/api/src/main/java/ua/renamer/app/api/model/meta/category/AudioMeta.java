package ua.renamer.app.api.model.meta.category;

import lombok.Builder;
import lombok.Value;

import java.util.Optional;

/**
 * Immutable metadata extracted from audio files.
 */
@Value
@Builder(setterPrefix = "with")
public class AudioMeta {
    String artistName;
    String albumName;
    String songName;
    Integer year;
    Integer length;

    public Optional<String> getArtistName() {
        return Optional.ofNullable(artistName);
    }

    public Optional<String> getAlbumName() {
        return Optional.ofNullable(albumName);
    }

    public Optional<String> getSongName() {
        return Optional.ofNullable(songName);
    }

    public Optional<Integer> getYear() {
        return Optional.ofNullable(year);
    }

    public Optional<Integer> getLength() {
        return Optional.ofNullable(length);
    }
}
