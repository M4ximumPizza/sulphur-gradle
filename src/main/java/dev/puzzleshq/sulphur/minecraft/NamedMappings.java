package dev.puzzleshq.sulphur.minecraft;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.util.MappingUtil;
import dev.puzzleshq.lunar.ConfigPair;
import dev.puzzleshq.sulphur.VersionResolver;
import dev.puzzleshq.sulphur.config.MinecraftConfig;
import dev.puzzleshq.sulphur.config.SulpherConfig;
import dev.puzzleshq.sulphur.minecraft.manifest.version.client.VersionData;
import dev.puzzleshq.sulphur.util.FileUtil;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.VisitableMappingTree;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NamedMappings {

    private static final String YARN_URL =
            "https://maven.fabricmc.net/net/fabricmc/yarn/";

    private static final String LEGACY_YARN_URL =
            "https://repo.legacyfabric.net/legacyfabric/net/legacyfabric/legacy-yarn/";

    private static final String ORNITHEMC_URL =
            "https://maven.ornithemc.net/releases/net/ornithemc/feather-gen2/";

    private static final String MAPPINGS_ENTRY = "mappings/mappings.tiny";

    private static final ConcurrentMap<String, IMapping> CACHE =
            new ConcurrentHashMap<>();

    private NamedMappings() {}

    // ------------------------------------------------------------
    // Official mappings
    // ------------------------------------------------------------

    public static IMapping getOfficialMappings(Project project, VersionData version) {
        File mappingsFile = new File(
                FileUtil.getMappingsDir(project),
                version.id + "-client-proguard.txt"
        );

        try {
            downloadIfMissing(version.downloads.client_mappings.url, mappingsFile);

            String mappings = readUtf8(mappingsFile);

            VisitableMappingTree tree = MappingUtil.fromString(
                    mappings,
                    MappingFormat.PROGUARD_FILE
            );

            return MappingUtil.fromMappingTree(tree, "source", "target").reverse();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load official mappings for Minecraft " + version.id,
                    e
            );
        }
    }

    // ------------------------------------------------------------
    // Yarn mappings
    // ------------------------------------------------------------

    public static IMapping getYarnMappings(Project project, String version, String build) {
        return getTinyMappings(
                project,
                YARN_URL,
                "yarn",
                version,
                build
        );
    }

    public static IMapping getLegacyYarnMappings(Project project, String version, String build) {
        return getTinyMappings(
                project,
                LEGACY_YARN_URL,
                "legacy-yarn",
                version,
                build
        );
    }

    public static IMapping getOrinthemcMappings(Project project, String version, String build) {
        return getTinyMappings(
                project,
                ORNITHEMC_URL,
                "feather-gen2",
                version,
                build
        );
    }

    // ------------------------------------------------------------
    // Generic Tiny mapping loader
    // ------------------------------------------------------------

    private static IMapping getTinyMappings(
            Project project,
            String repository,
            String artifact,
            String version,
            String build
    ) {
        String mappingId = version + "+" + build;
        String filename = artifact + "-" + mappingId + "-v2";

        File mappingsDir = FileUtil.getMappingsDir(project);
        File tinyFile = new File(mappingsDir, filename + ".txt");
        File jarFile = new File(mappingsDir, filename + ".jar");

        String cacheKey = tinyFile.getAbsolutePath();

        IMapping cached = CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            if (!tinyFile.isFile()) {
                URL url = URI.create(
                        repository + mappingId + "/" + filename + ".jar"
                ).toURL();

                downloadIfMissing(url, jarFile);
                extractMappings(jarFile, tinyFile);
            }

            String mappings = readUtf8(tinyFile);

            VisitableMappingTree tree = MappingUtil.fromString(
                    mappings,
                    MappingFormat.TINY_2_FILE
            );

            IMapping result = MappingUtil.fromMappingTree(
                    tree,
                    "intermediary",
                    "named"
            );

            IMapping previous = CACHE.putIfAbsent(cacheKey, result);

            return previous == null ? result : previous;

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load " + artifact + " mappings for Minecraft " + version,
                    e
            );
        }
    }

    // ------------------------------------------------------------
    // File operations
    // ------------------------------------------------------------

    private static String readUtf8(File file) throws IOException {
        return new String(
                Files.readAllBytes(file.toPath()),
                StandardCharsets.UTF_8
        );
    }

    private static void downloadIfMissing(URL url, File destination) throws IOException {
        if (destination.isFile()) {
            return;
        }

        File parent = destination.getParentFile();

        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Could not create directory: " + parent);
        }

        File temporary = File.createTempFile(
                "sulphur-mappings-",
                ".tmp",
                parent
        );

        try {
            try (InputStream input = url.openStream()) {
                Files.copy(input, temporary.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            try {
                Files.move(
                        temporary.toPath(),
                        destination.toPath(),
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (IOException e) {
                if (!destination.isFile()) {
                    Files.move(
                            temporary.toPath(),
                            destination.toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }

        } finally {
            Files.deleteIfExists(temporary.toPath());
        }
    }

    private static void extractMappings(File jarFile, File destination) throws IOException {
        try (ZipFile zip = new ZipFile(jarFile)) {
            ZipEntry entry = zip.getEntry(MAPPINGS_ENTRY);

            if (entry == null) {
                throw new IOException(
                        "Missing " + MAPPINGS_ENTRY + " in " + jarFile
                );
            }

            try (InputStream input = zip.getInputStream(entry)) {
                Files.copy(
                        input,
                        destination.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        }
    }

    // ------------------------------------------------------------
    // Mapping selection
    // ------------------------------------------------------------

    public static IMapping getMappings(Project project, String version) {
        ConfigPair<SulpherConfig> pair = SulpherConfig.get(project);

        return getMappings(
                project,
                VersionResolver.getManifest(project, pair)
                        .find(version)
                        .get(project),
                pair.getConfig().minecraft
        );
    }

    public static IMapping getMappings(
            Project project,
            VersionData version,
            MinecraftConfig config
    ) {
        String source = config.mappingsSource.get();
        String[] parts = source.split(":", 2);

        String type = parts[0];

        switch (type) {
            case "none":
                return null;

            case "official":
                return getOfficialMappings(project, version);

            case "yarn":
                return getYarnMappings(project, version.id, getBuild(parts, source));

            case "legacy-yarn":
                return getLegacyYarnMappings(project, version.id, getBuild(parts, source));

            case "ornithemc":
                return getOrinthemcMappings(project, version.id, getBuild(parts, source));

            default:
                throw new IllegalArgumentException(
                        "Unknown mapping type '" + source + "'"
                );
        }
    }

    private static String getBuild(String[] parts, String source) {
        if (parts.length < 2 || parts[1].isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing mapping build for '" + source + "'"
            );
        }

        return parts[1];
    }
}