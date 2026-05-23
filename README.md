# Tet4J

A block-stacking puzzle game built with [libGDX](https://libgdx.com/).

Arrange falling shapes into complete rows to clear them. No timer, no pressure — just pick a pace that suits you. Level up as you clear rows and unlock new backgrounds.

## How to play

| Key | Action |
|---|---|
| ← → / A D | Move piece |
| ↑ / W | Rotate piece |
| ↓ / S | Soft drop |
| Space | Hard drop |
| Space / Enter | Restart (game over) |

## Compile & run

Requires **Java 21+**.

```sh
gradlew.bat lwjgl3:run
```

To build a runnable jar:

```sh
gradlew.bat lwjgl3:jar
```

The jar will be at `lwjgl3/build/libs/`.
