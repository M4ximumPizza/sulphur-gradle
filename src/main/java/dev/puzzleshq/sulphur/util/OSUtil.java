package dev.puzzleshq.sulphur.util;

import java.util.Locale;

public class OSUtil {

    public static final boolean isLinux = (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("nix") || System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("nux"));
    public static final boolean isMac = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac");
    public static final boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");

    public static final String osId = isLinux ? "linux" : (isWindows ? "windows" : (isMac ? "osx" : null));
}
