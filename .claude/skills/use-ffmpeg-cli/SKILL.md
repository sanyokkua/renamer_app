---
name: use-ffmpeg-cli
description: FFmpeg commands for generating synthetic base test media files in test-data/. Use when creating test images, videos, or audio files before adding metadata with /use-exiftool-metadata.
disable-model-invocation: true
allowed-tools: Bash(ffmpeg *), Bash(ffprobe *)
---

# FFmpeg CLI — Renamer App

Invoke when generating base test files for `app/core/src/test/resources/test-data/`. FFmpeg creates the synthetic media files; ExifTool then adds metadata (see `/use-exiftool-metadata`).

---

## Installation

```bash
brew install ffmpeg   # macOS
```

---

## Test File Generation (Primary Use Case)

### Images

```bash
# JPEG — specify exact dimensions (metadata tests check width/height)
ffmpeg -f lavfi -i color=c=blue:s=800x600 -frames:v 1 test_jpeg.jpg
ffmpeg -f lavfi -i color=c=black:s=1920x1080 -frames:v 1 test_jpeg_hd.jpg

# PNG
ffmpeg -f lavfi -i color=c=red:s=1280x720 -frames:v 1 test_png.png

# TIFF
ffmpeg -f lavfi -i color=c=green:s=640x480 -frames:v 1 test_tiff.tif

# BMP
ffmpeg -f lavfi -i color=c=white:s=320x240 -frames:v 1 test_bmp.bmp
```

### Videos

```bash
# MP4 (5 seconds, black, H.264)
ffmpeg -f lavfi -i color=c=black:s=1280x720:r=30 \
       -f lavfi -i anullsrc=r=48000:cl=stereo \
       -c:v libx264 -pix_fmt yuv420p -c:a aac -t 5 test_mp4.mp4

# MOV (QuickTime)
ffmpeg -f lavfi -i color=c=black:s=1920x1080:r=24 \
       -f lavfi -i anullsrc=r=44100:cl=stereo \
       -c:v libx264 -pix_fmt yuv420p -c:a aac -t 5 test_mov.mov

# AVI (legacy format)
ffmpeg -f lavfi -i color=c=black:s=640x480:r=25 -t 5 test_avi.avi
```

### Audio

```bash
# MP3 (10 seconds, 1kHz sine, stereo)
ffmpeg -f lavfi -i "sine=frequency=1000:duration=10" -c:a libmp3lame -b:a 128k test_mp3.mp3

# WAV (silent, 5 seconds)
ffmpeg -f lavfi -i anullsrc=r=44100:cl=stereo -t 5 test_wav.wav

# FLAC
ffmpeg -f lavfi -i anullsrc=r=44100:cl=stereo -t 5 test_flac.flac

# OGG
ffmpeg -f lavfi -i "sine=frequency=440:duration=5" -c:a libvorbis test_ogg.ogg

# M4A
ffmpeg -f lavfi -i anullsrc=r=44100:cl=stereo -t 5 -c:a aac test_m4a.m4a
```

---

## Full Test Data Workflow

Standard pattern (FFmpeg generates file, ExifTool adds metadata):

```bash
# 1. Generate base file
ffmpeg -f lavfi -i color=c=black:s=800x600 -frames:v 1 test_jpeg_with_date.jpg

# 2. Add metadata with ExifTool (see /use-exiftool-metadata)
exiftool -AllDates="2025:12:11 21:00:35" test_jpeg_with_date.jpg
exiftool -GPSLatitude=48.8584 -GPSLongitude=2.2945 -GPSLatitudeRef=N -GPSLongitudeRef=E test_jpeg_with_date.jpg
rm test_jpeg_with_date.jpg_original

# 3. Verify
exiftool -a -G1 -s test_jpeg_with_date.jpg
```

---

## Key Flags Reference

| Flag | Purpose | Example |
|------|---------|---------|
| `-f lavfi` | Synthetic input source | `-f lavfi -i color=c=black:s=800x600` |
| `-frames:v 1` | Single image frame | for still images |
| `-t` | Duration in seconds | `-t 5` |
| `-c:v libx264` | H.264 video codec | for MP4/MOV |
| `-pix_fmt yuv420p` | Required for H.264 compat | always add with libx264 |
| `-c:a aac` | AAC audio codec | for MP4/MOV |
| `-c copy` | No re-encode | container change only |
| `-y` | Overwrite without prompt | `-y` at start |

---

## Dimension Note

H.264 requires dimensions divisible by 2. Use even numbers (800x600, 1280x720, 1920x1080).

```bash
# If you need odd dimensions, pad with:
-vf "scale=trunc(iw/2)*2:trunc(ih/2)*2"
```

---

## Verify File Info

```bash
ffprobe -v quiet -print_format json -show_format -show_streams file.mp4
```
