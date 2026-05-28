package net.krusher.tet4j;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Assets {

    public static Texture pixel;
    public static Texture splashTexture;
    public static Texture logoTexture;
    public static Texture[] blockTextures;
    public static Texture ghostTexture;
    public static Texture bgTexture;
    public static Texture relief;

    public static BitmapFont font;
    public static BitmapFont bigFont;
    public static ShaderProgram glowShader;

    public static Sound sfxMove;
    public static Sound sfxRotate;
    public static Sound sfxDrop;
    public static Sound sfxSoftDrop;
    public static Sound sfxGameOver;
    public static Sound[] sfxClear = new Sound[4];

    public static void load() {
        FreeTypeFontGenerator fontGen = new FreeTypeFontGenerator(file("fonts/ModernDOS8x16.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = Constants.FONT_SIZE * 4;
        font = fontGen.generateFont(param);
        font.getData().setScale(0.25f);
        param.size = Constants.FONT_SIZE_BIG * 4;
        bigFont = fontGen.generateFont(param);
        bigFont.getData().setScale(0.25f);
        fontGen.dispose();

        sfxMove = Gdx.audio.newSound(file("sounds/move.ogg"));
        sfxRotate = Gdx.audio.newSound(file("sounds/rotate.ogg"));
        sfxDrop = Gdx.audio.newSound(file("sounds/drop.ogg"));
        sfxSoftDrop = Gdx.audio.newSound(file("sounds/softdrop.ogg"));
        for (int i = 0; i < 4; i++)
            sfxClear[i] = Gdx.audio.newSound(file("sounds/clear" + (i + 1) + ".ogg"));
        sfxGameOver = Gdx.audio.newSound(file("sounds/gameover.ogg"));

        String[] blockFiles = {"block_i.png", "block_o.png", "block_t.png",
            "block_s.png", "block_z.png", "block_j.png", "block_l.png"};
        blockTextures = new Texture[7];
        for (int i = 0; i < 7; i++) {
            blockTextures[i] = new Texture(file("graphics/" + blockFiles[i]));
        }
        ghostTexture = new Texture(file("graphics/ghost.png"));
        bgTexture = new Texture(file("graphics/bg.png"));
        relief = new Texture(file("graphics/relief.png"));

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        splashTexture = new Texture(file("graphics/splash.jpg"));
        logoTexture = new Texture(file("graphics/tet4j_logo.png"));

        ShaderProgram.pedantic = false;
        glowShader = new ShaderProgram(file("shaders/glow.vert"), file("shaders/glow.frag"));
        if (!glowShader.isCompiled()) {
            Gdx.app.error("Assets", "glow shader compile failed: " + glowShader.getLog());
        }
    }

    public static void dispose() {
        if (font != null) font.dispose();
        if (bigFont != null) bigFont.dispose();
        if (blockTextures != null) for (Texture t : blockTextures) if (t != null) t.dispose();
        if (ghostTexture != null) ghostTexture.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (relief != null) relief.dispose();
        if (splashTexture != null) splashTexture.dispose();
        if (logoTexture != null) logoTexture.dispose();
        if (pixel != null) pixel.dispose();
        if (sfxMove != null) sfxMove.dispose();
        if (sfxRotate != null) sfxRotate.dispose();
        if (sfxDrop != null) sfxDrop.dispose();
        if (sfxSoftDrop != null) sfxSoftDrop.dispose();
        if (sfxClear != null) for (Sound s : sfxClear) if (s != null) s.dispose();
        if (sfxGameOver != null) sfxGameOver.dispose();
        if (glowShader != null) glowShader.dispose();
    }

    public static com.badlogic.gdx.files.FileHandle file(String path) {
        // Normalize path to forward slashes for consistency
        String normalizedPath = path.replace("\\", "/");
        if (!normalizedPath.startsWith("assets/")) {
            normalizedPath = "assets/" + normalizedPath;
        }

        // Try 1: Current working directory (Gradle run task)
        java.io.File assetsDir = new java.io.File("assets");
        if (assetsDir.exists() && assetsDir.isDirectory()) {
            return Gdx.files.local(normalizedPath);
        }

        // Try 2: Relative to user working directory
        java.io.File userDir = new java.io.File(System.getProperty("user.dir"));
        assetsDir = new java.io.File(userDir, "assets");
        if (assetsDir.exists() && assetsDir.isDirectory()) {
            return Gdx.files.local(normalizedPath);
        }

        // Try 3: Relative to executable/JAR location (for packaged builds)
        try {
            java.net.URL codeLocation = Assets.class.getProtectionDomain().getCodeSource().getLocation();
            if (codeLocation != null) {
                java.nio.file.Path codePath = new java.io.File(java.net.URLDecoder.decode(codeLocation.getPath(), "UTF-8")).toPath();

                // Handle macOS app bundle structure (.app/Contents/MacOS/...)
                if (codePath.toString().contains(".app/Contents/MacOS")) {
                    // For macOS bundles: go up from .../Tet4J.app/Contents/MacOS/bin to .../Tet4J.app/Contents/MacOS
                    java.io.File macOSDir = codePath.getParent().toFile();
                    assetsDir = new java.io.File(macOSDir, "assets");
                    if (assetsDir.exists() && assetsDir.isDirectory()) {
                        return Gdx.files.absolute(new java.io.File(assetsDir, normalizedPath.substring("assets/".length())).getAbsolutePath());
                    }
                }

                // Handle standard executable/JAR location
                java.io.File exeDir = codePath.getParent().toFile();
                assetsDir = new java.io.File(exeDir, "assets");
                if (assetsDir.exists() && assetsDir.isDirectory()) {
                    return Gdx.files.absolute(new java.io.File(assetsDir, normalizedPath.substring("assets/".length())).getAbsolutePath());
                }

                // For JAR files, try the directory containing the JAR
                if (codePath.toString().endsWith(".jar")) {
                    exeDir = codePath.getParent().toFile();
                    assetsDir = new java.io.File(exeDir, "assets");
                    if (assetsDir.exists() && assetsDir.isDirectory()) {
                        return Gdx.files.absolute(new java.io.File(assetsDir, normalizedPath.substring("assets/".length())).getAbsolutePath());
                    }
                }
            }
        } catch (Exception ignored) {
            // If something goes wrong, fall through to the default behavior
        }

        // Fallback to default behavior (Gdx will try from classpath)
        if (path.startsWith("assets/") || path.startsWith("assets\\")) {
            return Gdx.files.local(normalizedPath);
        } else {
            return Gdx.files.local("assets/" + normalizedPath);
        }
    }
}
