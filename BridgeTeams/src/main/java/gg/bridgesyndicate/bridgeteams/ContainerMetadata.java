package gg.bridgesyndicate.bridgeteams;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

public class ContainerMetadata {

    private String containerMetadataJson = "";

    ContainerMetadata(String url) throws IOException {
        if (url == null) {
            this.containerMetadataJson = HttpClient.getTestContainerMetadata();
        } else {
            this.containerMetadataJson = HttpClient.get(url);
        }
    }

    public String getTaskIP() {
        JsonElement jelement = new JsonParser().parse(containerMetadataJson);
        JsonObject jobject = jelement.getAsJsonObject();
        return (jobject.getAsJsonArray("Networks")
                .get(0)
                .getAsJsonObject()
                .getAsJsonArray("IPv4Addresses")
                .get(0)
                .getAsString());
    }

    public static void main(String[] args) throws IOException {
        ContainerMetadata containerMetadata = new ContainerMetadata(null);
        System.out.println(containerMetadata.getTaskIP());
    }
}