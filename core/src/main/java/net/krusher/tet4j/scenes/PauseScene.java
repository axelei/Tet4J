package net.krusher.tet4j.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.entities.Board;
import net.krusher.tet4j.Constants;

public class PauseScene implements Scene {
    private final ShapeRenderer shapes;
    private final SpriteBatch batch;
    private final Board board;
    private boolean askingExit;

    public PauseScene(ShapeRenderer shapes, SpriteBatch batch, Board board) {
        this.shapes = shapes;
        this.batch = batch;
        this.board = board;
    }

    @Override
    public void update(float dt) {}

    @Override
    public void render() {
        if (!askingExit) {
            return;
        }
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

    @Override
    public void handleInput() {
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
    }

    public boolean isAskingExit() {
        return askingExit;
    }
}
