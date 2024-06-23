package ua.renamer.app.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Represents metadata for a file, including dimensions for images/videos and tags for audio files.
 */
@Builder
@AllArgsConstructor
public class FileInformationMetadata {

    /**
     * The creation date and time of the file.
     */
    private final LocalDateTime creationDate;
    /**
     * The width of the image or video file.
     */
    private final Integer imgVidWidth;
    /**
     * The height of the image or video file.
     */
    private final Integer imgVidHeight;
    /**
     * The name of the artist for the audio file.
     */
    private final String audioArtistName;
    /**
     * The name of the album for the audio file.
     */
    private final String audioAlbumName;
    /**
     * The name of the song for the audio file.
     */
    private final String audioSongName;
    /**
     * The year of the audio file.
     */
    private final Integer audioYear;

    /**
     * Gets the creation date and time of the file as an Optional.
     *
     * @return an Optional containing the creation date if available, otherwise empty.
     */
    public Optional<LocalDateTime> getCreationDate() {
        return Optional.ofNullable(creationDate);
    }

    /**
     * Gets the width of the image or video file as an Optional.
     *
     * @return an Optional containing the width if available, otherwise empty.
     */
    public Optional<Integer> getImgVidWidth() {
        return Optional.ofNullable(imgVidWidth);
    }

    /**
     * Gets the height of the image or video file as an Optional.
     *
     * @return an Optional containing the height if available, otherwise empty.
     */
    public Optional<Integer> getImgVidHeight() {
        return Optional.ofNullable(imgVidHeight);
    }

    /**
     * Gets the name of the artist for the audio file as an Optional.
     *
     * @return an Optional containing the artist name if available, otherwise empty.
     */
    public Optional<String> getAudioArtistName() {
        return Optional.ofNullable(audioArtistName);
    }

    /**
     * Gets the name of the album for the audio file as an Optional.
     *
     * @return an Optional containing the album name if available, otherwise empty.
     */
    public Optional<String> getAudioAlbumName() {
        return Optional.ofNullable(audioAlbumName);
    }

    /**
     * Gets the name of the song for the audio file as an Optional.
     *
     * @return an Optional containing the song name if available, otherwise empty.
     */
    public Optional<String> getAudioSongName() {
        return Optional.ofNullable(audioSongName);
    }

    /**
     * Gets the year of the audio file as an Optional.
     *
     * @return an Optional containing the year if available, otherwise empty.
     */
    public Optional<Integer> getAudioYear() {
        return Optional.ofNullable(audioYear);
    }

}
