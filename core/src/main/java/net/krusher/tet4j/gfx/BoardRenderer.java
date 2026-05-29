package net.krusher.tet4j.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.entities.Block;
import net.krusher.tet4j.entities.Board;
import net.krusher.tet4j.Constants;
import net.krusher.tet4j.entities.Tetromino;
import net.krusher.tet4j.entities.Tetromino.Type;

public final class BoardRenderer {

    public static void drawBoardBackground(ShapeRenderer shapes) {
        int bx = Constants.BOARD_X;
        int by = Constants.BOARD_Y;
        int bw = Constants.BOARD_PX_W;
        int bh = Constants.BOARD_PX_H;
        int out = Constants.BOARD_BORDER_OUTER;
        int in = Constants.BOARD_BORDER_INNER;
        shapes.begin(ShapeType.Filled);
        shapes.setColor(Constants.BOARD_BORDER_COLOR_DARK);
        shapes.rect(bx - out, by - out, bw + out * 2, out);
        shapes.rect(bx - out, by + bh, bw + out * 2, out);
        shapes.rect(bx - out, by, out, bh);
        shapes.rect(bx + bw, by, out, bh);
        shapes.setColor(Constants.BOARD_BORDER_COLOR_LIGHT);
        shapes.rect(bx - in, by - in, bw + in * 2, in);
        shapes.rect(bx - in, by + bh, bw + in * 2, in);
        shapes.rect(bx - in, by, in, bh);
        shapes.rect(bx + bw, by, in, bh);
        shapes.end();
    }

    public static void drawGame(SpriteBatch batch, Board board, boolean askingExit) {

        batch.begin();
        batch.enableBlending();
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(1, 1, 1, Constants.BG_OPACITY);
        batch.draw(Assets.bgTexture, Constants.BOARD_X, Constants.BOARD_Y, Constants.BOARD_PX_W, Constants.BOARD_PX_H);
        batch.setColor(1, 1, 1, 1);

        if (board.state == Board.State.CLEARING) {
            drawClearingAnimation(batch, board);
        } else {
            drawLockedBlocks(batch, board);
            drawGhost(batch, board);
            drawCurrentPiece(batch, board);
        }
        if (!ParticleSystem.isEmpty()) {
            ParticleSystem.draw(batch);
        }

        InfoPanel.drawPreview(batch, board);
        InfoPanel.drawUI(batch, board);
        if (board.state == Board.State.PAUSED && !askingExit) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.setColor(0, 0, 0, Constants.PAUSE_OVERLAY_ALPHA);
            batch.draw(Assets.pixel, Constants.BOARD_X, Constants.BOARD_Y, Constants.BOARD_PX_W, Constants.BOARD_PX_H);
            batch.setColor(1, 1, 1, 1);
            Assets.bigFont.draw(batch, "PAUSED", Constants.BOARD_X + Constants.PAUSED_LABEL_X, Constants.BOARD_Y + Constants.BOARD_PX_H / 2f + Constants.PAUSED_LABEL_Y);
        }
        batch.end();
    }

    private static void drawBlock(SpriteBatch batch, Texture tex, float x, float y, float rotation, boolean drawRelief) {
        float half = Constants.BLOCK_SIZE / 2f;
        batch.draw(tex, x, y, half, half, Constants.BLOCK_SIZE, Constants.BLOCK_SIZE,
            1, 1, rotation, 0, 0, tex.getWidth(), tex.getHeight(), false, false);
        if (drawRelief) {
            batch.draw(Assets.relief, x, y, Constants.BLOCK_SIZE, Constants.BLOCK_SIZE);
        }
    }

    private static void drawLockedBlocks(SpriteBatch batch, Board board) {
        for (int r = 2; r < Constants.BOARD_ROWS; r++) {
            for (int c = 0; c < Constants.BOARD_COLS; c++) {
                Block block = board.grid[r][c];
                if (block != null) {
                    float x = Constants.BOARD_X + c * Constants.BLOCK_SIZE;
                    float y = Constants.BOARD_Y + (Constants.BOARD_VISIBLE_ROWS - 1 - (r - 2)) * Constants.BLOCK_SIZE;
                    drawBlock(batch, block.texture(), x, y, block.rotation() * 90f, true);
                }
            }
        }
    }

    private static void drawPiece(SpriteBatch batch, Type type, int rotation, float x, float y, Texture tex, boolean drawRelief) {
        if (type == null) {
            return;
        }
        int[][] shape = Tetromino.getShape(type, rotation);
        float rotAngle = rotation * 90f;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (shape[r][c] != 0) {
                    float gr = y + r;
                    float gc = x + c;
                    float screenR = Constants.BOARD_VISIBLE_ROWS - 1f - (gr - 2f);
                    if (screenR < 0f) {
                        continue;
                    }
                    drawBlock(batch, tex, Constants.BOARD_X + gc * Constants.BLOCK_SIZE, Constants.BOARD_Y + screenR * Constants.BLOCK_SIZE, rotAngle, drawRelief);
                }
            }
        }
    }

    private static void drawGhost(SpriteBatch batch, Board board) {
        if (board.currentType == null || board.state != Board.State.PLAYING) {
            return;
        }
        int gy = board.getGhostY();
        if (gy == board.currentY) {
            return;
        }
        drawPiece(batch, board.currentType, board.currentRotation, board.visualX, gy, Assets.ghostTexture, false);
    }

    private static void drawCurrentPiece(SpriteBatch batch, Board board) {
        if (board.currentType == null || board.state != Board.State.PLAYING) {
            return;
        }
        drawPiece(batch, board.currentType, board.currentRotation, board.visualX, board.visualY,
            Assets.blockTextures[board.currentType.ordinal()], true);
    }

    private static void drawClearingAnimation(SpriteBatch batch, Board board) {
        float flashEnd = Constants.CLEAR_DURATION * Constants.CLEARING_SLIDE_START;
        float slideDuration = Constants.CLEAR_DURATION * (1 - Constants.CLEARING_SLIDE_START);

        int shifted = 0;
        int[] rowShift = new int[Constants.BOARD_ROWS];
        for (int r = Constants.BOARD_ROWS - 1; r >= 0; r--) {
            if (board.clearedRows[r]) {
                shifted++;
            }
            rowShift[r] = shifted;
        }

        for (int r = 2; r < Constants.BOARD_ROWS; r++) {
            if (board.clearedRows[r]) {
                continue;
            }
            for (int c = 0; c < Constants.BOARD_COLS; c++) {
                Block block = board.grid[r][c];
                if (block == null) {
                    continue;
                }
                float elapsed = Math.max(0, board.clearTimer - flashEnd - board.fallDelays[r][c]);
                float t = Math.min(elapsed / slideDuration, 1f);
                t = t * t * (3 - 2 * t);
                float x = Constants.BOARD_X + c * Constants.BLOCK_SIZE;
                float y = Constants.BOARD_Y + (Constants.BOARD_VISIBLE_ROWS - 1 - (r - 2) - rowShift[r] * t) * Constants.BLOCK_SIZE;
                drawBlock(batch, block.texture(), x, y, block.rotation() * 90f, true);
            }
        }
    }

    private BoardRenderer() {}
}
