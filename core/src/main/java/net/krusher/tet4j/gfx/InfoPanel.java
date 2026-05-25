package net.krusher.tet4j.gfx;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.BuildInfo;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import net.krusher.tet4j.entities.Board;
import net.krusher.tet4j.Constants;
import net.krusher.tet4j.entities.Tetromino;

public class InfoPanel {
    private static final long START_TIME = System.currentTimeMillis();
    private static final Random rng = new Random();
    private static final int FALLING_COUNT = 15;
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private float swingTimer;

    private static class FallingPiece {
        float x, y, size, speed;
        int type, rotation;
    }

    private final ArrayList<FallingPiece> fallingPieces = new ArrayList<>();
    private final ArrayList<Particle> splashParticles = new ArrayList<>();
    private float burstTimer = 8f + rng.nextFloat() * 4f;

    public InfoPanel() {
        for (int i = 0; i < FALLING_COUNT; i++) {
            FallingPiece p = new FallingPiece();
            resetFallingPiece(p);
            p.y = rng.nextFloat() * Constants.SCREEN_HEIGHT;
            p.x = rng.nextFloat() * (Constants.SCREEN_WIDTH + 200f) - 100f;
            fallingPieces.add(p);
        }
    }

    private void resetFallingPiece(FallingPiece p) {
        p.x = rng.nextFloat() * (Constants.SCREEN_WIDTH + 200f) - 100f;
        p.y = Constants.SCREEN_HEIGHT + 50f + rng.nextFloat() * 100f;
        p.size = 20f + rng.nextFloat() * 40f;
        p.speed = 30f + rng.nextFloat() * 60f;
        p.type = rng.nextInt(7);
        p.rotation = rng.nextInt(4);
    }

    public void update(float dt) {
        swingTimer += dt;
        for (FallingPiece p : fallingPieces) {
            p.y -= p.speed * dt;
            if (p.y + p.size * 4f < -50f) {
                resetFallingPiece(p);
            }
        }
        burstTimer -= dt;
        if (burstTimer <= 0) {
            burstTimer = 8f + rng.nextFloat() * 4f;
            FallingPiece target = null;
            int visibleCount = 0;
            for (FallingPiece fp : fallingPieces) {
                if (fp.y > -50f && fp.y < Constants.SCREEN_HEIGHT + 50f) {
                    visibleCount++;
                    if (rng.nextInt(visibleCount) == 0) target = fp;
                }
            }
            if (target != null) {
                burstFallingPiece(target);
                resetFallingPiece(target);
            }
        }
        for (int i = splashParticles.size() - 1; i >= 0; i--) {
            Particle p = splashParticles.get(i);
            p.age += dt;
            float maxAge = Constants.CLEAR_DURATION * Constants.PARTICLE_MAX_AGE_MULTIPLIER;
            if (p.age >= maxAge) {
                splashParticles.remove(i);
                continue;
            }
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.vy -= Constants.PARTICLE_GRAVITY * dt;
            p.rotation += p.rotSpeed * dt;
        }
    }

