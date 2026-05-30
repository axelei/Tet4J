package net.krusher.tet4j.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.krusher.tet4j.Settings;
import net.krusher.tet4j.audio.MusicManager;
import net.krusher.tet4j.entities.Board;
import net.krusher.tet4j.entities.Tetromino;
import net.krusher.tet4j.modes.GameMode;
import net.krusher.tet4j.gfx.SplashBackground;

import static net.krusher.tet4j.Main.IS_WEB;

public class SplashScene implements Scene {
    private final SpriteBatch batch;
    private final Board board;
    private final Settings settings;
    private boolean finished;
    private boolean optionsRequested;
    private boolean audioStarted;
    private boolean anyKey;

    public SplashScene(SpriteBatch batch, Board board, Settings settings) {
        this.batch = batch;
        this.board = board;
        this.settings = settings;
    }

    @Override
    public void update(float dt) {
        SplashBackground.update(dt);
    }

    @Override
    public void render() {
        SplashBackground.draw(batch);
        if (!MusicManager.isTitlePlaying() && (!IS_WEB || audioStarted)) {
            MusicManager.playTitle();
            MusicManager.showMusicToast(MusicManager.getTitleMusicMeta());
        }
    }

    @Override
    public void handleInput() {
        anyKey |= Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || Gdx.input.justTouched();

        if (IS_WEB && anyKey && !audioStarted) {
            audioStarted = true;
            MusicManager.playTitle();
            MusicManager.showMusicToast(MusicManager.getTitleMusicMeta());
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F12)) {
            board.cheatMode = true;
            board.currentType = Tetromino.Type.I;
            board.nextType = Tetromino.Type.I;
            board.currentX = 3;
            board.currentY = 0;
            board.currentRotation = 0;
            finish();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            optionsRequested = true;
            finished = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            finish();
        }
    }

    private void finish() {
        finished = true;
        if (MusicManager.isTitlePlaying()) {
            MusicManager.stopTitle();
        }
        board.setGameMode(GameMode.forId(settings.getGameMode()));
        board.reset();
        MusicManager.selectNextTrack();
        MusicManager.playCurrentGm();
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isOptionsRequested() {
        return optionsRequested;
    }

    public void reset() {
        finished = false;
        optionsRequested = false;
    }
}
