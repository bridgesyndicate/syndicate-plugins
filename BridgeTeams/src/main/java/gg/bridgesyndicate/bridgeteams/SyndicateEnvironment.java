package gg.bridgesyndicate.bridgeteams;

public class SyndicateEnvironment {

    public static Environments SYNDICATE_ENV() {
        String syndicateEnv = System.getenv("SYNDICATE_ENV");
        if (syndicateEnv == null || syndicateEnv.equals("development")) {
            return Environments.DEVELOPMENT;
        } else if (syndicateEnv.equals("test") ) {
            return Environments.TEST;
        } else {
            return Environments.PRODUCTION;
        }
    }
}
