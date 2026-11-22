package dev.puzzleshq.sulphur.minecraft;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.impl.Mapping;
import dev.puzzleshq.sulphur.config.MinecraftConfig;
import dev.puzzleshq.sulphur.minecraft.manifest.client.VersionData;
import org.gradle.api.Project;

import java.util.Map;

public final class Mappings {

    private static final String SEPARATOR = "~";

    private Mappings() {
        // Utility class
    }

    /**
     * <p>Mappings without a named equivalent retain their intermediary
     * name. Entries that cannot be resolved through intermediary mappings
     * are preserved using their original owner/name.</p>
     *
     * @param intermediary obfuscated-to-intermediary mappings
     * @param named intermediary-to-named mappings
     * @return combined obfuscated-to-named mappings
     *
     * @author M4ximumPizza
     * @author Mr Zombii
     */
    public static IMapping getMappings(IMapping intermediary, IMapping named) {
        if (intermediary == null) return named;
        if (named == null) return null;

        IMapping combined = new Mapping();
        IMapping inverse = intermediary.reverse();

        mergeClasses(combined, intermediary, named, inverse);
        mergeFields(combined, intermediary, named, inverse);
        mergeMethods(combined, intermediary, named, inverse);

        return combined;
    }

    private static void mergeClasses(
            IMapping combined,
            IMapping intermediary,
            IMapping named,
            IMapping inverse
    ) {
        // Convert intermediary -> named into obfuscated -> named.
        for (Map.Entry<String, String> entry : named.getClassEntries()) {
            String intermediaryName = entry.getKey();
            String obfuscatedName = inverse.mapClass(intermediaryName);

            if (obfuscatedName.equals(intermediaryName)) {
                obfuscatedName = intermediaryName;
            }

            combined.putClass(obfuscatedName, entry.getValue());
        }

        // Preserve intermediary mappings without a named equivalent.
        for (Map.Entry<String, String> entry : intermediary.getClassEntries()) {
            String intermediaryName = entry.getValue();

            if (named.mapClass(intermediaryName).equals(intermediaryName)) {
                combined.putClass(entry.getKey(), intermediaryName);
            }
        }
    }

    private static void mergeFields(
            IMapping combined,
            IMapping intermediary,
            IMapping named,
            IMapping inverse
    ) {
        // Convert intermediary -> named into obfuscated -> named.
        for (Map.Entry<String, String> entry : named.getFieldEntries()) {
            String[] source = split(entry.getKey(), 2);
            String[] target = split(entry.getValue(), 2);

            String[] obfuscated = split(
                    inverse.mapField(source[0], source[1]), 2
            );

            combined.putField(
                    obfuscated[0],
                    obfuscated[1],
                    target[0],
                    target[1]
            );
        }

        // Preserve intermediary fields without a named equivalent.
        for (Map.Entry<String, String> entry : intermediary.getFieldEntries()) {
            String[] obfuscated = split(entry.getKey(), 2);
            String[] intermediate = split(entry.getValue(), 2);

            String mapped = named.mapField(intermediate[0], intermediate[1]);

            if (mapped.equals(entry.getValue())) {
                combined.putField(
                        obfuscated[0],
                        obfuscated[1],
                        intermediate[0],
                        intermediate[1]
                );
            }
        }
    }

    private static void mergeMethods(
            IMapping combined,
            IMapping intermediary,
            IMapping named,
            IMapping inverse
    ) {
        // Convert intermediary -> named into obfuscated -> named.
        for (Map.Entry<String, String> entry : named.getMethodEntries()) {
            String[] source = split(entry.getKey(), 3);
            String[] target = split(entry.getValue(), 3);

            String[] obfuscated = split(
                    inverse.mapMethod(source[0], source[1], source[2]), 3
            );

            combined.putMethod(
                    obfuscated[0],
                    obfuscated[1],
                    obfuscated[2],
                    target[0],
                    target[1],
                    target[2]
            );
        }

        // Preserve intermediary methods without a named equivalent.
        for (Map.Entry<String, String> entry : intermediary.getMethodEntries()) {
            String[] obfuscated = split(entry.getKey(), 3);
            String[] intermediate = split(entry.getValue(), 3);

            String mapped = named.mapMethod(
                    intermediate[0],
                    intermediate[1],
                    intermediate[2]
            );

            if (mapped.equals(entry.getValue())) {
                combined.putMethod(
                        obfuscated[0],
                        obfuscated[1],
                        obfuscated[2],
                        intermediate[0],
                        intermediate[1],
                        intermediate[2]
                );
            }
        }
    }

    /**
     * Splits a mapping entry and validates its structure.
     */
    private static String[] split(String value, int expectedParts) {
        String[] parts = value.split(SEPARATOR, -1);

        if (parts.length != expectedParts) {
            throw new IllegalArgumentException(
                    "Malformed mapping entry: expected "
                            + expectedParts + " components, got "
                            + parts.length + ": " + value
            );
        }

        return parts;
    }

    public static IMapping getMappings(Project project, String version) {
        IMapping intermediary = IntermediaryMappings.getMappings(project, version);
        IMapping named = NamedMappings.getMappings(project, version);

        return getMappings(intermediary, named);
    }

    public static IMapping getMappings(
            Project project,
            VersionData versionData,
            MinecraftConfig config
    ) {
        IMapping intermediary = IntermediaryMappings.getMappings(
                project, versionData, config
        );

        IMapping named = NamedMappings.getMappings(
                project, versionData, config
        );

        return getMappings(intermediary, named);
    }
}