package ua.renamer.app.core.v2.model.config;

/**
 * Sealed interface for all transformation configuration types.
 *
 * <p>This sealed hierarchy provides compile-time exhaustiveness checking
 * when pattern matching on config types. The compiler will enforce that
 * all permitted subtypes are handled in switch expressions.
 *
 * <p><b>Adding New Configs:</b> When adding a new transformation mode:
 * <ol>
 *   <li>Create the new config class</li>
 *   <li>Add it to the permits clause below</li>
 *   <li>Implement this interface in the config class</li>
 *   <li>Compiler will force updates in FileRenameOrchestratorImpl</li>
 * </ol>
 */
public sealed interface TransformationConfig
        permits AddTextConfig,
        RemoveTextConfig,
        ReplaceTextConfig,
        CaseChangeConfig,
        DateTimeConfig,
        ImageDimensionsConfig,
        SequenceConfig,
        ParentFolderConfig,
        TruncateConfig,
        ExtensionChangeConfig {
    // Marker interface - no methods required
}
