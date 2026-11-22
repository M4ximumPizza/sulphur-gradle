package dev.puzzleshq.sulphur.minecraft;

import dev.puzzleshq.gibberishdefiner.JarRemapper;
import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.lunar.ConfigPair;
import dev.puzzleshq.sulphur.VersionResolver;
import dev.puzzleshq.sulphur.config.MinecraftConfig;
import dev.puzzleshq.sulphur.config.SulpherConfig;
import dev.puzzleshq.sulphur.minecraft.manifest.assets.Asset;
import dev.puzzleshq.sulphur.minecraft.manifest.assets.AssetsManifest;
import dev.puzzleshq.sulphur.minecraft.manifest.version.Version;
import dev.puzzleshq.sulphur.minecraft.manifest.version.VersionManifest;
import dev.puzzleshq.sulphur.minecraft.manifest.version.client.VersionData;
import dev.puzzleshq.sulphur.minecraft.manifest.version.client.libraries.Artifact;
import dev.puzzleshq.sulphur.minecraft.manifest.version.client.libraries.Library;
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
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MinecraftSetup {

    public static void loadMinecraft(Project project, DependencyHandler handler, ConfigPair<SulpherConfig> pair) {
        SulpherConfig config = pair.getConfig();

        VersionManifest manifest = VersionResolver.getManifest(project, pair);
        Version version = manifest.find(config.minecraft.minecraftVersion.get());
        System.out.println("Found '" + manifest.versions.length + "' minecraft versions!");
        System.out.println("Found version '" + version.type + " " + version.id + "'");

        VersionData client = version.get(project);

        List<Library> toBeRemapped = loadLibrariesForVersion(project, handler, client);
        if (config.minecraft.mappingsSource.get().equals("none")) {
            downloadJars(project, handler, client, true);
            for (Library library : toBeRemapped) {
                handler.add(SulphurConfigurations.nonTransitiveInclude, library.name);
            }

            project.afterEvaluate((p) -> {
                ResolvableDependencies incoming = SulphurConfigurations.apiRemapConfiguration.getIncoming();
                for (ResolvedArtifactResult artifact : incoming.getArtifacts()) {
                    File inFile = artifact.getFile();
                    handler.add(SulphurConfigurations.nonTransitiveInclude, inFile);
                }
            });
        } else {
            downloadJars(project, handler, client, false);
            remapJars(project, handler, client, config.minecraft);

            for (Library library : toBeRemapped) {
                handler.add(SulphurConfigurations.internalRemapConfigurationName, library.name);
                File a = LocalMaven.getArtifactFile(project, LocalMaven.transformName(library.name));
                if (a.exists()) {
                    handler.add(SulphurConfigurations.nonTransitiveInclude, project.files(a));
                }
            }

            project.afterEvaluate((p) -> {
                ResolvableDependencies incoming = SulphurConfigurations.apiRemapConfiguration.getIncoming();
                for (ResolvedArtifactResult artifact : incoming.getArtifacts()) {
                    File inFile = artifact.getFile();
                    File file = LocalMaven.getArtifactDir(project, LocalMaven.transformName("files:" + inFile.getName().substring(0, inFile.getName().lastIndexOf('.')) + ":0.0.0"));
                    if (!file.exists()) file.mkdirs();
                    File remappedJar = new File(file, inFile.getName().replaceAll("\\.jar", "-remapped.jar"));
                    if (remappedJar.exists()) {
                        handler.add(SulphurConfigurations.nonTransitiveInclude, project.files(remappedJar));
                    }
                }
            });

        }
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

    public static void downloadJars(Project project, DependencyHandler dependencies, VersionData versionData, boolean addDep) {
        File clientFile = new File(FileUtil.getVersionFolder(project, versionData.id), "minecraft-" + versionData.id + "-client.jar");

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
        if (clientFile.exists() && addDep)
            dependencies.add("implementation", "com.mojang:minecraft:" + versionData.id + ":client");
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
        Map<String, String> requiredVersions = new HashMap<>();

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

    public static List<Library> loadLibrariesForVersion(Project project, DependencyHandler dependencies, VersionData client) {
        Library[] validLibs = client.getValidLibraries();
        System.out.println("Found '" + validLibs.length + "/" + client.libraries.length + "' valid libraries for this os '" + OSUtil.osId + "'");

        File wroteNatives = new File(FileUtil.getVersionNativesFolder(project, client.id), "i-wrote-natives-written.txt");

        applyManifestVersionConstraints(project, validLibs);

        List<Library> toBeRemapped = new ArrayList<>();
        for (Library validLib : validLibs) {
            if (validLib.downloads.artifact != null) {
                if (!validLib.name.contains("com.mojang")) {
                    dependencies.add(SulphurConfigurations.nonTransitiveInclude, validLib.name);
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
            ZipInputStream zipStream = new ZipInputStream(Files.newInputStream(nativeFile.toPath()));
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

    public static final String assetsUrl = "https://resources.download.minecraft.net/";
    private static final ExecutorService service = Executors.newFixedThreadPool(4);

    public static void downloadAssets(File assetsDir, VersionData versionData) {
        File indexesDir = new File(assetsDir, "indexes");
        File objectsDir = new File(assetsDir, "objects");
        if (!objectsDir.exists()) objectsDir.mkdirs();
        File assetsManifest = new File(indexesDir, versionData.assetIndex.id + ".json");
        String manifestData = "";
        if (assetsManifest.exists()) {
            try (InputStream input = Files.newInputStream(assetsManifest.getAbsoluteFile().toPath())) {
                manifestData = new String(JavaUtils.readAllBytes(input));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (manifestData.isEmpty() || !assetsManifest.exists()) {
            assetsManifest.getParentFile().mkdirs();
            try (InputStream input = versionData.assetIndex.url.openStream()) {
                assetsManifest.createNewFile();
                byte[] bytes = JavaUtils.readAllBytes(input);
                Files.write(assetsManifest.getAbsoluteFile().toPath(), bytes, StandardOpenOption.WRITE);
                manifestData = new String(bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Path objectsPath = objectsDir.getAbsoluteFile().toPath();

        List<Callable<Void>> toDownload = new ArrayList<>();

        AssetsManifest manifest = VersionResolver.GSON_LOCAL.get().fromJson(manifestData, AssetsManifest.class);
        Set<Map.Entry<String, Asset>> entries = manifest.objects.entrySet();
        for (Map.Entry<String, Asset> entry : entries) {
            toDownload.add(makeDownloadTask(objectsPath, entry.getValue().hash));
        }
        try {
            System.out.println("Downloading Assets");
            service.invokeAll(toDownload);
            System.out.println("Assets finished downloading!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Callable<Void> makeDownloadTask(Path objectsDir, String hash) {
        String subDir = hash.substring(0, 2);
        Path subPath = objectsDir.resolve(subDir);
        if (Files.notExists(subPath)) {
            try {
                Files.createDirectories(subPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return () -> {
            String fileUrl = assetsUrl + subDir + "/" + hash;
            try (InputStream input = URI.create(fileUrl).toURL().openStream()) {
                Files.copy(input, subPath.resolve(hash));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        };
    }
}
