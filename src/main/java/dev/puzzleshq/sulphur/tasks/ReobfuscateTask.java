package dev.puzzleshq.sulphur.tasks;

import dev.puzzleshq.gibberishdefiner.JarRemapper;
import dev.puzzleshq.sulphur.config.SulpherConfig;
import dev.puzzleshq.sulphur.transform.SulphurConfigurations;
import dev.puzzleshq.sulphur.transform.TransformPipeline;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;

public abstract class ReobfuscateTask extends DefaultTask {

    @Inject
    public ReobfuscateTask() {
        setGroup(TaskGroups.build);
    }

    @Internal
    private File archive;

    public void setArchive(File file) {
        this.archive = file;
    }

    public File getArchive() {
        if (archive == null) {
            archive = getProject().getTasksByName("jar", true).stream().findFirst().get().getOutputs().getFiles().getSingleFile();
        }
        return archive;
    }

    @TaskAction
    public void exec() {
        Project project = getProject();
        ResolvableDependencies incoming = SulphurConfigurations.internalRemapConfiguration.getIncoming();

        TransformPipeline transformPipeline = TransformPipeline.create(
                project,
                SulpherConfig.get(project).getConfig().minecraft.minecraftVersion.get()
        );

        File in = getArchive();
        File out = new File(getArchive().getParentFile(), getArchive().getName().replaceAll("\\.jar", "-reobf.jar"));

        transformPipeline.remix(incoming.getDependencies(), incoming.getArtifacts());
        transformPipeline.reverse();
        System.out.println("--------------------");
        System.out.println(transformPipeline.getMapping().mapClass("net/minecraft/entity/projectile/ProjectileEntity"));
        transformPipeline.remix(in);

        JarRemapper.applyMapping(in, out, transformPipeline.getMapping());
//        transformPipeline.remap(in, out);
    }

}
