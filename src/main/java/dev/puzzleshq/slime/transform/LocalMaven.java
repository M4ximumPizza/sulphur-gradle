package dev.puzzleshq.slime.transform;

import org.gradle.api.Project;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalMaven {

    private static File localSlimeCacheDir;
    private static File localMavenDir;

    public static String transformName(String dependency) {
        String[] parts = dependency.split(":");
        parts[0] = "puzzle.remap." + parts[0];
        return String.join(":", parts);
    }

    public static File getLocalSlimeCache(Project project) {
        if (localSlimeCacheDir != null) return localSlimeCacheDir;
        localSlimeCacheDir = new File(project.file(".gradle"), ".local-slime-cache");
        return localSlimeCacheDir;
    }

    public static File getLocalMaven(Project project) {
        if (localMavenDir != null) return localMavenDir;
        localMavenDir = new File(getLocalSlimeCache(project), "transform-maven");
        project.getRepositories().ivy(i -> {
            try {
                i.setUrl(localMavenDir.toURI().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            i.artifactPattern("/[module]-[revision]-[classifier].jar");
            i.metadataSources(s -> {
                s.artifact();
                s.ignoreGradleMetadataRedirection();
            });
        });
        return localMavenDir;
    }

    public static File getArtifactDir(Project project, String artifactString) {
        String[] split = artifactString.split(":");
        String group = split[0];
        String module = split[1];
        String version = split[2];

        String[] paths = group.split("\\.");

        File localMaven = getLocalMaven(project);
        Path artifactDir = Paths.get(Paths.get(localMaven.getAbsolutePath(), paths).toString(), module, version);
        return artifactDir.toFile();
    }

    public static File getArtifactFile(Project project, String artifactString) {
        File artifactDir = getArtifactDir(project, artifactString);

        String[] split = artifactString.split(":");
        String module = split[1];
        String version = split[2];

        return new File(artifactDir, module + "-" + version + "-remapped.jar");
    }

}
