package net.krusher.tet4j.scenes;

public interface Scene {
    void update(float dt);
    void render();
    void handleInput();
}
