package net.krusher.tet4j;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import net.krusher.tet4j.audio.MusicManager;
import net.krusher.tet4j.entities.Board;
import net.krusher.tet4j.gfx.*;
import net.krusher.tet4j.scenes.GameOverScene;
import net.krusher.tet4j.scenes.PauseScene;
import net.krusher.tet4j.scenes.PlayScene;
import net.krusher.tet4j.scenes.SplashScene;

public class Main extends ApplicationAdapter {
    static {
        java.util.logging.Logger.getLogger("org.jaudiotagger").setLevel(java.util.logging.Level.WARNING);
    }

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private Board board;
    private GraphicsManager graphicsManager;

    private ParticleSystem particleSystem;
    private BackgroundManager backgroundManager;
    private MusicManager musicManager;
    private BoardRenderer boardRenderer;
    private InfoPanel infoPanel;

    private Settings settings;

    private SplashScene splashScene;
    private PlayScene playScene;
    private GameOverScene gameOverScene;
    private PauseScene pauseScene;

    @Override
    public void create() {
        // Initialize settings
        settings = new Settings();

        // Set up fullscreen mode if enabled
        if (settings.isFullscreenEnabled()) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            // Calculate window size as 90% of current screen resolution
            com.badlogic.gdx.Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
            int windowWidth = (int) (displayMode.width * 0.9f);
            int windowHeight = (int) (displayMode.height * 0.9f);

            // Maintain the 16:9 aspect ratio
            int calculatedHeight = (int) (windowWidth / 1.777f); // 16:9 ratio
            if (calculatedHeight <= windowHeight) {
                windowHeight = calculatedHeight;
            } else {
                windowWidth = (int) (windowHeight * 1.777f);
            }

            Gdx.graphics.setWindowedMode(windowWidth, windowHeight);
        }

        graphicsManager = new GraphicsManager(settings);

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        Assets.load();

        board = new Board();
        particleSystem = new ParticleSystem();
        backgroundManager = new BackgroundManager();
        musicManager = new MusicManager(settings);
        boardRenderer = new BoardRenderer();
        infoPanel = new InfoPanel();

        splashScene = new SplashScene(batch, infoPanel, musicManager, board);
        playScene = new PlayScene(batch, shapes, board, particleSystem, backgroundManager, musicManager, boardRenderer, infoPanel, settings);
        gameOverScene = new GameOverScene(batch, shapes, board, infoPanel, particleSystem, backgroundManager, musicManager, settings);
        pauseScene = new PauseScene(shapes, batch);
    }

    @Override
    public void resize(int width, int height) {
        graphicsManager.resize(width, height);
    }

    @Override
    public void render() {
if (!splashScene.isFinished()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                Gdx.app.exit();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                // Continue game logic here
            }
        } else if ((Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT))
            && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            boolean full = !Gdx.graphics.isFullscreen();
            graphicsManager.toggleFullscreen();
            settings.setFullscreenEnabled(full);
        }
        Gdx.gl.glClearColor(Constants.BOARD_BG.r, Constants.BOARD_BG.g, Constants.BOARD_BG.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        graphicsManager.getViewport().apply();
        batch.setProjectionMatrix(graphicsManager.getCamera().combined);
        shapes.setProjectionMatrix(graphicsManager.getCamera().combined);

        float dt = Gdx.graphics.getDeltaTime();
        musicManager.updateToast(dt);
        musicManager.update(dt);

        if (!splashScene.isFinished()) {
            splashScene.render();
            return;
        }

        handleInput(dt);
        board.update(dt);

        if (board.justGameOver) {
            gameOverScene.playGameOverSound();
            board.justGameOver = false;
            board.justLocked = false;
        }

        playScene.processEvents(dt);
        playScene.render(pauseScene.isAskingExit());
        if (board.state == Board.State.GAME_OVER) {
            gameOverScene.render();
        } else {
            pauseScene.render();
        }
        if (musicManager.getToastTimer() >= 0) musicManager.drawToast(batch, Assets.font);
    }

    private void handleInput(float dt) {
        pauseScene.handleInput(board);
        if (pauseScene.isAskingExit()) return;

        if (board.state == Board.State.GAME_OVER) {
            gameOverScene.handleInput();
            return;
        }

        if (board.state != Board.State.PLAYING) return;

        playScene.handleInput(dt);
    }

    @Override
    public void dispose() {
        settings.save();
        batch.dispose();
        shapes.dispose();
        Assets.dispose();
        backgroundManager.dispose();
        musicManager.dispose();
    }
}
