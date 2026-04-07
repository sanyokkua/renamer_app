package ua.renamer.app.core.v2.service.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.enums.SortSource;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.AddTextConfig;
import ua.renamer.app.api.model.config.SequenceConfig;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for performance benchmarking of the file rename pipeline.
 * Tests parallel execution with virtual threads on various batch sizes.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PerformanceIntegrationTest extends BaseTransformationIntegrationTest {

    private static final long MAX_TIME_100_FILES_MS = 5000;    // 5 seconds
    private static final long MAX_TIME_1000_FILES_MS = 30000;  // 30 seconds

    // ==================== SMALL BATCH PERFORMANCE ====================

    @Test
    void testPerformance_10Files_ShouldBeInstant() throws IOException {
        List<File> files = createTestFiles("file", "txt", 10);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        Instant start = Instant.now();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                createTrackingCallback()
        );

        Instant end = Instant.now();
        long durationMs = Duration.between(start, end).toMillis();

        log.info("Renamed 10 files in {} ms", durationMs);

        assertEquals(10, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Should complete very quickly
        assertTrue(durationMs < 1000, "10 files should complete in under 1 second");
    }

    @Test
    void testPerformance_50Files_QuickExecution() throws IOException {
        List<File> files = createTestFiles("item", "dat", 50);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("processed_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        Instant start = Instant.now();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                createTrackingCallback()
        );

        Instant end = Instant.now();
        long durationMs = Duration.between(start, end).toMillis();

        log.info("Renamed 50 files in {} ms", durationMs);

        assertEquals(50, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Should complete reasonably quickly with virtual threads
        assertTrue(durationMs < 3000, "50 files should complete in under 3 seconds");
    }

    // ==================== MEDIUM BATCH PERFORMANCE ====================

    @Test
    void testPerformance_100Files_WithMetrics() throws IOException {
        log.info("Creating 100 test files...");
        List<File> files = createTestFiles("data", "bin", 100);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("archive_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        log.info("Starting rename operation...");
        Instant start = Instant.now();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                createTrackingCallback()
        );

        Instant end = Instant.now();
        long durationMs = Duration.between(start, end).toMillis();

        log.info("=== Performance Metrics (100 files) ===");
        log.info("Total time: {} ms", durationMs);
        log.info("Average per file: {} ms", durationMs / 100.0);
        log.info("Throughput: {} files/sec", (100.0 / durationMs) * 1000);

        assertEquals(100, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Verify performance threshold
        assertTrue(durationMs < MAX_TIME_100_FILES_MS,
                String.format("100 files should complete in under %d ms, took %d ms",
                        MAX_TIME_100_FILES_MS, durationMs));
    }

    @Test
    void testPerformance_100Files_SequenceMode() throws IOException {
        // Sequence mode is sequential, so should be slower but still reasonable
        List<File> files = createTestFiles("photo", "jpg", 100);

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(3)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        log.info("Starting sequence operation on 100 files...");
        Instant start = Instant.now();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.NUMBER_FILES,
                config,
                createTrackingCallback()
        );

        Instant end = Instant.now();
        long durationMs = Duration.between(start, end).toMillis();

        log.info("Sequence rename of 100 files completed in {} ms", durationMs);
        log.info("Average per file: {} ms", durationMs / 100.0);

        assertEquals(100, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Sequential mode will be slower, but should still complete in reasonable time
        assertTrue(durationMs < MAX_TIME_100_FILES_MS,
                "Sequence mode with 100 files should complete reasonably quickly");
    }

    // ==================== LARGE BATCH PERFORMANCE ====================

    @Test
    void testPerformance_1000Files_StressTest() throws IOException {
        log.info("Creating 1000 test files for stress test...");
        List<File> files = createTestFiles("stress", "dat", 1000);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("STRESS_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        log.info("Starting stress test with 1000 files...");
        Instant start = Instant.now();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                createTrackingCallback()
        );

        Instant end = Instant.now();
        long durationMs = Duration.between(start, end).toMillis();

        log.info("=== Performance Metrics (1000 files) ===");
        log.info("Total time: {} ms ({} sec)", durationMs, durationMs / 1000.0);
        log.info("Average per file: {} ms", durationMs / 1000.0);
        log.info("Throughput: {} files/sec", (1000.0 / durationMs) * 1000);

        assertEquals(1000, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Should complete within threshold (benefits from virtual threads)
        assertTrue(durationMs < MAX_TIME_1000_FILES_MS,
                String.format("1000 files should complete in under %d ms, took %d ms",
                        MAX_TIME_1000_FILES_MS, durationMs));

        // Verify all files exist
        for (int i = 1; i <= 1000; i++) {
            String expectedName = "STRESS_stress_" + i + ".dat";
            assertFileExists(expectedName);
        }

        log.info("Stress test completed successfully!");
    }

    // ==================== ASYNC EXECUTION TESTS ====================

    @Test
    void testPerformance_AsyncExecution_100Files() throws Exception {
        List<File> files = createTestFiles("async", "txt", 100);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("ASYNC_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        log.info("Starting async execution...");
        Instant start = Instant.now();

        // Use async execution
        var future = orchestrator.executeAsync(
                files,
                TransformationMode.ADD_TEXT,
                config,
                createTrackingCallback()
        );

        // Wait for completion
        List<RenameResult> results = future.get();

        Instant end = Instant.now();
        long durationMs = Duration.between(start, end).toMillis();

        log.info("Async execution completed in {} ms", durationMs);

        assertEquals(100, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));
    }

    @Test
    void testPerformance_AsyncExecution_DoesNotBlock() throws Exception {
        List<File> files = createTestFiles("nonblock", "dat", 50);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("NB_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // Start async execution
        var future = orchestrator.executeAsync(
                files,
                TransformationMode.NUMBER_FILES,
                config,
                null
        );

        // Should return immediately (future not completed yet)
        assertFalse(future.isDone(), "Future should not be done immediately");

        // Wait for completion
        List<RenameResult> results = future.get();

        // Now should be done
        assertTrue(future.isDone());
        assertEquals(50, results.size());
    }

    // ==================== PROGRESS TRACKING PERFORMANCE ====================

    @Test
    void testPerformance_ProgressCallback_NoSignificantOverhead() throws IOException {
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("CB_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // Warm-up pass: execute once (no callback, discarded) so that JVM class-loading
        // and JIT compilation do not inflate the first timed measurement.
        List<File> warmupFiles = createTestFiles("warmup", "txt", 100);
        List<RenameResult> warmupResults = orchestrator.execute(
                warmupFiles, TransformationMode.ADD_TEXT, config, null);
        warmupResults.forEach(r -> tempDir.resolve(r.getNewFileName()).toFile().delete());

        // BASELINE: timed run without callback on a warm JVM
        List<File> filesBaseline = createTestFiles("progress_base", "txt", 100);
        Instant start2 = Instant.now();
        List<RenameResult> resultsBaseline = orchestrator.execute(
                filesBaseline, TransformationMode.ADD_TEXT, config, null);
        Instant end2 = Instant.now();
        long withoutCallbackNs = Duration.between(start2, end2).toNanos();
        resultsBaseline.forEach(r -> tempDir.resolve(r.getNewFileName()).toFile().delete());

        // MEASURED: timed run with callback on the same warm JVM
        List<File> filesWithCb = createTestFiles("progress_cb", "txt", 100);
        Instant start1 = Instant.now();
        orchestrator.execute(filesWithCb, TransformationMode.ADD_TEXT, config, createTrackingCallback());
        Instant end1 = Instant.now();
        long withCallbackNs = Duration.between(start1, end1).toNanos();

        log.info("With callback: {} ns, Without callback: {} ns",
                withCallbackNs, withoutCallbackNs);

        // Overhead: how much MORE the callback run takes relative to the no-callback baseline.
        // Both runs are on a warm JVM so JIT start-up bias is eliminated.
        double overhead;
        if (withoutCallbackNs == 0) {
            overhead = 0.0; // both effectively instant — no measurable overhead
        } else {
            overhead = Math.abs(withCallbackNs - withoutCallbackNs) / (double) withoutCallbackNs;
        }

        log.info("Overhead: {}%", overhead * 100);

        // Allow up to 50% overhead for callback (very generous)
        assertTrue(overhead < 0.5, "Progress callback overhead: " + (overhead * 100) + "%");
    }

    // ==================== COMPARISON TESTS ====================

    @Test
    void testPerformance_CompareTransformationModes() throws IOException {
        // Compare AddText (parallel) vs Sequence (sequential)
        int fileCount = 100;

        // Test 1: AddText (parallel)
        List<File> files1 = createTestFiles("parallel", "txt", fileCount);
        AddTextConfig addConfig = AddTextConfig.builder()
                .withTextToAdd("PAR_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        Instant start1 = Instant.now();
        List<RenameResult> results1 = orchestrator.execute(
                files1,
                TransformationMode.ADD_TEXT,
                addConfig,
                null
        );
        Instant end1 = Instant.now();
        long parallelMs = Duration.between(start1, end1).toMillis();

        // Clean up
        results1.forEach(r -> tempDir.resolve(r.getNewFileName()).toFile().delete());

        // Test 2: Sequence (sequential)
        List<File> files2 = createTestFiles("sequential", "txt", fileCount);
        SequenceConfig seqConfig = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(3)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        Instant start2 = Instant.now();
        List<RenameResult> results2 = orchestrator.execute(
                files2,
                TransformationMode.NUMBER_FILES,
                seqConfig,
                null
        );
        Instant end2 = Instant.now();
        long sequentialMs = Duration.between(start2, end2).toMillis();

        log.info("=== Mode Comparison ({} files) ===", fileCount);
        log.info("Parallel (AddText): {} ms", parallelMs);
        log.info("Sequential (Sequence): {} ms", sequentialMs);
        log.info("Ratio: {}", (double) sequentialMs / parallelMs);

        // Both should complete successfully
        assertEquals(fileCount, results1.size());
        assertEquals(fileCount, results2.size());

        // Parallel should be faster or similar (sequence has sorting overhead)
        // But with virtual threads, difference may be minimal
        log.info("Performance comparison completed");
    }

    // ==================== MEMORY EFFICIENCY ====================

    @Test
    void testPerformance_MemoryEfficiency_NoLeak() throws IOException {
        // Run multiple batches to ensure no memory leaks
        int iterations = 10;
        int filesPerIteration = 50;

        for (int i = 0; i < iterations; i++) {
            log.info("Memory test iteration {}/{}", i + 1, iterations);

            List<File> files = createTestFiles("mem_" + i + "_", "dat", filesPerIteration);

            AddTextConfig config = AddTextConfig.builder()
                    .withTextToAdd("TEST_")
                    .withPosition(ItemPosition.BEGIN)
                    .build();

            List<RenameResult> results = orchestrator.execute(
                    files,
                    TransformationMode.ADD_TEXT,
                    config,
                    null
            );

            assertEquals(filesPerIteration, results.size());
            assertTrue(results.stream().allMatch(RenameResult::isSuccess));

            // Clean up immediately to free memory
            results.forEach(r -> {
                File file = tempDir.resolve(r.getNewFileName()).toFile();
                file.delete();
            });
        }

        log.info("Memory efficiency test completed: {} iterations", iterations);
        // If we get here without OutOfMemoryError, the test passed
    }
}
