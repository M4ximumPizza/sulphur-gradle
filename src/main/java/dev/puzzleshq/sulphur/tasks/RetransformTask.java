package dev.puzzleshq.sulphur.tasks;

import dev.puzzleshq.lunar.ConfigPair;
import dev.puzzleshq.sulphur.VersionResolver;
import dev.puzzleshq.sulphur.config.SulpherConfig;
import dev.puzzleshq.sulphur.minecraft.MinecraftSetup;
import dev.puzzleshq.sulphur.minecraft.manifest.Version;
import dev.puzzleshq.sulphur.minecraft.manifest.VersionManifest;
import dev.puzzleshq.sulphur.minecraft.manifest.client.VersionData;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.TaskAction;

public class RetransformTask extends DefaultTask {

    public RetransformTask() {
        setGroup("slime/transform");
    }

    @TaskAction
    public void exec() {
        Project project = getProject();
        DependencyHandler dependencies = project.getDependencies();

        ConfigPair<SulpherConfig> pair = SulpherConfig.get(project);
        SulpherConfig config = pair.getConfig();

        VersionManifest manifest = VersionResolver.getManifest(project, pair);
        Version version = manifest.find(config.minecraft.minecraftVersion.get());
        VersionData client = version.get(project);

        MinecraftSetup.remapJars(project, dependencies, client, config.minecraft);
    }

}
