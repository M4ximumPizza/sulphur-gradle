package dev.puzzleshq.slime.config;

import dev.puzzleshq.lunar.Comment;
import dev.puzzleshq.lunar.ConfigClass;
import dev.puzzleshq.lunar.ConfigPair;
import dev.puzzleshq.lunar.PuzzleConfigSpec;
import org.gradle.api.Project;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ConfigClass
public class SlimeConfig {

    private static final Map<String, ConfigPair<SlimeConfig>> perProjectConfig = new ConcurrentHashMap<>();

    public static ConfigPair<SlimeConfig> get(Project project) {
        if (perProjectConfig.containsKey(project.getName()))
            return perProjectConfig.get(project.getName());

        ConfigPair<SlimeConfig> config = PuzzleConfigSpec.ofClass(SlimeConfig.class, project.file("slime-config.toml"));
        config.getSpec().save();
        perProjectConfig.put(project.getName(), config);
        return config;
    }

    @Comment(" Minecraft Configuration Details")
    public MinecraftConfig minecraft;

    @Comment(" Puzzle Configuration Details")
    public PuzzleConfig puzzle;

}
