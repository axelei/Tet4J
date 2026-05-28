package net.krusher.tet4j;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Settings class that manages game preferences using Gdx.app.getPreferences.
 * Settings are automatically persisted to the platform-specific preferences storage.
 */
public class Settings {
    private static final String PREFS_NAME = "tet4j_settings";
    private final Preferences prefs;
    private boolean fullscreenEnabled;

    /**
     * Creates a new Settings instance by loading from Gdx preferences.
     * Creates default settings if preferences don't exist.
     */
    public Settings() {
        this.prefs = Gdx.app.getPreferences(PREFS_NAME);
        
        // Load all settings with defaults from enum
        if (!prefs.contains("music")) {
            initializeDefaults();
        }
        
        this.fullscreenEnabled = getBoolean("fullscreen", DefaultSettings.FULLSCREEN.getDefaultValue());
    }

    /**
     * Initializes preferences with default values from DefaultSettings enum.
     */
    private void initializeDefaults() {
        for (DefaultSettings setting : DefaultSettings.values()) {
            prefs.putString(setting.name().toLowerCase(), setting.getDefaultValue());
        }
        prefs.flush();
    }

    /**
     * Helper method to get boolean value from preferences.
     */
    private boolean getBoolean(String key, String defaultValue) {
        String value = prefs.getString(key, defaultValue);
        return value.equalsIgnoreCase("on");
    }

    public boolean isMusicEnabled() {
        return getBoolean("music", DefaultSettings.MUSIC.getDefaultValue());
    }

    public boolean isSoundEffectsEnabled() {
        return getBoolean("sound_effects", DefaultSettings.SOUND_EFFECTS.getDefaultValue());
    }

    public boolean isFullscreenEnabled() {
        return fullscreenEnabled;
    }

    public void setFullscreenEnabled(boolean enabled) {
        this.fullscreenEnabled = enabled;
        save();
    }

    /**
     * Saves current settings to preferences.
     */
    public void save() {
        prefs.putString("fullscreen", fullscreenEnabled ? "on" : "off");
        prefs.flush();
    }
}
