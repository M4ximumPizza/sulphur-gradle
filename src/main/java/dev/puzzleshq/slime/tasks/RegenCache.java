package dev.puzzleshq.slime.tasks;

import dev.puzzleshq.slime.VersionResolver;
import dev.puzzleshq.slime.util.FileUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class RegenCache extends DefaultTask {

    public RegenCache() {
        setGroup("slime/clean");
    }

    @TaskAction
    public void exec() {
        FileUtil.delete(FileUtil.getJarsDir(getProject()));
        FileUtil.delete(FileUtil.getNativesDir(getProject()));
        FileUtil.delete(FileUtil.getLibsDir(getProject()));
        VersionResolver.deleteManifestFiles(getProject());
        FileUtil.delete(FileUtil.getMappingsDir(getProject()));
    }

}
