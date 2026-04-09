package ua.renamer.app.api.model;

/**
 * Carries the user's choice from the folder drop dialog.
 *
 * <p>{@link Action#CANCEL} — discard the entire drop operation.
 * <p>{@link Action#USE_AS_ITEM} — add the folder itself as a single renamable entry.
 * <p>{@link Action#USE_CONTENTS} — expand the folder; options control depth and
 * whether sub-folders are added as items.
 *
 * @param action                the chosen action; never null
 * @param recursive             only meaningful when action == USE_CONTENTS; true means
 *                              traverse all descendant directories, false means immediate
 *                              children only
 * @param includeFoldersAsItems only meaningful when action == USE_CONTENTS; true means
 *                              sub-directories encountered during traversal are added as
 *                              renamable items as well as their file contents
 */
public record FolderDropOptions(Action action, boolean recursive, boolean includeFoldersAsItems) {

    /**
     * Convenience factory — cancel with no options.
     */
    public static FolderDropOptions cancel() {
        return new FolderDropOptions(Action.CANCEL, false, false);
    }

    /**
     * Convenience factory — use folder itself as a single item.
     */
    public static FolderDropOptions useAsItem() {
        return new FolderDropOptions(Action.USE_AS_ITEM, false, false);
    }

    /**
     * Describes what to do when a folder is dropped onto the file list.
     */
    public enum Action {
        CANCEL,
        USE_AS_ITEM,
        USE_CONTENTS
    }
}
