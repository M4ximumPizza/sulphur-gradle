package dev.puzzleshq.sulphur.util;

import java.util.Locale;

public final class OSUtil {

    private static final String OS_NAME =
            System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

    private static final String OS_ARCH =
            System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);

    public static final boolean isLinux =
            OS_NAME.contains("nix") ||
                    OS_NAME.contains("nux") ||
                    OS_NAME.contains("linux");

    public static final boolean isMac =
            OS_NAME.contains("mac");

    public static final boolean isWindows =
            OS_NAME.contains("win");

    public static final String osId =
            isLinux ? "linux" :
                    isWindows ? "windows" :
                            isMac ? "osx" : null;

    /**
     * JVM architecture bitness.
     */
    public static final int ARCH_BITS = detectArchitecture();

    public static final boolean is64Bit = ARCH_BITS == 64;
    public static final boolean is32Bit = ARCH_BITS == 32;

    /**
     * Mojang's native classifier architecture value.
     */
    public static final String archBits = String.valueOf(ARCH_BITS);

    private OSUtil() {
    }

    private static int detectArchitecture() {
        // Prefer the actual JVM data model.
        String dataModel = System.getProperty("sun.arch.data.model");

        if ("32".equals(dataModel)) return 32;
        if ("64".equals(dataModel)) return 64;

        // Fallback to the architecture name.
        switch (OS_ARCH) {
            case "x86":
            case "i386":
            case "i486":
            case "i586":
            case "i686":
            case "arm":
            case "arm32":
                return 32;

            case "x86_64":
            case "amd64":
            case "aarch64":
            case "arm64":
            case "ppc64":
            case "ppc64le":
            case "s390x":
            case "mips64":
            case "riscv64":
                return 64;

            default:
                throw new IllegalStateException(
                        "Unable to determine JVM architecture: " + OS_ARCH
                );
        }
    }
}