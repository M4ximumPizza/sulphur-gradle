package dev.puzzleshq.slime.minecraft.manifest.client.libraries;

import dev.puzzleshq.slime.util.OSUtil;

import java.util.regex.Pattern;

public class OSRule {

    public String name;
    public String version;

    private transient Pattern pattern;

    /// "linux": "natives-linux",
    /// "osx": "natives-osx",
    /// "windows": "natives-windows"
    public boolean isCorrectOs() {
        if (pattern == null && version != null) pattern = Pattern.compile(version);

        String osVersion = System.getProperty("os.version");

        if (name.equals(OSUtil.osId))
            return version == null || pattern.matcher(osVersion).matches();

        return false;
    }

    @Override
    public String toString() {
        return "OSRule{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", pattern=" + pattern +
                '}';
    }
}
