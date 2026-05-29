package net.krusher.tet4j.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import net.krusher.tet4j.Constants;
import net.krusher.tet4j.Settings;

public class GraphicsManager {
    private OrthographicCamera camera;
    private Viewport viewport;
    private Settings settings;

    public GraphicsManager(Settings settings) {
        this.settings = settings;
        initializeCamera();
    }

    private void initializeCamera() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);
        viewport.apply();
    }

    public void applyDisplayMode() {
        if (settings.isFullscreenEnabled()) {
            setFullscreenMode();
        } else {
            setWindowedMode();
        }
    }

    public void toggleFullscreen() {
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

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }
}