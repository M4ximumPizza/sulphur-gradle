package dev.puzzleshq.slime.minecraft;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.impl.Mapping;
import dev.puzzleshq.gibberishdefiner.util.MappingUtil;
import dev.puzzleshq.lunar.ConfigPair;
import dev.puzzleshq.slime.VersionResolver;
import dev.puzzleshq.slime.config.MinecraftConfig;
import dev.puzzleshq.slime.config.SlimeConfig;
import dev.puzzleshq.slime.minecraft.manifest.Version;
import dev.puzzleshq.slime.minecraft.manifest.client.VersionData;
import dev.puzzleshq.slime.util.FileUtil;
import dev.puzzleshq.slime.util.JavaUtils;
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
import java.util.Arrays;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Mappings {

    public static IMapping getMappings(Project project, String version) {
        IMapping intermediary = IntermediaryMappings.getMappings(project, version);
        IMapping named = NamedMappings.getMappings(project, version);

        if (intermediary == null) return named;
        if (named == null) return null;

        IMapping combined = new Mapping();
        for (Map.Entry<String, String> classEntry : intermediary.getClassEntries()) {
            combined.putClass(classEntry.getKey(), named.mapClass(classEntry.getValue()));
        }
        for (Map.Entry<String, String> fieldEntry : intermediary.getFieldEntries()) {
            String[] splitA = fieldEntry.getKey().split("~");
            String[] splitB = fieldEntry.getValue().split("~");
            String[] splitC = named.mapField(splitB[0], splitB[1]).split("~");

            combined.putField(splitA[0], splitA[1], splitC[0], splitC[1]);
        }
        for (Map.Entry<String, String> methodEntry : intermediary.getMethodEntries()) {
            String[] splitA = methodEntry.getKey().split("~");
            String[] splitB = methodEntry.getValue().split("~");
            String[] splitC = named.mapMethod(splitB[0], splitB[1], splitB[2]).split("~");

            combined.putMethod(
                    splitA[0], splitA[1], splitA[2],
                    splitC[0], splitC[1], splitC[2]
            );
        }

        return combined;
    }

    public static IMapping getMappings(Project project, VersionData versionData, MinecraftConfig config) {
        IMapping intermediary = IntermediaryMappings.getMappings(project, versionData, config);
        IMapping named = NamedMappings.getMappings(project, versionData, config);

        if (intermediary == null) return named;
        if (named == null) return null;

        IMapping combined = new Mapping();
        for (Map.Entry<String, String> classEntry : intermediary.getClassEntries()) {
            combined.putClass(classEntry.getKey(), named.mapClass(classEntry.getValue()));
        }
        for (Map.Entry<String, String> fieldEntry : intermediary.getFieldEntries()) {
            String[] splitA = fieldEntry.getKey().split("~");
            String[] splitB = fieldEntry.getValue().split("~");
            String[] splitC = named.mapField(splitB[0], splitB[1]).split("~");

            combined.putField(splitA[0], splitA[1], splitC[0], splitC[1]);
        }
        for (Map.Entry<String, String> methodEntry : intermediary.getMethodEntries()) {
            String[] splitA = methodEntry.getKey().split("~");
            String[] splitB = methodEntry.getValue().split("~");
            String[] splitC = named.mapMethod(splitB[0], splitB[1], splitB[2]).split("~");

            combined.putMethod(
                    splitA[0], splitA[1], splitA[2],
                    splitC[0], splitC[1], splitC[2]
            );
        }

        return combined;
    }
}
