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
        region = regionProviderChain.getRegion();
    }
}