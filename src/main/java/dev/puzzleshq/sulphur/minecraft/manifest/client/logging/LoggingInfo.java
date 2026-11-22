package dev.puzzleshq.sulphur.minecraft.manifest.client.logging;

public class LoggingInfo {

    public String argument;
    public LoggingFile file;
    public String type;

    @Override
    public String toString() {
        return "LoggingInfo{" +
                "argument='" + argument + '\'' +
                ", file=" + file +
                ", type='" + type + '\'' +
                '}';
    }
}
