package net.krusher.tet4j.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import net.krusher.tet4j.Board;
import net.krusher.tet4j.Constants;
import net.krusher.tet4j.Tetromino;
import net.krusher.tet4j.Tetromino.Type;

public class BoardRenderer {
    private final Texture[] blockTextures;
    private final Texture ghostTexture;
    private final Texture bgTexture;
    private final Texture pixel;

    public BoardRenderer(Texture[] blockTextures, Texture ghostTexture, Texture bgTexture, Texture pixel) {
        this.blockTextures = blockTextures;
        this.ghostTexture = ghostTexture;
        this.bgTexture = bgTexture;
        this.pixel = pixel;
    }

    public void drawBoardBackground(ShapeRenderer shapes) {
        shapes.begin(ShapeType.Filled);
        shapes.setColor(Constants.BOARD_BORDER_COLOR_DARK);
        shapes.rect(Constants.BOARD_X - Constants.BOARD_BORDER_OUTER, Constants.BOARD_Y - Constants.BOARD_BORDER_OUTER, Constants.BOARD_PX_W + Constants.BOARD_BORDER_OUTER * 2, Constants.BOARD_PX_H + Constants.BOARD_BORDER_OUTER * 2);
        shapes.setColor(Constants.BOARD_BORDER_COLOR_LIGHT);
        shapes.rect(Constants.BOARD_X - Constants.BOARD_BORDER_INNER, Constants.BOARD_Y - Constants.BOARD_BORDER_INNER, Constants.BOARD_PX_W + Constants.BOARD_BORDER_INNER * 2, Constants.BOARD_PX_H + Constants.BOARD_BORDER_INNER * 2);
        shapes.end();
    }

    public void drawGame(SpriteBatch batch, BitmapFont bigFont, Board board, ParticleSystem particles, InfoPanel infoPanel, BitmapFont font, boolean askingExit) {
        batch.begin();
        batch.draw(bgTexture, Constants.BOARD_X, Constants.BOARD_Y);

        if (board.state == Board.State.CLEARING) {
            drawClearingAnimation(batch, board);
        } else {
            drawLockedBlocks(batch, board);
            drawGhost(batch, board);
            drawCurrentPiece(batch, board);
        }
        if (!particles.isEmpty()) particles.draw(batch);

        infoPanel.drawPreview(batch, font, board, blockTextures);
        infoPanel.drawUI(batch, font, board);
        if (board.state == Board.State.PAUSED && !askingExit) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.setColor(0, 0, 0, Constants.PAUSE_OVERLAY_ALPHA);
            batch.draw(pixel, Constants.BOARD_X, Constants.BOARD_Y, Constants.BOARD_PX_W, Constants.BOARD_PX_H);
            batch.setColor(1, 1, 1, 1);
            bigFont.draw(batch, "PAUSED", Constants.BOARD_X + 40, Constants.BOARD_Y + Constants.BOARD_PX_H / 2f + 16);
        }
        batch.end();
    }

    private void drawLockedBlocks(SpriteBatch batch, Board board) {
        for (int r = 2; r < Constants.BOARD_ROWS; r++) {
            for (int c = 0; c < Constants.BOARD_COLS; c++) {
                int val = board.grid[r][c];
                if (val > 0) {
                    batch.draw(blockTextures[val - 1],
                        Constants.BOARD_X + c * Constants.BLOCK_SIZE,
                        Constants.BOARD_Y + (Constants.BOARD_VISIBLE_ROWS - 1 - (r - 2)) * Constants.BLOCK_SIZE);
                }
            }
        }
    }

    private void drawPiece(SpriteBatch batch, Type type, int rotation, int x, int y, Texture tex) {
        if (type == null) return;
        int[][] shape = Tetromino.getShape(type, rotation);
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (shape[r][c] != 0) {
                    int gr = y + r;
                    int gc = x + c;
                    if (gr < 2) continue;
                    int screenR = Constants.BOARD_VISIBLE_ROWS - 1 - (gr - 2);
                    if (screenR < 0 || screenR >= Constants.BOARD_VISIBLE_ROWS) continue;
                    batch.draw(tex, Constants.BOARD_X + gc * Constants.BLOCK_SIZE, Constants.BOARD_Y + screenR * Constants.BLOCK_SIZE);
                }
            }
        }
    }

    private void drawGhost(SpriteBatch batch, Board board) {
        if (board.currentType == null || board.state != Board.State.PLAYING) return;
        int gy = board.getGhostY();
        if (gy == board.currentY) return;
        drawPiece(batch, board.currentType, board.currentRotation, board.currentX, gy, ghostTexture);
    }

    private void drawCurrentPiece(SpriteBatch batch, Board board) {
        if (board.currentType == null || board.state != Board.State.PLAYING) return;
        drawPiece(batch, board.currentType, board.currentRotation, board.currentX, board.currentY,
            blockTextures[board.currentType.ordinal()]);
    }

    private void drawClearingAnimation(SpriteBatch batch, Board board) {
        float progress = board.clearTimer / Constants.CLEAR_DURATION;
        float slideProgress = Math.max(0, (progress - Constants.CLEARING_SLIDE_START) / (1 - Constants.CLEARING_SLIDE_START));
        slideProgress = slideProgress * slideProgress * (3 - 2 * slideProgress);

        int shifted = 0;
        int[] rowShift = new int[Constants.BOARD_ROWS];
        for (int r = Constants.BOARD_ROWS - 1; r >= 0; r--) {
            if (board.clearedRows[r]) shifted++;
            rowShift[r] = shifted;
        }

        for (int r = 2; r < Constants.BOARD_ROWS; r++) {
            if (board.clearedRows[r]) continue;
            for (int c = 0; c < Constants.BOARD_COLS; c++) {
                int val = board.grid[r][c];
                if (val == 0) continue;
                batch.draw(blockTextures[val - 1],
                    Constants.BOARD_X + c * Constants.BLOCK_SIZE,
                    Constants.BOARD_Y + (Constants.BOARD_VISIBLE_ROWS - 1 - (r - 2) - rowShift[r] * slideProgress) * Constants.BLOCK_SIZE);
            }
        }
    }
}
