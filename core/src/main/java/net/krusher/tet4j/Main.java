package net.krusher.tet4j;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import net.krusher.tet4j.audio.MusicManager;
import net.krusher.tet4j.entities.Board;
import net.krusher.tet4j.gfx.GraphicsManager;
import net.krusher.tet4j.gfx.BackgroundManager;
import net.krusher.tet4j.scenes.GameOverScene;
import net.krusher.tet4j.scenes.PauseScene;
import net.krusher.tet4j.scenes.PlayScene;
import net.krusher.tet4j.scenes.Scene;
import net.krusher.tet4j.scenes.SplashScene;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private Board board;

    private Settings settings;

    private Scene activeScene;
    private SplashScene splashScene;
    private PlayScene playScene;
    private GameOverScene gameOverScene;

    @Override
    public void create() {
        settings = new Settings();
        GraphicsManager.init(settings);

        GraphicsManager.applyDisplayMode();

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        Assets.load();

        board = new Board();
        BackgroundManager.init();
        MusicManager.init(settings);

        splashScene = new SplashScene(batch, board);
        PauseScene pauseScene = new PauseScene(shapes, batch, board);
        playScene = new PlayScene(batch, shapes, board, pauseScene, settings);
        gameOverScene = new GameOverScene(batch, shapes, board, splashScene, settings);

        activeScene = splashScene;
    }

    @Override
    public void resize(int width, int height) {
        GraphicsManager.resize(width, height);
    }

    @Override
    public void render() {
        if ((Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT))
            && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            GraphicsManager.toggleFullscreen();
        }

        Gdx.gl.glClearColor(Constants.BOARD_BG.r, Constants.BOARD_BG.g, Constants.BOARD_BG.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        GraphicsManager.getViewport().apply();
        batch.setProjectionMatrix(GraphicsManager.getCamera().combined);
        shapes.setProjectionMatrix(GraphicsManager.getCamera().combined);

        float dt = Gdx.graphics.getDeltaTime();
        MusicManager.updateToast(dt);
        MusicManager.update(dt);

        activeScene.update(dt);
        activeScene.render();
        activeScene.handleInput();

        if (activeScene == splashScene && splashScene.isFinished()) {
            activeScene = playScene;
        }
        if (activeScene == playScene && board.justGameOver) {
            gameOverScene.playGameOverSound();
            board.justGameOver = false;
            board.justLocked = false;
            activeScene = gameOverScene;
        }
        if (activeScene == gameOverScene && !splashScene.isFinished()) {
            activeScene = splashScene;
        }

        if (MusicManager.getToastTimer() >= 0) {
            MusicManager.drawToast(batch, Assets.font);
        }
    }

    @Override
    public void dispose() {
        settings.save();
        batch.dispose();
        shapes.dispose();
        Assets.dispose();
        BackgroundManager.dispose();
        MusicManager.dispose();
    }
}
