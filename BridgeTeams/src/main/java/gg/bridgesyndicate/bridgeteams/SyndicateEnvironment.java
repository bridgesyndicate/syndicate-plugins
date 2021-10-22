package gg.bridgesyndicate.bridgeteams;

public class SyndicateEnvironment {

    public static Environments SYNDICATE_ENV() {
        String syndicateEnv = System.getenv("SYNDICATE_ENV");
        if (syndicateEnv == null || syndicateEnv.equals("development")) {
            return Environments.DEVELOPMENT;
        }
        return Environments.PRODUCTION;
    }
}
