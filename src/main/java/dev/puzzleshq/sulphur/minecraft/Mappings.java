package dev.puzzleshq.sulphur.minecraft;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.impl.Mapping;
import dev.puzzleshq.sulphur.config.MinecraftConfig;
import dev.puzzleshq.sulphur.minecraft.manifest.client.VersionData;
import org.gradle.api.Project;

import java.util.Map;

public class Mappings {

    public static IMapping getMappings(IMapping intermediary, IMapping named) {
        if (intermediary == null) return named;
        if (named == null) return null;

        IMapping combined = new Mapping();
        IMapping inverseIntermediary = intermediary.reverse();

        for (Map.Entry<String, String> classEntry : named.getClassEntries()) {
            String intermediaryName = classEntry.getKey();
            String namedName = classEntry.getValue();
            if (inverseIntermediary.hasClass(intermediaryName) || !inverseIntermediary.mapClass(intermediaryName).equals(intermediaryName)) {
                String obfName = inverseIntermediary.mapClass(intermediaryName);
                combined.putClass(obfName, namedName);
            } else {
                // Fallback or log if an intermediary class has no obfuscated counterpart
                combined.putClass(intermediaryName, namedName);
            }
//            combined.putClass(inverseIntermediary.mapClass(classEntry.getKey()), classEntry.getValue());
        }
        for (Map.Entry<String, String> fieldEntry : named.getFieldEntries()) {
            String[] splitA = fieldEntry.getKey().split("~"); // intermediate
            String[] splitB = fieldEntry.getValue().split("~"); // named
            String[] splitC = inverseIntermediary.mapField(splitA[0], splitA[1]).split("~"); // obf
            combined.putField(splitC[0], splitC[1], splitB[0], splitB[1]);
        }
        for (Map.Entry<String, String> methodEntry : named.getMethodEntries()) {
            String[] splitA = methodEntry.getKey().split("~"); // intermediate
            String[] splitB = methodEntry.getValue().split("~"); // named
            String[] splitC = inverseIntermediary.mapMethod(splitA[0], splitA[1], splitA[2]).split("~"); // obf
            combined.putMethod(splitC[0], splitC[1], splitC[2], splitB[0], splitB[1], splitB[2]);
        }
        for (Map.Entry<String, String> classEntry : intermediary.getClassEntries()) {
            if (named.mapClass(classEntry.getValue()).equals(classEntry.getValue())) {
                combined.putClass(classEntry.getKey(), classEntry.getValue());
            }
        }
        for (Map.Entry<String, String> fieldEntry : intermediary.getFieldEntries()) {
            String[] splitA = fieldEntry.getKey().split("~"); // obf
            String b = fieldEntry.getValue(); // intermediate
            String[] splitB = b.split("~"); // intermediate
            if (named.mapField(splitB[0], splitB[1]).equals(b)) {
                combined.putField(splitA[0], splitA[1], splitB[0], splitB[1]);
            }
        }
        for (Map.Entry<String, String> methodEntry : intermediary.getMethodEntries()) {
            String[] splitA = methodEntry.getKey().split("~"); // obf
            String b = methodEntry.getValue(); // intermediate
            String[] splitB = b.split("~"); // intermediate
            if (named.mapMethod(splitB[0], splitB[1], splitB[2]).equals(b)) {
                combined.putMethod(splitA[0], splitA[1], splitA[2], splitB[0], splitB[1], splitB[2]);
            }
        }
        return combined;
    }

    public static IMapping getMappings(Project project, String version) {
        IMapping intermediary = IntermediaryMappings.getMappings(project, version);
        IMapping named = NamedMappings.getMappings(project, version);

        return getMappings(intermediary, named);
    }

    public static IMapping getMappings(Project project, VersionData versionData, MinecraftConfig config) {
        IMapping intermediary = IntermediaryMappings.getMappings(project, versionData, config);
        IMapping named = NamedMappings.getMappings(project, versionData, config);

        return getMappings(intermediary, named);
    }
}
