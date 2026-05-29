package net.krusher.tet4j.audio;

import com.badlogic.gdx.Application;
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

import static net.krusher.tet4j.Main.IS_WEB;

public final class MusicManager {
    private static Settings settings;

    private static String titleTrackPath;
    private static MusicMetadata titleMusicMeta;
    private static Music titleMusic;

    private static final List<String> gameplayTrackPaths = new ArrayList<>();
    private static final Map<String, MusicMetadata> metadataByPath = new HashMap<>();
    private static final Map<String, Music> loadedMusic = new HashMap<>();
    private static String currentTrackPath;
    private static Music currentGm;
    private static float fadeOutTimer = -1f;

    public static void init(Settings s) {
        settings = s;

        FileHandle csvFile = Assets.file("music/tags.csv");
        if (!csvFile.exists()) {
            return;
        }
        String content = csvFile.readString();
        String[] lines = content.split("\n");
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\|", 4);
            if (parts.length < 4) {
                continue;
            }
            String csvPath = parts[0];
            MusicMetadata meta = new MusicMetadata(parts[1], parts[2], parts[3]);

            if (csvPath.startsWith("music/title/")) {
                titleTrackPath = csvPath;
                titleMusicMeta = meta;
            } else if (csvPath.startsWith("music/gameplay/")) {
                gameplayTrackPaths.add(csvPath);
                metadataByPath.put(csvPath, meta);
            }
        }
    }

    private static Music loadMusic(String csvPath) {
        Music existing = loadedMusic.get(csvPath);
        if (existing != null) {
            return existing;
        }
        FileHandle fh = Assets.file(csvPath);
        if (!fh.exists()) {
            return null;
        }
        Music music = Gdx.audio.newMusic(fh);
        loadedMusic.put(csvPath, music);
        return music;
    }

    public static MusicMetadata getTitleMusicMeta() {
        return titleMusicMeta;
    }

    public static boolean isTitlePlaying() {
        return titleMusic != null && titleMusic.isPlaying();
    }

    private static void playMusic(Music music) {
        if (music != null && settings.isMusicEnabled()) {
            music.play();
        }
    }

    public static void playTitle() {
        if (titleMusic == null) {
            titleMusic = loadMusic(titleTrackPath);
        }
        playMusic(titleMusic);
    }

    public static void stopTitle() {
        if (titleMusic != null) {
            titleMusic.stop();
        }
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
        if (currentTrackPath == null) { return; }
        if (currentGm == null) {
            currentGm = loadMusic(currentTrackPath);
        }
        cancelFadeOut();
        showMusicToast(metadataByPath.get(currentTrackPath));
        playMusic(currentGm);
    }

    private static void cancelFadeOut() {
        fadeOutTimer = -1f;
        if (currentGm != null) { currentGm.setVolume(Constants.GM_VOLUME); }
    }

    public static void stopCurrentGm() {
        cancelFadeOut();
        if (currentGm != null) {
            currentGm.stop();
            currentGm = null;
            currentTrackPath = null;
        }
    }

    public static void selectNextTrack() {
        cancelFadeOut();
        if (currentGm != null) { currentGm.stop(); }
        if (gameplayTrackPaths.isEmpty()) { currentTrackPath = null; currentGm = null; return; }

        String previousPath = currentTrackPath;
        if (gameplayTrackPaths.size() == 1) {
            currentTrackPath = gameplayTrackPaths.getFirst();
        } else {
            do {
                currentTrackPath = gameplayTrackPaths.get((int)(Math.random() * gameplayTrackPaths.size()));
            } while (currentTrackPath.equals(previousPath));
        }

        currentGm = loadMusic(currentTrackPath);
        showMusicToast(metadataByPath.get(currentTrackPath));
    }

    private static void ensureTrackPlaying() {
        if (currentTrackPath == null) { return; }
        if (currentGm == null) {
            currentGm = loadMusic(currentTrackPath);
        }
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
            if (currentTrackPath == null) { return; }
            ensureTrackPlaying();
        } else if (state == Board.State.PAUSED) {
            if (currentTrackPath == null) { return; }
            ensureTrackPlaying();
            currentGm.setVolume(Constants.GM_VOLUME_PAUSED);
        }
    }

    public static void showMusicToast(MusicMetadata meta) {
        if (!settings.isMusicEnabled() || meta == null) { return; }
        Toast.setText(meta.title() + "\nBy: " + meta.artist() + "\n" + meta.license());
    }

    public static float getToastTimer() {
        return Toast.getTimer();
    }

    public static void updateToast(float dt) {
        Toast.update(dt);
    }

    public static void drawToast(SpriteBatch batch, BitmapFont font) {
        Toast.draw(batch, font);
    }

    public static void dispose() {
        if (titleMusic != null) { titleMusic.dispose(); }
        for (Music m : loadedMusic.values()) {
            if (m != null) {
                m.dispose();
            }
        }
        loadedMusic.clear();
    }

    private MusicManager() {}
}
