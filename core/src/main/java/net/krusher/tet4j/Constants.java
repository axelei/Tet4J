package net.krusher.tet4j;

import com.badlogic.gdx.graphics.Color;

public class Constants {
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    public static final int BLOCK_SIZE = 32;
    public static final int PREVIEW_BLOCK_SIZE = 20;

    public static final int INFO_X = 80;
    public static final int BOARD_X = 420;
    public static final int BOARD_Y = 40;

    public static final int BOARD_COLS = 10;
    public static final int BOARD_ROWS = 22;
    public static final int BOARD_VISIBLE_ROWS = 20;
    public static final int BOARD_PX_W = BOARD_COLS * BLOCK_SIZE;
    public static final int BOARD_PX_H = BOARD_VISIBLE_ROWS * BLOCK_SIZE;
    public static final int BOARD_BORDER_OUTER = 2;
    public static final int BOARD_BORDER_INNER = 1;
    public static final Color BOARD_BORDER_COLOR_DARK = new Color(0.05f, 0.05f, 0.08f, 1);
    public static final Color BOARD_BORDER_COLOR_LIGHT = new Color(0.12f, 0.12f, 0.16f, 1);
    public static final Color BOARD_BG = new Color(0.1f, 0.1f, 0.14f, 1);

    public static final int FONT_SIZE = 16;
    public static final int FONT_SIZE_BIG = 32;

    public static final int SPAWN_X = 3;

    public static final int LINES_PER_LEVEL = 10;
    public static final float INITIAL_DROP_INTERVAL = 1f;
    public static final float MIN_DROP_INTERVAL = 0.05f;
    public static final float DROP_INTERVAL_DECAY = 0.03167f;
    public static final float SOFT_DROP_INTERVAL = 0.05f;
    public static final float DAS_DELAY = 0.17f;
    public static final float DAS_REPEAT = 0.05f;

    public static final int ROTATION_MASK = 3;

    public static final int[] SCORE_TABLE = {0, 100, 300, 500, 800};

    public static final float CLEAR_DURATION = 0.5f;
    public static final float CLEARING_SLIDE_START = 0.35f;

    public static final float PARTICLE_GRAVITY = 800;
    public static final float PARTICLE_SPEED_MIN = 300;
    public static final float PARTICLE_SPEED_MAX = 700;
    public static final float PARTICLE_ANGLE_MIN = (float)(Math.PI * 0.25);
    public static final float PARTICLE_ANGLE_RANGE = (float)(Math.PI * 0.5);
    public static final float PARTICLE_ROTATION_MAX = 360;
    public static final float PARTICLE_ROT_SPEED_MAX = 360;
    public static final float PARTICLE_MAX_AGE_MULTIPLIER = 2;

    public static final float TOAST_DURATION = 4f;
    public static final float TOAST_MAX_WIDTH = 320;
    public static final float TOAST_PAD_X = 6;
    public static final float TOAST_PAD_Y = 6;
    public static final float TOAST_BG_ALPHA = 0.75f;
    public static final float TOAST_MARGIN_X = 10;
    public static final float TOAST_MARGIN_Y = 10;
    public static final float TOAST_SLIDE_IN = 0.3f;
    public static final float TOAST_SLIDE_OUT = 0.5f;
    public static final float TOAST_SLIDE_OFFSET = 20;

    public static final float GM_VOLUME = 0.75f;
    public static final float GM_VOLUME_PAUSED = GM_VOLUME * 0.5f;

    public static final int NUM_LEVEL_BG = 10;
    public static final float BG_FADE_DURATION = 0.8f;
    public static final int BG_SPLASH_DRAW_W = 1280;
    public static final int BG_SPLASH_DRAW_H = 720;

    public static final float PAUSE_OVERLAY_ALPHA = 0.6f;
    public static final float GAMEOVER_OVERLAY_ALPHA = 0.65f;

    public static final float TEXT_BG_PAD = 4;
    public static final float TEXT_BG_ALPHA = 0.75f;

    public static final Color SPLASH_TEXT_COLOR = Color.WHITE;
}
