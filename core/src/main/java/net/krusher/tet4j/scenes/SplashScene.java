package net.krusher.tet4j.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.entities.Board;
import net.krusher.tet4j.entities.Tetromino;
import net.krusher.tet4j.audio.MusicManager;
import net.krusher.tet4j.gfx.InfoPanel;

public class SplashScene {
    private final SpriteBatch batch;
    private final InfoPanel infoPanel;
    private final MusicManager musicManager;
    private final Board board;
    private boolean finished;

    public SplashScene(SpriteBatch batch, InfoPanel infoPanel, MusicManager musicManager, Board board) {
        this.batch = batch;
        this.infoPanel = infoPanel;
        this.musicManager = musicManager;
        this.board = board;
    }

    public void render() {
        infoPanel.update(Gdx.graphics.getDeltaTime());
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
            finished = true;
            musicManager.stopTitle();
            musicManager.playCurrentGm();
        }
    }

    public boolean isFinished() {
        return finished;
    }
}
