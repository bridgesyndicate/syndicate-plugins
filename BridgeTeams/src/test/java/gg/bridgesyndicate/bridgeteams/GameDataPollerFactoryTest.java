package gg.bridgesyndicate.bridgeteams;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameDataPollerFactoryTest {
    private ServerMock server;
    private BridgeTeams plugin;

    @Before
    public void setUp() throws Exception {
        server = MockBukkit.mock();
        server.addSimpleWorld("world");
        plugin = (BridgeTeams) MockBukkit.load(BridgeTeams.class);
    }

    @After
    public void tearDown() throws Exception {
        MockBukkit.unload();
    }

    @Test
    public void produce() throws JsonProcessingException {
        assertEquals(Game.GameState.AWAITING_PLAYERS, plugin.getGame().getState());
        assertTrue(Game.serialize(plugin.getGame()) instanceof String);
    }
}