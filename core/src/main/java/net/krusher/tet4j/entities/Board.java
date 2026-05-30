package net.krusher.tet4j.entities;

import java.util.Arrays;

import net.krusher.tet4j.Assets;
import net.krusher.tet4j.Constants;
import net.krusher.tet4j.entities.Tetromino.Type;
import net.krusher.tet4j.modes.GameMode;
import net.krusher.tet4j.modes.ModeId;

public class Board {
    public enum State { PLAYING, CLEARING, GAME_OVER, PAUSED }

    public Block[][] grid = new Block[Constants.BOARD_ROWS][Constants.BOARD_COLS];
    public Type currentType;
    public int currentX, currentY, currentRotation;
    public float visualX, visualY;
    public Type nextType;
    public int score, lines, level;
    public boolean gameOver;
    public State state = State.PLAYING;
    public boolean cheatMode;

    public boolean justCleared;
    public boolean justLocked;
    public int[] pieceCounts = new int[7];
    public boolean justGameOver;
    public int lockX;
    public boolean[] clearedRows = new boolean[Constants.BOARD_ROWS];
    public int linesCleared;
    public float clearTimer;
    public float[][] fallDelays = new float[Constants.BOARD_ROWS][Constants.BOARD_COLS];

    private GameMode gameMode = GameMode.forId(ModeId.NOVA);
    private float dropTimer;
    private float dropInterval = Constants.INITIAL_DROP_INTERVAL;

    public Board() {
        reset();
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode mode) {
        this.gameMode = mode;
    }

    private Type drawFromBag(Type previousType) {
        if (cheatMode) {
            return Type.I;
        }
        return gameMode.nextType(previousType);
    }

    public void spawnPiece() {
        Type prev = currentType;
        currentType = nextType;
        if (currentType != null) {
            pieceCounts[currentType.ordinal()]++;
        }
        nextType = drawFromBag(prev);
        currentX = Constants.SPAWN_X;
        currentY = 0;
        currentRotation = 0;
        visualX = currentX;
        visualY = currentY;
        dropTimer = 0;

        if (!canPlace(currentType, currentRotation, currentX, currentY)) {
            gameOver = true;
            state = State.GAME_OVER;
            justGameOver = true;
        }
    }

    public boolean canPlace(Type type, int rotation, int x, int y) {
        int[][] shape = Tetromino.getShape(type, rotation);
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (shape[r][c] != 0) {
                    int gr = y + r;
                    int gc = x + c;
                    if (gc < 0 || gc >= Constants.BOARD_COLS || gr < 0 || gr >= Constants.BOARD_ROWS) {
                        return false;
                    }
                    if (grid[gr][gc] != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void lockPiece() {
        justLocked = true;
        lockX = currentX;
        int[][] shape = Tetromino.getShape(currentType, currentRotation);
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (shape[r][c] != 0) {
                    int gr = currentY + r;
                    int gc = currentX + c;
                    if (gr >= 0 && gr < Constants.BOARD_ROWS && gc >= 0 && gc < Constants.BOARD_COLS) {
                        grid[gr][gc] = new Block(Assets.blockTextures[currentType.ordinal()], currentRotation);
                    }
                }
            }
        }

        Arrays.fill(clearedRows, false);
        linesCleared = 0;
        for (int r = Constants.BOARD_ROWS - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < Constants.BOARD_COLS; c++) {
                if (grid[r][c] == null) { full = false; break; }
            }
            if (full) {
                clearedRows[r] = true;
                linesCleared++;
            }
        }

         if (linesCleared > 0) {
            state = State.CLEARING;
            justCleared = true;
            clearTimer = 0;
            for (int r = 0; r < Constants.BOARD_ROWS; r++) {
                Arrays.fill(fallDelays[r], 0f);
            }
            int shifted = 0;
            for (int r = Constants.BOARD_ROWS - 1; r >= 0; r--) {
                if (clearedRows[r]) { shifted++; continue; }
                for (int c = 0; c < Constants.BOARD_COLS; c++) {
                    if (grid[r][c] != null) {
                        fallDelays[r][c] = (float)Math.random() * Constants.CLEAR_FALL_DELAY_MAX;
                    }
                }
            }
         } else {
            spawnPiece();
        }
    }

