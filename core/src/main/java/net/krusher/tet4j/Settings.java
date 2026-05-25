package net.krusher.tet4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Simple data class that holds game settings loaded from a properties file.
 * Settings are loaded once at initialization and provide read-only access.
 */
public class Settings {
    private final boolean musicEnabled;
    private final boolean soundEffectsEnabled;
    private boolean fullscreenEnabled;
    private final Properties props;

    /**
     * Creates a new Settings instance by loading from the settings file.
     * Creates default settings if file doesn't exist.
     */
    public Settings() {
        this.props = loadSettingsFile();

        // Load all settings with defaults from enum
        this.musicEnabled = getBoolean(props, "music", DefaultSettings.MUSIC.getDefaultValue());
        this.soundEffectsEnabled = getBoolean(props, "sound_effects", DefaultSettings.SOUND_EFFECTS.getDefaultValue());
        this.fullscreenEnabled = getBoolean(props, "fullscreen", DefaultSettings.FULLSCREEN.getDefaultValue());
    }

    /**
     * Loads settings from file, creating defaults if file doesn't exist.
     */
    private Properties loadSettingsFile() {
        File settingsFile = getSettingsFile();
        Properties props = new Properties();

        try {
            if (settingsFile.exists()) {
                FileInputStream in = new FileInputStream(settingsFile);
                props.load(in);
                in.close();
            } else {
                // Create default settings file
                createDefaultSettingsFile(settingsFile);
                // Load the newly created file
                FileInputStream in = new FileInputStream(settingsFile);
                props.load(in);
                in.close();
            }
        } catch (Exception e) {
            // If anything goes wrong, create default settings
            createDefaultSettingsFile(settingsFile);
        }

        return props;
    }

    /**
     * Gets the settings file location.
     * If working directory is "assets", uses parent directory.
     * Otherwise, uses working directory.
     */
    private File getSettingsFile() {
        File workingDir = new File(System.getProperty("user.dir"));

        // If working directory is "assets", use parent directory
        if (workingDir.getName().equals("assets")) {
            return new File(workingDir.getParentFile(), "settings.properties");
        }

        // Otherwise use working directory
        return new File(workingDir, "settings.properties");
    }

    /**
     * Creates a settings file with default values.
     */
    private void createDefaultSettingsFile(File settingsFile) {
        try {
            Properties defaultProps = new Properties();

            // Set all default values from DefaultSettings enum
            for (DefaultSettings setting : DefaultSettings.values()) {
                defaultProps.setProperty(setting.name().toLowerCase(), setting.getDefaultValue());
            }

            // Write to file
            FileOutputStream out = new FileOutputStream(settingsFile);
            defaultProps.store(out, "Game Settings - Default values");
            out.close();
        } catch (IOException e) {
            System.err.println("Could not create settings.properties file: " + e.getMessage());
        }
    }

    /**
     * Helper method to get boolean value from properties.
     */
    private boolean getBoolean(Properties props, String key, String defaultValue) {
        String value = props.getProperty(key, defaultValue);
        return value.equalsIgnoreCase("on");
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public boolean isSoundEffectsEnabled() {
        return soundEffectsEnabled;
    }

    public boolean isFullscreenEnabled() {
        return fullscreenEnabled;
    }

    public void setFullscreenEnabled(boolean enabled) {
        this.fullscreenEnabled = enabled;
    }

    public void save() {
        try {
            props.setProperty("fullscreen", fullscreenEnabled ? "on" : "off");
            FileOutputStream out = new FileOutputStream(getSettingsFile());
            props.store(out, "Game Settings");
            out.close();
        } catch (IOException e) {
            System.err.println("Could not save settings.properties: " + e.getMessage());
        }
    }
}
