---
title: "Developer Notes"
description: "Test data creation guide with FFmpeg and ExifTool instructions"
audience: "developers"
last_validated: "2026-04-09"
---

# Test data creation

## Create test files on macOS with FFmpeg

Install FFmpeg via Homebrew (`brew install ffmpeg`), then generate tiny test files from silence/colors in Terminal.

**Images** (1-10KB):

```
ffmpeg -f lavfi -i color=c=black:s=320x240:d=1 -frames:v 1 test.jpg
ffmpeg -f lavfi -i testsrc=duration=1:size=320x240:rate=1 test.png
sips -s format heic test.jpg --out test.heic  # For HEIF
```

**Videos** (<500KB, 2-5s):

```
ffmpeg -f lavfi -i testsrc=duration=3:size=320x240:rate=10 -c:v libx264 -pix_fmt yuv420p test.mp4
ffmpeg -f lavfi -i testsrc=duration=3:size=320x240:rate=10 -c:v qtrle test.mov
```

**Audio** (<100KB):

```
ffmpeg -f lavfi -i sine=frequency=1000:duration=2 test.mp3
ffmpeg -f lavfi -i sine=frequency=1000:duration=2 test.wav
```

Resize further with `-vf scale=160:120` for videos/images.

## ExifTool Installation on macOS

Install ExifTool via Homebrew: `brew install exiftool`. It reads/writes EXIF, GPS, XMP, IPTC, and QuickTime metadata
across 500+ formats.

## Supported Formats and Metadata Types

ExifTool handles your formats with these metadata types (worth modifying for testing):

| Format    | EXIF/GPS            | QuickTime | XMP   | IPTC  | Notes                       |
|-----------|---------------------|-----------|-------|-------|-----------------------------|
| JPEG/JPG  | R/W/C               | -         | R/W/C | R/W/C | Primary for images          |
| PNG       | R/W/C (text chunks) | -         | R/W/C | -     | GPS via XMP/tEXt            |
| BMP       | R (basic)           | -         | -     | -     | Limited; use XMP            |
| HEIF/HEIC | R/W/C               | R/W/C     | R/W/C | -     | GPS in EXIF/QuickTime       |
| MOV       | -                   | R/W/C     | R/W/C | -     | GPS via Keys:GPSCoordinates |
| MP4       | -                   | R/W/C     | R/W/C | -     | GPS via QuickTime           |
| AVI       | R (RIFF)            | -         | R     | -     | Basic GPS                   |
| PSD       | R/W/C               | -         | R/W/C | R/W/C | Photoshop IRB/XMP           |

R=Read, W=Write, C=Create. Modify EXIF/GPS/XMP for images (DateTimeOriginal, GPSLatitude); QuickTime for videos (
CreateDate, LocationGPS). BMP/AVI have limited sense beyond basic tests.

## Add GPS Coordinates

Use decimal degrees (e.g., 48.8584 for Paris lat). ExifTool auto-handles ref (N/S/E/W).

**Images (EXIF):**

```
exiftool -GPSLatitude=48.8584 -GPSLongitude=2.2945 test.jpg
```

**Videos (QuickTime, MP4/MOV):**

```
exiftool -Keys:GPSCoordinates="48.8584 2.2945" test.mp4
```

**HEIC/PNG (XMP fallback):**

```
exiftool -XMP:GPSLatitude=48.8584 -XMP:GPSLongitude=2.2945 test.heic
```

For all files: `exiftool -common_args -gps:all=48.8584 2.2945 *.jpg *.mp4` (uses EXIF or QuickTime).

## Set DateTime with Timezone

Format: `YYYY:MM:DD HH:MM:SS±HH:MM` (e.g., 2025:12:09 20:12:00+01:00). Key tags:

- Images: `DateTimeOriginal`, `CreateDate`, `ModifyDate`, `DateTimeDigitized`
- Videos: `QuickTime:CreateDate`, `QuickTime:ModifyDate`

**Examples (all formats):**

```
exiftool "-DateTimeOriginal=2025:12:09 20:12:00+01:00" "-CreateDate=2025:12:09 20:10:00+01:00" test.jpg  # JPEG/PNG/HEIC/PSD
exiftool "-QuickTime:CreateDate=2025:12:09 20:12:00+01:00" test.mov  # MOV/MP4
exiftool "-Keys:CreateDate=2025:12:09 20:12:00+01:00" test.mp4  # MP4 alternative
```

Shift existing: `exiftool -alldates+=0:30:00 test.jpg` (adds 30min). Use `-api QuickTimeUTC` for UTC video times.[3]

## Generate Files With/Without Metadata

**Strip all metadata:**

```
exiftool -all= -o test_no_meta.jpg test.jpg  # Preserves original
```

**Copy with metadata:** `exiftool -tagsFromFile src.jpg -all:all test.jpg`

Batch: `exiftool -r -ext jpg -ext mp4 -all= test_folder/` (backs up originals). Verify:
`exiftool -time:all -gps:all test.jpg`.