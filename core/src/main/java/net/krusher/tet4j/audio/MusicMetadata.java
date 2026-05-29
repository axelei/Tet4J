package net.krusher.tet4j.audio;

import com.badlogic.gdx.files.FileHandle;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

public record MusicMetadata(String title, String artist, String license) {

    public MusicMetadata(String title, String artist, String license) {
        this.title = title != null ? title : "Unknown";
        this.artist = artist != null ? artist : "Unknown";
        this.license = license != null ? license : "Unknown";
    }

    public static MusicMetadata fromFile(FileHandle file) {
        try {
            AudioFile af = AudioFileIO.read(file.file());
            Tag tag = af.getTag();
            String fallback = file.nameWithoutExtension();
            String t = tag.getFirst(FieldKey.TITLE);
            String a = tag.getFirst(FieldKey.ARTIST);
            String l = tag.getFirst(FieldKey.COPYRIGHT);
            if (l == null || l.isEmpty()) {
                l = tag.getFirst("LICENSE");
            }
            if (t == null || t.isEmpty()) {
                t = fallback;
            }
            return new MusicMetadata(t, a, l);
        } catch (Exception e) {
            return new MusicMetadata(file.nameWithoutExtension(), null, null);
        }
    }
}
