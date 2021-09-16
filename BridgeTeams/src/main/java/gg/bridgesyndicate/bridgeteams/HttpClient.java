package gg.bridgesyndicate.bridgeteams;

import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static com.amazonaws.auth.internal.SignerConstants.X_AMZ_CONTENT_SHA256;


public class HttpClient {

    public enum PUT_REASONS { FINISHED_GAME, CONTAINER_METADATA, ABORTED_GAME }

    public static URI uriFactory(String path){
        String protocol = "https";
        String host = "knopfnsxoh.execute-api.us-west-2.amazonaws.com";
        String url = protocol + "://" + host + path;
        return(URI.create(url));
    }

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

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        String gameJson = "{  \"blueTeam\": [\n" +
                "    \"vice9\"\n" +
                "  ],\n" +
                "  \"uuid\": \"12345678-d05a-4b36-a489-8584b10deb7a\",\n" +
                "  \"requiredPlayers\": 2,\n" +
                "  \"goalsToWin\": 2,\n" +
                "  \"gameLengthInSeconds\": 600,\n" +
                "  \"redTeam\": [\n" +
                "    \"NitroholicPls\"\n" +
                "  ]\n" +
                "}}\n";
        Game game = Game.deserialize(gameJson);
        game.addContainerMetaData();
//        game.playerJoined("NitroholicPls");
//        game.playerJoined("vice9");
        game.setState(Game.GameState.CAGED);
        game.setState(Game.GameState.DURING_GAME);
        game.addGoalInfo(UUID.randomUUID());
//        Thread.sleep(2000);
        game.setState(Game.GameState.AFTER_GAME);
        game.setEndTime();
        game.addKillInfo(UUID.randomUUID());
        System.out.println(Game.serialize(game));
        String foo = put(game, PUT_REASONS.FINISHED_GAME);
        System.out.println(foo);
    }

    public static String put(Game game, PUT_REASONS put_reason) throws IOException, URISyntaxException {
        if ( System.getenv("SYNDICATE_ENV") == null ){
            System.out.println("SYNDICATE_ENV not set. Skipping put().");
            return("Foo");
        } else if ( System.getenv("SYNDICATE_ENV").equals("development")) {
            System.out.println("SYNDICATE_ENV is development, using local endpoints.");
        }
        PutMetaObject putMetaObject = getPaylodAndResourceForPut(game, put_reason);

        DefaultAWSCredentialsProviderChain credentialsProvider = new DefaultAWSCredentialsProviderChain();
        AWSCredentials credentials = credentialsProvider.getCredentials();
        DefaultAwsRegionProviderChain regionProviderChain = new DefaultAwsRegionProviderChain();
        String region = regionProviderChain.getRegion();
        System.out.println(credentials.getAWSAccessKeyId());
        Request<Void> request = new DefaultRequest<Void>("execute-api");
        request.setHttpMethod(HttpMethodName.PUT);
        request.setEndpoint(new URI("https://knopfnsxoh.execute-api.us-west-2.amazonaws.com"));
        request.setResourcePath(putMetaObject.resource);
        String payloadString = putMetaObject.payload;
        request.setContent(new ByteArrayInputStream(payloadString.getBytes()));
        request.addHeader(X_AMZ_CONTENT_SHA256, "required");

        AWS4Signer signer = new AWS4Signer();
        signer.setRegionName(region);
        signer.setServiceName(request.getServiceName());
        signer.sign(request, credentials);

        Map<String, String> headerMap = request.getHeaders();
        Iterator<String> iterator = headerMap.keySet().iterator();

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPut httpPut = new HttpPut(uriFactory(putMetaObject.resource));

            HttpEntity body = new StringEntity(payloadString);
            httpPut.setEntity(body);

            while(iterator.hasNext()) {
                String key = iterator.next();
                String value = headerMap.get(key);
                httpPut.addHeader(key,value);
            }
            CloseableHttpResponse response = httpClient.execute(httpPut);
            try {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new IOException("Response Status not 200, was: " + response.getStatusLine().getStatusCode());
                }
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String returnValueString = EntityUtils.toString(entity);
                    System.out.println("Result from " + put_reason + returnValueString);
                    return(returnValueString);
                }
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
        throw new IOException("Something went terribly wrong");
    }

    private static PutMetaObject getPaylodAndResourceForPut(Game game, PUT_REASONS put_reason) throws JsonProcessingException {
        String payload = null;
        String resource = null;
        ObjectMapper mapper = new ObjectMapper();
        switch (put_reason) {
            case ABORTED_GAME:
            case FINISHED_GAME:
                resource = "/Prod/auth/game";
                payload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(game);
                break;
            case CONTAINER_METADATA:
                ArnUpdatePayload arnUpdatePayload = new ArnUpdatePayload(game.getUuid(), game.getTaskArn());
                payload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arnUpdatePayload);
                resource = "/Prod/auth/game/container_metadata";
            break;
            default:
        }
        return new PutMetaObject(resource, payload);
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
class PutMetaObject {
    public final String resource;
    public final String payload;

    PutMetaObject(String resource, String payload){
        this.payload = payload;
        this.resource = resource;
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