    public void update(float delta) {
        if (state == State.GAME_OVER || state == State.PAUSED) {
            return;
        }

        if (state == State.CLEARING) {
            clearTimer += delta;
            if (clearTimer >= Constants.CLEAR_DURATION + Constants.CLEAR_FALL_DELAY_MAX) {
                finishClearing();
            }
            return;
        }

        visualX += (currentX - visualX) * Math.min(1, Constants.PIECE_SLIDE_SPEED * delta);
        visualY += (currentY - visualY) * Math.min(1, Constants.PIECE_SLIDE_SPEED * delta);
        dropTimer += delta;
        if (dropTimer >= dropInterval) {
            dropTimer -= dropInterval;
            if (canPlace(currentType, currentRotation, currentX, currentY + 1)) {
                currentY++;
            } else {
            lockPiece();
        }
        }
    }

    private static float dropIntervalForLevel(int level) {
        float t = (Math.max(level, 1) - 1) / 29f;
        t = (float)Math.log(1 + t * (Math.E - 1));
        return Constants.INITIAL_DROP_INTERVAL
            - (Constants.INITIAL_DROP_INTERVAL - Constants.SOFT_DROP_INTERVAL) * t;
    }

    private void finishClearing() {
        lines += linesCleared;
        level = lines / Constants.LINES_PER_LEVEL;
        score += (int)(Constants.SCORE_TABLE[Math.min(linesCleared, 4)] * Math.max(1, Math.pow(level, 1.1)));
        dropInterval = dropIntervalForLevel(level);

        Block[][] newGrid = new Block[Constants.BOARD_ROWS][Constants.BOARD_COLS];
        int writeRow = Constants.BOARD_ROWS - 1;
        for (int r = Constants.BOARD_ROWS - 1; r >= 0; r--) {
            if (!clearedRows[r]) {
                System.arraycopy(grid[r], 0, newGrid[writeRow], 0, Constants.BOARD_COLS);
                writeRow--;
            }
        }
        grid = newGrid;

        for (int r = 0; r < Constants.BOARD_ROWS; r++) {
            Arrays.fill(fallDelays[r], 0f);
        }
        Arrays.fill(clearedRows, false);
        linesCleared = 0;
        state = State.PLAYING;
        spawnPiece();
    }

    public boolean moveLeft() {
        if (state != State.PLAYING) {
            return false;
        }
        if (canPlace(currentType, currentRotation, currentX - 1, currentY)) { currentX--; return true; }
        return false;
    }

    public boolean moveRight() {
        if (state != State.PLAYING) {
            return false;
        }
        if (canPlace(currentType, currentRotation, currentX + 1, currentY)) { currentX++; return true; }
        return false;
    }

    public boolean rotateCW() {
        if (state != State.PLAYING) {
            return false;
        }
        int newRot = (currentRotation + 1) & Constants.ROTATION_MASK;
        if (canPlace(currentType, newRot, currentX, currentY)) {
            currentRotation = newRot;
            return true;
        }
        if (canPlace(currentType, newRot, currentX - 1, currentY)) {
            currentRotation = newRot; currentX--;
            return true;
        }
        if (canPlace(currentType, newRot, currentX + 1, currentY)) {
            currentRotation = newRot; currentX++;
            return true;
        }
        if (canPlace(currentType, newRot, currentX, currentY - 1)) {
            currentRotation = newRot; currentY--;
            return true;
        }
        return false;
    }

    public void softDrop() {
        if (state != State.PLAYING) {
            return;
        }
        if (canPlace(currentType, currentRotation, currentX, currentY + 1)) {
            currentY++;
            score++;
            dropTimer = 0;
        } else {
            lockPiece();
        }
    }

    public void hardDrop() {
        if (state != State.PLAYING) {
            return;
        }
        int dropped = 0;
        while (canPlace(currentType, currentRotation, currentX, currentY + 1)) {
            currentY++;
            dropped++;
        }
        score += dropped * 2;
        lockPiece();
        justLocked = false;
    }

    public int getGhostY() {
        if (state != State.PLAYING) {
            return currentY;
        }
        int gy = currentY;
        while (canPlace(currentType, currentRotation, currentX, gy + 1)) {
            gy++;
        }
        return gy;
    }

    public void reset() {
        for (int r = 0; r < Constants.BOARD_ROWS; r++) {
            for (int c = 0; c < Constants.BOARD_COLS; c++) {
                grid[r][c] = null;
            }
            Arrays.fill(fallDelays[r], 0f);
        }
        score = 0;
        lines = Constants.STARTING_LEVEL * Constants.LINES_PER_LEVEL;
        level = Constants.STARTING_LEVEL;
        gameOver = false;
        lockX = Constants.SPAWN_X;
        state = State.PLAYING;
        dropInterval = dropIntervalForLevel(level);
        dropTimer = 0;
        Arrays.fill(clearedRows, false);
        linesCleared = 0;
        clearTimer = 0;
        java.util.Arrays.fill(pieceCounts, 0);
        nextType = drawFromBag(null);
        spawnPiece();
    }
}
