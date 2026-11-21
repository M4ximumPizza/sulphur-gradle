package dev.puzzleshq.slime.tasks;

import dev.puzzleshq.slime.config.SlimeConfig;
import dev.puzzleshq.slime.transform.SlimeConfigurations;
import dev.puzzleshq.slime.transform.TransformPipeline;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.*;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformTask extends DefaultTask {

    private static final Logger log = LoggerFactory.getLogger(TransformTask.class);

    public TransformTask() {
        setGroup("slime/transform");
    }

    @TaskAction
    public void exec() {
        Project project = getProject();
        DependencyHandler dependencies = project.getDependencies();
        ResolvableDependencies incoming = SlimeConfigurations.internalRemapConfiguration.getIncoming();

        TransformPipeline transformPipeline = TransformPipeline.create(
                project,
                SlimeConfig.get(project).getConfig().minecraft.minecraftVersion.get()
        );

        transformPipeline.remix(incoming.getDependencies(), incoming.getArtifacts());
        transformPipeline.remap(incoming.getDependencies(), incoming.getArtifacts(), dependencies);
    }

}
