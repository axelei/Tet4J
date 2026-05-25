package net.krusher.tet4j;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import net.krusher.tet4j.audio.MusicManager;
import net.krusher.tet4j.entities.Board;
import net.krusher.tet4j.gfx.BackgroundManager;
import net.krusher.tet4j.gfx.BoardRenderer;
import net.krusher.tet4j.gfx.InfoPanel;
import net.krusher.tet4j.gfx.ParticleSystem;
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
    private OrthographicCamera camera;

    private ParticleSystem particleSystem;
    private BackgroundManager backgroundManager;
    private MusicManager musicManager;
    private BoardRenderer boardRenderer;
    private InfoPanel infoPanel;

    private Viewport viewport;
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
        }

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);
        viewport.apply();

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
        viewport.update(width, height);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(Constants.BOARD_BG.r, Constants.BOARD_BG.g, Constants.BOARD_BG.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

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
        batch.dispose();
        shapes.dispose();
        Assets.dispose();
        backgroundManager.dispose();
        musicManager.dispose();
    }
}
