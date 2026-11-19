package dev.puzzleshq.slime.transform;

import org.gradle.api.Project;
import org.gradle.api.artifacts.*;

public class SlimeConfigurations {

    public static final String puzzleLoaderConfigurationName = "puzzleLoader";
    public static final String remapConfigurationName = "internalRemap";
    public static final String nonTransitiveInclude = "internalInclude";

    public static Configuration puzzleLoaderConfiguration;
    public static Configuration remapConfiguration;
    public static Configuration nonTransitiveIncludeConfiguration;

    public static void init(Project project) {
        puzzleLoaderConfiguration = project.getConfigurations().maybeCreate(puzzleLoaderConfigurationName);
        puzzleLoaderConfiguration.setCanBeResolved(false);

        remapConfiguration = project.getConfigurations().maybeCreate(SlimeConfigurations.remapConfigurationName);
        remapConfiguration.setCanBeResolved(true);
        remapConfiguration.setCanBeConsumed(false);
        remapConfiguration.setTransitive(false);

        nonTransitiveIncludeConfiguration = project.getConfigurations().maybeCreate(nonTransitiveInclude);
        project.getConfigurations().maybeCreate("implementation").extendsFrom(nonTransitiveIncludeConfiguration);
        nonTransitiveIncludeConfiguration.setTransitive(false);

    }

}
