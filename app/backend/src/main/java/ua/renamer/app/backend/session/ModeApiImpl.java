package ua.renamer.app.backend.session;

import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.ModeParameters;
import ua.renamer.app.api.session.ValidationResult;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Package-private implementation of {@link ModeApi} backed by {@link RenameSessionService}.
 *
 * <p>Parameter state is stored in an {@link AtomicReference} so that concurrent calls to
 * {@link #updateParameters} from any thread are race-free. Listener fan-out uses a
 * {@link CopyOnWriteArrayList} for lock-free reads; {@code addIfAbsent} silently drops
 * duplicate registrations as required by the interface contract.
 *
 * @param <P> the concrete {@link ModeParameters} subtype for this mode
 */
class ModeApiImpl<P extends ModeParameters> implements ModeApi<P> {

    private final TransformationMode transformationMode;
    private final RenameSessionService service;
    private final P defaultParams;
    private final AtomicReference<P> currentParams;
    private final CopyOnWriteArrayList<ModeApi.ParameterListener<P>> listeners = new CopyOnWriteArrayList<>();

    /**
     * Creates a new {@code ModeApiImpl} with the given initial parameters, mode, and backing service.
     *
     * @param initialParams the default parameters for the mode; must not be null
     * @param mode          the transformation mode this API manages; must not be null
     * @param service       the backing session service used to delegate parameter updates; must not be null
     */
    ModeApiImpl(P initialParams, TransformationMode mode, RenameSessionService service) {
        this.defaultParams = initialParams;
        this.currentParams = new AtomicReference<>(initialParams);
        this.transformationMode = mode;
        this.service = service;
    }

    @Override
    public TransformationMode mode() {
        return transformationMode;
    }

    @Override
    public P currentParameters() {
        return currentParams.get();
    }

    @Override
    public void addParameterListener(ModeApi.ParameterListener<P> listener) {
        listeners.addIfAbsent(listener);
    }

    @Override
    public void removeParameterListener(ModeApi.ParameterListener<P> listener) {
        listeners.remove(listener);
    }

    @Override
    public CompletableFuture<ValidationResult> updateParameters(ModeApi.ParamMutator<P> mutator) {
        P updated = mutator.apply(currentParams.get());
        return service.updateParameters(updated).thenApply(result -> {
            if (result.ok()) {
                currentParams.set(updated);
                listeners.forEach(l -> l.onParametersChanged(updated));
            }
            return result;
        });
    }

    @Override
    public Optional<String> previewSingleFile(String exampleName, String exampleExtension) {
        return service.previewSingleFile(transformationMode, currentParams.get(), exampleName, exampleExtension);
    }

    @Override
    public CompletableFuture<Void> resetParameters() {
        return service.updateParameters(defaultParams).thenApply(result -> {
            if (result.ok()) {
                currentParams.set(defaultParams);
                listeners.forEach(l -> l.onParametersChanged(defaultParams));
            }
            return null;
        });
    }
}
