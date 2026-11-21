package dev.puzzleshq.slime.transform;

import dev.puzzleshq.gibberishdefiner.JarRemapper;
import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.slime.minecraft.Mappings;
import dev.puzzleshq.slime.util.FileUtil;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ArtifactCollection;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.FileCollectionDependency;
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.internal.component.local.model.OpaqueComponentArtifactIdentifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TransformPipeline {

    private final Project project;
    private IMapping mapping;
    private final File clientFile;

    public TransformPipeline(Project project, IMapping mapping, File clientFile) {
        this.project = project;
        this.mapping = mapping;
        this.clientFile = clientFile;
    }

    public IMapping getMapping() {
        return mapping;
    }

    public Project getProject() {
        return project;
    }

    public File getClientFile() {
        return clientFile;
    }

    public static TransformPipeline create(Project project, String version) {
        IMapping mapping = Mappings.getMappings(project, version);
        File clientFile = new File(FileUtil.getVersionFolder(project, version), "minecraft-" + version + "-client.jar");

        IMapping remixed = JarRemapper.createFinishedMapping(clientFile, mapping);
        return new TransformPipeline(project, remixed, clientFile);
    }

    public void reverse() {
        mapping = mapping.reverse();
    }

    public void remix(File file) {
        mapping = JarRemapper.createFinishedMapping(file, mapping);
    }

    public void remix(DependencySet incomingDependencies, ArtifactCollection incoming) {
        for (ResolvedArtifactResult artifact : incoming) {
            ComponentArtifactIdentifier artifactId = artifact.getId();

            ComponentIdentifier componentIdentifier = artifactId.getComponentIdentifier();
            if (componentIdentifier instanceof ModuleComponentIdentifier) {
                ModuleComponentIdentifier id = (ModuleComponentIdentifier) componentIdentifier;
                Optional<Dependency> first = incomingDependencies.stream().filter(d -> d.getGroup().equals(id.getGroup()) && d.getVersion().equals(id.getVersion()) && d.getName().equals(id.getModule())).findFirst();
                if (!first.isPresent()) {
                    continue;
                }

                String artifactLocation = id.getGroup() + ":" + id.getModule() + ":" + id.getVersion();

                System.out.println("Inspecting '" + artifactLocation + "'");
                remix(artifact.getFile());
            } else {
                File inFile = artifact.getFile();
                System.out.println("Inspecting '" + inFile.getName() + "'");
                remix(inFile);
            }
        }
    }

    public void remap(File in, File out) {
        JarRemapper.applyMapping(in, out, mapping);
    }

    public void remap(DependencySet incomingDependencies, ArtifactCollection incoming, DependencyHandler dependencies) {
        List<File> remapped = new ArrayList<>();
        for (ResolvedArtifactResult artifact : incoming) {
            ComponentArtifactIdentifier artifactId = artifact.getId();

            ComponentIdentifier componentIdentifier = artifactId.getComponentIdentifier();
            if (componentIdentifier instanceof ModuleComponentIdentifier) {
                ModuleComponentIdentifier id = (ModuleComponentIdentifier) componentIdentifier;
                Optional<Dependency> first = incomingDependencies.stream().filter(d -> Objects.equals(d.getGroup(), id.getGroup()) && Objects.equals(d.getVersion(), id.getVersion()) && Objects.equals(d.getName(), id.getModule())).findFirst();
                if (!first.isPresent()) {
                    remapped.add(artifact.getFile());
                    continue;
                }
                String artifactLocation = id.getGroup() + ":" + id.getModule() + ":" + id.getVersion();

                System.out.println("Transforming '" + artifactLocation + "'");
                File file = LocalMaven.getArtifactDir(project, LocalMaven.transformName(artifactLocation));
                if (!file.exists()) file.mkdirs();

                File remappedJar = new File(file, artifact.getFile().getName().replaceAll("\\.jar", "-remapped.jar"));

                JarRemapper.applyMapping(artifact.getFile(), remappedJar, mapping);
                remapped.add(remappedJar);
            } else {
                File inFile = artifact.getFile();
                System.out.println("Transforming '" + inFile.getName() + "'");
                File file = LocalMaven.getArtifactDir(project, LocalMaven.transformName("files:" + inFile.getName().substring(0, inFile.getName().lastIndexOf('.')) + ":0.0.0"));
                if (!file.exists()) file.mkdirs();
                File remappedJar = new File(file, inFile.getName().replaceAll("\\.jar", "-remapped.jar"));
                JarRemapper.applyMapping(artifact.getFile(), remappedJar, mapping);
                remapped.add(remappedJar);
            }
        }

        dependencies.add(SlimeConfigurations.nonTransitiveInclude, project.files((Object) remapped.toArray(new File[0])));
    }
}
