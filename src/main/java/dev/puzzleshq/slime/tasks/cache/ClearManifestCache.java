package dev.puzzleshq.slime.tasks.cache;

import dev.puzzleshq.slime.VersionResolver;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class ClearManifestCache extends DefaultTask {

    public ClearManifestCache() {
        setGroup("slime/clean");
    }

    @TaskAction
    public void exec() {
        VersionResolver.deleteManifestFiles(getProject());
    }

}
