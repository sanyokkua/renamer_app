package ua.renamer.app.backend.integration.support;

public record ManifestEntry(
        String fileName,
        String extension,
        Integer width,
        Integer height,
        String contentCreationDate,
        String audioArtist,
        String audioTitle,
        Integer audioYear
) {
}
