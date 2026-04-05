package ua.renamer.app.core.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import ua.renamer.app.api.interfaces.FileMapper;
import ua.renamer.app.api.service.FileRenameOrchestrator;
import ua.renamer.app.core.mapper.ThreadAwareFileMapper;
import ua.renamer.app.core.service.DuplicateNameResolver;
import ua.renamer.app.core.service.RenameExecutionService;
import ua.renamer.app.core.service.impl.DuplicateNameResolverImpl;
import ua.renamer.app.core.service.impl.FileRenameOrchestratorImpl;
import ua.renamer.app.core.service.impl.RenameExecutionServiceImpl;
import ua.renamer.app.core.service.transformation.*;

/**
 * Dependency injection module for v2 services architecture.
 * Configures transformers, support services, and the main orchestrator.
 * Infrastructure bindings (FileUtils, DateTimeUtils, FileMetadataMapper, etc.)
 * are provided by DIMetadataModule installed in the composition root (backend).
 */
public class DIV2ServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        // FileMapper implementation — depends on FileUtils and FileMetadataMapper from DIMetadataModule
        bind(FileMapper.class).to(ThreadAwareFileMapper.class).in(Singleton.class);

        // Support services
        bind(DuplicateNameResolver.class).to(DuplicateNameResolverImpl.class).in(Singleton.class);
        bind(RenameExecutionService.class).to(RenameExecutionServiceImpl.class).in(Singleton.class);

        // Main orchestrator
        bind(FileRenameOrchestrator.class).to(FileRenameOrchestratorImpl.class).in(Singleton.class);

        // Transformers (stateless, can be singletons)
        bind(AddTextTransformer.class).in(Singleton.class);
        bind(RemoveTextTransformer.class).in(Singleton.class);
        bind(ReplaceTextTransformer.class).in(Singleton.class);
        bind(CaseChangeTransformer.class).in(Singleton.class);
        bind(DateTimeTransformer.class).in(Singleton.class);
        bind(TruncateTransformer.class).in(Singleton.class);
        bind(ExtensionChangeTransformer.class).in(Singleton.class);
        bind(ParentFolderTransformer.class).in(Singleton.class);
        bind(SequenceTransformer.class).in(Singleton.class);
        bind(ImageDimensionsTransformer.class).in(Singleton.class);
    }
}
