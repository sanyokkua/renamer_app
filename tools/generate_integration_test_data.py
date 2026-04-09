# /// script
# dependencies = ["mutagen", "pillow"]
# ///

"""
Generate integration test data for the Renamer App backend integration tests.

Run with: uv run tools/generate_integration_test_data.py

Idempotent — re-running overwrites existing files with identical content.
CI never runs this script; output is committed to git.
"""

import json
import subprocess
import sys
from pathlib import Path

# ---------------------------------------------------------------------------
# Paths
# ---------------------------------------------------------------------------

REPO_ROOT = Path(__file__).parent.parent
OUT_ROOT = REPO_ROOT / "app" / "backend" / "src" / "test" / "resources" / "integration-test-data"


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def run(cmd: list[str], check: bool = True) -> subprocess.CompletedProcess:
    """Run a shell command, streaming output on failure."""
    result = subprocess.run(cmd, capture_output=True, text=True)
    if check and result.returncode != 0:
        print(f"ERROR running: {' '.join(cmd)}", file=sys.stderr)
        print(result.stdout, file=sys.stderr)
        print(result.stderr, file=sys.stderr)
        sys.exit(1)
    return result


def ffmpeg(args: list[str], output: Path) -> None:
    """Run ffmpeg, overwriting output if it exists."""
    run(["ffmpeg", "-y"] + args + [str(output)])


def exiftool(args: list[str], target: Path) -> None:
    run(["exiftool"] + args + [str(target)])


def write_text_file(path: Path, content: str = "") -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content or f"Integration test file: {path.name}\n", encoding="utf-8")


# ---------------------------------------------------------------------------
# Directory creation
# ---------------------------------------------------------------------------

def create_directories() -> None:
    for d in [
        OUT_ROOT / "flat",
        OUT_ROOT / "media",
        OUT_ROOT / "nested" / "sublevel" / "deep",
        OUT_ROOT / "multi_folder" / "folder_a",
        OUT_ROOT / "multi_folder" / "folder_b",
    ]:
        d.mkdir(parents=True, exist_ok=True)
    print("Directories created.")


# ---------------------------------------------------------------------------
# Media file generation
# ---------------------------------------------------------------------------

def generate_media() -> None:
    media = OUT_ROOT / "media"

    # 1. JPEG 1920x1080 (blue)
    jpg_dated = media / "photo_1920x1080.jpg"
    ffmpeg(["-f", "lavfi", "-i", "color=c=blue:s=1920x1080:d=1", "-frames:v", "1"], jpg_dated)
    exiftool([
        '-DateTimeOriginal=2025:06:15 08:22:15',
        '-CreateDate=2025:06:15 08:22:15',
        '-overwrite_original',
    ], jpg_dated)
    print(f"  Created: {jpg_dated.relative_to(REPO_ROOT)}")

    # 2. JPEG 800x600 (red) — all EXIF stripped
    jpg_no_exif = media / "photo_no_exif.jpg"
    ffmpeg(["-f", "lavfi", "-i", "color=c=red:s=800x600:d=1", "-frames:v", "1"], jpg_no_exif)
    exiftool(["-all=", "-overwrite_original"], jpg_no_exif)
    print(f"  Created: {jpg_no_exif.relative_to(REPO_ROOT)}")

    # 3. PNG 800x600 (green)
    png = media / "image_800x600.png"
    ffmpeg(["-f", "lavfi", "-i", "color=c=green:s=800x600:d=1", "-frames:v", "1"], png)
    print(f"  Created: {png.relative_to(REPO_ROOT)}")

    # 4. MP3 1s sine wave + ID3 tags via mutagen
    mp3 = media / "song_with_tags.mp3"
    ffmpeg(["-f", "lavfi", "-i", "sine=frequency=440:duration=1", "-c:a", "libmp3lame"], mp3)
    _embed_mp3_tags(mp3)
    print(f"  Created: {mp3.relative_to(REPO_ROOT)}")

    # 5. WAV 1s sine wave (no tags)
    wav = media / "audio_no_tags.wav"
    ffmpeg(["-f", "lavfi", "-i", "sine=frequency=440:duration=1"], wav)
    print(f"  Created: {wav.relative_to(REPO_ROOT)}")


def _embed_mp3_tags(path: Path) -> None:
    from mutagen.id3 import ID3, ID3NoHeaderError, TIT2, TPE1, TDRC

    try:
        tags = ID3(str(path))
    except ID3NoHeaderError:
        from mutagen.id3 import ID3
        tags = ID3()

    tags.add(TIT2(encoding=3, text="TestSong"))
    tags.add(TPE1(encoding=3, text="TestArtist"))
    tags.add(TDRC(encoding=3, text="2020"))
    tags.save(str(path))


# ---------------------------------------------------------------------------
# Text file generation
# ---------------------------------------------------------------------------

