package dev.puzzleshq.sulphur.tasks;

import org.gradle.api.tasks.TaskContainer;

public class Tasks {

    public static void add(TaskContainer task) {
        task.register("runClient", RunClientTask.class);
        task.register("runServer", RunServerTask.class);
        task.register("downloadAssets", DownloadAssets.class);
        task.register("clearAssetsCache", ClearAssetsCache.class);

        task.register("reobfuscate", ReobfuscateTask.class);

        task.register("retransformGame", RetransformTask.class);
        task.register("transformDependencies", TransformTask.class);
        task.register("clearTransformCache", ClearTransformCache.class);
        task.register("clearManifestCache", ClearManifestsCache.class);
    }

}
