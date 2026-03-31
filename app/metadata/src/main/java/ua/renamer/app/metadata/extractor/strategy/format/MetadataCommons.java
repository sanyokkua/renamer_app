package ua.renamer.app.metadata.extractor.strategy.format;

import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.util.HashMap;
import java.util.Map;

public class MetadataCommons {
    public static Map<String, String> buildMetadataMap(Metadata metadata) {
        Map<String, String> metadataMap = new HashMap<>();

        for (Directory directory : metadata.getDirectories()) {
            String dirName = directory.getName();

            for (Tag tag : directory.getTags()) {
                String key = dirName + "." + tag.getTagName();
                String value = tag.getDescription();

                // Only add non-null values
                if (value != null) {
                    metadataMap.put(key, value);
                }
            }
        }

        return metadataMap;
    }
}
