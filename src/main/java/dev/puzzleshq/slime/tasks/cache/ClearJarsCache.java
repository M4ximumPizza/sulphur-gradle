package dev.puzzleshq.slime.tasks.cache;

import dev.puzzleshq.slime.util.FileUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class ClearJarsCache extends DefaultTask {

    public ClearJarsCache() {
        setGroup("slime/clean");
    }

    @TaskAction
    public void exec() {
        FileUtil.delete(FileUtil.getJarsDir(getProject()));
    }

}
