package ua.renamer.app.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.core.service.DuplicateNameResolver;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of DuplicateNameResolver that appends suffixes to duplicate target names.
 * Uses pattern: " (01)", " (02)", etc.
 */
@Slf4j
public class DuplicateNameResolverImpl implements DuplicateNameResolver {

    @Override
    public List<PreparedFileModel> resolve(List<PreparedFileModel> models) {
        // Group by target name (full name with extension)
        Map<String, List<PreparedFileModel>> nameGroups = models.stream()
                .collect(Collectors.groupingBy(PreparedFileModel::getNewFullName));

        // Track used names to prevent re-collision
        Set<String> usedNames = new HashSet<>(nameGroups.keySet());

        // Process each group
        List<PreparedFileModel> result = new ArrayList<>();

        for (Map.Entry<String, List<PreparedFileModel>> entry : nameGroups.entrySet()) {
            String targetName = entry.getKey();
            List<PreparedFileModel> group = entry.getValue();

            if (group.size() == 1) {
                // No collision
                result.add(group.get(0));
                continue;
            }

            // Multiple files with same target name - keep first, append suffixes to rest
            log.debug("Found {} files with duplicate target name: {}", group.size(), targetName);

            // Calculate padding for duplicate suffixes
            // Strategy: infer padding from the base name if it's numeric, otherwise use group size
            int digits = calculateSuffixPadding(group);
            int counter = 1;
            boolean isFirst = true;

            for (PreparedFileModel model : group) {
                // Skip files that already have errors
                if (model.isHasError()) {
                    result.add(model);
                    continue;
                }

                // Keep first file with original name, add suffixes to rest
                if (isFirst) {
                    result.add(model);
                    isFirst = false;
                    log.debug("Keeping original name: {}", model.getNewFullName());
                    continue;
                }

                // Generate unique name for subsequent duplicates
                String uniqueName;
                String uniqueFullName;
                do {
                    String suffix = String.format(" (%0" + digits + "d)", counter++);
                    uniqueName = model.getNewName() + suffix;
                    uniqueFullName = model.getNewExtension().isEmpty()
                            ? uniqueName
                            : uniqueName + "." + model.getNewExtension();
                } while (usedNames.contains(uniqueFullName));

                usedNames.add(uniqueFullName);

                // Create updated model using toBuilder
                PreparedFileModel updated = model.toBuilder()
                        .withNewName(uniqueName)
                        .build();

                result.add(updated);

                log.debug("Resolved duplicate: {} -> {}",
                        model.getNewFullName(), updated.getNewFullName());
            }
        }

        return result;
    }

    /**
     * Calculate the number of digits to use for duplicate suffixes.
     * <p>
     * Strategy:
     * 1. If base name is zero-padded numeric (e.g., "01", "001"), use max(length, groupSize)
     * 2. Otherwise, use padding based on group size (number of files in collision)
     * <p>
     * This ensures that duplicate suffixes scale with the collision group size
     * and maintain consistency with zero-padded base names when present.
     * <p>
     * Examples:
     * - "01" with 3 files → zero-padded, max(2, 1) = 2 → " (01)"
     * - "10" with 10 files → not zero-padded, len(10)=2 → " (01)"
     * - "0" with 5 files → zero-padded, max(1, 1) = 1 → " (1)"
     * - "999" with 100 files → not zero-padded, len(100)=3 → " (001)"
     * - "100" with 2 files → not zero-padded, len(2)=1 → " (1)"
     *
     * @param group List of files with the same target name
     * @return Number of digits to use for padding
     */
    private int calculateSuffixPadding(List<PreparedFileModel> group) {
        if (group.isEmpty()) {
            return 1;
        }

        PreparedFileModel firstModel = group.get(0);
        String baseName = firstModel.getNewName();

        // Calculate natural padding based on group size (total number of files)
        int groupSizePadding = String.valueOf(group.size()).length();

        // If base name is zero-padded numeric (starts with '0' and is all digits)
        // use its length as minimum padding to maintain visual consistency
        if (baseName.matches("\\d+") && baseName.startsWith("0")) {
            int baseNamePadding = baseName.length();
            return Math.max(baseNamePadding, groupSizePadding);
        }

        // For non-zero-padded names, use group size padding
        return groupSizePadding;
    }
}
