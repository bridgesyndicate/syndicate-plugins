package gg.bridgesyndicate.bridgeteams;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;

public class SyndicateAWSCredentials {

    private final com.amazonaws.auth.AWSCredentials credentials;
    private final String region;

    public AWSCredentials getCredentials() {
        return credentials;
    }

    public String getRegion() {
        return region;
    }

    public SyndicateAWSCredentials() {
        DefaultAWSCredentialsProviderChain credentialsProvider = new DefaultAWSCredentialsProviderChain();
        credentials = credentialsProvider.getCredentials();
        DefaultAwsRegionProviderChain regionProviderChain = new DefaultAwsRegionProviderChain();
        region = determineRegion(regionProviderChain.getRegion());
    }
    private String determineRegion(String region) {
        String syndicateWebServiceRegion = region;
        final String SYNDICATE_WEB_SERVICE_REGION = "SYNDICATE_WEB_SERVICE_REGION";
        if ( System.getenv(SYNDICATE_WEB_SERVICE_REGION) != null ) {
            syndicateWebServiceRegion = System.getenv(SYNDICATE_WEB_SERVICE_REGION);
            System.out.println("Using " + SYNDICATE_WEB_SERVICE_REGION + " from env: " + syndicateWebServiceRegion);
        }
        return syndicateWebServiceRegion;
    }
}