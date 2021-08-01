package gg.bridgesyndicate.bridgeteams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.text.DecimalFormat;

public class ChatBroadcasts {

    private static final String DASH = "▬";
    private static final String SPACE = " ";

    private static String ordinal(int i) {
        String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + suffixes[i % 10];
        }
    }

    public static String ordinalStringForPlayerGoal(Game game, Player player) {
        int score = game.getNumberOfGoalsForPlayer(player);
        return (ordinal(score) + " Goal");
    }

    public static void scoreMessage(Game game, Player player){
        int absInt = 0;
        String heartValue;

        if(absInt == 0){
            double health = player.getHealth();
            DecimalFormat format = new DecimalFormat("##.#");
            heartValue = format.format(health);
        } else {
            int heartInt = (20 + absInt);
            heartValue = String.valueOf(heartInt);
        }

        Bukkit.broadcastMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + getDashes(64) );
        broadcastNewLine();
        String goalLineString = MatchTeam.getChatColor(player).toString() + ChatColor.BOLD + player.getName() + ChatColor.GRAY + " (" +
                ChatColor.GREEN + heartValue + ChatColor.RED + "❤" + ChatColor.GRAY + ") " + ChatColor.YELLOW + "scored! " +
                ChatColor.GRAY + "(" + ChatColor.GOLD + ordinalStringForPlayerGoal(game, player) + ChatColor.GRAY + ")";
        int lengthOfGoalLineString = picaSize(goalLineString);
        int spaces = (64 - (lengthOfGoalLineString/1000))/2;
        Bukkit.broadcastMessage(getSpaces(spaces) + goalLineString);
        Bukkit.broadcastMessage(MatchTeam.getChatColor(player).toString() + ChatColor.BOLD +
                getSpaces(29)+ getScoreForPlayersTeam(player) + ChatColor.RESET +
                ChatColor.GRAY + " - " + MatchTeam.getOpponentChatColor(player) +
                ChatColor.BOLD + getScoreForOpponentsTeam(player));
        broadcastNewLine();
        Bukkit.broadcastMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + getDashes(64));
    }

    private static void broadcastNewLine() {
        Bukkit.broadcastMessage("");
    }

    private static int getScoreForOpponentsTeam(Player player) {
        return (MatchTeam.getChatColor(player) == ChatColor.BLUE) ? GameScore.getRed() : GameScore.getBlue();
    }

    private static int getScoreForPlayersTeam(Player player) {
        return (MatchTeam.getChatColor(player) == ChatColor.RED) ? GameScore.getRed() : GameScore.getBlue();
    }

    private static String getDashes(int n) {
        n = (n < 1 ) ? 1 : n;
        return (new String(new char[n]).replace("\0", DASH));
    }

    private static String getSpaces(int n) {
        n = (n < 1 ) ? 1 : n;
        return (new String(new char[n]).replace("\0", SPACE));
    }

    static int picaSize(String s) {
        // the following characters are sorted by width in Arial font
        String lookup = " .:,;'^`!|jl/\\i-()JfIt[]?{}sr*a\"ce_gFzLxkP+0123456789<=>~qvy$SbduEphonTBCXY#VRKZN%GUAHD@OQ&wmMW";
        int result = 0;
        for (int i = 0; i < s.length(); ++i)
        {
            int c = lookup.indexOf(s.charAt(i));
            result += (c < 0 ? 60 : c) * 7 + 200;
        }
        return result;
    }

    public static void gameStartMessage(Player player, String opponentNames, Game game){

        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + getDashes(64));
        player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "                             The Bridge Duel");
        player.sendMessage(ChatColor.YELLOW + "                    Cross the bridge to score goals.");
        player.sendMessage(ChatColor.YELLOW + "           Knock off your opponent to gain a clear path.");
        player.sendMessage(" ");
        player.sendMessage(" ");
        player.sendMessage(ChatColor.YELLOW + "                  First player to score " + game.goalsToWin + " goals wins!");
        player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "                       Opponent:  " + MatchTeam.getOpponentChatColor(player) + opponentNames);
        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + getDashes(64));

    }
}
