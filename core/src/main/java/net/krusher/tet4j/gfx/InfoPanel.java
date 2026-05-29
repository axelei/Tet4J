package net.krusher.tet4j.gfx;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.Constants;
import net.krusher.tet4j.entities.Board;
import net.krusher.tet4j.entities.Tetromino;

public final class InfoPanel {
    private static float swingTimer;

    public static void update(float dt) {
        swingTimer += dt;
    }

    public static void drawPreview(SpriteBatch batch, Board board) {
        int px = Constants.INFO_X;
        int py = Constants.BOARD_Y + Constants.BOARD_PX_H - Constants.INFO_PREVIEW_TOP_OFFSET;

        TextRenderer.drawTextWithBg(batch, Assets.font, "NEXT", px, py);
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

    public static void drawUI(SpriteBatch batch, Board board) {
        int px = Constants.INFO_X;
        int py = Constants.BOARD_Y + Constants.BOARD_PX_H - Constants.INFO_UI_TOP_OFFSET;

        TextRenderer.drawTextWithBg(batch, Assets.font, "SCORE", px, py); py -= Constants.INFO_LABEL_VALUE_GAP;
        TextRenderer.drawTextWithBg(batch, Assets.bigFont, String.valueOf(board.score), px, py); py -= Constants.INFO_VALUE_LABEL_GAP;
        TextRenderer.drawTextWithBg(batch, Assets.font, "LINES", px, py); py -= Constants.INFO_LABEL_VALUE_GAP;
        TextRenderer.drawTextWithBg(batch, Assets.bigFont, String.valueOf(board.lines), px, py); py -= Constants.INFO_VALUE_LABEL_GAP;
        TextRenderer.drawTextWithBg(batch, Assets.font, "LEVEL", px, py); py -= Constants.INFO_LABEL_VALUE_GAP;
        TextRenderer.drawTextWithBg(batch, Assets.bigFont, String.valueOf(board.level), px, py);

        TextRenderer.drawTextWithBg(batch, Assets.font, "ARROWS: Move/Rotate", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST);
        TextRenderer.drawTextWithBg(batch, Assets.font, "SPACE: Hard Drop", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST - Constants.INFO_KEY_LABEL_STEP);
        TextRenderer.drawTextWithBg(batch, Assets.font, "P: Pause", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST - Constants.INFO_KEY_LABEL_STEP * 2);
        TextRenderer.drawTextWithBg(batch, Assets.font, "ESC: Exit", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST - Constants.INFO_KEY_LABEL_STEP * 3);
        if (board.cheatMode) {
            TextRenderer.drawTextWithBg(batch, Assets.font, "CHEATER!!", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST - Constants.INFO_KEY_LABEL_STEP * 3);
        }
    }

    private InfoPanel() {}
}
