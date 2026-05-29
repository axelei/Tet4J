package net.krusher.tet4j.gfx;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.BuildInfo;
import net.krusher.tet4j.Constants;
import net.krusher.tet4j.entities.Particle;
import net.krusher.tet4j.entities.Tetromino;

import static net.krusher.tet4j.Main.IS_WEB;

public final class SplashBackground {
    private static final long START_TIME = System.currentTimeMillis();
    private static final Random RNG = new Random();
    private static final int FALLING_COUNT = 15;
    private static final ArrayList<FallingPiece> fallingPieces = new ArrayList<>();
    private static final ArrayList<Particle> splashParticles = new ArrayList<>();
    private static float burstTimer = 8f + RNG.nextFloat() * 4f;

    private static class FallingPiece {
        float x, y, size, speed;
        int type, rotation;
    }

    public static void init() {
        for (int i = 0; i < FALLING_COUNT; i++) {
            FallingPiece p = new FallingPiece();
            resetFallingPiece(p);
            p.y = RNG.nextFloat() * Constants.SCREEN_HEIGHT;
            p.x = RNG.nextFloat() * (Constants.SCREEN_WIDTH + 200f) - 100f;
            fallingPieces.add(p);
        }
    }

    private static void resetFallingPiece(FallingPiece p) {
        p.x = RNG.nextFloat() * (Constants.SCREEN_WIDTH + 200f) - 100f;
        p.y = Constants.SCREEN_HEIGHT + 50f + RNG.nextFloat() * 100f;
        p.size = 20f + RNG.nextFloat() * 40f;
        p.speed = 30f + RNG.nextFloat() * 60f;
        p.type = RNG.nextInt(7);
        p.rotation = RNG.nextInt(4);
    }

    public static void update(float dt) {
        for (FallingPiece p : fallingPieces) {
            p.y -= p.speed * dt;
            if (p.y + p.size * 4f < -50f) {
                resetFallingPiece(p);
            }
        }
        burstTimer -= dt;
        if (burstTimer <= 0) {
            burstTimer = 8f + RNG.nextFloat() * 4f;
            FallingPiece target = null;
            int visibleCount = 0;
            for (FallingPiece fp : fallingPieces) {
                if (fp.y > -50f && fp.y < Constants.SCREEN_HEIGHT + 50f) {
                    visibleCount++;
                    if (RNG.nextInt(visibleCount) == 0) {
                        target = fp;
                    }
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

    private static void burstFallingPiece(FallingPiece piece) {
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

    public static void draw(SpriteBatch batch) {
        batch.begin();
        batch.draw(Assets.splashTexture, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        batch.setColor(1, 1, 1, 0.8f);
        for (FallingPiece p : fallingPieces) {
            int[][] shape = Tetromino.SHAPES[p.type][p.rotation];
            var tex = Assets.blockTextures[p.type];
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

        String splashText = IS_WEB ? "press SPACE to start" : "SPACE to start, ESC to quit";

        TextRenderer.drawTextWithBg(batch, Assets.font, "TET4J " + BuildInfo.VERSION + " by Krusher 2026, licensed under GPL 3",
            Constants.SPLASH_LICENSE_X, Constants.SPLASH_LICENSE_Y, Constants.TEXT_BG_PAD * 2);
        TextRenderer.drawTextWithBgCentered(batch, Assets.bigFont, splashText,
            Constants.SPLASH_INSTRUCTION_Y, Constants.TEXT_BG_PAD * 2);
        batch.end();
    }

    private SplashBackground() {}
}
