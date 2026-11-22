package dev.puzzleshq.sulphur.minecraft.manifest.client;

public class JavaVersion {
    public String component;
    public int majorVersion;

    @Override
    public String toString() {
        return "JavaVersion{" +
                "component='" + component + '\'' +
                ", majorVersion=" + majorVersion +
                '}';
    }
}
