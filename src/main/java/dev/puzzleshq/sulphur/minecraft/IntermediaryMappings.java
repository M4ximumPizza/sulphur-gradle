package dev.puzzleshq.sulphur.minecraft;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.util.MappingUtil;
import dev.puzzleshq.lunar.ConfigPair;
import dev.puzzleshq.sulphur.config.MinecraftConfig;
import dev.puzzleshq.sulphur.config.SulpherConfig;
import dev.puzzleshq.sulphur.minecraft.manifest.version.client.VersionData;
import dev.puzzleshq.sulphur.util.FileUtil;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTreeView;
import net.fabricmc.mappingio.tree.VisitableMappingTree;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class IntermediaryMappings {

    private static final String YARN_URL =
            "https://maven.fabricmc.net/net/fabricmc/intermediary/";

    private static final String LEGACY_YARN_URL =
            "https://repo.legacyfabric.net/legacyfabric/net/legacyfabric/intermediary/";

    private static final String ORNITHEMC_URL =
            "https://maven.ornithemc.net/releases/net/ornithemc/calamus-intermediary-gen2/";

    private static final String MAPPINGS_ENTRY = "mappings/mappings.tiny";

    private static final ConcurrentMap<String, IMapping> CACHE =
            new ConcurrentHashMap<>();

    private IntermediaryMappings() {}

    // ------------------------------------------------------------
    // Yarn mappings
    // ------------------------------------------------------------

    public static IMapping getYarnMappings(Project project, String version) {
        return getMappings(
                project,
                YARN_URL,
                "yarn",
                "intermediary",
                version
        );
    }

    // ------------------------------------------------------------
    // Legacy Yarn mappings
    // ------------------------------------------------------------

    public static IMapping getLegacyYarnMappings(Project project, String version) {
        return getMappings(
                project,
                LEGACY_YARN_URL,
                "legacy-yarn",
                "intermediary",
                version
        );
    }

    // ------------------------------------------------------------
    // OrnitheMC mappings
    // ------------------------------------------------------------

    public static IMapping getOrinthemcMappings(Project project, String version) {
        return getMappings(
                project,
                ORNITHEMC_URL,
                "ornithemc",
                "calamus-intermediary-gen2",
                version
        );
    }

    // ------------------------------------------------------------
    // Generic mapping loader
    // ------------------------------------------------------------

    private static IMapping getMappings(
            Project project,
            String repository,
            String provider,
            String artifact,
            String version
    ) {
        String filename = provider + "-" + artifact + "-" + version + "-v2";

        File mappingsDir = FileUtil.getMappingsDir(project);
        File tinyFile = new File(mappingsDir, filename + ".txt");
        File jarFile = new File(mappingsDir, filename + ".jar");

        String cacheKey = tinyFile.getAbsolutePath();

        return CACHE.computeIfAbsent(cacheKey, key -> {
            try {
                if (!tinyFile.isFile()) {
                    URL url = URI.create(
                            repository + version + "/" +
                                    artifact + "-" + version + "-v2.jar"
                    ).toURL();

                    downloadIfMissing(url, jarFile);
                    extractMappings(jarFile, tinyFile);
                }

                return parseMappings(tinyFile);

            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to load " + provider +
                                " intermediary mappings for Minecraft " + version,
                        e
                );
            }
        });
    }

    // ------------------------------------------------------------
    // Mapping parsing
    // ------------------------------------------------------------

    private static IMapping parseMappings(File tinyFile) throws IOException {
        String mappingsData = readUtf8(tinyFile);

        VisitableMappingTree tree = MappingUtil.fromString(
                mappingsData,
                MappingFormat.TINY_2_FILE
        );

        boolean isSplit =
                tree.getNamespaceId("clientOfficial") !=
                        MappingTreeView.NULL_NAMESPACE_ID;

        return MappingUtil.fromMappingTree(
                tree,
                isSplit ? "clientOfficial" : "official",
                "intermediary"
        );
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

    private static void ensureDirectory(File directory) throws IOException {
        if (!directory.isDirectory() && !directory.mkdirs()) {
            throw new IOException(
                    "Could not create directory: " + directory
            );
        }
    }

    private static void downloadIfMissing(URL url, File destination) throws IOException {
        if (destination.isFile()) {
            return;
        }

        File parent = destination.getParentFile();
        ensureDirectory(parent);

        File temporary = File.createTempFile(
                "sulphur-intermediary-",
                ".tmp",
                parent
        );

        try {
            try (InputStream input = url.openStream()) {
                Files.copy(
                        input,
                        temporary.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            moveTemporaryFile(temporary, destination);

        } finally {
            Files.deleteIfExists(temporary.toPath());
        }
    }

    private static void extractMappings(File jarFile, File destination) throws IOException {
        File parent = destination.getParentFile();
        ensureDirectory(parent);

        File temporary = File.createTempFile(
                "sulphur-intermediary-",
                ".tmp",
                parent
        );

        try {
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
                            temporary.toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }

            moveTemporaryFile(temporary, destination);

        } finally {
            Files.deleteIfExists(temporary.toPath());
        }
    }

    private static void moveTemporaryFile(
            File temporary,
            File destination
    ) throws IOException {
        try {
            Files.move(
                    temporary.toPath(),
                    destination.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (AtomicMoveNotSupportedException e) {
            Files.move(
                    temporary.toPath(),
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    // ------------------------------------------------------------
    // Mapping selection
    // ------------------------------------------------------------

    public static IMapping getMappings(Project project, String version) {
        ConfigPair<SulpherConfig> configPair = SulpherConfig.get(project);

        MinecraftConfig config = configPair.getConfig().minecraft;
        String source = config.mappingsSource.get();
        String[] sourceParts = source.split(":", 2);

        switch (sourceParts[0]) {
            case "none":
            case "official":
                return null;

            case "yarn": return getYarnMappings(project, version);
            case "legacy-yarn": return getLegacyYarnMappings(project, version);
            case "ornithemc": return getOrinthemcMappings(project, version);
            default: throw new IllegalArgumentException("Unknown mapping type '" + source + "'" );
        }
    }

    public static IMapping getMappings(Project project, VersionData versionData, MinecraftConfig config) {
        String[] sourceParts = config.mappingsSource.get().split(":");

        switch (sourceParts[0]) {
            case "none":
            case "official":
                return null;
            case "yarn": return getYarnMappings(project, versionData.id);
            case "legacy-yarn": return getLegacyYarnMappings(project, versionData.id);
            case "ornithemc": return getOrinthemcMappings(project, versionData.id);
            default: throw new IllegalArgumentException("Unknown mapping type '" + config.mappingsSource.get() + "'");
        }
    }
}