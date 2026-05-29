package net.krusher.tet4j.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.entities.Board;
import net.krusher.tet4j.entities.Tetromino;
import net.krusher.tet4j.audio.MusicManager;
import net.krusher.tet4j.gfx.SplashBackground;

public class SplashScene implements Scene {
    private final SpriteBatch batch;
    private final SplashBackground splashBackground;
    private final Board board;
    private boolean finished;

    public SplashScene(SpriteBatch batch, SplashBackground splashBackground, Board board) {
        this.batch = batch;
        this.splashBackground = splashBackground;
        this.board = board;
    }

    @Override
    public void update(float dt) {
        splashBackground.update(dt);
    }

    @Override
    public void render() {
        splashBackground.draw(batch);
        if (!MusicManager.isTitlePlaying()) {
            MusicManager.playTitle();
            MusicManager.showMusicToast(MusicManager.getTitleMusicMeta());
        }
    }

    @Override
    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            finished = true;
            MusicManager.stopTitle();
            board.reset();
            MusicManager.selectNextTrack();
            MusicManager.playCurrentGm();
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public void reset() {
        finished = false;
    }
}
