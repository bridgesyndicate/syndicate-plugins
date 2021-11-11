package gg.bridgesyndicate.bridgeteams;

import gg.bridgesyndicate.util.ReadFile;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class GameTest {

    @Test
    public void deserialize() throws IOException {
        String json = ReadFile.read(ReadFile.pathToResources()  + "sample-json/another-game.json");
        Game game = Game.deserialize(json);
        assertTrue(game instanceof Game);
        assertTrue(Game.serialize(game) instanceof String);
    }
}