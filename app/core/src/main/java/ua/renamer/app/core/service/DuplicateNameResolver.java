package ua.renamer.app.core.service;

import ua.renamer.app.api.model.PreparedFileModel;

import java.util.List;

/**
 * Service for resolving duplicate target names by appending suffixes.
 */
public interface DuplicateNameResolver {
    /**
     * Resolve duplicate target names by appending suffixes like " (01)", " (02)", etc.
     * Returns new list with unique names.
     *
     * @param models List of prepared file models that may contain duplicates
     * @return List with unique target names
     */
    List<PreparedFileModel> resolve(List<PreparedFileModel> models);
}
