package gg.bridgesyndicate.bridgeteams;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

public class ContainerMetadata {

    private String containerMetadataJson = "";

    ContainerMetadata(boolean useTestData) throws IOException {
        if (useTestData) {
            this.containerMetadataJson = HttpClient.getTestContainerMetadata();
        } else {
            String url = System.getenv("ECS_CONTAINER_METADATA_URI_V4");
            this.containerMetadataJson = HttpClient.get(url);
        }
    }

    public String getTaskArn() {
        JsonElement jelement = new JsonParser().parse(containerMetadataJson);
        JsonObject jobject = jelement.getAsJsonObject();
        return (jobject.getAsJsonObject("Labels")
                .get("com.amazonaws.ecs.task-arn")
                .getAsString());
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
        ContainerMetadata containerMetadata = new ContainerMetadata(true);
        System.out.println(containerMetadata.getTaskIP());
    }
}