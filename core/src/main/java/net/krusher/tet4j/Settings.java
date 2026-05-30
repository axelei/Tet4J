package net.krusher.tet4j;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import net.krusher.tet4j.modes.ModeId;

import java.util.Locale;

public class Settings {
    private static final String PREFS_NAME = "tet4j_settings";
    private final Preferences prefs;
    private boolean fullscreenEnabled;
    private ModeId gameMode = ModeId.NOVA;

    public Settings() {
        this.prefs = Gdx.app.getPreferences(PREFS_NAME);

        if (!prefs.contains(DefaultSettings.MUSIC.name())) {
            initializeDefaults();
        }

        this.fullscreenEnabled = prefs.getBoolean(DefaultSettings.FULLSCREEN.name(), false);

        String modeName = prefs.getString(DefaultSettings.GAME_MODE.name(), null);
        if (modeName != null) {
            try {
                this.gameMode = ModeId.valueOf(modeName.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                this.gameMode = ModeId.NOVA;
            }
        }
    }

    private void initializeDefaults() {
        for (DefaultSettings setting : DefaultSettings.values()) {
            if (setting.stringValue != null) {
                prefs.putString(setting.name(), setting.stringValue);
            } else {
                prefs.putBoolean(setting.name(), setting.boolValue);
            }
        }
        prefs.flush();
    }

    public boolean isMusicEnabled() {
        return prefs.getBoolean(DefaultSettings.MUSIC.name(), false);
    }

    public boolean isSoundEffectsEnabled() {
        return prefs.getBoolean(DefaultSettings.SOUND_EFFECTS.name(), false);
    }

    public boolean isFullscreenEnabled() {
        return fullscreenEnabled;
    }

    public ModeId getGameMode() {
        return gameMode;
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

    public void setGameMode(ModeId mode) {
        this.gameMode = mode;
    }

    public int getBestScore(ModeId mode) {
        return prefs.getInteger("BEST_" + mode.name(), 0);
    }

    public boolean isNewBestScore(ModeId mode, int score) {
        if (score > getBestScore(mode)) {
            prefs.putInteger("BEST_" + mode.name(), score);
            prefs.flush();
            return true;
        }
        return false;
    }

    public void save() {
        prefs.putBoolean(DefaultSettings.FULLSCREEN.name(), fullscreenEnabled);
        prefs.putBoolean(DefaultSettings.SOUND_EFFECTS.name(), isSoundEffectsEnabled());
        prefs.putBoolean(DefaultSettings.MUSIC.name(), isMusicEnabled());
        prefs.putString(DefaultSettings.GAME_MODE.name(), gameMode.name());
        prefs.flush();
    }
}
