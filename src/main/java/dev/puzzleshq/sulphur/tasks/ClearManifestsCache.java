package dev.puzzleshq.sulphur.tasks;

import dev.puzzleshq.gibberishdefiner.JarRemapper;
import dev.puzzleshq.sulphur.config.SulpherConfig;
import dev.puzzleshq.sulphur.transform.SulphurConfigurations;
import dev.puzzleshq.sulphur.transform.TransformPipeline;
import dev.puzzleshq.sulphur.util.FileUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;

public abstract class ClearManifestsCache extends DefaultTask {

    @Inject
    public ClearManifestsCache() {
        setGroup(TaskGroups.clean);
    }

    @TaskAction
    public void exec() {
        Project project = getProject();

        FileUtil.delete(FileUtil.getPuzzleManifests(project));
        FileUtil.delete(FileUtil.getMinecraftManifests(project));
    }

}
