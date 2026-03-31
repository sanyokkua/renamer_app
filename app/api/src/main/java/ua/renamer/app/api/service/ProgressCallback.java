package ua.renamer.app.api.service;

/**
 * Functional interface for progress reporting during the rename pipeline.
 */
@FunctionalInterface
public interface ProgressCallback {

    /**
     * Called to report progress.
     *
     * @param currentValue the number of items processed so far
     * @param maxValue     the total number of items to process
     */
    void updateProgress(int currentValue, int maxValue);
}
