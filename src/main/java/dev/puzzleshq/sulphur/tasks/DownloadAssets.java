package dev.puzzleshq.sulphur.tasks;

import dev.puzzleshq.lunar.ConfigPair;
import dev.puzzleshq.sulphur.VersionResolver;
import dev.puzzleshq.sulphur.config.SulpherConfig;
import dev.puzzleshq.sulphur.minecraft.MinecraftSetup;
import dev.puzzleshq.sulphur.minecraft.manifest.version.Version;
import dev.puzzleshq.sulphur.minecraft.manifest.version.VersionManifest;
import dev.puzzleshq.sulphur.minecraft.manifest.version.client.VersionData;
import dev.puzzleshq.sulphur.util.FileUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class DownloadAssets extends DefaultTask {

    public DownloadAssets() {
        setGroup(TaskGroups.transform);
    }

    @TaskAction
    public void exec() {
        Project project = getProject();

        ConfigPair<SulpherConfig> pair = SulpherConfig.get(project);
        SulpherConfig config = pair.getConfig();

        VersionManifest manifest = VersionResolver.getManifest(project, pair);
        Version version = manifest.find(config.minecraft.minecraftVersion.get());
        VersionData versionData = version.get(project);

        MinecraftSetup.downloadAssets(FileUtil.getAssetsDir(getProject()), versionData);
    }

}
