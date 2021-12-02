package gg.bridgesyndicate.bridgeteams;

import com.amazonaws.http.HttpMethodName;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

public class SyndicateWebServiceHttpClient {
    private static SyndicateWebServiceRequest syndicateWebServiceRequest;
    private String returnValueString = "";

    public SyndicateWebServiceHttpClient(String resourcePath, HttpMethodName method, boolean development) throws URISyntaxException {
        SyndicateAWSCredentials credentials = new SyndicateAWSCredentials();
        syndicateWebServiceRequest = new SyndicateWebServiceRequest(credentials, resourcePath, method, development);
    }

    public int get() throws IOException, URISyntaxException {
        syndicateWebServiceRequest.createAndSignRequest();
        return(doGet());
    }

    public int put(String payload) throws IOException, URISyntaxException {
        syndicateWebServiceRequest.setBody(payload);
        syndicateWebServiceRequest.createAndSignRequest();
        return(doPut());
    }

    public String getReturnValueString() {
        return returnValueString;
    }

    private int doGet() throws IOException {
        Map<String, String> headerMap = syndicateWebServiceRequest.getRequest().getHeaders();
        Iterator<String> iterator = headerMap.keySet().iterator();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(syndicateWebServiceRequest.getURI());
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = headerMap.get(key);
                httpGet.addHeader(key, value);
            }
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    returnValueString = EntityUtils.toString(entity);
                    for (Header header : response.getAllHeaders()) {
//                        System.out.println(header.getName() + " : " + header.getValue());
                    }
                }
                return (response.getStatusLine().getStatusCode());
            }
        }
    }

    private int doPut() throws IOException {
        Map<String, String> headerMap = syndicateWebServiceRequest.getRequest().getHeaders();
        Iterator<String> iterator = headerMap.keySet().iterator();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPut httpPut = new HttpPut(syndicateWebServiceRequest.getURI());
            HttpEntity httpEntity = new ByteArrayEntity(syndicateWebServiceRequest.getBody().getBytes(StandardCharsets.UTF_8));
            httpPut.setEntity(httpEntity);
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = headerMap.get(key);
                httpPut.addHeader(key, value);
            }
            try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    returnValueString = EntityUtils.toString(entity);
                    for (Header header : response.getAllHeaders()) {
                        System.out.println(header.getName() + " : " + header.getValue());
                    }
                }
                return (response.getStatusLine().getStatusCode());
            }
        }
    }
}

/*
See https://docs.aws.amazon.com/general/latest/gr/sigv4_signing.html
Add -Djava.util.logging.config.file=logging.properties to your JVM for debugging of sigV4.

    handlers= java.util.logging.ConsoleHandler
    .level = ALL
    java.util.logging.FileHandler.pattern = %h/java%u.log
    java.util.logging.FileHandler.limit = 50000
    java.util.logging.FileHandler.count = 1
    java.util.logging.FileHandler.formatter = java.util.logging.XMLFormatter
    java.util.logging.ConsoleHandler.level = ALL
    java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter

 */