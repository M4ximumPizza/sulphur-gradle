package dev.puzzleshq.slime;

import com.google.gson.Gson;
import dev.puzzleshq.lunar.ConfigPair;
import dev.puzzleshq.slime.minecraft.manifest.client.VersionData;
import dev.puzzleshq.slime.config.MinecraftConfig;
import dev.puzzleshq.slime.config.SlimeConfig;
import dev.puzzleshq.slime.minecraft.manifest.Version;
import dev.puzzleshq.slime.minecraft.manifest.VersionManifest;
import dev.puzzleshq.slime.util.FileUtil;
import dev.puzzleshq.slime.util.JavaUtils;
import org.gradle.api.Project;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class VersionResolver {

    public static final int SECOND = 1000;
    public static final int MINUTE = SECOND * 60;
    public static final int HOUR = MINUTE * 60;

    public static final ThreadLocal<Gson> GSON_LOCAL = ThreadLocal.withInitial(Gson::new);

    public static VersionManifest getManifest(Project project, ConfigPair<SlimeConfig> pair) {
        MinecraftConfig config = pair.getConfig().minecraft;
        String manifestSource = config.manifestSource.get();

        Gson gson = GSON_LOCAL.get();

        try {
            URL url = URI.create(manifestSource).toURL();
            File fileEpoc = new File(FileUtil.getSlimeCache(project), Paths.get(url.getFile()).getFileName().toFile().getName() + ".epoc");
            File file = new File(FileUtil.getSlimeCache(project), Paths.get(url.getFile()).getFileName().toFile().getName());
            long current = System.currentTimeMillis();
            if (!file.exists() || !fileEpoc.exists() || (current - Long.parseLong(new String(Files.readAllBytes(fileEpoc.toPath())))) >= VersionResolver.HOUR) {
                Files.write(fileEpoc.toPath(), String.valueOf(current).getBytes(StandardCharsets.UTF_8));
                file.createNewFile();

                String manifestData;
                try (InputStream stream = url.openStream()) {
                    manifestData = new String(JavaUtils.readAllBytes(stream), StandardCharsets.UTF_8);
                }

                pair.getSpec().save();
                FileWriter fileWriter = new FileWriter(file, false);
                fileWriter.write(manifestData);
                fileWriter.close();

                return gson.fromJson(manifestData, VersionManifest.class);
            } else {
                String manifestData;
                try (FileInputStream stream = new FileInputStream(file)) {
                    manifestData = new String(JavaUtils.readAllBytes(stream));
                }
                return gson.fromJson(manifestData, VersionManifest.class);
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid url '" + manifestSource + "' passed as manifest source!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static VersionData getClientJson(Project project, Version version) {
        Gson gson = GSON_LOCAL.get();
        try {
            File file = new File(FileUtil.getMinecraftManifests(project), Paths.get(version.url.getFile()).getFileName().toFile().getName());
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            if (!file.exists()) {
                file.createNewFile();
                String manifestData;
                try (InputStream stream = version.url.openStream()) {
                    manifestData = new String(JavaUtils.readAllBytes(stream), StandardCharsets.UTF_8);
                }
                FileWriter fileWriter = new FileWriter(file, false);
                fileWriter.write(manifestData);
                fileWriter.close();

                return gson.fromJson(manifestData, VersionData.class);
            } else {
                String manifestData;
                try (FileInputStream stream = new FileInputStream(file)) {
                    manifestData = new String(JavaUtils.readAllBytes(stream));
                }
                return gson.fromJson(manifestData, VersionData.class);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void deleteManifestFiles(Project project) {
        MinecraftConfig config = SlimeConfig.get(project).getConfig().minecraft;
        String manifestSource = config.manifestSource.get();

        try {
            URL url = URI.create(manifestSource).toURL();

            File fileEpoc = new File(FileUtil.getSlimeCache(project), Paths.get(url.getFile()).getFileName().toFile().getName() + ".epoc");
            File file = new File(FileUtil.getSlimeCache(project), Paths.get(url.getFile()).getFileName().toFile().getName());
            file.delete();
            fileEpoc.delete();

            FileUtil.delete(FileUtil.getMinecraftManifests(project));
            FileUtil.delete(FileUtil.getPuzzleManifests(project));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
