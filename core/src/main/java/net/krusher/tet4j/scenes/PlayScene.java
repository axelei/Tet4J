package net.krusher.tet4j.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.entities.Board;
import net.krusher.tet4j.Constants;
import net.krusher.tet4j.Settings;
import net.krusher.tet4j.audio.MusicManager;
import net.krusher.tet4j.gfx.BackgroundManager;
import net.krusher.tet4j.gfx.BoardRenderer;
import net.krusher.tet4j.gfx.InfoPanel;
import net.krusher.tet4j.gfx.ParticleSystem;

public class PlayScene {
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final Board board;
    private final ParticleSystem particleSystem;
    private final BackgroundManager backgroundManager;
    private final MusicManager musicManager;
    private final BoardRenderer boardRenderer;
    private final InfoPanel infoPanel;
    private final Settings settings;

    private float moveTimer, dropTimer;
    private int moveDir;

    public PlayScene(SpriteBatch batch, ShapeRenderer shapes, Board board,
                     ParticleSystem particleSystem, BackgroundManager backgroundManager,
                     MusicManager musicManager, BoardRenderer boardRenderer,
                     InfoPanel infoPanel, Settings settings) {
        this.batch = batch;
        this.shapes = shapes;
        this.board = board;
        this.particleSystem = particleSystem;
        this.backgroundManager = backgroundManager;
        this.musicManager = musicManager;
        this.boardRenderer = boardRenderer;
        this.infoPanel = infoPanel;
        this.settings = settings;
    }

    public void handleInput(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            if (board.rotateCW()) playSfx(Assets.sfxRotate, piecePan());
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            board.hardDrop();
            if (board.state != Board.State.GAME_OVER) {
                playSfx(Assets.sfxDrop, piecePan(board.lockX));
            }
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
                boolean moved = dir < 0 ? board.moveLeft() : board.moveRight();
                if (moved) playSfx(Assets.sfxMove, piecePan());
            } else {
                moveTimer += dt;
                if (moveTimer >= Constants.DAS_DELAY) {
                    boolean moved = dir < 0 ? board.moveLeft() : board.moveRight();
                    float repeat = moveTimer - Constants.DAS_DELAY;
                    int count = (int)(repeat / Constants.DAS_REPEAT);
                    int prevCount = (int)((repeat - dt) / Constants.DAS_REPEAT);
                    if (moved && count > prevCount) playSfx(Assets.sfxMove, piecePan());
                }
            }
        } else {
            moveDir = 0;
            moveTimer = 0;
        }
    }

    public void processEvents(float dt) {
        if (board.justCleared) {
            int idx = Math.clamp(board.linesCleared, 1, 4) - 1;
            playSfx(Assets.sfxClear[idx]);
            board.justCleared = false;
            particleSystem.spawnClearingParticles(board);
        }

        if (board.justLocked) {
            playSfx(Assets.sfxSoftDrop, piecePan(board.lockX));
            board.justLocked = false;
        }

        particleSystem.update(dt);
        infoPanel.update(dt);
        musicManager.updateGameplayMusic(board.state);
        backgroundManager.update(dt, board.level);
    }

    public void render(boolean askingExit) {
        backgroundManager.draw(batch);
        boardRenderer.drawBoardBackground(shapes);
        boardRenderer.drawGame(batch, Assets.bigFont, board, particleSystem, infoPanel, Assets.font, askingExit);
    }

    private void playSfx(Sound sound) {
        if (sound != null && settings.isSoundEffectsEnabled()) sound.play();
    }

    private void playSfx(Sound sound, float pan) {
        if (sound != null && settings.isSoundEffectsEnabled()) sound.play(1f, 1f, pan);
    }

    private float piecePan() {
        return piecePan(board.currentX);
    }

    private float piecePan(float x) {
        float maxCol = Constants.BOARD_COLS - 1f;
        return ((x / maxCol) * 2f - 1f) * Constants.PAN_HARDNESS;
    }
}
