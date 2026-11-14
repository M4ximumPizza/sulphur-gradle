package dev.puzzleshq.slime.minecraft.manifest.client;

public class Downloads {

    public DownloadInfo client;
    public DownloadInfo server;

    public DownloadInfo client_mappings;
    public DownloadInfo server_mappings;

    public boolean hasOfficialMappings() {
        return client_mappings != null || server_mappings != null;
    }

    public boolean hasServer() {
        return server != null;
    }

}
