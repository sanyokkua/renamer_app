---
title: "Mode Reference Card"
description: "Quick reference for all transformation modes — names, purposes, and key options"
audience: "users"
last_validated: "2026-04-09"
last_commit: "3c570e2"
related_modules:
  - "app/core"
  - "app/api"
---

# Mode Reference Card

Quick reference for all 10 renaming modes — see the [User Guide](user-guide.md) for full explanations.

## Modes

| Mode                 | What It Does                                    | Key Parameters                                         | Example                                     |
|----------------------|-------------------------------------------------|--------------------------------------------------------|---------------------------------------------|
| **Add Text**         | Adds text to the start or end of filenames      | Text; Position (Begin / End)                           | `photo.jpg` → `vacation_photo.jpg`          |
| **Remove Text**      | Removes text from the start or end of filenames | Text to remove; Position (Begin / End)                 | `IMG_photo.jpg` → `photo.jpg`               |
| **Find & Replace**   | Replaces text in filenames                      | Find; Replace with; Where (Begin / End / Everywhere)   | `photo copy.jpg` → `photo backup.jpg`       |
| **Change Case**      | Changes the capitalization style                | Case style (UPPERCASE, lowercase, Title Case, etc.)    | `My File.jpg` → `my_file.jpg`               |
| **Add Date & Time**  | Adds a date or time from file or photo metadata | Source (5 options); Date format; Time format; Position | `photo.jpg` → `2024-03-15_photo.jpg`        |
| **Add Dimensions**   | Adds image or video resolution to the filename  | Include width; Include height; Position                | `wallpaper.jpg` → `wallpaper_1920x1080.jpg` |
| **Number Files**     | Adds sequential numbers to filenames            | Start; Step; Padding; Sort by (8 options); Per folder  | `photo.jpg` → `001_photo.jpg`               |
| **Add Folder Name**  | Adds the parent folder name to filenames        | Parent folders; Position; Separator                    | `file.txt` → `Reports_file.txt`             |
| **Trim Name**        | Removes characters from the start or end        | Characters; Remove from (Begin / End / Trim Empty)     | `___photo___.jpg` → `photo.jpg`             |
| **Change Extension** | Changes the file extension                      | New extension                                          | `photo.jpg` → `photo.jpeg`                  |

## Tips

1. The **Preview** column updates live — experiment freely before clicking **Rename files**.
2. To delete text without a replacement: use **Find & Replace**, leave "Replace with" empty.
3. To number photos in the order they were taken: use **Number Files** → Sort by **File Content Creation Datetime**.
4. Drop a folder and choose **Expand contents** to process all files inside at once.
