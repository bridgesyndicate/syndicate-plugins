package gg.bridgesyndicate.bridgeteams;

import org.apache.juneau.rest.client.RestClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ContainerMetadata {

    private String containerMetadata = null;

    public ContainerMetadata() throws URISyntaxException, IOException {
        String url = System.getenv("ECS_CONTAINER_METADATA_URI_V4");
        URI uri = new URI(url);
        containerMetadata = RestClient.create().plainText().build().doGet(uri).getResponseAsString();
    }
}
