package gg.bridgesyndicate.bridgeteams;

public interface GameDataPoller {
    int NO_START_ABORT_TIME_IN_SECONDS = 120;
    void poll(BridgeTeams bridgeTeams);
}
