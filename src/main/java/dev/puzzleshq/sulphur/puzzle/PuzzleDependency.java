package dev.puzzleshq.sulphur.puzzle;

public class PuzzleDependency {

    public String groupId;
    public String artifactId;
    public String version;
    public String classifier;
    public String type;

    @Override
    public String toString() {
        return "PuzzleDependency{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", classifier='" + classifier + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
