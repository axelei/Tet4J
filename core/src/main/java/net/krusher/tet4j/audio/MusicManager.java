package net.krusher.tet4j.audio;

import com.badlogic.gdx.Gdx;
import net.krusher.tet4j.Assets;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.files.FileHandle;
import net.krusher.tet4j.Board;
import net.krusher.tet4j.Constants;
import net.krusher.tet4j.audio.MusicMetadata;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicManager {
    private final Music titleMusic;
    private final MusicMetadata titleMusicMeta;
    private final List<Music> gameplayMusic = new ArrayList<>();
    private final Map<Music, MusicMetadata> musicMeta = new HashMap<>();
    private Music currentGm;

    private String toastText;
    private float toastTimer = -1;
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private final Texture pixel;

    public MusicManager(Texture pixel) {
        this.pixel = pixel;

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
        pickNextGmTrack();
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

    public void playTitle() {
        if (titleMusic != null) titleMusic.play();
    }

    public void stopTitle() {
        if (titleMusic != null) titleMusic.stop();
    }

    public String getToastText() {
        return toastText;
    }

    public float getToastTimer() {
        return toastTimer;
    }

    public void updateToast(float dt) {
        if (toastTimer >= 0) {
            toastTimer += dt;
            if (toastTimer > Constants.TOAST_DURATION) toastTimer = -1;
        }
    }

    public void setToast(String text) {
        toastText = text;
        toastTimer = 0;
    }

    public void updateGameplayMusic(Board.State state) {
        if (state == Board.State.GAME_OVER) {
            if (currentGm != null && currentGm.isPlaying()) currentGm.stop();
        } else if (state == Board.State.PLAYING) {
            if (currentGm != null) {
                currentGm.setVolume(Constants.GM_VOLUME);
                if (!currentGm.isPlaying()) {
                    pickNextGmTrack();
                    if (currentGm != null) currentGm.play();
                }
            }
        } else if (state == Board.State.PAUSED) {
            if (currentGm != null) currentGm.setVolume(Constants.GM_VOLUME_PAUSED);
        }
    }

    public void playCurrentGm() {
        if (currentGm != null) {
            MusicMetadata meta = musicMeta.get(currentGm);
            if (meta != null) setToast(meta.title + "\n" + meta.artist + "\n" + meta.license);
            currentGm.play();
        }
    }

    public void pickNextGmTrack() {
        if (currentGm != null) currentGm.stop();
        if (gameplayMusic.isEmpty()) { currentGm = null; return; }
        currentGm = gameplayMusic.get((int)(Math.random() * gameplayMusic.size()));
        MusicMetadata meta = musicMeta.get(currentGm);
        if (meta != null) setToast(meta.title + "\n" + meta.artist + "\n" + meta.license);
    }

    public void drawToast(SpriteBatch batch, BitmapFont font) {
        if (toastText == null || toastTimer < 0) return;

        glyphLayout.setText(font, toastText, com.badlogic.gdx.graphics.Color.WHITE, Constants.TOAST_MAX_WIDTH,
            com.badlogic.gdx.utils.Align.left, true);
        float th = glyphLayout.height + Constants.TOAST_PAD_Y * 2;

        float alpha = 1f;
        float yOffset;
        if (toastTimer < Constants.TOAST_SLIDE_IN) {
            float t = toastTimer / Constants.TOAST_SLIDE_IN;
            alpha = t;
            yOffset = (1 - t) * -(th + Constants.TOAST_SLIDE_OFFSET);
        } else if (toastTimer > Constants.TOAST_DURATION - Constants.TOAST_SLIDE_OUT) {
            float t = (Constants.TOAST_DURATION - toastTimer) / Constants.TOAST_SLIDE_OUT;
            alpha = t;
            yOffset = (1 - t) * -(th + Constants.TOAST_SLIDE_OFFSET);
        } else {
            yOffset = 0;
        }

        batch.begin();
        float tx = Constants.SCREEN_WIDTH - Constants.TOAST_MAX_WIDTH - Constants.TOAST_MARGIN_X;
        float ty = Constants.TOAST_MARGIN_Y + th + yOffset;

        batch.setColor(0, 0, 0, Constants.TOAST_BG_ALPHA * alpha);
        batch.draw(pixel, tx, ty - th, Constants.TOAST_MAX_WIDTH, th);
        batch.setColor(1, 1, 1, alpha);
        font.draw(batch, glyphLayout, tx + Constants.TOAST_PAD_X, ty - Constants.TOAST_PAD_Y);
        batch.setColor(1, 1, 1, 1);
        batch.end();
    }

    public void dispose() {
        if (titleMusic != null) titleMusic.dispose();
        for (Music m : gameplayMusic) if (m != null) m.dispose();
    }
}
