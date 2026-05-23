package net.krusher.tet4j;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import net.krusher.tet4j.Tetromino.Type;

public class Main extends ApplicationAdapter {
    private static final int BLOCK_SIZE = 32;
    private static final int INFO_X = 80;
    private static final int BOARD_X = 420;
    private static final int BOARD_Y = 40;
    private static final int BOARD_PX_W = Board.COLS * BLOCK_SIZE;
    private static final int BOARD_PX_H = Board.VISIBLE_ROWS * BLOCK_SIZE;

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private Board board;
    private BitmapFont font, bigFont;
    private OrthographicCamera camera;
    private Texture pixel;
    private GlyphLayout glyphLayout = new GlyphLayout();

    private Texture[] blockTextures;
    private Texture ghostTexture, bgTexture, splashTexture;
    private Texture[] levelBgTextures;
    private Texture[] masterBgTextures;
    private Texture currentBgTex, prevBgTex;
    private boolean showSplash = true;

    private int prevLevel = -1;
    private float bgFadeTimer = -1;
    private static final float BG_FADE_DURATION = 0.8f;
    private static final int NUM_LEVEL_BG = 10;
    private static final float GM_VOLUME = 0.75f;
    private static final float GM_VOLUME_PAUSED = GM_VOLUME * 0.5f;

    private Sound sfxMove, sfxRotate, sfxDrop, sfxGameOver;
    private Sound[] sfxClear = new Sound[4];
    private Music titleMusic;
    private MusicMetadata titleMusicMeta;
    private java.util.List<Music> gameplayMusic;
    private Music currentGm;
    private java.util.Map<Music, MusicMetadata> musicMeta = new java.util.HashMap<>();
    private String toastText;
    private float toastTimer = -1;
    private static final float TOAST_DURATION = 4f;

    private float moveTimer, dropTimer;
    private static final float DAS_DELAY = 0.17f;
    private static final float DAS_REPEAT = 0.05f;
    private int moveDir;

    private static class Particle {
        float x, y, vx, vy, rotation, rotSpeed;
        Texture texture;
        float age;
    }
    private java.util.List<Particle> particles = new java.util.ArrayList<>();

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        FreeTypeFontGenerator fontGen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/pixel.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = 16;
        font = fontGen.generateFont(param);
        param.size = 32;
        bigFont = fontGen.generateFont(param);
        fontGen.dispose();

        sfxMove = Gdx.audio.newSound(Gdx.files.internal("sounds/move.wav"));
        sfxRotate = Gdx.audio.newSound(Gdx.files.internal("sounds/rotate.wav"));
        sfxDrop = Gdx.audio.newSound(Gdx.files.internal("sounds/drop.wav"));
        for (int i = 0; i < 4; i++)
            sfxClear[i] = Gdx.audio.newSound(Gdx.files.internal("sounds/clear" + (i + 1) + ".wav"));
        sfxGameOver = Gdx.audio.newSound(Gdx.files.internal("sounds/gameover.wav"));

