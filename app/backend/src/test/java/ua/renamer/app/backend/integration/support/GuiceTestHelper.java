package ua.renamer.app.backend.integration.support;

import com.google.inject.Guice;
import com.google.inject.Injector;
import ua.renamer.app.api.service.FileRenameOrchestrator;
import ua.renamer.app.api.session.StatePublisher;
import ua.renamer.app.backend.config.DIBackendModule;

import static org.mockito.Mockito.mock;

public final class GuiceTestHelper {

    private static volatile Injector injector;

    private GuiceTestHelper() {
    }

    public static synchronized Injector getInjector() {
        if (injector == null) {
            injector = Guice.createInjector(
                    new DIBackendModule(),
                    b -> b.bind(StatePublisher.class).toInstance(mock(StatePublisher.class))
            );
        }
        return injector;
    }

    public static FileRenameOrchestrator getOrchestrator() {
        return getInjector().getInstance(FileRenameOrchestrator.class);
    }
}
