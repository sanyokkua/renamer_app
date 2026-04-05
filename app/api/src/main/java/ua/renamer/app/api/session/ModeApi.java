package ua.renamer.app.api.session;

import ua.renamer.app.api.model.TransformationMode;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * API contract for interacting with a single transformation mode's configuration.
 *
 * <p>An instance is obtained by calling {@link SessionApi#selectMode(TransformationMode)}.
 * Selecting a different mode invalidates any previously returned instance.
 *
 * @param <P> the concrete {@link ModeParameters} subtype for this mode
 */
public interface ModeApi<P extends ModeParameters> {

    /**
     * Returns the transformation mode this API manages. Never null.
     */
    TransformationMode mode();

    /**
     * Returns an immutable snapshot of the current parameter values. Never null.
     */
    P currentParameters();

    /**
     * Registers a listener notified on parameter changes. Duplicates are ignored.
     */
    void addParameterListener(ParameterListener<P> listener);

    /**
     * Removes a previously registered listener. No-op if not registered.
     */
    void removeParameterListener(ParameterListener<P> listener);

    /**
     * Applies {@code mutator} to the current parameters, validates the result, and —
     * if valid — stores the new parameters and notifies all registered listeners.
     *
     * @param mutator receives the current (immutable) parameters and returns a modified copy
     * @return future completing with {@link ValidationResult#valid()} on success,
     * or a field-level error result if validation fails; never null
     */
    CompletableFuture<ValidationResult> updateParameters(ParamMutator<P> mutator);

    /**
     * Resets parameters to defaults. Notifies all registered listeners.
     *
     * @return future completing with {@code null} when the reset is done; never null
     */
    CompletableFuture<Void> resetParameters();

    /**
     * Synchronously transforms a synthetic example filename using the current parameters.
     * Returns empty if parameters are invalid or transformation fails.
     * Used by mode controllers to populate the live-preview label.
     */
    Optional<String> previewSingleFile(String exampleName, String exampleExtension);

    /**
     * Notified whenever parameters managed by a {@link ModeApi} change.
     */
    @FunctionalInterface
    interface ParameterListener<P extends ModeParameters> {
        /**
         * Called after a successful update or reset. {@code updated} is never null.
         */
        void onParametersChanged(P updated);
    }

    /**
     * Produces a new parameter instance from the current one.
     * Must not mutate {@code current} — all {@link ModeParameters} are immutable records.
     */
    @FunctionalInterface
    interface ParamMutator<P extends ModeParameters> {
        /**
         * @param current the current immutable parameters; never null. Must not return null.
         */
        P apply(P current);
    }
}
