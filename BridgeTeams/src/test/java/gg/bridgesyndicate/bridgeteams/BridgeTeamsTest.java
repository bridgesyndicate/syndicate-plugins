package gg.bridgesyndicate.bridgeteams;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.inventory.PlayerInventoryMock;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class BridgeTeamsTest {
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
    public void onPlayerJoin() {
//        PlayerMock player = server.addPlayer();
//        PlayerMock player = new PlayerMock(server, "Izzy Berland", UUID.fromString("2b7fa93b-f690-46b8-bfe6-a07b2ec42563"));
//       PlayerInventoryMock inventory = new PlayerInventoryMock(player, "");//       Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(player, "foo"));
//        assertEquals(1, Bukkit.getOnlinePlayers().size());
//        Bukkit.getPluginManager().callEvent(new PlayerQuitEvent(player, "foo"));
//        Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(player, "foo"));
//        assertEquals(1, Bukkit.getOnlinePlayers().size());
    }
}