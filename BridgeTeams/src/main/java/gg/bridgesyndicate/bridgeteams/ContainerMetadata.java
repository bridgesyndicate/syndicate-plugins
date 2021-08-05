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

    public String getTaskArn() {
        JsonElement jelement = new JsonParser().parse(containerMetadataJson);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("Labels");
        return(jobject.get("com.amazonaws.ecs.task-arn").getAsString());
    }
}
