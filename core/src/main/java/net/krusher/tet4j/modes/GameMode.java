package net.krusher.tet4j.modes;

import net.krusher.tet4j.entities.Tetromino;

public interface GameMode {

    Tetromino.Type nextType(Tetromino.Type previousType);

    ModeId getId();

    static GameMode forId(ModeId id) {
        return switch (id) {
            case NOVA -> new NovaMode();
            case REMISSION -> new BrickMode();
            case CHAOS -> new RemissionMode();
        };
    }
}