    private void burstFallingPiece(FallingPiece piece) {
        int[][] shape = Tetromino.SHAPES[piece.type][piece.rotation];
        float bs = piece.size;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (shape[r][c] != 0) {
                    float px = piece.x + c * bs + bs / 2f;
                    float py = piece.y - r * bs + bs / 2f;
                    splashParticles.add(new Particle(px, py, Assets.blockTextures[piece.type], bs));
                }
            }
        }
    }

    public void drawTextWithBg(SpriteBatch batch, BitmapFont font, String text, float x, float y) {
        drawTextWithBg(batch, font, text, x, y, Constants.TEXT_BG_PAD);
    }

    public void drawTextWithBg(SpriteBatch batch, BitmapFont font, String text, float x, float y, float extraBottomPad) {
        glyphLayout.setText(font, text);
        float botPad = Constants.TEXT_BG_PAD + extraBottomPad;
        batch.setColor(0, 0, 0, Constants.TEXT_BG_ALPHA);
        batch.draw(Assets.pixel, x - Constants.TEXT_BG_PAD, y - glyphLayout.height - botPad,
            glyphLayout.width + Constants.TEXT_BG_PAD * 2, glyphLayout.height + Constants.TEXT_BG_PAD + botPad);
        batch.setColor(1, 1, 1, 1);
        font.draw(batch, text, x, y);
    }

    public void drawPreview(SpriteBatch batch, BitmapFont font, Board board) {
        int px = Constants.INFO_X;
        int py = Constants.BOARD_Y + Constants.BOARD_PX_H - Constants.INFO_PREVIEW_TOP_OFFSET;

        drawTextWithBg(batch, font, "NEXT", px, py);
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

    public void drawUI(SpriteBatch batch, BitmapFont font, BitmapFont bigFont, Board board) {
        int px = Constants.INFO_X;
        int py = Constants.BOARD_Y + Constants.BOARD_PX_H - Constants.INFO_UI_TOP_OFFSET;

        drawTextWithBg(batch, font, "SCORE", px, py); py -= Constants.INFO_LABEL_VALUE_GAP;
        drawTextWithBg(batch, bigFont, String.valueOf(board.score), px, py); py -= Constants.INFO_VALUE_LABEL_GAP;
        drawTextWithBg(batch, font, "LINES", px, py); py -= Constants.INFO_LABEL_VALUE_GAP;
        drawTextWithBg(batch, bigFont, String.valueOf(board.lines), px, py); py -= Constants.INFO_VALUE_LABEL_GAP;
        drawTextWithBg(batch, font, "LEVEL", px, py); py -= Constants.INFO_LABEL_VALUE_GAP;
        drawTextWithBg(batch, bigFont, String.valueOf(board.level), px, py);

        drawTextWithBg(batch, font, "ARROWS: Move/Rotate", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST);
        drawTextWithBg(batch, font, "SPACE: Hard Drop", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST - Constants.INFO_KEY_LABEL_STEP);
        drawTextWithBg(batch, font, "P: Pause", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST - Constants.INFO_KEY_LABEL_STEP * 2);
        drawTextWithBg(batch, font, "ESC: Exit", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST - Constants.INFO_KEY_LABEL_STEP * 3);
        if (board.cheatMode) {
            drawTextWithBg(batch, font, "CHEATER!!", Constants.INFO_X, Constants.INFO_KEY_LABEL_FIRST - Constants.INFO_KEY_LABEL_STEP * 3);
        }
    }

    public void drawSplash(SpriteBatch batch, BitmapFont bigFont, BitmapFont font) {
        batch.begin();
        batch.draw(Assets.splashTexture, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        batch.setColor(1, 1, 1, 0.8f);
        for (FallingPiece p : fallingPieces) {
            int[][] shape = Tetromino.SHAPES[p.type][p.rotation];
            Texture tex = Assets.blockTextures[p.type];
            float bs = p.size;
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    if (shape[r][c] != 0) {
                        batch.draw(tex, p.x + c * bs, p.y - r * bs, bs, bs);
                        batch.draw(Assets.relief, p.x + c * bs, p.y - r * bs, bs, bs);
                    }
                }
            }
        }
        float particleMaxAge = Constants.CLEAR_DURATION * Constants.PARTICLE_MAX_AGE_MULTIPLIER;
        for (Particle p : splashParticles) {
            float a = Math.max(0, 1 - p.age / particleMaxAge);
            batch.setColor(1, 1, 1, a);
            float hs = p.size / 2f;
            batch.draw(p.texture, p.x - hs, p.y - hs, hs, hs, p.size, p.size, 1, 1, p.rotation, 0, 0, p.texture.getWidth(), p.texture.getHeight(), false, false);
            batch.draw(Assets.relief, p.x - hs, p.y - hs, hs, hs, p.size, p.size, 1, 1, p.rotation, 0, 0, Assets.relief.getWidth(), Assets.relief.getHeight(), false, false);
        }
        batch.setColor(1, 1, 1, 1);
        float logoH = Constants.SCREEN_HEIGHT;
        float logoW = logoH * Assets.logoTexture.getWidth() / Assets.logoTexture.getHeight();
        float logoX = (Constants.SCREEN_WIDTH - logoW) / 2f;
        float logoY = 0f;
        ShaderProgram prev = batch.getShader();
        ShaderProgram shader = Assets.glowShader;
        batch.setShader(shader);
        shader.bind();
        shader.setUniformf("u_texelSize", 1f / Assets.logoTexture.getWidth(), 1f / Assets.logoTexture.getHeight());
        float now = (System.currentTimeMillis() - START_TIME) / 1000f;
        shader.setUniformf("u_time", now);
        shader.setUniformf("u_hue", (System.currentTimeMillis() % 20000L) / 1000f * 0.05f);
        batch.draw(Assets.logoTexture, logoX, logoY, logoW, logoH);
        batch.setShader(prev);
        float pushX = Constants.SCREEN_WIDTH / 2f - Constants.SPLASH_TEXT_OFFSET_X;
        drawTextWithBg(batch, bigFont, "push any key", pushX, Constants.SPLASH_TEXT_Y, Constants.TEXT_BG_PAD * 2);
        drawTextWithBg(batch, font, "TET4K " + BuildInfo.VERSION + " by Krusher 2026, licensed under GPL 3", 20, 40, Constants.TEXT_BG_PAD * 2);
        batch.end();
    }

    public void drawGameOver(SpriteBatch batch, ShapeRenderer shapes, BitmapFont bigFont, BitmapFont font) {
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
