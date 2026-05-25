package net.krusher.tet4j.gfx;

import com.badlogic.gdx.graphics.Texture;
import net.krusher.tet4j.entities.Block;
import net.krusher.tet4j.Constants;

public class Particle {
    public float x, y, vx, vy, rotation, rotSpeed;
    public Texture texture;
    public float age;

    public Particle(Block block, int col, int row) {
        x = Constants.BOARD_X + col * Constants.BLOCK_SIZE;
        y = Constants.BOARD_Y + (Constants.BOARD_VISIBLE_ROWS - 1 - (row - 2)) * Constants.BLOCK_SIZE;
        float angle = Constants.PARTICLE_ANGLE_MIN + (float)(Math.random() * Constants.PARTICLE_ANGLE_RANGE);
        float speed = Constants.PARTICLE_SPEED_MIN + (float)(Math.random() * (Constants.PARTICLE_SPEED_MAX - Constants.PARTICLE_SPEED_MIN));
        vx = (float)Math.cos(angle) * speed;
        vy = (float)Math.sin(angle) * speed;
        rotation = (float)(Math.random() * Constants.PARTICLE_ROTATION_MAX);
        rotSpeed = (float)(Math.random() * Constants.PARTICLE_ROT_SPEED_MAX * 2 - Constants.PARTICLE_ROT_SPEED_MAX);
        texture = block.texture;
        age = 0;
    }
}
