---
title: "User Guide"
description: "Complete user guide for the Renamer App — getting started, all transformation modes, and settings"
audience: "users"
last_validated: "2026-04-09"
last_commit: "3c570e2"
related_modules:
  - "app/ui"
  - "app/core"
---

# User Guide

## Table of Contents

- [What Is This App?](#what-is-this-app)
- [The Interface](#the-interface)
- [Adding Files](#adding-files)
- [Modes](#modes)
    - [Add Text](#add-text)
    - [Remove Text](#remove-text)
    - [Replace Text](#replace-text)
    - [Change Case](#change-case)
    - [Add Date & Time](#add-date--time)
    - [Add Dimensions](#add-dimensions)
    - [Number Files](#number-files)
    - [Add Folder Name](#add-folder-name)
    - [Trim Name](#trim-name)
    - [Change Extension](#change-extension)
- [Preview & Rename](#preview--rename)
- [Settings](#settings)
- [Tips & Tricks](#tips--tricks)

---

## What Is This App?

The Renamer app lets you rename hundreds of files at once. Instead of renaming files one by one, you pick a mode —
like "add a date to every filename" or "number all these photos" — set your options, and the app renames everything in
seconds.

You can add text, remove text, swap text, change capitalization, insert dates from photo metadata, add image dimensions,
number files in order, include the parent folder name, trim unwanted characters, or change the file extension.

Metadata features (dates, image dimensions) work with these file types: `jpg`, `jpeg`, `png`, `gif`, `bmp`, `heif`,
`heic`, `tiff`, `tif`, `psd`, `ico`, `pcx`, `webp`, `epsf`, `eps`, `epsi`, `mp4`, `m4v`, `mov`, `qt`, `avi`, `m4a`,
`m4b`, `m4p`, `m4r`, `wav`, `wave`.

---

## The Interface

![App overview](../screens/v2/app_overview.png)

The app has four main areas:

| Area                 | Location   | What it does                                                       |
|----------------------|------------|--------------------------------------------------------------------|
| **Mode selector**    | Top bar    | Choose which renaming mode to use                                  |
| **File list**        | Center     | Shows all your files and a live **Preview** column                 |
| **Parameters panel** | Right side | Options for the selected mode                                      |
| **Action buttons**   | Bottom     | **Preview renaming**, **Rename files**, **Clear**, **Reload File** |

The **Preview** column updates automatically as you change parameters — you always see exactly what the new filenames
will look like before committing.

---

## Adding Files

Drag files or folders from your file manager and drop them onto the file list.

**Dropping a folder** — a dialog appears asking what you want to do:

- **Expand contents** — adds all the files inside the folder to the list
- **Add folder itself** — adds the folder as a single item (useful when renaming the folder itself)

You can drag in multiple folders and files at the same time. To start fresh, click **Clear**.

---

## Modes

### Add Text

![Add Text mode](../screens/v2/mode_add_text.png)

Add any word, phrase, or characters to the beginning or end of every filename.

| Parameter       | What it does                                                                                    |
|-----------------|-------------------------------------------------------------------------------------------------|
| Text field      | The text you want to add                                                                        |
| **Add text to** | **Begin** — adds before the filename · **End** — adds after the filename (before the extension) |

**Examples:**

- `photo.jpg` → `vacation_photo.jpg` (added `vacation_` at the beginning)
- `report.pdf` → `report_final.pdf` (added `_final` at the end)

---

### Remove Text

![Remove Text mode](../screens/v2/mode_remove_text.png)

Remove a specific word or phrase from the beginning or end of filenames. Useful for stripping camera prefixes like
`IMG_` or `DSC_`.

| Parameter  | What it does                                                        |
|------------|---------------------------------------------------------------------|
| Text field | The text you want to remove                                         |
| Position   | **Begin** — removes from the start · **End** — removes from the end |

**Examples:**

- `IMG_photo.jpg` → `photo.jpg` (removed `IMG_` from the beginning)
- `photo_copy.jpg` → `photo.jpg` (removed `_copy` from the end)

---

### Replace Text

![Replace Text mode](../screens/v2/mode_replace_text.png)

Find any text in your filenames and replace it with something else. You can target the very start, the very end, or
every occurrence anywhere in the name.

| Parameter    | What it does                                                    |
|--------------|-----------------------------------------------------------------|
| Find         | The text to search for                                          |
| Replace with | What to put in its place (leave empty to delete the found text) |
| Where        | **Begin** · **End** · **Everywhere**                            |

**Examples:**

- `my photo copy.jpg` → `my photo backup.jpg` (replaced `copy` with `backup`, Everywhere)
- `---report.pdf` → `report.pdf` (replaced `---` with nothing, at Begin)

---

### Change Case

![Change Case mode](../screens/v2/mode_change_case.png)

Change the capitalization of every filename in one click.

| Parameter      | What it does                                        |
|----------------|-----------------------------------------------------|
| **Text Case**  | Pick a style from the list below                    |
| **Capitalize** | Also capitalize the very first letter of the result |

**Available styles:**

| Style                | Example         |
|----------------------|-----------------|
| UPPERCASE            | `MY PHOTO FILE` |
| lowercase            | `my photo file` |
| Title Case           | `My Photo File` |
| camelCase            | `myPhotoFile`   |
| PascalCase           | `MyPhotoFile`   |
| snake_case           | `my_photo_file` |
| SCREAMING_SNAKE_CASE | `MY_PHOTO_FILE` |
| kebab-case           | `my-photo-file` |

**Example:**

- `My Photo File.jpg` → `my_photo_file.jpg` (snake_case)

---

### Add Date & Time

![Add Date & Time mode](../screens/v2/mode_add_datetime.png)

Add a date or time to every filename, pulled from the file's own metadata or a date you choose.

| Parameter   | What it does                                                     |
|-------------|------------------------------------------------------------------|
| **Source**  | Where the date comes from (see below)                            |
| Date format | How to format the date part (e.g., `yyyy-MM-dd`)                 |
| Time format | How to format the time part (leave blank to omit time)           |
| Position    | **Begin** — adds before the name · **End** — adds after the name |

**Date sources:**

| Source                         | What it uses                                                    |
|--------------------------------|-----------------------------------------------------------------|
| File Creation Datetime         | When the file was created on your computer                      |
| File Modification Datetime     | When the file was last changed                                  |
| File Content Creation Datetime | When the photo or video was actually taken (from EXIF metadata) |
| Current Datetime               | Today's date and time                                           |
| Custom Datetime                | A date you type in manually                                     |

**Examples:**

- `photo.jpg` → `2024-03-15_photo.jpg` (File Content Creation Date at Begin)
- `report.pdf` → `report_2024-03-15.pdf` (File Modification Date at End)

---

### Add Dimensions

![Add Dimensions mode](../screens/v2/mode_add_dimensions.png)

Add the image or video resolution to the filename. Works with image and video files that have dimension metadata.

| Parameter       | What it does                                                     |
|-----------------|------------------------------------------------------------------|
| Left side       | What to show on the left (Width, Height, or Do not use)         |
| Right side      | What to show on the right (Width, Height, or Do not use)        |
| Separator       | Character between left and right (usually "x")                  |
| Name separator  | Character between dimensions and filename (e.g., "_", "-", " ") |
| Position        | **Begin**, **End**, or **Replace** (dimensions replace filename) |

**Examples:**

- `wallpaper.jpg` → `wallpaper_1920x1080.jpg` (both width and height, at End)
- `photo.jpg` → `4032x3024_photo.jpg` (both, at Begin)

---

### Number Files

![Number Files mode](../screens/v2/mode_number_files.png)

Add sequential numbers to filenames. Great for sorting a batch of photos or documents into a defined order.

| Parameter    | What it does                                                           |
|--------------|------------------------------------------------------------------------|
| Start number | The first number in the sequence (e.g., `1`, `100`)                    |
| Step         | How much to increment each time (e.g., `1`, `5`)                       |
| Padding      | Minimum digits — adds leading zeros (e.g., `3` gives `001`, `002`)     |
| Sort by      | The order files are numbered — by name, size, date, or dimensions      |
| Per folder   | When checked, restarts the count from the start number for each folder |

**Sort options:** File Name · File Path · File Size · File Creation Datetime · File Modification Datetime · File Content
Creation Datetime · Image/Video Width · Image/Video Height

**Examples:**

- `photo.jpg` → `001_photo.jpg` (start at 1, padding 3, at Begin)
- `report.pdf` → `report_01.pdf` (start at 1, padding 2, at End)

---

### Add Folder Name

![Add Folder Name mode](../screens/v2/mode_add_folder_name.png)

Add the name of the file's parent folder — or multiple parent folders — to the filename. Useful for giving files context
about where they came from.

| Parameter      | What it does                                                        |
|----------------|---------------------------------------------------------------------|
| Parent folders | How many levels of parent folders to include (1 = immediate parent) |
| Position       | **Begin** or **End**                                                |
| Separator      | The character placed between the folder name and the filename       |

**Examples:**

- `file.txt` in folder `Reports` → `Reports_file.txt` (1 parent, Begin, separator `_`)
- `photo.jpg` in `2024 > Vacation` → `2024-Vacation-photo.jpg` (2 parents, Begin, separator `-`)

---

### Trim Name

![Trim Name mode](../screens/v2/mode_trim_name.png)

Remove a set number of characters from the start or end of filenames, or strip leading/trailing spaces.

| Parameter   | What it does                                                               |
|-------------|----------------------------------------------------------------------------|
| Characters  | How many characters to remove                                              |
| Remove from | **Begin** · **End** · **Trim Empty** (removes leading and trailing spaces) |

**Examples:**

- `___photo___.jpg` → `photo.jpg` (Trim Empty removes the spaces/underscores at both ends)
- `IMG_photo.jpg` → `photo.jpg` (remove 4 characters from Begin)
- `report_v2.pdf` → `report.pdf` (remove 3 characters from End, before the extension)

---

### Change Extension

![Change Extension mode](../screens/v2/mode_change_extension.png)

Change the file extension for every file in the list.

| Parameter     | What it does                                                |
|---------------|-------------------------------------------------------------|
| New extension | The extension to apply (type without the dot, e.g., `jpeg`) |

**Examples:**

- `photo.jpg` → `photo.jpeg`
- `archive.tar.gz` → `archive.tar.zip`

---

## Preview & Rename

The **Preview** column in the file list shows the new filename for every file as you adjust parameters. It updates
live — no button press needed.

When you're happy with the preview:

1. Click **Rename files** to apply the changes.
2. The app renames all files immediately.
3. If two files would end up with the same name, the app automatically adds `_1`, `_2`, etc. to make them unique — it
   never silently overwrites a file.
4. If a file can't be renamed (for example, it's locked by another app, or you don't have permission), the file list row
   shows an error. Other files in the batch are still renamed successfully.

To reset and start over, click **Clear** to remove all files from the list.

---

## Settings

Open **Settings** from the menu bar to adjust these options:

| Setting       | What it does                                                                                                                    |
|---------------|---------------------------------------------------------------------------------------------------------------------------------|
| **Language**  | Changes the app's display language. 19 languages are supported. Takes effect immediately.                                       |
| **Log level** | Controls how much detail the app writes to its internal log. Leave this at the default unless you're troubleshooting a problem. |

---

## Tips & Tricks

1. **Sort chaotic photo dumps with Number Files.** Drop your photos in, sort by **File Content Creation Datetime**, and
   prefix every file with `001_`, `002_` — instant chronological order.

2. **Date-stamp photos by when they were taken, not copied.** In **Add Date & Time**, choose **File Content Creation
   Datetime** as the source. This reads the EXIF data recorded by your camera, not the file's creation date on your
   computer.

3. **Use Replace Text to delete text.** Leave the "Replace with" field empty. Set "Where" to **Everywhere**. The app
   removes every occurrence of the search text without replacing it with anything.

4. **Drop a whole folder to process everything inside.** When the folder dialog appears, choose **Expand contents** —
   the app adds all the files inside at once, so you don't have to select them individually.

5. **Combine modes by running them in rounds.** Run **Remove Text** first to strip unwanted prefixes, then run **Add
   Text** to add new ones. Each run is a fresh operation on the current state of your files.

6. **Tweak parameters before committing.** The Preview column updates as you type. There's no cost to experimenting —
   nothing changes on disk until you click **Rename files**.
