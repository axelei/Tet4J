package net.krusher.tet4j.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import net.krusher.tet4j.Assets;

import java.nio.charset.StandardCharsets;

public class DesktopFileResolver implements Assets.FileResolver {

    @Override
    public FileHandle resolve(String path) {

        // Normalize path to forward slashes for consistency
        String normalizedPath = path.replace("\\", "/");
        if (!normalizedPath.startsWith("assets/")) {
            normalizedPath = "assets/" + normalizedPath;
        }

        // Try 1: Current working directory (Gradle run task)
        java.io.File assetsDir = new java.io.File("assets");
        if (assetsDir.exists() && assetsDir.isDirectory()) {
            return Gdx.files.local(normalizedPath);
        }

        // Try 2: Relative to user working directory
        java.io.File userDir = new java.io.File(System.getProperty("user.dir"));
        assetsDir = new java.io.File(userDir, "assets");
        if (assetsDir.exists() && assetsDir.isDirectory()) {
            return Gdx.files.local(normalizedPath);
        }

        // Try 3: Relative to executable/JAR location (for packaged builds)
        try {
            java.net.URL codeLocation = Assets.class.getProtectionDomain().getCodeSource().getLocation();
            if (codeLocation != null) {
                java.nio.file.Path codePath = new java.io.File(java.net.URLDecoder.decode(codeLocation.getPath(), StandardCharsets.UTF_8)).toPath();

                // Handle macOS app bundle structure (.app/Contents/MacOS/...)
                if (codePath.toString().contains(".app/Contents/MacOS")) {
                    // For macOS bundles: go up from .../Tet4J.app/Contents/MacOS/bin to .../Tet4J.app/Contents/MacOS
                    java.io.File macOSDir = codePath.getParent().toFile();
                    assetsDir = new java.io.File(macOSDir, "assets");
                    if (assetsDir.exists() && assetsDir.isDirectory()) {
                        return Gdx.files.absolute(new java.io.File(assetsDir, normalizedPath.substring("assets/".length())).getAbsolutePath());
                    }
                }

                // Handle standard executable/JAR location
                java.io.File exeDir = codePath.getParent().toFile();
                assetsDir = new java.io.File(exeDir, "assets");
                if (assetsDir.exists() && assetsDir.isDirectory()) {
                    return Gdx.files.absolute(new java.io.File(assetsDir, normalizedPath.substring("assets/".length())).getAbsolutePath());
                }

                // For JAR files, try the directory containing the JAR
                if (codePath.toString().endsWith(".jar")) {
                    exeDir = codePath.getParent().toFile();
                    assetsDir = new java.io.File(exeDir, "assets");
                    if (assetsDir.exists() && assetsDir.isDirectory()) {
                        return Gdx.files.absolute(new java.io.File(assetsDir, normalizedPath.substring("assets/".length())).getAbsolutePath());
                    }
                }
            }
        } catch (Exception ignored) {
            // If something goes wrong, fall through to the default behavior
        }

        // Fallback to default behavior (Gdx will try from classpath)
        if (path.startsWith("assets/") || path.startsWith("assets\\")) {
            return Gdx.files.local(normalizedPath);
        } else {
            return Gdx.files.local("assets/" + normalizedPath);
        }
    }
}
