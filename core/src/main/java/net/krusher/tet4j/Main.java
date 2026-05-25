package net.krusher.tet4j;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import net.krusher.tet4j.audio.MusicManager;
import net.krusher.tet4j.gfx.BackgroundManager;
import net.krusher.tet4j.gfx.BoardRenderer;
import net.krusher.tet4j.gfx.InfoPanel;
import net.krusher.tet4j.gfx.ParticleSystem;

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
    private boolean showSplash = true;
    private Settings settings;

    private float moveTimer, dropTimer;
    private int moveDir;
    private boolean askingExit;

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
    }

    private void playSfx(Sound sound) {
        if (sound != null && settings.isSoundEffectsEnabled()) sound.play();
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

        if (showSplash) {
            infoPanel.drawSplash(batch, Assets.bigFont, Assets.font);
            if (!musicManager.isTitlePlaying()) {
                musicManager.playTitle();
                musicManager.showMusicToast(musicManager.getTitleMusicMeta());
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.F12)) {
                board.cheatMode = !board.cheatMode;
                if (board.cheatMode) {
                    board.currentType = Tetromino.Type.I;
                    board.nextType = Tetromino.Type.I;
                    board.currentX = 3;
                    board.currentY = 0;
                    board.currentRotation = 0;
                }
            }
            musicManager.drawToast(batch, Assets.font);
            if (Gdx.input.isKeyPressed(Input.Keys.ANY_KEY) || Gdx.input.isTouched()) {
                showSplash = false;
                musicManager.stopTitle();
                musicManager.playCurrentGm();
            }
            return;
        }

        handleInput(dt);
        board.update(dt);

        if (board.justCleared) {
            int idx = Math.min(Math.max(board.linesCleared, 1), 4) - 1;
            playSfx(Assets.sfxClear[idx]);
            board.justCleared = false;
            particleSystem.spawnClearingParticles(board);
        }

        if (board.justGameOver) {
            playSfx(Assets.sfxGameOver);
            board.justGameOver = false;
            board.justLocked = false;
        } else if (board.justLocked) {
            playSfx(Assets.sfxSoftDrop);
            board.justLocked = false;
        }

        particleSystem.update(dt);
        infoPanel.update(dt);
        musicManager.updateGameplayMusic(board.state);
        backgroundManager.update(dt, board.level);

        backgroundManager.draw(batch);
        boardRenderer.drawBoardBackground(shapes);
        boardRenderer.drawGame(batch, Assets.bigFont, board, particleSystem, infoPanel, Assets.font, askingExit);
        if (board.state == Board.State.GAME_OVER) {
            infoPanel.drawGameOver(batch, shapes, board, Assets.bigFont, Assets.font);
        } else if (askingExit) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0, 0, 0, Constants.PAUSE_OVERLAY_ALPHA);
            shapes.rect(Constants.BOARD_X, Constants.BOARD_Y, Constants.BOARD_PX_W, Constants.BOARD_PX_H);
            shapes.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
            batch.begin();
            Assets.bigFont.draw(batch, "Quit?",
                Constants.BOARD_X + Constants.QUIT_PROMPT_X, Constants.BOARD_Y + Constants.BOARD_PX_H / 2f + Constants.TEXT_CENTER_Y_OFFSET_LARGE);
            Assets.font.draw(batch, "Y / N",
                Constants.BOARD_X + Constants.CONFIRM_PROMPT_X, Constants.BOARD_Y + Constants.BOARD_PX_H / 2f + Constants.TEXT_CENTER_Y_OFFSET_SMALL);
            batch.end();
        }
        if (musicManager.getToastTimer() >= 0) musicManager.drawToast(batch, Assets.font);
    }

    private void handleInput(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (board.state == Board.State.PLAYING) {
                board.state = Board.State.PAUSED;
                askingExit = true;
            } else if (board.state == Board.State.PAUSED) {
                if (askingExit) {
                    board.state = Board.State.PLAYING;
                    askingExit = false;
                } else {
                    askingExit = true;
                }
            }
        }

        if (askingExit) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) {
                Gdx.app.exit();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
                board.state = Board.State.PLAYING;
                askingExit = false;
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            if (board.state == Board.State.PLAYING) {
                board.state = Board.State.PAUSED;
            } else if (board.state == Board.State.PAUSED) {
                board.state = Board.State.PLAYING;
            }
        }

        if (board.state != Board.State.PLAYING) {
            if (board.state == Board.State.GAME_OVER &&
                (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER))) {
                board.reset();
                particleSystem.clear();
                backgroundManager.reset(Constants.STARTING_LEVEL);
                musicManager.selectNextTrack();
                musicManager.playCurrentGm();
                playSfx(Assets.sfxDrop);
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            board.rotateCW();
            playSfx(Assets.sfxRotate);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            board.hardDrop();
            if (board.state == Board.State.PLAYING) playSfx(Assets.sfxDrop);
        }

        boolean softDropping = Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);
        if (softDropping) {
            dropTimer += dt;
            if (dropTimer >= Constants.SOFT_DROP_INTERVAL) {
                dropTimer -= Constants.SOFT_DROP_INTERVAL;
                board.softDrop();
            }
        } else {
            dropTimer = 0;
        }

        int dir = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) dir = -1;
        else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) dir = 1;

        if (dir != 0) {
            if (moveDir != dir) {
                moveDir = dir;
                moveTimer = 0;
                if (dir < 0) board.moveLeft(); else board.moveRight();
                playSfx(Assets.sfxMove);
            } else {
                moveTimer += dt;
                if (moveTimer >= Constants.DAS_DELAY) {
                    if (dir < 0) board.moveLeft(); else board.moveRight();
                    float repeat = moveTimer - Constants.DAS_DELAY;
                    int count = (int)(repeat / Constants.DAS_REPEAT);
                    int prevCount = (int)((repeat - dt) / Constants.DAS_REPEAT);
                    if (count > prevCount) playSfx(Assets.sfxMove);
                }
            }
        } else {
            moveDir = 0;
            moveTimer = 0;
        }
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
