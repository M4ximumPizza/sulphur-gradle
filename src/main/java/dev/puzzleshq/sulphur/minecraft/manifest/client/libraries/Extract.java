package dev.puzzleshq.sulphur.minecraft.manifest.client.libraries;

import java.util.Arrays;

public class Extract {

    public String[] exclude;

    @Override
    public String toString() {
        return "Extract{" +
                "exclude=" + Arrays.toString(exclude) +
                '}';
    }
}
