package net.krusher.tet4j.util;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MetadataIndexer {

    private static final String[] AUDIO_EXTENSIONS = {"mp3", "ogg"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: MetadataIndexer <assets-dir>");
            System.exit(1);
        }

        File assetsDir = new File(args[0]);
        if (!assetsDir.exists() || !assetsDir.isDirectory()) {
            System.err.println("Assets directory not found: " + assetsDir);
            System.exit(1);
        }

        File musicDir = new File(assetsDir, "music");
        if (!musicDir.exists() || !musicDir.isDirectory()) {
            System.err.println("Music directory not found: " + musicDir);
            System.exit(0);
        }

        List<String[]> entries = new ArrayList<>();
        scanDir(musicDir, musicDir, entries);

        File outputFile = new File(musicDir, "tags.csv");
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("path|title|artist|license\n");
            for (String[] entry : entries) {
                writer.write(entry[0] + "|" + escape(entry[1]) + "|" + escape(entry[2]) + "|" + escape(entry[3]) + "\n");
            }
            System.out.println("Generated " + outputFile.getAbsolutePath() + " with " + entries.size() + " entries.");
        } catch (IOException e) {
            System.err.println("Error writing tags.csv: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void scanDir(File baseDir, File dir, List<String[]> entries) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDir(baseDir, file, entries);
            } else {
                String name = file.getName().toLowerCase();
                boolean isAudio = false;
                for (String ext : AUDIO_EXTENSIONS) {
                    if (name.endsWith("." + ext)) {
                        isAudio = true;
                        break;
                    }
                }
                if (!isAudio) continue;

                String relativePath = "music/"
                    + baseDir.toPath().relativize(file.toPath()).toString().replace("\\", "/");

                String title = null;
                String artist = null;
                String license = null;

                try {
                    AudioFile af = AudioFileIO.read(file);
                    Tag tag = af.getTag();
                    if (tag != null) {
                        title = tag.getFirst(FieldKey.TITLE);
                        artist = tag.getFirst(FieldKey.ARTIST);
                        license = tag.getFirst(FieldKey.COPYRIGHT);
                        if (license == null || license.isEmpty()) {
                            license = tag.getFirst("LICENSE");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Warning: could not read tags from " + file.getName() + ": " + e.getMessage());
                }

                if (title == null || title.isEmpty()) title = file.getName();
                if (artist == null || artist.isEmpty()) artist = "Unknown";
                if (license == null || license.isEmpty()) license = "Unknown";

                entries.add(new String[]{relativePath, title, artist, license});
            }
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("|", "/").replace("\n", " ").replace("\r", " ");
    }
}
