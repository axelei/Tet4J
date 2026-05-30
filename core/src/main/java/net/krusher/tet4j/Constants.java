package net.krusher.tet4j;

import com.badlogic.gdx.graphics.Color;

public class Constants {
    public static final int SCREEN_WIDTH = 1920;
    public static final int SCREEN_HEIGHT = 1080;

    public static final int BLOCK_SIZE = 48;
    public static final int PREVIEW_BLOCK_SIZE = 42;

    public static final int INFO_X = 120;
    public static final int BOARD_X = 630;
    public static final int BOARD_Y = 60;

    public static final int BOARD_COLS = 10;
    public static final int BOARD_ROWS = 22;
    public static final int BOARD_VISIBLE_ROWS = 20;
    public static final int BOARD_PX_W = BOARD_COLS * BLOCK_SIZE;
    public static final int BOARD_PX_H = BOARD_VISIBLE_ROWS * BLOCK_SIZE;
    public static final int BOARD_BORDER_OUTER = 3;
    public static final int BOARD_BORDER_INNER = 2;
    public static final Color BOARD_BORDER_COLOR_DARK = new Color(0.05f, 0.05f, 0.08f, 1);
    public static final Color BOARD_BORDER_COLOR_LIGHT = new Color(0.12f, 0.12f, 0.16f, 1);
    public static final Color BOARD_BG = new Color(0.1f, 0.1f, 0.14f, 1);

    public static final int FONT_SIZE = 36;
    public static final int FONT_SIZE_BIG = 72;

    public static final int SPAWN_X = 3;
    public static final int STARTING_LEVEL = 0;

    public static final int LINES_PER_LEVEL = 10;
    public static final float INITIAL_DROP_INTERVAL = 1f;
    public static final float SOFT_DROP_INTERVAL = 0.05f;
    public static final float DAS_DELAY = 0.17f;
    public static final float DAS_REPEAT = 0.05f;
    public static final float PIECE_SLIDE_SPEED = 24f;

    public static final int ROTATION_MASK = 3;

    public static final int[] SCORE_TABLE = {0, 100, 450, 2000, 8000};

    public static final float CLEAR_DURATION = 0.5f;
    public static final float CLEARING_SLIDE_START = 0.35f;
    public static final float CLEAR_FALL_DELAY_MAX = 0.2f;

    public static final float PARTICLE_GRAVITY = 1200;
    public static final float PARTICLE_SPEED_MIN = 450;
    public static final float PARTICLE_SPEED_MAX = 1050;
    public static final float PARTICLE_ANGLE_MIN = (float)(Math.PI * 0.25);
    public static final float PARTICLE_ANGLE_RANGE = (float)(Math.PI * 0.5);
    public static final float PARTICLE_ROTATION_MAX = 360;
    public static final float PARTICLE_ROT_SPEED_MAX = 360;
    public static final float PARTICLE_MAX_AGE_MULTIPLIER = 2;

    public static final float TOAST_DURATION = 6f;
    public static final float TOAST_MAX_WIDTH = 480;
    public static final float TOAST_PAD_X = 9;
    public static final float TOAST_PAD_Y = 9;
    public static final float TOAST_BG_ALPHA = 0.75f;
    public static final float TOAST_MARGIN_X = 15;
    public static final float TOAST_MARGIN_Y = 15;
    public static final float TOAST_SLIDE_IN = 0.3f;
    public static final float TOAST_SLIDE_OUT = 0.5f;
    public static final float TOAST_SLIDE_OFFSET = 30;

    public static final float PAN_HARDNESS = 0.50f;
    public static final float GM_VOLUME = 0.75f;
    public static final float GM_VOLUME_PAUSED = GM_VOLUME * 0.5f;
    public static final float GM_FADE_DURATION = 2.0f;

    public static final int NUM_LEVEL_BG = 10;
    public static final float BG_FADE_DURATION = 0.8f;
    public static final float BG_TINT_STRENGTH = 0.6f;

    public static final float BG_OPACITY = 0.75f;
    public static final float PAUSE_OVERLAY_ALPHA = 0.6f;
    public static final float GAMEOVER_OVERLAY_ALPHA = 0.65f;

    public static final float TEXT_BG_PAD = 6;
    public static final float TEXT_BG_ALPHA = 0.75f;

    public static final int INFO_PREVIEW_TOP_OFFSET = 30;
    public static final int INFO_UI_TOP_OFFSET = 180;
    public static final int INFO_PREVIEW_GAP = 18;
    public static final int INFO_PREVIEW_BLOCK_X = 135;
    public static final int INFO_LABEL_VALUE_GAP = 48;
    public static final int INFO_VALUE_LABEL_GAP = 66;
    public static final int INFO_KEY_X_LEFT = 120;
    public static final int INFO_KEY_X_RIGHT = 1400;
    public static final int INFO_KEY_LABEL_FIRST = 180;
    public static final int INFO_KEY_LABEL_STEP = 45;
    public static final int INFO_KEY_LABEL_TOP = 880;
    public static final int PIECE_COUNT_Y = 660;
    public static final int PIECE_COUNT_SIZE = 24;

    public static final int QUIT_PROMPT_X = 105;
    public static final int CONFIRM_PROMPT_X = 165;
    public static final int TEXT_CENTER_Y_OFFSET_LARGE = 45;
    public static final int TEXT_CENTER_Y_OFFSET_SMALL = -23;

    public static final int PAUSED_LABEL_X = 60;
    public static final int PAUSED_LABEL_Y = 24;

    public static final int GAME_OVER_LABEL_X = 30;
    public static final int GAME_OVER_NEW_BEST_Y = 415;
    public static final int RESTART_PROMPT_X = 68;

    public static final int OPTIONS_TITLE_Y = 900;
    public static final int OPTIONS_FIRST_Y = 720;
    public static final int OPTIONS_STEP = 80;
    public static final int OPTIONS_VALUE_X = 1100;
    public static final float OPTIONS_OVERLAY_ALPHA = 0.65f;
    public static final Color OPTIONS_TINT = new Color(0.08f, 0.08f, 0.1f, 0.85f);

    public static final int SPLASH_LICENSE_X = 20;
    public static final int SPLASH_LICENSE_Y = 40;
    public static final int SPLASH_INSTRUCTION_Y = 420;

    public static final int SMALL_TEXT_HEIGHT = 45;
}
