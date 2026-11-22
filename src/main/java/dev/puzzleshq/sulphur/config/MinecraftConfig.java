package dev.puzzleshq.sulphur.config;

import dev.puzzleshq.lunar.Comment;
import dev.puzzleshq.lunar.ConfigClass;
import dev.puzzleshq.lunar.values.ConfigValue;
import dev.puzzleshq.lunar.values.ConfigValues;

@ConfigClass
public class MinecraftConfig {

    @Comment(" Minecraft Version")
    public final ConfigValue<String> minecraftVersion = ConfigValues.value("b1.7.3");

    @Comment(" Minecraft Version Manifest Source")
    public final ConfigValue<String> manifestSource = ConfigValues.value("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");

    @Comment(" Minecraft Dev Player Username")
    public final ConfigValue<String> mcUsername = ConfigValues.value("dev-player");

    @Comment(" Mappings Source for Obfuscated Minecraft [none, official, yarn:build.x, ornithemc:build.x, legacy-yarn:build.x]")
    public final ConfigValue<String> mappingsSource = ConfigValues.value("none");

}
