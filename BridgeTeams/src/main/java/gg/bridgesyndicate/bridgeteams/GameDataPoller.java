package gg.bridgesyndicate.bridgeteams;

public interface GameDataPoller {
    float NO_START_ABORT_TIME_IN_SECONDS = 120.0f;
    void poll(BridgeTeams bridgeTeams);
}
