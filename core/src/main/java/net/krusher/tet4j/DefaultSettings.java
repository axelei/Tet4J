package net.krusher.tet4j;

import net.krusher.tet4j.modes.ModeId;

public enum DefaultSettings {
    MUSIC(true),
    SOUND_EFFECTS(true),
    FULLSCREEN(true),
    GAME_MODE(ModeId.NOVA.name());

    public final boolean boolValue;
    public final String stringValue;

    DefaultSettings(boolean defaultValue) {
        this.boolValue = defaultValue;
        this.stringValue = null;
    }

    DefaultSettings(String defaultValue) {
        this.boolValue = false;
        this.stringValue = defaultValue;
    }
}
