package dev.puzzleshq.slime.transform;

import org.gradle.api.Project;
import org.gradle.api.artifacts.*;

public class SlimeConfigurations {

    public static final String apiRemapConfigurationName = "remap";
    public static final String puzzleLoaderConfigurationName = "puzzleLoader";
    public static final String internalRemapConfigurationName = "internalRemap";
    public static final String nonTransitiveInclude = "internalInclude";

    public static Configuration puzzleLoaderConfiguration;
    public static Configuration internalRemapConfiguration;
    public static Configuration apiRemapConfiguration;
    public static Configuration nonTransitiveIncludeConfiguration;

    public static void init(Project project) {
        puzzleLoaderConfiguration = project.getConfigurations().maybeCreate(puzzleLoaderConfigurationName);
        puzzleLoaderConfiguration.setCanBeResolved(false);

        internalRemapConfiguration = project.getConfigurations().maybeCreate(SlimeConfigurations.internalRemapConfigurationName);
        internalRemapConfiguration.setCanBeResolved(true);
        internalRemapConfiguration.setCanBeConsumed(false);
        internalRemapConfiguration.setTransitive(false);

        apiRemapConfiguration = project.getConfigurations().maybeCreate(SlimeConfigurations.apiRemapConfigurationName);
        apiRemapConfiguration.setCanBeResolved(true);
        apiRemapConfiguration.setCanBeConsumed(false);
        apiRemapConfiguration.setTransitive(false);
        internalRemapConfiguration.extendsFrom(apiRemapConfiguration);

        nonTransitiveIncludeConfiguration = project.getConfigurations().maybeCreate(nonTransitiveInclude);
        project.getConfigurations().maybeCreate("implementation").extendsFrom(nonTransitiveIncludeConfiguration);
        nonTransitiveIncludeConfiguration.setTransitive(false);

    }

}
