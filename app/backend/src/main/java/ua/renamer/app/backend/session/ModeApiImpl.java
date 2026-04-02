package ua.renamer.app.backend.session;

import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.ModeParameters;
import ua.renamer.app.api.session.ValidationResult;

import java.util.concurrent.CompletableFuture;

/**
 * Package-private implementation of {@link ModeApi} backed by {@link RenameSessionService}.
 *
 * <p>Full listener fan-out and reset support will be added in TASK-3.5.
 * This stub satisfies the {@link RenameSessionService#selectMode} call contract.
 *
 * @param <P> the concrete {@link ModeParameters} subtype for this mode
 */
class ModeApiImpl<P extends ModeParameters> implements ModeApi<P> {

    private final TransformationMode mode;
    private final RenameSessionService service;
    private volatile P currentParams;

    /**
     * Creates a new {@code ModeApiImpl} with the given initial parameters, mode, and backing service.
     *
     * @param initialParams the default parameters for the mode; must not be null
     * @param mode          the transformation mode this API manages; must not be null
     * @param service       the backing session service used to delegate parameter updates; must not be null
     */
    ModeApiImpl(P initialParams, TransformationMode mode, RenameSessionService service) {
        this.currentParams = initialParams;
        this.mode = mode;
        this.service = service;
    }

    @Override
    public TransformationMode mode() {
        return mode;
    }

    @Override
    public P currentParameters() {
        return currentParams;
    }

    @Override
    public void addParameterListener(ModeApi.ParameterListener<P> listener) {
        // Full implementation in TASK-3.5
    }

    @Override
    public void removeParameterListener(ModeApi.ParameterListener<P> listener) {
        // Full implementation in TASK-3.5
    }

    @Override
    public CompletableFuture<ValidationResult> updateParameters(ModeApi.ParamMutator<P> mutator) {
        P updated = mutator.apply(currentParams);
        return service.updateParameters(updated).thenApply(v -> {
            if (!v.isError()) {
                currentParams = updated;
            }
            return v;
        });
    }

    @Override
    public CompletableFuture<Void> resetParameters() {
        throw new UnsupportedOperationException("resetParameters() will be implemented in TASK-3.5");
    }
}
