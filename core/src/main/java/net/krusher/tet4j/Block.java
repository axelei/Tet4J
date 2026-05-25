package net.krusher.tet4j;

import com.badlogic.gdx.graphics.Texture;

public class Block {
    public final Texture texture;
    public final int rotation;

    public Block(Texture texture, int rotation) {
        this.texture = texture;
        this.rotation = rotation;
    }
}
