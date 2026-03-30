---
name: use-exiftool-metadata
description: ExifTool commands for embedding datetime and GPS metadata into test files in test-data/. Use when generating or modifying test file metadata.
disable-model-invocation: true
allowed-tools: Bash(exiftool *)
---

# ExifTool DateTime & GPS Metadata — Renamer App

Invoke when generating or modifying test file metadata for `app/core/src/test/resources/test-data/`. Used to embed datetime, GPS, and EXIF data into test files so the metadata extraction tests have real metadata to verify.

See also: `/use-ffmpeg-cli` for generating the base test files before adding metadata.

---

## Installation

```bash
brew install exiftool   # macOS
```

---

## DateTime Metadata

### Key Tags

| Tag | Purpose |
|-----|---------|
| `DateTimeOriginal` | Capture date — the "Date Taken" — most important |
| `AllDates` | Shortcut: sets DateTimeOriginal, CreateDate, ModifyDate at once |
| `FileModifyDate` | Filesystem date — NOT the capture date |

### Format

```
YYYY:MM:DD HH:MM:SS
# With timezone:
YYYY:MM:DD HH:MM:SS±HH:MM
```

### Set date on images (JPEG, PNG, HEIC, TIFF)

```bash
# Set all at once — preferred for test data
exiftool -AllDates="2025:12:11 21:00:35" test.jpg

# With timezone
exiftool -AllDates="2025:12:11 21:00:35+02:00" test.jpg

# Verify
exiftool -time:all -G1 -s test.jpg
```

### Set date on videos (MP4, MOV)

```bash
# CRITICAL: always use -api QuickTimeUTC for videos
exiftool -api QuickTimeUTC -CreateDate="2025:12:11 21:00:35" test.mp4
exiftool -api QuickTimeUTC -ModifyDate="2025:12:11 21:00:35" test.mp4

# Verify
exiftool -time:all -G1 -s test.mp4
```

### Shift dates

```bash
exiftool -AllDates+=1 test.jpg      # Add 1 hour
exiftool -AllDates-=2:30 test.jpg   # Subtract 2 hours 30 minutes
```

---

## GPS Metadata

### Write GPS to images

```bash
# Explicit (recommended)
exiftool -GPSLatitude=48.8584 -GPSLongitude=2.2945 -GPSLatitudeRef=N -GPSLongitudeRef=E test.jpg

# With altitude
exiftool -GPSLatitude=48.8584 -GPSLongitude=2.2945 \
         -GPSLatitudeRef=N -GPSLongitudeRef=E \
         -GPSAltitude=35 -GPSAltitudeRef=0 test.jpg

# Verify
exiftool -gps:all -G1 -s test.jpg
```

**Coordinate convention:**
- Positive latitude = North; negative = South
- Positive longitude = East; negative = West

### Write GPS to videos

```bash
exiftool -GPSCoordinates="48.8584, 2.2945, 35" test.mp4
```

### Remove GPS

```bash
exiftool -gps:all= test.jpg         # Images
exiftool -GPSCoordinates= test.mp4  # Videos
```

---

## Image Dimensions Metadata

Image dimensions are read from the actual pixel data — no need to set them manually. Generate files at the right size with FFmpeg:

```bash
# 800x600 image
ffmpeg -f lavfi -i color=c=blue:s=800x600 -frames:v 1 test_800x600.jpg
```

---

## Test Data Generation Workflow

Standard pattern for creating a test file with full metadata:

```bash
# 1. Generate base file with FFmpeg
ffmpeg -f lavfi -i color=c=black:s=800x600 -frames:v 1 test_jpeg_with_date.jpg

# 2. Add datetime
exiftool -AllDates="2025:12:11 21:00:35" test_jpeg_with_date.jpg

# 3. Add GPS (optional)
exiftool -GPSLatitude=48.8584 -GPSLongitude=2.2945 -GPSLatitudeRef=N -GPSLongitudeRef=E test_jpeg_with_date.jpg

# 4. Remove backup created by exiftool
rm test_jpeg_with_date.jpg_original

# 5. Verify all metadata is correct
exiftool -a -G1 -s test_jpeg_with_date.jpg
```

---

## Format-Specific Notes

| Format | DateTime | GPS | Notes |
|--------|----------|-----|-------|
| JPEG | Excellent R/W | Excellent R/W | Gold standard |
| PNG | Good R/W | Good R/W | Via XMP |
| HEIC/HEIF | Good R/W | Good R/W | Like MOV structure |
| TIFF | Excellent R/W | Excellent R/W | Full EXIF |
| MP4/MOV | Good R/W | Good R/W | **Use `-api QuickTimeUTC`** |
| AVI | Limited read | Poor | Legacy — avoid |
| BMP | None | None | Cannot write metadata |

---

## Batch Processing

```bash
# All JPEGs in test-data/
exiftool -AllDates="2025:12:11 21:00:35" -ext jpg app/core/src/test/resources/test-data/

# Multiple extensions
exiftool -AllDates="2025:12:11 21:00:35" -ext jpg -ext png -ext heic app/core/src/test/resources/test-data/

# Recursive
exiftool -r -AllDates="2025:12:11 21:00:35" -ext jpg app/core/src/test/resources/test-data/
```

---

## Strip All Metadata (test "no metadata" case)

```bash
# Creates test_no_meta.jpg without touching original
exiftool -all= -o test_no_meta.jpg test.jpg

# Verify it's clean
exiftool test_no_meta.jpg
```

---

## Safety

ExifTool creates `filename_original` backups by default. Remove with:
```bash
exiftool -overwrite_original [command] file
```

Or clean up manually:
```bash
rm app/core/src/test/resources/test-data/*_original
```
