package dev.puzzleshq.sulphur.puzzle;

import java.util.List;

public class PuzzleDependencies {

    public List<PuzzleDependency> client;
    public List<PuzzleDependency> common;
    public List<PuzzleDependency> server;
    public List<PuzzleRepo> repos;

    @Override
    public String toString() {
        return "PuzzleDependencies{" +
                "client=" + client +
                ", common=" + common +
                ", server=" + server +
                ", repos=" + repos +
                '}';
    }
}
