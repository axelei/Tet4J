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
import net.krusher.tet4j.gfx.BoardRenderer;
import net.krusher.tet4j.gfx.GameOverOverlay;
import net.krusher.tet4j.gfx.InfoPanel;
import net.krusher.tet4j.gfx.ParticleSystem;

public class GameOverScene implements Scene {
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final Board board;
    private final ParticleSystem particleSystem;
    private final BackgroundManager backgroundManager;
    private final InfoPanel infoPanel;
    private final SplashScene splashScene;
    private final Settings settings;
    private long gameOverSoundId = -1;

    public GameOverScene(SpriteBatch batch, ShapeRenderer shapes, Board board,
                         ParticleSystem particleSystem, BackgroundManager backgroundManager,
                         InfoPanel infoPanel,
                         SplashScene splashScene, Settings settings) {
        this.batch = batch;
        this.shapes = shapes;
        this.board = board;
        this.particleSystem = particleSystem;
        this.backgroundManager = backgroundManager;
        this.infoPanel = infoPanel;
        this.splashScene = splashScene;
        this.settings = settings;
    }

    @Override
    public void update(float dt) {
        particleSystem.update(dt);
        MusicManager.updateGameplayMusic(board.state);
    }

    @Override
    public void render() {
        backgroundManager.draw(batch);
        BoardRenderer.drawBoardBackground(shapes);
        BoardRenderer.drawGame(batch, board, particleSystem, infoPanel, false);
        GameOverOverlay.draw(batch, shapes);
    }

    @Override
    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (gameOverSoundId != -1 && Assets.sfxGameOver != null) {
                Assets.sfxGameOver.stop(gameOverSoundId);
                gameOverSoundId = -1;
            }
            particleSystem.clear();
            backgroundManager.reset(Constants.STARTING_LEVEL);
            splashScene.reset();
        }
    }

    public void playGameOverSound() {
        if (Assets.sfxGameOver != null && settings.isSoundEffectsEnabled()) {
            gameOverSoundId = Assets.sfxGameOver.play();
        }
    }
}
