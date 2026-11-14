package dev.puzzleshq.slime.minecraft.manifest.client.logging;

public class LoggingFile {

    public String id;
    public String sha1;
    public int size;
    public String url;

    @Override
    public String toString() {
        return "LoggingFile{" +
                "id='" + id + '\'' +
                ", sha1='" + sha1 + '\'' +
                ", size=" + size +
                ", url='" + url + '\'' +
                '}';
    }
}
