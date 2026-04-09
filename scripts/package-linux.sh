#!/usr/bin/env bash
# Build Linux executable and .deb installer
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/jpackage-common.sh"

ICON="$PROJECT_ROOT/icon.png"

# Check for fakeroot (required for .deb packaging)
if ! command -v fakeroot &>/dev/null; then
    echo "WARNING: fakeroot not found — .deb packaging will be skipped."
    echo "         Install with: sudo apt-get install fakeroot"
    SKIP_DEB=true
else
    SKIP_DEB=false
fi

BASE_ARGS=(
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

# Flags valid only for installer types (deb), not app-image
DEB_ARGS=(
    --linux-package-deps false
    --linux-shortcut
    --linux-menu-group "Utility"
    --linux-deb-maintainer "renamer-app@example.com"
    --linux-app-category "utils"
)

# Build app-image (raw executable directory)
echo "=== Building Linux app-image ==="
rm -rf "$OUTPUT_DIR/$APP_NAME"
jpackage "${BASE_ARGS[@]}" \
    --type app-image

echo "App image created: $OUTPUT_DIR/$APP_NAME/"

# Build .deb package
if [ "$SKIP_DEB" = true ]; then
    echo "=== Skipping .deb package (fakeroot not available) ==="
else
    echo "=== Building .deb package ==="
    rm -f "$OUTPUT_DIR/"*.deb
    jpackage "${BASE_ARGS[@]}" "${DEB_ARGS[@]}" \
        --type deb

    echo "=== Linux packaging complete ==="
    ls -la "$OUTPUT_DIR/"*.deb 2>/dev/null || true
fi
