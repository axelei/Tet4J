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
    private GameMode gameMode = GameMode.BAG;

    /**
     * Creates a new Settings instance by loading from Gdx preferences.
     * Creates default settings if preferences don't exist.
     */
    public Settings() {
        this.prefs = Gdx.app.getPreferences(PREFS_NAME);

        // Load all settings with defaults from enum
        if (!prefs.contains(DefaultSettings.MUSIC.name())) {
            initializeDefaults();
        }

        this.fullscreenEnabled = prefs.getBoolean(DefaultSettings.FULLSCREEN.name(), true);
    }

    /**
     * Initializes preferences with default values from DefaultSettings enum.
     */
    private void initializeDefaults() {
        for (DefaultSettings setting : DefaultSettings.values()) {
            prefs.putBoolean(setting.name(), setting.getDefaultValue());
        }
        prefs.flush();
    }

    public boolean isMusicEnabled() {
        return prefs.getBoolean(DefaultSettings.MUSIC.name(), true);
    }

    public boolean isSoundEffectsEnabled() {
        return prefs.getBoolean(DefaultSettings.SOUND_EFFECTS.name(), true);
    }

    public boolean isFullscreenEnabled() {
        return fullscreenEnabled;
    }

    public void setFullscreenEnabled(boolean enabled) {
        this.fullscreenEnabled = enabled;
    }

    public void setMusicEnabled(boolean enabled) {
        prefs.putBoolean(DefaultSettings.MUSIC.name(), enabled);
        prefs.flush();
    }

    public void setSoundEffectsEnabled(boolean enabled) {
        prefs.putBoolean(DefaultSettings.SOUND_EFFECTS.name(), enabled);
        prefs.flush();
    }

    /**
     * Saves current settings to preferences.
     */
    public void save() {
        prefs.putBoolean(DefaultSettings.FULLSCREEN.name(), fullscreenEnabled);
        prefs.putBoolean(DefaultSettings.SOUND_EFFECTS.name(), isSoundEffectsEnabled());
        prefs.putBoolean(DefaultSettings.MUSIC.name(), isMusicEnabled());
        prefs.flush();
    }
}
