package dev.puzzleshq.slime;

import dev.puzzleshq.lunar.ConfigPair;
import dev.puzzleshq.slime.config.SlimeConfig;
import dev.puzzleshq.slime.minecraft.MinecraftSetup;
import dev.puzzleshq.slime.tasks.Tasks;
import dev.puzzleshq.slime.util.FileUtil;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.MalformedURLException;

public class SlimePlugin implements Plugin<@NotNull Project> {

    @Override
    public void apply(Project target) {
        File jarsDir = new File(FileUtil.getSlimeCache(target), "jars");
        if (!jarsDir.exists()) jarsDir.mkdirs();

        DependencyHandler dependencies = target.getDependencies();
        RepositoryHandler repoManagement = target.getRepositories();

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
                c.includeModule("puzzle.remapped", "minecraft");
            });
        });

        ConfigPair<SlimeConfig> pair = SlimeConfig.get(target);
        pair.getSpec().load();
        pair.getSpec().clearCaches();

        MinecraftSetup.loadMinecraft(target, dependencies, pair);

        Configurations.init(target);
        target.afterEvaluate(p -> {
            p.getConfigurations().all(c -> {
                Configurations.getLoader(p, dependencies, repoManagement, c);
            });
        });

        Tasks.add(target.getTasks());
    }

    @Override
    public String toString() {
        return "Slime Plugin";
    }

}
