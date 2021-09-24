package gg.bridgesyndicate.bridgeteams;

import com.amazonaws.http.HttpMethodName;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

public class TestHttpGet {

    private static void doTestGet() throws URISyntaxException, IOException {
//        UUID uuid = UUID.randomUUID();
        boolean development = false;
        String uuid = "c5ca7535-2946-4cba-8863-511cc739c0c0";
        SyndicateWebServiceHttpClient syndicateWebServiceHttpClient =
                new SyndicateWebServiceHttpClient("/auth/user/by-minecraft-uuid/" + uuid, HttpMethodName.GET, development);
        int httpStatus = syndicateWebServiceHttpClient.get();
        System.out.println("status was " + httpStatus);
        System.out.println(syndicateWebServiceHttpClient.getReturnValueString());
    }

    private static void doTestPut() throws URISyntaxException, IOException {
        boolean development = false;
        String uuid = "c996dae3-433c-48f4-8a90-d4ea2d50f2a6";
        String taskArn = "10.18.0.1";
        ObjectMapper mapper = new ObjectMapper();
        ArnUpdatePayload arnUpdatePayload = new ArnUpdatePayload(uuid, taskArn);
        String payload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arnUpdatePayload);
        System.out.println(payload);
        SyndicateWebServiceHttpClient syndicateWebServiceHttpClient =
                new SyndicateWebServiceHttpClient("/auth/game/container_metadata", HttpMethodName.PUT, development);
        int httpStatus = syndicateWebServiceHttpClient.put(payload);
        System.out.println("status was " + httpStatus);
        System.out.println(syndicateWebServiceHttpClient.getReturnValueString());
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
//        doTestGet();
        doTestPut();
    }
}

