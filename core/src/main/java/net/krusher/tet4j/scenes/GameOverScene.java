package net.krusher.tet4j.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.entities.Board;
import net.krusher.tet4j.Constants;
import net.krusher.tet4j.Settings;
import net.krusher.tet4j.audio.MusicManager;
import net.krusher.tet4j.gfx.BackgroundManager;
import net.krusher.tet4j.gfx.InfoPanel;
import net.krusher.tet4j.gfx.ParticleSystem;

public class GameOverScene {
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final Board board;
    private final InfoPanel infoPanel;
    private final ParticleSystem particleSystem;
    private final BackgroundManager backgroundManager;
    private final MusicManager musicManager;
    private final Settings settings;
    private long gameOverSoundId = -1;

    public GameOverScene(SpriteBatch batch, ShapeRenderer shapes, Board board, InfoPanel infoPanel,
                         ParticleSystem particleSystem, BackgroundManager backgroundManager,
                         MusicManager musicManager, Settings settings) {
        this.batch = batch;
        this.shapes = shapes;
        this.board = board;
        this.infoPanel = infoPanel;
        this.particleSystem = particleSystem;
        this.backgroundManager = backgroundManager;
        this.musicManager = musicManager;
        this.settings = settings;
    }

    public void render() {
        infoPanel.drawGameOver(batch, shapes, Assets.bigFont, Assets.font);
    }

    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (gameOverSoundId != -1 && Assets.sfxGameOver != null) {
                Assets.sfxGameOver.stop(gameOverSoundId);
                gameOverSoundId = -1;
            }
            board.reset();
            particleSystem.clear();
            backgroundManager.reset(Constants.STARTING_LEVEL);
            musicManager.selectNextTrack();
            musicManager.playCurrentGm();
        }
    }

    public void playGameOverSound() {
        if (Assets.sfxGameOver != null && settings.isSoundEffectsEnabled()) {
            gameOverSoundId = Assets.sfxGameOver.play();
        }
    }
}
