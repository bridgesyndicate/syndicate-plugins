package gg.bridgesyndicate.bridgeteams;

import gg.bridgesyndicate.util.ReadFile;

import java.io.IOException;

public class FileGameDataPoller implements GameDataPoller {
    @Override
    public void poll(BridgeTeams bridgeTeams) {
        String jsonPath = ReadFile.pathToResources() + "sample-json/game.json";
        bridgeTeams.getServer().getLogger().info("In test, using " + jsonPath);
        String json = null;
        try {
            json = ReadFile.read(jsonPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        bridgeTeams.getServer().getLogger().info("found message");
        Game game = Game.deserialize(json);
        bridgeTeams.receiveGame(game);
    }
}
