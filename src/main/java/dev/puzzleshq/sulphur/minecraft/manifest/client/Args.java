package dev.puzzleshq.sulphur.minecraft.manifest.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Args {

    public JsonArray game;
    public JsonArray jvm;

    @Override
    public String toString() {
        return "Args{" +
                "game=" + game +
                ", jvm=" + jvm +
                '}';
    }
}
