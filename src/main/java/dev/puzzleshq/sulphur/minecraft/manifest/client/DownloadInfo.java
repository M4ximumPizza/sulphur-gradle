package dev.puzzleshq.sulphur.minecraft.manifest.client;

import java.net.URL;

public class DownloadInfo {

    public String sha1;
    public long size;
    public URL url;

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "sha1='" + sha1 + '\'' +
                ", size=" + size +
                ", url=" + url +
                '}';
    }
}
