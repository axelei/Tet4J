package net.krusher.tet4j.modes;

import net.krusher.tet4j.entities.Tetromino;

import java.util.Random;

public class RemissionMode implements GameMode {
    private static final Random RNG = new Random();
    private static final Tetromino.Type[] TYPES = Tetromino.Type.values();

    @Override
    public Tetromino.Type nextType(Tetromino.Type previousType) {
        return TYPES[RNG.nextInt(TYPES.length)];
    }

    @Override
    public ModeId getId() {
        return ModeId.CHAOS;
    }
}
