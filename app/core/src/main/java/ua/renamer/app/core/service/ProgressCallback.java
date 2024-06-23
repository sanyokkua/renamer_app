package ua.renamer.app.core.service;

/**
 * A functional interface for progress callbacks.
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
