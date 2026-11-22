package dev.puzzleshq.sulphur.minecraft.manifest.client.libraries;

import java.util.Map;

public class Download {

    public Artifact artifact;
    public Map<String, Artifact> classifiers;

    @Override
    public String toString() {
        return "Download{" +
                "artifact=" + artifact +
                ", classifiers=" + classifiers +
                '}';
    }
}
