package net.krusher.tet4j.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import net.krusher.tet4j.Constants;
import net.krusher.tet4j.Settings;

public final class GraphicsManager {
    private static OrthographicCamera camera;
    private static Viewport viewport;
    private static Settings settings;

    public static void init(Settings s) {
        settings = s;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);
        viewport.apply();
    }

    public static void applyDisplayMode() {
        if (settings.isFullscreenEnabled()) {
            setFullscreenMode();
        } else {
            setWindowedMode();
        }
    }

    public static void toggleFullscreen() {
        boolean full = !Gdx.graphics.isFullscreen();
        if (full) {
            setFullscreenMode();
        } else {
            setWindowedMode();
        }
        settings.setFullscreenEnabled(full);
    }

    public static void setFullscreenMode() {
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
    }

    public static void setWindowedMode() {
        com.badlogic.gdx.Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
        int windowWidth = (int) (displayMode.width * 0.9f);
        int windowHeight = (int) (displayMode.height * 0.9f);

        int calculatedHeight = (int) (windowWidth / 1.777f);
        if (calculatedHeight <= windowHeight) {
            windowHeight = calculatedHeight;
        } else {
            windowWidth = (int) (windowHeight * 1.777f);
        }

        Gdx.graphics.setWindowedMode(windowWidth, windowHeight);
    }

    public static OrthographicCamera getCamera() {
        return camera;
    }

    public static Viewport getViewport() {
        return viewport;
    }

    public static void resize(int width, int height) {
        viewport.update(width, height);
    }

    private GraphicsManager() {}
}
