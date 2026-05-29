package net.krusher.tet4j.audio;

public record MusicMetadata(String title, String artist, String license) {

    public MusicMetadata(String title, String artist, String license) {
        this.title = title != null ? title : "Unknown";
        this.artist = artist != null ? artist : "Unknown";
        this.license = license != null ? license : "Unknown";
    }
}
