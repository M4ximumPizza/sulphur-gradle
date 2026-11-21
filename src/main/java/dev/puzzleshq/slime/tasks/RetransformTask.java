package dev.puzzleshq.slime.tasks;

import dev.puzzleshq.lunar.ConfigPair;
import dev.puzzleshq.slime.VersionResolver;
import dev.puzzleshq.slime.config.SlimeConfig;
import dev.puzzleshq.slime.minecraft.MinecraftSetup;
import dev.puzzleshq.slime.minecraft.manifest.Version;
import dev.puzzleshq.slime.minecraft.manifest.VersionManifest;
import dev.puzzleshq.slime.minecraft.manifest.client.VersionData;
import dev.puzzleshq.slime.transform.SlimeConfigurations;
import dev.puzzleshq.slime.transform.TransformPipeline;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetransformTask extends DefaultTask {

    public RetransformTask() {
        setGroup("slime/transform");
    }

    @TaskAction
    public void exec() {
        Project project = getProject();
        DependencyHandler dependencies = project.getDependencies();

        ConfigPair<SlimeConfig> pair = SlimeConfig.get(project);
        SlimeConfig config = pair.getConfig();

        VersionManifest manifest = VersionResolver.getManifest(project, pair);
        Version version = manifest.find(config.minecraft.minecraftVersion.get());
        VersionData client = version.get(project);

        MinecraftSetup.remapJars(project, dependencies, client, config.minecraft);
    }

}
