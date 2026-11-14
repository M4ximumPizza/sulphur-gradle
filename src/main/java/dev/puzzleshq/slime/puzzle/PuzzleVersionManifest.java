package dev.puzzleshq.slime.puzzle;

import java.util.Map;

public class PuzzleVersionManifest {

    public Map<String, PuzzleVersion> versions;

    @Override
    public String toString() {
        return "PuzzleVersionManifest{" +
                "versions=" + versions +
                '}';
    }
}
