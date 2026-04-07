package ua.renamer.app.core.service.impl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.*;
import ua.renamer.app.api.model.config.AddTextConfig;
import ua.renamer.app.api.model.config.SequenceConfig;
import ua.renamer.app.api.service.ProgressCallback;
import ua.renamer.app.core.mapper.ThreadAwareFileMapper;
import ua.renamer.app.core.service.DuplicateNameResolver;
import ua.renamer.app.core.service.RenameExecutionService;
import ua.renamer.app.core.service.transformation.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.anyInt;

/**
 * Comprehensive unit tests for FileRenameOrchestratorImpl.
 * Tests the complete 4-phase pipeline: Extract → Transform → Deduplicate → Execute.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileRenameOrchestratorImplTest {

    private FileRenameOrchestratorImpl orchestrator;
    private ThreadAwareFileMapper fileMapper;
    private DuplicateNameResolver duplicateResolver;
    private RenameExecutionService renameExecutor;

    // Mock transformers
    private AddTextTransformer addTextTransformer;
    private RemoveTextTransformer removeTextTransformer;
    private ReplaceTextTransformer replaceTextTransformer;
    private CaseChangeTransformer caseChangeTransformer;
    private DateTimeTransformer dateTimeTransformer;
    private ImageDimensionsTransformer imageDimensionsTransformer;
    private SequenceTransformer sequenceTransformer;
    private ParentFolderTransformer parentFolderTransformer;
    private TruncateTransformer truncateTransformer;
    private ExtensionChangeTransformer extensionChangeTransformer;

    @BeforeAll
    void setUp() {
        fileMapper = mock(ThreadAwareFileMapper.class);
        duplicateResolver = mock(DuplicateNameResolver.class);
        renameExecutor = mock(RenameExecutionService.class);

        // Create mock transformers
        addTextTransformer = mock(AddTextTransformer.class);
        removeTextTransformer = mock(RemoveTextTransformer.class);
        replaceTextTransformer = mock(ReplaceTextTransformer.class);
        caseChangeTransformer = mock(CaseChangeTransformer.class);
        dateTimeTransformer = mock(DateTimeTransformer.class);
        imageDimensionsTransformer = mock(ImageDimensionsTransformer.class);
        sequenceTransformer = mock(SequenceTransformer.class);
        parentFolderTransformer = mock(ParentFolderTransformer.class);
        truncateTransformer = mock(TruncateTransformer.class);
        extensionChangeTransformer = mock(ExtensionChangeTransformer.class);

        orchestrator = new FileRenameOrchestratorImpl(
                fileMapper,
                duplicateResolver,
                renameExecutor,
                addTextTransformer,
                removeTextTransformer,
                replaceTextTransformer,
                caseChangeTransformer,
                dateTimeTransformer,
                imageDimensionsTransformer,
                sequenceTransformer,
                parentFolderTransformer,
                truncateTransformer,
                extensionChangeTransformer
        );
    }

    @BeforeEach
    void resetMocks() {
        reset(fileMapper, duplicateResolver, renameExecutor,
                addTextTransformer, removeTextTransformer, replaceTextTransformer,
                caseChangeTransformer, dateTimeTransformer, imageDimensionsTransformer,
                sequenceTransformer, parentFolderTransformer, truncateTransformer,
                extensionChangeTransformer);
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private File createMockFile(String name) {
        return new File("/test/" + name);
    }

    private FileModel createFileModel(String name, String extension) {
        return FileModel.builder()
                .withFile(createMockFile(name + "." + extension))
                .withIsFile(true)
                .withFileSize(1024L)
                .withName(name)
                .withExtension(extension)
                .withAbsolutePath("/test/" + name + "." + extension)
                .withCreationDate(LocalDateTime.now().minusDays(1))
                .withModificationDate(LocalDateTime.now())
                .withDetectedMimeType("text/plain")
                .withDetectedExtensions(Collections.emptySet())
                .withCategory(Category.GENERIC)
                .withMetadata(null)
                .build();
    }

    private PreparedFileModel createPreparedFile(FileModel fileModel, String newName,
                                                 boolean hasError, String errorMsg) {
        return PreparedFileModel.builder()
                .withOriginalFile(fileModel)
                .withNewName(newName)
                .withNewExtension(fileModel.getExtension())
                .withHasError(hasError)
                .withErrorMessage(hasError ? errorMsg : null)
                .withTransformationMeta(TransformationMetadata.builder()
                        .withMode(TransformationMode.ADD_TEXT)
                        .withAppliedAt(LocalDateTime.now())
                        .withConfig(Map.of("test", "data"))
                        .build())
                .build();
    }

    private RenameResult createRenameResult(PreparedFileModel preparedFile, RenameStatus status) {
        return RenameResult.builder()
                .withPreparedFile(preparedFile)
                .withStatus(status)
                .withErrorMessage(null)
                .withExecutedAt(LocalDateTime.now())
                .build();
    }

    // ============================================================================
    // A. Complete Flow Tests
    // ============================================================================

    @Test
    void testExecute_CompleteFlow_AddText_Success() {
        // Given
        File file1 = createMockFile("file1.txt");
        File file2 = createMockFile("file2.txt");
        List<File> files = List.of(file1, file2);

        FileModel model1 = createFileModel("file1", "txt");
        FileModel model2 = createFileModel("file2", "txt");

        PreparedFileModel prepared1 = createPreparedFile(model1, "prefix_file1", false, null);
        PreparedFileModel prepared2 = createPreparedFile(model2, "prefix_file2", false, null);

        RenameResult result1 = createRenameResult(prepared1, RenameStatus.SUCCESS);
        RenameResult result2 = createRenameResult(prepared2, RenameStatus.SUCCESS);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("prefix_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // Mock Phase 1: Metadata extraction
        when(fileMapper.mapFrom(file1)).thenReturn(model1);
        when(fileMapper.mapFrom(file2)).thenReturn(model2);

        // Mock Phase 2: Transformation
        when(addTextTransformer.transform(model1, config)).thenReturn(prepared1);
        when(addTextTransformer.transform(model2, config)).thenReturn(prepared2);
        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);

        // Mock Phase 2.5: Deduplication
        when(duplicateResolver.resolve(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock Phase 3: Rename execution
        when(renameExecutor.execute(prepared1)).thenReturn(result1);
        when(renameExecutor.execute(prepared2)).thenReturn(result2);

        // When
        List<RenameResult> results = orchestrator.execute(files, TransformationMode.ADD_TEXT,
                config, null);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(RenameStatus.SUCCESS, results.get(0).getStatus());
        assertEquals(RenameStatus.SUCCESS, results.get(1).getStatus());

        // Verify all phases executed
        verify(fileMapper, times(2)).mapFrom(any());
        verify(addTextTransformer, times(2)).transform(any(), eq(config));
        verify(duplicateResolver, times(1)).resolve(any());
        verify(renameExecutor, times(2)).execute(any());
    }

    @Test
    void testExecute_CompleteFlow_Sequence_Sequential() {
        // Given
        File file1 = createMockFile("file1.txt");
        List<File> files = List.of(file1);

        FileModel model1 = createFileModel("file1", "txt");
        PreparedFileModel prepared1 = createPreparedFile(model1, "file_001", false, null);
        RenameResult result1 = createRenameResult(prepared1, RenameStatus.SUCCESS);

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(3)
                .withSortSource(null)
                .build();

        // Mock Phase 1
        when(fileMapper.mapFrom(file1)).thenReturn(model1);

        // Mock Phase 2 - Sequential execution
        when(sequenceTransformer.requiresSequentialExecution()).thenReturn(true);
        when(sequenceTransformer.transformBatch(any(), eq(config))).thenReturn(List.of(prepared1));

        // Mock Phase 2.5
        when(duplicateResolver.resolve(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock Phase 3
        when(renameExecutor.execute(prepared1)).thenReturn(result1);

        // When
        List<RenameResult> results = orchestrator.execute(files, TransformationMode.NUMBER_FILES,
                config, null);

        // Then
        assertEquals(1, results.size());
        assertEquals(RenameStatus.SUCCESS, results.get(0).getStatus());

        // Verify sequential execution (transformBatch called, not individual transforms)
        verify(sequenceTransformer, times(1)).transformBatch(any(), eq(config));
        verify(sequenceTransformer, never()).transform(any(), any());
    }

    // ============================================================================
    // B. Phase Isolation Tests
    // ============================================================================

    @Test
    void testExecute_Phase1_MetadataExtraction() {
        // Given
        File file1 = createMockFile("file1.txt");
        FileModel model1 = createFileModel("file1", "txt");
        PreparedFileModel prepared1 = createPreparedFile(model1, "new1", false, null);
        RenameResult result1 = createRenameResult(prepared1, RenameStatus.SUCCESS);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // Mock all phases
        when(fileMapper.mapFrom(file1)).thenReturn(model1);
        when(addTextTransformer.transform(model1, config)).thenReturn(prepared1);
        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);
        when(duplicateResolver.resolve(any())).thenAnswer(inv -> inv.getArgument(0));
        when(renameExecutor.execute(prepared1)).thenReturn(result1);

        // When
        orchestrator.execute(List.of(file1), TransformationMode.ADD_TEXT, config, null);

        // Then - Verify Phase 1 called
        verify(fileMapper, times(1)).mapFrom(file1);
    }

    @Test
    void testExecute_Phase2_Transformation_Parallel() {
        // Given
        File file1 = createMockFile("file1.txt");
        FileModel model1 = createFileModel("file1", "txt");
        PreparedFileModel prepared1 = createPreparedFile(model1, "new1", false, null);
        RenameResult result1 = createRenameResult(prepared1, RenameStatus.SUCCESS);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // Mock all phases
        when(fileMapper.mapFrom(file1)).thenReturn(model1);
        when(addTextTransformer.transform(model1, config)).thenReturn(prepared1);
        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);
        when(duplicateResolver.resolve(any())).thenAnswer(inv -> inv.getArgument(0));
        when(renameExecutor.execute(prepared1)).thenReturn(result1);

        // When
        orchestrator.execute(List.of(file1), TransformationMode.ADD_TEXT, config, null);

        // Then - Verify Phase 2 called with parallel execution
        verify(addTextTransformer, times(1)).transform(model1, config);
        verify(addTextTransformer, never()).transformBatch(any(), any());
    }

    @Test
    void testExecute_Phase25_DuplicateResolution() {
        // Given
        File file1 = createMockFile("file1.txt");
        File file2 = createMockFile("file2.txt");
        FileModel model1 = createFileModel("file1", "txt");
        FileModel model2 = createFileModel("file2", "txt");

        // Both transform to same name - duplicates!
        PreparedFileModel prepared1 = createPreparedFile(model1, "duplicate", false, null);
        PreparedFileModel prepared2 = createPreparedFile(model2, "duplicate", false, null);

        // After deduplication
        PreparedFileModel deduplicated1 = createPreparedFile(model1, "duplicate (1)", false, null);
        PreparedFileModel deduplicated2 = createPreparedFile(model2, "duplicate (2)", false, null);

        RenameResult result1 = createRenameResult(deduplicated1, RenameStatus.SUCCESS);
        RenameResult result2 = createRenameResult(deduplicated2, RenameStatus.SUCCESS);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // Mock phases
        when(fileMapper.mapFrom(file1)).thenReturn(model1);
        when(fileMapper.mapFrom(file2)).thenReturn(model2);
        when(addTextTransformer.transform(model1, config)).thenReturn(prepared1);
        when(addTextTransformer.transform(model2, config)).thenReturn(prepared2);
        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);

        // Mock deduplication
        when(duplicateResolver.resolve(any())).thenReturn(List.of(deduplicated1, deduplicated2));

        when(renameExecutor.execute(deduplicated1)).thenReturn(result1);
        when(renameExecutor.execute(deduplicated2)).thenReturn(result2);

        // When
        orchestrator.execute(List.of(file1, file2), TransformationMode.ADD_TEXT, config, null);

        // Then - Verify Phase 2.5 called
        verify(duplicateResolver, times(1)).resolve(any());
    }

    @Test
    void testExecute_Phase3_PhysicalRename() {
        // Given
        File file1 = createMockFile("file1.txt");
        FileModel model1 = createFileModel("file1", "txt");
        PreparedFileModel prepared1 = createPreparedFile(model1, "new1", false, null);
        RenameResult result1 = createRenameResult(prepared1, RenameStatus.SUCCESS);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // Mock phases
        when(fileMapper.mapFrom(file1)).thenReturn(model1);
        when(addTextTransformer.transform(model1, config)).thenReturn(prepared1);
        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);
        when(duplicateResolver.resolve(any())).thenAnswer(inv -> inv.getArgument(0));
        when(renameExecutor.execute(prepared1)).thenReturn(result1);

        // When
        orchestrator.execute(List.of(file1), TransformationMode.ADD_TEXT, config, null);

        // Then - Verify Phase 3 called
        verify(renameExecutor, times(1)).execute(prepared1);
    }

    // ============================================================================
    // C. Sequential Order Tests
    // ============================================================================

    @Test
    void testExecute_AllPhases_ExecuteInSequence() {
        // Given
        File file1 = createMockFile("file1.txt");
        FileModel model1 = createFileModel("file1", "txt");
        PreparedFileModel prepared1 = createPreparedFile(model1, "new1", false, null);
        RenameResult result1 = createRenameResult(prepared1, RenameStatus.SUCCESS);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // Track call order
        AtomicInteger callOrder = new AtomicInteger(0);
        AtomicInteger phase1Order = new AtomicInteger(-1);
        AtomicInteger phase2Order = new AtomicInteger(-1);
        AtomicInteger phase25Order = new AtomicInteger(-1);
        AtomicInteger phase3Order = new AtomicInteger(-1);

        when(fileMapper.mapFrom(file1)).thenAnswer(inv -> {
            phase1Order.set(callOrder.incrementAndGet());
            return model1;
        });

        when(addTextTransformer.transform(model1, config)).thenAnswer(inv -> {
            phase2Order.set(callOrder.incrementAndGet());
            return prepared1;
        });
        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);

        when(duplicateResolver.resolve(any())).thenAnswer(inv -> {
            phase25Order.set(callOrder.incrementAndGet());
            return inv.getArgument(0);
        });

        when(renameExecutor.execute(prepared1)).thenAnswer(inv -> {
            phase3Order.set(callOrder.incrementAndGet());
            return result1;
        });

        // When
        orchestrator.execute(List.of(file1), TransformationMode.ADD_TEXT, config, null);

        // Then - Verify execution order
        assertTrue(phase1Order.get() < phase2Order.get(), "Phase 1 should execute before Phase 2");
        assertTrue(phase2Order.get() < phase25Order.get(), "Phase 2 should execute before Phase 2.5");
        assertTrue(phase25Order.get() < phase3Order.get(), "Phase 2.5 should execute before Phase 3");
    }

    // ============================================================================
    // D. Parallel vs Sequential Execution Tests
    // ============================================================================

    @Test
    void testExecute_ParallelExecution_AddTextMode() {
        // Given
        File file1 = createMockFile("file1.txt");
        FileModel model1 = createFileModel("file1", "txt");
        PreparedFileModel prepared1 = createPreparedFile(model1, "new1", false, null);
        RenameResult result1 = createRenameResult(prepared1, RenameStatus.SUCCESS);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        when(fileMapper.mapFrom(file1)).thenReturn(model1);
        when(addTextTransformer.transform(model1, config)).thenReturn(prepared1);
        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);
        when(duplicateResolver.resolve(any())).thenAnswer(inv -> inv.getArgument(0));
        when(renameExecutor.execute(prepared1)).thenReturn(result1);

        // When
        orchestrator.execute(List.of(file1), TransformationMode.ADD_TEXT, config, null);

        // Then - Parallel mode: transform() called, not transformBatch()
        verify(addTextTransformer, times(1)).transform(any(), any());
        verify(addTextTransformer, never()).transformBatch(any(), any());
    }

    @Test
    void testExecute_SequentialExecution_SequenceMode() {
        // Given
        File file1 = createMockFile("file1.txt");
        FileModel model1 = createFileModel("file1", "txt");
        PreparedFileModel prepared1 = createPreparedFile(model1, "file_001", false, null);
        RenameResult result1 = createRenameResult(prepared1, RenameStatus.SUCCESS);

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(3)
                .withSortSource(null)
                .build();

        when(fileMapper.mapFrom(file1)).thenReturn(model1);
        when(sequenceTransformer.requiresSequentialExecution()).thenReturn(true);
        when(sequenceTransformer.transformBatch(any(), eq(config))).thenReturn(List.of(prepared1));
        when(duplicateResolver.resolve(any())).thenAnswer(inv -> inv.getArgument(0));
        when(renameExecutor.execute(prepared1)).thenReturn(result1);

        // When
        orchestrator.execute(List.of(file1), TransformationMode.NUMBER_FILES, config, null);

        // Then - Sequential mode: transformBatch() called, not transform()
        verify(sequenceTransformer, times(1)).transformBatch(any(), any());
        verify(sequenceTransformer, never()).transform(any(), any());
    }

    // ============================================================================
    // E. Error Propagation Tests
    // ============================================================================

    @Test
    void testExecute_ErrorInPhase1_ContinuesWithRemainingFiles() {
        // Given
        File file1 = createMockFile("file1.txt");
        File file2 = createMockFile("file2.txt");

        FileModel model2 = createFileModel("file2", "txt");
        PreparedFileModel prepared2 = createPreparedFile(model2, "new2", false, null);
        RenameResult result2 = createRenameResult(prepared2, RenameStatus.SUCCESS);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // Mock Phase 1 - file1 fails, file2 succeeds
        when(fileMapper.mapFrom(file1)).thenThrow(new RuntimeException("Extraction error"));
        when(fileMapper.mapFrom(file2)).thenReturn(model2);

        // When Phase 1 fails, the orchestrator creates an error FileModel (isFile=false) for file1.
        // Stub Phase 2 + 3 for that error model so the pipeline doesn't NPE in the depth-sort.
        when(addTextTransformer.transform(argThat(fm -> fm != null && !fm.isFile()), eq(config)))
                .thenAnswer(inv -> {
                    FileModel errFm = inv.getArgument(0);
                    return PreparedFileModel.builder()
                            .withOriginalFile(errFm)
                            .withNewName(errFm.getName())
                            .withNewExtension(errFm.getExtension())
                            .withHasError(true)
                            .withErrorMessage("Extraction error")
                            .build();
                });
        when(addTextTransformer.transform(model2, config)).thenReturn(prepared2);
        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);
        when(duplicateResolver.resolve(any())).thenAnswer(inv -> inv.getArgument(0));
        when(renameExecutor.execute(argThat(p -> p != null && p.isHasError()))).thenAnswer(inv -> {
            PreparedFileModel p = inv.getArgument(0);
            return RenameResult.builder()
                    .withPreparedFile(p)
                    .withStatus(RenameStatus.ERROR_EXTRACTION)
                    .withErrorMessage(p.getErrorMessage().orElse(null))
                    .withExecutedAt(java.time.LocalDateTime.now())
                    .build();
        });
        when(renameExecutor.execute(prepared2)).thenReturn(result2);

        // When
        List<RenameResult> results = orchestrator.execute(List.of(file1, file2),
                TransformationMode.ADD_TEXT, config, null);

        // Then - file2 should still succeed (file1 error at index 0, file2 success at index 1)
        assertEquals(2, results.size());
        assertEquals(RenameStatus.SUCCESS, results.get(1).getStatus());
    }

    @Test
    void testExecute_ErrorInPhase2_RecordedInPreparedFile() {
        // Given
        File file1 = createMockFile("file1.txt");
        FileModel model1 = createFileModel("file1", "txt");

        // Phase 2 returns error in PreparedFile
        PreparedFileModel preparedWithError = createPreparedFile(model1, "file1", true,
                "Transformation error");
        RenameResult result1 = createRenameResult(preparedWithError, RenameStatus.SKIPPED);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        when(fileMapper.mapFrom(file1)).thenReturn(model1);
        when(addTextTransformer.transform(model1, config)).thenReturn(preparedWithError);
        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);
        when(duplicateResolver.resolve(any())).thenAnswer(inv -> inv.getArgument(0));
        when(renameExecutor.execute(preparedWithError)).thenReturn(result1);

        // When
        List<RenameResult> results = orchestrator.execute(List.of(file1),
                TransformationMode.ADD_TEXT, config, null);

        // Then - Error should be in result
        assertEquals(1, results.size());
        assertEquals(RenameStatus.SKIPPED, results.get(0).getStatus());
        assertTrue(results.get(0).getPreparedFile().isHasError());
    }

    @Test
    void testExecute_ErrorInPhase3_RecordedInResult() {
        // Given
        File file1 = createMockFile("file1.txt");
        FileModel model1 = createFileModel("file1", "txt");
        PreparedFileModel prepared1 = createPreparedFile(model1, "new1", false, null);

        // Phase 3 returns error result
        RenameResult errorResult = RenameResult.builder()
                .withPreparedFile(prepared1)
                .withStatus(RenameStatus.ERROR_EXECUTION)
                .withErrorMessage("File already exists")
                .withExecutedAt(LocalDateTime.now())
                .build();

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        when(fileMapper.mapFrom(file1)).thenReturn(model1);
        when(addTextTransformer.transform(model1, config)).thenReturn(prepared1);
        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);
        when(duplicateResolver.resolve(any())).thenAnswer(inv -> inv.getArgument(0));
        when(renameExecutor.execute(prepared1)).thenReturn(errorResult);

        // When
        List<RenameResult> results = orchestrator.execute(List.of(file1),
                TransformationMode.ADD_TEXT, config, null);

        // Then
        assertEquals(1, results.size());
        assertEquals(RenameStatus.ERROR_EXECUTION, results.get(0).getStatus());
        assertTrue(results.get(0).getErrorMessage().isPresent());
    }

    @Test
    void testExecute_MixedErrors_PipelineContinues() {
        // Given
        File file1 = createMockFile("file1.txt");
        File file2 = createMockFile("file2.txt");
        File file3 = createMockFile("file3.txt");

        FileModel model1 = createFileModel("file1", "txt");
        FileModel model2 = createFileModel("file2", "txt");
        FileModel model3 = createFileModel("file3", "txt");

        PreparedFileModel prepared1 = createPreparedFile(model1, "new1", false, null);
        PreparedFileModel prepared2 = createPreparedFile(model2, "new2", true, "Transform error");
        PreparedFileModel prepared3 = createPreparedFile(model3, "new3", false, null);

        RenameResult result1 = createRenameResult(prepared1, RenameStatus.SUCCESS);
        RenameResult result2 = createRenameResult(prepared2, RenameStatus.SKIPPED);
        RenameResult result3 = createRenameResult(prepared3, RenameStatus.SUCCESS);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        when(fileMapper.mapFrom(file1)).thenReturn(model1);
        when(fileMapper.mapFrom(file2)).thenReturn(model2);
        when(fileMapper.mapFrom(file3)).thenReturn(model3);

        when(addTextTransformer.transform(model1, config)).thenReturn(prepared1);
        when(addTextTransformer.transform(model2, config)).thenReturn(prepared2);
        when(addTextTransformer.transform(model3, config)).thenReturn(prepared3);
        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);

        when(duplicateResolver.resolve(any())).thenAnswer(inv -> inv.getArgument(0));

        when(renameExecutor.execute(prepared1)).thenReturn(result1);
        when(renameExecutor.execute(prepared2)).thenReturn(result2);
        when(renameExecutor.execute(prepared3)).thenReturn(result3);

        // When
        List<RenameResult> results = orchestrator.execute(List.of(file1, file2, file3),
                TransformationMode.ADD_TEXT, config, null);

        // Then
        assertEquals(3, results.size());
        assertEquals(RenameStatus.SUCCESS, results.get(0).getStatus());
        assertEquals(RenameStatus.SKIPPED, results.get(1).getStatus());
        assertEquals(RenameStatus.SUCCESS, results.get(2).getStatus());
    }

    // ============================================================================
    // F. Progress Callback Tests
    // ============================================================================

    @Test
    void testExecute_ProgressCallback_Phase1() {
        // Given
        File file1 = createMockFile("file1.txt");
        FileModel model1 = createFileModel("file1", "txt");
        PreparedFileModel prepared1 = createPreparedFile(model1, "new1", false, null);
        RenameResult result1 = createRenameResult(prepared1, RenameStatus.SUCCESS);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        ProgressCallback progressCallback = mock(ProgressCallback.class);

        when(fileMapper.mapFrom(file1)).thenReturn(model1);
        when(addTextTransformer.transform(model1, config)).thenReturn(prepared1);
        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);
        when(duplicateResolver.resolve(any())).thenAnswer(inv -> inv.getArgument(0));
        when(renameExecutor.execute(prepared1)).thenReturn(result1);

        // When
        orchestrator.execute(List.of(file1), TransformationMode.ADD_TEXT, config, progressCallback);

        // Then - Progress callback should be called
        verify(progressCallback, atLeastOnce()).updateProgress(anyInt(), anyInt());
    }

    @Test
    void testExecute_ProgressCallback_AllPhases() {
        // Given
        File file1 = createMockFile("file1.txt");
        File file2 = createMockFile("file2.txt");

        FileModel model1 = createFileModel("file1", "txt");
        FileModel model2 = createFileModel("file2", "txt");

        PreparedFileModel prepared1 = createPreparedFile(model1, "new1", false, null);
        PreparedFileModel prepared2 = createPreparedFile(model2, "new2", false, null);

        RenameResult result1 = createRenameResult(prepared1, RenameStatus.SUCCESS);
        RenameResult result2 = createRenameResult(prepared2, RenameStatus.SUCCESS);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        ProgressCallback progressCallback = mock(ProgressCallback.class);

        when(fileMapper.mapFrom(file1)).thenReturn(model1);
        when(fileMapper.mapFrom(file2)).thenReturn(model2);
        when(addTextTransformer.transform(model1, config)).thenReturn(prepared1);
        when(addTextTransformer.transform(model2, config)).thenReturn(prepared2);
        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);
        when(duplicateResolver.resolve(any())).thenAnswer(inv -> inv.getArgument(0));
        when(renameExecutor.execute(prepared1)).thenReturn(result1);
        when(renameExecutor.execute(prepared2)).thenReturn(result2);

        // When
        orchestrator.execute(List.of(file1, file2), TransformationMode.ADD_TEXT, config,
                progressCallback);

        // Then - Should have progress updates for each phase
        ArgumentCaptor<Integer> currentCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> maxCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(progressCallback, atLeast(6)).updateProgress(currentCaptor.capture(),
                maxCaptor.capture());

        // Verify progress values are reasonable
        List<Integer> currents = currentCaptor.getAllValues();
        List<Integer> maxes = maxCaptor.getAllValues();

        for (int i = 0; i < currents.size(); i++) {
            assertTrue(currents.get(i) >= 0, "Current should be >= 0");
            assertTrue(maxes.get(i) >= 0, "Max should be >= 0");
        }
    }

    @Test
    void testExecute_ProgressCallback_NullCallback() {
        // Given - Null callback should not cause errors
        File file1 = createMockFile("file1.txt");
        FileModel model1 = createFileModel("file1", "txt");
        PreparedFileModel prepared1 = createPreparedFile(model1, "new1", false, null);
        RenameResult result1 = createRenameResult(prepared1, RenameStatus.SUCCESS);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        when(fileMapper.mapFrom(file1)).thenReturn(model1);
        when(addTextTransformer.transform(model1, config)).thenReturn(prepared1);
        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);
        when(duplicateResolver.resolve(any())).thenAnswer(inv -> inv.getArgument(0));
        when(renameExecutor.execute(prepared1)).thenReturn(result1);

        // When
        List<RenameResult> results = orchestrator.execute(List.of(file1),
                TransformationMode.ADD_TEXT, config, null);

        // Then - Should complete successfully
        assertEquals(1, results.size());
        assertEquals(RenameStatus.SUCCESS, results.get(0).getStatus());
    }

    // ============================================================================
    // G. Async Execution Tests
    // ============================================================================

    @Test
    void testExecuteAsync_ReturnsCompletableFuture() {
        // Given
        File file1 = createMockFile("file1.txt");
        FileModel model1 = createFileModel("file1", "txt");
        PreparedFileModel prepared1 = createPreparedFile(model1, "new1", false, null);
        RenameResult result1 = createRenameResult(prepared1, RenameStatus.SUCCESS);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        when(fileMapper.mapFrom(file1)).thenReturn(model1);
        when(addTextTransformer.transform(model1, config)).thenReturn(prepared1);
        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);
        when(duplicateResolver.resolve(any())).thenAnswer(inv -> inv.getArgument(0));
        when(renameExecutor.execute(prepared1)).thenReturn(result1);

        // When
        CompletableFuture<List<RenameResult>> future = orchestrator.executeAsync(
                List.of(file1), TransformationMode.ADD_TEXT, config, null);

        // Then
        assertNotNull(future);
        assertFalse(future.isDone() && future.isCancelled());

        // Wait for completion
        List<RenameResult> results = future.join();
        assertEquals(1, results.size());
        assertEquals(RenameStatus.SUCCESS, results.get(0).getStatus());
    }

    // ============================================================================
    // H. Edge Cases and Error Handling
    // ============================================================================

    @Test
    void testExecute_UnknownMode_ReturnsErrorResults() {
        // Given - Mode not in registry
        File file1 = createMockFile("file1.txt");
        FileModel model1 = createFileModel("file1", "txt");

        when(fileMapper.mapFrom(file1)).thenReturn(model1);

        // When - The orchestrator catches exceptions and returns error results
        List<RenameResult> results = orchestrator.execute(List.of(file1),
                TransformationMode.REMOVE_TEXT, null, null);

        // Then - Should return error results
        assertEquals(1, results.size());
        assertEquals(RenameStatus.ERROR_EXTRACTION, results.get(0).getStatus());
        assertTrue(results.get(0).getErrorMessage().isPresent());
        assertTrue(results.get(0).getErrorMessage().get().contains("Pipeline error"));
    }

    @Test
    void testExecute_EmptyFileList() {
        // Given
        List<File> emptyList = Collections.emptyList();

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        List<RenameResult> results = orchestrator.execute(emptyList,
                TransformationMode.ADD_TEXT, config, null);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testExecute_NullConfig_ReturnsErrorResult() {
        // Given
        File file1 = createMockFile("file1.txt");
        FileModel model1 = createFileModel("file1", "txt");

        when(fileMapper.mapFrom(file1)).thenReturn(model1);

        // When - Null config should be rejected by pattern matching
        List<RenameResult> results = orchestrator.execute(
                List.of(file1), TransformationMode.ADD_TEXT, null, null);

        // Then - Should return error result
        assertNotNull(results);
        assertEquals(1, results.size());
        RenameResult result = results.get(0);
        assertEquals(RenameStatus.ERROR_EXTRACTION, result.getStatus());
        assertTrue(result.getErrorMessage().isPresent(),
                "Error message should be present");
        assertTrue(result.getErrorMessage().get().contains("ADD_TEXT requires AddTextConfig"),
                "Error message should indicate config type mismatch");
        assertTrue(result.getErrorMessage().get().contains("null"),
                "Error message should mention null config");
    }

    @Test
    void testExecute_PipelineException_ReturnsErrorResults() {
        // Given
        File file1 = createMockFile("file1.txt");

        // Mock catastrophic failure in Phase 1
        when(fileMapper.mapFrom(file1)).thenThrow(new OutOfMemoryError("Test OOM"));

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When - Should catch exception and return error results
        List<RenameResult> results = orchestrator.execute(List.of(file1),
                TransformationMode.ADD_TEXT, config, null);

        // Then - Should return error result for the file
        assertEquals(1, results.size());
        assertEquals(RenameStatus.ERROR_EXTRACTION, results.get(0).getStatus());
        assertTrue(results.get(0).getErrorMessage().isPresent());
        assertTrue(results.get(0).getErrorMessage().get().contains("Pipeline error"));
    }

    @Test
    void testExecute_MultipleFiles_VerifyAllProcessed() {
        // Given - 5 files
        List<File> files = List.of(
                createMockFile("file1.txt"),
                createMockFile("file2.txt"),
                createMockFile("file3.txt"),
                createMockFile("file4.txt"),
                createMockFile("file5.txt")
        );

        List<FileModel> models = files.stream()
                .map(f -> createFileModel(f.getName().replace(".txt", ""), "txt"))
                .toList();

        List<PreparedFileModel> prepared = models.stream()
                .map(m -> createPreparedFile(m, "new_" + m.getName(), false, null))
                .toList();

        List<RenameResult> results = prepared.stream()
                .map(p -> createRenameResult(p, RenameStatus.SUCCESS))
                .toList();

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // Mock all
        for (int i = 0; i < files.size(); i++) {
            when(fileMapper.mapFrom(files.get(i))).thenReturn(models.get(i));
            when(addTextTransformer.transform(models.get(i), config)).thenReturn(prepared.get(i));
            when(renameExecutor.execute(prepared.get(i))).thenReturn(results.get(i));
        }

        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);
        when(duplicateResolver.resolve(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        List<RenameResult> actualResults = orchestrator.execute(files,
                TransformationMode.ADD_TEXT, config, null);

        // Then
        assertEquals(5, actualResults.size());
        actualResults.forEach(r -> assertEquals(RenameStatus.SUCCESS, r.getStatus()));
    }

    // ============================================================================
    // I. Config Type Validation Tests (NEW - Added for v2 pattern matching)
    // ============================================================================

    @Test
    void testExecute_WrongConfigType_ReturnsErrorResult() {
        // Given
        File file1 = createMockFile("file1.txt");
        FileModel model1 = createFileModel("file1", "txt");

        // Wrong config type - passing SequenceConfig for ADD_TEXT mode
        SequenceConfig wrongConfig = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(3)
                .withSortSource(null)
                .build();

        when(fileMapper.mapFrom(file1)).thenReturn(model1);

        // When - Wrong config type should be rejected by pattern matching
        List<RenameResult> results = orchestrator.execute(
                List.of(file1), TransformationMode.ADD_TEXT, wrongConfig, null);

        // Then - Should return error result
        assertNotNull(results);
        assertEquals(1, results.size());
        RenameResult result = results.get(0);
        assertEquals(RenameStatus.ERROR_EXTRACTION, result.getStatus());
        assertTrue(result.getErrorMessage().isPresent(),
                "Error message should be present");
        assertTrue(result.getErrorMessage().get().contains("ADD_TEXT requires AddTextConfig"),
                "Error message should indicate expected config type");
        assertTrue(result.getErrorMessage().get().contains("SequenceConfig"),
                "Error message should mention wrong config type");
    }

    // ============================================================================
    // J. Ordering Tests — Phase 3 Depth-Sort
    // ============================================================================

    @Test
    void execute_withParentAndChildInList_childRenamedBeforeParent() {
        // Parent dir and its child file both appear in the rename list.
        // Child has deeper path (more name components) → must be executed first.
        File parentFile = new File("/projects/myfolder");
        File childFile = new File("/projects/myfolder/notes.txt");

        FileModel parentModel = buildDirModel(parentFile, "myfolder");
        FileModel childModel = FileModel.builder()
                .withFile(childFile)
                .withIsFile(true)
                .withFileSize(100L)
                .withName("notes")
                .withExtension("txt")
                .withAbsolutePath(childFile.getAbsolutePath())
                .withCreationDate(LocalDateTime.now())
                .withModificationDate(LocalDateTime.now())
                .withDetectedMimeType("text/plain")
                .withDetectedExtensions(Collections.emptySet())
                .withCategory(Category.GENERIC)
                .withMetadata(null)
                .build();

        PreparedFileModel preparedParent = createPreparedFile(parentModel, "renamed_folder", false, null);
        PreparedFileModel preparedChild = createPreparedFile(childModel, "renamed_notes", false, null);

        RenameResult resultParent = createRenameResult(preparedParent, RenameStatus.SUCCESS);
        RenameResult resultChild = createRenameResult(preparedChild, RenameStatus.SUCCESS);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("renamed_").withPosition(ItemPosition.BEGIN).build();

        when(fileMapper.mapFrom(parentFile)).thenReturn(parentModel);
        when(fileMapper.mapFrom(childFile)).thenReturn(childModel);
        when(addTextTransformer.transform(parentModel, config)).thenReturn(preparedParent);
        when(addTextTransformer.transform(childModel, config)).thenReturn(preparedChild);
        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);
        when(duplicateResolver.resolve(any())).thenAnswer(inv -> inv.getArgument(0));
        when(renameExecutor.execute(preparedParent)).thenReturn(resultParent);
        when(renameExecutor.execute(preparedChild)).thenReturn(resultChild);

        // Pass parent first — sort must reorder to child-before-parent
        List<RenameResult> results = orchestrator.execute(
                List.of(parentFile, childFile), TransformationMode.ADD_TEXT, config, null);

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(r -> r.getStatus() == RenameStatus.SUCCESS));

        // Verify child was renamed before parent
        InOrder order = inOrder(renameExecutor);
        order.verify(renameExecutor).execute(preparedChild);
        order.verify(renameExecutor).execute(preparedParent);
    }

    @Test
    void execute_withDeeplyNestedFolders_allRenameSucceed() {
        // Three levels: /a  →  /a/b  →  /a/b/c
        // Sort must produce: c first, then b, then a
        File aFile = new File("/a");
        File bFile = new File("/a/b");
        File cFile = new File("/a/b/c");

        FileModel aModel = buildDirModel(aFile, "a");
        FileModel bModel = buildDirModel(bFile, "b");
        FileModel cModel = buildDirModel(cFile, "c");

        PreparedFileModel prepA = createPreparedFile(aModel, "A", false, null);
        PreparedFileModel prepB = createPreparedFile(bModel, "B", false, null);
        PreparedFileModel prepC = createPreparedFile(cModel, "C", false, null);

        RenameResult resA = createRenameResult(prepA, RenameStatus.SUCCESS);
        RenameResult resB = createRenameResult(prepB, RenameStatus.SUCCESS);
        RenameResult resC = createRenameResult(prepC, RenameStatus.SUCCESS);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("X").withPosition(ItemPosition.BEGIN).build();

        when(fileMapper.mapFrom(aFile)).thenReturn(aModel);
        when(fileMapper.mapFrom(bFile)).thenReturn(bModel);
        when(fileMapper.mapFrom(cFile)).thenReturn(cModel);
        when(addTextTransformer.transform(aModel, config)).thenReturn(prepA);
        when(addTextTransformer.transform(bModel, config)).thenReturn(prepB);
        when(addTextTransformer.transform(cModel, config)).thenReturn(prepC);
        when(addTextTransformer.requiresSequentialExecution()).thenReturn(false);
        when(duplicateResolver.resolve(any())).thenAnswer(inv -> inv.getArgument(0));
        when(renameExecutor.execute(prepA)).thenReturn(resA);
        when(renameExecutor.execute(prepB)).thenReturn(resB);
        when(renameExecutor.execute(prepC)).thenReturn(resC);

        // Pass in shallow-first order to ensure sort does the reordering
        List<RenameResult> results = orchestrator.execute(
                List.of(aFile, bFile, cFile), TransformationMode.ADD_TEXT, config, null);

        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(r -> r.getStatus() == RenameStatus.SUCCESS));

        // Deepest (c) first, then b, then a
        InOrder order = inOrder(renameExecutor);
        order.verify(renameExecutor).execute(prepC);
        order.verify(renameExecutor).execute(prepB);
        order.verify(renameExecutor).execute(prepA);
    }

    private FileModel buildDirModel(File f, String name) {
        return FileModel.builder()
                .withFile(f)
                .withIsFile(false)
                .withFileSize(0L)
                .withName(name)
                .withExtension("")
                .withAbsolutePath(f.getAbsolutePath())
                .withCreationDate(LocalDateTime.now())
                .withModificationDate(LocalDateTime.now())
                .withDetectedMimeType("")
                .withDetectedExtensions(Collections.emptySet())
                .withCategory(Category.GENERIC)
                .withMetadata(null)
                .build();
    }
}
