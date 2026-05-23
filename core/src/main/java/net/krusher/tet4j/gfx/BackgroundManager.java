package net.krusher.tet4j.gfx;

import com.badlogic.gdx.Gdx;
import net.krusher.tet4j.Assets;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.krusher.tet4j.Constants;

public class BackgroundManager {
    private final Texture[] levelBgTextures;
    private final Texture[] masterBgTextures;
    private Texture currentBgTex, prevBgTex;
    private float bgFadeTimer = -1;
    private int prevLevel = -1;

    public BackgroundManager() {
        levelBgTextures = new Texture[Constants.NUM_LEVEL_BG];
        for (int i = 0; i < Constants.NUM_LEVEL_BG; i++) {
            levelBgTextures[i] = new Texture(Assets.file("backgrounds/level" + (i + 1) + ".jpg"));
        }
        java.util.List<Texture> masterList = new java.util.ArrayList<>();
        for (com.badlogic.gdx.files.FileHandle f : Assets.file("backgrounds/master").list()) {
            String n = f.name().toLowerCase();
            if (n.endsWith(".jpg") || n.endsWith(".png")) masterList.add(new Texture(f));
        }
        masterBgTextures = masterList.toArray(new Texture[0]);
        currentBgTex = levelBgTextures[0];
    }

    public void update(float dt, int level) {
        if (level == prevLevel) return;
        prevLevel = level;

        Texture tex;
        if (level < Constants.NUM_LEVEL_BG) {
            tex = levelBgTextures[level];
        } else {
            tex = masterBgTextures[(int)(Math.random() * masterBgTextures.length)];
        }

        if (tex != currentBgTex) {
            prevBgTex = currentBgTex;
            currentBgTex = tex;
            bgFadeTimer = 0;
        }
        if (bgFadeTimer >= 0) {
            bgFadeTimer += dt;
            if (bgFadeTimer >= Constants.BG_FADE_DURATION) bgFadeTimer = -1;
        }
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
        }
        batch.draw(currentBgTex, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        batch.setColor(1, 1, 1, 1);

        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.end();
    }

    public void reset() {
        prevLevel = -1;
        currentBgTex = levelBgTextures[0];
        prevBgTex = null;
        bgFadeTimer = -1;
    }

    public void dispose() {
        for (Texture t : levelBgTextures) if (t != null) t.dispose();
        for (Texture t : masterBgTextures) if (t != null) t.dispose();
    }
}
