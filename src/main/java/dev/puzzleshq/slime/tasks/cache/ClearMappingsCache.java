package dev.puzzleshq.slime.tasks.cache;

import dev.puzzleshq.slime.util.FileUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class ClearMappingsCache extends DefaultTask {

    public ClearMappingsCache() {
        setGroup("slime/clean");
    }

    @TaskAction
    public void exec() {
        FileUtil.delete(FileUtil.getMappingsDir(getProject()));
    }

}
