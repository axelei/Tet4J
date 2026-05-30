package net.krusher.tet4j.modes;

import net.krusher.tet4j.entities.Tetromino;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NovaMode implements GameMode {
    private final List<Tetromino.Type> bag = new ArrayList<>();

    @Override
    public Tetromino.Type nextType(Tetromino.Type previousType) {
        if (bag.isEmpty()) {
            fillBag();
        }
        return bag.removeLast();
    }

    private void fillBag() {
        bag.clear();
        Collections.addAll(bag, Tetromino.Type.values());
        Collections.shuffle(bag, new java.util.Random());
    }

    @Override
    public ModeId getId() {
        return ModeId.NOVA;
    }
}
