#!/usr/bin/env bash
# Build Linux executable and .deb installer
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/jpackage-common.sh"

ICON="$PROJECT_ROOT/icon.png"

COMMON_ARGS=(
    --input "$INPUT_DIR"
    --dest "$OUTPUT_DIR"
    --name "$APP_NAME"
    --main-jar "$MAIN_JAR"
    --main-class "$MAIN_CLASS"
    --app-version "$APP_VERSION"
    --vendor "$APP_VENDOR"
    --description "$APP_DESCRIPTION"
    --copyright "$APP_COPYRIGHT"
    --icon "$ICON"
    --java-options "--enable-preview"
    --java-options "-Xmx512m"
    --jlink-options "$JLINK_OPTIONS"
    --linux-shortcut
    --linux-menu-group "Utility"
)

# Build app-image (raw executable directory)
echo "=== Building Linux app-image ==="
rm -rf "$OUTPUT_DIR/$APP_NAME"
jpackage "${COMMON_ARGS[@]}" \
    --type app-image

echo "App image created: $OUTPUT_DIR/$APP_NAME/"

# Build .deb package
echo "=== Building .deb package ==="
rm -f "$OUTPUT_DIR/"*.deb
jpackage "${COMMON_ARGS[@]}" \
    --type deb \
    --linux-deb-maintainer "renamer-app@example.com" \
    --linux-app-category "utils"

echo "=== Linux packaging complete ==="
ls -la "$OUTPUT_DIR/"*.deb 2>/dev/null || true
