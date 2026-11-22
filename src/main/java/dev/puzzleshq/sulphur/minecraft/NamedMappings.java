package dev.puzzleshq.sulphur.minecraft;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.util.MappingUtil;
import dev.puzzleshq.lunar.ConfigPair;
import dev.puzzleshq.sulphur.VersionResolver;
import dev.puzzleshq.sulphur.config.MinecraftConfig;
import dev.puzzleshq.sulphur.config.SulpherConfig;
import dev.puzzleshq.sulphur.minecraft.manifest.Version;
import dev.puzzleshq.sulphur.minecraft.manifest.client.VersionData;
import dev.puzzleshq.sulphur.util.FileUtil;
import dev.puzzleshq.sulphur.util.JavaUtils;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.VisitableMappingTree;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NamedMappings {

    public static IMapping getOfficialMappings(Project project, VersionData versionData) {
        File mappingsFile = new File(FileUtil.getMappingsDir(project), new File(versionData.id + "-client-proguard.txt").getName());
        String mappingsData;
        if (!mappingsFile.exists()) {
            try (InputStream stream = versionData.downloads.client_mappings.url.openStream()) {
                byte[] bytes = JavaUtils.readAllBytes(stream);
                mappingsData = new String(bytes);
                Files.write(mappingsFile.toPath(), bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                mappingsData = new String(Files.readAllBytes(mappingsFile.toPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            VisitableMappingTree tree = MappingUtil.fromString(mappingsData, MappingFormat.PROGUARD_FILE);
            return MappingUtil.fromMappingTree(tree, "source", "target").reverse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String yarnMappingUrl = "https://maven.fabricmc.net/net/fabricmc/yarn/";

    public static IMapping getYarnMappings(Project project, String version, String yarnBuild) {
        try {
            String mappingId = version + "+" + yarnBuild;
            String tinyFileName = "yarn-" + mappingId + "-v2";

            URL finalUrl = URI.create(yarnMappingUrl + mappingId + "/" + tinyFileName + ".jar").toURL();

            File tinyFile = new File(FileUtil.getMappingsDir(project), tinyFileName + ".txt");
            if (tinyFile.exists()) {
                String mappingsData = new String(Files.readAllBytes(tinyFile.toPath()));

                VisitableMappingTree tree = MappingUtil.fromString(mappingsData, MappingFormat.TINY_2_FILE);
                return MappingUtil.fromMappingTree(tree, "intermediary", "named");
            }

            File mappingsFile = new File(FileUtil.getMappingsDir(project), new File(tinyFileName + ".jar").getName());
            if (!mappingsFile.exists()) {
                mappingsFile.getParentFile().mkdirs();
                mappingsFile.createNewFile();
                try (InputStream stream = finalUrl.openStream()) {
                    byte[] bytes = JavaUtils.readAllBytes(stream);
                    Files.write(mappingsFile.toPath(), bytes);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            String mappingsData;
            try (ZipFile zip = new ZipFile(mappingsFile)) {
                ZipEntry entry = zip.getEntry("mappings/mappings.tiny");

                try (InputStream stream = zip.getInputStream(entry)) {
                    FileOutputStream outputStream = new FileOutputStream(tinyFile);
                    byte[] bytes = JavaUtils.readAllBytes(stream);
                    mappingsData = new String(bytes);
                    outputStream.write(bytes);
                    outputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            VisitableMappingTree tree = MappingUtil.fromString(mappingsData, MappingFormat.TINY_2_FILE);
            return MappingUtil.fromMappingTree(tree, "intermediary", "named");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String legacyYarnMappingUrl = "https://repo.legacyfabric.net/legacyfabric/net/legacyfabric/legacy-yarn/";

    public static IMapping getLegacyYarnMappings(Project project, String version, String yarnBuild) {
        try {
            String mappingId = version + "+" + yarnBuild;
            String tinyFileName = "legacy-yarn-" + mappingId + "-v2";

            URL finalUrl = URI.create(legacyYarnMappingUrl + mappingId + "/" + tinyFileName + ".jar").toURL();

            File tinyFile = new File(FileUtil.getMappingsDir(project), tinyFileName + ".txt");
            if (tinyFile.exists()) {
                String mappingsData = new String(Files.readAllBytes(tinyFile.toPath()));

                VisitableMappingTree tree = MappingUtil.fromString(mappingsData, MappingFormat.TINY_2_FILE);
                return MappingUtil.fromMappingTree(tree, "intermediary", "named");
            }

            File mappingsFile = new File(FileUtil.getMappingsDir(project), new File(tinyFileName + ".jar").getName());
            if (!mappingsFile.exists()) {
                mappingsFile.createNewFile();
                try (InputStream stream = finalUrl.openStream()) {
                    byte[] bytes = JavaUtils.readAllBytes(stream);
                    Files.write(mappingsFile.toPath(), bytes);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            String mappingsData;
            try (ZipFile zip = new ZipFile(mappingsFile)) {
                ZipEntry entry = zip.getEntry("mappings/mappings.tiny");

                try (InputStream stream = zip.getInputStream(entry)) {
                    FileOutputStream outputStream = new FileOutputStream(tinyFile);
                    byte[] bytes = JavaUtils.readAllBytes(stream);
                    mappingsData = new String(bytes);
                    outputStream.write(bytes);
                    outputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            VisitableMappingTree tree = MappingUtil.fromString(mappingsData, MappingFormat.TINY_2_FILE);
            return MappingUtil.fromMappingTree(tree, "intermediary", "named");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String orinthemcMappingUrl = "https://maven.ornithemc.net/releases/net/ornithemc/feather-gen2/";

    public static IMapping getOrinthemcMappings(Project project, String version, String ornithemcBuild) {
        try {
            String mappingId = version + "+" + ornithemcBuild;
            String tinyFileName = "feather-gen2-" + mappingId + "-v2";

            URL finalUrl = URI.create(orinthemcMappingUrl + mappingId + "/" + tinyFileName + ".jar").toURL();

            File tinyFile = new File(FileUtil.getMappingsDir(project), tinyFileName + ".txt");
            if (tinyFile.exists()) {
                String mappingsData = new String(Files.readAllBytes(tinyFile.toPath()));

                VisitableMappingTree tree = MappingUtil.fromString(mappingsData, MappingFormat.TINY_2_FILE);
                return MappingUtil.fromMappingTree(tree, "intermediary", "named");
            }

            File mappingsFile = new File(FileUtil.getMappingsDir(project), new File(tinyFileName + ".jar").getName());
            if (!mappingsFile.exists()) {
                mappingsFile.createNewFile();
                try (InputStream stream = finalUrl.openStream()) {
                    byte[] bytes = JavaUtils.readAllBytes(stream);
                    Files.write(mappingsFile.toPath(), bytes);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            String mappingsData;
            try (ZipFile zip = new ZipFile(mappingsFile)) {
                ZipEntry entry = zip.getEntry("mappings/mappings.tiny");

                try (InputStream stream = zip.getInputStream(entry)) {
                    FileOutputStream outputStream = new FileOutputStream(tinyFile);
                    byte[] bytes = JavaUtils.readAllBytes(stream);
                    mappingsData = new String(bytes);
                    outputStream.write(bytes);
                    outputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            VisitableMappingTree tree = MappingUtil.fromString(mappingsData, MappingFormat.TINY_2_FILE);
            return MappingUtil.fromMappingTree(tree, "intermediary", "named");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static IMapping getMappings(Project project, String version) {
        ConfigPair<SulpherConfig> configPair = SulpherConfig.get(project);
        SulpherConfig slimeConfig = configPair.getConfig();
        MinecraftConfig minecraftConfig = slimeConfig.minecraft;

        String[] sourceParts = minecraftConfig.mappingsSource.get().split(":");

        switch (sourceParts[0]) {
            case "none": return null;
            case "official": {
                Version gameVersion = VersionResolver.getManifest(project, SulpherConfig.get(project)).find(version);
                VersionData versionData = gameVersion.get(project);
                return getOfficialMappings(project, versionData);
            }
            case "yarn": return getYarnMappings(project, version, sourceParts[1]);
            case "legacy-yarn": return getLegacyYarnMappings(project, version, sourceParts[1]);
            case "ornithemc": return getOrinthemcMappings(project, version, sourceParts[1]);
            default: throw new IllegalArgumentException("Unknown mapping type '" + minecraftConfig.mappingsSource.get() + "'");
        }
    }

    public static IMapping getMappings(Project project, VersionData versionData, MinecraftConfig config) {
        String[] sourceParts = config.mappingsSource.get().split(":");

        switch (sourceParts[0]) {
            case "none": return null;
            case "official": return getOfficialMappings(project, versionData);
            case "yarn": return getYarnMappings(project, versionData.id, sourceParts[1]);
            case "legacy-yarn": return getLegacyYarnMappings(project, versionData.id, sourceParts[1]);
            case "ornithemc": return getOrinthemcMappings(project, versionData.id, sourceParts[1]);
            default: throw new IllegalArgumentException("Unknown mapping type '" + config.mappingsSource.get() + "'");
        }
    }
}
