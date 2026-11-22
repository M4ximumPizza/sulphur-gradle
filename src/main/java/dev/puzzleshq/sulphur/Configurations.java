package dev.puzzleshq.sulphur;

import dev.puzzleshq.sulphur.puzzle.*;
import dev.puzzleshq.sulphur.util.FileUtil;
import dev.puzzleshq.sulphur.util.JavaUtils;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Configurations {

    public static void getLoader(Project project, DependencyHandler dependencies, RepositoryHandler repositories, Configuration c) {
        if (c.getName().equals("puzzleLoader")) {
            for (Dependency dependency : c.getDependencies()) {
                String notation = dependency.getGroup() + ":" + dependency.getName() + ":" + dependency.getVersion();
                dependencies.add("implementation", notation + ":common");
                dependencies.add("implementation", notation + ":client");
                dependencies.add("implementation", notation + ":server");
                try {
                    getLoaderDeps(project, repositories, dependencies, dependency.getName(), dependency.getVersion(), "common");
                    getLoaderDeps(project, repositories, dependencies, dependency.getName(), dependency.getVersion(), "client");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static void getLoaderDeps(Project p, RepositoryHandler repositories, DependencyHandler dependencies, String name, String version, String classifier) throws IOException {
        File file = new File(FileUtil.getPuzzleManifests(p), name + "-manifest.json");
        File fileEpoc = new File(FileUtil.getPuzzleManifests(p), name + "-manifest.epoc");
        File depsFile = new File(FileUtil.getPuzzleManifests(p), name + "-" + version + "-manifest.json");
        File depsFileEpoc = new File(FileUtil.getPuzzleManifests(p), name + "-" + version + "-manifest.epoc");
        if (!file.exists() || !fileEpoc.exists() || !depsFile.exists() || !depsFileEpoc.exists()) {
            fileEpoc.getParentFile().mkdirs();
        }

        boolean refreshManifest = !file.exists() || !fileEpoc.exists() || System.currentTimeMillis() - Long.valueOf(new String(Files.readAllBytes(fileEpoc.toPath()))) >= VersionResolver.HOUR;
        boolean refreshDepsManifest = !depsFile.exists() || !depsFileEpoc.exists() || System.currentTimeMillis() - Long.valueOf(new String(Files.readAllBytes(depsFileEpoc.toPath()))) >= VersionResolver.HOUR;

        String manifestData;
        if (refreshManifest) {
            Files.write(fileEpoc.toPath(), String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
            try (InputStream stream = URI.create("https://raw.githubusercontent.com/PuzzlesHQ/" + name + "/refs/heads/versioning/versions.json").toURL().openStream()) {
                byte[] bytes = JavaUtils.readAllBytes(stream);
                manifestData = new String(bytes);
                Files.write(file.toPath(), bytes);
            }
        } else {
            manifestData = new String(Files.readAllBytes(file.toPath()));
        }

        String depsData;
        if (refreshDepsManifest) {
            PuzzleVersionManifest versionManifest = VersionResolver.GSON_LOCAL.get().fromJson(manifestData, PuzzleVersionManifest.class);
            PuzzleVersion v = versionManifest.versions.get(version);

            Files.write(depsFileEpoc.toPath(), String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
            try (InputStream stream = v.dependencies.openStream()) {
                byte[] bytes = JavaUtils.readAllBytes(stream);
                depsData = new String(bytes);
                Files.write(depsFile.toPath(), bytes);
            }
        } else {
            depsData = new String(Files.readAllBytes(depsFile.toPath()));
        }
        PuzzleDependencies puzzleDependencies = VersionResolver.GSON_LOCAL.get().fromJson(depsData, PuzzleDependencies.class);
        for (PuzzleRepo repo : puzzleDependencies.repos) {
            repositories.maven(m -> {
                m.setName(repo.name);
                m.setUrl(repo.url);
            });
        }
        if (classifier.equals("common")) {
            for (PuzzleDependency puzzleDependency : puzzleDependencies.common) {
                if (!puzzleDependency.type.equals("implementation")) continue;
                String artifact = puzzleDependency.groupId + ":" + puzzleDependency.artifactId + ":" + puzzleDependency.version;
                if (puzzleDependency.classifier != null) {
                    artifact += ":" + puzzleDependency.classifier;
                }
                dependencies.add("implementation", artifact);
            }
        }

    }

}
