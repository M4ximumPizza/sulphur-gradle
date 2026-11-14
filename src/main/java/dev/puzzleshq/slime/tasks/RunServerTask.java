package dev.puzzleshq.slime.tasks;

import com.google.gson.JsonElement;
import dev.puzzleshq.lunar.ConfigPair;
import dev.puzzleshq.slime.VersionResolver;
import dev.puzzleshq.slime.config.SlimeConfig;
import dev.puzzleshq.slime.minecraft.manifest.client.VersionData;
import dev.puzzleshq.slime.util.FileUtil;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class RunServerTask extends JavaExec {

    public RunServerTask() {
        setGroup("jigsaw/runs");

        File runDir = getProject().file("run");

        if (!runDir.exists())
            runDir.mkdirs();

        classpath(getProject().getExtensions().getByType(SourceSetContainer.class).getByName("main").getRuntimeClasspath());

        ConfigPair<SlimeConfig> pair = SlimeConfig.get(getProject());
        SlimeConfig config = pair.getConfig();

        VersionData versionData = VersionResolver.getManifest(getProject(), pair).find(config.minecraft.minecraftVersion.get()).get(getProject());

        File jarsDir = new File(FileUtil.getSlimeCache(getProject()), "jars");
        File clientJar = new File(jarsDir, versionData.id + "-client.jar");

        jvmArgs(
                "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump",
                "-Dminecraft.launcher.brand=minecraft-launcher",
                "-Dminecraft.launcher.version=2.6.16",
                "-Xss1M",
                "-Xmx2G",
                "-Djava.library.path=" + FileUtil.getVersionNativesFolder(getProject(), versionData.id).getAbsolutePath(),
                "-Dminecraft.client.jar=" + clientJar.getAbsolutePath(),
                "-XX:+UnlockExperimentalVMOptions",
                "-XX:+UseG1GC",
                "-XX:G1NewSizePercent=20",
                "-XX:G1ReservePercent=20",
                "-XX:MaxGCPauseMillis=50",
                "-XX:G1HeapRegionSize=32M"
        );

        jvmArgs(
                "-Dpuzzle.core.gameProvider=dev.puzzleshq.puzzleloader.loader.provider.game.impl.MinecraftProvider"
        );

        args(
//                "--mod-folder", new File(getProject().file("runs"), "pmods").getAbsolutePath()
//            "--version", versionData.id
        );

        getMainClass().set("dev.puzzleshq.puzzleloader.loader.launch.pieces.ServerPiece");
//        getMainClass().set(versionData.mainClass);

//        ConfigurableFileCollection collection = (ConfigurableFileCollection) getClasspath();
//        collection.from(JigsawGame.CLIENT_SOURCE_SET.getRuntimeClasspath().getFiles());
//        collection.from(JigsawGame.COMMON_SOURCE_SET.getRuntimeClasspath().getFiles());

        setStandardInput(System.in);
        workingDir(runDir);
    }

}