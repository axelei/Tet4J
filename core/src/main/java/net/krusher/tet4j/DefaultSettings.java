package net.krusher.tet4j;

public enum DefaultSettings {
    MUSIC(true),
    SOUND_EFFECTS(true),
    FULLSCREEN(true);

    public final boolean defaultValue;

    DefaultSettings(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }
}
