package ua.renamer.app.core.v2.service;

/**
 * A functional interface for progress callbacks in V2 architecture.
 *
 * <p>This is a V2-specific copy independent from V1 progress tracking.</p>
 */
@FunctionalInterface
public interface ProgressCallback {

    /**
     * Updates the progress with the current and maximum values.
     *
     * @param currentValue the current value of the progress.
     * @param maxValue     the maximum value of the progress.
     */
    void updateProgress(int currentValue, int maxValue);

}
