package gg.bridgesyndicate.bridgeteams;

public class GameDataPollerFactory {
    public static GameDataPoller produce(){
        if (SyndicateEnvironment.SYNDICATE_ENV().equals(Environments.TEST) ||
                SyndicateEnvironment.SYNDICATE_ENV().equals(Environments.DEVELOPMENT)) {
            return new FileGameDataPoller();
        } else {
            return new SqsGameDataPoller();
        }
    }
}
