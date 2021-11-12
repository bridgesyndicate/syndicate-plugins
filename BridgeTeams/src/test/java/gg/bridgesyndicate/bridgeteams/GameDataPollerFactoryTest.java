package gg.bridgesyndicate.bridgeteams;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.inventory.PlayerInventoryMock;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

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
        assertEquals(Game.GameState.BEFORE_GAME, plugin.getGame().getState());
        assertTrue(Game.serialize(plugin.getGame()) instanceof String);
    }
}