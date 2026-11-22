package dev.puzzleshq.sulphur.minecraft;

import dev.puzzleshq.gibberishdefiner.JarRemapper;
import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.lunar.ConfigPair;
import dev.puzzleshq.sulphur.VersionResolver;
import dev.puzzleshq.sulphur.config.MinecraftConfig;
import dev.puzzleshq.sulphur.config.SulpherConfig;
import dev.puzzleshq.sulphur.minecraft.manifest.Version;
import dev.puzzleshq.sulphur.minecraft.manifest.VersionManifest;
import dev.puzzleshq.sulphur.minecraft.manifest.client.VersionData;
import dev.puzzleshq.sulphur.minecraft.manifest.client.libraries.Artifact;
import dev.puzzleshq.sulphur.minecraft.manifest.client.libraries.Library;
import dev.puzzleshq.sulphur.transform.LocalMaven;
import dev.puzzleshq.sulphur.transform.SulphurConfigurations;
import dev.puzzleshq.sulphur.util.FileUtil;
import dev.puzzleshq.sulphur.util.JavaUtils;
import dev.puzzleshq.sulphur.util.OSUtil;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MinecraftSetup {

    public static void loadMinecraft(Project project, DependencyHandler handler, ConfigPair<SulpherConfig> pair) {
        SulpherConfig config = pair.getConfig();
        MinecraftConfig minecraft = config.minecraft;

        VersionManifest manifest = VersionResolver.getManifest(project, pair);
        Version version = manifest.find(minecraft.minecraftVersion.get());
        VersionData client = version.get(project);

        System.out.println("Found '" + manifest.versions.length + "' minecraft versions!");
        System.out.println("Found version '" + version.type + " " + version.id + "'");

        List<Library> libraries = loadLibrariesForVersion(project, handler, client);
        boolean remapping = !"none".equals(minecraft.mappingsSource.get());

        downloadJars(project, handler, client, !remapping);

        if (!remapping) {
            libraries.forEach(library ->
                    handler.add(SulphurConfigurations.nonTransitiveInclude, library.name)
            );

            registerUnmappedDependencies(project, handler);
            return;
        }

        remapJars(project, handler, client, minecraft);

        for (Library library : libraries) {
            handler.add(SulphurConfigurations.internalRemapConfigurationName, library.name);

            File remapped = LocalMaven.getArtifactFile(
                    project,
                    LocalMaven.transformName(library.name)
            );

            if (remapped.isFile()) {
                handler.add(SulphurConfigurations.nonTransitiveInclude, project.files(remapped));
            }
        }

        registerRemappedDependencies(project, handler);
    }

    private static void registerUnmappedDependencies(Project project, DependencyHandler handler) {
        project.afterEvaluate(p -> {
            for (ResolvedArtifactResult artifact :
                    SulphurConfigurations.apiRemapConfiguration
                            .getIncoming()
                            .getArtifacts()) {

                handler.add(
                        SulphurConfigurations.nonTransitiveInclude,
                        artifact.getFile()
                );
            }
        });
    }

    private static void registerRemappedDependencies(Project project, DependencyHandler handler) {
        project.afterEvaluate(p -> {
            for (ResolvedArtifactResult artifact :
                    SulphurConfigurations.apiRemapConfiguration
                            .getIncoming()
                            .getArtifacts()) {

                File input = artifact.getFile();
                String filename = input.getName();

                int extension = filename.lastIndexOf('.');
                String artifactName = extension == -1
                        ? filename
                        : filename.substring(0, extension);

                File directory = LocalMaven.getArtifactDir(
                        project,
                        LocalMaven.transformName("files:" + artifactName + ":0.0.0")
                );

                File remapped = new File(directory, artifactName + "-remapped.jar");

                if (remapped.isFile()) {
                    handler.add(
                            SulphurConfigurations.nonTransitiveInclude,
                            project.files(remapped)
                    );
                }
            }
        });
    }

    public static void remapJars(Project project, DependencyHandler dependencies, VersionData versionData, MinecraftConfig mcCfg) {
        File clientFile = new File(FileUtil.getVersionFolder(project, versionData.id), "minecraft-" + versionData.id + "-client.jar");
        File dir = LocalMaven.getArtifactDir(project, "puzzle.remap.com.mojang:minecraft:" + versionData.id);
        if (!dir.exists()) dir.mkdirs();
        File remappedInfo = new File(dir, "minecraft-" + versionData.id + "-client.info");
        File remappedClientFile = new File(dir, "minecraft-" + versionData.id + "-client.jar");

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

    public static void downloadJars(
            Project project,
            DependencyHandler dependencies,
            VersionData version,
            boolean addDependency
    ) {
        File clientJar = new File(
                FileUtil.getVersionFolder(project, version.id),
                "minecraft-" + version.id + "-client.jar"
        );

        if (!clientJar.isFile() && version.downloads.client != null) {
            try {
                File parent = clientJar.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Could not create directory: " + parent);
                }

                try (InputStream input = version.downloads.client.url.openStream()) {
                    Files.copy(input, clientJar.toPath());
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to download Minecraft " + version.id, e);
            }
        }

        if (addDependency && clientJar.isFile()) {
            dependencies.add(
                    "implementation",
                    "com.mojang:minecraft:" + version.id + ":client"
            );
        }
    }

    private static void applyManifestVersionConstraints(Project project, Library[] libraries) {
        // Build a lookup map of group:name -> version straight from the manifest
        Map<String, String> requiredVersions = getRequiredVersionsMap(libraries);

        // Apply globally across all configurations so transitive bumps are clamped
        project.getConfigurations().all(configuration -> configuration.getResolutionStrategy().eachDependency(details -> {
            String requestedKey = details.getRequested().getGroup() + ":" + details.getRequested().getName();

            if (requiredVersions.containsKey(requestedKey)) {
                String forcedVersion = requiredVersions.get(requestedKey);
                details.useVersion(forcedVersion);
                details.because("Locked automatically to match Minecraft version requirements.");
            }
        }));
    }

    private static @NotNull Map<String, String> getRequiredVersionsMap(Library[] libraries) {
        Map<String, String> requiredVersions = new java.util.HashMap<>();

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
        return requiredVersions;
    }

    public static List<Library> loadLibrariesForVersion(
            Project project,
            DependencyHandler dependencies,
            VersionData client
    ) {
        Library[] libraries = client.getValidLibraries();

        System.out.println(
                "Found '" + libraries.length + "/" + client.libraries.length +
                        "' valid libraries for this os '" + OSUtil.osId + "'"
        );

        applyManifestVersionConstraints(project, libraries);

        File nativeDir = FileUtil.getVersionNativesFolder(project, client.id);
        File libraryDir = FileUtil.getVersionLibsFolder(project, client.id);
        File nativeMarker = new File(nativeDir, "i-wrote-natives-written.txt");

        boolean extractNatives = !nativeMarker.isFile();

        List<Library> toBeRemapped = new ArrayList<>();

        for (Library library : libraries) {
            if (library.downloads.artifact != null) {
                if (library.name.contains("com.mojang")) {
                    toBeRemapped.add(library);
                } else {
                    dependencies.add(
                            SulphurConfigurations.nonTransitiveInclude,
                            library.name
                    );
                }
            }

            if (!extractNatives || library.usesNewFormat()) {
                continue;
            }

            if (library.getNativeArtifactId() == null) {
                continue;
            }

            Artifact artifact = library.getNativeArtifact();
            String filename = new File(artifact.url.getFile()).getName();

            File nativeJar = new File(libraryDir, filename);

            try {
                if (!nativeJar.isFile()) {
                    File parent = nativeJar.getParentFile();

                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Could not create directory: " + parent);
                    }

                    try (InputStream input = artifact.url.openStream()) {
                        Files.copy(input, nativeJar.toPath());
                    }
                }

                extract(project, library, client, nativeJar);

            } catch (IOException e) {
                throw new RuntimeException("Failed to process native library: " + filename, e);
            }
        }

        if (extractNatives) {
            try {
                if (!nativeDir.isDirectory() && !nativeDir.mkdirs()) {
                    throw new IOException("Could not create native directory: " + nativeDir);
                }

                Files.createFile(nativeMarker.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to create native extraction marker", e);
            }
        }

        return toBeRemapped;
    }

    private static void extract(
            Project project,
            Library library,
            VersionData client,
            File nativeJar
    ) {
        File nativeDir = FileUtil.getVersionNativesFolder(project, client.id);

        try (ZipInputStream zip = new ZipInputStream(
                Files.newInputStream(nativeJar.toPath())
        )) {
            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                String name = entry.getName();

                if (isExcluded(library, name)) {
                    continue;
                }

                File output = new File(nativeDir, name);

                // Prevent ZIP entries from escaping the native directory.
                if (!output.toPath().normalize().startsWith(nativeDir.toPath().normalize())) {
                    throw new IOException("Invalid native archive entry: " + name);
                }

                File parent = output.getParentFile();

                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Could not create directory: " + parent);
                }

                Files.copy(zip, output.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to extract natives from " + nativeJar, e);
        }
    }

    private static boolean isExcluded(Library library, String name) {
        if (library.extract == null || library.extract.exclude == null) {
            return false;
        }

        for (String excluded : library.extract.exclude) {
            if (name.startsWith(excluded)) {
                return true;
            }
        }

        return false;
    }
}
