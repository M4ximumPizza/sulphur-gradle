package dev.puzzleshq.slime.minecraft;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.util.MappingUtil;
import dev.puzzleshq.slime.minecraft.manifest.client.VersionData;
import dev.puzzleshq.slime.util.FileUtil;
import dev.puzzleshq.slime.util.JavaUtils;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTreeView;
import net.fabricmc.mappingio.tree.VisitableMappingTree;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class Mappings {

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

    public static IMapping getYarnMappings(Project project, VersionData versionData, String yarnBuild) {
        try {
            String mappingId = versionData.id + "+" + yarnBuild;
            String tinyFileName = "yarn-" + mappingId + "-v2";

            URL finalUrl = URI.create(yarnMappingUrl + mappingId + "/" + tinyFileName + ".jar").toURL();

            File tinyFile = new File(FileUtil.getMappingsDir(project), tinyFileName + ".txt");
            if (tinyFile.exists()) {
                String mappingsData = new String(Files.readAllBytes(tinyFile.toPath()));

                VisitableMappingTree tree = MappingUtil.fromString(mappingsData, MappingFormat.TINY_2_FILE);
                return MappingUtil.fromMappingTree(tree, "official", "named");
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
            return MappingUtil.fromMappingTree(tree, "official", "named").reverse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String legacyYarnMappingUrl = "https://repo.legacyfabric.net/legacyfabric/net/legacyfabric/legacy-yarn/";

    public static IMapping getLegacyYarnMappings(Project project, VersionData versionData, String yarnBuild) {
        try {
            String mappingId = versionData.id + "+" + yarnBuild;
            String tinyFileName = "legacy-yarn-" + mappingId;

            URL finalUrl = URI.create(legacyYarnMappingUrl + mappingId + "/" + tinyFileName + ".jar").toURL();

            File tinyFile = new File(FileUtil.getMappingsDir(project), tinyFileName + ".txt");
            if (tinyFile.exists()) {
                String mappingsData = new String(Files.readAllBytes(tinyFile.toPath()));

                VisitableMappingTree tree = MappingUtil.fromString(mappingsData, MappingFormat.TINY_FILE);
                return MappingUtil.fromMappingTree(tree, "official", "named");
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

            VisitableMappingTree tree = MappingUtil.fromString(mappingsData, MappingFormat.TINY_FILE);
            return MappingUtil.fromMappingTree(tree, "official", "named");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String orinthemcMappingUrl = "https://maven.ornithemc.net/releases/net/ornithemc/feather-gen2/";

    public static IMapping getOrinthemcMappings(Project project, VersionData versionData, String ornithemcBuild) {
        try {
            String mappingId = versionData.id + "+" + ornithemcBuild;
            String tinyFileName = "feather-gen2-" + mappingId;

            URL finalUrl = URI.create(orinthemcMappingUrl + mappingId + "/" + tinyFileName + ".jar").toURL();

            File tinyFile = new File(FileUtil.getMappingsDir(project), tinyFileName + ".txt");
            if (tinyFile.exists()) {
                String mappingsData = new String(Files.readAllBytes(tinyFile.toPath()));

                VisitableMappingTree tree = MappingUtil.fromString(mappingsData, MappingFormat.TINY_FILE);
                boolean isSplit = tree.getNamespaceId("clientOfficial") != MappingTreeView.NULL_NAMESPACE_ID;
                return MappingUtil.fromMappingTree(tree, isSplit ? "clientOfficial" : "official", "named");
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

            VisitableMappingTree tree = MappingUtil.fromString(mappingsData, MappingFormat.TINY_FILE);
            boolean isSplit = tree.getNamespaceId("clientOfficial") != MappingTreeView.NULL_NAMESPACE_ID;

            return MappingUtil.fromMappingTree(tree, isSplit ? "clientOfficial" : "official", "named");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
