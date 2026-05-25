package net.krusher.tet4j.gfx;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.Board;
import net.krusher.tet4j.Constants;

import java.util.ArrayList;
import java.util.List;

public class ParticleSystem {
    private final List<Particle> particles = new ArrayList<>();

    public ParticleSystem() {
    }

    public void spawnClearingParticles(Board board) {
        particles.clear();
        for (int r = 2; r < Constants.BOARD_ROWS; r++) {
            if (!board.clearedRows[r]) continue;
            for (int c = 0; c < Constants.BOARD_COLS; c++) {
                int val = board.grid[r][c];
                if (val == 0) continue;
                Particle p = new Particle();
                p.x = Constants.BOARD_X + c * Constants.BLOCK_SIZE;
                p.y = Constants.BOARD_Y + (Constants.BOARD_VISIBLE_ROWS - 1 - (r - 2)) * Constants.BLOCK_SIZE;
                float angle = Constants.PARTICLE_ANGLE_MIN + (float)(Math.random() * Constants.PARTICLE_ANGLE_RANGE);
                float speed = Constants.PARTICLE_SPEED_MIN + (float)(Math.random() * (Constants.PARTICLE_SPEED_MAX - Constants.PARTICLE_SPEED_MIN));
                p.vx = (float)Math.cos(angle) * speed;
                p.vy = (float)Math.sin(angle) * speed;
                p.rotation = (float)(Math.random() * Constants.PARTICLE_ROTATION_MAX);
                p.rotSpeed = (float)(Math.random() * Constants.PARTICLE_ROT_SPEED_MAX * 2 - Constants.PARTICLE_ROT_SPEED_MAX);
                p.texture = Assets.blockTextures[val - 1];
                particles.add(p);
            }
        }
    }

    public void update(float dt) {
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

    public void draw(SpriteBatch batch) {
        float maxAge = Constants.CLEAR_DURATION * Constants.PARTICLE_MAX_AGE_MULTIPLIER;
        for (Particle p : particles) {
            float a = Math.max(0, 1 - p.age / maxAge);
            batch.setColor(1, 1, 1, a);
            batch.draw(p.texture, p.x, p.y, Constants.BLOCK_SIZE / 2f, Constants.BLOCK_SIZE / 2f,
                Constants.BLOCK_SIZE, Constants.BLOCK_SIZE, 1, 1, p.rotation, 0, 0, Constants.BLOCK_SIZE, Constants.BLOCK_SIZE, false, false);
        }
        batch.setColor(1, 1, 1, 1);
    }

    public void clear() {
        particles.clear();
    }

    public boolean isEmpty() {
        return particles.isEmpty();
    }

    private static class Particle {
        float x, y, vx, vy, rotation, rotSpeed;
        Texture texture;
        float age;
    }
}
