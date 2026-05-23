package net.krusher.tet4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import net.krusher.tet4j.Tetromino.Type;

public class Board {
    public static final int COLS = 10;
    public static final int ROWS = 22;
    public static final int VISIBLE_ROWS = 20;
    public static final float CLEAR_DURATION = 0.5f;

    public enum State { PLAYING, CLEARING, GAME_OVER, PAUSED }

    public int[][] grid = new int[ROWS][COLS];
    public Type currentType;
    public int currentX, currentY, currentRotation;
    public Type nextType;
    public int score, lines, level;
    public boolean gameOver;
    public State state = State.PLAYING;
    public boolean cheatMode;

    public boolean justCleared;
    public boolean[] clearedRows = new boolean[ROWS];
    public int linesCleared;
    public float clearTimer;

    private List<Type> bag = new ArrayList<>();
    private float dropTimer;
    private float dropInterval = 1f;

    private static final int[] SCORE_TABLE = {0, 100, 300, 500, 800};

    public Board() {
        fillBag();
        nextType = drawFromBag();
        spawnPiece();
    }

    private void fillBag() {
        bag.clear();
        Collections.addAll(bag, Type.values());
        Collections.shuffle(bag, new java.util.Random());
    }

    private Type drawFromBag() {
        if (cheatMode) return Type.I;
        if (bag.isEmpty()) fillBag();
        return bag.remove(bag.size() - 1);
    }

    public void spawnPiece() {
        currentType = nextType;
        nextType = drawFromBag();
        currentX = 3;
        currentY = 0;
        currentRotation = 0;
        dropTimer = 0;

        if (!canPlace(currentType, currentRotation, currentX, currentY)) {
            gameOver = true;
            state = State.GAME_OVER;
        }
    }

    public boolean canPlace(Type type, int rotation, int x, int y) {
        int[][] shape = Tetromino.getShape(type, rotation);
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (shape[r][c] != 0) {
                    int gr = y + r;
                    int gc = x + c;
                    if (gc < 0 || gc >= COLS || gr < 0 || gr >= ROWS) return false;
                    if (grid[gr][gc] != 0) return false;
                }
            }
        }
        return true;
    }

    public void lockPiece() {
        int[][] shape = Tetromino.getShape(currentType, currentRotation);
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (shape[r][c] != 0) {
                    int gr = currentY + r;
                    int gc = currentX + c;
                    if (gr >= 0 && gr < ROWS && gc >= 0 && gc < COLS) {
                        grid[gr][gc] = currentType.ordinal() + 1;
                    }
                }
            }
        }

        Arrays.fill(clearedRows, false);
        linesCleared = 0;
        for (int r = ROWS - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] == 0) { full = false; break; }
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
        } else {
            spawnPiece();
        }
    }

    public void update(float delta) {
        if (state == State.GAME_OVER || state == State.PAUSED) return;

        if (state == State.CLEARING) {
            clearTimer += delta;
            if (clearTimer >= CLEAR_DURATION) {
                finishClearing();
            }
            return;
        }

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

    private void finishClearing() {
        lines += linesCleared;
        level = lines / 10;
        score += SCORE_TABLE[Math.min(linesCleared, 4)] * Math.max(1, level);
        dropInterval = Math.max(0.05f, 1f - level * 0.08f);

        int[][] newGrid = new int[ROWS][COLS];
        int writeRow = ROWS - 1;
        for (int r = ROWS - 1; r >= 0; r--) {
            if (!clearedRows[r]) {
                System.arraycopy(grid[r], 0, newGrid[writeRow], 0, COLS);
                writeRow--;
            }
        }
        grid = newGrid;

        Arrays.fill(clearedRows, false);
        linesCleared = 0;
        state = State.PLAYING;
        spawnPiece();
    }

    public void moveLeft() {
        if (state != State.PLAYING) return;
        if (canPlace(currentType, currentRotation, currentX - 1, currentY)) currentX--;
    }

    public void moveRight() {
        if (state != State.PLAYING) return;
        if (canPlace(currentType, currentRotation, currentX + 1, currentY)) currentX++;
    }

    public void rotateCW() {
        if (state != State.PLAYING) return;
        int newRot = (currentRotation + 1) & 3;
        if (canPlace(currentType, newRot, currentX, currentY)) {
            currentRotation = newRot;
            return;
        }
        if (canPlace(currentType, newRot, currentX - 1, currentY)) {
            currentRotation = newRot; currentX--;
            return;
        }
        if (canPlace(currentType, newRot, currentX + 1, currentY)) {
            currentRotation = newRot; currentX++;
            return;
        }
        if (canPlace(currentType, newRot, currentX, currentY - 1)) {
            currentRotation = newRot; currentY--;
            return;
        }
    }

    public void softDrop() {
        if (state != State.PLAYING) return;
        if (canPlace(currentType, currentRotation, currentX, currentY + 1)) {
            currentY++;
            score++;
            dropTimer = 0;
        } else {
            lockPiece();
        }
    }

    public void hardDrop() {
        if (state != State.PLAYING) return;
        int dropped = 0;
        while (canPlace(currentType, currentRotation, currentX, currentY + 1)) {
            currentY++;
            dropped++;
        }
        score += dropped * 2;
        lockPiece();
    }

    public int getGhostY() {
        if (state != State.PLAYING) return currentY;
        int gy = currentY;
        while (canPlace(currentType, currentRotation, currentX, gy + 1)) gy++;
        return gy;
    }

    public void reset() {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                grid[r][c] = 0;
        score = 0;
        lines = 0;
        level = 0;
        gameOver = false;
        state = State.PLAYING;
        dropInterval = 1f;
        dropTimer = 0;
        Arrays.fill(clearedRows, false);
        linesCleared = 0;
        clearTimer = 0;
        bag.clear();
        fillBag();
        nextType = drawFromBag();
        spawnPiece();
    }
}
