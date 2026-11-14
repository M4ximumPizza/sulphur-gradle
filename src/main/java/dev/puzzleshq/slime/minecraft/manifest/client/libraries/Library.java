package dev.puzzleshq.slime.minecraft.manifest.client.libraries;

import dev.puzzleshq.slime.util.OSUtil;

import java.util.Arrays;
import java.util.Map;

public class Library {

    public Download downloads;
    public String name;
    public String url;
    public Rule[] rules;
    public Extract extract;
    public Map<String, String> natives;

    public boolean shouldDownload() {
        if (name.contains("org.ow2.asm:")) return false;
        if (rules == null) return true;
        boolean allowed = false;

        for (Rule rule : rules) {
            if (rule.os == null || rule.os.isCorrectOs()) {
                allowed = rule.action.equals("allow");
            }
        }

        return allowed;
    }

    public boolean usesNewFormat() {
        return downloads.classifiers == null || natives == null;
    }

    public Artifact getNativeArtifact() {
        if (usesNewFormat()) {
            return null;
        }

        String nativesClassifier = natives.get(OSUtil.osId);
        return downloads.classifiers.get(nativesClassifier);
    }

    public String getNativeArtifactId() {
        if (usesNewFormat()) {
            return null;
        }

        if (natives.get(OSUtil.osId) == null)
            return null;

        return name + ":" + (natives.get(OSUtil.osId).replaceAll("\\$\\{arch}", System.getProperty("os.arch").equals("x86") ? "32" : "64"));
    }

    @Override
    public String toString() {
        return "Library{" +
                "downloads=" + downloads +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", rules=" + Arrays.toString(rules) +
                ", extract=" + extract +
                ", natives=" + natives +
                '}';
    }
}
