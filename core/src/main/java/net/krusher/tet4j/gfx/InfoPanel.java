package net.krusher.tet4j.gfx;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.Constants;
import net.krusher.tet4j.entities.Board;
import net.krusher.tet4j.entities.Tetromino;
import net.krusher.tet4j.entities.Tetromino.Type;
import net.krusher.tet4j.scenes.OptionsScene;

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
        OptionsScene.drawPiece(py, baseX, shape, ps, rot, tex, batch);
    }

    public static void drawUI(SpriteBatch batch, Board board, int bestScore) {
        int px = Constants.INFO_X;
        int py = Constants.BOARD_Y + Constants.BOARD_PX_H - Constants.INFO_UI_TOP_OFFSET;

        TextRenderer.drawTextWithBg(batch, Assets.font, "SCORE", px, py); py -= Constants.INFO_LABEL_VALUE_GAP;
        TextRenderer.drawTextWithBg(batch, Assets.bigFont, String.valueOf(board.score), px, py); py -= Constants.INFO_VALUE_LABEL_GAP;
        TextRenderer.drawTextWithBg(batch, Assets.font, "LINES", px, py); py -= Constants.INFO_LABEL_VALUE_GAP;
        TextRenderer.drawTextWithBg(batch, Assets.bigFont, String.valueOf(board.lines), px, py); py -= Constants.INFO_VALUE_LABEL_GAP;
        TextRenderer.drawTextWithBg(batch, Assets.font, "LEVEL", px, py); py -= Constants.INFO_LABEL_VALUE_GAP;
        TextRenderer.drawTextWithBg(batch, Assets.bigFont, String.valueOf(board.level), px, py); py -= Constants.INFO_VALUE_LABEL_GAP;

        TextRenderer.drawTextWithBg(batch, Assets.font, "MODE", px, py); py -= Constants.INFO_LABEL_VALUE_GAP;
        TextRenderer.drawTextWithBg(batch, Assets.bigFont, board.getGameMode().getId().name(), px, py); py -= Constants.INFO_VALUE_LABEL_GAP;

        TextRenderer.drawTextWithBg(batch, Assets.font, "BEST", px, py); py -= Constants.INFO_LABEL_VALUE_GAP;
        TextRenderer.drawTextWithBg(batch, Assets.bigFont, String.valueOf(bestScore), px, py);

        TextRenderer.drawTextWithBg(batch, Assets.font, "ARROWS: Move/Rotate", Constants.INFO_KEY_X_RIGHT, Constants.INFO_KEY_LABEL_TOP);
        TextRenderer.drawTextWithBg(batch, Assets.font, "SPACE: Hard Drop", Constants.INFO_KEY_X_RIGHT, Constants.INFO_KEY_LABEL_TOP - Constants.INFO_KEY_LABEL_STEP);
        TextRenderer.drawTextWithBg(batch, Assets.font, "P: Pause", Constants.INFO_KEY_X_RIGHT, Constants.INFO_KEY_LABEL_TOP - Constants.INFO_KEY_LABEL_STEP * 2);
        TextRenderer.drawTextWithBg(batch, Assets.font, "ESC: Exit", Constants.INFO_KEY_X_RIGHT, Constants.INFO_KEY_LABEL_TOP - Constants.INFO_KEY_LABEL_STEP * 3);
        if (board.cheatMode) {
            TextRenderer.drawTextWithBg(batch, Assets.font, "CHEATER!!", Constants.INFO_KEY_X_RIGHT, Constants.INFO_KEY_LABEL_TOP + Constants.INFO_KEY_LABEL_STEP);
        }

        drawPieceCounts(batch, board);
    }

    private static void drawPieceCounts(SpriteBatch batch, Board board) {
        int ps = Constants.PIECE_COUNT_SIZE;
        int startX = Constants.INFO_KEY_X_RIGHT;
        int yBase = Constants.PIECE_COUNT_Y;
        int pieceW = ps * 4 + 12;
        int pieceH = ps * 4 + 24;
        int colGap = 20;
        int cols = 4;
        int rows = 2;
        int rowH = pieceH + colGap;

        int bgTop = yBase + 36;
        int bgBottom = yBase - (rows - 1) * rowH - 4 * ps - 44;
        int bgW = cols * pieceW + (cols - 1) * colGap + 16;
        int bgH = bgTop - bgBottom;

        batch.setColor(0, 0, 0, 0.5f);
        batch.draw(Assets.pixel, startX - 12, bgBottom, bgW, bgH);
        batch.setColor(1, 1, 1, 1);

        for (int i = 0; i < 7; i++) {
            Type type = Type.values()[i];
            int[][] shape = Tetromino.getShape(type, 0);
            Texture tex = Assets.blockTextures[i];
            int count = board.pieceCounts[i];

            int col = i % cols;
            int row = i / cols;
            int cx = startX + col * (pieceW + colGap);
            int y = yBase - row * rowH;

            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    if (shape[r][c] != 0) {
                        batch.draw(tex, cx + c * ps, y - r * ps, ps / 2f, ps / 2f, ps, ps, 1, 1, 0, 0, 0, tex.getWidth(), tex.getHeight(), false, false);
                        batch.draw(Assets.relief, cx + c * ps, y - r * ps, ps, ps);
                    }
                }
            }
            String stringCount = String.format("%03d", count);
            Assets.font.draw(batch, stringCount, cx, y - 2 * ps - 4);
        }
    }

    private InfoPanel() {}
}
