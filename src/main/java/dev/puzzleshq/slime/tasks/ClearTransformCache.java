package dev.puzzleshq.slime.tasks;

import dev.puzzleshq.slime.transform.LocalMaven;
import dev.puzzleshq.slime.util.FileUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class ClearTransformCache extends DefaultTask {

    public ClearTransformCache() {
        setGroup("slime/clean");
    }

    @TaskAction
    public void exec() {
        FileUtil.delete(LocalMaven.getLocalSlimeCache(getProject()));
    }

}
