package net.krusher.tet4j;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

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

    public interface FileResolver {
        FileHandle resolve(String path);
    }

    public static FileResolver fileResolver = path -> {
        String normalized = path.replace("\\", "/");
        if (!normalized.startsWith("assets/")) {
            normalized = "assets/" + normalized;
        }
        return Gdx.files.internal(normalized);
    };

    public static FileHandle file(String path) {
        return fileResolver.resolve(path);
    }

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
        for (int i = 0; i < 4; i++) {
            sfxClear[i] = Gdx.audio.newSound(file("sounds/clear" + (i + 1) + ".ogg"));
        }
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

    private static void disposeSafely(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    public static void dispose() {
        disposeSafely(font);
        disposeSafely(bigFont);
        if (blockTextures != null) {
            for (Texture t : blockTextures) {
                disposeSafely(t);
            }
        }
        disposeSafely(ghostTexture);
        disposeSafely(bgTexture);
        disposeSafely(relief);
        disposeSafely(splashTexture);
        disposeSafely(logoTexture);
        disposeSafely(pixel);
        disposeSafely(sfxMove);
        disposeSafely(sfxRotate);
        disposeSafely(sfxDrop);
        disposeSafely(sfxSoftDrop);
        if (sfxClear != null) {
            for (Sound s : sfxClear) {
                disposeSafely(s);
            }
        }
        disposeSafely(sfxGameOver);
        disposeSafely(glowShader);
    }
}
