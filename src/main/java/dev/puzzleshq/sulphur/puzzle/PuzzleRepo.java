package dev.puzzleshq.sulphur.puzzle;

import java.net.URL;

public class PuzzleRepo {

    public String name;
    public URL url;

    @Override
    public String toString() {
        return "PuzzleRepo{" +
                "name='" + name + '\'' +
                ", url=" + url +
                '}';
    }
}
