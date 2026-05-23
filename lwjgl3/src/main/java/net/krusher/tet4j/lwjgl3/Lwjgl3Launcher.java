package net.krusher.tet4j.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import net.krusher.tet4j.Main;

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        try {
            if (StartupHelper.startNewJvmIfRequired()) return;
            createApplication();
        } catch (Throwable t) {
            try {
                java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter("tet4j.log"));
                t.printStackTrace(pw);
                pw.close();
            } catch (java.io.IOException ignored) {}
            throw t;
        }
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Main(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Tet4J");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        configuration.setWindowedMode(1280, 720);
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}
