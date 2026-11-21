package dev.puzzleshq.slime.config;

import dev.puzzleshq.lunar.Comment;
import dev.puzzleshq.lunar.ConfigClass;
import dev.puzzleshq.lunar.values.ConfigValue;
import dev.puzzleshq.lunar.values.ConfigValueList;
import dev.puzzleshq.lunar.values.ConfigValues;

@ConfigClass
public class PuzzleConfig {

    @Comment(" Interface Injectors")
    public final ConfigValueList<String> injectors = ConfigValues.list(new String[0], 0, Integer.MAX_VALUE);

    @Comment(" Access Transformer/Manipulators/Wideners")
    public final ConfigValueList<String> wideners = ConfigValues.list(new String[0], 0, Integer.MAX_VALUE);

}
