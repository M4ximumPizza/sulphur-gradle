package dev.puzzleshq.sulphur.minecraft.manifest.version;

import dev.puzzleshq.sulphur.VersionResolver;
import dev.puzzleshq.sulphur.minecraft.manifest.version.client.VersionData;
import org.gradle.api.Project;

import java.net.URL;

public class Version {

    public String id;
    public String type;
    public URL url;
    public String time;
    public String releaseTime;
    public String sha1;
    public String complianceLevel;

    @Override
    public String toString() {
        return "Version{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", time='" + time + '\'' +
                ", releaseTime='" + releaseTime + '\'' +
                ", sha1='" + sha1 + '\'' +
                ", complianceLevel='" + complianceLevel + '\'' +
                '}';
    }

    public VersionData get(Project project) {
        return VersionResolver.getClientJson(project, this);
    }

}
