# Tet4J

An easygoing block-stacking puzzle game built (mostly vibecoded) with [libGDX](https://libgdx.com/) in a relaxed mood.

Arrange falling shapes into complete rows to clear them. Level up as you clear rows and unlock new backgrounds.

Assets by the same author under [CC BY-SA 4.0 license](https://creativecommons.org/licenses/by-sa/4.0/deed) unless stated so. See the [attributions file](attributions.md).

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
