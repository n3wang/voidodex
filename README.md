# Void Codex

A 2D space-strategy roguelike inspired by FTL, built with [libGDX](https://libgdx.com/).

## Quick Start - How to Run

### Windows
```bash
gradlew.bat :lwjgl3:run
```

### Linux/Mac
```bash
./gradlew :lwjgl3:run
```

This will launch the game window. You should see the main menu where you can start a new game.

## Game Controls

- **Mouse**: Click buttons and interact with UI elements
- **Ship Screen**: Click on rooms to view details, manage crew, and systems
- **Codex Screen**: Read codex pages before hyperspace jumps (required!)
- **Navigation**: Use buttons to move between screens

## Building a Runnable JAR

To create a standalone JAR file:

### Windows
```bash
gradlew.bat :lwjgl3:jar
```

### Linux/Mac
```bash
./gradlew :lwjgl3:jar
```

The JAR will be created at `lwjgl3/build/libs/voidcodex-1.0.0.jar`

You can then run it with:
```bash
java -jar lwjgl3/build/libs/voidcodex-1.0.0.jar
```

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.
