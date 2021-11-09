package gg.bridgesyndicate.bridgeteams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatHandler implements Listener {

    private static final String PLAYER_PREFIX = ChatColor.GREEN + "[GAME] ";
    private static final String SPECTATOR_PREFIX = ChatColor.GRAY + "[SPECTATOR] ";

    public ChatHandler() {
    }

    @EventHandler
    public static void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if(player.getGameMode().equals(GameMode.SPECTATOR)){
            broadcastSpectatorMessage(player, message);
        } else {
            broadcastPlayerMessage(player, message);
        }
        event.setCancelled(true);
    }

    private static void broadcastSpectatorMessage(Player spectator, String message){
        Bukkit.broadcastMessage(SPECTATOR_PREFIX + spectator.getName() + ": " + message);
    }

    private static void broadcastPlayerMessage(Player player, String message){
        Bukkit.broadcastMessage(PLAYER_PREFIX + MatchTeam.getChatColor(player) + player.getName() + ChatColor.WHITE + ": " + message);
    }
}
