# Tet4J

<p align="center">
  <img src="assets/graphics/tet4j_logo.png" alt="Tet4J Logo" width="600">
</p>

<p align="center">
  <strong>Tet4J</strong> (Tetrominoes for Java) is an easygoing, block-stacking puzzle game in a relaxed mood, built (and mostly vibecoded) using <b><a href="https://libgdx.com/">libGDX</a></b>.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/Framework-libGDX-red" alt="libGDX">
</p>

---

### 🕹️ The Game
Arrange falling shapes into complete rows to clear them from the board. As you clear lines, you will level up, increase your score, and unlock new beautifully crafted backgrounds to match the chill atmosphere.

### 🎨 Assets & Licensing
* **Original Assets:** Created by the author and distributed under the [CC BY-SA 4.0 license](https://creativecommons.org/licenses/by-sa/4.0/deed) unless stated otherwise.
* **Art & Branding:** The logo and the splash screen are AI-generated. (Help wanted to replace them!)
* **Third-party Assets:** For full credits and external licenses, please check the [attributions file](attributions.md).

## How to play

| Key | Action             |
|---|--------------------|
| ← → / A D | Move piece         |
| ↑ / W | Rotate piece       |
| ↓ / S | Soft drop          |
| Space | Hard drop          |
| Alt+Enter | Switch full screen |
| Alt+F4 | Quit immediately   |

## 🛠️ System Requirements
* **Java Development Kit (JDK):** Version 21 or higher (only required if you are building from source).
* **Operating System:** Windows, Linux, or macOS. (Or just a modern browser if playing the web version)
* **Graphics Compatibility:** A graphics card supporting OpenGL 3.0 or higher.

---

## 📦 Downloads & Pre-built Releases
You don't need to compile the code to play the game! Ready-to-run packages for Windows, Linux, and macOS are automatically built using **Construo**. 

Simply head over to the [Releases](https://github.com/axelei/Tet4J/releases) section of this repository, download the compressed archive corresponding to your operating system, extract it, and run the executable.

---

## 🍏 Special Instructions for macOS Users

Since the game is bundled as a standalone `.app` using **Construo** but is not digitally signed or notarized with an official Apple Developer certificate, modern versions of macOS (such as Sonoma and Sequoia) will strictly block its execution, often claiming the application is **"damaged and cannot be opened"**.

To bypass this security restriction, use one of the following methods:

### Option 1: The Terminal Fix (Recommended & Most Reliable)
Because modern macOS versions have disabled the classic "Right-Click -> Open" shortcut for unsigned apps, the fastest way to run the game is to clear Apple's quarantine flag manually:

1. Open your **Terminal**.
2. Run the following command, making sure to point it to the path where your `Tet4J.app` is located:
```bash
   xattr -cr /path/to/Tet4J.app
   ```
   *(The `-cr` flags will recursively strip the quarantine attributes from the entire application bundle).*
3. Double-click `Tet4J.app` in Finder to launch the game normally.

### Option 2: Via System Settings
If you prefer not to use the terminal:
1. Double-click `Tet4J.app` once to let macOS register and block the application.
2. Open your Mac's **System Settings** and navigate to **Privacy & Security**.
3. Scroll down until you see a security message regarding `Tet4J.app`.
4. Click the **"Open Anyway"** button and authenticate with your Mac's password or Touch ID.
5. The game will launch, and you won't need to repeat this step again.

---

## 🚀 Building and Running from Source

This project uses Gradle, so there is no need to install it globally on your system.

### Run in Development Mode:
```bash
./gradlew desktop:run
```
