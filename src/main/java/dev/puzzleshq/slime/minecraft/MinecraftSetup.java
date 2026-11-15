package dev.puzzleshq.slime.minecraft;

import dev.puzzleshq.gibberishdefiner.JarRemapper;
import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.lunar.ConfigPair;
import dev.puzzleshq.slime.VersionResolver;
import dev.puzzleshq.slime.config.MinecraftConfig;
import dev.puzzleshq.slime.config.SlimeConfig;
import dev.puzzleshq.slime.minecraft.manifest.Version;
import dev.puzzleshq.slime.minecraft.manifest.VersionManifest;
import dev.puzzleshq.slime.minecraft.manifest.client.VersionData;
import dev.puzzleshq.slime.minecraft.manifest.client.libraries.Artifact;
import dev.puzzleshq.slime.minecraft.manifest.client.libraries.Library;
import dev.puzzleshq.slime.transform.SlimeConfigurations;
import dev.puzzleshq.slime.util.FileUtil;
import dev.puzzleshq.slime.util.JavaUtils;
import dev.puzzleshq.slime.util.OSUtil;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MinecraftSetup {

    public static void loadMinecraft(Project project, DependencyHandler handler, ConfigPair<SlimeConfig> pair) {
        SlimeConfig config = pair.getConfig();

        VersionManifest manifest = VersionResolver.getManifest(project, pair);
        Version version = manifest.find(config.minecraft.minecraftVersion.get());
        System.out.println("Found '" + manifest.versions.length + "' minecraft versions!");
        System.out.println("Found version '" + version.type + " " + version.id + "'");

        VersionData client = version.get(project);

        List<Library> toBeRemapped = loadLibrariesForVersion(project, handler, client);
        if (config.minecraft.mappingsSource.get().equals("none")) {
            downloadJars(project, handler, client, true);
            for (Library library : toBeRemapped) {
                handler.add(SlimeConfigurations.nonTransitiveInclude, library.name);
            }
        } else {
            downloadJars(project, handler, client, false);
            remapJars(project, handler, client, config.minecraft);
            for (Library library : toBeRemapped) {
                handler.add(SlimeConfigurations.remapConfigurationName, library.name);
            }
        }
    }

    private static void remapJars(Project project, DependencyHandler dependencies, VersionData versionData, MinecraftConfig mcCfg) {
        File clientFile = new File(FileUtil.getVersionFolder(project, versionData.id), "minecraft-" + versionData.id + "-client.jar");
        File remappedInfo = new File(FileUtil.getRemappedFolder(project, "minecraft", versionData.id), "minecraft-" + versionData.id + "-client.info");
        File remappedClientFile = new File(FileUtil.getRemappedFolder(project, "minecraft", versionData.id), "minecraft-" + versionData.id + "-client.jar");

        boolean doRemap;
        try {
            doRemap = !remappedInfo.exists() || !remappedClientFile.exists() || !new String(Files.readAllBytes(remappedInfo.toPath())).equals(mcCfg.mappingsSource.get());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (doRemap) {
            System.out.println("Remapping 'Minecraft " + versionData.id + "' using '" + mcCfg.mappingsSource.get() + "' mappings");
            try {
                Files.write(remappedInfo.toPath(), mcCfg.mappingsSource.get().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            IMapping mapping = Mappings.getMappings(project, versionData, mcCfg);

            JarRemapper.applyBareMapping(clientFile, remappedClientFile, mapping, false);
        }
        dependencies.add("implementation", project.files(remappedClientFile));
    }

    public static void downloadJars(Project project, DependencyHandler dependencies, VersionData versionData, boolean addDep) {
        File clientFile = new File(FileUtil.getVersionFolder(project, versionData.id), "minecraft-" + versionData.id + "-client.jar");
//        File serverFile = new File(FileUtil.getVersionFolder(project, versionData.id), "minecraft-" + versionData.id + "-server.jar");

        if (versionData.downloads.client != null && !clientFile.exists()) {
            try (InputStream stream = versionData.downloads.client.url.openStream()) {
                byte[] bytes = JavaUtils.readAllBytes(stream);
                FileOutputStream outputStream = new FileOutputStream(clientFile);
                outputStream.write(bytes);
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
//        if (versionData.downloads.server != null && !serverFile.exists()) {
//            try (InputStream stream = versionData.downloads.server.url.openStream()) {
//                byte[] bytes = JavaUtils.readAllBytes(stream);
//                FileOutputStream outputStream = new FileOutputStream(serverFile);
//                outputStream.write(bytes);
//                outputStream.close();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
        if (clientFile.exists() && addDep)
            dependencies.add("implementation", "com.mojang:minecraft:" + versionData.id + ":client");
    }

    private static void applyManifestVersionConstraints(Project project, Library[] libraries) {
        // Build a lookup map of group:name -> version straight from the manifest
        java.util.Map<String, String> requiredVersions = new java.util.HashMap<>();

        for (Library lib : libraries) {
            // lib.name is typically in standard Maven format: "group:name:version"
            String[] parts = lib.name.split("[:@]");
            if (parts.length >= 3) {
                String group = parts[0];
                String name = parts[1];
                String version = parts[2];
                requiredVersions.put(group + ":" + name, version);
            }
        }

        // Apply globally across all configurations so transitive bumps are clamped
        project.getConfigurations().all(configuration -> {
            configuration.getResolutionStrategy().eachDependency(details -> {
                String requestedKey = details.getRequested().getGroup() + ":" + details.getRequested().getName();

                if (requiredVersions.containsKey(requestedKey)) {
                    String forcedVersion = requiredVersions.get(requestedKey);
                    details.useVersion(forcedVersion);
                    details.because("Locked automatically to match Minecraft version requirements.");
                }
            });
        });
    }

    public static List<Library> loadLibrariesForVersion(Project project, DependencyHandler dependencies, VersionData client) {
        Library[] validLibs = client.getValidLibraries();
        System.out.println("Found '" + validLibs.length + "/" + client.libraries.length + "' valid libraries for this os '" + OSUtil.osId + "'");

        File wroteNatives = new File(FileUtil.getVersionNativesFolder(project, client.id), "i-wrote-natives-written.txt");

        applyManifestVersionConstraints(project, validLibs);

        List<Library> toBeRemapped = new ArrayList<>();
        for (Library validLib : validLibs) {
            if (validLib.downloads.artifact != null) {
                if (!validLib.name.contains("com.mojang")) {
                    dependencies.add(SlimeConfigurations.nonTransitiveInclude, validLib.name);
                } else {
                    toBeRemapped.add(validLib);
                }
            }
            if (!validLib.usesNewFormat() && validLib.getNativeArtifactId() != null) {
                if (wroteNatives.exists()) continue;

                Artifact artifact = validLib.getNativeArtifact();
                String fileName = new File(artifact.url.getFile()).getName();
                File nativeFile = new File(FileUtil.getVersionLibsFolder(project, client.id), fileName);
                if (nativeFile.exists()) {
                    extract(project, validLib, client, nativeFile);
                    continue;
                }

                byte[] bytes;
                try (InputStream stream = artifact.url.openStream()) {
                    bytes = JavaUtils.readAllBytes(stream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    if (!nativeFile.getParentFile().exists()) nativeFile.getParentFile().mkdirs();
                    nativeFile.createNewFile();
                    FileOutputStream outputStream = new FileOutputStream(nativeFile);
                    outputStream.write(bytes);
                    outputStream.close();
                    extract(project, validLib, client, nativeFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//                dependencies.add("implementation", validLib.getNativeArtifactId());
            }
        }
        if (!wroteNatives.exists()) {
            try {
                wroteNatives.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return toBeRemapped;
    }

    private static void extract(Project project, Library lib, VersionData client, File nativeFile) {
        try {
            ZipInputStream zipStream = new ZipInputStream(new FileInputStream(nativeFile));
            ZipEntry zipEntry;
            while ((zipEntry = zipStream.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) continue;
                boolean excluded = false;
                if (lib.extract != null) {
                    for (String s : lib.extract.exclude) {
                        if (zipEntry.getName().startsWith(s)) {
                            excluded = true;
                            break;
                        }
                    }
                }
                if (excluded) continue;
                File file = new File(FileUtil.getVersionNativesFolder(project, client.id), zipEntry.getName());
                if (!file.getParentFile().exists())
                    file.getParentFile().mkdirs();

                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(JavaUtils.readAllBytes(zipStream));
                fileOutputStream.close();
            }
            zipStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
