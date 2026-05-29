package net.krusher.tet4j.gfx;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.Constants;

public class Toast {
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private String text;
    private float timer = -1;

    public Toast() {
    }

    public String getText() {
        return text;
    }

    public float getTimer() {
        return timer;
    }

    public void setText(String text) {
        this.text = text;
        timer = 0;
    }

    public void update(float dt) {
        if (timer >= 0) {
            timer += dt;
            if (timer > Constants.TOAST_DURATION) {
                timer = -1;
            }
        }
    }

    public void draw(SpriteBatch batch, BitmapFont font) {
        if (text == null || timer < 0) {
            return;
        }

        glyphLayout.setText(font, text, com.badlogic.gdx.graphics.Color.WHITE, Constants.TOAST_MAX_WIDTH,
            com.badlogic.gdx.utils.Align.left, true);
        float th = glyphLayout.height + Constants.TOAST_PAD_Y * 2;

        float alpha = 1f;
        float yOffset;
        if (timer < Constants.TOAST_SLIDE_IN) {
            float t = timer / Constants.TOAST_SLIDE_IN;
            alpha = t;
            yOffset = (1 - t) * -(th + Constants.TOAST_SLIDE_OFFSET);
        } else if (timer > Constants.TOAST_DURATION - Constants.TOAST_SLIDE_OUT) {
            float t = (Constants.TOAST_DURATION - timer) / Constants.TOAST_SLIDE_OUT;
            alpha = t;
            yOffset = (1 - t) * -(th + Constants.TOAST_SLIDE_OFFSET);
        } else {
            yOffset = 0;
        }

        batch.begin();
        float tx = Constants.SCREEN_WIDTH - Constants.TOAST_MAX_WIDTH - Constants.TOAST_MARGIN_X;
        float ty = Constants.TOAST_MARGIN_Y + th + yOffset;

        batch.setColor(0, 0, 0, Constants.TOAST_BG_ALPHA * alpha);
        batch.draw(Assets.pixel, tx, ty - th, Constants.TOAST_MAX_WIDTH, th);
        batch.setColor(1, 1, 1, alpha);
        font.draw(batch, glyphLayout, tx + Constants.TOAST_PAD_X, ty - Constants.TOAST_PAD_Y);
        batch.setColor(1, 1, 1, 1);
        batch.end();
    }
}
