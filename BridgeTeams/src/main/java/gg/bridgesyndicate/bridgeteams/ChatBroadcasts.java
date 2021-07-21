package gg.bridgesyndicate.bridgeteams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

        double health = player.getHealth();
        DecimalFormat format = new DecimalFormat("##.#");
        String heartValue = format.format(health);

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

        double health = player.getHealth();
        DecimalFormat format = new DecimalFormat("##.#");
        String heartValue = format.format(health);

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
}