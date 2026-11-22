package dev.puzzleshq.sulphur.minecraft.manifest.version.client;

import java.net.URL;

public class AssetIndex {

    public String id;
    public String sha1;
    public long size;
    public long totalSize;
    public URL url;

    @Override
    public String toString() {
        return "AssetIndex{" +
                "id='" + id + '\'' +
                ", sha1='" + sha1 + '\'' +
                ", size=" + size +
                ", totalSize=" + totalSize +
                ", url=" + url +
                '}';
    }
}
