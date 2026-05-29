package net.krusher.tet4j.gfx;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.Constants;

public final class TextRenderer {
    private static final GlyphLayout glyphLayout = new GlyphLayout();

    public static void drawTextWithBg(SpriteBatch batch, BitmapFont font, String text, float x, float y) {
        drawTextWithBg(batch, font, text, x, y, Constants.TEXT_BG_PAD);
    }

    public static void drawTextWithBg(SpriteBatch batch, BitmapFont font, String text, float x, float y, float extraBottomPad) {
        glyphLayout.setText(font, text);
        float botPad = Constants.TEXT_BG_PAD + extraBottomPad;
        batch.setColor(0, 0, 0, Constants.TEXT_BG_ALPHA);
        batch.draw(Assets.pixel, x - Constants.TEXT_BG_PAD, y - glyphLayout.height - botPad,
            glyphLayout.width + Constants.TEXT_BG_PAD * 2, glyphLayout.height + Constants.TEXT_BG_PAD + botPad);
        batch.setColor(1, 1, 1, 1);
        font.draw(batch, text, x, y);
    }

    public static void drawTextWithBgCentered(SpriteBatch batch, BitmapFont font, String text, float y) {
        drawTextWithBgCentered(batch, font, text, y, Constants.TEXT_BG_PAD);
    }

    public static void drawTextWithBgCentered(SpriteBatch batch, BitmapFont font, String text, float y, float extraBottomPad) {
        glyphLayout.setText(font, text);
        float x = (Constants.SCREEN_WIDTH - glyphLayout.width) / 2f;
        drawTextWithBg(batch, font, text, x, y, extraBottomPad);
    }

    private TextRenderer() {}
}
