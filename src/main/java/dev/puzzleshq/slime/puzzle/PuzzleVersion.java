package dev.puzzleshq.slime.puzzle;

import java.net.URL;

public class PuzzleVersion {

    public String id;
    public URL dependencies;

    @Override
    public String toString() {
        return "PuzzleVersion{" +
                "id='" + id + '\'' +
                ", dependencies=" + dependencies +
                '}';
    }
}
