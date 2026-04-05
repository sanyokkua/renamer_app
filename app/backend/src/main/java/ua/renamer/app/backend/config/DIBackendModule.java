package ua.renamer.app.backend.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import ua.renamer.app.api.session.SessionApi;
import ua.renamer.app.backend.service.BackendExecutor;
import ua.renamer.app.backend.session.RenameSessionService;
import ua.renamer.app.core.config.DIV2ServiceModule;
import ua.renamer.app.metadata.config.DIMetadataModule;

/**
 * Guice module that wires the backend service layer.
 *
 * <p>Installs {@link DIMetadataModule} and {@link DIV2ServiceModule} as sub-modules,
 * making this the composition root for all file-processing infrastructure.
 * {@link BackendExecutor} and {@link RenameSessionService} are bound as singletons.
 * {@code StatePublisher} is intentionally NOT bound here —
 * {@code app/ui}'s {@code DIUIModule} provides the JavaFX-aware implementation.
 */
public class DIBackendModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new DIMetadataModule());
        install(new DIV2ServiceModule());

        bind(BackendExecutor.class).in(Scopes.SINGLETON);
        bind(SessionApi.class).to(RenameSessionService.class).in(Scopes.SINGLETON);
        // StatePublisher is NOT bound here — app/ui's DIUIModule provides it
    }
}
