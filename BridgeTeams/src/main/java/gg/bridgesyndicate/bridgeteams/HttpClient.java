package gg.bridgesyndicate.bridgeteams;

import com.amazonaws.http.HttpMethodName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;

public class HttpClient {

    public enum PUT_REASONS { FINISHED_GAME, CONTAINER_METADATA, ABORTED_GAME }

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
                    return (EntityUtils.toString(entity));
                }
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
        throw new IOException("Something went terribly wrong");
    }

    public static String put(Game game, PUT_REASONS put_reason) throws IOException, URISyntaxException {
        boolean development = false;
        if ( System.getenv("SYNDICATE_SKIP_SERVICE_CALLS") != null ) {
            System.out.println("SYNDICATE_SKIP_SERVICE_CALLS is set. Skipping put().");
            return ("");
        }
        if ( System.getenv("SYNDICATE_ENV").equals("development")) {
            System.out.println("SYNDICATE_ENV is development, using local endpoints.");
            System.out.println("set SYNDICATE_SKIP_SERVICE_CALLS to skip service calls in development.");
            development = true;
        }
        PutMetaObject putMetaObject = getPaylodAndResourceForPut(game, put_reason);
        SyndicateWebServiceHttpClient syndicateWebServiceHttpClient =
                new SyndicateWebServiceHttpClient(putMetaObject.resource, HttpMethodName.PUT, development);
        int httpStatus = syndicateWebServiceHttpClient.put(putMetaObject.payload);
        if (httpStatus != HttpStatus.SC_OK) {
            throw new IOException("Response Status not 200, was: " + httpStatus);
        }
        System.out.println(put_reason + ": " + syndicateWebServiceHttpClient.getReturnValueString());
        return (syndicateWebServiceHttpClient.getReturnValueString());
    }

    public static String post(String taskArn) throws IOException, URISyntaxException {
        boolean development = false;
        if ( System.getenv("SYNDICATE_SKIP_SERVICE_CALLS") != null ) {
            System.out.println("SYNDICATE_SKIP_SERVICE_CALLS is set. Skipping post().");
            return ("");
        }
        if ( System.getenv("SYNDICATE_ENV").equals("development")) {
            System.out.println("SYNDICATE_ENV is development, using local endpoints.");
            System.out.println("set SYNDICATE_SKIP_SERVICE_CALLS to skip service calls in development.");
            development = true;
        }

        String resource = "/auth/scale-in";
        String payload = getPayloadForPost(taskArn);
        SyndicateWebServiceHttpClient syndicateWebServiceHttpClient =
                new SyndicateWebServiceHttpClient(resource, HttpMethodName.POST, development);
        int httpStatus = syndicateWebServiceHttpClient.post(payload);
        if (httpStatus != HttpStatus.SC_OK) {
            throw new IOException("Response Status not 200, was: " + httpStatus);
        }
        System.out.println("scale-in called, status: 200");
        return (syndicateWebServiceHttpClient.getReturnValueString());
    }

    private static String getPayloadForPost(String taskArn) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        PostPayload postPayload = new PostPayload(taskArn);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(postPayload);
    }

    private static PutMetaObject getPaylodAndResourceForPut(Game game, PUT_REASONS put_reason) throws JsonProcessingException {
        String payload = null;
        String resource = null;
        ObjectMapper mapper = new ObjectMapper();
        switch (put_reason) {
            case ABORTED_GAME:
            case FINISHED_GAME:
                resource = "/auth/game";
                payload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(game);
                break;
            case CONTAINER_METADATA:
                ArnUpdatePayload arnUpdatePayload = new ArnUpdatePayload(game.getUuid(), game.getTaskIP());
                payload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arnUpdatePayload);
                resource = "/auth/game/container_metadata";
            break;
            default:
        }
        return new PutMetaObject(resource, payload);
    }

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        String taskArn = "arn:aws:ecs:us-east-2:595508394202:task/SyndicateECSCluster/250d85bc107e4dcbb39666340c2a3d11";
        String payload = getPayloadForPost(taskArn);
        System.out.println(payload);
    }

    public static String getTestContainerMetadata(){
        return ("{\n" +
                "  \"DockerId\": \"250d85bc107e4dcbb39666340c2a3d1e-1942472365\",\n" +
                "  \"Name\": \"SyndicateBridgeTask\",\n" +
                "  \"DockerName\": \"SyndicateBridgeTask\",\n" +
                "  \"Image\": \"595508394202.dkr.ecr.us-west-2.amazonaws.com/syn-bridge-servers:latest\",\n" +
                "  \"ImageID\": \"sha256:40a17781c6efe883d633db4b9134a5ac613998d8856e9b7b19805641e0c6d009\",\n" +
                "  \"Labels\": {\n" +
                "    \"com.amazonaws.ecs.cluster\": \"arn:aws:ecs:us-east-2:595508394202:cluster/SyndicateECSCluster\",\n" +
                "    \"com.amazonaws.ecs.container-name\": \"SyndicateBridgeTask\",\n" +
                "    \"com.amazonaws.ecs.task-arn\": \"arn:aws:ecs:us-east-2:595508394202:task/SyndicateECSCluster/250d85bc107e4dcbb39666340c2a3d11\",\n" +
                "    \"com.amazonaws.ecs.task-definition-family\": \"SyndicateBridgeTaskDefinition\",\n" +
                "    \"com.amazonaws.ecs.task-definition-version\": \"5\"\n" +
                "  },\n" +
                "  \"DesiredStatus\": \"RUNNING\",\n" +
                "  \"KnownStatus\": \"RUNNING\",\n" +
                "  \"Limits\": {\n" +
                "    \"CPU\": 2\n" +
                "  },\n" +
                "  \"CreatedAt\": \"2022-03-23T03:32:38.199359593Z\",\n" +
                "  \"StartedAt\": \"2022-03-23T03:32:38.199359593Z\",\n" +
                "  \"Type\": \"NORMAL\",\n" +
                "  \"Networks\": [\n" +
                "    {\n" +
                "      \"NetworkMode\": \"awsvpc\",\n" +
                "      \"IPv4Addresses\": [\n" +
                "        \"10.1.15.193\"\n" +
                "      ],\n" +
                "      \"AttachmentIndex\": 0,\n" +
                "      \"MACAddress\": \"02:ba:db:de:5f:34\",\n" +
                "      \"IPv4SubnetCIDRBlock\": \"10.1.0.0/20\",\n" +
                "      \"DomainNameServers\": [\n" +
                "        \"10.1.0.2\"\n" +
                "      ],\n" +
                "      \"DomainNameSearchList\": [\n" +
                "        \"us-east-2.compute.internal\"\n" +
                "      ],\n" +
                "      \"PrivateDNSName\": \"ip-10-1-15-193.us-east-2.compute.internal\",\n" +
                "      \"SubnetGatewayIpv4Address\": \"10.1.0.1/20\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"ContainerARN\": \"arn:aws:ecs:us-east-2:595508394202:container/SyndicateECSCluster/250d85bc107e4dcbb39666340c2a3d1e/4cd5ef94-4d1b-4608-8570-1df2d310817b\",\n" +
                "  \"LogOptions\": {\n" +
                "    \"awslogs-group\": \"/ecs/SyndicateBridgeTask\",\n" +
                "    \"awslogs-region\": \"us-east-2\",\n" +
                "    \"awslogs-stream\": \"ecs/SyndicateBridgeTask/250d85bc107e4dcbb39666340c2a3d1e\"\n" +
                "  },\n" +
                "  \"LogDriver\": \"awslogs\"\n" +
                "}\n");
    }
}
class PutMetaObject {
    public final String resource;
    public final String payload;

    PutMetaObject(String resource, String payload){
        this.payload = payload;
        this.resource = resource;
    }
}

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
class PostPayload {
    public final String taskArn;
    PostPayload(String taskArn) {
        this.taskArn = taskArn;
    }
}

class ArnUpdatePayload {
    public final String uuid;
    public final String taskArn;

    ArnUpdatePayload(String uuid, String taskArn){
        this.uuid = uuid;
        this.taskArn = taskArn;
    }
}