        loadTextures();
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();
        splashTexture = new Texture(Gdx.files.internal("graphics/splash.jpg"));
        levelBgTextures = new Texture[NUM_LEVEL_BG];
        for (int i = 0; i < NUM_LEVEL_BG; i++) {
            levelBgTextures[i] = new Texture(Gdx.files.internal("backgrounds/level" + (i + 1) + ".jpg"));
        }
        java.util.List<Texture> masterList = new java.util.ArrayList<>();
        for (com.badlogic.gdx.files.FileHandle f : Gdx.files.internal("backgrounds/master").list()) {
            String n = f.name().toLowerCase();
            if (n.endsWith(".jpg") || n.endsWith(".png")) masterList.add(new Texture(f));
        }
        masterBgTextures = masterList.toArray(new Texture[0]);
        currentBgTex = levelBgTextures[0];
        board = new Board();
        for (com.badlogic.gdx.files.FileHandle f : Gdx.files.internal("music/title").list()) {
            String n = f.name().toLowerCase();
            if (n.endsWith(".mp3") || n.endsWith(".ogg")) {
                titleMusic = Gdx.audio.newMusic(f);
                titleMusicMeta = MusicMetadata.fromFile(f);
                break;
            }
        }
        gameplayMusic = new java.util.ArrayList<>();
        for (com.badlogic.gdx.files.FileHandle f : Gdx.files.internal("music/gameplay").list()) {
            String n = f.name().toLowerCase();
            if (n.endsWith(".mp3") || n.endsWith(".ogg")) {
                Music m = Gdx.audio.newMusic(f);
                gameplayMusic.add(m);
                musicMeta.put(m, MusicMetadata.fromFile(f));
            }
        }
        pickNextGmTrack();
    }

    private void loadTextures() {
        String[] blockFiles = {"block_i.png", "block_o.png", "block_t.png",
            "block_s.png", "block_z.png", "block_j.png", "block_l.png"};
        blockTextures = new Texture[7];
        for (int i = 0; i < 7; i++) {
            blockTextures[i] = new Texture(Gdx.files.internal("graphics/" + blockFiles[i]));
        }
        ghostTexture = new Texture(Gdx.files.internal("graphics/ghost.png"));
        bgTexture = new Texture(Gdx.files.internal("graphics/bg.png"));
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.14f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        float dt = Gdx.graphics.getDeltaTime();
        if (toastTimer >= 0) {
            toastTimer += dt;
            if (toastTimer > TOAST_DURATION) toastTimer = -1;
        }

        if (showSplash) {
            drawSplash();
            if (!titleMusic.isPlaying()) {
                titleMusic.play();
                toastText = titleMusicMeta.title + "\n" + titleMusicMeta.artist + "\n" + titleMusicMeta.license;
                toastTimer = 0;
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
            drawToast();
            if (Gdx.input.isKeyPressed(Input.Keys.ANY_KEY) || Gdx.input.isTouched()) {
                showSplash = false;
                titleMusic.stop();
                if (currentGm != null) {
                    MusicMetadata meta = musicMeta.get(currentGm);
                    if (meta != null) {
                        toastText = meta.title + "\n" + meta.artist + "\n" + meta.license;
                        toastTimer = 0;
                    }
                    currentGm.play();
                }
            }
            return;
        }

        if (board.justCleared) {
            int idx = Math.min(Math.max(board.linesCleared, 1), 4) - 1;
            if (sfxClear[idx] != null) sfxClear[idx].play();
            board.justCleared = false;
            spawnClearingParticles();
        }

        handleInput(dt);
        board.update(dt);
        updateClearingParticles(dt);

        if (board.state == Board.State.GAME_OVER) {
            if (currentGm != null && currentGm.isPlaying()) currentGm.stop();
        } else if (board.state == Board.State.PLAYING) {
            if (currentGm != null) {
                currentGm.setVolume(GM_VOLUME);
                if (!currentGm.isPlaying()) {
                    pickNextGmTrack();
                    if (currentGm != null) currentGm.play();
                }
            }
        } else if (board.state == Board.State.PAUSED) {
            if (currentGm != null) currentGm.setVolume(GM_VOLUME_PAUSED);
        }

        updateLevelBackground(dt);
        drawLevelBackground();
        drawBoardBackground();
        drawGame();
        if (board.state == Board.State.GAME_OVER) drawGameOver();
        if (toastTimer >= 0) drawToast();
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
                particles.clear();
                prevLevel = -1;
                currentBgTex = levelBgTextures[0];
                prevBgTex = null;
                bgFadeTimer = -1;
                pickNextGmTrack();
                if (currentGm != null) currentGm.play();
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
        Texture tex;
        if (board.level < NUM_LEVEL_BG) {
            tex = levelBgTextures[board.level];
        } else {
            tex = masterBgTextures[(int)(Math.random() * masterBgTextures.length)];
        }

        if (tex != currentBgTex) {
            prevBgTex = currentBgTex;
            currentBgTex = tex;
            bgFadeTimer = 0;
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

        if (bgFadeTimer >= 0 && prevBgTex != null) {
            float alpha = bgFadeTimer / BG_FADE_DURATION;
            batch.setColor(1, 1, 1, 1 - alpha);
            batch.draw(prevBgTex, 0, 0, 1280, 720);
            batch.setColor(1, 1, 1, alpha);
        }
        batch.draw(currentBgTex, 0, 0, 1280, 720);
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
        if (!particles.isEmpty()) drawClearingParticles();

        drawPreview();
        drawUI();
        if (board.state == Board.State.PAUSED) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.setColor(0, 0, 0, 0.6f);
            batch.draw(pixel, BOARD_X, BOARD_Y, BOARD_PX_W, BOARD_PX_H);
            batch.setColor(1, 1, 1, 1);
            bigFont.draw(batch, "PAUSED", BOARD_X + 40, BOARD_Y + BOARD_PX_H / 2f + 16);
        }
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

    private void spawnClearingParticles() {
        particles.clear();
        for (int r = 2; r < Board.ROWS; r++) {
            if (!board.clearedRows[r]) continue;
            for (int c = 0; c < Board.COLS; c++) {
                int val = board.grid[r][c];
                if (val == 0) continue;
                Particle p = new Particle();
                p.x = BOARD_X + c * BLOCK_SIZE;
                p.y = BOARD_Y + (Board.VISIBLE_ROWS - 1 - (r - 2)) * BLOCK_SIZE;
                float angle = (float)(Math.PI * 0.25 + Math.random() * Math.PI * 0.5);
                float speed = 300 + (float)(Math.random() * 400);
                p.vx = (float)Math.cos(angle) * speed;
                p.vy = (float)Math.sin(angle) * speed;
                p.rotation = (float)(Math.random() * 360);
                p.rotSpeed = (float)(Math.random() * 720 - 360);
                p.texture = blockTextures[val - 1];
                particles.add(p);
            }
        }
    }

    private void updateClearingParticles(float dt) {
        float maxAge = Board.CLEAR_DURATION * 2;
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.age += dt;
            if (p.age >= maxAge) { particles.remove(i); continue; }
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.vy -= 80 * dt;
            p.rotation += p.rotSpeed * dt;
        }
    }

    private void drawClearingAnimation() {
        float progress = board.clearTimer / Board.CLEAR_DURATION;
        float slideProgress = Math.max(0, (progress - 0.35f) / 0.65f);
        slideProgress = slideProgress * slideProgress * (3 - 2 * slideProgress);

        int shifted = 0;
        int[] rowShift = new int[Board.ROWS];
        for (int r = Board.ROWS - 1; r >= 0; r--) {
            if (board.clearedRows[r]) shifted++;
            rowShift[r] = shifted;
        }

        for (int r = 2; r < Board.ROWS; r++) {
            if (board.clearedRows[r]) continue;
            for (int c = 0; c < Board.COLS; c++) {
                int val = board.grid[r][c];
                if (val == 0) continue;
                batch.draw(blockTextures[val - 1],
                    BOARD_X + c * BLOCK_SIZE,
                    BOARD_Y + (Board.VISIBLE_ROWS - 1 - (r - 2) - rowShift[r] * slideProgress) * BLOCK_SIZE);
            }
        }

    }

    private void drawClearingParticles() {
        float maxAge = Board.CLEAR_DURATION * 2;
        for (Particle p : particles) {
            float a = Math.max(0, 1 - p.age / maxAge);
            batch.setColor(1, 1, 1, a);
            batch.draw(p.texture, p.x, p.y, BLOCK_SIZE / 2f, BLOCK_SIZE / 2f,
                BLOCK_SIZE, BLOCK_SIZE, 1, 1, p.rotation, 0, 0, BLOCK_SIZE, BLOCK_SIZE, false, false);
        }
        batch.setColor(1, 1, 1, 1);
    }

    private void drawGhost() {
        if (board.currentType == null || board.state != Board.State.PLAYING) return;
        int gy = board.getGhostY();
        if (gy == board.currentY) return;
        drawPiece(board.currentType, board.currentRotation, board.currentX, gy, ghostTexture);
    }

    private void pickNextGmTrack() {
        if (currentGm != null) currentGm.stop();
        if (gameplayMusic.isEmpty()) { currentGm = null; return; }
        currentGm = gameplayMusic.get((int)(Math.random() * gameplayMusic.size()));
        MusicMetadata meta = musicMeta.get(currentGm);
        if (meta != null) {
            toastText = meta.title + "\n" + meta.artist + "\n" + meta.license;
            toastTimer = 0;
        }
    }

    private void drawToast() {
        if (toastText == null || toastTimer < 0) return;

        float alpha = 1f;
        if (toastTimer < 0.3f) {
            alpha = toastTimer / 0.3f;
        } else if (toastTimer > TOAST_DURATION - 1f) {
            alpha = (TOAST_DURATION - toastTimer) / 1f;
        }

        batch.begin();
        float maxW = 320;
        float padX = 6;
        float padY = 6;
        glyphLayout.setText(font, toastText, com.badlogic.gdx.graphics.Color.WHITE, maxW,
            com.badlogic.gdx.utils.Align.left, true);
        float tw = maxW;
        float th = glyphLayout.height + padY * 2;
        float tx = 1280 - tw - 10;
        float ty = 10 + th;

        batch.setColor(0, 0, 0, 0.75f * alpha);
        batch.draw(pixel, tx, ty - th, tw, th);
        batch.setColor(1, 1, 1, alpha);
        font.draw(batch, glyphLayout, tx + padX, ty - padY);
        batch.setColor(1, 1, 1, 1);
        batch.end();
    }

    private void drawTextBg(String text, float x, float y, BitmapFont fnt) {
        glyphLayout.setText(fnt, text);
        float pad = 4;
        batch.setColor(0, 0, 0, 0.75f);
        batch.draw(pixel, x - pad, y - glyphLayout.height - pad,
            glyphLayout.width + pad * 2, glyphLayout.height + pad * 2);
        batch.setColor(1, 1, 1, 1);
    }

    private void drawPreview() {
        int px = INFO_X;
        int py = BOARD_Y + BOARD_PX_H - 20;

        drawTextBg("NEXT", px, py, font);
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

        drawTextBg("SCORE", px, py, font);
        font.draw(batch, "SCORE", px, py); py -= 22;
        drawTextBg(String.valueOf(board.score), px, py, font);
        font.draw(batch, String.valueOf(board.score), px, py); py -= 35;
        drawTextBg("LINES", px, py, font);
        font.draw(batch, "LINES", px, py); py -= 22;
        drawTextBg(String.valueOf(board.lines), px, py, font);
        font.draw(batch, String.valueOf(board.lines), px, py); py -= 35;
        drawTextBg("LEVEL", px, py, font);
        font.draw(batch, "LEVEL", px, py); py -= 22;
        drawTextBg(String.valueOf(board.level), px, py, font);
        font.draw(batch, String.valueOf(board.level), px, py);

        drawTextBg("ARROWS: Move/Rotate", INFO_X, 78, font);
        font.draw(batch, "ARROWS: Move/Rotate", INFO_X, 78);
        drawTextBg("SPACE: Hard Drop", INFO_X, 60, font);
        font.draw(batch, "SPACE: Hard Drop", INFO_X, 60);
        drawTextBg("P: Pause", INFO_X, 42, font);
        font.draw(batch, "P: Pause", INFO_X, 42);
        if (board.cheatMode) {
            drawTextBg("CHEATER!!", INFO_X, 24, font);
            font.draw(batch, "CHEATER!!", INFO_X, 24);
        }
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
        if (pixel != null) pixel.dispose();
        if (levelBgTextures != null) for (Texture t : levelBgTextures) if (t != null) t.dispose();
        if (masterBgTextures != null) for (Texture t : masterBgTextures) if (t != null) t.dispose();
        if (sfxMove != null) sfxMove.dispose();
        if (sfxRotate != null) sfxRotate.dispose();
        if (sfxDrop != null) sfxDrop.dispose();
        for (Sound s : sfxClear) if (s != null) s.dispose();
        if (sfxGameOver != null) sfxGameOver.dispose();
        if (titleMusic != null) titleMusic.dispose();
        if (gameplayMusic != null) for (Music m : gameplayMusic) if (m != null) m.dispose();
    }
}
