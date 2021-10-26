package gg.bridgesyndicate.bridgeteams;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ArrowCooldown implements Listener {
    private final BridgeTeams plugin;

    public ArrowCooldown(BridgeTeams pluginMain) {
        this.plugin = pluginMain;
    }

    @EventHandler // just to see if this method would go through (it didnt)
    public void onPlayerC(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("works");
    }


}
