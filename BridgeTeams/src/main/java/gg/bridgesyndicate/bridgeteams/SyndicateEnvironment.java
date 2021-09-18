package gg.bridgesyndicate.bridgeteams;

public class SyndicateEnvironment {

    public static Environments SYNDICATE_ENV() {
        String syndicateEnv = System.getenv("SYNDICATE_ENV");
        if (syndicateEnv == null) {
            return Environments.DEVELOPMENT;
        }
        return Environments.PRODUCTION;
    }
}
