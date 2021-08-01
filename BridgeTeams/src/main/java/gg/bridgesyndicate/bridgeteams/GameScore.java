package gg.bridgesyndicate.bridgeteams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

class GameScore {
    private static GameScore single_instance = null;
    public static int red;
    public static int blue;
    public enum scoreboardSections { RED_SCORE, BLUE_SCORE, KILLS, GOALS }
    private static final String BUBBLE = "⬤";
    private static final String RED_SCORE_LINE = ChatColor.RED + "[R] " + ChatColor.GRAY;
    private static final String BLUE_SCORE_LINE = ChatColor.BLUE + "[B] " + ChatColor.GRAY;
    private static final String KILLS_LINE = ChatColor.WHITE + "Kills: ";


    private GameScore() {
        red = 0;
        blue = 0;
    }

    /* METHODS */

    public static GameScore getInstance() {
        if (single_instance == null)
            single_instance = new GameScore();
        return single_instance;
    }

    public static int getRed() {
        return red;
    }

    public static int getBlue() {
        return blue;
    }

    public static void initialize(Scoreboard board, Objective objective) {
        Team redScore = board.registerNewTeam(String.valueOf(scoreboardSections.RED_SCORE));
        redScore.addEntry(RED_SCORE_LINE);
        redScore.setSuffix(new String(new char[5]).replace("\0", BUBBLE));
        redScore.setPrefix("");
        objective.getScore(RED_SCORE_LINE).setScore(0);

        Team blueScore = board.registerNewTeam(String.valueOf(scoreboardSections.BLUE_SCORE));
        blueScore.addEntry(BLUE_SCORE_LINE);
        blueScore.setSuffix(new String(new char[5]).replace("\0", BUBBLE));
        blueScore.setPrefix("");
        objective.getScore(BLUE_SCORE_LINE).setScore(0);

        Team kills = board.registerNewTeam(String.valueOf(scoreboardSections.KILLS));
        kills.addEntry(KILLS_LINE);
        kills.setSuffix(ChatColor.GREEN + "0");
        kills.setPrefix("");
        objective.getScore(KILLS_LINE).setScore(0);

    }

    public void increment(TeamType playerTeam) {
        if (playerTeam == TeamType.BLUE) {
            blue++;
        } else {
            red++;
        }
        updateScoreboardScore();
    }

    private void updateScoreboardScore() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard board = player.getScoreboard();
            updateScoreBubbles(board);
        }
    }

    private void updateScoreBubbles(Scoreboard board) {
        Team redScore = board.getTeam(String.valueOf(scoreboardSections.RED_SCORE));
        redScore.setSuffix(getBubbles(red, ChatColor.RED));
        Team blueScore = board.getTeam(String.valueOf(scoreboardSections.BLUE_SCORE));
        blueScore.setSuffix(getBubbles(blue, ChatColor.BLUE));
    }

    public void updateKillersKills(Player killer, Game game) {
        Scoreboard board = killer.getScoreboard();
        Team kills = board.getTeam(String.valueOf(scoreboardSections.KILLS));
        kills.setSuffix(ChatColor.GREEN + "" + game.getNumberOfKillsForPlayer(killer) );
    }

    private String getBubbles(int n, ChatColor teamColor) {
        String bubbles = new String();
        int idx = 0;
        while (idx < 5) {
            if (n > idx) { // team-colored bubble
                bubbles = bubbles.concat(teamColor + BUBBLE);
            } else { //grey bubble
                bubbles = bubbles.concat(ChatColor.GRAY + BUBBLE);
            }
            idx++;
        }
        return(bubbles);
    }

    public void printScore() {
        Bukkit.broadcastMessage("The score is red:" + this.getRed() + ", blue:" + this.getBlue());
    }
}


