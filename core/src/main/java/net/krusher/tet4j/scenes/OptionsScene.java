package net.krusher.tet4j.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.Constants;
import net.krusher.tet4j.Settings;
import net.krusher.tet4j.modes.ModeId;
import net.krusher.tet4j.audio.MusicManager;
import net.krusher.tet4j.entities.Tetromino;
import net.krusher.tet4j.gfx.GraphicsManager;
import net.krusher.tet4j.gfx.SplashBackground;
import net.krusher.tet4j.gfx.TextRenderer;

public class OptionsScene implements Scene {
    private static final String[] OPTIONS = {"Full Screen", "Music", "Sound FX", "Mode", "Exit"};

    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final Settings settings;
    private final SplashScene splashScene;
    private int selected;
    private int cursorPieceType;
    private int cursorRotation;
    private boolean showFullscreen;
    private boolean showMusic;
    private boolean showSfx;
    private int showMode;

    public OptionsScene(SpriteBatch batch, ShapeRenderer shapes, Settings settings, SplashScene splashScene) {
        this.batch = batch;
        this.shapes = shapes;
        this.settings = settings;
        this.splashScene = splashScene;
        randomizeCursor();
    }

    public void reset() {
        selected = 0;
        showFullscreen = settings.isFullscreenEnabled();
        showMusic = settings.isMusicEnabled();
        showSfx = settings.isSoundEffectsEnabled();
        showMode = settings.getGameMode().ordinal();
        randomizeCursor();
    }

    private void applyAndExit() {
        if (showFullscreen != settings.isFullscreenEnabled()) {
            if (showFullscreen) {
                GraphicsManager.setFullscreenMode();
            } else {
                GraphicsManager.setWindowedMode();
            }
            settings.setFullscreenEnabled(showFullscreen);
        }
        settings.save();
        splashScene.reset();
    }

    private void randomizeCursor() {
        cursorPieceType = (int)(Math.random() * 7);
        cursorRotation = (int)(Math.random() * 4);
    }

    @Override
    public void update(float dt) {
        SplashBackground.update(dt);
    }

    @Override
    public void render() {
        SplashBackground.draw(batch);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Constants.OPTIONS_TINT);
        shapes.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        shapes.end();

        batch.begin();

        TextRenderer.drawTextWithBgCentered(batch, Assets.bigFont, "OPTIONS", Constants.OPTIONS_TITLE_Y);

        BitmapFont font = Assets.bigFont;
        for (int i = 0; i < OPTIONS.length; i++) {
            float y = Constants.OPTIONS_FIRST_Y - i * Constants.OPTIONS_STEP;
            String value = valueFor(i);

            TextRenderer.drawTextWithBg(batch, font, OPTIONS[i], Constants.INFO_X + 30, y);
            if (value != null) {
                TextRenderer.drawTextWithBg(batch, font, value, Constants.OPTIONS_VALUE_X, y);
            }
        }

        drawCursor();

        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private String valueFor(int idx) {
        return switch (idx) {
            case 0 -> showFullscreen ? "ON" : "OFF";
            case 1 -> showMusic ? "ON" : "OFF";
            case 2 -> showSfx ? "ON" : "OFF";
            case 3 -> ModeId.values()[showMode].name();
            default -> null;
        };
    }

    private void drawCursor() {
        float y = Constants.OPTIONS_FIRST_Y - selected * Constants.OPTIONS_STEP;
        float x = Constants.INFO_X - 80;

        int[][] shape = Tetromino.SHAPES[cursorPieceType][cursorRotation];
        int ps = 28;
        Texture tex = Assets.blockTextures[cursorPieceType];

        drawPiece(y, x, shape, ps, 0, tex, batch);
    }

    public static void drawPiece(float y, float x, int[][] shape, int ps, float rot, Texture tex, SpriteBatch batch) {
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (shape[r][c] != 0) {
                    batch.draw(tex,
                        x + c * ps, y - r * ps,
                        ps / 2f, ps / 2f,
                        ps, ps,
                        1, 1, rot,
                        0, 0, tex.getWidth(), tex.getHeight(),
                        false, false);
                    batch.draw(Assets.relief, x + c * ps, y - r * ps, ps, ps);
                }
            }
        }
    }

    @Override
    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selected = Math.max(0, selected - 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selected = Math.min(OPTIONS.length - 1, selected + 1);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            toggleCurrent(Input.Keys.LEFT);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            toggleCurrent(Input.Keys.RIGHT);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            if (selected == 4) {
                applyAndExit();
            } else {
                toggleCurrent();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            applyAndExit();
        }
    }

    private void toggleCurrent() {
        toggleCurrent(null);
    }

    private void updateShowMode(Integer key) {
        ModeId[] modes = ModeId.values();
        int numGameModes = modes.length;
        int prev = showMode;
        if (key == Input.Keys.LEFT) {
            showMode = (showMode - 1 + numGameModes) % numGameModes;
        } else {
            showMode = (showMode + 1) % numGameModes;
        }
        if (showMode != prev) {
            settings.setGameMode(modes[showMode]);
        }
    }

    private void toggleCurrent(Integer key) {
        switch (selected) {
            case 0:
                showFullscreen = !showFullscreen;
                break;
            case 1:
                showMusic = !showMusic;
                settings.setMusicEnabled(showMusic);
                if (showMusic) {
                    MusicManager.playTitle();
                } else {
                    MusicManager.stopTitle();
                    MusicManager.stopCurrentGm();
                }
                break;
            case 2:
                showSfx = !showSfx;
                settings.setSoundEffectsEnabled(showSfx);
                break;
            case 3:
                updateShowMode(key);
                break;
        }
    }
}
