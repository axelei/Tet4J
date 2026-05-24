package net.krusher.tet4j;

public enum DefaultSettings {
    MUSIC("on"),
    SOUND_EFFECTS("on"),
    FULLSCREEN("off");
    
    public final String defaultValue;
    
    DefaultSettings(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
}