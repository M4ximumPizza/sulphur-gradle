package dev.puzzleshq.sulphur.tasks;

import dev.puzzleshq.sulphur.transform.LocalMaven;
import dev.puzzleshq.sulphur.util.FileUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class ClearTransformCache extends DefaultTask {

    public ClearTransformCache() {
        setGroup(TaskGroups.clean);
    }

    @TaskAction
    public void exec() {
        FileUtil.delete(LocalMaven.getLocalSlimeCache(getProject()));
    }

}
