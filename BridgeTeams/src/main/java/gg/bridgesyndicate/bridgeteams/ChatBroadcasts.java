package gg.bridgesyndicate.bridgeteams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.UUID;

public class ChatBroadcasts {

    public static String nthGoal(Player player) {
        UUID ScorerId = player.getUniqueId();
        int score = BridgeTeams.goals.get(ScorerId);
        String nth;
        switch (score) {
            case 1: {
                nth = "1st Goal";
                break;
            }
            case 2: {
                nth = "2nd Goal";
                break;
            }
            case 3: {
                nth = "3rd Goal";
                break;
            }
            case 4: {
                nth = "4th Goal";
                break;
            }
            case 5: {
                nth = "5th Goal";
                break;
            }
            default:
                nth = "Unexpected Goal Amount";
                break;
        }
        return nth;

    }

    public static void redScoreMessage(Player player){

        CraftPlayer craft = (CraftPlayer) player;
        float absFloat = craft.getHandle().getAbsorptionHearts();
        int absInt = (int) absFloat;

        String heartValue;

        if(absInt == 0){
            double health = player.getHealth();
            DecimalFormat format = new DecimalFormat("##.#");
            heartValue = format.format(health);
        }
        else{
            int heartInt = (20 + absInt);
            heartValue = String.valueOf(heartInt);
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "                   "+ player.getName() + ChatColor.GRAY + " (" +
                ChatColor.GREEN + heartValue + ChatColor.RED + "❤" + ChatColor.GRAY + ") " + ChatColor.YELLOW + "scored! " +
                ChatColor.GRAY + "(" + ChatColor.GOLD + nthGoal(player) + ChatColor.GRAY + ")");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "                                     "+ GameScore.getRed() +
                ChatColor.RESET + "" + ChatColor.GRAY + " - " + ChatColor.BLUE + "" + ChatColor.BOLD + GameScore.getBlue());
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

    }

    public static void blueScoreMessage(Player player){

        CraftPlayer craft = (CraftPlayer) player;
        float absFloat = craft.getHandle().getAbsorptionHearts();
        int absInt = (int) absFloat;

        String heartValue;

        if(absInt == 0){
            double health = player.getHealth();
            DecimalFormat format = new DecimalFormat("##.#");
            heartValue = format.format(health);
        }
        else{
            int heartInt = (20 + absInt);
            heartValue = String.valueOf(heartInt);
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "                   " + player.getName() + ChatColor.GRAY + " (" +
                ChatColor.GREEN + heartValue + ChatColor.RED + "❤" + ChatColor.GRAY + ") " + ChatColor.YELLOW + "scored! " +
                ChatColor.GRAY + "(" + ChatColor.GOLD + nthGoal(player) + ChatColor.GRAY + ")");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "                                     " + GameScore.getBlue() +
                ChatColor.RESET + "" + ChatColor.GRAY + " - " + ChatColor.RED + "" + ChatColor.BOLD + GameScore.getRed());
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }

    public static void gameStartMessage(Player player, String opponentNames){

        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "                             The Bridge Duel");
        player.sendMessage(ChatColor.YELLOW + "                    Cross the bridge to score goals.");
        player.sendMessage(ChatColor.YELLOW + "           Knock off your opponent to gain a clear path.");
        player.sendMessage(" ");
        player.sendMessage(" ");
        player.sendMessage(ChatColor.YELLOW + "                  First player to score 5 goals wins!");
        player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "                       Opponent:  " + MatchTeam.getOpponentChatColor(player) + opponentNames);
        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

    }
}
