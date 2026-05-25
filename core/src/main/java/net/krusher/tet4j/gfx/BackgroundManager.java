package net.krusher.tet4j.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.Constants;

public class BackgroundManager {
    private Texture currentBgTex;
    private Texture prevBgTex;
    private float bgFadeTimer = -1;
    private int prevLevel = -1;
    private final Color tintColor = new Color(1, 1, 1, 1);
    private float currentLuminosity;

    public BackgroundManager() {
        prevLevel = Constants.STARTING_LEVEL;
        currentBgTex = Constants.STARTING_LEVEL < Constants.NUM_LEVEL_BG
            ? loadLevelTexture(Constants.STARTING_LEVEL)
            : loadMasterTexture();
    }

    private Texture loadLevelTexture(int level) {
        return loadTexture(Assets.file("backgrounds/level" + (level + 1) + ".jpg"));
    }

    private Texture loadMasterTexture() {
        java.util.ArrayList<com.badlogic.gdx.files.FileHandle> masters = new java.util.ArrayList<>();
        for (com.badlogic.gdx.files.FileHandle f : Assets.file("backgrounds/master").list()) {
            String n = f.name().toLowerCase();
            if (n.endsWith(".jpg") || n.endsWith(".png")) masters.add(f);
        }
        if (masters.isEmpty()) return loadLevelTexture(0);
        com.badlogic.gdx.files.FileHandle chosen = masters.get((int) (Math.random() * masters.size()));
        return loadTexture(chosen);
    }

    private Texture loadTexture(com.badlogic.gdx.files.FileHandle file) {
        Pixmap pm = new Pixmap(file);
        currentLuminosity = computeLuminosity(pm);
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private static float computeLuminosity(Pixmap pm) {
        int total = 0, count = 0;
        int w = pm.getWidth(), h = pm.getHeight();
        for (int y = 0; y < h; y += 4) {
            for (int x = 0; x < w; x += 4) {
                int px = pm.getPixel(x, y);
                total += (int)(0.2126f * ((px >> 24) & 0xFF)
                             + 0.7152f * ((px >> 16) & 0xFF)
                             + 0.0722f * ((px >> 8) & 0xFF));
                count++;
            }
        }
        return count > 0 ? (float) total / count / 255f : 0.5f;
    }

    public void update(float dt, int level) {
        if (bgFadeTimer >= 0) {
            bgFadeTimer += dt;
            if (bgFadeTimer >= Constants.BG_FADE_DURATION) {
                bgFadeTimer = -1;
                if (prevBgTex != null) {
                    prevBgTex.dispose();
                    prevBgTex = null;
                }
            }
        }

        if (level == prevLevel) return;
        prevLevel = level;

        if (prevBgTex != null) {
            prevBgTex.dispose();
            prevBgTex = null;
        }

        Texture newTex = level < Constants.NUM_LEVEL_BG ? loadLevelTexture(level) : loadMasterTexture();
        prevBgTex = currentBgTex;
        currentBgTex = newTex;
        bgFadeTimer = 0;
        generateTint();
    }

    private void generateTint() {
        float h = (float) Math.random() * 360f;
        float l = Math.max(0.12f, Math.min(0.40f, 0.38f - currentLuminosity * 0.20f));
        hslToRgb(h, 0.75f, l, tintColor);
    }

    private static void hslToRgb(float h, float s, float l, Color out) {
        float c = (1f - Math.abs(2f * l - 1f)) * s;
        float x = c * (1f - Math.abs((h / 60f) % 2f - 1f));
        float m = l - c / 2f;
        float r, g, b;
        if (h < 60) { r = c; g = x; b = 0; }
        else if (h < 120) { r = x; g = c; b = 0; }
        else if (h < 180) { r = 0; g = c; b = x; }
        else if (h < 240) { r = 0; g = x; b = c; }
        else if (h < 300) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }
        out.set(r + m, g + m, b + m, 1f);
    }

    public void draw(SpriteBatch batch) {
        batch.begin();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (bgFadeTimer >= 0 && prevBgTex != null) {
            float alpha = bgFadeTimer / Constants.BG_FADE_DURATION;
            batch.setColor(1, 1, 1, 1 - alpha);
            batch.draw(prevBgTex, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

            batch.setColor(1, 1, 1, alpha);
            batch.draw(currentBgTex, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

            batch.setColor(tintColor.r, tintColor.g, tintColor.b, alpha * Constants.BG_TINT_STRENGTH);
            batch.draw(Assets.pixel, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        } else {
            batch.setColor(1, 1, 1, 1);
            batch.draw(currentBgTex, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
            batch.setColor(tintColor.r, tintColor.g, tintColor.b, Constants.BG_TINT_STRENGTH);
            batch.draw(Assets.pixel, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        }
        batch.setColor(1, 1, 1, 1);

        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.end();
    }

    public void reset(int level) {
        prevLevel = level;
        if (prevBgTex != null) { prevBgTex.dispose(); prevBgTex = null; }
        if (currentBgTex != null) { currentBgTex.dispose(); currentBgTex = null; }
        currentBgTex = level < Constants.NUM_LEVEL_BG ? loadLevelTexture(level) : loadMasterTexture();
        generateTint();
        bgFadeTimer = -1;
    }

    public void dispose() {
        if (currentBgTex != null) { currentBgTex.dispose(); currentBgTex = null; }
        if (prevBgTex != null) { prevBgTex.dispose(); prevBgTex = null; }
    }
}
