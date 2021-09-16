package gg.bridgesyndicate.bridgeteams;

import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.http.HttpMethodName;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static com.amazonaws.auth.internal.SignerConstants.X_AMZ_CONTENT_SHA256;


public class SyndicateWebServiceRequest {

    private final String resourcePath;
    private Request<Void> request;
    private boolean development = false;
    private HttpMethodName method;
    private String body = null;
    private final SyndicateAWSCredentials credentials;

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public Request<Void> getRequest() {
        return request;
    }

    public SyndicateWebServiceRequest(SyndicateAWSCredentials credentials, String resourcePath, HttpMethodName method, boolean development) throws URISyntaxException {
        this.credentials = credentials;
        this.development = development;
        this.method = method;
        this.resourcePath = (development) ? resourcePath : addProductionEnvironmentToResource(resourcePath);
//        request = createRequest();
//        signRequest(request, credentials);
    }

    public void createAndSignRequest() throws URISyntaxException {
        request = new DefaultRequest<>("execute-api");
        if (body != null) {
            request.setContent(new ByteArrayInputStream(body.getBytes()));
            request.addHeader(X_AMZ_CONTENT_SHA256, "required");
        }
        request.setHttpMethod(method);
        request.setEndpoint(new URI(getEndpoint()));
        request.setResourcePath(this.resourcePath);
        signRequest();
    }

    private void signRequest() {
        AWS4Signer signer = new AWS4Signer();
        signer.setRegionName(credentials.getRegion());
        signer.setServiceName(request.getServiceName());
        signer.sign(request, credentials.getCredentials());
    }

    private String addProductionEnvironmentToResource(String resourcePath) {
        String environment = "/Prod";
        return (environment + resourcePath);
    }

    private String getEndpoint() {
        return (development) ? getDevelopmentEndpoint () : getProductionEndpoint();
    }

    private String getDevelopmentEndpoint() {
        String host = "localhost:4567";
        String protocol = "http";
        return (protocol + "://" + host);
    }

    public String getProductionEndpoint() {
        String host = "knopfnsxoh.execute-api.us-west-2.amazonaws.com";
        String protocol = "https";
        return (protocol + "://" + host);
    }

    public URI getURI() {
        String url = getEndpoint() + resourcePath;
        return (URI.create(url));
    }
}
