# Renamer App

**Batch rename files using metadata, patterns, and smart rules — no scripting required.**

[![CI](https://github.com/sanyokkua/renamer_app/actions/workflows/ci.yml/badge.svg)](https://github.com/sanyokkua/renamer_app/actions/workflows/ci.yml)

---

## Screenshots

![Main Window](docs/screens/v2/main_window.png)
![Mode Panel](docs/screens/v2/mode_panel.png)
![Rename Result](docs/screens/v2/rename_result.png)

---

## Features

- **10 renaming modes** — add text, remove text, find & replace, change case, add date/time, add image dimensions,
  number files, add folder name, trim name, change extension
- **Drag & drop** — drop files or entire folders onto the file list
- **Live preview** — see every new filename before committing a single rename
- **Metadata-aware** — reads EXIF dates, image dimensions, and file dates to build filenames automatically
- **19 UI languages** — switch at any time from Settings
- **Native installers** — one-click install on macOS, Linux, and Windows; no Java installation needed

---

## Download

Download the latest installer from [GitHub Releases](https://github.com/sanyokkua/renamer_app/releases).

### macOS

1. Download the `.pkg` or `.dmg` installer.
2. Open the installer and follow the on-screen steps.
3. Launch **Renamer App** from Applications.

### Windows

1. Download the `.exe` or `.msi` installer.
2. Run the installer and follow the on-screen steps.
3. Launch **Renamer App** from the Start menu.

### Linux

1. Download the `.deb`, `.rpm`, or `.AppImage` package for your distribution.
2. Install via your package manager or run the AppImage directly.
3. Launch **Renamer App** from your applications menu.

---

## Build from Source

**Prerequisites:** Java 25+ ([Temurin](https://adoptium.net) recommended), Apache Maven 3.9+

```bash
git clone https://github.com/sanyokkua/renamer_app.git
cd renamer_app/app
mvn clean install -q
```

Run the app after building:

```bash
cd ui
mvn javafx:run
```

See the [Build & Package Guide](docs/developers/guides/build-and-package.md) for native installer builds, signing, and
CI pipeline details.

---

## Documentation

**Users**

- [User Guide](docs/users/user-guide.md) — interface walkthrough, all 10 modes explained with examples
- [Mode Reference Card](docs/users/mode-reference-card.md) — one-page quick-reference cheat sheet

**Developers**

- [Developer Docs](docs/developers/) — architecture, guides, and reference material
    - [Project Overview](docs/developers/architecture/project-overview.md)
    - [Pipeline Architecture](docs/developers/architecture/pipeline-architecture.md)
    - [Transformation Modes](docs/developers/architecture/transformation-modes.md)
    - [Build & Package Guide](docs/developers/guides/build-and-package.md)
    - [Icon Creation Guide](docs/developers/guides/icon-creation.md)

---

## Modules

The app is a multi-module Maven project under `app/`. Each module has a single responsibility:

| Module     | Responsibility                                                            |
|------------|---------------------------------------------------------------------------|
| `api`      | Shared interfaces, enums, and immutable models used across all modules    |
| `core`     | Business logic — all 10 transformers and the rename pipeline orchestrator |
| `backend`  | Session management and file I/O service layer                             |
| `metadata` | File metadata extractors (EXIF dates, image dimensions, MIME types)       |
| `ui`       | JavaFX frontend — controllers, FXML layouts, CSS theming                  |
| `utils`    | Standalone utility classes; not imported by other modules                 |

See [Architecture Docs](docs/developers/architecture/) for data-flow diagrams and module dependency details.

---

## Tech Stack

| Library            | Version | Role                                            |
|--------------------|---------|-------------------------------------------------|
| Java               | 25      | Language and runtime                            |
| JavaFX             | 25.0.2  | Desktop UI framework                            |
| Google Guice       | 7.0.0   | Dependency injection                            |
| metadata-extractor | 2.19.0  | EXIF and metadata reading from images and video |
| Apache Tika        | 3.3.0   | MIME type detection                             |
| Lombok             | 1.18.44 | Boilerplate reduction                           |
| JUnit 5            | 6.0.3   | Unit and integration testing                    |

---

## Contributing

Contributions are welcome. The project uses a structured AI-assisted development workflow with six specialized agents
covering investigation, architecture, implementation, testing, debugging, and documentation. Before contributing, read
the [AI Agent Setup Guide](docs/developers/reference/ai-agent-setup.md) to understand the agent pipeline and how to
invoke each agent for your task type.

---

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
