package dev.puzzleshq.slime.util;

import org.gradle.api.Project;

import java.io.File;

public class FileUtil {

    private static File slimeCache;
    private static File jarsDir;
    private static File natives;
    private static File libsDir;
    private static File mappingsDir;

    public static File getSlimeCache(Project project) {
        if (slimeCache != null) return slimeCache;
        File slimeCache = new File(project.getGradle().getGradleUserHomeDir(), ".slime-cache");
        if (!slimeCache.exists()) slimeCache.mkdirs();
        FileUtil.slimeCache = slimeCache;
        return slimeCache;
    }

    public static File getJarsDir(Project project) {
        if (jarsDir != null) return jarsDir;
        File jarsDir = new File(getSlimeCache(project), "jars");
        if (!jarsDir.exists()) jarsDir.mkdirs();
        FileUtil.jarsDir = jarsDir;
        return jarsDir;
    }

    public static File getLibsDir(Project project) {
        if (libsDir != null) return libsDir;
        File libsDir = new File(getSlimeCache(project), "libs");
        if (!libsDir.exists()) libsDir.mkdirs();
        FileUtil.libsDir = libsDir;
        return libsDir;
    }

    public static File getMappingsDir(Project project) {
        if (mappingsDir != null) return mappingsDir;
        File mappingsDir = new File(getSlimeCache(project), "mappings");
        if (!mappingsDir.exists()) mappingsDir.mkdirs();
        FileUtil.mappingsDir = mappingsDir;
        return mappingsDir;
    }

    public static File getNativesDir(Project project) {
        if (natives != null) return natives;
        File nativesDir = new File(getSlimeCache(project), "natives");
        if (!nativesDir.exists()) nativesDir.mkdirs();
        FileUtil.natives = nativesDir;
        return nativesDir;
    }

    public static File getVersionFolder(Project project, String version) {
        File file = new File(getJarsDir(project), "com.mojang/minecraft/" + version);
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    public static File getRemappedFolder(Project project, String artifact, String version) {
        File file = new File(getJarsDir(project), "puzzle.remapped/" + artifact + "/" + version);
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    public static File getVersionLibsFolder(Project project, String version) {
        File file = new File(getLibsDir(project), version);
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    public static File getVersionNativesFolder(Project project, String version) {
        File file = new File(getNativesDir(project), version);
        if (!file.exists())
            file.mkdirs();
        return file;
    }

}
