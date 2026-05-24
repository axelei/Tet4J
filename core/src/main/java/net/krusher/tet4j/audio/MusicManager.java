package net.krusher.tet4j.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.Board;
import net.krusher.tet4j.Constants;
import net.krusher.tet4j.Settings;
import net.krusher.tet4j.gfx.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicManager {
    private final Settings settings;

    private final Music titleMusic;
    private final MusicMetadata titleMusicMeta;

    private final List<Music> gameplayMusic = new ArrayList<>();
    private final Map<Music, MusicMetadata> musicMeta = new HashMap<>();
    private Music currentGm;

    private final Toast toast;

    public MusicManager(Texture pixel, Settings settings) {
        toast = new Toast(pixel);
        this.settings = settings;

        // Load title music by enumerating files at runtime
        Music foundTitle = null;
        MusicMetadata foundTitleMeta = null;
        FileHandle titleDir = Assets.file("music/title");
        if (titleDir.exists() && titleDir.isDirectory()) {
            for (FileHandle file : titleDir.list()) {
                if (file.extension().equalsIgnoreCase("mp3") || file.extension().equalsIgnoreCase("ogg")) {
                    foundTitle = Gdx.audio.newMusic(file);
                    foundTitleMeta = MusicMetadata.fromFile(file);
                    break;
                }
            }
        }
        titleMusic = foundTitle;
        titleMusicMeta = foundTitleMeta;

        // Load gameplay music by enumerating files at runtime
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

    public Music getTitleMusic() {
        return titleMusic;
    }

    public MusicMetadata getTitleMusicMeta() {
        return titleMusicMeta;
    }

    public boolean isTitlePlaying() {
        return titleMusic != null && titleMusic.isPlaying();
    }

    // --- Music control ---

    private void playMusic(Music music) {
        if (music != null && settings.isMusicEnabled()) music.play();
    }

    public void playTitle() {
        playMusic(titleMusic);
    }

    public void stopTitle() {
        if (titleMusic != null) titleMusic.stop();
    }

    public void playCurrentGm() {
        if (currentGm == null) return;
        showMusicToast(musicMeta.get(currentGm));
        playMusic(currentGm);
    }

    /** Selects a random track (different from previous) and shows its toast. Does NOT play. */
    public void selectNextTrack() {
        if (currentGm != null) currentGm.stop();
        if (gameplayMusic.isEmpty()) { currentGm = null; return; }

        Music previousTrack = currentGm;
        if (gameplayMusic.size() == 1) {
            currentGm = gameplayMusic.get(0);
        } else {
            do {
                currentGm = gameplayMusic.get((int)(Math.random() * gameplayMusic.size()));
            } while (currentGm == previousTrack);
        }

        showMusicToast(musicMeta.get(currentGm));
    }

    /** Plays the current track if music is enabled, otherwise stops any playing track. */
    private void ensureTrackPlaying() {
        if (currentGm == null) return;
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

    public void updateGameplayMusic(Board.State state) {
        if (state == Board.State.GAME_OVER) {
            if (currentGm != null && currentGm.isPlaying()) currentGm.stop();
        } else if (state == Board.State.PLAYING) {
            if (currentGm == null) return;
            ensureTrackPlaying();
        } else if (state == Board.State.PAUSED) {
            if (currentGm != null) currentGm.setVolume(Constants.GM_VOLUME_PAUSED);
        }
    }

    // --- Toast ---

    public void showMusicToast(MusicMetadata meta) {
        if (!settings.isMusicEnabled() || meta == null) return;
        toast.setText(meta.title + "\nBy: " + meta.artist + "\n" + meta.license);
    }

    public String getToastText() {
        return toast.getText();
    }

    public float getToastTimer() {
        return toast.getTimer();
    }

    public void setToast(String text) {
        toast.setText(text);
    }

    public void updateToast(float dt) {
        toast.update(dt);
    }

    public void drawToast(SpriteBatch batch, BitmapFont font) {
        toast.draw(batch, font);
    }

    public void dispose() {
        if (titleMusic != null) titleMusic.dispose();
        for (Music m : gameplayMusic) if (m != null) m.dispose();
    }
}
