package net.krusher.tet4j.teavm;

import com.badlogic.gdx.Gdx;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
import net.krusher.tet4j.Assets;
import net.krusher.tet4j.Main;

public class TeavmLauncher {
    public static void main(String[] args) {
        Assets.fileResolver = path -> {
            String normalized = path.replace("\\", "/");
            if (normalized.startsWith("assets/")) {
                normalized = normalized.substring("assets/".length());
            }
            return Gdx.files.internal("/" + normalized);
        };

        WebApplicationConfiguration config = new WebApplicationConfiguration("canvas");
        config.width = 0;
        config.height = 0;
        new WebApplication(new Main(), config);
    }
}
