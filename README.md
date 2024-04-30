# renamer_app
Desktop tool to help with renaming files on your PC

# Problem

Usually during work with files on the PC you need to rename several files using some pattern.

For example, you have a list of images with names:

- IMG_0001.jpg
- IMG_0002.jpg
- IMG_0003.jpg
- IMG_0004.jpg
- IMG_0005.jpg

Also, you know that these files has creation date, or modification date, or exif data that can be used as name.
 
As a result you want to have names like these:

- 20240506_172055.jpg
- 20240507_123011.jpg
- 20240507_134022.jpg
- 20240508_050033.jpg
- 20240508_105066.jpg

Currently, you have only one way to rename it - manually check properties and create name.

This app should simplify such renaming.
You will be able to rename files based on the provided pattern or some properties of the file.

Also, there are cases when you don't need to use any file properties, but just want to remove from name prefixes or suffixes.
This option also can be available.

# Used libs

- [ExifRead](https://pypi.org/project/ExifRead/), [Docs](https://github.com/ianare/exif-py) - Exif read tool for some types of images, reserve lib
- [Pillow](https://pypi.org/project/pillow/), [Docs](https://pillow.readthedocs.io/en/stable/) - Parsing Image Data
- [Pillow Heif](https://pypi.org/project/pillow-heif/), [Docs](https://pillow-heif.readthedocs.io/en/latest/) - Support for HEIF codec and file types like **".heic"** for Pillow
- [Mutagen](https://pypi.org/project/mutagen/), [Docs](https://mutagen.readthedocs.io/en/latest/) - Support for extracting audio metadata for MP3, FLAC, OGG, etc.
- [PySide6](https://pypi.org/project/PySide6/), [Docs](https://doc.qt.io/qtforpython-6/api.html) - UI Widgets

# How to run app from sources

Following should be installed:
- Python 3
- Pipenv

```shell
pipenv install
```

TODO: