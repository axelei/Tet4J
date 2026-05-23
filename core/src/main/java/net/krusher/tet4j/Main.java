package net.krusher.tet4j;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import net.krusher.tet4j.audio.MusicManager;
import net.krusher.tet4j.Assets;
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
    private BitmapFont font, bigFont;
    private OrthographicCamera camera;
    private Texture pixel, splashTexture;
    private Texture[] blockTextures;
    private Texture ghostTexture, bgTexture;

    private Sound sfxMove, sfxRotate, sfxDrop, sfxGameOver, sfxSoftDrop;
    private Sound[] sfxClear = new Sound[4];

    private ParticleSystem particleSystem;
    private BackgroundManager backgroundManager;
    private MusicManager musicManager;
    private BoardRenderer boardRenderer;
    private InfoPanel infoPanel;

    private boolean showSplash = true;

    private float moveTimer, dropTimer;
    private int moveDir;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        FreeTypeFontGenerator fontGen = new FreeTypeFontGenerator(Assets.file("fonts/pixel.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = Constants.FONT_SIZE;
        font = fontGen.generateFont(param);
        param.size = Constants.FONT_SIZE_BIG;
        bigFont = fontGen.generateFont(param);
        fontGen.dispose();

        sfxMove = Gdx.audio.newSound(Assets.file("sounds/move.wav"));
        sfxRotate = Gdx.audio.newSound(Assets.file("sounds/rotate.wav"));
        sfxDrop = Gdx.audio.newSound(Assets.file("sounds/drop.wav"));
        sfxSoftDrop = Gdx.audio.newSound(Assets.file("sounds/softdrop.wav"));
        for (int i = 0; i < 4; i++)
            sfxClear[i] = Gdx.audio.newSound(Assets.file("sounds/clear" + (i + 1) + ".wav"));
        sfxGameOver = Gdx.audio.newSound(Assets.file("sounds/gameover.wav"));

        loadTextures();
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();
        splashTexture = new Texture(Assets.file("graphics/splash.jpg"));

        board = new Board();
        particleSystem = new ParticleSystem(blockTextures);
        backgroundManager = new BackgroundManager();
        musicManager = new MusicManager(pixel);
        boardRenderer = new BoardRenderer(blockTextures, ghostTexture, bgTexture, pixel);
        infoPanel = new InfoPanel(pixel);
    }

    private void loadTextures() {
        String[] blockFiles = {"block_i.png", "block_o.png", "block_t.png",
            "block_s.png", "block_z.png", "block_j.png", "block_l.png"};
        blockTextures = new Texture[7];
        for (int i = 0; i < 7; i++) {
            blockTextures[i] = new Texture(Assets.file("graphics/" + blockFiles[i]));
        }
        ghostTexture = new Texture(Assets.file("graphics/ghost.png"));
        bgTexture = new Texture(Assets.file("graphics/bg.png"));
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(Constants.BOARD_BG.r, Constants.BOARD_BG.g, Constants.BOARD_BG.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        float dt = Gdx.graphics.getDeltaTime();
        musicManager.updateToast(dt);

        if (showSplash) {
            infoPanel.drawSplash(batch, splashTexture, bigFont);
            if (!musicManager.isTitlePlaying()) {
                musicManager.playTitle();
                if (musicManager.getTitleMusicMeta() != null) {
                    musicManager.setToast(musicManager.getTitleMusicMeta().title + "\n"
                        + musicManager.getTitleMusicMeta().artist + "\n"
                        + musicManager.getTitleMusicMeta().license);
                }
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
            musicManager.drawToast(batch, font);
            if (Gdx.input.isKeyPressed(Input.Keys.ANY_KEY) || Gdx.input.isTouched()) {
                showSplash = false;
                musicManager.stopTitle();
                musicManager.playCurrentGm();
            }
            return;
        }

        if (board.justCleared) {
            int idx = Math.min(Math.max(board.linesCleared, 1), 4) - 1;
            if (sfxClear[idx] != null) sfxClear[idx].play();
            board.justCleared = false;
            particleSystem.spawnClearingParticles(board);
        }

        handleInput(dt);
        board.update(dt);
        if (board.justAutoDropped) {
            if (sfxSoftDrop != null) sfxSoftDrop.play();
            board.justAutoDropped = false;
        }
        particleSystem.update(dt);
        musicManager.updateGameplayMusic(board.state);
        backgroundManager.update(dt, board.level);

        backgroundManager.draw(batch);
        boardRenderer.drawBoardBackground(shapes);
        boardRenderer.drawGame(batch, bigFont, board, particleSystem, infoPanel, font);
        if (board.state == Board.State.GAME_OVER) {
            infoPanel.drawGameOver(batch, shapes, board, bigFont, font);
        }
        if (musicManager.getToastTimer() >= 0) musicManager.drawToast(batch, font);
    }

    private void handleInput(float dt) {
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
                backgroundManager.reset();
                musicManager.pickNextGmTrack();
                musicManager.playCurrentGm();
                if (sfxDrop != null) sfxDrop.play();
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            board.rotateCW();
            if (sfxRotate != null) sfxRotate.play();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            board.hardDrop();
            if (sfxDrop != null) sfxDrop.play();
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
                if (sfxMove != null) sfxMove.play();
            } else {
                moveTimer += dt;
                if (moveTimer >= Constants.DAS_DELAY) {
                    if (dir < 0) board.moveLeft(); else board.moveRight();
                    float repeat = moveTimer - Constants.DAS_DELAY;
                    int count = (int)(repeat / Constants.DAS_REPEAT);
                    int prevCount = (int)((repeat - dt) / Constants.DAS_REPEAT);
                    if (count > prevCount && sfxMove != null) sfxMove.play();
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
        font.dispose();
        bigFont.dispose();
        for (Texture t : blockTextures) if (t != null) t.dispose();
        if (ghostTexture != null) ghostTexture.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (splashTexture != null) splashTexture.dispose();
        if (pixel != null) pixel.dispose();
        backgroundManager.dispose();
        musicManager.dispose();
        if (sfxMove != null) sfxMove.dispose();
        if (sfxRotate != null) sfxRotate.dispose();
        if (sfxDrop != null) sfxDrop.dispose();
        if (sfxSoftDrop != null) sfxSoftDrop.dispose();
        for (Sound s : sfxClear) if (s != null) s.dispose();
        if (sfxGameOver != null) sfxGameOver.dispose();
    }
}
