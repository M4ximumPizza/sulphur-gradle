package dev.puzzleshq.sulphur;

import dev.puzzleshq.lunar.ConfigPair;
import dev.puzzleshq.sulphur.config.SulpherConfig;
import dev.puzzleshq.sulphur.minecraft.MinecraftSetup;
import dev.puzzleshq.sulphur.tasks.Tasks;
import dev.puzzleshq.sulphur.transform.SulphurConfigurations;
import dev.puzzleshq.sulphur.util.FileUtil;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.MalformedURLException;

public class SulphurPlugin implements Plugin<@NotNull Project> {

    @Override
    public void apply(Project project) {
        SulphurConfigurations.init(project);

        File jarsDir = new File(FileUtil.getSlimeCache(project), "jars");
        if (!jarsDir.exists()) jarsDir.mkdirs();

        DependencyHandler dependencies = project.getDependencies();
        RepositoryHandler repoManagement = project.getRepositories();

        repoManagement.maven(a -> a.setUrl("https://libraries.minecraft.net/"));
        repoManagement.ivy(i -> {
            try {
                i.setUrl(jarsDir.toURI().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            i.artifactPattern("/[revision]-[classifier].jar");
            i.metadataSources(s -> {
                s.artifact();
                s.ignoreGradleMetadataRedirection();
            });

            i.content(c -> {
                c.includeModule("com.mojang", "minecraft");
            });
        });

        ConfigPair<SulpherConfig> pair = SulpherConfig.get(project);
        pair.getSpec().load();
        pair.getSpec().clearCaches();

        MinecraftSetup.loadMinecraft(project, dependencies, pair);
        project.afterEvaluate(p -> {
            p.getConfigurations().all(c -> {
                Configurations.getLoader(p, dependencies, repoManagement, c);
            });
        });

        Tasks.add(project.getTasks());
    }

    @Override
    public String toString() {
        return "Slime Plugin";
    }

}
