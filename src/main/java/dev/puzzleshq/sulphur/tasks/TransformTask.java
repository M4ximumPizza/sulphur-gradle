package dev.puzzleshq.sulphur.tasks;

import dev.puzzleshq.sulphur.config.SulpherConfig;
import dev.puzzleshq.sulphur.transform.SulphurConfigurations;
import dev.puzzleshq.sulphur.transform.TransformPipeline;
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
        ResolvableDependencies incoming = SulphurConfigurations.internalRemapConfiguration.getIncoming();

        TransformPipeline transformPipeline = TransformPipeline.create(
                project,
                SulpherConfig.get(project).getConfig().minecraft.minecraftVersion.get()
        );

        transformPipeline.remix(incoming.getDependencies(), incoming.getArtifacts());
        transformPipeline.remap(incoming.getDependencies(), incoming.getArtifacts(), dependencies);
    }

}
