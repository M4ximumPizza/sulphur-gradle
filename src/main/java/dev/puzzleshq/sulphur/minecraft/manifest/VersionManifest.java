package dev.puzzleshq.sulphur.minecraft.manifest;

import java.util.Arrays;

public final class VersionManifest {

    public Latest latest;
    public Version[] versions;

    public Version find(String id) {
         Version[] found = Arrays.stream(versions).filter(v -> v.id.equals(id)).toArray(Version[]::new);
         if (found.length == 0)
             throw new IllegalArgumentException("Cannot find version '" + id + "' in the versions list, try updating the manifest or fix a typo!");

         return found[0];
    }

    @Override
    public String toString() {
        return "VersionManifest{" +
                "latest=" + latest +
                ", versions=" + Arrays.toString(versions) +
                '}';
    }

}
