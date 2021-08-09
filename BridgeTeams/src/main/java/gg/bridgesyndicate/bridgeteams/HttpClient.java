package gg.bridgesyndicate.bridgeteams;

import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

import static com.amazonaws.auth.internal.SignerConstants.X_AMZ_CONTENT_SHA256;


public class HttpClient {

    public enum PUT_REASONS { FINISHED_GAME, CONTAINER_METADATA, ABORTED_GAME }

    public static URI uriFactory(){
        String protocol = "https";
        String host = "knopfnsxoh.execute-api.us-west-2.amazonaws.com";
        String path = "/Prod/auth/game/container_metadata";
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

    public static void main(String[] args) throws IOException, URISyntaxException {
        put(new Game(), PUT_REASONS.ABORTED_GAME);
    }

    public static String getFileContent(FileInputStream fis, String encoding) throws IOException
    {
        try( BufferedReader br =
                     new BufferedReader( new InputStreamReader(fis, encoding)))
        {
            StringBuilder sb = new StringBuilder();
            String line;
            while(( line = br.readLine()) != null ) {
                sb.append( line );
                sb.append( '\n' );
            }
            return sb.toString();
        }
    }

    public static String put(Game game, PUT_REASONS PUT_REASONS) throws IOException, URISyntaxException {
        DefaultAWSCredentialsProviderChain credentialsProvider = new DefaultAWSCredentialsProviderChain();
        AWSCredentials credentials = credentialsProvider.getCredentials();
        System.out.println(credentials.getAWSAccessKeyId());
        System.out.println(credentials.getAWSSecretKey());
        DefaultAwsRegionProviderChain regionProviderChain = new DefaultAwsRegionProviderChain();
        String region = regionProviderChain.getRegion();

        Request<Void> request = new DefaultRequest<Void>("execute-api");
        request.setHttpMethod(HttpMethodName.PUT);
        request.setEndpoint(new URI("https://knopfnsxoh.execute-api.us-west-2.amazonaws.com"));
        request.setResourcePath("/Prod/auth/game/container_metadata");
        String PAYLOAD_FILE = "/home/harry/syndicate-web-service/spec/mocks/game/container_metadata.json";
        FileInputStream fis2 = new FileInputStream(PAYLOAD_FILE);
        String payloadString = getFileContent(fis2, "UTF-8");
        request.setContent(new ByteArrayInputStream(payloadString.getBytes()));
        request.addHeader(X_AMZ_CONTENT_SHA256, "required");

        AWS4Signer signer = new AWS4Signer();
        signer.setRegionName(region);
        signer.setServiceName(request.getServiceName());
        // 2021-08-09 T 01:46:33 Z//
//        Date staticTestingDate = new Date(121, 7, 8, 18, 46, 33);
//        signer.setOverrideDate(staticTestingDate);
        signer.sign(request, credentials);

        Map<String, String> headerMap = request.getHeaders();
        Iterator<String> iterator = headerMap.keySet().iterator();

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPut httpPut = new HttpPut(uriFactory());

            HttpEntity body = new StringEntity(payloadString);
            httpPut.setEntity(body);

            while(iterator.hasNext()) {
                String key = iterator.next();
                String value = headerMap.get(key);
                System.out.println(key + " : " + value);
                httpPut.addHeader(key,value);
            }
            CloseableHttpResponse response = httpClient.execute(httpPut);
            try {
                if (response.getStatusLine().getStatusCode() != 200) {
                    HttpEntity entity = response.getEntity();
                    System.out.println(EntityUtils.toString(entity));
                    throw new IOException("Response Status not 200, was: " + response.getStatusLine().getStatusCode());
                }
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    System.out.println(EntityUtils.toString(entity));
                    return ("foo");
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