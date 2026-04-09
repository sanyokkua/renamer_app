#!/usr/bin/env bash
# Build macOS .app bundle and .dmg installer
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/jpackage-common.sh"

ICON="$PROJECT_ROOT/icon.icns"

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
)

# Build app-image (raw .app bundle)
echo "=== Building macOS app-image ==="
rm -rf "$OUTPUT_DIR/$APP_NAME.app"
jpackage "${COMMON_ARGS[@]}" \
    --type app-image

echo "App image created: $OUTPUT_DIR/$APP_NAME.app"

# Build .dmg installer
echo "=== Building macOS .dmg ==="
rm -f "$OUTPUT_DIR/${APP_NAME}-${APP_VERSION}.dmg"
jpackage "${COMMON_ARGS[@]}" \
    --type dmg

echo "DMG created in: $OUTPUT_DIR/"
echo "=== macOS packaging complete ==="
