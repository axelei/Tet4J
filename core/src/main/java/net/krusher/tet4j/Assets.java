package net.krusher.tet4j;

import com.badlogic.gdx.Gdx;

public class Assets {
    // Returns a FileHandle pointing to external assets when an 'assets' directory exists
    // Falls back to the original relative path if not.
    public static com.badlogic.gdx.files.FileHandle file(String path) {
        java.io.File assetsDir = new java.io.File("assets");
        if (assetsDir.exists() && assetsDir.isDirectory()) {
            if (path.startsWith("assets/") || path.startsWith("assets\\")) {
                return Gdx.files.local(path);
            } else {
                return Gdx.files.local("assets/" + path);
            }
        } else {
            return Gdx.files.local(path);
        }
    }
}
