package net.krusher.tet4j.gfx;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.entities.Block;
import net.krusher.tet4j.entities.Board;
import net.krusher.tet4j.Constants;
import net.krusher.tet4j.entities.Particle;

import java.util.ArrayList;
import java.util.List;

public final class ParticleSystem {
    private static final List<Particle> particles = new ArrayList<>();

    public static void spawnClearingParticles(Board board) {
        particles.clear();
        for (int r = 2; r < Constants.BOARD_ROWS; r++) {
            if (!board.clearedRows[r]) {
                continue;
            }
            for (int c = 0; c < Constants.BOARD_COLS; c++) {
                Block block = board.grid[r][c];
                if (block == null) {
                    continue;
                }
                particles.add(new Particle(block, c, r));
            }
        }
    }

    public static void update(float dt) {
        float maxAge = Constants.CLEAR_DURATION * Constants.PARTICLE_MAX_AGE_MULTIPLIER;
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.age += dt;
            if (p.age >= maxAge) { particles.remove(i); continue; }
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.vy -= Constants.PARTICLE_GRAVITY * dt;
            p.rotation += p.rotSpeed * dt;
        }
    }

    public static void draw(SpriteBatch batch) {
        float maxAge = Constants.CLEAR_DURATION * Constants.PARTICLE_MAX_AGE_MULTIPLIER;
        for (Particle p : particles) {
            float a = Math.max(0, 1 - p.age / maxAge);
            batch.setColor(1, 1, 1, a);
            batch.draw(p.texture, p.x, p.y, Constants.BLOCK_SIZE / 2f, Constants.BLOCK_SIZE / 2f,
                Constants.BLOCK_SIZE, Constants.BLOCK_SIZE, 1, 1, p.rotation, 0, 0, Constants.BLOCK_SIZE, Constants.BLOCK_SIZE, false, false);
            batch.draw(Assets.relief, p.x, p.y, Constants.BLOCK_SIZE / 2f, Constants.BLOCK_SIZE / 2f,
                Constants.BLOCK_SIZE, Constants.BLOCK_SIZE, 1, 1, p.rotation, 0, 0, Assets.relief.getWidth(), Assets.relief.getHeight(), false, false);
        }
        batch.setColor(1, 1, 1, 1);
    }

    public static void clear() {
        particles.clear();
    }

    public static boolean isEmpty() {
        return particles.isEmpty();
    }

    private ParticleSystem() {}
}
