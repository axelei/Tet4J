package net.krusher.tet4j.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.Board;
import net.krusher.tet4j.Constants;
import net.krusher.tet4j.Tetromino;

public class InfoPanel {
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private float swingTimer;

    public InfoPanel() {
    }

    public void update(float dt) {
        swingTimer += dt;
    }

    public void drawTextBg(SpriteBatch batch, BitmapFont font, String text, float x, float y) {
        glyphLayout.setText(font, text);
        float pad = Constants.TEXT_BG_PAD;
        batch.setColor(0, 0, 0, Constants.TEXT_BG_ALPHA);
        batch.draw(Assets.pixel, x - pad, y - glyphLayout.height - pad,
            glyphLayout.width + pad * 2, glyphLayout.height + pad * 2);
        batch.setColor(1, 1, 1, 1);
    }

    public void drawPreview(SpriteBatch batch, BitmapFont font, Board board) {
        int px = Constants.INFO_X;
        int py = Constants.BOARD_Y + Constants.BOARD_PX_H - Constants.INFO_PREVIEW_TOP_OFFSET;

        drawTextBg(batch, font, "NEXT", px, py);
        font.draw(batch, "NEXT", px, py);
        py -= Constants.INFO_PREVIEW_GAP;

        int[][] shape = Tetromino.getShape(board.nextType, 0);
        int ps = Constants.PREVIEW_BLOCK_SIZE;
        float swing = (float) Math.sin(swingTimer * 2.5) * 4f;
        float rot = (float) Math.sin(swingTimer * 2.5) * 3f;
        int baseX = px + Constants.INFO_PREVIEW_BLOCK_X + (int) swing;
        Texture tex = Assets.blockTextures[board.nextType.ordinal()];
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (shape[r][c] != 0) {
                    batch.draw(tex,
                        baseX + c * ps, py - r * ps,
                        ps / 2f, ps / 2f,
                        ps, ps,
                        1, 1, rot,
                        0, 0, tex.getWidth(), tex.getHeight(),
                        false, false);
                    batch.draw(Assets.relief, baseX + c * ps, py - r * ps, ps, ps);
                }
            }
        }
    }

    public void drawUI(SpriteBatch batch, BitmapFont font, Board board) {
        int px = Constants.INFO_X;
        int py = Constants.BOARD_Y + Constants.BOARD_PX_H - Constants.INFO_UI_TOP_OFFSET;

        drawTextBg(batch, font, "SCORE", px, py);
        font.draw(batch, "SCORE", px, py); py -= Constants.INFO_LABEL_VALUE_GAP;
        drawTextBg(batch, font, String.valueOf(board.score), px, py);
        font.draw(batch, String.valueOf(board.score), px, py); py -= Constants.INFO_VALUE_LABEL_GAP;
        drawTextBg(batch, font, "LINES", px, py);
        font.draw(batch, "LINES", px, py); py -= Constants.INFO_LABEL_VALUE_GAP;
        drawTextBg(batch, font, String.valueOf(board.lines), px, py);
        font.draw(batch, String.valueOf(board.lines), px, py); py -= Constants.INFO_VALUE_LABEL_GAP;
        drawTextBg(batch, font, "LEVEL", px, py);
        font.draw(batch, "LEVEL", px, py); py -= Constants.INFO_LABEL_VALUE_GAP;
        drawTextBg(batch, font, String.valueOf(board.level), px, py);
        font.draw(batch, String.valueOf(board.level), px, py);

        drawTextBg(batch, font, "ARROWS: Move/Rotate", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST);
        font.draw(batch, "ARROWS: Move/Rotate", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST);
        drawTextBg(batch, font, "SPACE: Hard Drop", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST - Constants.INFO_KEY_LABEL_STEP);
        font.draw(batch, "SPACE: Hard Drop", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST - Constants.INFO_KEY_LABEL_STEP);
        drawTextBg(batch, font, "P: Pause", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST - Constants.INFO_KEY_LABEL_STEP * 2);
        font.draw(batch, "P: Pause", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST - Constants.INFO_KEY_LABEL_STEP * 2);
        drawTextBg(batch, font, "ESC: Exit", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST - Constants.INFO_KEY_LABEL_STEP * 3);
        font.draw(batch, "ESC: Exit", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST - Constants.INFO_KEY_LABEL_STEP * 3);
        if (board.cheatMode) {
            drawTextBg(batch, font, "CHEATER!!", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST - Constants.INFO_KEY_LABEL_STEP * 3);
            font.draw(batch, "CHEATER!!", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST - Constants.INFO_KEY_LABEL_STEP * 3);
        }
    }

    public void drawSplash(SpriteBatch batch, BitmapFont bigFont, BitmapFont font) {
        batch.begin();
        batch.draw(Assets.splashTexture, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        bigFont.draw(batch, "push any key", Constants.SCREEN_WIDTH / 2f - Constants.SPLASH_TEXT_OFFSET_X, Constants.SPLASH_TEXT_Y);
        font.draw(batch, "TET4K by Krusher 2026, licensed under GPL 3", 20, 40);
        batch.end();
    }

    public void drawGameOver(SpriteBatch batch, ShapeRenderer shapes, Board board, BitmapFont bigFont, BitmapFont font) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeType.Filled);
        shapes.setColor(0, 0, 0, Constants.GAMEOVER_OVERLAY_ALPHA);
        shapes.rect(Constants.BOARD_X, Constants.BOARD_Y, Constants.BOARD_PX_W, Constants.BOARD_PX_H);
        shapes.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        bigFont.draw(batch, "GAME OVER", Constants.BOARD_X + Constants.GAME_OVER_LABEL_X, Constants.BOARD_Y + Constants.BOARD_PX_H / 2f + Constants.TEXT_CENTER_Y_OFFSET_LARGE);
        font.draw(batch, "Press SPACE to restart", Constants.BOARD_X + Constants.RESTART_PROMPT_X, Constants.BOARD_Y + Constants.BOARD_PX_H / 2f + Constants.TEXT_CENTER_Y_OFFSET_SMALL);
        batch.end();
    }
}
