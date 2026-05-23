package net.krusher.tet4j;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import net.krusher.tet4j.Tetromino.Type;

public class Main extends ApplicationAdapter {
    private static final int BLOCK_SIZE = 32;
    private static final int INFO_X = 180;
    private static final int BOARD_X = 615;
    private static final int BOARD_Y = 40;
    private static final int BOARD_PX_W = Board.COLS * BLOCK_SIZE;
    private static final int BOARD_PX_H = Board.VISIBLE_ROWS * BLOCK_SIZE;

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private Board board;
    private BitmapFont font, bigFont;
    private OrthographicCamera camera;

    private Texture[] blockTextures;
    private Texture ghostTexture, bgTexture, splashTexture;
    private Texture[] levelBgTextures;
    private boolean showSplash = true;

    private int prevLevel = -1;
    private int currentBgIdx, prevBgIdx;
    private float bgFadeTimer = -1;
    private static final float BG_FADE_DURATION = 0.8f;
    private static final int NUM_LEVEL_BG = 10;

    private Sound sfxMove, sfxRotate, sfxDrop, sfxClear, sfxGameOver;

    private float moveTimer, dropTimer;
    private static final float DAS_DELAY = 0.17f;
    private static final float DAS_REPEAT = 0.05f;
    private int moveDir;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        FreeTypeFontGenerator fontGen = new FreeTypeFontGenerator(Gdx.files.internal("pixel.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = 16;
        font = fontGen.generateFont(param);
        param.size = 32;
        bigFont = fontGen.generateFont(param);
        fontGen.dispose();

        sfxMove = Gdx.audio.newSound(Gdx.files.internal("move.wav"));
        sfxRotate = Gdx.audio.newSound(Gdx.files.internal("rotate.wav"));
        sfxDrop = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        sfxClear = Gdx.audio.newSound(Gdx.files.internal("clear.wav"));
        sfxGameOver = Gdx.audio.newSound(Gdx.files.internal("gameover.wav"));

        loadTextures();
        splashTexture = new Texture(Gdx.files.internal("splash.jpg"));
        levelBgTextures = new Texture[NUM_LEVEL_BG];
        for (int i = 0; i < NUM_LEVEL_BG; i++) {
            levelBgTextures[i] = new Texture(Gdx.files.internal("level" + (i + 1) + ".jpg"));
        }
        board = new Board();
    }

    private void loadTextures() {
        String[] blockFiles = {"block_i.png", "block_o.png", "block_t.png",
            "block_s.png", "block_z.png", "block_j.png", "block_l.png"};
        blockTextures = new Texture[7];
        for (int i = 0; i < 7; i++) {
            blockTextures[i] = new Texture(Gdx.files.internal(blockFiles[i]));
        }
        ghostTexture = new Texture(Gdx.files.internal("ghost.png"));
        bgTexture = new Texture(Gdx.files.internal("bg.png"));
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.14f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        if (showSplash) {
            drawSplash();
            if (Gdx.input.isKeyPressed(Input.Keys.ANY_KEY) || Gdx.input.isTouched()) {
                showSplash = false;
            }
            return;
        }

        float dt = Gdx.graphics.getDeltaTime();

        if (board.justCleared) {
            if (sfxClear != null) sfxClear.play();
            board.justCleared = false;
        }

        handleInput(dt);
        board.update(dt);

        updateLevelBackground(dt);
        drawLevelBackground();
        drawBoardBackground();
        drawGame();
        if (board.state == Board.State.GAME_OVER) drawGameOver();
    }

    private void handleInput(float dt) {
        if (board.state != Board.State.PLAYING) {
            if (board.state == Board.State.GAME_OVER &&
                (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER))) {
                board.reset();
                prevLevel = -1;
                currentBgIdx = 0;
                prevBgIdx = -1;
                bgFadeTimer = -1;
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
            if (dropTimer >= 0.05f) {
                dropTimer -= 0.05f;
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
                if (moveTimer >= DAS_DELAY) {
                    if (dir < 0) board.moveLeft(); else board.moveRight();
                    float repeat = moveTimer - DAS_DELAY;
                    int count = (int)(repeat / DAS_REPEAT);
                    int prevCount = (int)((repeat - dt) / DAS_REPEAT);
                    if (count > prevCount && sfxMove != null) sfxMove.play();
                }
            }
        } else {
            moveDir = 0;
            moveTimer = 0;
        }
    }

    private void updateLevelBackground(float dt) {
        int level = Math.min(board.level, NUM_LEVEL_BG - 1);
        if (level != prevLevel) {
            prevBgIdx = currentBgIdx;
            currentBgIdx = level;
            bgFadeTimer = 0;
            prevLevel = level;
        }
        if (bgFadeTimer >= 0) {
            bgFadeTimer += dt;
            if (bgFadeTimer >= BG_FADE_DURATION) bgFadeTimer = -1;
        }
    }

    private void drawLevelBackground() {
        batch.begin();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (bgFadeTimer >= 0) {
            float alpha = bgFadeTimer / BG_FADE_DURATION;
            if (prevBgIdx >= 0) {
                batch.setColor(1, 1, 1, 1 - alpha);
                batch.draw(levelBgTextures[prevBgIdx], 0, 0, 1280, 720);
            }
            batch.setColor(1, 1, 1, alpha);
        }
        batch.draw(levelBgTextures[currentBgIdx], 0, 0, 1280, 720);
        batch.setColor(1, 1, 1, 1);

        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.end();
    }

    private void drawBoardBackground() {
        shapes.begin(ShapeType.Filled);
        shapes.setColor(0.05f, 0.05f, 0.08f, 1);
        shapes.rect(BOARD_X - 2, BOARD_Y - 2, BOARD_PX_W + 4, BOARD_PX_H + 4);
        shapes.setColor(0.12f, 0.12f, 0.16f, 1);
        shapes.rect(BOARD_X - 1, BOARD_Y - 1, BOARD_PX_W + 2, BOARD_PX_H + 2);
        shapes.end();
    }

    private void drawGame() {
        batch.begin();
        batch.draw(bgTexture, BOARD_X, BOARD_Y);

        if (board.state == Board.State.CLEARING) {
            drawClearingAnimation();
        } else {
            drawLockedBlocks();
            drawGhost();
            drawCurrentPiece();
        }

        drawPreview();
        drawUI();
        batch.end();
    }

    private void drawLockedBlocks() {
        for (int r = 2; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                int val = board.grid[r][c];
                if (val > 0) {
                    batch.draw(blockTextures[val - 1],
                        BOARD_X + c * BLOCK_SIZE,
                        BOARD_Y + (Board.VISIBLE_ROWS - 1 - (r - 2)) * BLOCK_SIZE);
                }
            }
        }
    }