def generate_text_files() -> None:
    specs = [
        ("flat/document.txt",                   "Plain text document for integration tests.\n"),
        ("flat/report_final.md",                 "# Report\nMarkdown report for integration tests.\n"),
        ("flat/data_export.csv",                 "id,name,value\n1,alpha,10\n2,beta,20\n"),
        ("flat/config_dev.json",                 '{"env": "dev", "debug": true}\n'),
        ("flat/no_extension",                    "File with no extension.\n"),
        ("nested/level1_a.txt",                  "Nested level 1 A.\n"),
        ("nested/level1_b.txt",                  "Nested level 1 B.\n"),
        ("nested/sublevel/level2_a.txt",         "Nested level 2 A.\n"),
        ("nested/sublevel/deep/level3_a.txt",    "Nested level 3 A.\n"),
        ("multi_folder/folder_a/file_x.txt",     "Multi-folder A / file X.\n"),
        ("multi_folder/folder_a/file_y.txt",     "Multi-folder A / file Y.\n"),
        ("multi_folder/folder_b/file_p.txt",     "Multi-folder B / file P.\n"),
        ("multi_folder/folder_b/file_q.txt",     "Multi-folder B / file Q.\n"),
    ]
    for rel, content in specs:
        p = OUT_ROOT / Path(rel)
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(content, encoding="utf-8")
        print(f"  Created: {p.relative_to(REPO_ROOT)}")


# ---------------------------------------------------------------------------
# Manifest
# ---------------------------------------------------------------------------

NULL_META = {
    "width": None,
    "height": None,
    "content_creation_date": None,
    "audio_artist": None,
    "audio_title": None,
    "audio_year": None,
}


def _text_entry(file_name: str, extension: str | None) -> dict:
    return {
        "file_name": file_name,
        "extension": extension,
        **NULL_META,
    }


def generate_manifest() -> None:
    manifest = {
        "generated_at": "2026-04-08T12:00:00Z",
        "platform_note": (
            "fs_creation_date and fs_modification_date are NOT reliable after file copy. "
            "Only content_creation_date (EXIF-embedded) is stable."
        ),
        "files": {
            # --- media ---
            "media/photo_1920x1080.jpg": {
                "file_name": "photo_1920x1080",
                "extension": "jpg",
                "width": 1920,
                "height": 1080,
                "content_creation_date": "2025-06-15T08:22:15",
                "audio_artist": None,
                "audio_title": None,
                "audio_year": None,
            },
            "media/photo_no_exif.jpg": {
                "file_name": "photo_no_exif",
                "extension": "jpg",
                "width": 800,
                "height": 600,
                "content_creation_date": None,
                "audio_artist": None,
                "audio_title": None,
                "audio_year": None,
            },
            "media/image_800x600.png": {
                "file_name": "image_800x600",
                "extension": "png",
                "width": 800,
                "height": 600,
                "content_creation_date": None,
                "audio_artist": None,
                "audio_title": None,
                "audio_year": None,
            },
            "media/song_with_tags.mp3": {
                "file_name": "song_with_tags",
                "extension": "mp3",
                "width": None,
                "height": None,
                "content_creation_date": None,
                "audio_artist": "TestArtist",
                "audio_title": "TestSong",
                "audio_year": 2020,
            },
            "media/audio_no_tags.wav": {
                "file_name": "audio_no_tags",
                "extension": "wav",
                **NULL_META,
            },
            # --- flat ---
            "flat/document.txt":       _text_entry("document", "txt"),
            "flat/report_final.md":    _text_entry("report_final", "md"),
            "flat/data_export.csv":    _text_entry("data_export", "csv"),
            "flat/config_dev.json":    _text_entry("config_dev", "json"),
            "flat/no_extension":       _text_entry("no_extension", None),
            # --- nested ---
            "nested/level1_a.txt":                _text_entry("level1_a", "txt"),
            "nested/level1_b.txt":                _text_entry("level1_b", "txt"),
            "nested/sublevel/level2_a.txt":       _text_entry("level2_a", "txt"),
            "nested/sublevel/deep/level3_a.txt":  _text_entry("level3_a", "txt"),
            # --- multi_folder ---
            "multi_folder/folder_a/file_x.txt":  _text_entry("file_x", "txt"),
            "multi_folder/folder_a/file_y.txt":  _text_entry("file_y", "txt"),
            "multi_folder/folder_b/file_p.txt":  _text_entry("file_p", "txt"),
            "multi_folder/folder_b/file_q.txt":  _text_entry("file_q", "txt"),
        },
    }

    manifest_path = OUT_ROOT / "manifest.json"
    manifest_path.write_text(json.dumps(manifest, indent=2), encoding="utf-8")
    print(f"  Written: {manifest_path.relative_to(REPO_ROOT)}  ({len(manifest['files'])} entries)")


# ---------------------------------------------------------------------------
# Verification summary
# ---------------------------------------------------------------------------

def print_summary() -> None:
    print("\n--- Verification Summary ---")
    total_bytes = 0
    for p in sorted(OUT_ROOT.rglob("*")):
        if p.is_file():
            size = p.stat().st_size
            total_bytes += size
            print(f"  {str(p.relative_to(OUT_ROOT)):<55} {size:>8,} bytes")
    total_mb = total_bytes / (1024 * 1024)
    print(f"\nTotal: {total_bytes:,} bytes ({total_mb:.2f} MB)")
    if total_mb >= 10:
        print("WARNING: total size exceeds 10 MB limit!", file=sys.stderr)
    else:
        print("Size check: OK (< 10 MB)")


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main() -> None:
    print(f"Output root: {OUT_ROOT}")
    print("\n[1/4] Creating directories...")
    create_directories()

    print("\n[2/4] Generating media files...")
    generate_media()

    print("\n[3/4] Generating text files...")
    generate_text_files()

    print("\n[4/4] Writing manifest.json...")
    generate_manifest()

    print_summary()
    print("\nDone. Commit app/backend/src/test/resources/integration-test-data/ to git.")


if __name__ == "__main__":
    main()
