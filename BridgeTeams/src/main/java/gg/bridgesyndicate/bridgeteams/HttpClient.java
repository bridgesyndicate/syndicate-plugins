package gg.bridgesyndicate.bridgeteams;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClient {

    public static String get(String url) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpGet request = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(request);
            try {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new IOException("Response Status not 200, was: " + response.getStatusLine().getStatusCode());
                }
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity);
                    return (result);
                }
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
        throw new IOException("Something went terribly wrong");
    }

    public static String getTestContainerMetadata(){
        return ("{\n" +
                "  \"DockerId\": \"5b42900b6d094b249db5315b7cb83612-2590604502\",\n" +
                "  \"Name\": \"bridge-dev-server\",\n" +
                "  \"DockerName\": \"bridge-dev-server\",\n" +
                "  \"Image\": \"595508394202.dkr.ecr.us-west-2.amazonaws.com/syn-bridge-servers\",\n" +
                "  \"ImageID\": \"sha256:95d7ff1afab8632f4fad8a05a68c7f19fd69f989f1fcbebdfdcfe08684648b9a\",\n" +
                "  \"Labels\": {\n" +
                "    \"com.amazonaws.ecs.cluster\": \"arn:aws:ecs:us-west-2:595508394202:cluster/default\",\n" +
                "    \"com.amazonaws.ecs.container-name\": \"bridge-dev-server\",\n" +
                "    \"com.amazonaws.ecs.task-arn\": \"arn:aws:ecs:us-west-2:595508394202:task/default/5b42900b6d094b249db5315b7cb83612\",\n" +
                "    \"com.amazonaws.ecs.task-definition-family\": \"bridge-dev-server\",\n" +
                "    \"com.amazonaws.ecs.task-definition-version\": \"4\"\n" +
                "  },\n" +
                "  \"DesiredStatus\": \"RUNNING\",\n" +
                "  \"KnownStatus\": \"RUNNING\",\n" +
                "  \"Limits\": {\n" +
                "    \"CPU\": 2\n" +
                "  },\n" +
                "  \"CreatedAt\": \"2021-08-01T18:48:46.83778976Z\",\n" +
                "  \"StartedAt\": \"2021-08-01T18:48:46.83778976Z\",\n" +
                "  \"Type\": \"NORMAL\",\n" +
                "  \"Networks\": [\n" +
                "    {\n" +
                "      \"NetworkMode\": \"awsvpc\",\n" +
                "      \"IPv4Addresses\": [\n" +
                "        \"10.0.0.35\"\n" +
                "      ],\n" +
                "      \"AttachmentIndex\": 0,\n" +
                "      \"MACAddress\": \"06:87:73:2d:ba:a9\",\n" +
                "      \"IPv4SubnetCIDRBlock\": \"10.0.0.0/24\",\n" +
                "      \"DomainNameServers\": [\n" +
                "        \"10.0.0.2\"\n" +
                "      ],\n" +
                "      \"DomainNameSearchList\": [\n" +
                "        \"us-west-2.compute.internal\"\n" +
                "      ],\n" +
                "      \"PrivateDNSName\": \"ip-10-0-0-35.us-west-2.compute.internal\",\n" +
                "      \"SubnetGatewayIpv4Address\": \"10.0.0.1/24\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"ContainerARN\": \"arn:aws:ecs:us-west-2:595508394202:container/default/5b42900b6d094b249db5315b7cb83612/f6af1056-cadb-4547-bf7c-a61988aab72c\",\n" +
                "  \"LogOptions\": {\n" +
                "    \"awslogs-group\": \"/ecs/bridge-dev-server\",\n" +
                "    \"awslogs-region\": \"us-west-2\",\n" +
                "    \"awslogs-stream\": \"ecs/bridge-dev-server/5b42900b6d094b249db5315b7cb83612\"\n" +
                "  },\n" +
                "  \"LogDriver\": \"awslogs\"\n" +
                "}\n");
    }
}