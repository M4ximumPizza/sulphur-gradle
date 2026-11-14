package dev.puzzleshq.slime.minecraft.manifest.client.libraries;

public class Rule {

    public String action;
    public OSRule os;

    @Override
    public String toString() {
        return "Rule{" +
                "action='" + action + '\'' +
                ", os=" + os +
                '}';
    }
}
