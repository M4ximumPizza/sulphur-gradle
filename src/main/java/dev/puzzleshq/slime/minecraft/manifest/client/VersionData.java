package dev.puzzleshq.slime.minecraft.manifest.client;

import dev.puzzleshq.slime.minecraft.manifest.client.libraries.Library;
import dev.puzzleshq.slime.minecraft.manifest.client.logging.Logging;

import java.util.Arrays;

public class VersionData {

    public String id;
    public String mainClass;
    public String releaseTime;
    public String time;
    public String type;
    public JavaVersion javaVersion;
    public Downloads downloads;
    public AssetIndex assetIndex;
    public String assets;
    public Library[] libraries;
    public Logging logging;
    public String minecraftArguments;
    public Args arguments;

    public Library[] getValidLibraries() {
        return Arrays.stream(libraries).filter(Library::shouldDownload).toArray(Library[]::new);
    }

    public String getReleaseTime() {
        if (releaseTime != null) return releaseTime;
        if (time != null) return time;
        return null;
    }

    @Override
    public String toString() {
        return "VersionData{" +
                "id='" + id + '\'' +
                ", mainClass='" + mainClass + '\'' +
                ", releaseTime='" + releaseTime + '\'' +
                ", time='" + time + '\'' +
                ", type='" + type + '\'' +
                ", javaVersion=" + javaVersion +
                ", downloads=" + downloads +
                ", assetIndex=" + assetIndex +
                ", assets='" + assets + '\'' +
                ", libraries=" + Arrays.toString(libraries) +
                ", logging=" + logging +
                ", minecraftArguments='" + minecraftArguments + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}
