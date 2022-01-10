package gg.bridgesyndicate.bridgeteams;

import gg.bridgesyndicate.util.ReadFile;

import java.io.IOException;

public class FileGameDataPoller implements GameDataPoller {
    @Override
    public void poll(BridgeTeams bridgeTeams) {
        String jsonPath = ReadFile.pathToResources() + "sample-json/" + getGameJsonName();
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

    private String getGameJsonName() {
        if ( SyndicateEnvironment.SYNDICATE_ENV().equals(Environments.DEVELOPMENT ) ){
            String gameJson = System.getenv("SYNDICATE_TEST_GAME_JSON");
            if (gameJson != null && gameJson.length() > 0)
                return (gameJson);
        }
        return("game.json");
    }
}
