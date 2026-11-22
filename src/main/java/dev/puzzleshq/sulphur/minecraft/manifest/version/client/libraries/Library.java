package dev.puzzleshq.sulphur.minecraft.manifest.version.client.libraries;

import dev.puzzleshq.sulphur.util.OSUtil;

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
                allowed = "allow".equals(rule.action);
            }
        }

        return allowed;
    }

    public boolean usesNewFormat() {
        return downloads == null ||
                downloads.classifiers == null ||
                natives == null;
    }

    /**
     * Resolves the native classifier for the current OS and JVM architecture.
     */
    public String getNativeClassifier() {
        if (usesNewFormat() || OSUtil.osId == null) {
            return null;
        }

        String classifier = natives.get(OSUtil.osId);

        if (classifier == null) {
            return null;
        }

        return classifier.replace("${arch}", OSUtil.archBits);
    }

    /**
     * Gets the native artifact for the current architecture.
     */
    public Artifact getNativeArtifact() {
        String classifier = getNativeClassifier();

        if (classifier == null) {
            return null;
        }

        return downloads.classifiers.get(classifier);
    }

    /**
     * Gets the Maven dependency identifier for the native artifact.
     */
    public String getNativeArtifactId() {
        String classifier = getNativeClassifier();

        if (classifier == null) {
            return null;
        }

        return name + ":" + classifier;
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