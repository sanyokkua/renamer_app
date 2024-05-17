package ua.renamer.app.core.model;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Metadata {
    @Builder.Default
    @Setter(AccessLevel.PRIVATE)
    private long creationDate = 0;
    @Builder.Default
    @Setter(AccessLevel.PRIVATE)
    private int imgVidWidth = 0;
    @Builder.Default
    @Setter(AccessLevel.PRIVATE)
    private int imgVidHeight = 0;
    @Builder.Default
    @Setter(AccessLevel.PRIVATE)
    private String audioArtistName = "";
    @Builder.Default
    @Setter(AccessLevel.PRIVATE)
    private String audioAlbumName = "";
    @Builder.Default
    @Setter(AccessLevel.PRIVATE)
    private String audioSongName = "";
    @Builder.Default
    @Setter(AccessLevel.PRIVATE)
    private int audioYear = 0;
    @Builder.Default
    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> otherFoundTagValues = new HashMap<>();
}
