package net.krusher.tet4j.modes;

import net.krusher.tet4j.entities.Tetromino;

import java.util.Random;

public class BrickMode implements GameMode {
    private static final Random RNG = new Random();
    private static final Tetromino.Type[] TYPES = Tetromino.Type.values();

    @Override
    public Tetromino.Type nextType(Tetromino.Type previousType) {
        if (previousType == null) {
            return TYPES[RNG.nextInt(TYPES.length)];
        }
        Tetromino.Type first = TYPES[RNG.nextInt(TYPES.length)];
        if (first == previousType) {
            return TYPES[RNG.nextInt(TYPES.length)];
        }
        return first;
    }

    @Override
    public ModeId getId() {
        return ModeId.REMISSION;
    }
}