    private void drawPiece(Type type, int rotation, int x, int y, Texture tex) {
        if (type == null) return;
        int[][] shape = Tetromino.getShape(type, rotation);
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (shape[r][c] != 0) {
                    int gr = y + r;
                    int gc = x + c;
                    if (gr < 2) continue;
                    int screenR = Board.VISIBLE_ROWS - 1 - (gr - 2);
                    if (screenR < 0 || screenR >= Board.VISIBLE_ROWS) continue;
                    batch.draw(tex, BOARD_X + gc * BLOCK_SIZE, BOARD_Y + screenR * BLOCK_SIZE);
                }
            }
        }
    }

    private void drawCurrentPiece() {
        if (board.currentType == null || board.state != Board.State.PLAYING) return;
        drawPiece(board.currentType, board.currentRotation, board.currentX, board.currentY,
            blockTextures[board.currentType.ordinal()]);
    }

    private void drawClearingAnimation() {
        float progress = board.clearTimer / Board.CLEAR_DURATION;
        float shiftProgress = Math.max(0, (progress - 0.2f) / 0.8f);
        shiftProgress = shiftProgress * shiftProgress * (3 - 2 * shiftProgress);

        int shifted = 0;
        int[] rowShift = new int[Board.ROWS];
        for (int r = Board.ROWS - 1; r >= 0; r--) {
            if (board.clearedRows[r]) shifted++;
            rowShift[r] = shifted;
        }

        float flash = (float)Math.abs(Math.sin(progress * (float)Math.PI * 8));

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        for (int r = 2; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                int val = board.grid[r][c];
                if (val == 0) continue;

                float screenY = BOARD_Y + (Board.VISIBLE_ROWS - 1 - (r - 2)) * BLOCK_SIZE;

                if (board.clearedRows[r]) {
                    batch.setColor(1, 1, 1, flash * 0.6f + 0.4f);
                    batch.draw(blockTextures[val - 1],
                        BOARD_X + c * BLOCK_SIZE, screenY);
                    batch.setColor(1, 1, 1, 1);
                } else {
                    screenY -= rowShift[r] * shiftProgress * BLOCK_SIZE;
                    batch.draw(blockTextures[val - 1],
                        BOARD_X + c * BLOCK_SIZE, screenY);
                }
            }
        }
    }

    private void drawGhost() {
        if (board.currentType == null || board.state != Board.State.PLAYING) return;
        int gy = board.getGhostY();
        if (gy == board.currentY) return;
        drawPiece(board.currentType, board.currentRotation, board.currentX, gy, ghostTexture);
    }

    private void drawPreview() {
        int px = INFO_X;
        int py = BOARD_Y + BOARD_PX_H - 20;

        font.draw(batch, "NEXT", px, py);
        py -= 10;

        int[][] shape = Tetromino.getShape(board.nextType, 0);
        int ps = 20;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (shape[r][c] != 0) {
                    batch.draw(blockTextures[board.nextType.ordinal()],
                        px + 90 + c * ps, py - r * ps, ps, ps);
                }
            }
        }
    }

    private void drawUI() {
        int px = INFO_X;
        int py = BOARD_Y + BOARD_PX_H - 120;

        font.draw(batch, "SCORE", px, py); py -= 22;
        font.draw(batch, String.valueOf(board.score), px, py); py -= 35;
        font.draw(batch, "LINES", px, py); py -= 22;
        font.draw(batch, String.valueOf(board.lines), px, py); py -= 35;
        font.draw(batch, "LEVEL", px, py); py -= 22;
        font.draw(batch, String.valueOf(board.level), px, py);

        font.draw(batch, "ARROWS: Move/Rotate", INFO_X, 60);
        font.draw(batch, "SPACE: Hard Drop", INFO_X, 42);
    }

    private void drawSplash() {
        batch.begin();
        batch.draw(splashTexture, 0, 0, 1280, 720);
        bigFont.draw(batch, "push any key", 1280 / 2f - 150, 100);
        batch.end();
    }

    private void drawGameOver() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeType.Filled);
        shapes.setColor(0, 0, 0, 0.65f);
        shapes.rect(BOARD_X, BOARD_Y, BOARD_PX_W, BOARD_PX_H);
        shapes.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        bigFont.draw(batch, "GAME OVER", BOARD_X + 20, BOARD_Y + BOARD_PX_H / 2f + 30);
        font.draw(batch, "Press SPACE to restart", BOARD_X + 45, BOARD_Y + BOARD_PX_H / 2f - 15);
        batch.end();
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
        if (levelBgTextures != null) for (Texture t : levelBgTextures) if (t != null) t.dispose();
        if (sfxMove != null) sfxMove.dispose();
        if (sfxRotate != null) sfxRotate.dispose();
        if (sfxDrop != null) sfxDrop.dispose();
        if (sfxClear != null) sfxClear.dispose();
        if (sfxGameOver != null) sfxGameOver.dispose();
    }
}
