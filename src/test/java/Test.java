import dev.puzzleshq.sulphur.minecraft.manifest.Latest;
import dev.puzzleshq.sulphur.minecraft.manifest.Version;
import dev.puzzleshq.sulphur.minecraft.manifest.VersionManifest;

import static dev.puzzleshq.sulphur.VersionResolver.GSON_LOCAL;

public class Test {

//    public static final ThreadLocal<Gson> GSON_LOCAL = ThreadLocal.withInitial(Gson::new);
    public static void main(String[] args) {
        VersionManifest versionManifest = new VersionManifest();
        versionManifest.latest = new Latest();
        versionManifest.versions = new Version[1];

        Version version = new Version();
        version.id = "jfdsklaf";
        versionManifest.versions[0] = version;
        System.out.println(GSON_LOCAL.get().toJson(versionManifest));
    }
}
