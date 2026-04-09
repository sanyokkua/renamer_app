package ua.renamer.app.backend.integration.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class TestManifest {

    private static TestManifest instance;
    private final Map<String, ManifestEntry> files;

    private TestManifest() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/integration-test-data/manifest.json");
            if (is == null) {
                throw new IllegalStateException("manifest.json not found at /integration-test-data/manifest.json");
            }
            JsonNode root = mapper.readTree(is);
            JsonNode filesNode = root.get("files");
            Map<String, ManifestEntry> map = new LinkedHashMap<>();
            filesNode.fields().forEachRemaining(e -> {
                JsonNode v = e.getValue();
                map.put(e.getKey(), new ManifestEntry(
                        textOrNull(v, "file_name"),
                        textOrNull(v, "extension"),
                        intOrNull(v, "width"),
                        intOrNull(v, "height"),
                        textOrNull(v, "content_creation_date"),
                        textOrNull(v, "audio_artist"),
                        textOrNull(v, "audio_title"),
                        intOrNull(v, "audio_year")
                ));
            });
            files = Map.copyOf(map);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load manifest.json", e);
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return (child == null || child.isNull()) ? null : child.textValue();
    }

    private static Integer intOrNull(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return (child == null || child.isNull()) ? null : child.intValue();
    }

    public static synchronized TestManifest load() {
        if (instance == null) {
            instance = new TestManifest();
        }
        return instance;
    }

    /**
     * Returns the manifest entry for the given relative path.
     *
     * @param relativePath relative path from integration-test-data root, e.g. "media/photo_1920x1080.jpg"
     */
    public ManifestEntry get(String relativePath) {
        ManifestEntry entry = files.get(relativePath);
        if (entry == null) {
            throw new IllegalArgumentException("No manifest entry for: " + relativePath);
        }
        return entry;
    }

    /**
     * Returns the content creation date for the given file, or empty if not present.
     */
    public Optional<LocalDateTime> getContentCreationDate(String relativePath) {
        String raw = get(relativePath).contentCreationDate();
        if (raw == null) {
            return Optional.empty();
        }
        return Optional.of(LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}
