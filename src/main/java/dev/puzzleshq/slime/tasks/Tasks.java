package dev.puzzleshq.slime.tasks;

import dev.puzzleshq.slime.tasks.cache.ClearJarsCache;
import dev.puzzleshq.slime.tasks.cache.ClearManifestCache;
import dev.puzzleshq.slime.tasks.cache.ClearMappingsCache;
import dev.puzzleshq.slime.tasks.cache.ClearNativesCache;
import org.gradle.api.tasks.TaskContainer;

public class Tasks {

    public static void add(TaskContainer task) {
        task.register("runClient", RunClientTask.class);
        task.register("runServer", RunServerTask.class);

//        task.register("clearNativesCache", ClearNativesCache.class);
//        task.register("clearManifestCache", ClearManifestCache.class);
//        task.register("clearMappingsCache", ClearMappingsCache.class);
//        task.register("clearJarsCache", ClearJarsCache.class);
//        task.register("regenCache", RegenCache.class);
        task.register("transformTask", TransformTask.class);
        task.register("clearTransformCache", ClearTransformCache.class);
    }

}
