package ua.renamer.app.core.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import ua.renamer.app.core.service.helper.DateTimeOperations;
import ua.renamer.app.core.v2.mapper.ThreadAwareFileMapper;
import ua.renamer.app.core.v2.mapper.ThreadAwareFileMetadataMapper;
import ua.renamer.app.core.v2.model.TransformationMode;
import ua.renamer.app.core.v2.service.DuplicateNameResolver;
import ua.renamer.app.core.v2.service.FileRenameOrchestrator;
import ua.renamer.app.core.v2.service.FileTransformationService;
import ua.renamer.app.core.v2.service.RenameExecutionService;
import ua.renamer.app.core.v2.service.impl.DuplicateNameResolverImpl;
import ua.renamer.app.core.v2.service.impl.FileRenameOrchestratorImpl;
import ua.renamer.app.core.v2.service.impl.RenameExecutionServiceImpl;
import ua.renamer.app.core.v2.service.transformation.*;
import ua.renamer.app.core.v2.util.CommonFileUtils;
import ua.renamer.app.core.v2.util.DateTimeConverter;

import java.util.Map;

/**
 * Dependency injection module for v2 services architecture.
 * Configures transformers, support services, and the main orchestrator.
 */
public class DIV2ServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        // Core v2 components (already exist from v2 mapper implementation)
        bind(ThreadAwareFileMapper.class).in(Singleton.class);
        bind(ThreadAwareFileMetadataMapper.class).in(Singleton.class);
        bind(CommonFileUtils.class).in(Singleton.class);
        bind(DateTimeConverter.class).in(Singleton.class);

        // v1 helper used by DateTimeTransformer
        bind(DateTimeOperations.class).in(Singleton.class);

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
        bind(TruncateTransformer.class).in(Singleton.class);
        bind(ExtensionChangeTransformer.class).in(Singleton.class);
        bind(ParentFolderTransformer.class).in(Singleton.class);
        bind(SequenceTransformer.class).in(Singleton.class);
        bind(ImageDimensionsTransformer.class).in(Singleton.class);
    }

    /**
     * Provides DateTimeTransformer as singleton with DateTimeConverter dependency.
     */
    @Provides
    @Singleton
    public DateTimeTransformer provideDateTimeTransformer(DateTimeConverter dateTimeConverter) {
        return new DateTimeTransformer(dateTimeConverter);
    }

    /**
     * Provides transformer registry mapping modes to their implementations.
     */
    @Provides
    @Singleton
    @Named("v2TransformerRegistry")
    public Map<TransformationMode, FileTransformationService<?>> provideTransformerRegistry(
            AddTextTransformer addText,
            RemoveTextTransformer removeText,
            ReplaceTextTransformer replaceText,
            CaseChangeTransformer caseChange,
            DateTimeTransformer dateTime,
            ImageDimensionsTransformer dimensions,
            SequenceTransformer sequence,
            ParentFolderTransformer parentFolder,
            TruncateTransformer truncate,
            ExtensionChangeTransformer extension
    ) {
        return Map.of(
                TransformationMode.ADD_TEXT, addText,
                TransformationMode.REMOVE_TEXT, removeText,
                TransformationMode.REPLACE_TEXT, replaceText,
                TransformationMode.CHANGE_CASE, caseChange,
                TransformationMode.USE_DATETIME, dateTime,
                TransformationMode.USE_IMAGE_DIMENSIONS, dimensions,
                TransformationMode.ADD_SEQUENCE, sequence,
                TransformationMode.USE_PARENT_FOLDER_NAME, parentFolder,
                TransformationMode.TRUNCATE_FILE_NAME, truncate,
                TransformationMode.CHANGE_EXTENSION, extension
        );
    }
}
