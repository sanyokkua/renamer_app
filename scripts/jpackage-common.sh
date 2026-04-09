#!/usr/bin/env bash
# Common jpackage configuration
# Source this file from platform-specific scripts

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
UI_TARGET="$PROJECT_ROOT/app/ui/target"
INPUT_DIR="$UI_TARGET/libs"
OUTPUT_DIR="$PROJECT_ROOT/dist"

# App metadata
APP_NAME="Renamer"
APP_VERSION="${APP_VERSION:-$(mvn -f "$PROJECT_ROOT/app/pom.xml" help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null || echo "2.0.0")}"
APP_VENDOR="Renamer App"
APP_DESCRIPTION="Batch file renaming application"
APP_COPYRIGHT="Copyright (c) Renamer App"
MAIN_JAR="$(ls "$INPUT_DIR"/ua.renamer.app.ui-*.jar 2>/dev/null | head -1 | xargs basename 2>/dev/null || true)"
MAIN_CLASS="ua.renamer.app.Launcher"

# jlink options for size reduction
JLINK_OPTIONS="--strip-debug --no-header-files --no-man-pages --compress zip-6"

# Verify prerequisites
if [ ! -d "$INPUT_DIR" ]; then
    echo "ERROR: $INPUT_DIR not found. Run 'cd app && mvn clean package -DskipTests' first."
    exit 1
fi

if [ -z "$MAIN_JAR" ]; then
    echo "ERROR: Could not find ua.renamer.app.ui-*.jar in $INPUT_DIR"
    exit 1
fi

mkdir -p "$OUTPUT_DIR"

echo "App: $APP_NAME v$APP_VERSION"
echo "Main JAR: $MAIN_JAR"
echo "Input dir: $INPUT_DIR"
echo "Output dir: $OUTPUT_DIR"
