package dev.puzzleshq.slime.minecraft.manifest.client.libraries;

import java.io.File;
import java.net.URL;

public class Artifact {

    public String path;
    public String sha1;
    public long size;
    public URL url;

    @Override
    public String toString() {
        return "Artifact{" +
                "path=" + path +
                ", sha1='" + sha1 + '\'' +
                ", size=" + size +
                ", url=" + url +
                '}';
    }
}
