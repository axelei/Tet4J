package net.krusher.tet4j.teavm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import net.krusher.tet4j.Assets;

public class TeaVmFileResolver implements Assets.FileResolver {
    @Override
    public FileHandle resolve(String path) {
        String normalized = path.replace("\\", "/");
        if (!normalized.startsWith("assets/")) {
            normalized = "assets/" + normalized;
        }
        return Gdx.files.internal(normalized);
    }
}
