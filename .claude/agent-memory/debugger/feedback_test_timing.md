---
name: Use toNanos() for performance-test timing
description: Performance assertions using toMillis() fail on fast machines when both durations round to zero
type: feedback
---

Use `Duration.between(...).toNanos()` rather than `toMillis()` whenever a test
asserts a _relative_ timing overhead. On fast machines (or CI), sub-millisecond
operations round to 0 ms, turning a division into `Infinity` or `NaN`, which
never satisfies `< threshold`.

**Why:** `PerformanceIntegrationTest.testPerformance_ProgressCallback_NoSignificantOverhead`
divided by `Math.min(withCallbackMs, withoutCallbackMs)` which was 0 ms, producing
`Double.POSITIVE_INFINITY`. Bug confirmed 2026-04-01.

**How to apply:**
- Always use `toNanos()` for overhead / ratio computations in tests.
- Use the _baseline_ (without-callback, without-feature) value as the denominator,
  not `Math.min(a, b)` — the baseline is the reference point for relative overhead.
- Guard the zero-baseline case: `if (baseline == 0) overhead = 0.0;` so the test
  passes unconditionally when both runs are effectively instant.
