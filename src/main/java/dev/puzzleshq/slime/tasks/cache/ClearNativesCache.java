package dev.puzzleshq.slime.tasks.cache;

import dev.puzzleshq.slime.util.FileUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class ClearNativesCache extends DefaultTask {

    public ClearNativesCache() {
        setGroup("slime/clean");
    }

    @TaskAction
    public void exec() {
        FileUtil.delete(FileUtil.getNativesDir(getProject()));
        FileUtil.delete(FileUtil.getLibsDir(getProject()));
    }

}
