#!/bin/bash
# Sequential build for Claude Code — follows the linting standards execution order:
# compile → checkstyle → PMD → SpotBugs → test
# Stops at first failure on compile/test. Linters run informational (|| true).
# Run from any directory in the project.
set -e

cd "$(dirname "$0")/../app"
QUIET="-q -B --no-transfer-progress -ff"
LINT="-B --no-transfer-progress"   # no -q: violations are [WARN] level, -q would hide them

echo "=== Step 1: Compile ==="
mvn compile $QUIET
echo "✓ Compilation successful"

echo "=== Step 2: Checkstyle ==="
# Violations appear as [WARN] lines — filter out Maven infrastructure lines
mvn checkstyle:check $LINT -Pcode-quality 2>&1 \
  | grep -E '^\[WARN\]' | grep -v 'Checkstyle violations\|You have 0' | head -60 || true
echo "✓ Checkstyle done"

echo "=== Step 3: PMD ==="
# PMD violations appear as [WARNING] PMD Failure: lines.
mvn pmd:check $LINT -Pcode-quality 2>&1 \
  | grep -E '^\[WARNING\]' | head -60 || true
echo "✓ PMD done"

echo "=== Step 4: SpotBugs ==="
# SpotBugs violations appear as [ERROR] lines.
mvn spotbugs:check $LINT -Pcode-quality 2>&1 \
  | grep -E '^\[ERROR\]' | grep -v 'BUILD\|Spotbugs\|spotbugs:check' | head -60 || true
echo "✓ SpotBugs done"

echo "=== Step 5: Test ==="
mvn test $QUIET -Dai=true
echo "✓ All tests passed"

echo ""
echo "=== Build complete ==="
