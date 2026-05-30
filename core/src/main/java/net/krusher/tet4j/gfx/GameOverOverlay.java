package net.krusher.tet4j.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.Constants;

public final class GameOverOverlay {

    public static void draw(SpriteBatch batch, ShapeRenderer shapes, boolean newBest) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeType.Filled);
        shapes.setColor(0, 0, 0, Constants.GAMEOVER_OVERLAY_ALPHA);
        shapes.rect(Constants.BOARD_X, Constants.BOARD_Y, Constants.BOARD_PX_W, Constants.BOARD_PX_H);
        shapes.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);

        batch.begin();
        Assets.bigFont.draw(batch, "GAME OVER", Constants.BOARD_X + Constants.GAME_OVER_LABEL_X, Constants.BOARD_Y + Constants.BOARD_PX_H / 2f + Constants.TEXT_CENTER_Y_OFFSET_LARGE);
        if (newBest) {
            Assets.bigFont.draw(batch, "NEW BEST!", Constants.BOARD_X + Constants.GAME_OVER_LABEL_X, Constants.BOARD_Y + Constants.BOARD_PX_H / 2f + Constants.GAME_OVER_NEW_BEST_Y);
            Assets.bigFont.draw(batch, "WELL DONE!", Constants.BOARD_X + Constants.GAME_OVER_LABEL_X, Constants.BOARD_Y + Constants.BOARD_PX_H / 2f + Constants.GAME_OVER_NEW_BEST_Y - Constants.OPTIONS_STEP);
        }
        Assets.font.draw(batch, "Press SPACE to restart",
            Constants.BOARD_X + Constants.RESTART_PROMPT_X, Constants.BOARD_Y + Constants.BOARD_PX_H / 2f + Constants.TEXT_CENTER_Y_OFFSET_SMALL);
        batch.end();
    }
    private GameOverOverlay() {}
}
