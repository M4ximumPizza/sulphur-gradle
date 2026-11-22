package dev.puzzleshq.sulphur.minecraft.manifest.version.client;

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

    @Override
    public String toString() {
        return "Downloads{" +
                "client=" + client +
                ", server=" + server +
                ", client_mappings=" + client_mappings +
                ", server_mappings=" + server_mappings +
                '}';
    }
}
