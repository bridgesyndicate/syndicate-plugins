package gg.bridgesyndicate.bridgeteams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatHandler implements Listener {

    private static final String TEAM_PREFIX = ChatColor.GREEN + "[TEAM] ";
    private static final String PLAYER_PREFIX = ChatColor.GREEN + "[GAME] ";
    private static final String SPECTATOR_PREFIX = ChatColor.GRAY + "[SPECTATOR] ";
    private static final int REQUIRED_PLAYERS_IN_SOLO = 2;

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
        for (Player anyone : Bukkit.getOnlinePlayers()) {
            if(anyone.getGameMode().equals(GameMode.SPECTATOR)){
                anyone.sendMessage(SPECTATOR_PREFIX + spectator.getName() + ": " + message);
            }
        }
    }

    private static void broadcastPlayerMessage(Player player, String message){
        String formattedMsg = ChatColor.WHITE + ": " + message;
        int requiredPlayers = BridgeTeams.getRequiredPlayers();
        if (requiredPlayers > REQUIRED_PLAYERS_IN_SOLO) {
            for (String receiverName : MatchTeam.getPlayers(MatchTeam.getTeam(player))) {
                Player receiver = Bukkit.getPlayer(receiverName);
                receiver.sendMessage(TEAM_PREFIX + MatchTeam.getChatColor(player) + player.getName() + formattedMsg);
            }
            // only broadcast to teammates in teams modes
        } else {
            Bukkit.broadcastMessage(PLAYER_PREFIX + MatchTeam.getChatColor(player) + player.getName() + formattedMsg);
            // broadcast to everyone in solo
        }
    }
}
