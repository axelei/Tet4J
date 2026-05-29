package net.krusher.tet4j.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.entities.Board;
import net.krusher.tet4j.Constants;
import net.krusher.tet4j.Settings;
import net.krusher.tet4j.gfx.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MusicManager {
    private static Settings settings;

    private static Music titleMusic;
    private static MusicMetadata titleMusicMeta;

    private static final List<Music> gameplayMusic = new ArrayList<>();
    private static final Map<Music, MusicMetadata> musicMeta = new HashMap<>();
    private static Music currentGm;
    private static float fadeOutTimer = -1f;

    private static final Toast toast = new Toast();

    public static void init(Settings s) {
        settings = s;

        FileHandle titleDir = Assets.file("music/title");
        if (titleDir.exists() && titleDir.isDirectory()) {
            for (FileHandle file : titleDir.list()) {
                if (file.extension().equalsIgnoreCase("mp3") || file.extension().equalsIgnoreCase("ogg")) {
                    titleMusic = Gdx.audio.newMusic(file);
                    titleMusicMeta = MusicMetadata.fromFile(file);
                    break;
                }
            }
        }

        FileHandle gameplayDir = Assets.file("music/gameplay");
        if (gameplayDir.exists() && gameplayDir.isDirectory()) {
            for (FileHandle file : gameplayDir.list()) {
                if (file.extension().equalsIgnoreCase("mp3") || file.extension().equalsIgnoreCase("ogg")) {
                    Music m = Gdx.audio.newMusic(file);
                    gameplayMusic.add(m);
                    musicMeta.put(m, MusicMetadata.fromFile(file));
                }
            }
        }
        selectNextTrack();
    }

    public static MusicMetadata getTitleMusicMeta() {
        return titleMusicMeta;
    }

    public static boolean isTitlePlaying() {
        return titleMusic != null && titleMusic.isPlaying();
    }

    private static void playMusic(Music music) {
        if (music != null && settings.isMusicEnabled()) { music.play(); }
    }

    public static void playTitle() {
        playMusic(titleMusic);
    }

    public static void stopTitle() {
        if (titleMusic != null) { titleMusic.stop(); }
    }

    public static void update(float dt) {
        if (fadeOutTimer < 0) { return; }
        fadeOutTimer += dt;
        if (fadeOutTimer >= Constants.GM_FADE_DURATION) {
            if (currentGm != null) { currentGm.stop(); }
            fadeOutTimer = -1f;
        } else {
            float t = fadeOutTimer / Constants.GM_FADE_DURATION;
            if (currentGm != null) { currentGm.setVolume(Constants.GM_VOLUME * (1f - t)); }
        }
    }

    public static void playCurrentGm() {
        if (currentGm == null) { return; }
        cancelFadeOut();
        showMusicToast(musicMeta.get(currentGm));
        playMusic(currentGm);
    }

    private static void cancelFadeOut() {
        fadeOutTimer = -1f;
        if (currentGm != null) { currentGm.setVolume(Constants.GM_VOLUME); }
    }

    public static void selectNextTrack() {
        cancelFadeOut();
        if (currentGm != null) { currentGm.stop(); }
        if (gameplayMusic.isEmpty()) { currentGm = null; return; }

        Music previousTrack = currentGm;
        if (gameplayMusic.size() == 1) {
            currentGm = gameplayMusic.getFirst();
        } else {
            do {
                currentGm = gameplayMusic.get((int)(Math.random() * gameplayMusic.size()));
            } while (currentGm == previousTrack);
        }

        showMusicToast(musicMeta.get(currentGm));
    }

    private static void ensureTrackPlaying() {
        if (currentGm == null) { return; }
        if (settings.isMusicEnabled()) {
            currentGm.setVolume(Constants.GM_VOLUME);
            if (!currentGm.isPlaying()) {
                selectNextTrack();
                playMusic(currentGm);
            }
        } else if (currentGm.isPlaying()) {
            currentGm.stop();
        }
    }

    public static void updateGameplayMusic(Board.State state) {
        if (state == Board.State.GAME_OVER) {
            if (currentGm != null && currentGm.isPlaying() && fadeOutTimer < 0) {
                fadeOutTimer = 0f;
            }
        } else if (state == Board.State.PLAYING) {
            if (currentGm == null) { return; }
            ensureTrackPlaying();
        } else if (state == Board.State.PAUSED) {
            if (currentGm == null) { return; }
            ensureTrackPlaying();
            currentGm.setVolume(Constants.GM_VOLUME_PAUSED);
        }
    }

    public static void showMusicToast(MusicMetadata meta) {
        if (!settings.isMusicEnabled() || meta == null) { return; }
        toast.setText(meta.title() + "\nBy: " + meta.artist() + "\n" + meta.license());
    }

    public static float getToastTimer() {
        return toast.getTimer();
    }

    public static void updateToast(float dt) {
        toast.update(dt);
    }

    public static void drawToast(SpriteBatch batch, BitmapFont font) {
        toast.draw(batch, font);
    }

    public static void dispose() {
        if (titleMusic != null) { titleMusic.dispose(); }
        for (Music m : gameplayMusic) {
            if (m != null) {
                m.dispose();
            }
        }
    }

    private MusicManager() {}
}
