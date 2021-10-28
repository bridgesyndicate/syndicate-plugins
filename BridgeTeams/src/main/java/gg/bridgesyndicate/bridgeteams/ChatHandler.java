package gg.bridgesyndicate.bridgeteams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatHandler implements Listener {

    public ChatHandler() {
    }

    @EventHandler
    public static void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String msg = event.getMessage();
        event.setCancelled(true);
        Bukkit.broadcastMessage(ChatColor.GREEN + "[GAME] " + player.getName() + ChatColor.WHITE + ": " + msg);
    }
}
